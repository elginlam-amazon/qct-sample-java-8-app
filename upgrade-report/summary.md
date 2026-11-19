# ☕ Java Version Upgrade: Executive Report  
  
**Project:** `download-server` (`/data/code`) 📦  
**Transformation:** `AWS/java-version-upgrade` 🔄  
**Date:** November 19, 2026 📅  
**Outcome:** ✅ **COMPLETE**. All exit criteria passed.  
  
---  
  
## 1. 📋 Executive Summary  
  
AWS Transform Custom CLI moved the `download-server` Spring Boot application from a legacy Java baseline to **Java 17**. The job was configured with Java 1.6 as the source version, but the original `pom.xml` actually compiled at `source/target 1.8`. The application went from **Spring Boot 1.2.3 / Spring 4.1.6** to **Spring Boot 3.5.16 / Spring Framework 6.2.19** on Jakarta EE. The upgrade also turned on the Spock test suite, which had never run before. The project now builds cleanly on **JDK 17 and JDK 25**, and **12 of 12 tests pass** (the baseline ran 0 tests). The work took **42.73 agent minutes**, changed **7 files** in **2 commits**, and left the working tree clean. ⚠️ Two CRITICAL CVEs in `spring-webmvc` 6.2.19 are still open, because they are fixed only in Spring Framework 7 / Spring Boot 4. They are documented and deferred.  
  
---  
  
## 2. 🏗️ Application Changes  
  
- 🎯 **Java target:** `source/target 1.8` → `<release>17</release>`, with `java.version=17`  
- 🌱 **Framework:** Spring Boot 1.2.3.RELEASE → **3.5.16**. Spring Framework 4.1.6 → **6.2.19**, now managed by the BOM.  
- 🧩 **Platform:** `javax` → **Jakarta EE 10**, brought in through Spring Boot 3. The code had no `javax.*` imports to migrate.  
- 🧪 **Testing:** The Spock spec was never compiled or run before. It is now compiled and runs **12 tests** that pass.  
- 🔐 **Security:** CVE-driven version overrides for Jackson, Tomcat and Log4j were applied and documented.  
- 🌐 **Build hygiene:** Removed the plain-HTTP `repo.spring.io/milestone` repository, which Maven 3.9+ blocks. Everything now resolves from Maven Central.  
- 📝 **Documentation:** Added a "Java 17 upgrade notes" section to `README.md` with a table of deviations and deferred items.  
  
---  
  
## 3. 🛠️ Tools Used  
  
- ☁️ **AWS Transform Custom CLI (`atx`)**  
  - 🔧 Command: `atx custom def exec --transformation-name AWS/java-version-upgrade --non-interactive --trust-all-tools`  
  - 🌏 Region: `ap-southeast-2`  
  - 🏢 AWS Managed transformation, which applied learnings from earlier runs (dependency verification, Spock/Groovy test execution, Spring Boot/Java compatibility)  
  - 🌿 Committed to its own branch, `atx-result-staging-20261119_195145_a3543163`. The original branch was left untouched.  
- ☕ **Java SDKs (Amazon Corretto, aarch64)**  
  - 🕰️ **JDK 8** (`java-1.8.0-amazon-corretto`): baseline build of commit `73556be`  
  - 🎯 **JDK 17** (`java-17-amazon-corretto`): target runtime, used for the build, tests and app boot check  
  - 🚀 **JDK 25** (25.0.5.1, default): forward-compatibility build and tests  
- 📦 **Apache Maven 3.9.16**: `mvn -B clean install -Dspotbugs.skip=true -Dcheckstyle.skip=true`  
- 🔍 **Supporting checks:** Maven Central metadata lookups for versions, `mvn dependency:list` for the CVE scan, and `curl` against `/actuator/health`  
  
---  
  
## 4. 💻 Code Changes  
  
**7 files changed:** +87 / −65 lines, 152 lines modified in total  
  
### 📄 `pom.xml`  
- ⬆️ `maven-compiler-plugin` 3.0 → **3.13.0**, now using `<release>17</release>`  
- ⬆️ `spring-boot-starter-parent` 1.2.3.RELEASE → **3.5.16**. The mandated version was 3.2.12; see the deviations below.  
- ✂️ Removed the hard-coded Spring 4.1.6 and Boot 1.2.3 versions so the BOM manages them, and dropped the `spring-test` exclusions and the duplicate dependency.  
- 🔐 CVE overrides: `jackson-bom.version` **2.21.8**, `tomcat.version` **10.1.62**, `log4j2.version` **2.25.5**  
- 📚 Library upgrades:  
  - guava 18.0 → **33.8.0-jre**  
  - commons-io 2.4 → **2.22.0**  
  - commons-codec 1.10 → **1.23.0**  
  - cglib-nodep 3.1 → **3.3.0**  
- 🟢 `org.codehaus.groovy:groovy-all` 2.4.3 → `org.apache.groovy:groovy` **5.0.8**  
- 🧪 Spock `1.0-groovy-2.4` → **`2.5-groovy-5.0`** (spock-core and spock-spring)  
- ➕ Added `gmavenplus-plugin` **5.1.0** (`addTestSources`, `compileTests`, `targetBytecode 17`)  
- ➕ Added surefire includes for `**/*Test.java`, `**/*Tests.java` and `**/*Spec.java`  
- 🚫 Removed the plain-HTTP milestone repository  
  
### ☕ `MainApplication.java`  
- 🔁 Replaced the deprecated `new Integer("1234")` with `Integer.valueOf("1234")`  
  
### 🏷️ `Sha512ShallowEtagHeaderFilter.java` (test)  
- 🔁 Changed the override from `generateETagHeaderValue(byte[])` to the Spring 6 signature `generateETagHeaderValue(InputStream, boolean)`  
- ✅ Weak ETags keep their `W/` prefix, and the method uses `inputStream.readAllBytes()`  
  
### 🧪 `DownloadControllerTest.groovy`  
- 🔁 Replaced `@ContextConfiguration(classes = [MainApplication])` with `@SpringBootTest(classes = [MainApplication])`  
  
### 🆔 `FileExamples.java` (test)  
- 🔁 Replaced the `UUID.randomUUID()` fixtures with fixed `UUID.fromString(...)` values, so the fixtures stay the same across separate class initializations  
  
### 🪵 `logback.xml`  
- 🔁 Moved the unnamed inline appender to a named `CONSOLE` appender referenced with `<appender-ref>`  
  
### 📝 `README.md`  
- ➕ Added a table of deviations and deferred items covering Spring Boot, Jackson, Tomcat, Log4j, Groovy/Spock, the `spring-webmvc` CVEs, Lombok/Gradle and the Jakarta APIs  
  
### ⚖️ Deviations from Mandated Versions  
| Item | Mandated | Chosen | Reason |  
|------|----------|--------|--------|  
| 🌱 Spring Boot | 3.2.12 | 3.5.16 | Spring 6.1.15 carries CVE-2025-41249 (HIGH), which has no fix in the 6.1 line |  
| 📊 Jackson | 2.15.2 | 2.21.8 | HIGH CVEs, for example CVE-2026-91777 |  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
- 🤖 **Automated effort:** about **43 minutes** (42.73 agent minutes)  
- 👩‍💻 **Estimated manual effort:** about **3–5 developer days** (24–40 hours)  
  - 🔍 Discovery, baseline build, and finding out why tests never ran: about 4–6 h  
  - 🌱 Spring Boot 1.x → 3.x migration (BOM cleanup, Spring 6 API changes, Jakarta): about 8–12 h  
  - 🧪 Getting the Groovy 5 / Spock 2.5 / gmavenplus toolchain to work on JDK 17 and 25: about 4–8 h  
  - 🔐 Version research, CVE scanning and override selection: about 4–8 h  
  - ✅ Multi-JDK validation, app boot check and documentation: about 4–6 h  
- 💰 **Estimated savings:** about **95%+** of the engineering time, roughly **23–39 hours**  
- ℹ️ The codebase is small (a single module with a handful of Java and Groovy files), but the two-major-version framework jump and the toolchain repair make up most of the manual effort. These figures are an estimate based on scope, not measured data.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation  
- 👀 Review the `atx-result-staging-20261119_195145_a3543163` branch (commits `db178c1` and `3eb3c22`) and open a PR using the generated `git_instructions.md`.  
- 🏗️ Run the build in CI on **JDK 17**: `mvn -B clean install`  
- 🧪 Check that all **12 Spock tests** pass in CI. Add the test reports to the pipeline, because the inventory captured before the upgrade was empty.  
- 🌐 Smoke-test in a staging environment: `/actuator/health` should report `UP`, the download endpoint should return `200`, and ETag / weak-ETag headers should behave as before.  
- 🔎 Check whether any clients or monitoring depend on Spring Boot 1.x actuator paths. Boot 3 serves them under `/actuator/*` (for example `/health` → `/actuator/health`).  
- 🪵 Confirm that log output format and levels are unchanged after the `logback.xml` restructure. The root logger is still at `DEBUG`.  
  
### 💡 Improvement Recommendations  
- 🚨 **Plan the Spring Boot 4 / Spring Framework 7 upgrade** to fix CVE-2026-47884 and CVE-2026-47890 (CRITICAL, `spring-webmvc` 6.2.19). Until then, make sure `XsltView` and SSE fragment rendering stay unused.  
- 🔁 Remove the manual `jackson`, `tomcat` and `log4j2` overrides once a later Boot 3.5.x BOM includes the fixed versions.  
- 🧹 Remove `cglib-nodep`, which is unused, and move `groovy` to `test` scope if production code doesn't need it.  
- 🤖 Add automated dependency and CVE scanning to CI, for example OWASP Dependency-Check or Dependabot.  
- 🧪 Add unit and integration test coverage beyond the single controller spec.  
- 🆙 Consider targeting **Java 21 LTS**. The build already passes on JDK 25.  
- 🔧 Remove the unused `-Dspotbugs.skip` / `-Dcheckstyle.skip` flags, or configure those plugins properly for static analysis.  
  
---  
  
*📎 Source: `/data/iteration-2/code-upgrade.log`. Detailed validation is in the atx artifact `validation_summary.md`.*

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 9 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 0

## Kiro credit usage

💳 Kiro CLI credits used: 3.03  
Includes 0.32 for job setup (startup smoke test and knowledge-base indexing).  
