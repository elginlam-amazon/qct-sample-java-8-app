# 🚀 Java Version Upgrade: Executive Report  
  
**Project:** `download-server` (`/data/code`) · **Transformation:** `AWS/java-version-upgrade` · **Date:** 2026-11-19 · **Region:** ap-southeast-2  
  
---  
  
## 📋 1. Executive Summary  
  
The `download-server` application moved from Java 8 bytecode and Spring Boot 1.2.3 to **Java 17** on **Spring Boot 3.5.16**, using the AWS Transform Custom CLI in non-interactive mode. The transformation finished with ✅ **OVERALL STATUS: COMPLETE**. The build passes on JDK 17 and JDK 25, the compiled classes are Java 17 bytecode (major version 61), and **12 of 12 tests pass**. Before the upgrade, the Spock test suite was never compiled, so 0 tests ran. The application was started on JDK 17 and both `/actuator/health` and the download endpoint returned HTTP 200. One known risk remains: two CRITICAL CVEs in `spring-webmvc` 6.2.19 that are fixed only in Spring Framework 7.x.  
  
- ⏱️ **Run time:** about 9 minutes wall-clock (17:39 to 17:48), 54.41 agent minutes billed  
- 📁 **Scope:** 7 files changed, 89 lines added, 64 deleted  
- 🌿 **Branch:** commits are on `atx-result-staging-20261119_173914_20b4ec3a`. The original branch `ec2-jobrole-mwnu2wv3-atx-upgrade` was left unchanged.  
- 🔢 **Commits:** 5 incremental commits, each with a passing build  
  
---  
  
## 🏗️ 2. Application Changes  
  
- ☕ **Java target:** compiler `source/target 1.8` became `release 17`, and `java.version` is now 17  
- 🌱 **Spring Boot:** 1.2.3.RELEASE → **3.5.16**, which manages Spring Framework **6.2.19**  
  - 🧹 The 9 hard-coded Spring `4.1.6.RELEASE` artifact versions and a duplicate `spring-test` entry were removed, so the Boot BOM now manages them  
  - ⚠️ The mandated version was 3.2.12. It was raised to 3.5.16 because Spring 6.1.15 has HIGH/CRITICAL CVEs with no fix in the 6.1 line.  
- 🧪 **Test toolchain:**  
  - Groovy `org.codehaus.groovy:groovy-all` 2.4.3 → `org.apache.groovy:groovy` **5.0.8**  
  - Spock 1.0-groovy-2.4 → **2.5-groovy-5.0**  
  - 🆕 Added `gmavenplus-plugin` 5.1.0 and a Surefire `**/*Spec.java` include, so the dormant Spock specs now compile and run  
- 🔐 **CVE remediation overrides:** Tomcat **10.1.62**, Jackson **2.21.8** (mandated 2.15.2 has HIGH CVEs), Log4j API **2.25.5**  
- 📦 **Utility libraries:**  
  - Guava 18.0 → **33.8.0-jre**  
  - commons-io 2.4 → **2.22.0**  
  - commons-codec 1.10 → **1.23.0**  
  - cglib-nodep 3.1 → **3.3.0**  
- 🛡️ **Build hygiene:** removed the plain-HTTP `repo.spring.io/milestone` repository, which Maven 3.9+ blocks  
- 📝 **Documentation:** `README.md` now records version deviations, skipped items, and the remaining CVEs  
  
---  
  
## 🛠️ 3. Tools Used  
  
- ☕ **Java SDKs**  
  - Amazon Corretto **8**: baseline build (BUILD SUCCESS, 0 tests)  
  - Amazon Corretto **17**: target runtime for the build, tests, and app boot check  
  - JDK **25.0.5.1**: default launcher, used to confirm the build also works on newer JDKs  
- ☁️ **AWS Transform Custom CLI (`atx`)**  
  - Command: `atx custom def exec --transformation-name AWS/java-version-upgrade --non-interactive --trust-all-tools`  
  - 🏢 AWS Managed transformation with a planned, stepwise run and a git commit at each step  
  - 🧠 Applied learnings from earlier runs: dependency version verification (9 lessons), Spock/Groovy test execution (4), and Spring Boot/Java compatibility (5)  
  - ℹ️ CodeKB MCP tools were unavailable for this job, and the run went ahead without them  
- 🔨 **Apache Maven 3.9.16**: `mvn -B clean install -Dspotbugs.skip=true -Dcheckstyle.skip=true`  
- 🔍 **Supporting tools:** `javap` for bytecode and API checks, `mvn dependency:list` for resolved versions, Maven Central metadata lookups, and `curl` smoke tests  
  
---  
  
## 💻 4. Code Changes  
  
| 📄 File | 🔧 Change | 💡 Reason |  
|---|---|---|  
| `pom.xml` | Compiler plugin 3.0 → 3.13.0 with `release 17`; Boot parent, Groovy, Spock, gmavenplus, Surefire, CVE overrides, and library bumps | Java 17 target, current toolchain, CVE-clean dependencies |  
| `MainApplication.java` | `new Integer("1234")` → `Integer.valueOf("1234")` | The boxed-primitive constructor is deprecated for removal |  
| `Sha512ShallowEtagHeaderFilter.java` | Override changed from `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean)` and reads the stream with Guava `ByteStreams` | The Spring 6 API signature changed |  
| `DownloadControllerTest.groovy` | `@ContextConfiguration` → `@SpringBootTest` | Spring 6 / Spock 2 context injection |  
| `FileExamples.java` | `UUID.randomUUID()` → fixed `UUID.fromString(...)` | Fixtures stay stable across separate class initialization |  
| `logback.xml` | Inline unnamed appender replaced with a named `CONSOLE` appender plus `appender-ref` | Newer Logback versions do not accept unnamed inline appenders |  
| `README.md` | Added a "Java 17 upgrade notes" section | Records deviations and the known remaining CVEs |  
  
- ✅ No `javax.*` EE imports were present, so no Jakarta source migration was needed  
- ✅ Lombok, a Gradle wrapper, and Jakarta APIs are not used, so those mandates did not apply  
  
---  
  
## ⏳ 5. Time Savings Estimate  
  
| 🧩 Task | 👤 Manual estimate |  
|---|---|  
| Analyze the codebase and plan a 4-major-version Spring Boot jump (1.x → 3.x) | 2–3 h |  
| Upgrade the build and compiler, and fix the blocked HTTP repository | 1 h |  
| Rationalize the Spring BOM and remove pinned versions | 1–2 h |  
| Move Groovy/Spock to Groovy 5 / Spock 2.5 and get the dormant specs to compile and pass | 3–5 h |  
| Fix the Spring 6 API break, the test annotation, UUID fixtures, and logback | 2–3 h |  
| CVE scan, version research, overrides, and re-verification | 3–4 h |  
| Verify on two JDKs, check bytecode, smoke-test the boot, write documentation | 2–3 h |  
| **Total** | **~14–21 h (about 2–3 engineer-days)** |  
  
- ⚡ **Automated run:** about 9 minutes wall-clock  
- 📉 **Estimated savings:** about **95%** of hands-on engineering effort. Human review of the branch is still needed and should take 1–2 h.  
- 📏 These figures are estimates for a small codebase (8 main source files). Savings grow with larger or multi-module projects that need the same kinds of changes.  
  
---  
  
## 🎯 6. Next Steps  
  
### ✅ Validation  
- 👀 Review the 5 commits on `atx-result-staging-20261119_173914_20b4ec3a` and open a PR using `git_instructions.md` from the atx artifacts folder  
- 🔁 Run the CI pipeline on JDK 17, without `-Dspotbugs.skip` / `-Dcheckstyle.skip`, to confirm the static analysis gates pass  
- 🌐 Deploy to a staging environment on a Java 17 runtime and regression-test the download flows: ETag/SHA-512 headers, 404s, and throttling  
- 📊 Check actuator endpoint changes. Boot 1.x paths moved under `/actuator/*` in Boot 2+, so dashboards and health probes may need updates.  
- 🧾 Confirm logging output format and levels after the logback 1.5 change  
  
### 🔧 Improvements  
- 🚨 **Highest priority:** plan the Spring Boot 4 / Spring Framework 7 upgrade to fix CRITICAL CVE-2026-47884 and CVE-2026-47890 in `spring-webmvc` 6.2.19. The app does not use `XsltView` or SSE fragment rendering, which reduces the exposure.  
- 🔒 Add automated dependency CVE scanning (for example OWASP Dependency-Check) to CI  
- 🧪 Expand test coverage. All 12 current tests are in a single Spock spec (`DownloadControllerSpec`).  
- 🆙 Consider Java 21 LTS as the next target, since the build already works on JDK 25  
- 🧹 Remove the explicit Spring artifact declarations that `spring-boot-starter-web` already brings in, to simplify the POM  
- 📦 Consider replacing `cglib-nodep` with Spring's built-in proxying or ByteBuddy if it is not directly needed

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 11 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 0

## Kiro credit usage

💳 Kiro CLI credits used: 3.29  
Includes 0.31 for job setup (startup smoke test and knowledge-base indexing).  
