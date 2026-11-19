# Java 17 Upgrade — Iteration 2 Executive Report  
  
## Executive Summary  
  
- 🎯 This iteration upgraded the `download-server` Spring Boot application from **Java 8 / Spring Boot 1.2.3.RELEASE** to **Java 17 / Spring Boot 2.7.18**, the final javax-namespace Spring Boot 2 release  
- 🤖 OpenRewrite (`com.amazonaws.java.migrate.UpgradeToJava17`) ran first and updated the compiler configuration and one source file; it left the Spring Boot parent, all explicit Spring Framework artifacts, and the Groovy/Spock test stack untouched  
- 🔧 The Kiro CLI build-fix pass completed the upgrade: it rewrote the POM to adopt the Spring Boot 2.7.18 BOM, replaced the incompatible Groovy 2 / Spock 1 test stack with Groovy 3 / Spock 2, added the missing Groovy compilation plugin, and fixed two Java source files with API-level incompatibilities introduced by the Spring 5 and classloader context changes  
- ✅ Post-upgrade verification: **BUILD SUCCESS** — 12 tests run, 0 failures, 0 errors, 0 skipped  
- ⚠️ Application runtime behaviour is **unverified**: the smoke-start tier was skipped (opt-in); actual startup on Java 17 must be confirmed before promoting to a shared environment  
- 🛡️ Static Spring compatibility check passed: no Java-17-incompatible Spring version detected; Spring Framework is managed by the Spring Boot 2.7.18 BOM (effectively 5.3.x) rather than pinned directly in the POM  
  
---  
  
## Application Changes  
  
- 📄 **4 files changed** — 62 insertions, 108 deletions  
- 📋 Files on the branch: `pom.xml`, `src/main/java/com/nurkiewicz/download/MainApplication.java`, `src/test/java/com/nurkiewicz/download/FileExamples.java`, `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  
### Dependencies added (2)  
  
| Artifact | Version | Type | Scope |  
|---|---|---|---|  
| `org.codehaus.gmavenplus:gmavenplus-plugin` | 3.0.2 | Build plugin | Test-compile phase |  
| `org.apache.maven.plugins:maven-surefire-plugin` | 3.5.2 | Build plugin | Test-execute phase |  
  
- ℹ️ Both are Maven build plugins only; neither is packaged in the application JAR and neither introduces a classloader-conflict risk  
  
### Dependencies removed (11)  
  
| Artifact | Version removed |  
|---|---|  
| `org.springframework:spring-beans` | 4.1.6.RELEASE |  
| `org.springframework:spring-context` | 4.1.6.RELEASE |  
| `org.springframework:spring-context-support` | 4.1.6.RELEASE |  
| `org.springframework:spring-core` | 4.1.6.RELEASE |  
| `org.springframework:spring-web` | 4.1.6.RELEASE |  
| `org.springframework:spring-webmvc` | 4.1.6.RELEASE |  
| `org.springframework:spring-aop` | 4.1.6.RELEASE |  
| `org.springframework:spring-expression` | 4.1.6.RELEASE |  
| `org.springframework:spring-test` | 4.1.6.RELEASE (duplicate declaration) |  
| `cglib:cglib-nodep` | 3.1 (bundled in spring-core since Spring 5) |  
| `org.codehaus.groovy:groovy-all` | 2.4.3 (incompatible with Java 17; was compile-scoped) |  
  
- ✅ All Spring Framework artifacts are now version-managed by the Spring Boot 2.7.18 parent BOM and no longer need explicit declarations  
  
### Dependencies upgraded (9)  
  
| Artifact | From | To |  
|---|---|---|  
| `org.springframework.boot:spring-boot-starter-parent` | 1.2.3.RELEASE | 2.7.18 |  
| `org.springframework.boot:spring-boot-starter-web` | 1.2.3.RELEASE | managed by parent BOM |  
| `org.springframework.boot:spring-boot-starter-actuator` | 1.2.3.RELEASE | managed by parent BOM |  
| `org.springframework.boot:spring-boot-starter-test` | 1.2.3.RELEASE | managed by parent BOM |  
| `org.spockframework:spock-core` | 1.0-groovy-2.4 | 2.3-groovy-3.0 |  
| `org.spockframework:spock-spring` | 1.0-groovy-2.4 | 2.3-groovy-3.0 |  
| `org.apache.maven.plugins:maven-compiler-plugin` | 3.0 | 3.13.0 |  
| `commons-io:commons-io` | 2.4 | 2.17.0 |  
| `commons-codec:commons-codec` | 1.10 | 1.17.2 |  
  
---  
  
## Tools Used  
  
- ☕ **Java SDK**: Amazon Corretto 17 (`javac [debug parameters release 17]` confirmed in post-upgrade-verification.log)  
- 🔄 **OpenRewrite**: `rewrite:6.42.0` running recipe `com.amazonaws.java.migrate.UpgradeToJava17`  
- 🤖 **Amazon Kiro CLI**: version `kiro-cli 2.35.1`, model **Claude Sonnet 4.6**  
  
---  
  
## Code Changes  
  
### `pom.xml` (OpenRewrite + Kiro CLI)  
  
- ➕ Added `<java.version>17</java.version>` property; removed `<spring.version>4.1.1.RELEASE</spring.version>`  
- 🔧 `maven-compiler-plugin`: `3.0` → `3.13.0`; compiler configuration changed from `<source>1.8</source><target>1.8</target>` to `<release>${java.version}</release>` (OpenRewrite `UseMavenCompilerPluginReleaseConfiguration`)  
- 🚀 Spring Boot parent: `1.2.3.RELEASE` → `2.7.18`  
- 🗑️ Removed HTTP milestone repository (`http://repo.spring.io/milestone/`) — not needed for GA releases and blocked by Maven 3.8+ HTTP policy  
- ➕ Added `gmavenplus-plugin:3.0.2` with goals `addTestSources`, `generateTestStubs`, `compileTests`, `removeTestStubs` — the Groovy Spock tests were silently uncompiled and unexecuted before this addition  
- ➕ Added `maven-surefire-plugin:3.5.2` with `Spec*` / `Test*` / `Tests*` includes — Spock 2 uses JUnit Platform, which Surefire 3.x supports via auto-detected `JUnitPlatformProvider`  
- 🔀 `spock-core` and `spock-spring`: `1.0-groovy-2.4` → `2.3-groovy-3.0`; Spock 2 transitively brings Groovy 3.0.19 (test scope), replacing the removed compile-scoped `groovy-all:2.4.3`  
- 📦 `commons-io`: `2.4` → `2.17.0` (not managed by Spring Boot BOM; explicit version required)  
- 📦 `commons-codec`: `1.10` → `1.17.2`  
  
### `src/main/java/com/nurkiewicz/download/MainApplication.java` (OpenRewrite)  
  
- 🔧 `new Integer("1234")` → `Integer.valueOf("1234")` (OpenRewrite recipe `PrimitiveWrapperClassConstructorToValueOf` — constructor deprecated since Java 9, removed in Java 17 preview)  
  
### `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` (Kiro CLI)  
  
- 🔧 Overridden method signature updated from the Spring 4.x API `generateETagHeaderValue(byte[] bytes)` to the Spring 5.x API `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`; body updated to call `inputStream.readAllBytes()` (Java 9+) and prefix the hash with `W/"` for weak ETags  
  
### `src/test/java/com/nurkiewicz/download/FileExamples.java` (Kiro CLI)  
  
- 🐛 `TXT_FILE_UUID = UUID.randomUUID()` → `UUID.fromString("11111111-1111-1111-1111-111111111111")`  
- 🐛 `NOT_FOUND_UUID = UUID.randomUUID()` → `UUID.fromString("22222222-2222-2222-2222-222222222222")`  
- 📝 Spring Boot 2.7.x loads its application context through a separate classloader path from the Spock/Groovy test runner; `UUID.randomUUID()` was being called once per classloader, producing two different values so `FileStorageStub`'s equality check always failed and every test returned 404. Fixed by using deterministic UUIDs whose bit-level `equals()` returns `true` regardless of which classloader evaluated `UUID.fromString()`  
  
---  
  
## Risks  
  
- ⚠️ **Runtime startup unverified** — The smoke-start tier was skipped (`ENABLE_RUNTIME_VALIDATION` not set). The application JAR was produced and all MockMvc tests passed, but an actual JVM boot of the application on Java 17 has not been confirmed. Promote to a shared environment only after a manual or CI startup check  
- ⚠️ **Spring Boot 2.7.18 is end-of-life** — Spring Boot 2.7.x reached end of OSS support. The next recommended step is a Spring Boot 3.x migration (requires `javax.*` → `jakarta.*` namespace migration across the codebase). This iteration deliberately stays on `javax.*` to minimise change scope  
- ⚠️ **Maven 4 compatibility** — The post-upgrade-verification.log records: `"Plugin is modifying testCompileSourceRoots through iterator.remove(), which will not work in Maven 4.0.0"` from `gmavenplus-plugin:3.0.2`. No impact under Maven 3.x; will break compilation under Maven 4 until GMavenPlus is upgraded  
- ⚠️ **Guava 18.0 remains old** — `com.google.guava:guava:18.0` (released 2014) is unchanged on the compile classpath. Several methods used (`Files.hash()`, `Hashing.sha512()`) are deprecated in later Guava versions. No compilation error at Java 17, but upgrading to Guava 32+ is recommended to remove deprecation exposure  
- ℹ️ **Groovy removed from compile classpath** — `groovy-all:2.4.3` was previously at compile scope, meaning Groovy classes were bundled in the application JAR. Post-upgrade, Groovy 3.0.19 is test-scoped only. If any production code path relied on Groovy being available at runtime, it would now fail — inspection of main sources shows no such dependency, so this is an improvement not a regression  
  
---  
  
## Time Savings  
  
- ⏱️ **Transformation duration**: 12 minutes  
- 💡 **Estimated time saved vs. manual upgrade**: 180 minutes (3 hours)  
- 📊 The main time-saving items were: identifying the multi-layered incompatibility (Spring Boot 1.x + Groovy 2 + Spock 1 all simultaneously incompatible with Java 17); configuring GMavenPlus + Surefire 3 for the first time in this project; diagnosing and fixing the classloader isolation UUID bug (the most difficult item — all tests returned 404 with no obvious error)  
  
---  
  
## Next Steps  
  
### Validation  
  
- 🔍 **Enable runtime validation**: run with `ENABLE_RUNTIME_VALIDATION=true` to verify the application actually starts on Java 17 with Spring Boot 2.7.18  
- 🔍 **Integration / smoke test**: exercise the `/download/{uuid}` and `/download/{uuid}/{filename}` endpoints against a live instance to confirm ETag, `If-Modified-Since`, throttling, and content-type headers all work end-to-end  
- 🔍 **Review actuator exposure**: Spring Boot 2.7.x exposes only the `health` endpoint at `/actuator` by default — confirm this matches the operational requirements  
  
### Recommended improvements  
  
- ⬆️ **Migrate to Spring Boot 3.x** (next major iteration) — requires `javax.*` → `jakarta.*` namespace migration; plan this as a dedicated iteration  
- ⬆️ **Upgrade Guava** from `18.0` to `32.x` or later to eliminate deprecated-API exposure (`Files.hash()`, `Hashing.sha512()`)  
- ⬆️ **Upgrade GMavenPlus** to a version that uses `addTestCompileSourceRoot()` rather than iterator removal, to remove the Maven 4 compatibility warning  
- 🔀 **Replace `@ContextConfiguration` + `@WebAppConfiguration` with `@SpringBootTest`** in `DownloadControllerSpec` — the current combination relies on a Spring Boot 1.x context-loading behaviour that no longer applies natively in Spring Boot 2.x and required the UUID determinism workaround; `@SpringBootTest(webEnvironment = MOCK)` is the idiomatic Spring Boot 2/3 equivalent  
- 🔀 **Submit pull request** — `iteration_details.pullrequest_submission` is `not_started`; create a PR on branch `egress-mwndydaz-java-upgrade-itr-2` for peer review before merging

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 12 minute(s)
- ⏳ Estimated time saved: 180 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 18.90  
Includes 0.48 for job setup (startup smoke test and knowledge-base indexing).  
