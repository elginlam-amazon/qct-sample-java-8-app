# Java 17 Upgrade — Iteration 2 Executive Report  
  
## Executive Summary  
  
This iteration upgraded `download-server` from Java 8 (source/target) to Java 17, moving the Spring Boot parent from `1.2.3.RELEASE` to `2.7.18` in the process. OpenRewrite applied the `UpgradeToJava17` recipe, which updated the compiler configuration and one deprecated API call. Because `springBootTargetVersion=none` was set, OpenRewrite explicitly skipped the Spring Boot upgrade — Spring Framework remained at 4.1.x after the automated pass, which bundles an ASM/CGLIB that cannot verify Java 17 class files and would have caused `VerifyError` on every CGLIB proxy creation at runtime. The Amazon Kiro CLI completed the upgrade: it promoted the Spring Boot parent to 2.7.18 (which provides Spring Framework 5.3.39 via its BOM), repaired a broken `ShallowEtagHeaderFilter` override whose method signature changed in Spring 5.1, corrected a silent Groovy/Spock test compilation gap that had left 0 tests running, and fixed a classloader-boundary UUID mismatch that caused 11 of 12 integration tests to return 404. The post-upgrade build passed with **12 tests, 0 failures**. The runtime static-compatibility check passed. Application boot was not verified (smoke-start tier was skipped).  
  
---  
  
## Application Changes  
  
### `pom.xml`  
- 🔼 **Spring Boot parent** `1.2.3.RELEASE` → `2.7.18`; all Spring Framework versions are now managed exclusively by the Boot BOM (5.3.39) — no explicit Spring pins remain  
- 🗑️ **`spring.version` property** (`4.1.1.RELEASE`) removed  
- 🗑️ **Standalone Spring Framework dependency pins** removed (10 entries: `spring-beans`, `spring-context`, `spring-context-support`, `spring-webmvc`, `spring-web`, `spring-aop`, `spring-core`, `spring-expression`, and a duplicate `spring-test`, all at `4.1.6.RELEASE`)  
- 🗑️ **`cglib:cglib-nodep:3.1`** removed — Spring 5.3 bundles a repackaged CGLIB internally; the standalone artifact conflicts with it  
- 🗑️ **`org.codehaus.groovy:groovy-all:2.4.3`** removed — Groovy 2.4 is incompatible with Java 17  
- 🔼 **`maven-compiler-plugin`** `3.0` → `3.16.0`; `<source>1.8>/<target>1.8>` replaced with `<release>17>`  
- 🔼 **`commons-io:commons-io`** `2.4` → `2.15.1`  
- 🔼 **`commons-codec:commons-codec`** `1.10` → `1.17.2`  
- 🔼 **`org.spockframework:spock-core`** `1.0-groovy-2.4` → `2.3-groovy-4.0`  
- 🔼 **`org.spockframework:spock-spring`** `1.0-groovy-2.4` → `2.3-groovy-4.0`  
- ➕ **`org.apache.groovy:groovy:4.0.21`** (test scope) — Groovy 4 runtime needed for Spock 2.x  
- ➕ **`org.codehaus.gmavenplus:gmavenplus-plugin:3.0.2`** (build plugin) — compiles `.groovy` test sources; without it the entire Spock suite was silently skipped  
- ➕ **`org.apache.maven.plugins:maven-surefire-plugin:3.2.5`** (build plugin) — Surefire 3.x provides JUnit Platform support required by Spock 2.x; configured with `**/*Spec.java` include pattern  
- 🗑️ Milestone repository `http://repo.spring.io/milestone/` removed — Spring Boot 2.7.18 GA is available from Maven Central  
  
### `src/main/java/com/nurkiewicz/download/MainApplication.java`  
- ✏️ OpenRewrite (`PrimitiveWrapperClassConstructorToValueOf`) replaced the deprecated `new Integer("1234")` constructor call with `Integer.valueOf("1234")`  
  
### `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
- ✏️ The overridden method signature changed in Spring 5.1: `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`; `ByteStreams.toByteArray(inputStream)` added to read the stream before hashing  
  
### `src/test/java/com/nurkiewicz/download/FileExamples.java`  
- ✏️ `TXT_FILE_UUID` changed from `UUID.randomUUID()` to `UUID.fromString("aaaaaaaa-0000-0000-0000-aaaaaaaaaaaa")`  
- ✏️ `NOT_FOUND_UUID` changed from `UUID.randomUUID()` to `UUID.fromString("bbbbbbbb-0000-0000-0000-bbbbbbbbbbbb")`  
- 🐛 **Root cause:** Spring Boot 2.7's `SpringBootContextLoader` initialises the application context through a separate classloader instance from the one Surefire/Spock uses to load the Groovy spec. `UUID.randomUUID()` produced a different value in each classloader, so `FileStorageStub` compared against a UUID that never matched the URL the test constructed — causing 11 of 12 tests to return 404. Using deterministic fixed values eliminates the cross-classloader race  
  
### `summary.md`  
- ➕ New file added by the Kiro CLI containing migration notes  
  
---  
  
## Tools Used  
  
- ☕ **Java SDK:** JDK 17 (javac `[debug parameters release 17]`, as shown in `post-upgrade-verification.log`)  
- 🔧 **OpenRewrite:** `rewrite-maven-plugin 6.42.0`, recipe `com.amazonaws.java.migrate.UpgradeToJava17`  
- 🤖 **Amazon Kiro CLI:** `kiro-cli 2.35.1`, model **Claude Sonnet 4.6** — performed the Spring Boot upgrade, all dependency reconciliation, and all code fixes after the OpenRewrite pass  
  
---  
  
## Code Changes  
  
> All changes are as shown by `git diff 73556bed1b36e07af017a930ee46d5f994aa51cc HEAD`.  
  
| File | Change | Agent |  
|---|---|---|  
| `MainApplication.java` | `new Integer("1234")` → `Integer.valueOf("1234")` | OpenRewrite `PrimitiveWrapperClassConstructorToValueOf` |  
| `Sha512ShallowEtagHeaderFilter.java` | Override signature: `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`; body: `Hashing.sha512().hashBytes(bytes)` → `Hashing.sha512().hashBytes(ByteStreams.toByteArray(inputStream))` | Kiro CLI |  
| `FileExamples.java` | `UUID.randomUUID()` × 2 → `UUID.fromString("aaaaaaaa-…")` / `UUID.fromString("bbbbbbbb-…")` | Kiro CLI |  
| `pom.xml` | Spring Boot parent, compiler plugin, Spock/Groovy stack, build plugins — see Application Changes above | OpenRewrite (compiler plugin, `java.version`, `commons-codec`) + Kiro CLI (all other changes) |  
  
---  
  
## Risks  
  
### ⚠️ Application boot not verified  
- The smoke-start tier was skipped (`Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)`); whether the application starts successfully under Java 17 with Spring Boot 2.7.18 has not been confirmed in this iteration  
  
### ⚠️ Spring Boot major version jump (1.2 → 2.7)  
- This is a large jump (five minor versions); only the areas exercised by the 12 MockMvc integration tests have been validated — any endpoint, filter, actuator, or auto-configuration behaviour not covered by the test suite is unverified  
- Spring Boot 2.7 changed the default URL-matching strategy to `PathPatternParser`; paths with unusual characters or suffix patterns may behave differently  
  
### ⚠️ Guava 18.0 remains on the classpath  
- `com.google.guava:guava:18.0` is unchanged (predates the Java 9 module system); it compiles and its APIs used in production code (`Hashing`, `Files.hash`, `RateLimiter`) are not removed, but `Hashing.sha512()` and `com.google.common.io.Files.hash()` are deprecated in Guava 25+. No classloader-conflict risk exists since Guava is not provided by Java 17 or the embedded Tomcat  
  
### ℹ️ Added dependencies — classloader-conflict assessment  
- **`org.apache.groovy:groovy:4.0.21`** — test scope only; not on the production classpath; no conflict risk  
- **`org.codehaus.gmavenplus:gmavenplus-plugin:3.0.2`** — build-time plugin only; not on the runtime classpath; no conflict risk  
- **`org.apache.maven.plugins:maven-surefire-plugin:3.2.5`** — build-time plugin only; not on the runtime classpath; no conflict risk  
  
### ℹ️ javax namespace preserved  
- The upgrade intentionally stays on the **javax** namespace (Spring Boot 2.7 / Spring Framework 5.3); no `jakarta.*` migration was performed or is needed at this target  
  
### ℹ️ `java.util.Date` in `DownloadController`  
- `@RequestHeader Optional<Date>` uses the deprecated `java.util.Date`; this compiles and tests pass under Spring 5.3 but is a known technical debt item  
  
---  
  
## Time Savings  
  
- ⏱️ **Transformation duration:** 14 minutes  
- 💰 **Estimated time saved vs. manual upgrade:** 360 minutes (6 hours)  
  - Research: Spring Boot 1.x → 2.7 migration compatibility matrix, ASM/CGLIB Java 17 constraint, Spock 2.x / Groovy 4 compatibility, Spring 5.1 API break in `ShallowEtagHeaderFilter`  
  - Diagnosis: classloader-boundary UUID mismatch causing silent test failures  
  - Execution: dependency reconciliation, build-fix iterations, test validation  
  
---  
  
## Next Steps  
  
### 🔴 Must do before production deployment  
- 🔴 **Verify application boot:** run with `ENABLE_RUNTIME_VALIDATION=true` or start the application manually and confirm the actuator health endpoint responds; the smoke-start tier was skipped and application startup under Java 17 + Spring Boot 2.7.18 is not confirmed  
- 🔴 **Expand test coverage:** only 12 MockMvc integration tests exist; add tests for any endpoints, filters, or actuator behaviours not currently exercised before promoting to production  
  
### 🟡 Recommended follow-up  
- 🟡 **Upgrade Guava** from 18.0 to a current release (32.x or later) to eliminate deprecated-API warnings in `FileSystemPointer` and `Sha512ShallowEtagHeaderFilter`; update `Optional.transform()` / `Optional.or()` usages in `ExistingFile.java` to `java.util.Optional`  
- 🟡 **Replace `java.util.Date`** in `DownloadController`'s `@RequestHeader Optional<Date> ifModifiedSinceOpt` with `Instant` or `ZonedDateTime`  
- 🟡 **Migrate Spock test setup** from `@WebAppConfiguration` + `@ContextConfiguration(classes = [MainApplication])` to `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`, which is the idiomatic Spring Boot 2.x test pattern and avoids classloader-separation issues at their source  
  
### 🔵 Future upgrade path  
- 🔵 **Java 21 / Spring Boot 3:** Spring Framework 5.3 (javax) cannot proxy Java 21 bytecode; any future move to Java 21 or 25 requires a full Spring Boot 3.x + `jakarta.*` namespace migration — the current state (Boot 2.7 / Spring 5.3 / javax) is the correct stable waypoint before that next hop

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 14 minute(s)
- ⏳ Estimated time saved: 360 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 22.93  
Includes 0.48 for job setup (startup smoke test and knowledge-base indexing).  
