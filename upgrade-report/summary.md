# ☕ Java 17 Upgrade – Iteration 2 Executive Report  
  
## 📋 Executive summary  
- 🎯 The `download-server` Maven project was upgraded from Java 8 to Java 17. OpenRewrite (`com.amazonaws.java.migrate.UpgradeToJava17`) ran first, then a Kiro CLI build-fix pass.  
- 🌱 OpenRewrite logged "Skipping Spring Boot upgrade: not requested (springBootTargetVersion=none)". The Kiro CLI pass moved the Spring Boot parent from 1.2.3.RELEASE to 2.7.18 anyway and pinned Spring Framework to 5.3.39, because Spring 4.1 cannot run on Java 17. The project stays on the `javax` namespace.  
- ✅ The post-upgrade verification build succeeded (`BUILD SUCCESS`, compiled with `release 17`).  
- 🧪 Test execution was not recorded. Surefire 2.22.2 ran in the verification build, but the log shows no test totals.  
- 🔍 Runtime validation: "OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3)."  
- ⚠️ Runtime validation: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started, so its runtime behaviour is unverified.  
- 📦 The changes are committed to branch `ec2-jobrole-mwnxvull-or-kiro-upgrade-itr-2` (commit `922fe7a`, "Upgrade to Java 17 on Iteration 2").  
  
## 🛠️ Application changes  
- 📄 4 files changed, 35 insertions, 34 deletions:  
  - `pom.xml`  
  - `src/main/java/com/nurkiewicz/download/MainApplication.java`  
  - `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  - `summary.md` (new file in the repository)  
- ➕ Dependencies added: none.  
- ➖ Dependencies removed:  
  - `cglib:cglib-nodep` (version 3.1)  
- ⬆️ Dependencies upgraded. Entries listed as "LATEST" had their hard-coded version removed in the diff and now take their version from the Spring Boot 2.7.18 parent:  
  - `org.springframework:spring-test` (4.1.6.RELEASE -> LATEST)  
  - `commons-codec:commons-codec` (1.10 -> 1.17.2)  
  - `org.springframework.boot:spring-boot-starter-web` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-actuator` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-test` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework:spring-beans` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context-support` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-webmvc` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-web` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-aop` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-core` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-expression` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-parent` (1.2.3.RELEASE -> 2.7.18)  
  - `org.apache.maven.plugins:maven-compiler-plugin` (3.0 -> 3.16.0)  
  
## 🧰 Tools used  
- ☕ Java SDK: Amazon Corretto 17.0.21.1 ran Maven and javac (`release 17`).  
- 🔧 OpenRewrite: `rewrite-maven-plugin` 6.42.0 with the recipe `com.amazonaws.java.migrate.UpgradeToJava17`. It applied `UseMavenCompilerPluginReleaseConfiguration`, `UpdateMavenProjectPropertyJavaVersion`, `UpgradePluginVersion`, `UpgradeDependencyVersion` (commons-codec 1.17.x) and `PrimitiveWrapperClassConstructorToValueOf`.  
- 🤖 Amazon Kiro CLI 2.35.1 with model `claude-opus-5.5` ran the build fix and succeeded on attempt 1.  
  
## 💻 Code changes  
- 🔁 `MainApplication.java` (OpenRewrite): `new Integer("1234")` -> `Integer.valueOf("1234")`.  
- 🏗️ `pom.xml`:  
  - 🆕 Added the property `<java.version>17</java.version>`. The compiler plugin now uses `<release>${java.version}</release>` instead of `<source>1.8</source>` / `<target>1.8</target>`.  
  - 🌱 Replaced `<spring.version>4.1.1.RELEASE</spring.version>` with `<spring-framework.version>5.3.39</spring-framework.version>`.  
  - 🌱 Changed the Spring Boot parent from `1.2.3.RELEASE` to `2.7.18`.  
  - 🧹 Removed the hard-coded `1.2.3.RELEASE` and `4.1.6.RELEASE` versions from the Boot starters and Spring artifacts.  
  - 🧹 Removed the duplicate `spring-test` declaration and the `cglib-nodep` dependency.  
  - 🔒 Changed the repository URL from `http://repo.spring.io/milestone/` to `https://repo.spring.io/milestone/`.  
- 🧪 `Sha512ShallowEtagHeaderFilter.java` (test source, Kiro CLI): `generateETagHeaderValue(byte[] bytes)` became `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. It still hashes with SHA-512, reads the stream through `ByteStreams.toByteArray`, and adds a `W/` prefix when `isWeak` is true.  
  
## ⚠️ Risks  
- 🚫 Runtime validation: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." Startup and runtime behaviour on Java 17 are unverified.  
- 🧪 Test execution was not recorded. The only test, the Spock spec `src/test/groovy/.../DownloadControllerTest.groovy`, is not compiled by the build because there is no Groovy compiler plugin. Its behaviour after the upgrade is unchecked.  
- 🌱 The jump from Spring Boot 1.2.3 to 2.7.18 crosses two major versions. Changes in auto-configuration, actuator endpoints and property names were not exercised by any test or app start. Runtime validation reports "OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3)."  
- 🏷️ The ETag filter now emits a `W/` prefix for weak ETags. This is the Spring 5 base-class behaviour, but it differs from the old method. The class is in test sources.  
- 📚 The Spock 1.0-groovy-2.4 / Groovy 2.4.3 test stack does not support Java 17. `groovy-all` 2.4.3 is declared with compile scope.  
- 🌐 OpenRewrite logged that it could not reach `http://repo.spring.io/milestone/` and `https://repository.apache.org/snapshots`.  
  
## ⏱️ Time savings  
- ⚡ Transformation duration: 5 minute(s).  
- 💰 Estimated time saved: 150 minute(s).  
  
## 🚀 Next steps  
- ▶️ Enable the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`) and confirm that the application boots on Java 17 with Spring Boot 2.7.18.  
- 🧪 Add a Groovy compiler plugin (for example gmavenplus) and move to Spock 2.x with Groovy 3+, so that `DownloadControllerTest.groovy` compiles and runs and test totals are recorded.  
- 📦 Move `groovy-all` to test scope, or upgrade it together with Spock.  
- 🔎 Review the Spring Boot 2.x migration points: `application` properties, actuator endpoint paths and security defaults.  
- 🧹 Optionally update Guava 18.0 and commons-io 2.4.  
- 🌐 Check whether the `repo.spring.io/milestone` repository is still needed, and remove it if it is not.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 5 minute(s)
- ⏳ Estimated time saved: 150 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 11.99  
Includes 1.17 for job setup (startup smoke test and knowledge-base indexing).  
