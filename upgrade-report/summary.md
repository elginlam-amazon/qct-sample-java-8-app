# Java 17 Upgrade — Iteration 2 Executive Report  
  
## Executive Summary  
  
🎯 This iteration successfully upgraded the `download-server` Spring Boot application from **Java 8 / Spring Boot 1.2.3.RELEASE** to **Java 17 / Spring Boot 2.7.18**, staying on the `javax` namespace throughout. OpenRewrite applied the `com.amazonaws.java.migrate.UpgradeToJava17` recipe to set the compiler target and modernise deprecated JDK APIs; the Kiro CLI then resolved the full cascade of incompatibilities that OpenRewrite could not reach — Spring Boot, Groovy/Spock, and the test infrastructure — until a clean build with 12/12 passing tests was achieved. The post-upgrade verification build **succeeded** and the static Spring compatibility check is **clean**. The application's runtime behaviour is **unverified** because the smoke-start tier was not enabled; a manual boot check is recommended before promoting this branch.  
  
---  
  
## Application Changes  
  
### 📦 Build file (`pom.xml`)  
- ✅ Spring Boot parent: `1.2.3.RELEASE` → `2.7.18` (last javax/Spring 5.3 line; Java 17 compatible)  
- ✅ Removed stale `<spring.version>4.1.1.RELEASE</spring.version>` property; version management delegated to the Boot 2.7.18 BOM  
- ✅ `maven-compiler-plugin`: `3.0` → `3.16.0`; `<source>/<target>` replaced with `<release>${java.version}</release>` (OpenRewrite)  
- ✅ Milestone repository URL hardened from `http://` to `https://`  
- ✅ 11 hand-pinned `org.springframework:spring-*` declarations (spring-beans, spring-context, spring-context-support, spring-webmvc, spring-web, spring-aop, spring-core, spring-expression) and a duplicate `spring-test` entry removed; all now BOM-managed  
- ✅ `cglib:cglib-nodep:3.1` removed — Spring 5.3 inlines a Java-17-compatible CGLIB  
- ✅ `org.codehaus.groovy:groovy-all:2.4.3` removed; replaced with Apache Groovy 4.x  
- ✅ `gmavenplus-plugin:3.0.2` added — without it `src/test/groovy` was silently ignored by the Java compiler  
- ✅ `maven-surefire-plugin:3.2.5` added with `**/*Spec.java` include — Surefire 2.17 has no JUnit Platform support; Surefire 3.x also requires the `*Spec` pattern to discover `DownloadControllerSpec`  
- ✅ Spock: `1.0-groovy-2.4` → `2.3-groovy-4.0` (both `spock-core` and `spock-spring`)  
- ✅ `org.apache.groovy:groovy:4.0.21` added (test scope)  
- ✅ `org.junit.platform:junit-platform-launcher` added (test scope) — required by Surefire 3.x for JUnit Platform engine discovery; not pulled transitively by `spring-boot-starter-test` in Boot 2.7  
- ✅ `com.google.guava:guava`: `18.0` → `32.1.3-jre`  
- ✅ `commons-io:commons-io`: `2.4` → `2.11.0`  
- ✅ `commons-codec:commons-codec`: `1.10` → `1.17.2` (OpenRewrite)  
  
### 🧪 Test files  
- ✅ `DownloadControllerTest.groovy`: `@WebAppConfiguration` + `@ContextConfiguration(classes=[MainApplication])` replaced with `@SpringBootTest(webEnvironment=MOCK)` + `@AutoConfigureMockMvc`; setter injection of `WebApplicationContext` replaced with direct `@Autowired MockMvc mockMvc` field  
- ✅ `FileExamples.java`: `UUID.randomUUID()` → `UUID.fromString("00000000-0000-0000-0000-000000000001")` and `UUID.fromString("00000000-0000-0000-0000-000000000002")` for `TXT_FILE_UUID` and `NOT_FOUND_UUID` respectively  
- ✅ `Sha512ShallowEtagHeaderFilter.java`: `generateETagHeaderValue(byte[] bytes)` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException` to match the Spring 5.1+ API; `inputStream.readAllBytes()` used to obtain the byte payload  
  
### ☕ Production source (`MainApplication.java`)  
- ✅ `new Integer("1234")` → `Integer.valueOf("1234")` (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`)  
  
---  
  
## Tools Used  
  
- 🔧 **Java SDK**: Java 17.0.21.1 (Amazon Corretto, confirmed in post-upgrade-verification.log)  
- 🤖 **OpenRewrite**: `rewrite:6.42.0` running recipe `com.amazonaws.java.migrate.UpgradeToJava17`  
- 🤖 **Amazon Kiro CLI**: `kiro-cli 2.35.1`, model **Claude Sonnet 4.6** (`claude-sonnet-4.6`), 1 attempt, no additional prompt  
  
---  
  
## Code Changes  
  
> All changes are as shown by `git diff 73556bed1b36e07af017a930ee46d5f994aa51cc HEAD`.  
  
### `src/main/java/com/nurkiewicz/download/MainApplication.java`  
- 🔄 `new Integer("1234")` → `Integer.valueOf("1234")` — OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`; the `Integer(String)` constructor was deprecated since Java 9 and removed in Java 17  
  
### `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
- 🔄 Override signature: `protected String generateETagHeaderValue(byte[] bytes)` → `protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`  
- ➕ Added `java.io.IOException` and `java.io.InputStream` imports  
- 🔄 Body: `Hashing.sha512().hashBytes(bytes)` now operates on `inputStream.readAllBytes()` to match the new contract  
  
### `src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy`  
- 🔄 Class annotations: `@WebAppConfiguration` + `@ContextConfiguration(classes=[MainApplication])` removed; `@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)` + `@AutoConfigureMockMvc` added  
- 🔄 `private MockMvc mockMvc` (manual setup) → `@Autowired MockMvc mockMvc` (auto-configured field injection)  
- ➖ Removed `@Autowired public void setWebApplicationContext(WebApplicationContext wac)` setter and the `MockMvcBuilders.webAppContextSetup(wac).build()` call  
- ➖ Removed imports: `ContextConfiguration`, `WebAppConfiguration`, `MockMvcBuilders`, `WebApplicationContext`  
- ➕ Added imports: `SpringBootTest`, `AutoConfigureMockMvc`  
  
### `src/test/java/com/nurkiewicz/download/FileExamples.java`  
- 🔄 `TXT_FILE_UUID`: `UUID.randomUUID()` → `UUID.fromString("00000000-0000-0000-0000-000000000001")`  
- 🔄 `NOT_FOUND_UUID`: `UUID.randomUUID()` → `UUID.fromString("00000000-0000-0000-0000-000000000002")`  
  
---  
  
## Dependencies  
  
### ➕ Added  
| Artifact | Version | Scope | Notes |  
|---|---|---|---|  
| `org.apache.groovy:groovy` | `4.0.21` | test | Groovy 4.x runtime for Spock 2.x; `org.codehaus.groovy` groupId renamed to `org.apache.groovy` in 4.x |  
| `org.junit.platform:junit-platform-launcher` | BOM-managed | test | Required by Surefire 3.x to load JUnit Platform engines; not pulled transitively by Boot 2.7 starter-test |  
| `org.codehaus.gmavenplus:gmavenplus-plugin` | `3.0.2` | build (plugin) | Compiles `src/test/groovy`; no runtime scope, no classloader-conflict risk |  
| `org.apache.maven.plugins:maven-surefire-plugin` | `3.2.5` | build (plugin) | JUnit Platform provider support; no runtime scope, no classloader-conflict risk |  
  
> ⚠️ No added compile- or runtime-scoped dependency duplicates an API provided by an application server or the JVM itself; no classloader-conflict risk identified.  
  
### ➖ Removed  
| Artifact | Version |  
|---|---|  
| `org.springframework:spring-test` | `4.1.6.RELEASE` |  
| `cglib:cglib-nodep` | `3.1` |  
| `org.springframework:spring-beans` | `4.1.6.RELEASE` |  
| `org.springframework:spring-context` | `4.1.6.RELEASE` |  
| `org.springframework:spring-context-support` | `4.1.6.RELEASE` |  
| `org.springframework:spring-webmvc` | `4.1.6.RELEASE` |  
| `org.springframework:spring-web` | `4.1.6.RELEASE` |  
| `org.springframework:spring-aop` | `4.1.6.RELEASE` |  
| `org.springframework:spring-core` | `4.1.6.RELEASE` |  
| `org.springframework:spring-expression` | `4.1.6.RELEASE` |  
| `org.codehaus.groovy:groovy-all` | `2.4.3` |  
  
### 🔼 Upgraded  
| Artifact | Old Version | New Version |  
|---|---|---|  
| `org.springframework.boot:spring-boot-starter-parent` | `1.2.3.RELEASE` | `2.7.18` |  
| `org.springframework.boot:spring-boot-starter-web` | `1.2.3.RELEASE` | BOM-managed (2.7.18) |  
| `org.springframework.boot:spring-boot-starter-actuator` | `1.2.3.RELEASE` | BOM-managed (2.7.18) |  
| `org.springframework.boot:spring-boot-starter-test` | `1.2.3.RELEASE` | BOM-managed (2.7.18) |  
| `org.spockframework:spock-core` | `1.0-groovy-2.4` | `2.3-groovy-4.0` |  
| `org.spockframework:spock-spring` | `1.0-groovy-2.4` | `2.3-groovy-4.0` |  
| `com.google.guava:guava` | `18.0` | `32.1.3-jre` |  
| `commons-io:commons-io` | `2.4` | `2.11.0` |  
| `commons-codec:commons-codec` | `1.10` | `1.17.2` |  
| `org.apache.maven.plugins:maven-compiler-plugin` | `3.0` | `3.16.0` |  
  
---  
  
## Risks  
  
- 🔴 **Runtime behaviour unverified** — The smoke-start tier was skipped (`Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)`). It is unknown whether the application boots and responds to HTTP requests under Java 17. A manual boot check is required before merging.  
  
- 🟡 **Groovy 4.x compile-time constant folding** — Groovy 4.x inlines `static final` expressions at compile time by executing them inside the Groovy compiler's classloader. `UUID.randomUUID()` in `FileExamples` was baked as a string constant in the spec bytecode at compile time, while `FileStorageStub` received a different UUID at test runtime, causing all 11 non-404 tests to return 404 silently. Fixed by switching to deterministic `UUID.fromString(...)` constants; teams should be aware of this Groovy 4.x behaviour when writing tests that cross the compile-time/runtime boundary.  
  
- 🟡 **Deprecated Guava API in `FileSystemPointer.java`** — `com.google.common.io.Files.hash(File, HashFunction)` is deprecated in Guava 30+ (post-upgrade-verification.log: `FileSystemPointer.java uses or overrides a deprecated API`). The build succeeds and no functional regression was observed, but this should be migrated to `Files.asByteSource(file).hash(hashFunction)` in a follow-up.  
  
- 🟡 **Spring Boot 2.7 is out of OSS support** — 2.7.18 is the final patch on the 2.x javax line. While it is the correct target for a javax-safe Java 17 upgrade, the application should be tracked for a future Spring Boot 3.x / Jakarta EE migration.  
  
- 🟢 **Spring static compatibility check: clean** — Runtime validation reports: *"OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)."* Spring Framework is managed entirely by the Boot 2.7.18 BOM (5.3.31); no residual old-Spring version pins were found.  
  
- 🟢 **javax namespace preserved** — No `jakarta.*` imports were introduced. The upgrade stays on `javax.*` throughout, consistent with the Spring Boot 2.7 / Spring Framework 5.3 javax-terminal line.  
  
---  
  
## Time Savings  
  
- ⏱️ **Transformation duration**: 20 minutes  
- 💰 **Estimated time saved vs. manual upgrade**: 180 minutes  
  
The saving reflects the multi-layer complexity: diagnosing a silent Surefire test-discovery failure, tracing a UUID mismatch to Groovy 4.x compile-time constant-folding via bytecode inspection, and aligning a full Spring Boot 1.x → 2.7 migration with Spock 1→2 / Groovy 2→4 stack replacements simultaneously.  
  
---  
  
## Next Steps  
  
### 🔍 Validation  
- 🔲 **Enable and run the smoke-start tier** (`export ENABLE_RUNTIME_VALIDATION=true`) to verify the application boots under Java 17 and the `/actuator/health` endpoint responds before merging  
- 🔲 **Confirm ETag SHA-512 behaviour end-to-end** with an integration or manual test — `Sha512ShallowEtagHeaderFilter` is not referenced by the current test suite and its behaviour is untested post-upgrade  
- 🔲 **Review `FileSystemStorage.findFile()`** (production `@Profile("!test")` stub) under Java 17 to ensure `java.nio.file.Files.probeContentType()` returns expected MIME types on the target OS  
  
### 🛠️ Improvement Recommendations  
- 🔲 **Migrate deprecated Guava call** in `FileSystemPointer.java`: `com.google.common.io.Files.hash(target, Hashing.sha512())` → `com.google.common.io.Files.asByteSource(target).hash(Hashing.sha512())`  
- 🔲 **Plan Spring Boot 3.x / Jakarta EE migration** — Spring Boot 2.7 is out of OSS support; the next upgrade will require `javax.*` → `jakarta.*` namespace migration across the codebase  
- 🔲 **Pin `org.junit.platform:junit-platform-launcher`** to a specific version in the BOM override to avoid silent version drift as Spring Boot minor releases update the managed JUnit Platform version  
- 🔲 **Review Guava `HttpHeaders` usage** in `DownloadControllerSpec` — `com.google.common.net.HttpHeaders` constants are still present in Guava 32.x but are candidates for replacement with `org.springframework.http.HttpHeaders` to remove the Guava test dependency

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 20 minute(s)
- ⏳ Estimated time saved: 180 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 37.10  
Includes 0.49 for job setup (startup smoke test and knowledge-base indexing).  
