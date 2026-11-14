# 📊 Executive Report: Java Version Upgrade, `download-server`  
  
**Date:** 2026-11-14 · **Region:** ap-southeast-2 · **Overall status:** ✅ COMPLETE  
  
---  
  
## 1. 🧭 Executive Summary  
  
AWS Transform Custom upgraded the single-module Maven application `com.nurkiewicz:download-server` in two automated steps. Step 1 (`AWS/java-version-upgrade`) moved it from Java 8 (the run config listed 1.6) and Spring Boot 1.2.3 to **Java 25** and Spring Boot 3.5.16. Step 2 (`AWS/spring-boot-version-upgrade`) moved it to **Spring Boot 4.0.9**. The build passes on Corretto JDK 25.0.5 with Maven 3.9.16 and runs **12 of 12 Spock tests with no failures**. Before the upgrade, the original build ran **0 tests** because the Groovy specs were never compiled. The app was started on JDK 25, and `/actuator/health` returned `UP`. All exit criteria in both steps passed, and no tests, modules, or dependency versions were removed, skipped, or downgraded.  
  
- 🎯 **Java:** 8 → **25**  
- 🌱 **Spring Boot:** 1.2.3 → 3.5.16 → **4.0.9**  
- 🧪 **Tests:** 0 executed → **12 passing**  
- 🔒 **CVE check:** no known vulnerabilities in the selected versions  
- ⏱️ **Run time:** about 12 minutes wall clock (15:53–16:05), **77.45 agent minutes** (32.61 + 44.84)  
  
---  
  
## 2. 🏗️ Application Changes  
  
- 📦 **Build system:** Rewrote `pom.xml` for Java 25 (`java.version` and `maven.compiler.release` set to 25)  
- 🌱 **Framework:** Spring 4.1.6 → Spring 6.2 (Boot 3.5.16) → Spring Boot 4.0.9, which also moved the dependencies to the `jakarta.*` namespace  
- 🧹 **Dependency cleanup:**  
  - ❌ Removed 8 explicitly declared Spring Framework modules that the starters already provide (`spring-beans`, `-core`, `-context`, `-context-support`, `-webmvc`, `-web`, `-aop`, `-expression`)  
  - ❌ Removed duplicate `spring-test` entries and `cglib-nodep` 3.1  
  - 🔐 Removed the insecure `http://repo.spring.io/milestone/` repository, which modern Maven rejects  
- 🧪 **Test toolchain:** The Spock suite, which had never run, is now compiled and executed  
- 📝 **Logging:** Restructured `logback.xml` to work with Logback 1.5.x  
- 📁 **Files changed:** 6 in step 1 (+87 / −111 lines). Step 2 changed 2 lines in `pom.xml` and added 5 migration artifacts.  
  
---  
  
## 3. 🛠️ Tools Used  
  
- ☕ **Java SDK:** Amazon Corretto **25.0.5** (`25.0.5.0.10`) as the target and build JDK. Java 1.8 (`/usr/lib/jvm/java-1.8.0`) was used to record the baseline.  
- 🤖 **AWS Transform Custom CLI (`atx`):**  
  - 🔄 `AWS/java-version-upgrade`: AWS Managed transformation, conversation `20261114_155331_3eaaf88a`  
  - 🔄 `AWS/spring-boot-version-upgrade`: AWS Managed transformation, conversation `20261114_155858_8959ae69`  
  - ⚙️ Ran with `--non-interactive --trust-all-tools`  
  - 🧠 Applied learnings from earlier runs: Spock/Groovy test execution, Spring Boot/Java compatibility, Spock test detection  
- 🧰 **Build:** Apache Maven 3.9.16 with `mvn -B clean install -Dspotbugs.skip=true -Dcheckstyle.skip=true`  
- 🌿 **Version control:** Each step committed to its own staging branch, leaving the original `main-upgrade` branch untouched  
  
---  
  
## 4. 💻 Code Changes  
  
### 📦 `pom.xml`  
- ⬆️ `spring-boot-starter-parent`: `1.2.3.RELEASE` → `3.5.16` → `4.0.9`  
- 🔁 `spring-boot-starter-web` → `spring-boot-starter-webmvc` (the Boot 4 starter name)  
- ⬆️ `maven-compiler-plugin`: `3.0` (`source`/`target 1.8`) → `3.14.1` (`<release>25</release>`)  
- ⬆️ `commons-io` 2.4 → **2.22.0** · `commons-codec` 1.10 → **1.23.0** · `guava` 18.0 → **33.8.0-jre**  
- 🔁 `groovy-all` 2.4.3 → `org.apache.groovy:groovy` **5.0.8** (test scope)  
- ⬆️ `spock-core` / `spock-spring`: `1.0-groovy-2.4` → **`2.5-groovy-5.0`**  
- ➕ Added `gmavenplus-plugin` 5.1.0 (`compileTests`, `targetBytecode` 25)  
- ➕ Added a surefire `<include>` for `**/*Spec.java` and the argLine `-javaagent:${org.mockito:mockito-core:jar}`, because JDK 25 blocks Mockito's dynamic self-attach  
- ➕ Added the `maven-dependency-plugin` `properties` goal so the Mockito jar path can be resolved  
  
### ☕ Java / Groovy sources  
- 🔧 `Sha512ShallowEtagHeaderFilter.java`: Overrides the Spring 6 signature `generateETagHeaderValue(InputStream, boolean)` and adds weak-ETag (`W/`) support. SHA-512 hashing is unchanged.  
- 🧹 `MainApplication.java`: Removed the unused, deprecated `new Integer("1234")` statement  
- 🧪 `FileExamples.java`: Replaced `UUID.randomUUID()` static fixtures with fixed `UUID.fromString(...)` literals  
- 🧪 `DownloadControllerTest.groovy`: `@ContextConfiguration` → `@SpringBootTest`, so Web MVC autoconfiguration is applied  
  
### 📝 Resources  
- 🪵 `logback.xml`: Named the `CONSOLE` appender and referenced it with `<appender-ref>`, because Logback 1.5.x rejects unnamed nested appenders  
  
### ⚠️ Documented deviation  
- 📌 Spring Boot **3.5.16** was used in step 1 instead of the mandated 3.2.12, because 3.2.x cannot read Java 25 bytecode (`Unsupported class file major version 69`). 3.5.x is also the direct path to Boot 4.0.  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
| Activity | Manual estimate | 🤖 Automated |  
|---|---|---|  
| 🔍 Assessment, version research, CVE checks | 4–6 h | included |  
| 📦 POM rewrite and dependency rationalization | 4–6 h | included |  
| 🧪 Reviving the Groovy/Spock toolchain on JDK 25 (gmavenplus, surefire, Mockito agent) | 6–10 h | included |  
| 🔧 Spring 6 API fixes, Logback, test fixture fixes | 3–5 h | included |  
| 🌱 Boot 3.5 → 4.0 migration and checks for deprecated patterns | 3–4 h | included |  
| ✅ Build/run verification and documentation | 3–5 h | included |  
| **Total** | **~23–36 h (3–4.5 days)** | **~12 min wall clock / 77 agent-min** |  
  
- 💰 **Estimated savings:** about **3–4 engineer-days** (~95% less effort)  
- 📈 **Why the manual estimate is this high:** The upgrade spans three Spring Boot major versions (1 → 3 → 4), 17 Java releases, the Jakarta namespace move, and JDK 25-specific problems (ASM bytecode support, Mockito self-attach) that usually take several rounds of trial and error to diagnose.  
- ℹ️ The manual figures are estimates for a small (~800 LOC) codebase, not measured values.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation  
- 🔀 Review and merge the staging branches (`atx-result-staging-20261114_155331_3eaaf88a` → `atx-result-staging-20261114_155858_8959ae69`) through a pull request. Instructions are in `git_instructions.md`.  
- 🏗️ Run `mvn -B clean verify` on JDK 25 in CI, **without** the `spotbugs`/`checkstyle` skip flags  
- 🧪 Run functional and regression tests on file download, ETag / `If-None-Match` handling, and range/throttled streaming  
- 🩺 Check actuator endpoint exposure, because the defaults differ between Boot 3.x and Boot 4  
- 🔐 Run your organization's SCA scanner on the Boot 4.0.9 dependency tree  
  
### 💡 Improvements  
- 📦 Add `spring-boot-maven-plugin` to produce an executable fat jar. It is not declared today, which was already the case before the upgrade.  
- 🧪 Keep the Mockito `-javaagent` argLine, or replace it with a cleaner agent setup as the Spock/Mockito tooling matures  
- 🔤 Rename `DownloadControllerTest.groovy` to match its class `DownloadControllerSpec`, for clearer test discovery  
- 🧱 Add JUnit 5 tests or more Spock specs. Current coverage is 12 controller-level specs.  
- 🔄 Track Spring Boot 4.0.x patch releases and plan an evaluation of 4.1.x  
- 🐳 Update container base images and deployment runtimes to Java 25

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 14 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 0

## Kiro credit usage

💳 Kiro CLI credits used: 4.44  
Includes 0.31 for job setup (startup smoke test and knowledge-base indexing).  
