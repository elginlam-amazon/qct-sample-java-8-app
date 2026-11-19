# ☕ Java 17 Upgrade – Iteration 2 Executive Report  
  
## 📋 Executive summary  
- 🎯 **Scope:** the `download-server` Spring Boot application (single Maven module) was upgraded from Java 8 (`<source>1.8</source>` / `<target>1.8</target>`) to Java 17 (`<release>${java.version}</release>`, `java.version` = 17).  
- 🔧 **How:** OpenRewrite recipe `com.amazonaws.java.migrate.UpgradeToJava17` changed `pom.xml` and `MainApplication.java`. The Amazon Kiro CLI build-fix pass then moved the Spring Boot parent from 1.2.3.RELEASE to 2.7.18, aligned the Spring, Groovy and Spock dependencies, and fixed one test compilation error.  
- ✅ **Build:** the post-upgrade verification build finished with `BUILD SUCCESS`.  
- 🧪 **Tests:** test execution was not recorded. Surefire 2.22.2 ran, but the log shows no test totals.  
- 🌱 **Spring:** runtime validation reports "OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3)." The project stays on the **javax** namespace.  
- ⚠️ **Runtime not verified:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started, so its runtime behaviour on Java 17 is unverified.  
- 📦 **Committed:** 4 files changed, 45 insertions and 43 deletions on branch `ec2-jobrole-mwnu2wv3-or-kiro-upgrade-itr-2` (commit `b0ac410`).  
  
## 🛠️ Application changes  
- 📄 **Files changed (4):**  
  - `pom.xml`  
  - `src/main/java/com/nurkiewicz/download/MainApplication.java`  
  - `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  - `summary.md` (new file with in-repo upgrade notes)  
- ➕ **Dependencies added:**  
  - `org.codehaus.groovy:groovy` (version LATEST). The diff declares it with no `<version>` and no `<scope>`, so the Spring Boot 2.7.18 parent manages the version and the scope is the default **compile** scope.  
- ➖ **Dependencies removed:**  
  - `cglib:cglib-nodep` (version 3.1)  
  - `org.codehaus.groovy:groovy-all` (version 2.4.3)  
- ⬆️ **Dependencies upgraded:**  
  - `org.springframework.boot:spring-boot-starter-parent` (1.2.3.RELEASE -> 2.7.18)  
  - `org.springframework.boot:spring-boot-starter-web` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-actuator` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-test` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework:spring-test` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-beans` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context-support` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-webmvc` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-web` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-aop` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-core` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-expression` (4.1.6.RELEASE -> LATEST)  
  - `org.spockframework:spock-spring` (1.0-groovy-2.4 -> 2.3-groovy-3.0)  
  - `org.spockframework:spock-core` (1.0-groovy-2.4 -> 2.3-groovy-3.0)  
  - `commons-codec:commons-codec` (1.10 -> 1.17.2)  
  - `org.apache.maven.plugins:maven-compiler-plugin` (3.0 -> 3.16.0)  
- ℹ️ **About "LATEST":** for these entries the diff removes the explicit `<version>`, so the Spring Boot 2.7.18 parent manages the version. The diff also replaces the `spring.version` property (4.1.1.RELEASE) with `spring-framework.version` set to **5.3.39**.  
  
## 🧰 Tools used  
- ☕ **Java SDK:** compilation targets Java 17 (`javac [debug parameters release 17]` in the verification log).  
- 🔄 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0 running the recipe `com.amazonaws.java.migrate.UpgradeToJava17`. Its sub-recipes included `UseMavenCompilerPluginReleaseConfiguration`, `UpdateMavenProjectPropertyJavaVersion`, `UpgradePluginVersion`, `UpgradeDependencyVersion` (commons-codec 1.17.x) and `PrimitiveWrapperClassConstructorToValueOf`.  
- 🤖 **Amazon Kiro CLI:** kiro-cli 2.35.1 with model **claude-opus-5.5**, which succeeded on the first attempt. This pass did the build fixes and the dependency alignment.  
- 🏗️ **Build:** Apache Maven with maven-compiler-plugin 3.16.0 and maven-surefire-plugin 2.22.2.  
  
## 💻 Code changes  
- 🔢 **`MainApplication.java`** (OpenRewrite): `new Integer("1234")` became `Integer.valueOf("1234")`.  
- 🏷️ **`Sha512ShallowEtagHeaderFilter.java`** (test code, Kiro CLI):  
  - The override `generateETagHeaderValue(byte[] bytes)` became `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`, matching the changed method in Spring's `ShallowEtagHeaderFilter`.  
  - The method still hashes with SHA-512. It now reads the stream with `ByteStreams.toByteArray(inputStream)` and prefixes `W/` when `isWeak` is true.  
  - It adds imports for `com.google.common.io.ByteStreams`, `java.io.IOException` and `java.io.InputStream`.  
- 📦 **`pom.xml`:**  
  - Adds `java.version` 17 and replaces `<source>1.8</source>` / `<target>1.8</target>` with `<release>${java.version}</release>`.  
  - Moves the Boot parent to 2.7.18 and replaces `spring.version` 4.1.1.RELEASE with `spring-framework.version` 5.3.39.  
  - Removes the explicit 1.2.3.RELEASE and 4.1.6.RELEASE versions from the Boot starters and Spring artifacts.  
  - Removes the duplicate `spring-test` declaration. The original build log warned about it ("must be unique ... duplicate declaration").  
  - Removes `cglib-nodep` 3.1 and the `groovy-all` exclusion from `spock-spring`, and replaces `groovy-all` 2.4.3 with `groovy`.  
  
## ⚠️ Risks  
- 🚫 **Runtime unverified:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was never started, so startup and behaviour on Java 17 are not confirmed.  
- 🧪 **No recorded test results:**  
  - Test execution was not recorded, and the parity check found 0 test classes before and 0 after the upgrade.  
  - The Kiro CLI output in code-upgrade.log notes that the Groovy Spock spec `DownloadControllerSpec` is never compiled or run by the Maven build. This was already true before the upgrade.  
  - In a one-off manual run, 11 of 12 specs failed with 404 because Groovy 3 inlines the `TEXT_FILE` constant at compile time. All 12 passed with `final` removed from that field. That edit was reverted and is not on the branch.  
- 🔀 **Big framework jump:** moving from Spring Boot 1.2.3 to 2.7.18 and Spring 4.1 to 5.3 crosses several major releases. Defaults for auto-configuration, Actuator endpoints and MVC behaviour changed along the way, and the build does not exercise them.  
- 📚 **Groovy packaged at compile scope:** `org.codehaus.groovy:groovy` uses the default compile scope, so it ships inside the application jar even though only tests use Groovy. This repeats the old `groovy-all` setup. Neither Java 17 nor the embedded Spring Boot runtime provides Groovy, so the diff shows no server-provided API packaged into the app. A Groovy version clash would only be a risk if the jar is deployed somewhere that supplies its own Groovy.  
- 🏷️ **ETag format:** the test ETag filter now emits `W/"..."` when Spring requests a weak ETag. Before the upgrade it always emitted a strong ETag.  
- 🌐 **Unreachable repository:** OpenRewrite logged "Failed to access maven repository http://repo.spring.io/milestone/". That entry is still in `pom.xml`.  
  
## ⏱️ Time savings  
- ⌛ **Transformation duration:** 12 minute(s)  
- 💰 **Estimated time saved:** 300 minute(s)  
  
## 🚀 Next steps  
- ▶️ **Smoke-test startup:** run the opt-in smoke-start check (`export ENABLE_RUNTIME_VALIDATION=true`) or a manual start in a test environment to confirm the app boots on Java 17.  
- 🧪 **Run the Spock spec in CI:** add `gmavenplus-plugin` (addTestSources, compileTests) and a Surefire include for `**/*Spec*`, and drop `final` from `TEXT_FILE` so all 12 specs can pass.  
- 🔍 **Review Boot 1.x -> 2.7 behaviour:** check Actuator endpoint exposure, the conditional request handling (ETag / If-Modified-Since) in `DownloadController`, and logging configuration.  
- 📦 **Consider test scope for Groovy:** declare `org.codehaus.groovy:groovy` as test scope if production code does not need it.  
- 🧹 **Clean up the POM:** remove the unreachable `http://repo.spring.io/milestone/` repository.  
- ⬆️ **Optional upgrades:** Guava 18.0 and commons-io 2.4 were left unchanged and could be moved to current versions in a later pass.  
- 🔭 **Further upgrades:** moving to Spring Boot 3.x (jakarta) or Java 21+ would be a separate, larger migration.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 12 minute(s)
- ⏳ Estimated time saved: 300 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 25.59  
Includes 1.18 for job setup (startup smoke test and knowledge-base indexing).  
