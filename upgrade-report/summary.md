# Executive Report — Java Version Upgrade Iteration 2  
  
**Project:** `com.nurkiewicz.download:download-server` (Spring Boot download-server)  
**Branch:** `e2e-737-mwf3opz366fa-upgrade-itr-2`  
**Transformation engine:** OpenRewrite + Kiro CLI (Amazon Q / Claude Sonnet 4.6)  
**Confidence score:** 88 / 100 (High)  
  
---  
  
## Executive Summary  
  
- ✅ The Java compilation target was successfully raised from Java 8 (`<source>1.8><target>1.8>`) to Java 17 (`<release>17>`) using the OpenRewrite `com.amazonaws.java.migrate.UpgradeToJava17` recipe, with Kiro CLI completing the remaining pom fixes.  
- ✅ The post-upgrade verification build passed — all 8 main-source files and 3 Java test-support files compiled cleanly under `javac --release 17`.  
- ⚠️ **A critical runtime warning was raised and must not be ignored:** the runtime-validation check detected that the effective Spring Framework version on the classpath is **4.1**, which is below the 5.3 minimum required for Java 17 compatibility. Spring Framework 4.1's bundled ASM library cannot parse Java 17 class files; **the application is expected to crash at startup during component scanning.** The Spring upgrade did not fully take and manual follow-up is required.  
- 🚫 Application startup behaviour is **unverified** — the smoke-start tier was skipped and the application was not booted.  
- ⚠️ Test execution was not recorded (no Surefire or Failsafe test totals in the verification log).  
- 🔄 This iteration resolves the compilation target change but is **incomplete** until the Spring Framework is upgraded to ≥ 5.3.  
  
---  
  
## Application Changes  
  
- 📄 **3 files changed** on the iteration branch: `pom.xml`, `src/main/java/com/nurkiewicz/download/MainApplication.java`, `summary.md`  
- 📦 **Dependencies upgraded** (2):  
  - `commons-codec:commons-codec` → **1.10 → 1.17.2**  
  - `org.apache.maven.plugins:maven-compiler-plugin` → **3.0 → 3.16.0**  
- ➕ **Dependencies added:** none  
- ➖ **Dependencies removed:** none (a duplicate declaration was cleaned up — see Code Changes)  
  
---  
  
## Tools Used  
  
- ☕ **Java SDK:** JDK 17 (`javac --release 17`, confirmed by post-upgrade-verification.log)  
- 🔧 **OpenRewrite:** `rewrite:6.42.0`, recipe `com.amazonaws.java.migrate.UpgradeToJava17`  
- 🤖 **Kiro CLI:** `kiro-cli 2.35.1`, model **Claude Sonnet 4.6** (`[kiro-retry] attempt 1/5 model: claude-sonnet-4.6` — succeeded on first attempt)  
  
---  
  
## Code Changes  
  
### `pom.xml`  
- ➕ Added `<java.version>17</java.version>` property — applied by `org.openrewrite.maven.UpdateMavenProjectPropertyJavaVersion`  
- 🔁 `maven-compiler-plugin` configuration changed from `<source>1.8><target>1.8>` to `<release>${java.version}>` (i.e. `--release 17`) — applied by `org.openrewrite.maven.UseMavenCompilerPluginReleaseConfiguration`  
- ⬆️ `maven-compiler-plugin` version: **3.0 → 3.16.0** — applied by `org.openrewrite.maven.UpgradePluginVersion`  
- ⬆️ `commons-codec:commons-codec` version: **1.10 → 1.17.2** — applied by `org.openrewrite.java.dependencies.UpgradeDependencyVersion`  
- 🧹 Stale `spring.version` property corrected: **4.1.1.RELEASE → 4.1.6.RELEASE** — applied by Kiro CLI (aligns the property with the version literals already in use by all Spring dependency declarations)  
- 🗑️ Duplicate `org.springframework:spring-test:4.1.6.RELEASE` (test scope) declaration removed — applied by Kiro CLI (Maven had flagged this as a model stability warning)  
  
### `src/main/java/com/nurkiewicz/download/MainApplication.java`  
- 🔁 `new Integer("1234")` → `Integer.valueOf("1234")` — applied by `org.openrewrite.staticanalysis.PrimitiveWrapperClassConstructorToValueOf` (the `Integer(String)` constructor was deprecated in Java 9 and removed in Java 17)  
  
### `summary.md`  
- ➕ New file added by Kiro CLI — documents upgrade findings and next steps for the iteration record  
  
---  
  
## Risks  
  
- 🔴 **Critical — Spring Framework 4.1 on a Java 17 build (runtime-validation.log WARNING):**  
  > *"WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 17 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required."*  
  
  Spring Framework 4.1 bundles ASM 5.x, which predates Java 17 bytecode support (ASM 9.x, introduced in Spring 5.3). Although the application compiles, the Spring container will fail to scan and register components at runtime. **This is a confirmed risk, not a false positive.** The fix requires upgrading the Spring Boot parent from `1.2.3.RELEASE` to a Java-17-compatible line (minimum Spring Boot 2.7.x, which provides Spring Framework 5.3).  
  
- 🔴 **Application startup unverified:**  
  > *"Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)."*  
  
  No application boot was attempted. Runtime behaviour, including bean wiring, endpoint availability, and ETag/download logic, is unconfirmed.  
  
- 🟡 **Test execution not recorded:** The Surefire phase ran but produced no test totals in the verification log. The Groovy/Spock test file (`DownloadControllerSpec.groovy`, Spock 1.0 / Groovy 2.4) is not compiled or executed — Groovy 2.4 is incompatible with Java 17 and no Groovy compiler plugin (`gmavenplus-plugin`) is configured. Test coverage of the download and ETag logic is therefore absent.  
  
- 🟡 **Spring Boot 1.2.3.RELEASE:** The parent POM remains on a 2015 release that bundles Tomcat 8.0.x. Even independent of the Spring Framework ASM issue, Tomcat 8.0.x does not support Java 17 at runtime. A Spring Boot upgrade is required before any deployment.  
  
- 🟢 **`commons-codec` upgrade (1.10 → 1.17.2):** This is a safe patch/minor upgrade within the same javax-compatible line. No API breakage expected.  
  
- 🟢 **`maven-compiler-plugin` upgrade (3.0 → 3.16.0):** Standard build tooling upgrade; no effect on compiled bytecode semantics beyond adopting the `--release` flag.  
  
---  
  
## Time Savings  
  
- ⏱️ **Transformation duration:** 3 minute(s)  
- 💰 **Estimated time saved vs. manual upgrade:** 45 minute(s)  
  
---  
  
## Next Steps  
  
### 🔴 Must-do before this upgrade is usable  
- 🔧 **Upgrade Spring Boot parent to 2.7.x** (e.g. `2.7.18`) — this pulls in Spring Framework 5.3.x, which bundles ASM 9.x and can read Java 17 class files. This is the minimum fix to resolve the critical runtime warning. Alternatively, upgrade directly to Spring Boot 3.3.x (Jakarta namespace) if a full jakarta migration is planned.  
- 🧪 **Enable and run the smoke-start tier** (`export ENABLE_RUNTIME_VALIDATION=true`) after the Spring upgrade to confirm the application boots and component scanning succeeds.  
- ✅ **Validate build after Spring Boot upgrade** — re-run `mvn clean install` and confirm zero errors after the parent POM change.  
  
### 🟡 Should-do in a following iteration  
- 🧪 **Restore Groovy/Spock test execution:**  
  - Upgrade `groovy-all` to `org.apache.groovy:groovy-all:4.0.x`  
  - Upgrade `spock-core` and `spock-spring` to `2.3-groovy-4.0`  
  - Add `org.codehaus.gmavenplus:gmavenplus-plugin:3.x` to the build  
  - Configure Surefire to include `**/*Spec.class`  
- 🔁 **Migrate Guava `Optional` usages** in `ExistingFile.toMediaType()` to `java.util.Optional` now that the target is Java 17.  
- 🔁 **Review `Hashing.sha512()` deprecation** in `FileSystemPointer` and `Sha512ShallowEtagHeaderFilter` — deprecated since Guava 25.1 (project is on 18.0); consider upgrading Guava.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 3 minute(s)
- ⏳ Estimated time saved: 45 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - ⚠️ WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 17 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required.
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 5.41  
Includes 0.47 for job setup (startup smoke test and knowledge-base indexing).  
