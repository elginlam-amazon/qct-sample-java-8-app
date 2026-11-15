# 📋 Java 25 Upgrade: Iteration 13 Executive Report  
  
## 🧭 Executive summary  
- 🎯 `download-server` was upgraded from Java 1.8 to Java 25 (`maven-compiler-plugin` `<release>${java.version}</release>`, `java.version` 25), and from Spring Boot 1.2.3.RELEASE to Spring Boot 4.0.9.  
- 🛠️ OpenRewrite applied `com.amazonaws.java.migrate.UpgradeToJava25`, then `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. Kiro CLI build-fix passes then fixed the remaining test compile error and restored the Spock test toolchain.  
- ✅ The post-upgrade verification build succeeded, and 12 automated tests ran (`DownloadControllerSpec`: 0 failures, 0 errors, 0 skipped).  
- 🌱 Spring: runtime validation reports "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." The Spring Framework version comes from the Spring Boot 4.0.9 parent, and no artifact shows an old Spring version.  
- ⚠️ Runtime behaviour is **unverified**: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started during validation.  
- ⏱️ Transformation duration: 17 minute(s). Estimated time saved: 300 minute(s).  
  
## 🧩 Application changes  
- 📁 Files changed: 7 (112 insertions, 59 deletions), all on the iteration branch (commit `551f98a`, "Upgrade to Java 25 on Iteration 13"):  
  - 📄 `pom.xml`  
  - ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java`  
  - ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`  
  - ⚙️ `src/main/resources/logback.xml`  
  - 🧪 `src/test/groovy/com/nurkiewicz/download/{DownloadControllerTest.groovy => DownloadControllerSpec.groovy}` (rename only, 100% similarity)  
  - 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  - 📝 `summary.md`  
- ➕ **Dependencies and plugins added** (scope from the `pom.xml` diff):  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, BOM-managed): compile scope (no `<scope>`). Replaces `spring-boot-starter-web`.  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST, BOM-managed): **test** scope, so it is not packaged with the application.  
  - `org.apache.groovy:groovy` (version LATEST, BOM-managed): **test** scope.  
  - `org.springframework.boot:spring-boot-maven-plugin` (version LATEST, from the parent): build plugin.  
  - `org.codehaus.gmavenplus:gmavenplus-plugin` (version 5.1.0): build plugin (`addTestSources`, `compileTests`, `targetBytecode ${java.version}`).  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST, from the parent): build plugin (`properties` goal).  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST, from the parent): build plugin (Byte Buddy `-javaagent` argLine; includes `**/*Test.java`, `**/*Tests.java`, `**/*Spec.java`).  
- ➖ **Dependencies removed:**  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
  - `cglib:cglib-nodep` (version 3.1)  
  - `org.codehaus.groovy:groovy-all` (version 2.4.3), which was compile scope before  
- ⬆️ **Dependencies upgraded:**  
  - `org.springframework:spring-test` (4.1.6.RELEASE -> LATEST); the duplicate `spring-test` declaration was also removed  
  - `org.spockframework:spock-spring` (1.0-groovy-2.4 -> `${spock.version}` = 2.5-groovy-5.0)  
  - `commons-codec:commons-codec` (1.10 -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-actuator` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-test` (1.2.3.RELEASE -> LATEST)  
  - `org.spockframework:spock-core` (1.0-groovy-2.4 -> `${spock.version}` = 2.5-groovy-5.0)  
  - `com.google.guava:guava` (18.0 -> 29.0-jre)  
  - `org.springframework:spring-beans` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context-support` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-webmvc` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-web` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-aop` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-core` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-expression` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-parent` (1.2.3.RELEASE -> 4.0.9)  
  - `org.apache.maven.plugins:maven-compiler-plugin` (3.0 -> 3.16.0)  
- 🧹 Other `pom.xml` changes: removed the `spring.version` (4.1.1.RELEASE) property and the plain-HTTP `http://repo.spring.io/milestone/` repository. Added the `argLine`, `java.version` (25) and `spock.version` (2.5-groovy-5.0) properties.  
  
## 🧰 Tools used  
- ☕ Java SDK: JDK 25 (Amazon Corretto 25.0.5, as reported by the Kiro build-fix pass in `code-upgrade.log`)  
- 🔧 OpenRewrite: `rewrite-maven-plugin` 6.42.0, with recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25` (applied through `OfficialUpgradeToJava25WithoutIfElseIfToSwitch`, including `UseMavenCompilerPluginReleaseConfiguration`, `UpgradePluginVersion`, `PrimitiveWrapperClassConstructorToValueOf` and `AddMockitoJavaAgentToMavenSurefirePlugin`)  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (applied through `org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0`, including `JavaxServletToJakartaServlet`, `NoAutowiredOnConstructor`, and `ChangeDependency` from `spring-boot-starter-web` to `spring-boot-starter-webmvc`)  
- 🤖 Amazon Kiro CLI: `kiro-cli 2.35.1`, model `claude-opus-5.5`, for the automated build fix and report generation  
  
## ✏️ Code changes (as shown in the diff)  
- ☕ `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")` (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`).  
- ☕ `DownloadController.java`: removed `@Autowired` from the single constructor and removed the `org.springframework.beans.factory.annotation.Autowired` import (OpenRewrite `NoAutowiredOnConstructor`).  
- 🧪 `Sha512ShallowEtagHeaderFilter.java`: override changed from `generateETagHeaderValue(byte[] bytes)` to `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. The bytes are read with `ByteStreams.toByteArray`, hashed with SHA-512, and the result is prefixed with `W/` when `isWeak` is true (Kiro fix).  
- ⚙️ `logback.xml`: the unnamed inline `ConsoleAppender` under `<root>` became a named `CONSOLE` appender referenced by `<appender-ref ref="CONSOLE"/>`. The pattern and logger levels are unchanged (Kiro fix).  
- 🧪 `DownloadControllerTest.groovy` renamed to `DownloadControllerSpec.groovy`, with no content change.  
- 📄 `pom.xml`: Java 25 compiler `release`, Spring Boot 4.0.9 parent, Spock 2.5/Groovy toolchain, executable-jar plugin, and the dependency changes listed above.  
  
## ⚠️ Risks  
- 🚦 **Runtime unverified:** the smoke-start tier was skipped, so application startup, request handling and actuator behaviour on Java 25 / Spring Boot 4.0.9 have not been exercised.  
- 🌱 **Spring:** the static check passed ("OK (static)"). `code-upgrade.log` also notes that plain (non-Boot) Spring artifacts were detected alongside the Boot upgrade path. In this single-module build the explicit `spring-*` dependencies no longer pin versions and are managed by the Boot 4.0.9 parent, but they are redundant and should be reviewed.  
- 📦 **Classloader-conflict risk:** `spring-boot-starter-webmvc` is compile scope and brings an embedded servlet container and servlet API into the packaged jar. That is fine for the executable jar, but deploying the artifact into an external servlet/application server, which already provides the servlet API, could cause conflicts. `jakarta.servlet-api` itself is test scope and is not packaged.  
- 🔁 **Large framework jump:** going from Spring Boot 1.2.3 to 4.0.9 changes defaults (for example actuator endpoint exposure, logging and MVC behaviour) that the 12 controller specs do not cover.  
- 🏷️ **ETag behaviour:** the test filter `Sha512ShallowEtagHeaderFilter` now uses a new Spring Framework API and can emit weak (`W/`) ETags. It lives in test sources only, so its behaviour is not checked by any test.  
- 📚 **Older libraries:** `guava` 29.0-jre and `commons-io` (unchanged in the diff) are old releases that remain on the compile classpath.  
- 🧪 **Test toolchain:** the Spock 2.5-groovy-5.0, Groovy and gmavenplus 5.1.0 setup is new, and test discovery depends on the Surefire `*Spec` include.  
  
## ⏱️ Time savings  
- 💰 Estimated time saved: 300 minute(s).  
- ⌛ Transformation duration: 17 minute(s).  
  
## 🚀 Next steps  
### ✅ Validation  
- 🔀 Review and merge the iteration branch through a pull request.  
- 🏗️ Run `mvn -B clean verify` on JDK 25 in CI.  
- 🚦 Enable the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`), or start the app in a test environment, to confirm it boots.  
- 🧪 Run functional and regression tests on file download, ETag / `If-None-Match` / `If-Modified-Since` handling, HEAD requests and throttled streaming.  
- 🩺 Review actuator endpoint exposure and security under Spring Boot 4 defaults.  
- 🔐 Run an SCA / vulnerability scan on the Spring Boot 4.0.9 dependency tree.  
- 🐳 Update container base images and deployment runtimes to Java 25, and deploy the executable jar standalone rather than into an external servlet container.  
  
### 💡 Improvements  
- 🧹 Remove redundant explicit `spring-*` dependencies, the `spring-test` exclusion on `spring-boot-starter-test`, and the test-scope `jakarta.servlet-api`, since the Boot starters already provide them.  
- ⬆️ Upgrade `guava` (29.0-jre) and `commons-io` to current releases.  
- 🗑️ Remove the unused `Integer temp = Integer.valueOf("1234")` from `MainApplication.main`.  
- 🧪 Add tests for `Sha512ShallowEtagHeaderFilter` and for actuator endpoints.  
- 🔄 Track Spring Boot 4.0.x patch releases and plan an evaluation of 4.1.x.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 17 minute(s)
- ⏳ Estimated time saved: 300 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 18.95  
Includes 1.18 for job setup (startup smoke test and knowledge-base indexing).  
