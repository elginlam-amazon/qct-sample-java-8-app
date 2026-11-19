# 📊 Executive Report: Java 25 Upgrade, Iteration 26  
  
## 🧭 Executive summary  
- 🎯 `download-server` was upgraded from Java 1.8 to **Java 25**, and from Spring Boot `1.2.3.RELEASE` to **Spring Boot `4.0.9`**. Two OpenRewrite recipes did the first pass, and Amazon Kiro CLI build-fix passes did the rest.  
- ✅ The post-upgrade verification build reports **BUILD SUCCESS**, with **12 tests run** in `DownloadControllerSpec` (0 failures, 0 errors, 0 skipped).  
- 🟢 Spring status: runtime validation reports "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." The Kiro build-fix pass reports that all Spring artifacts resolve to `7.0.10`. No artifact shows an old Spring version.  
- ⚠️ Runtime behaviour is **unverified**: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." Nothing in this iteration shows the application starting or running outside the test context.  
- 🧪 Before the upgrade, no test classes ran. The parity check reports "0 test class(es) ran before the upgrade, 1 after", so there is no before/after test comparison.  
- 🔁 This iteration fixes the iteration 25 compile failure in `Sha512ShallowEtagHeaderFilter.java`.  
  
## 🛠️ Application changes  
- 📁 **7 files changed** on branch `main-upgrade-itr-26` (107 insertions, 54 deletions):  
  - 📄 `pom.xml`  
  - ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java`  
  - ☕ `src/main/java/com/nurkiewicz/download/FileSystemPointer.java`  
  - ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`  
  - ⚙️ `src/main/resources/logback.xml`  
  - 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  - 📝 `summary.md`  
- ➕ **Dependencies and plugins added:**  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, BOM-managed). Scope: **compile** (default; no scope in the diff).  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST, BOM-managed). Scope: **test**, so it is not packaged with the application.  
  - `org.apache.groovy:groovy` (version LATEST, BOM-managed). Scope: **test**.  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST). This is a build plugin (`properties` goal), not a packaged dependency.  
  - `org.codehaus.gmavenplus:gmavenplus-plugin` (version 5.1.0). This is a build plugin (`addTestSources`, `compileTests`, `targetBytecode` = `${java.version}`).  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST). This is a build plugin that adds the Byte Buddy agent `argLine` and includes `**/*Test.java` and `**/*Spec.java`.  
- ➖ **Dependencies removed:**  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE), replaced by `spring-boot-starter-webmvc`.  
  - `cglib:cglib-nodep` (version 3.1).  
  - `org.codehaus.groovy:groovy-all` (version 2.4.3), which had compile scope.  
- ⬆️ **Dependencies upgraded:**  
  - `org.springframework:spring-test`: 4.1.6.RELEASE -> LATEST (BOM-managed). The duplicate `spring-test` declaration was also removed.  
  - `org.spockframework:spock-spring`: 1.0-groovy-2.4 -> 2.4-groovy-5.0  
  - `commons-codec:commons-codec`: 1.10 -> LATEST  
  - `org.springframework.boot:spring-boot-starter-actuator`: 1.2.3.RELEASE -> LATEST  
  - `org.springframework.boot:spring-boot-starter-test`: 1.2.3.RELEASE -> LATEST  
  - `org.spockframework:spock-core`: 1.0-groovy-2.4 -> 2.4-groovy-5.0  
  - `com.google.guava:guava`: 18.0 -> 29.0-jre  
  - `org.springframework:spring-beans`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-context`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-context-support`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-webmvc`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-web`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-aop`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-core`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-expression`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework.boot:spring-boot-starter-parent`: 1.2.3.RELEASE -> 4.0.9  
  - `org.apache.maven.plugins:maven-compiler-plugin`: 3.0 -> LATEST (explicit version removed; the post-upgrade build used 3.14.1 from the parent)  
- 🔧 **Build properties:** `java.version` = `25` and an empty `argLine` were added. The unused `spring.version` (4.1.1.RELEASE) property was removed. `maven-compiler-plugin` now uses `<release>${java.version}</release>` instead of `<source>`/`<target>` 1.8.  
  
## 🧰 Tools used  
- ☕ **Java SDK:** JDK 25 (Amazon Corretto 25.0.5.1), compiling with `release 25`.  
- 🔄 **OpenRewrite recipes:**  
  - `com.amazonaws.java.migrate.UpgradeToJava25` changed `pom.xml` and `MainApplication.java`. Sub-recipes that made changes include `UseMavenCompilerPluginReleaseConfiguration`, `UpdateMavenProjectPropertyJavaVersion`, `UpgradePluginVersion`, `UpgradeDependencyVersion`, `AddMockitoJavaAgentToMavenSurefirePlugin` and `PrimitiveWrapperClassConstructorToValueOf`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` changed `pom.xml` and `DownloadController.java`.  
- 🤖 **Amazon Kiro CLI:** version `kiro-cli 2.35.1`, model **claude-opus-5.5**. It ran the automated build-fix pass, the carry-over prompt from iteration 25, and a post-migration build-fix pass, each of which succeeded on its first attempt.  
  
## 💻 Code changes  
- ☕ `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")`.  
- ☕ `DownloadController.java`: removed `@Autowired` from the single constructor, and removed its `org.springframework.beans.factory.annotation.Autowired` import.  
- ☕ `FileSystemPointer.java`: `Files.hash(target, Hashing.sha512())` -> `Files.asByteSource(target).hash(Hashing.sha512())`.  
- 🧪 `Sha512ShallowEtagHeaderFilter.java`: the override `generateETagHeaderValue(byte[] bytes)` -> `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. It still hashes with SHA-512 (via `inputStream.readAllBytes()`) and now adds a `W/` prefix when `isWeak` is true. `java.io.IOException` and `java.io.InputStream` imports were added.  
- ⚙️ `logback.xml`: the unnamed inline `ConsoleAppender` inside `<root>` -> a top-level appender named `CONSOLE`, referenced via `<appender-ref ref="CONSOLE"/>`. The pattern and logger levels are unchanged.  
- 📄 `pom.xml`: the dependency, plugin and property changes are listed above.  
  
## ⚠️ Risks  
- 🚦 **Runtime unverified:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started.  
- 🟢 **Spring (static check only):** "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." This is a static check, not a runtime check.  
- 🔀 **Mixed Boot / non-Boot Spring note:** `code-upgrade.log` notes that plain (non-Boot) Spring artifacts were also detected and must be validated separately. In this project they are declared without versions, so the Boot 4.0.9 BOM manages them. The Kiro pass reports that they resolve to 7.0.10.  
- 📦 **Packaging and classloader:** `jakarta.servlet-api` is **test** scope, so it is not packaged. `spring-boot-starter-webmvc` is **compile** scope. If the application is ever deployed to an external servlet container instead of running standalone, check whether the server-provided servlet API conflicts with anything this starter packages.  
- 🧪 **Thin test baseline:** no tests ran before the upgrade, so there is no before/after test comparison. The 12 Spock tests cover only the download controller, through MockMvc.  
- 🔁 **Behaviour change in test helper:** `Sha512ShallowEtagHeaderFilter` now adds `W/` to weak ETags, and it reads the whole response stream into memory to hash it.  
- 🌐 **Repository warnings:** OpenRewrite could not reach `http://repo.spring.io/milestone/` (a plain-HTTP repository that is still declared in `pom.xml`) or several snapshot repositories.  
- 📌 **Explicit versions outside the BOM:** Spock `2.4-groovy-5.0`, Guava `29.0-jre`, commons-io `2.4` and gmavenplus-plugin `5.1.0` are pinned by hand. Guava and commons-io are old releases.  
- 🧷 **Spring 7 major-version jump:** the Spring 4.1 to 7 jump (Spring Boot 1.2 to 4.0) changes defaults beyond what the controller tests check, for example actuator endpoints.  
  
## ⏱️ Time savings  
- 🕒 **Transformation duration:** 9 minute(s).  
- 💰 **Estimated time saved:** 210 minute(s).  
  
## 🚀 Next steps  
- 🏃 Set `ENABLE_RUNTIME_VALIDATION=true` and rerun, so the smoke-start tier checks that the application starts.  
- 🔍 Review the 7 changed files, especially the `pom.xml` dependency changes and the `Sha512ShallowEtagHeaderFilter` weak-ETag handling.  
- 🧪 Add tests beyond `DownloadControllerSpec`, for example for `FileSystemStorage`, `ThrottlingInputStream` and actuator endpoints.  
- 🩺 Check actuator endpoint exposure and other Spring Boot 4 defaults against what you expect in dev, test and prod.  
- 🧹 Remove the unused plain-HTTP `repo.spring.io/milestone` repository from `pom.xml`.  
- ⬆️ Consider updating Guava (29.0-jre) and commons-io (2.4).  
- 📝 Rename `DownloadControllerTest.groovy` to match its class, `DownloadControllerSpec`.  
- 👁️ Do a manual code review before merging `main-upgrade-itr-26`.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 9 minute(s)
- ⏳ Estimated time saved: 210 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 19.29  
Includes 1.18 for job setup (startup smoke test and knowledge-base indexing).  
