# 📊 Executive Report: Java 8 → 25 and Spring Boot 1.2 → 4.0 Upgrade  
  
**Project:** `download-server` (single-module Maven, Spring MVC file download service)  
**Date:** November 13, 2026 · **AWS Region:** ap-southeast-2 · **Baseline commit:** `73556be`  
  
---  
  
## 1. 🧭 Executive Summary  
  
AWS Transform Custom ran two managed transformations in sequence on `download-server`. `AWS/java-version-upgrade` moved the app from Java 8 to Java 25 and from Spring Boot 1.2.3 to 3.5.16. `AWS/spring-boot-version-upgrade` then moved it from Spring Boot 3.5.16 to 4.0.9. Both steps exited with code 0, and both final validation summaries report **OVERALL STATUS: COMPLETE**. The upgraded build passes `mvn clean verify` on Corretto JDK 25.0.5 with **12/12 tests passing**. The Java 8 baseline ran 0 tests because its Spock suite was never compiled. A runtime smoke test booted the app, and `/actuator/health` and `/download/{uuid}` both returned HTTP 200. All transitive CVEs found during the run were remediated.  
  
- ✅ **Outcome:** Upgrade succeeded. Build is green, tests pass, app boots on Java 25.  
- 🎯 **Final stack:** Java 25 · Spring Boot 4.0.9 · Groovy 5.0.8 · Spock 2.5  
- 🧪 **Test coverage:** 0 tests executed before → 12 tests executed and passing after  
- 🔐 **Security:** CVE scan of the resolved dependencies passed  
- ⏱️ **Run time:** about 20 minutes wall-clock (21:58 to 22:18), 128.35 agent minutes (54.82 + 73.53)  
  
---  
  
## 2. 🏗️ Application Changes  
  
| Area | Before | After |  
|---|---|---|  
| ☕ Java | 1.8 (`source`/`target`) | 25 (`maven.compiler.release`, bytecode major 69) |  
| 🌱 Spring Boot | 1.2.3.RELEASE | 4.0.9 (via 3.5.16) |  
| 🍃 Spring Framework | 4.1.6.RELEASE (pinned in pom) | Managed by the Boot 4 BOM (Framework 7) |  
| 🌐 Web starter | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |  
| 🐱 Embedded server | Tomcat (Boot 1.2) | Tomcat 11 (Boot 4 BOM) |  
| 🧩 Groovy | `groovy-all` 2.4.3 | `org.apache.groovy:groovy` 5.0.8 |  
| 🧪 Spock | 1.0-groovy-2.4 (never run) | 2.5-groovy-5.0 (12 tests run) |  
| 📦 Guava | 18.0 | 33.8.0-jre |  
| 📦 commons-io | 2.4 | 2.22.0 |  
| 📦 commons-codec | 1.10 | 1.23.0 |  
  
- 🗑️ Removed the unused `cglib-nodep` 3.1 dependency  
- 🔒 Removed the insecure plain-HTTP `repo.spring.io/milestone` repository  
- 🔁 Moved Spring module versions to BOM management instead of pinning them by hand  
- 🧪 Turned on the dormant Groovy/Spock test suite  
  
---  
  
## 3. 🛠️ Tools Used  
  
- ☕ **Java SDK:** Amazon Corretto 25.0.5 (target runtime). The Java 8 JDK (`/usr/lib/jvm/java-1.8.0`) was used for the baseline build.  
- 🤖 **AWS Transform Custom CLI (`atx`)**, AWS Managed transformations:  
  - 1️⃣ `AWS/java-version-upgrade`, conversation `20261113_215835_401696af`  
  - 2️⃣ `AWS/spring-boot-version-upgrade`, conversation `20261113_220818_9c79895d`  
  - ⚙️ Ran in `--non-interactive --trust-all-tools` mode, committing to staging branches `atx-result-staging-*` and leaving the original branch untouched  
  - 🧠 Applied learnings from earlier runs: Spock test execution, dependency version and CVE checks, Spring Boot/Java compatibility, Boot 4 smoke testing  
- 🧰 **Build tooling:** Apache Maven 3.9.16 and the Maven Wrapper (`./mvnw`)  
- 🔌 **Plugins added or updated:** maven-compiler-plugin 3.0 → 3.13.0, gmavenplus-plugin 5.1.0 (new), maven-dependency-plugin (new), maven-surefire-plugin (configured)  
- 🔍 **Verification:** Maven Central metadata lookups, CVE scanning of resolved coordinates, `javap` bytecode check, `curl` runtime smoke tests  
  
---  
  
## 4. 💻 Code Changes  
  
### 📄 Build configuration (`pom.xml`)  
- 🔧 Set `java.version` and `maven.compiler.release` to `25`, and switched the compiler to `<release>`  
- ⬆️ Bumped the parent from 1.2.3.RELEASE to 3.5.16, then to 4.0.9  
- 🧪 Added gmavenplus-plugin (`compileTests`, `targetBytecode 25`) and the surefire `**/*Spec.java` include  
- 🪝 Added a Mockito startup `-javaagent` (through maven-dependency-plugin `properties` and surefire `argLine`) because JDK 25 blocks Mockito's dynamic self-attach  
- 🛡️ Step 1 added BOM overrides `tomcat.version=10.1.61` and `jackson-bom.version=2.21.8` to fix 5 CRITICAL Tomcat CVEs and 3 HIGH Jackson CVEs  
- ♻️ Step 2 removed those overrides, because Boot 4 manages Tomcat 11 and Jackson 3 itself  
  
### ☕ Java sources  
- `FileSystemPointer.java`: replaced the removed Guava `Files.hash(...)` call with `Files.asByteSource(target).hash(...)`  
- `MainApplication.java`: removed dead code `new Integer("1234")`, which uses a deprecated-for-removal constructor  
- `Sha512ShallowEtagHeaderFilter.java`: changed the override from `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean)` to match the newer Spring API, with weak-ETag support  
  
### 🧪 Tests  
- `DownloadControllerTest.groovy`: replaced `@ContextConfiguration` with `@SpringBootTest(classes = [MainApplication])`  
- `FileExamples.java`: replaced random `UUID.randomUUID()` fixtures with fixed UUIDs so they stay stable across separate class initializations  
  
### 📝 Configuration and artifacts  
- `logback.xml`: replaced the nested unnamed appender with a named `CONSOLE` appender and an `appender-ref`  
- 📑 Added `code-intelligence-report.md`, `migration-plan.json`, `.migration-test-baseline.tsv`, `migration-results.yaml` and `validation_summary.md`  
  
### 📈 Change volume  
- Step 1: 7 files, +87 / −75 lines  
- Step 2: 6 files, +263 / −4 lines, mostly migration and verification artifacts  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
| Activity | Manual estimate |  
|---|---|  
| 🔎 Assess the upgrade path (Java 8→25, Boot 1.2→3→4, Groovy 2→5, Spock 1→2) | 0.5–1 day |  
| 📦 Rework dependencies and plugins, check version compatibility | 1 day |  
| 🧩 Fix API breaks (Guava, Spring filter API, test annotations, logback) | 0.5 day |  
| 🧪 Revive the dormant Spock suite (gmavenplus, Mockito agent on JDK 25, UUID fixtures) | 1 day |  
| 🛡️ Scan for CVEs and add BOM overrides | 0.5 day |  
| 🌱 Migrate Boot 3.5 → 4.0 and validate | 0.5–1 day |  
| ✅ Smoke test, validate, and write documentation | 0.5 day |  
| **Total manual effort** | **about 4.5–5.5 developer-days (36–44 hours)** |  
  
- 🤖 **Automated:** about 20 minutes wall-clock (128 agent minutes)  
- 💰 **Estimated saving:** roughly **35–40 engineer-hours**, over 90% less effort  
- ℹ️ The codebase is small (~700 LOC). The effort comes from the 11-year version gap across the toolchain, not from code volume. These figures are estimates based on scope, not measured.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation  
- 👀 Review and merge staging branches `atx-result-staging-20261113_215835_401696af` and `atx-result-staging-20261113_220818_9c79895d` (PR instructions are in each conversation's `artifacts/git_instructions.md`)  
- 🔁 Run `./mvnw -B clean verify` on JDK 25 in CI to confirm the 12/12 result outside the transform environment  
- 🌐 Run functional and regression tests in a staging environment, focusing on download, ETag/`If-None-Match`, and range or throttled streaming  
- 📊 Check that actuator endpoints and their exposure match production expectations, since Boot 1.x and Boot 4 defaults differ  
- 🔐 Run your organization's own SCA/CVE scanner on the final Boot 4.0.9 dependency tree  
  
### 💡 Improvements  
- 📦 Add `spring-boot-maven-plugin` so the build produces an executable fat jar (the smoke test had to call the plugin by its full coordinates)  
- 🧹 Remove explicitly declared Spring modules that the starters already bring in (`spring-beans`, `spring-core`, `spring-webmvc`, etc.)  
- 🏷️ Rename `DownloadControllerTest.groovy` to match its `DownloadControllerSpec` class, and add JUnit 5 tests for the Java classes  
- ⚙️ Review the Mockito `-javaagent` workaround when Mockito/Spock add native JDK 25 support  
- 🗂️ Decide whether to keep the migration artifacts (`migration-plan.json`, `.migration-test-baseline.tsv`, etc.) in the repo or move them to docs  
- 🔄 Enable Dependabot or Renovate so future Boot 4.x patches and CVE fixes are picked up automatically

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 22 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 0

## Kiro credit usage

💳 Kiro CLI credits used: 4.63  
Includes 0.31 for job setup (startup smoke test and knowledge-base indexing).  
