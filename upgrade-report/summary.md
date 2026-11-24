# 📋 Executive Report: Java 25 Upgrade, Iteration 34  
  
## 🧭 Executive summary  
- 🎯 `download-server` was moved from Java 1.8 source/target to `release 25`, and from Spring Boot `1.2.3.RELEASE` to Spring Boot `4.0.10` (parent POM).  
- 🔧 OpenRewrite applied `com.amazonaws.java.migrate.UpgradeToJava25` and then `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. A Kiro CLI build-fix pass fixed the one remaining test-source compilation error.  
- ✅ The post-upgrade verification build finished with `BUILD SUCCESS`. Main sources (8 files) and test sources (3 files) compiled with `javac [debug parameters release 25]`.  
- 🧪 Automated tests: test execution not recorded. The verification log has no Surefire test totals.  
- 🌱 Spring: runtime validation reports "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." The Spring Boot 4.0.10 parent manages Spring versions. The Kiro pass reported Spring Framework 7.0.11 resolved from that BOM.  
- ⚠️ Runtime behaviour is unverified: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started.  
- 🌿 Changes are committed to branch `main-upgrade-itr-34` (commit `ae52730`, 5 files changed, 68 insertions, 33 deletions).  
  
## 🗂️ Application changes  
- 📄 Files changed on the branch (5):  
  - `pom.xml` (73 lines changed)  
  - `src/main/java/com/nurkiewicz/download/DownloadController.java` (2 lines removed)  
  - `src/main/java/com/nurkiewicz/download/MainApplication.java` (1 line changed)  
  - `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` (9 lines changed)  
  - `summary.md` (new, 15 lines)  
- ➕ Dependencies added:  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, managed by the Boot parent), **compile** scope (no scope in the diff). It replaces `spring-boot-starter-web`.  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST, managed by the Boot parent), **test** scope. It is not packaged with the application.  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST), build plugin running the `properties` goal.  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST), build plugin with `argLine` set to `@{argLine} -javaagent:${net.bytebuddy:byte-buddy-agent:jar}`.  
- ➖ Dependencies removed:  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
- ⬆️ Dependencies upgraded:  
  - `org.springframework.boot:spring-boot-starter-parent` 1.2.3.RELEASE → 4.0.10  
  - `org.apache.maven.plugins:maven-compiler-plugin` 3.0 → 3.16.0  
  - `com.google.guava:guava` 18.0 → 29.0-jre  
  - `org.springframework:spring-test` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-beans` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-context` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-context-support` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-webmvc` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-web` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-aop` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-core` 4.1.6.RELEASE → LATEST  
  - `org.springframework:spring-expression` 4.1.6.RELEASE → LATEST  
  - `org.springframework.boot:spring-boot-starter-actuator` 1.2.3.RELEASE → LATEST  
  - `org.springframework.boot:spring-boot-starter-test` 1.2.3.RELEASE → LATEST  
  - `commons-codec:commons-codec` 1.10 → LATEST  
  - `org.codehaus.groovy:groovy-all` 2.4.3 → LATEST. The diff pins it to `2.4.16` in a new `<dependencyManagement>` block.  
  - ℹ️ "LATEST" means the explicit version was removed and the Spring Boot parent BOM now manages it.  
  
## 🛠️ Tools used  
- ☕ Java SDK: compilation targets `release 25` (`maven-compiler-plugin` 3.16.0).  
- 🔄 OpenRewrite: `rewrite-maven-plugin` 6.42.0 with these recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which changed `pom.xml` and `MainApplication.java`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (chaining Boot 2.0 → 4.0 and Spring Framework 5.0 → 6.0 recipes), which changed `pom.xml` and `DownloadController.java`.  
- 🤖 Amazon Kiro CLI `kiro-cli 2.35.1` with model `claude-opus-5.5`. It succeeded on the first attempt, doing the build fix and the report.  
  
## 💻 Code changes  
- 🔢 `MainApplication.java`: `new Integer("1234")` → `Integer.valueOf("1234")` (recipe `PrimitiveWrapperClassConstructorToValueOf`).  
- 🧹 `DownloadController.java`: removed `@Autowired` from the single constructor and its `org.springframework.beans.factory.annotation.Autowired` import (recipe chain including `SpringBoot2BestPractices`).  
- 🔐 `Sha512ShallowEtagHeaderFilter.java` (test source): override changed from `generateETagHeaderValue(byte[] bytes)` to `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. It hashes `inputStream.readAllBytes()` with SHA-512 and prefixes `W/` when `isWeak` is true. Imports `java.io.IOException` and `java.io.InputStream` were added (Kiro fix).  
- 📦 `pom.xml`:  
  - Added `<java.version>25</java.version>` and an empty `<argLine></argLine>` property.  
  - Removed the `spring.version` property (4.1.1.RELEASE).  
  - Replaced compiler `<source>1.8</source>`/`<target>1.8</target>` with `<release>${java.version}</release>`.  
  - Removed explicit Spring/Boot/commons-codec/groovy-all versions so the BOM manages them.  
  - Removed the duplicate `spring-test` 4.1.6.RELEASE declaration.  
  - Added a `junit:junit` exclusion to `spock-core`.  
- 📝 `summary.md`: a new upgrade summary added to the repository root.  
  
## ⚠️ Risks  
- 🚫 Runtime is unverified: the smoke-start tier was skipped, so application start-up and request handling on Java 25 / Spring Boot 4 have not been exercised.  
- 🧪 Test execution was not recorded. The Kiro pass noted that the Spock spec `DownloadControllerTest.groovy` is not compiled by the build (no Groovy compiler plugin), and that Groovy 2.4 / Spock 1.0 are not compatible with JDK 25. Controller behaviour (ETag, If-Modified-Since, HEAD, 404) therefore has no recorded test evidence.  
- 🧩 `spring-boot-starter-webmvc` has compile scope and packages the Boot web/servlet stack with the application. If the application were deployed as a WAR to an external servlet container that already provides the Servlet API, that could cause a classloader conflict. Packaged as a Boot executable JAR, this does not apply. `jakarta.servlet-api` is test scope and is not packaged.  
- 📚 Old libraries outside the BOM remain: `guava` 29.0-jre, `commons-io` 2.4, `cglib-nodep` 3.1 and `groovy-all` 2.4.16. `groovy-all` has compile scope, so it ships with the application.  
- 🧭 The Spring Boot recipe logged: "Spring Boot upgrade path was used for the whole reactor, but plain (non-Boot) Spring artifacts were also detected." The plain `org.springframework:*` dependencies now take their versions from the Boot 4.0.10 BOM, and runtime validation reports OK (static).  
- 🪫 `FileSystemPointer.java` "uses or overrides a deprecated API" (post-upgrade verification warning).  
- 🌐 The `http://repo.spring.io/milestone/` repository could not be reached during the OpenRewrite run and is still declared in `pom.xml`.  
- 🏷️ The surefire `argLine` loads `byte-buddy-agent` as a Java agent. This is the mechanism `AddMockitoJavaAgentToMavenSurefirePlugin` adds for Java 25 tests, and it depends on that artifact resolving.  
  
## ⏱️ Time savings  
- ⏳ Transformation duration: 3 minute(s).  
- 💰 Estimated time saved: 180 minute(s).  
  
## 🚀 Next steps  
- ▶️ Run the smoke-start check with `ENABLE_RUNTIME_VALIDATION=true`, or start the application in a test environment and call `/download/{uuid}` and `/download/{uuid}/{filename}`.  
- 🧪 Make the Spock tests compile and run: add `gmavenplus-plugin`, move to Spock 2.4 with Groovy 4 (`org.apache.groovy`), and confirm Surefire reports test totals.  
- 🔍 Check the `Sha512ShallowEtagHeaderFilter` override against the Spring 7 `ShallowEtagHeaderFilter` contract, including weak ETag output.  
- ⬆️ Upgrade `guava`, `commons-io` and `cglib-nodep` (or remove `cglib-nodep` if unused), and move `groovy-all` to test scope.  
- 🧹 Remove the unreachable `http://repo.spring.io/milestone/` repository and fix the deprecated API use in `FileSystemPointer.java`.  
- 👀 Review and merge branch `main-upgrade-itr-34` after the checks above pass.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 3 minute(s)
- ⏳ Estimated time saved: 180 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 9.41  
Includes 1.17 for job setup (startup smoke test and knowledge-base indexing).  
