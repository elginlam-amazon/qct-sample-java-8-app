# Java Version Upgrade — Executive Report  
**Project:** `com.nurkiewicz.download:download-server` | **Date:** 2026-11-19 | **Iteration:** 20  
  
---  
  
## 1. 🏆 Executive Summary  
  
The `download-server` application was successfully modernised from **Spring Boot 1.2.3.RELEASE / Java 8** to **Spring Boot 4.0.9 (Spring Framework 7.0.10) / Java 25**. The upgrade was completed in a single automated pipeline combining two OpenRewrite recipe passes and an AI-assisted build-fix pass. The final build exits with **BUILD SUCCESS — 12 tests run, 0 failures, 0 errors, 0 skipped**. No business logic was altered; all HTTP download, ETag caching, If-Modified-Since, HEAD, and 404 flows verified green by the existing Spock specification suite.  
  
---  
  
## 2. 📦 Application Changes  
  
- **Artifact:** `download-server 0.0.1-SNAPSHOT` — a Spring MVC file-download service with ETag caching, rate-limited streaming, and Spring Boot Actuator  
- **Source baseline:** Spring Boot `1.2.3.RELEASE`, Java source/target `1.8`, `maven-compiler-plugin:3.0`  
- **Target baseline:** Spring Boot `4.0.9` (Spring Framework `7.0.10`, Jakarta EE 11), Java `25`, `maven-compiler-plugin:3.16.0`  
- 🔄 Build system retargeted from `<source>/<target>` flags to the modern `<release>25</release>` configuration  
- 🔄 Starter rename applied: `spring-boot-starter-web` → `spring-boot-starter-webmvc` (Spring Boot 4.0 modular starter)  
- 🔄 `jakarta.servlet-api` added as a test-scoped dependency (Jakarta EE 11 namespace migration)  
- 🔄 Explicit version pins removed for starters now managed by the Spring Boot 4.0.9 parent BOM  
- 🔄 Test framework stack fully replaced (see Code Changes section)  
  
---  
  
## 3. 🛠️ Tools Used  
  
| Tool | Version / Detail |  
|---|---|  
| ☕ **Target JDK** | Java 25 (OpenJDK 64-Bit Server VM) |  
| 🔁 **OpenRewrite** | `rewrite:6.42.0` via Maven plugin |  
| 📋 **Java upgrade recipe** | `com.amazonaws.java.migrate.UpgradeToJava25` (cumulative: Java 8 → 11 → 17 → 21 → 25) |  
| 🌱 **Spring Boot upgrade recipe** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (cumulative: Boot 1.2 → 2.0 → … → 3.5 → 4.0) |  
| 🤖 **Claude CLI** | Claude Code `v2.1.266` |  
| 🧠 **Claude model** | `global.anthropic.claude-sonnet-4-6` |  
| 🏗️ **Build system** | Apache Maven (Surefire `3.5.6`, Dependency Plugin `3.9.0`) |  
  
---  
  
## 4. 🔧 Code Changes  
  
### OpenRewrite — Java 25 recipe (`pom.xml`, `MainApplication.java`)  
- ✅ `<java.version>25</java.version>` property added; `maven-compiler-plugin` updated `3.0 → 3.16.0`; `<source>/<target>1.8</source>` replaced with `<release>${java.version}</release>`  
- ✅ `maven-dependency-plugin` and `maven-surefire-plugin` (ByteBuddy agent argLine) added by `AddMockitoJavaAgentToMavenSurefirePlugin`  
- ✅ `MainApplication.java` — `new Integer("1234")` → `Integer.valueOf("1234")` (deprecated constructor removal via `PrimitiveWrapperClassConstructorToValueOf`)  
  
### OpenRewrite — Spring Boot 4.0 recipe (`pom.xml`, `DownloadController.java`)  
- ✅ Parent bumped `spring-boot-starter-parent` `1.2.3.RELEASE → 4.0.9` (traversing every minor: Boot 2.0 → 2.7 → 3.0 → 3.5 → 4.0)  
- ✅ `spring-boot-starter-web` → `spring-boot-starter-webmvc` (`ChangeDependency` for Spring Boot 4.0 starter split)  
- ✅ `jakarta.servlet-api` added (`JavaxServletToJakartaServlet` / `AddDependency`)  
- ✅ Explicit version overrides removed for starters now BOM-managed  
- ✅ JUnit 4 exclusions cleaned (`RemoveExclusion`, `ExcludeJUnit4UnlessUsingTestcontainers`)  
- ✅ `DownloadController.java` — `@Autowired` removed from single-arg constructor (`NoAutowiredOnConstructor` best-practices recipe)  
  
### Claude CLI manual fixes (`Sha512ShallowEtagHeaderFilter.java`, `pom.xml`)  
  
**`src/test/java/…/Sha512ShallowEtagHeaderFilter.java`**  
- 🔨 `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`  
  — Spring Framework 7 (`spring-web 7.0.10`) changed the protected hook signature in `ShallowEtagHeaderFilter`; the old `byte[]` overload no longer exists  
  
**`pom.xml` — Groovy / Spock / GMavenPlus stack**  
- 🔨 `org.codehaus.groovy:groovy-all:2.4.16` (JAR) → `org.apache.groovy:groovy-all:5.0.8` (POM/BOM) — groupId migrated to Apache Groovy, `groovy-all` is now a dependency-aggregating BOM  
- 🔨 `spock-core` + `spock-spring` `1.0-groovy-2.4` → `2.5-groovy-5.0` — Spock 1.x ran on JUnit 4 (incompatible with Boot 4 / JUnit 6); Spock 2.5 uses JUnit Platform, compatible with the JUnit `6.0.3` pulled transitively by Spring Boot 4.0.9  
- 🔨 Added `gmavenplus-plugin:4.3.1` with goal `compileTests` and `<targetBytecode>25</targetBytecode>` — `maven-compiler-plugin` does not compile Groovy; GMavenPlus is required  
- 🔨 Added `**/*Spec.groovy` include to Surefire — `DownloadControllerSpec` did not match the default `*Test` include pattern and was silently skipped  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
| Activity | OpenRewrite estimate | Manual developer estimate |  
|---|---|---|  
| Java 8 → 25 build-system migration | 1h *(log line: "Estimate time saved: 1h")* | 3–5 h |  
| Spring Boot 1.2 → 4.0 recipe chain | 2h 32m *(log line: "Estimate time saved: 2h 32m")* | 8–12 h |  
| Spring Framework 7 API fix (`ShallowEtagHeaderFilter`) | — | 1–2 h (research + fix) |  
| Groovy 2.4 → 5.0 / Spock 1.0 → 2.5 / GMavenPlus setup | — | 3–5 h (version research, BOM type discovery, Surefire include fix) |  
| **Total automated saving** | **~3h 32m (OpenRewrite)** | **~15–24 h manual** |  
  
> 💡 The automated pipeline replaced an estimated **2–3 developer-days** of tedious dependency resolution, API migration, and test-framework wiring work.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation (required before production)  
- 👀 Review and merge staging branch `atx-result-staging-20261118_183031_5fc233b0` via PR (see `git_instructions.md` in ATX artifacts)  
- 🧪 Run the full test suite in CI on a **Corretto 25** image to validate outside the local NFS build environment  
- 🌐 Deploy to staging and exercise all endpoints: `GET /download/{uuid}/{file}` (200), `GET /download/{uuid}` (301), ETag → 304, If-Modified-Since → 304, HEAD → 200 no-body, missing UUID → 404, `/actuator/health` → 200  
- 📡 Wire external monitoring/alerting to `/actuator/health`  
- 🔒 Run a dependency CVE scan (OWASP Dependency-Check or Amazon Inspector) — `commons-io:2.4` and `guava:29.0-jre` both carry known CVEs and should be upgraded  
  
### 💡 Recommendations (follow-up iterations)  
- 🧹 Re-enable **SpotBugs and Checkstyle** (skipped by upgrade build) and resolve any findings on the upgraded codebase  
- 🧪 Expand the test suite beyond the single Spock spec (`DownloadControllerSpec`); add exception/error-path assertions (currently 0 negative-path tests)  
- ⬆️ Upgrade `commons-io:2.4 → 2.17+` and `guava:29.0-jre → 33+` to clear CVE findings and remove unsupported old versions  
- 🔁 For a future move to **Spring Boot 4.1**, re-run the pre-4.1 deprecation gate and property migration steps against this branch  
- 🤖 Add **Dependabot or Renovate** to track Spring Boot 4.0.x patch releases automatically  
- 🏷️ Align pipeline job metadata: source Java listed as `1.6` in job config but original `pom.xml` compiled at `1.8` — correct the config for future runs  
- ⚠️ Note: stale property `<spring.version>4.1.1.RELEASE</spring.version>` remains in `pom.xml` — it is not used by any active dependency (versions are BOM-managed) but should be removed to avoid confusion

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 7 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 2.651563  
