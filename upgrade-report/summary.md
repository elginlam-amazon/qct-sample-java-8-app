# ☕ Java 17 Upgrade – Iteration 2 Executive Report  
  
## 📋 Executive summary  
- 🎯 **Scope:** The `download-server` Maven project was moved from Java 8 (`source`/`target` 1.8) to Java 17 (`<release>17</release>`). OpenRewrite made the first pass and Kiro CLI fixed the build afterwards.  
- 🌱 **Spring:** The Spring Boot parent moved from 1.2.3.RELEASE to 2.7.18 and Spring Framework is pinned to 5.3.39, so the code stays on javax. Runtime validation reports: "OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3)."  
- ✅ **Build:** The post-upgrade verification build succeeded (`BUILD SUCCESS`, exit code 0). It compiled 8 main and 3 test source files with `javac [debug parameters release 17]`.  
- 🧪 **Tests:** Test execution was not recorded. Surefire 2.22.2 ran, but post-upgrade-verification.log shows no test totals.  
- ⚠️ **Runtime:** Runtime behaviour is unverified. "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started.  
- ⏱️ **Effort:** The transformation took 4 minute(s). The estimated time saved is 180 minute(s).  
  
## 🛠️ Application changes  
The branch has 4 files changed, with 36 insertions and 33 deletions:  
- 📦 `pom.xml`: Java 17 build settings, Spring Boot 2.7.18 parent and Spring 5.3.39, Spring version cleanup, and the cglib removal.  
- ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`: replaced a deprecated wrapper constructor.  
- 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: moved to the Spring 5 ETag API.  
- 📝 `summary.md`: notes from the build-fix pass.  
  
### 📚 Dependencies  
- ➕ **Added:** none  
- ➖ **Removed:**  
  - `cglib:cglib-nodep` (version 3.1)  
- ⬆️ **Upgraded:**  
  - `org.springframework.boot:spring-boot-starter-parent` (version 1.2.3.RELEASE -> 2.7.18)  
  - `org.apache.maven.plugins:maven-compiler-plugin` (version 3.0 -> 3.16.0)  
  - `commons-codec:commons-codec` (version 1.10 -> 1.17.2)  
  - The artifacts below are reported as `-> LATEST` because their explicit `<version>` was removed. The `spring-boot-starter-parent` 2.7.18 parent now manages them, and the new `spring-framework.version` property is 5.3.39:  
    - `org.springframework:spring-test` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE -> LATEST)  
    - `org.springframework.boot:spring-boot-starter-actuator` (version 1.2.3.RELEASE -> LATEST)  
    - `org.springframework.boot:spring-boot-starter-test` (version 1.2.3.RELEASE -> LATEST)  
    - `org.springframework:spring-beans` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-context` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-context-support` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-webmvc` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-web` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-aop` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-core` (version 4.1.6.RELEASE -> LATEST)  
    - `org.springframework:spring-expression` (version 4.1.6.RELEASE -> LATEST)  
  
## 🧰 Tools used  
- ☕ **Java SDK:** the build targets Java 17 (`release 17`).  
- 🔄 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0 with the active recipe `com.amazonaws.java.migrate.UpgradeToJava17`, which includes:  
  - `Java8toJava11`  
  - `UpgradeBuildToJava17`  
  - `UpgradePluginsForJava17`  
  - `UseMavenCompilerPluginReleaseConfiguration`  
  - `PrimitiveWrapperClassConstructorToValueOf`  
  - `UpgradeDependencyVersion` for commons-codec 1.17.x  
- 🤖 **Amazon Kiro CLI:** version 2.35.1 with the model `claude-opus-5.5`. It succeeded on the first attempt and did the automated build fix.  
- ℹ️ The automated Spring Boot recipe step was skipped (`springBootTargetVersion=none`). The Spring Boot parent bump came from the Kiro CLI build-fix pass.  
  
## 💻 Code changes  
  
### 📦 `pom.xml`  
- Added the `<java.version>17</java.version>` property.  
- Replaced `<source>1.8</source>` / `<target>1.8</target>` with `<release>${java.version}</release>`.  
- Bumped `maven-compiler-plugin` from 3.0 to 3.16.0.  
- Moved the parent `spring-boot-starter-parent` from 1.2.3.RELEASE to 2.7.18.  
- Replaced `<spring.version>4.1.1.RELEASE</spring.version>` with `<spring-framework.version>5.3.39</spring-framework.version>`.  
- Removed the explicit `4.1.6.RELEASE` / `1.2.3.RELEASE` versions from the Spring and Spring Boot dependencies, so the Boot parent now manages them.  
- Removed the second, duplicate `spring-test` (test scope) declaration.  
- Removed the `cglib:cglib-nodep` 3.1 dependency.  
- Bumped `commons-codec` from 1.10 to 1.17.2.  
  
### ☕ `MainApplication.java`  
- Replaced `new Integer("1234")` with `Integer.valueOf("1234")`.  
  
### 🧪 `Sha512ShallowEtagHeaderFilter.java`  
- Replaced the override `generateETagHeaderValue(byte[] bytes)` with `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`.  
- The method still hashes with SHA-512. It now reads the stream through `ByteStreams.toByteArray(inputStream)` and prefixes `W/` when `isWeak` is true.  
- Added imports for `com.google.common.io.ByteStreams`, `java.io.IOException` and `java.io.InputStream`.  
  
## ⚠️ Risks  
- 🚫 **The application was not started.** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The static Spring check passed ("OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3)."), but that does not prove the application starts or runs on Java 17.  
- 🧪 **No test results.** Test execution was not recorded, so nothing in the build output confirms behaviour.  
  - The parity evidence shows "0 test class(es) ran before the upgrade, 0 after."  
  - The Kiro CLI notes say the Spock spec `DownloadControllerTest.groovy` is not compiled, because the POM has no Groovy compiler plugin.  
- 🔀 **Spring Boot 1.2.3 → 2.7.18 is a large jump.** Behaviour, auto-configuration and Actuator defaults changed between these versions. The Kiro CLI notes say that in Boot 2.x the Actuator endpoints move under `/actuator`.  
- 🧬 **cglib removal.** `cglib-nodep` 3.1 is gone. Any code that relied on that jar would need Spring's repackaged CGLIB instead. The build compiles without it.  
- 🏷️ **ETag helper change.** The test helper `Sha512ShallowEtagHeaderFilter` now emits a `W/` prefix for weak ETags, which is new output.  
- 🧓 **Old libraries remain.** `org.codehaus.groovy:groovy-all` is still declared in `pom.xml`. The Kiro CLI notes say its version, Groovy 2.4.3, does not support Java 17.  
- 🌐 **Unreachable repository.** OpenRewrite reported "Failed to access maven repository http://repo.spring.io/milestone/". That plain-HTTP repository is still declared in `pom.xml`.  
- 📦 **No added dependencies,** so there is no new classloader-conflict risk from packaged APIs.  
  
## ⏱️ Time savings  
- 💰 **Estimated time saved:** 180 minute(s)  
- ⚡ **Transformation duration:** 4 minute(s)  
  
## 🚀 Next steps  
- ▶️ **Start the app:** enable the smoke-start tier (`ENABLE_RUNTIME_VALIDATION=true`) or boot the app in a test environment to check runtime behaviour on Java 17.  
- 🧪 **Run the Spock tests:**  
  - Add a Groovy compiler plugin, such as gmavenplus.  
  - Upgrade Groovy and Spock to versions that support Java 17.  
  - Confirm that `DownloadControllerSpec` runs and passes, so test results are recorded.  
- 📡 **Check Actuator users:** review monitoring and health checks against the Spring Boot 2.x Actuator endpoint paths and defaults.  
- 🌐 **Clean up the repository:** remove the unreachable `http://repo.spring.io/milestone/` repository from `pom.xml`.  
- 📦 **Upgrade old libraries:**  
  - Guava 18.0, commons-io 2.4 and Groovy 2.4.3 remain.  
  - Check the scope of `groovy-all` and move it to test scope if only the tests use it.  
- 🔍 **Review behaviour:** check the Boot 1.x → 2.7 configuration and behaviour changes against the application's endpoints, including ETag, `If-Modified-Since` and HEAD handling.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 4 minute(s)
- ⏳ Estimated time saved: 180 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 12.02  
Includes 1.18 for job setup (startup smoke test and knowledge-base indexing).  
