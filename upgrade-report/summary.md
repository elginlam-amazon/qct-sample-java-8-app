# Java 25 Upgrade: Executive Report (Iteration 61)  
  
## 📋 Executive summary  
- 🎯 This iteration upgraded `download-server` to Java 25 and Spring Boot 4.0.9. The service serves files over `GET`/`HEAD` under `/download/{uuid}` and `/download/{uuid}/{filename}`, with ETag and `If-Modified-Since` caching and throttled output. The work used OpenRewrite (`UpgradeToJava25`, then `UpgradeSpringBoot_4_0`) followed by two Amazon Kiro CLI build-fix passes.  
- ✅ The post-upgrade verification build succeeded, and 12 automated tests ran (`DownloadControllerSpec`, 0 failures, 0 errors, 0 skipped).  
- ✅ The static runtime check found no Java-17-incompatible Spring. No build file pins a Spring Framework version, because the Spring Boot 4.0.9 parent manages it.  
- ⚠️ The smoke-start tier was skipped, so the application was never started. Its runtime behaviour on Java 25 is **unverified**.  
- 🌿 Commit `608e6c2` ("Upgrade to Java 25 on Iteration 61") on the iteration branch contains the changes: 7 files, 99 insertions, 62 deletions.  
  
## 🛠️ Application changes  
- 📦 **Build (`pom.xml`):**  
  - Java 25 via `<java.version>25</java.version>` and compiler `<release>`.  
  - Spring Boot parent 1.2.3.RELEASE → 4.0.9. Explicit Spring and Boot versions were removed so the BOM manages them.  
  - Spock 2.5 for Groovy 5, with the Groovy test compiler (gmavenplus) added.  
  - Surefire runs `*Spec` classes and attaches the byte-buddy agent.  
  - The duplicate `spring-test` declaration, the unused `spring.version` property and the plain-HTTP `repo.spring.io/milestone` repository were removed.  
- ☕ **Application code:** `DownloadController.java`, `FileSystemPointer.java`, `MainApplication.java`.  
- 📝 **Configuration:** `src/main/resources/logback.xml`.  
- 🧪 **Test code:** `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`.  
- 📄 **Notes:** `summary.md` (the upgrade notes the build-fix pass wrote in the project root).  
  
### ➕ Dependencies added  
| Dependency | Version | Scope (from diff) |  
|---|---|---|  
| `org.springframework.boot:spring-boot-starter-webmvc` | LATEST (Boot BOM) | compile |  
| `jakarta.servlet:jakarta.servlet-api` | LATEST (Boot BOM) | test |  
| `org.apache.groovy:groovy` | LATEST (Boot BOM) | test |  
| `org.codehaus.gmavenplus:gmavenplus-plugin` | 5.1.0 | build plugin |  
| `org.apache.maven.plugins:maven-dependency-plugin` | LATEST (Boot parent) | build plugin (`properties` goal) |  
| `org.apache.maven.plugins:maven-surefire-plugin` | LATEST (Boot parent) | build plugin |  
  
- 🔍 `jakarta.servlet-api` is test scope only, so the packaged application does not include it and it cannot conflict with a servlet container's copy. `spring-boot-starter-webmvc` is compile scope and brings an embedded servlet container, which is the normal setup for a Spring Boot jar.  
  
### ➖ Dependencies removed  
- 🗑️ `org.springframework.boot:spring-boot-starter-web` 1.2.3.RELEASE, replaced by `spring-boot-starter-webmvc`.  
- 🗑️ `cglib:cglib-nodep` 3.1.  
- 🗑️ `org.codehaus.groovy:groovy-all` 2.4.3, replaced by `org.apache.groovy:groovy` in test scope.  
  
### ⬆️ Dependencies upgraded  
- 🌱 `org.springframework.boot:spring-boot-starter-parent` 1.2.3.RELEASE → 4.0.9  
- 🌱 `org.springframework.boot:spring-boot-starter-actuator` 1.2.3.RELEASE → LATEST  
- 🌱 `org.springframework.boot:spring-boot-starter-test` 1.2.3.RELEASE → LATEST  
- 🍃 These Spring artifacts all went from 4.1.6.RELEASE → LATEST (BOM-managed):  
  - `org.springframework:spring-test`  
  - `org.springframework:spring-beans`  
  - `org.springframework:spring-context`  
  - `org.springframework:spring-context-support`  
  - `org.springframework:spring-webmvc`  
  - `org.springframework:spring-web`  
  - `org.springframework:spring-aop`  
  - `org.springframework:spring-core`  
  - `org.springframework:spring-expression`  
- 🧪 `org.spockframework:spock-core` 1.0-groovy-2.4 → `${spock.version}` (defined as `2.5-groovy-5.0` in the POM)  
- 🧪 `org.spockframework:spock-spring` 1.0-groovy-2.4 → `${spock.version}` (`2.5-groovy-5.0`)  
- 📚 `commons-io:commons-io` 2.4 → 2.22.0  
- 📚 `commons-codec:commons-codec` 1.10 → LATEST (BOM-managed)  
- 📚 `com.google.guava:guava` 18.0 → 33.8.0-jre  
- 🔧 `org.apache.maven.plugins:maven-compiler-plugin` 3.0 → 3.16.0  
  
## 🧰 Tools used  
- ☕ **Java SDK:** JDK 25 (`java-25`). The compiler ran with `release 25`.  
- 🔄 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0 with these recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which ran `UseMavenCompilerPluginReleaseConfiguration`, `UpdateMavenProjectPropertyJavaVersion`, `UpgradePluginVersion`, `PrimitiveWrapperClassConstructorToValueOf` and `AddMockitoJavaAgentToMavenSurefirePlugin`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which ran `UpgradeParentVersion`, `ChangeDependency` (starter-web → starter-webmvc), `JavaxMigrationToJakarta`, `NoAutowiredOnConstructor`, `RemoveDuplicateDependencies` and others.  
- 🤖 **Amazon Kiro CLI:** `kiro-cli` 2.35.1 on model `claude-opus-5.5`. It ran two build-fix passes, and each succeeded on its first attempt.  
  
## 💻 Code changes  
- ☕ `MainApplication.java`: `new Integer("1234")` → `Integer.valueOf("1234")` (OpenRewrite).  
- 🌱 `DownloadController.java`: removed `@Autowired` and its import from the single constructor (OpenRewrite `NoAutowiredOnConstructor`). Spring still injects through the constructor.  
- 🔐 `FileSystemPointer.java`: `Files.hash(target, Hashing.sha512())` → `Files.asByteSource(target).hash(Hashing.sha512())`. This replaces a deprecated Guava API and computes the same SHA-512 ETag.  
- 🧪 `Sha512ShallowEtagHeaderFilter.java` (test): the override `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. It hashes `inputStream.readAllBytes()` with SHA-512 and adds a `W/` prefix when `isWeak` is true.  
- 📝 `logback.xml`: the unnamed `ConsoleAppender` inside `<root>` became a named `CONSOLE` appender, and `<root>` now references it with `<appender-ref ref="CONSOLE"/>`. The pattern and log levels are unchanged.  
  
## ⚠️ Risks  
- 🚦 **Runtime not verified:** the runtime log says: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." Nobody has checked whether the application starts or serves requests on Java 25 with Spring Boot 4.0.9.  
- ✅ **Spring compatibility (static only):** the runtime log says: "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." This is a static check, not a runtime one.  
- 🧩 **Mixed Boot / non-Boot artifacts:** code-upgrade.log notes that plain (non-Boot) Spring artifacts were detected alongside the Boot upgrade path. The direct `spring-*` dependencies now have no explicit version, so the Boot 4.0.9 BOM aligns them. They are redundant with the starters.  
- 📈 **Large version jump:** Boot went from 1.2.3 to 4.0.9 and Spring from 4.1.6 to the Boot 4 line. Behaviour can change in ways the 12 specs don't cover, such as Actuator endpoint paths and exposure defaults, MVC path matching, and conditional-request handling.  
- 🔀 **Test coverage gap:** the specs cover `/download/{uuid}/{filename}` (ETag, `If-Modified-Since`, `HEAD`, content length and content type). No test covers the redirect endpoint `/download/{uuid}`.  
- 🪵 **Logging change:** the `logback.xml` change alters how logs reach the console. Check that log output looks correct in each environment.  
- ⚙️ **Build plugin coupling:** the Surefire `argLine` depends on the `${net.bytebuddy:byte-buddy-agent:jar}` property that `maven-dependency-plugin:properties` sets. If that execution is removed, the test JVM won't start.  
- 📦 **Version pins outside the BOM:** `commons-io` 2.22.0, `guava` 33.8.0-jre, Spock `2.5-groovy-5.0` and gmavenplus 5.1.0 are pinned in the POM, so they need manual upgrades from now on.  
  
## ⏱️ Time savings  
- 🕒 Transformation duration: 8 minute(s).  
- 💰 Estimated time saved: 180 minute(s).  
  
## 🚀 Next steps  
- ▶️ Turn on the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`) or boot the service in a dev environment. Call `GET`/`HEAD /download/{uuid}` and `/download/{uuid}/{filename}`, including requests with `If-None-Match` and `If-Modified-Since`.  
- 🩺 Check the Actuator endpoints under Spring Boot 4 and set the exposure and security you need.  
- 🧪 Add a spec for the `/download/{uuid}` redirect (301 to the filename URL).  
- 🧹 Consider removing the direct `spring-*` dependencies that the starters already bring in. Also remove the unused `Integer temp` line in `MainApplication.main`.  
- 📁 Replace the `FileSystemStorage` stub before production use. It serves `logback.xml` for any UUID, and `new File(getResource(...).getFile())` does not work inside a packaged jar.  
- 👀 Review and merge commit `608e6c2` once the runtime checks pass.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 8 minute(s)
- ⏳ Estimated time saved: 180 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 17.90  
Includes 1.18 for job setup (startup smoke test and knowledge-base indexing).  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
