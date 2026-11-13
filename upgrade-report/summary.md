# ☕ Java 17 Upgrade – Iteration 2 Executive Report  
  
## 📋 Executive summary  
- 🎯 **Scope:** the `download-server` Maven project (`com.nurkiewicz.download:download-server:0.0.1-SNAPSHOT`) was upgraded from Java 8 (`source`/`target` 1.8) to Java 17 (`<release>17</release>`).  
- 🔧 **How:** OpenRewrite's `com.amazonaws.java.migrate.UpgradeToJava17` recipe ran first. The Amazon Kiro CLI pass then moved the Spring Boot parent from 1.2.3.RELEASE to 2.7.18, pinned `spring-framework.version` to 5.3.39, removed the explicit Spring 4.1.6 module dependencies, and moved Groovy/Spock to the Groovy 3 line.  
- ✅ **Build:** the post-upgrade verification build ended with `BUILD SUCCESS` (exit code 0).  
- 🌱 **Spring:** runtime validation reports "OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3)." The project stays on the `javax` namespace (Spring Boot 2.7 line).  
- ⚠️ **Runtime behaviour is unverified:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started by the validation.  
- 🧪 **Tests:** test execution not recorded. The verification log shows a Surefire execution but no test totals.  
- 📦 **Footprint:** 4 files changed, 40 insertions, 69 deletions.  
  
## 🗂️ Application changes  
- 📄 `pom.xml`: Java 17 compiler config, Spring Boot 2.7.18 parent, Spring Framework 5.3.39 property, dependency cleanup and Groovy/Spock upgrade.  
- ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`: deprecated wrapper constructor replaced.  
- 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: ETag override moved to the Spring 5 method signature.  
- 📝 `summary.md` (repository root): upgrade notes written by the Kiro CLI pass.  
  
### ➕ Dependencies added  
- `org.codehaus.groovy:groovy` (version LATEST in report.json; the diff declares no `<version>`, so the Spring Boot parent manages it). **Scope: compile** (no `<scope>` element in the diff).  
  
### ➖ Dependencies removed  
- `cglib:cglib-nodep` 3.1  
- `org.springframework:spring-beans` 4.1.6.RELEASE  
- `org.springframework:spring-context` 4.1.6.RELEASE  
- `org.springframework:spring-context-support` 4.1.6.RELEASE  
- `org.springframework:spring-webmvc` 4.1.6.RELEASE  
- `org.springframework:spring-web` 4.1.6.RELEASE  
- `org.springframework:spring-aop` 4.1.6.RELEASE  
- `org.springframework:spring-core` 4.1.6.RELEASE  
- `org.springframework:spring-expression` 4.1.6.RELEASE  
- `org.codehaus.groovy:groovy-all` 2.4.3  
  
### ⬆️ Dependencies upgraded  
- `org.springframework:spring-test` 4.1.6.RELEASE → LATEST (explicit version removed; managed by the Boot parent)  
- `org.spockframework:spock-spring` 1.0-groovy-2.4 → 2.3-groovy-3.0  
- `commons-codec:commons-codec` 1.10 → 1.17.2  
- `org.springframework.boot:spring-boot-starter-web` 1.2.3.RELEASE → LATEST (managed by the Boot parent)  
- `org.springframework.boot:spring-boot-starter-actuator` 1.2.3.RELEASE → LATEST (managed by the Boot parent)  
- `org.springframework.boot:spring-boot-starter-test` 1.2.3.RELEASE → LATEST (managed by the Boot parent)  
- `org.spockframework:spock-core` 1.0-groovy-2.4 → 2.3-groovy-3.0  
- `org.springframework.boot:spring-boot-starter-parent` 1.2.3.RELEASE → 2.7.18  
- `org.apache.maven.plugins:maven-compiler-plugin` 3.0 → 3.16.0  
  
## 🛠️ Tools used  
- ☕ **Java SDK:** compilation targets Java 17 (`javac [debug parameters release 17]` in the verification log).  
- 🔁 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0 with recipe `com.amazonaws.java.migrate.UpgradeToJava17`. Applied sub-recipes include `UseMavenCompilerPluginReleaseConfiguration`, `UpdateMavenProjectPropertyJavaVersion`, `UpgradePluginVersion`, `UpgradeDependencyVersion` (commons-codec 1.17.x) and `PrimitiveWrapperClassConstructorToValueOf`.  
- ⏭️ The Spring Boot upgrade leg was skipped by the pipeline ("not requested (springBootTargetVersion=none)"); the Boot parent change came from the Kiro CLI pass.  
- 🤖 **Amazon Kiro CLI:** `kiro-cli 2.35.1`, model `claude-opus-5.5`, for the build-fix pass and report generation.  
  
## 💻 Code changes  
- ☕ `MainApplication.java`: `new Integer("1234")` → `Integer.valueOf("1234")` (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`).  
- 🧪 `Sha512ShallowEtagHeaderFilter.java`: `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. The SHA-512 hash is now computed from `ByteStreams.toByteArray(inputStream)`, and the value is prefixed with `W/` when `isWeak` is true.  
- 📄 `pom.xml`:  
  - ➕ `<java.version>17</java.version>`; compiler `<source>1.8</source>`/`<target>1.8</target>` → `<release>${java.version}</release>`.  
  - 🔄 `<spring.version>4.1.1.RELEASE</spring.version>` → `<spring-framework.version>5.3.39</spring-framework.version>`.  
  - 🧹 Explicit `<version>` removed from the Boot starters and `spring-test`; the duplicate `spring-test` declaration removed.  
  - 🔄 `groovy-all` 2.4.3 → `groovy` with no explicit version.  
  
## ⚠️ Risks  
- 🚦 **Runtime unverified:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started, so startup and request handling on Java 17 are not proven by this iteration.  
- 🧪 **Test coverage unknown:** test execution not recorded. The build success alone does not show that any behaviour was exercised.  
- 📦 **Groovy at compile scope:** `org.codehaus.groovy:groovy` is packaged with the application. It is not an API that the embedded runtime provides, so a classloader conflict with a server-provided API is not expected. Its presence on the classpath does activate Spring Boot's Groovy auto-configuration, and it adds weight to the artifact.  
- 🔀 **Framework jump:** Spring Boot 1.2.3 → 2.7.18 and Spring 4.1 → 5.3 span several major releases. Changes in configuration properties, actuator endpoints and MVC defaults are possible and not covered by the static check.  
- 🏷️ **ETag behaviour change:** `Sha512ShallowEtagHeaderFilter` now emits a `W/` prefix for weak ETags. It is test code, but any test asserting on ETag values may need review.  
- 🧩 **Removed `spring-context-support`:** no longer declared and not pulled by the Boot starters. Code that later needs its classes (caching, mail, scheduling support) will need it re-added.  
- 🔢 **Managed versions:** several entries show "LATEST" in report.json because their versions now come from the Boot 2.7.18 parent rather than the POM.  
- 🐛 **Groovy tests:** the verification log shows only 3 test source files compiled by `javac` and no Groovy compilation step, so Spock specs under `src/test/groovy` are likely not compiled or run.  
  
## ⏱️ Time savings  
- 🕒 **Transformation duration:** 6 minute(s).  
- 💰 **Estimated time saved:** 240 minute(s).  
  
## 🚀 Next steps  
- 🔍 Enable the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`) and re-run validation to confirm the application boots on Java 17.  
- 🧪 Add a Groovy compiler plugin (for example `gmavenplus-plugin`) so the Spock 2 specs compile and run, then confirm Surefire records test totals.  
- 🌐 Smoke-test the `/download/{uuid}` and `/download/{uuid}/{filename}` endpoints (GET and HEAD, ETag and `If-Modified-Since` handling) in a dev environment.  
- 📦 Decide whether Groovy is needed at compile scope; if it is only used by tests, change it to `test` scope.  
- 🔄 Review remaining older dependencies left unchanged (for example `guava` 18.0) for Java 17 support.  
- 🧭 Keep Spring Boot 3.x / Jakarta migration as a separate, planned step; this iteration intentionally stays on `javax`.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 6 minute(s)
- ⏳ Estimated time saved: 240 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring 5.3).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 13.28  
Includes 1.14 for job setup (startup smoke test and knowledge-base indexing).  
