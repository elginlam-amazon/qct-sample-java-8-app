# Executive Report: Java 25 Upgrade (Iteration 5): download-server  
  
## 📋 Executive summary  
- 🎯 This iteration moved `download-server` from Java 8 / Spring Boot 1.2.3.RELEASE to Java 25 (`release 25`) on the `spring-boot-starter-parent` 4.0.8 parent. Two OpenRewrite passes did most of the work (`UpgradeToJava25` and `UpgradeSpringBoot_4_0`). A Kiro CLI build-fix pass then fixed the one remaining test-compilation error.  
- ✅ The post-upgrade verification build (`mvn clean install`) finished with BUILD SUCCESS. Main sources (8 files) and test sources (3 files) compiled with `release 25`.  
- 🧪 No automated tests were discovered or run. Verification covered compilation only.  
- ⚠️ Runtime validation logged a WARNING: *"WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required."*  
- 🔴 The Spring upgrade is therefore reported as incomplete and needs follow-up. It is not reported as upgraded.  
- ⏭️ The smoke-start tier was skipped (*"Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)."*). The application was never started, so its runtime behaviour is unverified.  
- 🧾 Commit `91bd791` on `oom-repro-274-upgrade-itr-5` changed 5 files: 73 insertions, 32 deletions.  
  
## 🛠️ Application changes  
- 📦 `pom.xml`: Java version, parent, dependencies and build plugins (72 lines changed)  
- ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java`: removed constructor `@Autowired` (2 lines)  
- ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`: replaced a deprecated wrapper constructor (2 lines)  
- 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: moved to the new ETag method signature (9 lines)  
- 📝 `summary.md`: build-fix notes from the Kiro CLI pass (20 lines)  
  
### ➕ Dependencies added (report.json, scope from the `pom.xml` diff)  
- `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST). Compile scope (no `<scope>`). OpenRewrite's `ChangeDependency` put it in place of `spring-boot-starter-web`.  
- `jakarta.servlet:jakarta.servlet-api` (version LATEST). Test scope, so it is not packaged with the application.  
- `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST). Build plugin, so no dependency scope. Resolved as `dependency:3.9.0` in the verification build. Runs the `properties` goal.  
- `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST). Build plugin, so no dependency scope. Resolved as `surefire:3.5.6` in the verification build. Its `argLine` adds `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}`.  
  
### ➖ Dependencies removed (report.json)  
- `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
  
### ⬆️ Dependencies upgraded (report.json)  
- `org.springframework:spring-test`: 4.1.6.RELEASE -> LATEST  
- `commons-codec:commons-codec`: 1.10 -> LATEST  
- `org.springframework.boot:spring-boot-starter-actuator`: 1.2.3.RELEASE -> LATEST  
- `org.springframework.boot:spring-boot-starter-test`: 1.2.3.RELEASE -> LATEST  
- `com.google.guava:guava`: 18.0 -> 29.0-jre  
- `org.springframework:spring-beans`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-context`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-context-support`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-webmvc`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-web`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-aop`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-core`: 4.1.6.RELEASE -> LATEST  
- `org.springframework:spring-expression`: 4.1.6.RELEASE -> LATEST  
- `org.codehaus.groovy:groovy-all`: 2.4.3 -> LATEST. The diff pins it to 2.4.16 in a new `<dependencyManagement>` block.  
- `org.springframework.boot:spring-boot-starter-parent`: 1.2.3.RELEASE -> 4.0.8  
- `org.apache.maven.plugins:maven-compiler-plugin`: 3.0 -> 3.16.0  
- ℹ️ "LATEST" means the diff removed the explicit `<version>` and left the version to the 4.0.8 parent's dependency management.  
  
## 🧰 Tools used  
- ☕ Java SDK: Amazon Corretto OpenJDK 25.0.4.1 (build 25.0.4.1+10-LTS), with Apache Maven 3.9.16  
- 🔧 OpenRewrite: `rewrite-maven-plugin` 6.42.0 ran two recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which changed `pom.xml` and `MainApplication.java`  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which changed `pom.xml` and `DownloadController.java`  
- 🤖 Amazon Kiro CLI: `kiro-cli 2.14.2` with model `claude-opus-5.5`. It finished the automated build-fix pass on the first attempt.  
  
## 💻 Code changes (as shown in the diff)  
- ☕ `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")` (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`)  
- ☕ `DownloadController.java`: removed `@Autowired` from the constructor `DownloadController(FileStorage storage)` and dropped its `org.springframework.beans.factory.annotation.Autowired` import (OpenRewrite `NoAutowiredOnConstructor`)  
- 🧪 `Sha512ShallowEtagHeaderFilter.java` (Kiro CLI build fix). The original override was `generateETagHeaderValue(byte[] bytes)`, which failed with *"method does not override or implement a method from a supertype"*. The new override is:  
  - signature `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`  
  - hash computed as `Hashing.sha512().hashBytes(inputStream.readAllBytes())`  
  - return value `(isWeak ? "W/" : "") + "\"" + hash + "\""`  
  - new imports `java.io.IOException` and `java.io.InputStream`  
- 📦 `pom.xml`:  
  - Added `<java.version>25</java.version>` and an empty `<argLine></argLine>` property.  
  - Changed the compiler `<source>1.8</source>`/`<target>1.8</target>` settings to `<release>${java.version}</release>`.  
  - Moved the parent from 1.2.3.RELEASE to 4.0.8.  
  - Removed the explicit `4.1.6.RELEASE` / `1.2.3.RELEASE` versions and a duplicate `spring-test` declaration. That duplicate is what caused the earlier Maven warning *"must be unique: org.springframework:spring-test:jar"*.  
  - Added a `junit:junit` exclusion on `spock-core`.  
  
## ⚠️ Risks  
- 🔴 **Runtime-validation WARNING:** the lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. The validator says the app will crash at startup during component scanning, and that Kiro or manual follow-up is required. The Spring upgrade must be treated as incomplete.  
  - 🔍 My assessment: this may be a false positive. The diff removes every explicit `4.1.6.RELEASE` Spring version, which leaves the versions to the 4.0.8 parent. However, the unused property `<spring.version>4.1.1.RELEASE</spring.version>` is still in `pom.xml`, and a static check may be reading it.  
  - ❗ code-upgrade.log has no "Verified: effective Spring Framework version..." line confirming the effective version. The warning stands until it is disproven.  
- ⏭️ **Runtime unverified:** the smoke-start tier was skipped, so nothing shows that the application starts or serves requests on Java 25 / Spring Boot 4.0.8.  
- 🧪 **No test evidence:**  
  - No automated tests were discovered or run; the parity evidence reports 0 test classes before and after the upgrade.  
  - The ETag, `If-Modified-Since` and HEAD-request behaviour of `DownloadController` is not covered by any executed test.  
  - The Spock spec under `src/test/groovy` was not part of the javac test compilation (3 Java test files were compiled).  
- 🔀 **Namespace jump:** the Boot 4 recipe chain applied `JavaxMigrationToJakarta` / `JavaxServletToJakartaServlet`, so the application is now on the jakarta line. Any external consumer or deployment target that expects `javax.*` is incompatible.  
- 🧩 **Classloader conflicts:**  
  - `jakarta.servlet-api` is test-scoped, so it is not packaged.  
  - `spring-boot-starter-webmvc` is compile-scoped. Whatever servlet container or Servlet API it brings in transitively will ship with the application. If the app is deployed to an external servlet container that provides its own Servlet API, check for duplicate API classes.  
- 🧪 **Legacy test stack:**  
  - `spock-core` / `spock-spring` stay at `1.0-groovy-2.4`, and `groovy-all` is pinned to 2.4.16 alongside a Spring Boot 4 / Java 25 build.  
  - `cglib-nodep` 3.1 remains in compile scope.  
  - The dependency-version diff did not change any of these.  
- 🐛 **ETag change:** the `Sha512ShallowEtagHeaderFilter` override now emits a `W/` prefix whenever `isWeak` is true. This is a small behavioural difference from the original method.  
- 💣 **OOM test fixture:**  
  - The `vutk-274-oom-hog` `exec-maven-plugin` execution, bound to `validate`, ran during verification ("buffering 6 GiB in tail RSS").  
  - Its own POM comment says "Never merge this into main".  
- 📉 **Deprecation:** the verification build reported that `FileSystemPointer.java` uses or overrides a deprecated API.  
  
## ⏱️ Time savings  
- ⏳ Transformation duration: 19 minute(s)  
- 💰 Estimated time saved: 240 minute(s)  
  
## 🚀 Next steps  
- 🔴 **Resolve the Spring warning:**  
  - Remove the stale `<spring.version>4.1.1.RELEASE</spring.version>` property.  
  - Confirm the effective Spring Framework version with `mvn help:effective-pom` / `mvn dependency:tree`.  
  - Re-run runtime validation until the "< 5.3" warning clears.  
- ▶️ **Prove runtime behaviour:** re-run runtime validation with `ENABLE_RUNTIME_VALIDATION=true` so the smoke-start tier actually boots the app.  
- 🧪 **Restore test coverage:**  
  - Add Groovy compilation (for example gmavenplus) and move Spock/Groovy to versions that support Java 25 and the JUnit Platform, so the `DownloadControllerSpec` scenarios run.  
  - Alternatively, port those scenarios to JUnit 5.  
- 🔍 **Review the ETag change:** confirm the `W/` weak-ETag handling in `Sha512ShallowEtagHeaderFilter` matches the intended behaviour.  
- 🧹 **Clean up the build:**  
  - Assess removing `cglib-nodep` 3.1.  
  - Review the `http://repo.spring.io/milestone/` repository entry.  
  - Consider moving `guava` beyond 29.0-jre.  
  - Fix the deprecated API use in `FileSystemPointer.java`.  
- 💣 **Keep the OOM fixture out of main:** make sure the `vutk-274-oom-hog` fixture is not merged into `main`.  
- 🧩 **Check the deployment model:** confirm the app is run as a standalone Boot jar rather than on an external servlet container that would conflict with the compile-scoped web starter.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: none discovered or run (compilation only)
- ⏱️ Transformation duration: 19 minute(s)
- ⏳ Estimated time saved: 240 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - ⚠️ WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required.
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 9.43  
Includes 0.89 for job setup (startup smoke test and knowledge-base indexing).  
