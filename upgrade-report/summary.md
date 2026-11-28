# 📋 Executive Report: Java 25 Upgrade, Iteration 37  
  
## 🧭 Executive summary  
- 🎯 The `download-server` Maven project moved from Java 8 to **Java 25** (`<release>25</release>`), and from Spring Boot 1.2.3.RELEASE to **Spring Boot 4.0.10** (`spring-boot-starter-parent`).  
- 🛠️ Two OpenRewrite recipes did the first pass: `com.amazonaws.java.migrate.UpgradeToJava25` and `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. The Kiro CLI build-fix passes then fixed the one remaining test compilation error, enabled the Spock test suite, upgraded libraries and cleaned up the build.  
- ✅ The post-upgrade verification build ended in **BUILD SUCCESS**, with **12 tests run** (0 failures, 0 errors, 0 skipped) in `DownloadControllerSpec`.  
- 🌱 Spring: the runtime check reports "OK (static)". The Spring Boot 4.0.10 parent manages the Spring Framework version, because no build file pins one. No old Spring version is reported anywhere.  
- ⚠️ **Runtime behaviour is unverified.** The smoke-start tier was skipped, so the application was not started outside the tests. The check needs `ENABLE_RUNTIME_VALIDATION=true`.  
- 🧪 No before/after test comparison is possible. 0 test classes ran before the upgrade and 1 ran after, because the Spock spec was never compiled on the original build.  
- 🌿 The changes are committed on branch `main-upgrade-itr-37` as commit `80ea78e`. It changes 7 files, with 111 insertions and 61 deletions.  
  
## 🧩 Application changes  
- 📦 **Build (`pom.xml`)**  
  - The Java version, Spring Boot parent, compiler plugin and dependency versions were upgraded.  
  - Explicit Spring versions were removed, so the Boot BOM now manages them.  
  - Groovy and Spock tests were enabled.  
  - Unused and stale entries were removed.  
- ☕ **Application code**: `DownloadController.java`, `FileSystemPointer.java` and `MainApplication.java`.  
- 📝 **Configuration**: `src/main/resources/logback.xml`.  
- 🧪 **Test support code**: `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`.  
- 📄 **Documentation**: `summary.md` (an in-repository upgrade summary).  
  
### ➕ Dependencies added  
| Dependency | Version | Scope (build-file diff) |  
|---|---|---|  
| `org.springframework.boot:spring-boot-starter-webmvc` | LATEST (managed by Boot parent 4.0.10) | compile (default) ⚠️ |  
| `jakarta.servlet:jakarta.servlet-api` | LATEST (managed by Boot parent 4.0.10) | test |  
| `org.apache.groovy:groovy` | LATEST (managed by Boot parent 4.0.10) | test |  
| `org.codehaus.gmavenplus:gmavenplus-plugin` | `${gmavenplus.version}` (= 5.1.0) | build plugin |  
| `org.apache.maven.plugins:maven-dependency-plugin` | LATEST (managed by Boot parent) | build plugin |  
| `org.apache.maven.plugins:maven-surefire-plugin` | LATEST (managed by Boot parent) | build plugin |  
  
- ⚠️ `spring-boot-starter-webmvc` is in compile scope, so it packages the web stack, including the embedded servlet container that the starter pulls in, with the application. On an external servlet container or application server this is a classloader-conflict risk. See Risks.  
- ✅ `jakarta.servlet-api` is test-scoped, so it is not packaged with the application.  
  
### ➖ Dependencies removed  
- 🗑️ `org.springframework.boot:spring-boot-starter-web` 1.2.3.RELEASE, replaced by `spring-boot-starter-webmvc` through OpenRewrite `ChangeDependency`.  
- 🗑️ `cglib:cglib-nodep` 3.1 (unused).  
- 🗑️ `org.codehaus.groovy:groovy-all` 2.4.3, replaced by test-scoped `org.apache.groovy:groovy`.  
  
### ⬆️ Dependencies upgraded  
- 🌱 `org.springframework.boot:spring-boot-starter-parent` 1.2.3.RELEASE → **4.0.10**  
- 🌱 `org.springframework.boot:spring-boot-starter-actuator` 1.2.3.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework.boot:spring-boot-starter-test` 1.2.3.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-test` 4.1.6.RELEASE → LATEST (BOM-managed). The duplicate `spring-test` declaration was also removed.  
- 🌱 `org.springframework:spring-beans` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-context` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-context-support` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-webmvc` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-web` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-aop` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-core` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🌱 `org.springframework:spring-expression` 4.1.6.RELEASE → LATEST (BOM-managed)  
- 🧪 `org.spockframework:spock-core` 1.0-groovy-2.4 → `${spock.version}` (= 2.5-groovy-5.0)  
- 🧪 `org.spockframework:spock-spring` 1.0-groovy-2.4 → `${spock.version}` (= 2.5-groovy-5.0)  
- 📚 `commons-io:commons-io` 2.4 → **2.22.0**  
- 📚 `commons-codec:commons-codec` 1.10 → LATEST (BOM-managed)  
- 📚 `com.google.guava:guava` 18.0 → **33.8.0-jre**  
- 🔧 `org.apache.maven.plugins:maven-compiler-plugin` 3.0 → **3.16.0**  
  
## 🧰 Tools used  
- ☕ **Java SDK**: JDK 25. Main and test sources were compiled with `javac [... release 25]`.  
- 🔄 **OpenRewrite**: `rewrite-maven-plugin` 6.42.0, with these recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which changed `pom.xml` and `MainApplication.java`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which changed `pom.xml` and `DownloadController.java`. It includes the Spring Boot 2.x/3.x chain, the Spring Framework 5.x/6.0 steps and the JakartaEE10 migration.  
- 🤖 **Amazon Kiro CLI**: `kiro-cli 2.35.1` with model **claude-opus-5.5**. It ran the build-fix pass, the additional carry-over prompt pass and a final rebuild pass. Each pass succeeded on its first attempt.  
  
## 💻 Code changes (from the branch diff)  
- 🔢 **`MainApplication.java`** (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`):  
  - `new Integer("1234")` → `Integer.valueOf("1234")`  
- 🧹 **`DownloadController.java`** (OpenRewrite `NoAutowiredOnConstructor`):  
  - Removed `@Autowired` from the single constructor, and its `org.springframework.beans.factory.annotation.Autowired` import.  
- #️⃣ **`FileSystemPointer.java`** (Kiro, replaces a deprecated Guava API):  
  - `Files.hash(target, Hashing.sha512())` → `Files.asByteSource(target).hash(Hashing.sha512())`  
- 🏷️ **`Sha512ShallowEtagHeaderFilter.java`** (Kiro, Spring 7 signature change):  
  - `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`.  
  - The body hashes `inputStream.readAllBytes()` with SHA-512 and adds the `W/` prefix when `isWeak` is true.  
  - `java.io.IOException` and `java.io.InputStream` imports were added.  
- 📝 **`logback.xml`** (Kiro):  
  - The anonymous inline `<appender>` under `<root>` became a named `<appender name="CONSOLE">`, which `<root>` now references with `<appender-ref ref="CONSOLE"/>`.  
  - The pattern and logger levels are unchanged.  
- 📦 **`pom.xml`**:  
  - Added `<java.version>25</java.version>` and `<release>${java.version}</release>`.  
  - Added the `spock.version` and `gmavenplus.version` properties.  
  - Added the `gmavenplus-plugin` (`addTestSources`, `compileTests`) and the `maven-dependency-plugin` `properties` goal.  
  - Configured Surefire with the `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` argLine and includes `**/*Test`, `**/*Tests` and `**/*Spec`.  
  - Removed the `spring.version` property (4.1.1.RELEASE) and the `http://repo.spring.io/milestone/` repository.  
  - Made the dependency changes listed above.  
  
## ⚠️ Risks  
- 🚫 **Runtime unverified.** Runtime validation reported "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started, so its startup and runtime behaviour outside the MockMvc tests is unverified.  
- ℹ️ **Static Spring check only.** Runtime validation reported "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)."  
  - This is a static check, not a runtime check.  
- 🌱 **Plain Spring artifacts.** `code-upgrade.log` noted that plain (non-Boot) Spring artifacts were detected alongside the Spring Boot path.  
  - They are single-module and declared without versions, so the Boot 4.0.10 parent manages them.  
  - Consider whether the explicit `spring-*` declarations are still needed, given that the starters already bring them in.  
- 🧩 **Classloader conflict.** `spring-boot-starter-webmvc` is compile-scoped and packages the web stack and the embedded servlet container with the application.  
  - On an external servlet container or application server that already provides the Servlet API, this can cause classloader conflicts.  
- 📦 **Packaging.** The verification build produced a plain jar (`download-server-0.0.1-SNAPSHOT.jar` from `maven-jar-plugin`), and the diff adds no `spring-boot-maven-plugin`.  
  - The deployment artifact may therefore not be an executable Boot jar.  
- 🧪 **Test coverage.**  
  - **Narrow suite.** The 12 tests are all MockMvc tests in one spec, `DownloadControllerSpec`.  
  - **No baseline.** No tests ran before the upgrade, so there is no before/after parity baseline.  
  - **Test-only helper.** The changed `Sha512ShallowEtagHeaderFilter` is test code, and nothing in the diff shows it registered as a filter.  
- 🔁 **Major version jumps.** Spring Boot moved from 1.2 to 4.0, Spring Framework from 4.1 to the Boot-managed 7.x line, Spock from 1.0 to 2.5, Groovy from 2.4 to 5 and Guava from 18 to 33.  
  - Behaviour differences the tests don't cover may surface, for example in actuator endpoints and defaults, which no test exercises.  
- 🏷️ **ETag computation.** ETags are now computed through `Files.asByteSource(...).hash(...)` and, in test code, from an `InputStream`.  
  - The SHA-512 algorithm and the quoted format are unchanged in the diff. The ETag tests in `DownloadControllerSpec` pass.  
- 📝 **Logging config.** The `logback.xml` structure changed. The levels are unchanged, but `com.nurkiewicz` still logs at `ALL`, which is noisy for production.  
- 🌐 **Unreachable repositories.** During OpenRewrite, `http://repo.spring.io/milestone/` and `https://repository.apache.org/snapshots` could not be reached.  
  - The milestone repository has since been removed from `pom.xml`.  
  
## ⏱️ Time savings  
- ⚡ Transformation duration: **6 minute(s)**.  
- 💰 Estimated time saved: **300 minute(s)**.  
  
## 🚀 Next steps  
- ▶️ Run the smoke-start tier with `export ENABLE_RUNTIME_VALIDATION=true`, or start the application in a test environment, to verify startup and runtime behaviour.  
- 📦 Confirm the deployment target.  
  - **Executable jar with embedded container:** add `spring-boot-maven-plugin` (version managed by the parent).  
  - **External servlet container:** switch to WAR packaging, mark the embedded container `provided` and add a `SpringBootServletInitializer`.  
- 🧪 Extend test coverage beyond `DownloadControllerSpec`. Cover actuator endpoints and the `FileSystemStorage` (non-test profile) path. Consider a test that registers `Sha512ShallowEtagHeaderFilter`.  
- 🧹 Review whether the explicit `org.springframework:spring-*` dependencies can be dropped in favour of the Boot starters, so there are fewer declarations to maintain.  
- 📝 Add a quieter `logback-test.xml` and environment-specific log levels for dev, test and prod.  
- 🔐 Run a dependency vulnerability scan on the upgraded set, especially the unmanaged `guava` 33.8.0-jre and `commons-io` 2.22.0.  
- 👀 Review commit `80ea78e` on branch `main-upgrade-itr-37` against the base commit `73556be` before raising a pull request.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 6 minute(s)
- ⏳ Estimated time saved: 300 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 18.36  
Includes 1.19 for job setup (startup smoke test and knowledge-base indexing).  
