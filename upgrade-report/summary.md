# 📋 Executive Report: Java 25 Upgrade, Iteration 36  
  
## 🧭 Executive summary  
- 🎯 **Scope:** the `download-server` Maven project (`com.nurkiewicz.download:download-server`) was upgraded to Java 25 (`release 25`) and from Spring Boot 1.2.3.RELEASE to Spring Boot 4.0.10. The upgrade used OpenRewrite recipes followed by Kiro CLI build-fix passes.  
- ✅ **Build:** the post-upgrade verification build ends in `BUILD SUCCESS`. The 8 main and 3 test source files compile with `javac [debug parameters release 25]`.  
- 🧪 **Tests:** test execution not recorded. Surefire 3.5.6 was invoked, but `post-upgrade-verification.log` shows no test totals.  
- 🌱 **Spring:** runtime-validation.log reports "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." The Spring Boot 4.0.10 parent now manages the Spring Framework versions.  
- ⚠️ **Runtime:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started, so its runtime behaviour is **unverified**.  
- 📦 **Branch:** commit `3bf0d64` ("Upgrade to Java 25 on Iteration 36") on `main-upgrade-itr-36` changes 5 files, with 90 insertions and 33 deletions.  
  
## 🛠️ Application changes  
- 📄 Files changed (5, from report.json and `git diff --stat 73556be..HEAD`):  
  - `pom.xml` (73 lines changed)  
  - `src/main/java/com/nurkiewicz/download/DownloadController.java` (2 lines removed)  
  - `src/main/java/com/nurkiewicz/download/MainApplication.java` (1 line changed)  
  - `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` (10 lines changed)  
  - `summary.md` (36 lines added, the in-repo build-fix notes)  
- ➕ **Dependencies added:**  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST). Compile scope, since the diff declares no scope. The Boot parent manages its version. It replaces `spring-boot-starter-web` through OpenRewrite `ChangeDependency`.  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST). **Test** scope, so it is not packaged with the application.  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST). Build plugin running the `properties` goal, with no dependency scope.  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST). Build plugin configured with `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}`, with no dependency scope.  
- ➖ **Dependencies removed:**  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
- ⬆️ **Dependencies upgraded:**  
  - `org.springframework.boot:spring-boot-starter-parent` (1.2.3.RELEASE -> 4.0.10)  
  - `org.apache.maven.plugins:maven-compiler-plugin` (3.0 -> 3.16.0)  
  - `com.google.guava:guava` (18.0 -> 29.0-jre)  
  - `org.springframework:spring-test` (4.1.6.RELEASE -> LATEST)  
  - `commons-codec:commons-codec` (1.10 -> LATEST)  
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
  - `org.codehaus.groovy:groovy-all` (2.4.3 -> LATEST). The diff pins it to 2.4.16 in a new `<dependencyManagement>` block.  
  - ℹ️ "LATEST" entries had their explicit `<version>` removed in the diff, so the Spring Boot 4.0.10 parent now manages them.  
  
## 🧰 Tools used  
- ☕ **Java SDK:** Java 25. The compiler plugin is configured with `<release>${java.version}</release>` and `java.version` set to 25.  
- 🔧 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0, run twice:  
  - `com.amazonaws.java.migrate.UpgradeToJava25` changed `pom.xml` and `MainApplication.java`. Sub-recipes included `UseMavenCompilerPluginReleaseConfiguration`, `UpgradePluginVersion`, `PrimitiveWrapperClassConstructorToValueOf` and `AddMockitoJavaAgentToMavenSurefirePlugin`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` changed `pom.xml` and `DownloadController.java`. Sub-recipes included `UpgradeSpringBoot_3_5`, `UpgradeParentVersion`, `ChangeDependency` and `JavaxServletToJakartaServlet`.  
- 🤖 **Amazon Kiro CLI:** kiro-cli 2.35.1 with model `claude-opus-5.5`. It ran the automated build-fix pass and the additional-prompt pass, and each succeeded on its first attempt.  
  
## 💻 Code changes  
- 🔢 **`MainApplication.java`:** `new Integer("1234")` -> `Integer.valueOf("1234")`. OpenRewrite made this change with `PrimitiveWrapperClassConstructorToValueOf`.  
- 💉 **`DownloadController.java`:** removed `@Autowired` from the single constructor and removed the now-unused `org.springframework.beans.factory.annotation.Autowired` import.  
- 🏷️ **`Sha512ShallowEtagHeaderFilter.java` (test source):** Kiro made this fix for the compile error `method does not override or implement a method from a supertype`. The override changed from `generateETagHeaderValue(byte[] bytes)` to `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. It reads the stream with Guava `ByteStreams.toByteArray`, still hashes with SHA-512, and adds the `W/` prefix when `isWeak` is true.  
- 🧱 **`pom.xml`:**  
  - Spring Boot parent moved to 4.0.10. Added `java.version` 25 and an empty `argLine` property. Removed the unused `spring.version` (4.1.1.RELEASE) property.  
  - Compiler plugin moved to 3.16.0 and `<source>/<target> 1.8` changed to `<release>`.  
  - Removed explicit Spring versions (4.1.6.RELEASE) and the duplicate `spring-test` declaration that Maven had flagged as non-unique.  
  - Added a `junit:junit` exclusion on `spock-core`.  
  
## ⚠️ Risks  
- 🚫 **Runtime unverified:** the smoke-start tier was skipped, so the application was never started in this iteration. Only compilation and packaging are confirmed.  
- 🧪 **Test execution not recorded:** behaviour parity with the pre-upgrade code is not shown by any test totals. The Groovy Spock spec `DownloadControllerTest.groovy` uses Spock `1.0-groovy-2.4` and Groovy 2.4.x. The POM has no Groovy compiler plugin, and those versions predate Spring 7 and JDK 25.  
- 🏷️ **ETag behaviour:** `Sha512ShallowEtagHeaderFilter` now follows the Spring 7 `InputStream`/weak-ETag contract. Its output is not covered by a recorded test.  
- 🧩 **Classloader conflict:** `spring-boot-starter-webmvc` is at compile scope and brings an embedded servlet container with the servlet API. This conflicts if the jar is deployed into an external servlet container or application server that already provides `jakarta.servlet`. `jakarta.servlet-api` itself is test-scoped and is not packaged.  
- 📚 **Old or legacy libraries:** `commons-io` 2.4, `guava` 29.0-jre, `cglib-nodep` 3.1 and `groovy-all` 2.4.16 (at compile scope) were kept. The in-repo `summary.md` lists known CVEs for the `commons-io` and `guava` versions.  
- 🕰️ **Deprecated API:** the final build warns that `FileSystemPointer.java` "uses or overrides a deprecated API".  
- 🌐 **Insecure repository:** code-upgrade.log shows `Failed to access maven repository http://repo.spring.io/milestone/`. The POM still declares this plain-HTTP repository.  
- 🔀 **Mixed Boot and non-Boot artifacts:** code-upgrade.log notes that plain (non-Boot) Spring artifacts were detected alongside the Spring Boot path. They are now version-managed by the Boot parent, and runtime-validation reports "OK (static)".  
  
## ⏱️ Time savings  
- ⚡ **Transformation duration:** 4 minute(s)  
- 💰 **Estimated time saved:** 60 minute(s)  
  
## 🚀 Next steps  
- ▶️ Enable the smoke-start check (`export ENABLE_RUNTIME_VALIDATION=true`) or start the application in a test environment to verify runtime behaviour.  
- 🧪 Make the Spock spec run: add `gmavenplus-plugin`, move to Spock 2.x with Groovy 4 (`org.apache.groovy`), and confirm that Surefire reports test totals.  
- 🔐 Upgrade `commons-io` to 2.14.0 or later and `guava` to 32.0.0-jre or later. Then re-run the build and tests.  
- 🧹 Remove unused `cglib-nodep`. Move `groovy-all` out of compile scope. Drop the `http://repo.spring.io/milestone/` repository.  
- 🕰️ Rebuild with `-Xlint:deprecation` and replace the deprecated API used in `FileSystemPointer.java`.  
- 📦 Confirm the deployment target is the executable jar with the embedded container. If you deploy to an external servlet container instead, switch to WAR packaging and mark the container as `provided`.  
- 👀 Review the iteration branch `main-upgrade-itr-36` (commit `3bf0d64`) before raising a pull request.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 4 minute(s)
- ⏳ Estimated time saved: 60 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 11.24  
Includes 1.19 for job setup (startup smoke test and knowledge-base indexing).  
