# ☕ Java 25 Upgrade – Executive Report (Iteration 17)  
  
## 📋 Executive summary  
- 🚀 The `download-server` Maven project moved from Java 8 (`source`/`target` 1.8) and Spring Boot `1.2.3.RELEASE` to Java 25 (`<release>${java.version}</release>`, `java.version` = 25) and Spring Boot `4.0.9`. The work was committed to the iteration branch as `4f6fbc0 Upgrade to Java 25 on Iteration 17`.  
- 🔧 OpenRewrite applied two recipes: `com.amazonaws.java.migrate.UpgradeToJava25` and `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. Kiro CLI then fixed the remaining test compilation error, got the Spock test suite compiling and running, and corrected `logback.xml`.  
- ✅ The post-upgrade verification build succeeded, and 12 automated tests ran (0 failures, 0 errors, 0 skipped in `DownloadControllerSpec`).  
- 🌱 The Spring outcome is consistent with the runtime check, which reports *"OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)."* No Spring Framework version is pinned in `pom.xml`, so the Spring Boot 4.0.9 parent manages it.  
- ⚠️ **Runtime behaviour is unverified.** *"Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)."* The application was not started during this iteration.  
- ⚠️ `code-upgrade.log` notes that plain (non-Boot) Spring artifacts were found alongside Spring Boot, and that non-Boot modules are not upgraded by the Spring Boot recipe and must be validated separately (see Risks).  
  
## 🗂️ Application changes  
- 📦 6 files changed, 122 insertions, 48 deletions (`git diff --stat 73556be..HEAD`, matching `report.json`):  
  - 🛠️ `pom.xml`: parent, Java level, plugins, dependencies and a Spock BOM import.  
  - 🎮 `src/main/java/com/nurkiewicz/download/DownloadController.java`: redundant `@Autowired` removed.  
  - 🏁 `src/main/java/com/nurkiewicz/download/MainApplication.java`: deprecated boxing constructor replaced.  
  - 📝 `src/main/resources/logback.xml`: console appender declared correctly.  
  - 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: override moved to the new Spring Framework method signature.  
  - 📄 `summary.md`: upgrade notes in the repository root.  
- ➕ **Dependencies added** (`report.json`; scope from the `pom.xml` diff):  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, managed by the Boot parent): compile scope (no `<scope>`). It replaces `spring-boot-starter-web`.  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST, Boot-managed): **test** scope.  
  - `org.apache.groovy:groovy` (version LATEST, Boot-managed): **test** scope.  
  - `org.codehaus.gmavenplus:gmavenplus-plugin` (version 4.2.1): build plugin, runs `compileTests` with `targetBytecode` = `${java.version}`.  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST, Boot-managed): build plugin, runs the `properties` goal.  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST, Boot-managed): build plugin, with `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` and `**/*Test.java` / `**/*Spec.java` includes.  
- ➖ **Dependencies removed** (`report.json`):  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
  - `org.codehaus.groovy:groovy-all` (version 2.4.3)  
- ⬆️ **Dependencies upgraded** (`report.json`; "LATEST" means the explicit version was removed and the Boot parent or Spock BOM now manages it):  
  - `org.springframework:spring-test` (4.1.6.RELEASE -> LATEST). The duplicate `spring-test` declaration was also removed.  
  - `org.spockframework:spock-spring` (1.0-groovy-2.4 -> LATEST, via `spock-bom` `2.5-groovy-5.0`)  
  - `commons-codec:commons-codec` (1.10 -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-actuator` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-test` (1.2.3.RELEASE -> LATEST)  
  - `org.spockframework:spock-core` (1.0-groovy-2.4 -> LATEST, via `spock-bom` `2.5-groovy-5.0`)  
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
- 🧹 The unused `spring.version` property (`4.1.1.RELEASE`) was removed from `pom.xml`.  
  
## 🧰 Tools used  
- ☕ **Java SDK:** JDK 25 target (`java.version` 25, compiler `<release>` 25). The Kiro CLI pass reports building on Corretto 25.0.5.  
- 🔁 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0 with:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which changed `pom.xml` and `MainApplication.java` (including `PrimitiveWrapperClassConstructorToValueOf`, `UseMavenCompilerPluginReleaseConfiguration` and `AddMockitoJavaAgentToMavenSurefirePlugin`).  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which changed `pom.xml` and `DownloadController.java` (including `UpgradeParentVersion` to 4.0.x, `ChangeDependency` from `spring-boot-starter-web` to `spring-boot-starter-webmvc`, `JavaxServletToJakartaServlet` / `AddDependency`, `RemoveDuplicateDependencies` and `NoAutowiredOnConstructor`).  
- 🤖 **Amazon Kiro CLI:** `kiro-cli 2.35.1`, model `claude-opus-5.5`. It ran the automated build fix and report generation.  
  
## 💻 Code changes (as the diff shows them)  
- 🏁 `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")`.  
- 🎮 `DownloadController.java`: removed `import org.springframework.beans.factory.annotation.Autowired;` and the `@Autowired` annotation on the single constructor `DownloadController(FileStorage storage)`.  
- 🧪 `Sha512ShallowEtagHeaderFilter.java`: `generateETagHeaderValue(byte[] bytes)` -> `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`.  
  - It now hashes `inputStream.readAllBytes()` with SHA-512 and returns `(isWeak ? "W/" : "") + "\"" + hash + "\""`.  
  - It adds the `java.io.IOException` and `java.io.InputStream` imports.  
- 📝 `logback.xml`: the unnamed `ConsoleAppender` nested inside `<root>` became a top-level `<appender name="CONSOLE">` with the same pattern, and `<root>` now uses `<appender-ref ref="CONSOLE"/>`. Logger levels are unchanged.  
- 🛠️ `pom.xml`: Java 25 compiler `<release>`, the Boot 4.0.9 parent, a `spock-bom` `2.5-groovy-5.0` import, GMavenPlus / dependency / Surefire plugin configuration, and explicit versions removed in favour of BOM management (see Application changes).  
  
## ⚠️ Risks  
- 🚫 **Runtime unverified:** the smoke-start tier was skipped, so the application was not booted. Startup, auto-configuration, embedded server and actuator behaviour under Spring Boot 4.0.9 / Java 25 have not been checked at runtime.  
- 🌱 **Non-Boot Spring artifacts:** `code-upgrade.log` reports plain (non-Boot) Spring artifacts alongside Boot. In this single-module build they are all versionless and managed by the Boot parent, and runtime validation reports "OK (static)". Still, confirm that no non-Boot module or external parent pins an older Spring version.  
- 🧪 **Weakly tested filter change:** `Sha512ShallowEtagHeaderFilter` (test sources) was rewritten to the new `InputStream`/`isWeak` signature. Nothing in the diff registers it, so the 12 tests do not exercise it, and only compilation confirms the change.  
- 🔄 **Large framework jump:** Spring Boot 1.2.3 -> 4.0.9 and Spring Framework 4.1.6 -> Boot-managed 7.x carry default behaviour changes (for example, actuator endpoint paths and exposure, and Jakarta namespace). MockMvc tests cover the download controller only.  
- 🧬 **New test stack:** Spock `2.5-groovy-5.0` and Groovy (Boot-managed) on the JUnit Platform supplied by `spring-boot-starter-test` is a recent combination. Watch for test-framework issues.  
- 📦 **Classloader-conflict check:**  
  - `jakarta.servlet-api` is **test** scope, so it is not packaged with the application. No new container-provided API is bundled at compile or runtime scope by the added direct dependencies.  
  - `spring-boot-starter-webmvc` (compile) brings the embedded web server. If the application is ever deployed as a WAR to an external servlet container, that server and the container's own servlet API could conflict.  
- 🐢 **Tooling warning:** while the OpenRewrite Java 25 recipe was validating, `code-upgrade.log` shows `sun.misc.Unsafe::objectFieldOffset` called by Caffeine 2.9.3, a terminally deprecated API. This came from the recipe tooling's run, not from the post-upgrade verification build.  
- 📚 **Older libraries kept:** `guava` 29.0-jre, `commons-io` 2.4 and `cglib-nodep` 3.1 remain at compile scope (the last two are unchanged in the diff).  
  
## ⏱️ Time savings  
- ⌛ **Transformation duration:** 16 minute(s).  
- 💰 **Estimated time saved:** 300 minute(s).  
  
## 🔜 Next steps  
- ▶️ Enable the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`) or boot the application in a test environment to confirm it starts on Java 25 / Spring Boot 4.0.9.  
- 🌐 Run end-to-end HTTP checks for `/download/{uuid}` and `/download/{uuid}/{filename}`: `If-None-Match`, `If-Modified-Since`, HEAD, 404 and redirect behaviour.  
- 🩺 Update any external monitoring to the Boot 4 actuator paths (`/actuator/health`), and review actuator endpoint exposure.  
- 🧪 Either register `Sha512ShallowEtagHeaderFilter` and add test coverage for it, or remove it if it is unused.  
- 🌱 Confirm that every Spring artifact resolves to the single Boot-managed Spring Framework version, with no non-Boot module or external parent pinning an older one.  
- 🧹 Remove `cglib-nodep` if it is unused, and update `commons-io` 2.4 and `guava` 29.0-jre to current releases.  
- 🔒 Review the plain-HTTP `http://repo.spring.io/milestone/` repository that is still declared in `pom.xml`; switch it to HTTPS or remove it.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 16 minute(s)
- ⏳ Estimated time saved: 300 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 15.71  
Includes 1.14 for job setup (startup smoke test and knowledge-base indexing).  
