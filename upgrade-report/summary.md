# 🚀 Executive Report: Java 25 & Spring Boot 4.0 Upgrade (`download-server`)  
  
## 📋 Executive Summary  
  
The `download-server` application is a small Spring MVC service that serves files by UUID, with ETag and `If-Modified-Since` caching (HTTP 304), permanent redirects, and download throttling at 1 MB/s. AWS Transform Custom upgraded it in two automated runs. The first run (`AWS/java-version-upgrade`) moved the project from Java 8 / Spring Boot 1.2.3 to Java 25 / Spring Boot 3.5.16. The second run (`AWS/spring-boot-version-upgrade`) moved it on to Spring Boot 4.0.9. ✅ Both runs finished with status **COMPLETE**. `mvn clean verify` succeeds on Amazon Corretto 25.0.5 with **12 of 12 tests passing**, and the application starts on Java 25 (`/actuator/health` returns 200). 🎯 The Spock test suite never ran before the upgrade (0 tests executed) and now runs in every build.  
  
---  
  
## 🏗️ Application Changes  
  
- ☕ **Java runtime:** Java 8 (`source/target 1.8`) ➡️ **Java 25** (`maven.compiler.release=25`)  
- 🌱 **Spring Boot:** 1.2.3.RELEASE ➡️ 3.5.16 ➡️ **4.0.9**, the latest patch on the 4.0 line, as the run configuration required  
- 🧩 **Spring Framework:** pinned 4.1.6.RELEASE artifacts ➡️ versions now managed by the Spring Boot BOM (Spring Framework 7)  
- 🧪 **Test stack:** Groovy 2.4.3 + Spock 1.0 ➡️ **Groovy 5.0.8 + Spock 2.5-groovy-5.0**  
- 🔓 **Dormant tests re-enabled:** the Spock specs are now compiled and run (0 ➡️ 12 tests)  
- 🔐 **Security cleanup:** removed the plain-HTTP repository `http://repo.spring.io/milestone/`; CVE scan of new and changed dependencies passed  
- 🗑️ **Dependency removed:** `cglib-nodep` 3.1, which Spring has bundled internally for many releases  
- 📁 **Files changed:** 5 files in run 1 (+63 / −72 lines) and 1 build file in run 2, plus generated migration artifacts  
- 🌿 **Git:** commits `1cadcda` (Java 25) and `52d490e` (Spring Boot 4.0.9) on branch `atx-result-staging-20261105_095621_414fd426`. The original branch is unchanged.  
  
---  
  
## 🛠️ Tools Used  
  
- ☕ **Java SDK:** Amazon Corretto **25.0.5** (target JDK); Java 8 for the baseline build  
- 🤖 **AWS Transform Custom CLI (`atx`):** AWS-managed transformations run non-interactively in region `ap-southeast-2`  
  - 🔄 `AWS/java-version-upgrade` used **35.28 agent minutes**  
  - 🔄 `AWS/spring-boot-version-upgrade` used **42.34 agent minutes**  
  - 🧠 Applied lessons from earlier runs: Spring Boot/Java compatibility and Spock test execution  
- 📦 **Apache Maven 3.9.16** with the Maven wrapper (`mvnw`)  
- 🔌 **Build plugins:** `maven-compiler-plugin` 3.13.0, `gmavenplus-plugin` 5.1.0, `maven-surefire-plugin` (version managed by the BOM)  
  
---  
  
## 💻 Code Changes  
  
### 📦 `pom.xml`  
- ⬆️ Parent `spring-boot-starter-parent`: 1.2.3.RELEASE ➡️ 3.5.16 ➡️ **4.0.9**  
- 🔁 Starter renamed: `spring-boot-starter-web` ➡️ `spring-boot-starter-webmvc`, the new name in Boot 4  
- ⚙️ `maven-compiler-plugin` 3.0 ➡️ 3.13.0 with `<release>25</release>`  
- ➕ Added `gmavenplus-plugin` 5.1.0 (`compileTests`, `targetBytecode` 25), because version 4.1.1 could not emit Java 25 bytecode  
- ➕ Added surefire includes for `**/*Spec.java` so the Spock specs actually run  
- ⬆️ Guava 18.0 ➡️ 33.8.0-jre · commons-io 2.4 ➡️ 2.22.0 · commons-codec 1.10 ➡️ 1.23.0  
- 🔁 `org.codehaus.groovy:groovy-all` 2.4.3 ➡️ `org.apache.groovy:groovy` 5.0.8  
- 🧹 Removed hard-coded Spring versions, duplicate `spring-test` and `spock-spring` entries, their exclusions, and `cglib-nodep`  
  
### 📝 `src/main/resources/logback.xml`  
- 🔧 The unnamed `<appender>` nested inside `<root>` is now a named `CONSOLE` appender referenced with `<appender-ref>`, as Logback 1.5 requires  
  
### 🧪 Test sources  
- 🔑 `Sha512ShallowEtagHeaderFilter.java`: overrides `generateETagHeaderValue(InputStream, boolean)` instead of the `byte[]` method removed in Spring 6, and supports weak ETags (`W/"..."`)  
- 🏷️ `DownloadControllerTest.groovy`: `@ContextConfiguration` ➡️ `@SpringBootTest(classes = [MainApplication])`  
- 🎲 `FileExamples.java`: `UUID.randomUUID()` ➡️ fixed `UUID.fromString(...)` values, so separately initialised test classes see the same IDs  
  
### ⚠️ Issues Resolved During the Upgrade  
- ❌➡️✅ Test compilation failed on the removed `ShallowEtagHeaderFilter` byte-array API; fixed by moving to the `InputStream` overload  
- ❌➡️✅ gmavenplus 4.1.1 rejected bytecode version 25; fixed by upgrading to 5.1.0  
- ❌➡️✅ The default target, Spring Boot 3.2.12, bundles an ASM version that cannot read Java 25 class files (major version 69); fixed by using 3.5.16, a documented deviation  
  
---  
  
## ⏱️ Time Savings Estimate  
  
| 🧩 Activity | 👤 Manual Estimate | 🤖 Automated |  
|---|---|---|  
| Dependency research & version alignment (Boot 1.2 ➡️ 3.5 ➡️ 4.0, Groovy/Spock) | 1–1.5 days | ✅ |  
| Build plugin fixes (compiler, gmavenplus, surefire) | 0.5 day | ✅ |  
| API migration (ETag filter, test annotations, logback config) | 0.5 day | ✅ |  
| Re-enabling dormant Spock suite & stabilising fixtures | 0.5 day | ✅ |  
| Build/runtime validation, CVE check, documentation | 0.5–1 day | ✅ |  
| **Total** | **⏳ ~3–4 engineer-days** | **⚡ ~78 agent minutes** |  
  
- 💰 **Estimated saving: about 85–90% of the effort**, i.e. 3–4 days of manual work reduced to a review of 1–2 hours  
- 📈 The saving comes mainly from jumping three major Spring Boot generations and diagnosing the Java 25 bytecode failures (ASM, gmavenplus) without manual trial and error  
  
---  
  
## 🔜 Next Steps  
  
### ✅ Validation  
- 👀 Review and merge the staging branch `atx-result-staging-20261105_095621_414fd426`; PR instructions are in the ATX `artifacts/git_instructions.md`  
- 🌐 Run functional tests in a staging environment for `GET`/`HEAD /download/{uuid}/{filename}`: ETag and `If-Modified-Since` handling, the 304 response, the 301 redirect, and 1 MB/s throttling  
- 🚦 Deploy to CI/CD and container images built on a **Java 25** base image (e.g. Corretto 25)  
- 📊 Check that actuator endpoint exposure and paths meet monitoring expectations under Boot 4 defaults  
- 🔍 Run a full SCA/CVE scan of the whole dependency tree, including transitive dependencies  
  
### 💡 Recommended Improvements  
- 🧹 `MainApplication.main` still contains `Integer temp = new Integer("1234");`. This unused variable calls a constructor deprecated for removal; delete it, or use `Integer.valueOf`  
- 📦 Move `Sha512ShallowEtagHeaderFilter` out of the `org.springframework.web.filter` package into an application package  
- 🧽 Remove the explicit `spring-*` dependencies, which the starters already provide, to reduce maintenance  
- 🧪 Add a Java test runner (JUnit 5) alongside Spock, and add coverage reporting (JaCoCo) to the build  
- 🔭 Plan a later move to **Spring Boot 4.1.x**. Boot 4.1 removes APIs deprecated in 4.0, so rerun the deprecation checks when doing it  
- 🗂️ Decide whether to keep `code-intelligence-report.md`, `migration-plan.json`, `.migration-test-baseline.tsv` and the untracked `migration-results.yaml` in the repository or archive them  

## Kiro credit usage

💳 Kiro CLI credits used: 4.14  
Includes 0.31 for job setup (startup smoke test and knowledge-base indexing).  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
