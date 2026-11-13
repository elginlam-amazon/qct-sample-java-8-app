# 🚀 Java 25 Upgrade – Iteration 10 Executive Report  
  
## 📋 Executive summary  
- 🎯 `download-server` was upgraded from Java 1.8 to Java 25 (`maven-compiler-plugin` `<release>${java.version}</release>`, `java.version` = 25). Spring Boot moved from 1.2.3.RELEASE to 4.0.9 (`spring-boot-starter-parent`).  
- 🔧 OpenRewrite applied `com.amazonaws.java.migrate.UpgradeToJava25` and `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which included `JavaxMigrationToJakarta`. Kiro CLI build-fix passes then fixed the remaining compile errors and got the Spock tests compiling.  
- ✅ The post-upgrade verification build succeeded, with 12 automated tests run (`DownloadControllerSpec`: 0 failures, 0 errors, 0 skipped).  
- 🌱 Spring: runtime-validation.log reports "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." This is a Spring Boot upgrade, so the Boot 4.0.9 parent manages the Spring Framework version.  
- ⚠️ The runtime smoke start was not run: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." Application runtime behaviour is **unverified**.  
- 📦 Changes were committed to the iteration branch as commit `971c456` ("Upgrade to Java 25 on Iteration 10"): 6 files, 101 insertions, 47 deletions.  
  
## 🗂️ Application changes  
- 📄 `pom.xml`: Java 25 target, Spring Boot 4.0.9 parent, jakarta servlet API, Spock 2.4 / Groovy (`org.apache.groovy`), new build plugins, and Spring versions now managed by the Boot BOM.  
- ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java`: removed `@Autowired` from the single constructor.  
- ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`: replaced a deprecated wrapper constructor.  
- 📝 `src/main/resources/logback.xml`: the console appender is now named and referenced.  
- 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: ported to the Spring 7 `ShallowEtagHeaderFilter` API.  
- 🗒️ `summary.md`: added the in-repo upgrade notes.  
  
### ➕ Dependencies added  
- 🌐 `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, BOM-managed): compile scope (no `<scope>` in the diff). It replaces `spring-boot-starter-web`.  
- 🧩 `jakarta.servlet:jakarta.servlet-api` (version LATEST, BOM-managed): **test** scope, so it is not packaged with the application.  
- 🟢 `org.apache.groovy:groovy` (version LATEST, BOM-managed): **test** scope.  
- 🔌 `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST): build plugin running the `properties` goal (no dependency scope).  
- 🔌 `org.codehaus.gmavenplus:gmavenplus-plugin` (version 4.3.1): build plugin running `addTestSources` and `compileTests`, with `targetBytecode=${java.version}` (no dependency scope).  
- 🔌 `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST): build plugin configuration with `*Test`, `*Tests` and `*Spec` includes and a byte-buddy `-javaagent` argLine (no dependency scope).  
  
### ➖ Dependencies removed  
- ❌ `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
- ❌ `org.codehaus.groovy:groovy-all` (version 2.4.3)  
  
### ⬆️ Dependencies upgraded  
- `org.springframework:spring-test`: 4.1.6.RELEASE -> LATEST (the duplicate declaration was also removed)  
- `org.spockframework:spock-spring`: 1.0-groovy-2.4 -> ${spock.version} (`2.4-groovy-5.0`)  
- `commons-codec:commons-codec`: 1.10 -> LATEST  
- `org.springframework.boot:spring-boot-starter-actuator`: 1.2.3.RELEASE -> LATEST  
- `org.springframework.boot:spring-boot-starter-test`: 1.2.3.RELEASE -> LATEST  
- `org.spockframework:spock-core`: 1.0-groovy-2.4 -> ${spock.version} (`2.4-groovy-5.0`)  
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
- `org.apache.maven.plugins:maven-compiler-plugin`: 3.0 -> 3.16.0  
- ℹ️ "LATEST" means the explicit version was removed and the Spring Boot 4.0.9 BOM now manages it.  
  
## 🛠️ Tools used  
- ☕ Java SDK: JDK 25. The build compiled with `javac [debug parameters release 25]`.  
- 🔄 OpenRewrite: `rewrite-maven-plugin` 6.42.0 with the recipes `com.amazonaws.java.migrate.UpgradeToJava25` and `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`.  
- 🤖 Amazon Kiro CLI: `kiro-cli 2.35.1`, model `claude-opus-5.5`, used for the automated build fix and report generation.  
  
## 💻 Code changes  
- 🔁 `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")` (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`).  
- 🧹 `DownloadController.java`: removed the `@Autowired` annotation and its import from the constructor (OpenRewrite `SpringBoot2BestPractices`).  
- 🔐 `Sha512ShallowEtagHeaderFilter.java` (Kiro): `generateETagHeaderValue(byte[] bytes)` -> `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. It hashes `ByteStreams.toByteArray(inputStream)` with SHA-512 and adds the `W/` prefix when `isWeak` is true.  
- 📝 `logback.xml` (Kiro): the inline `<appender>` inside `<root>` became a named `CONSOLE` appender, with `<appender-ref ref="CONSOLE"/>` in `<root>`. The pattern is unchanged.  
- 📄 `pom.xml`: `<source>/<target>1.8` -> `<release>${java.version}</release>`. Added `<argLine></argLine>`, `java.version=25` and `spock.version=2.4-groovy-5.0`. Removed the unused `spring.version` (4.1.1.RELEASE) property and the `groovy-all` exclusion on `spock-spring`.  
  
## ⚠️ Risks  
- 🚫 **Runtime unverified**: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was never started, so startup, auto-configuration and actuator behaviour on Spring Boot 4.0.9 and Java 25 have not been checked.  
- 🌱 Spring static check: "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." This is a static check only.  
- ℹ️ code-upgrade.log notes that the Spring Boot upgrade path was used for the whole reactor although plain (non-Boot) Spring artifacts were also detected. Those artifacts are single-module and now BOM-managed, but they should be confirmed.  
- 📈 The major-version jump (Boot 1.2 -> 4.0, Spring 4.1 -> 7) carries behaviour-change risk beyond what the 12 controller tests cover, for example in header parsing, `Optional` `@RequestHeader` binding and actuator endpoints.  
- 🧪 Test coverage: the 12 Spock tests exercise `DownloadController` through MockMvc only. Nothing tests `FileSystemStorage` or the throttled streaming under load.  
- 🔐 `Sha512ShallowEtagHeaderFilter` now reads the whole response stream into memory to hash it. It lives in test sources only, so production is not affected.  
- 🧩 Classloader conflicts: `jakarta.servlet-api` is test-scoped, so no container-provided API is packaged with the application. `spring-boot-starter-webmvc` (compile) brings the embedded server, which is expected for a Boot application.  
- ⏳ The build uses deprecated APIs: `FileSystemPointer.java` "uses or overrides a deprecated API" (post-upgrade-verification.log). It compiles today but may break on a future library upgrade.  
- 🏚️ Older libraries remain: `guava` 29.0-jre, `commons-io` 2.4 and `cglib-nodep` 3.1. The plain-HTTP `repo.spring.io/milestone` repository is still declared.  
  
## ⏱️ Time savings  
- ⌛ Transformation duration: **16 minute(s)**  
- 💰 Estimated time saved: **480 minute(s)**  
  
## 🔜 Next steps  
- ▶️ Run the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`), or start the app in a dev environment, and check `/download/{uuid}` plus the actuator endpoints.  
- 🧪 Add integration tests for `FileSystemStorage`, the throttled streaming, and the `If-None-Match` / `If-Modified-Since` handling end to end.  
- 🔍 Review the actuator configuration against Spring Boot 4 defaults (endpoint exposure and base path).  
- 🧹 Remove `cglib-nodep` (no usages found), the plain-HTTP milestone repository, and the leftover unused `Integer temp` line in `MainApplication`.  
- ⬆️ Consider upgrading `guava` and `commons-io` to current releases and running `-Xlint:deprecation` to find and replace the deprecated API used in `FileSystemPointer.java`.  
- ✂️ Consider trimming the explicit `spring-*` dependencies that `spring-boot-starter-webmvc` already provides.  
- 👀 Do a manual review of the branch before merging.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 16 minute(s)
- ⏳ Estimated time saved: 480 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 15.74  
Includes 1.20 for job setup (startup smoke test and knowledge-base indexing).  
