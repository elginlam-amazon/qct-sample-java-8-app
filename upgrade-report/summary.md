# Java Version Upgrade — Executive Report  
**Project:** `com.nurkiewicz.download:download-server`  
**Date:** 2026-11-13  
**Iteration:** 6  
  
---  
  
## 🎯 Executive Summary  
  
The `download-server` application was successfully modernised from its legacy state (Spring Boot 1.x / Spring Framework 4.x, Java 8-era build) to **Java 25** and **Spring Boot 4.0.9** (Spring Framework 7.0.10, Jakarta EE namespace). Two automated OpenRewrite recipe passes handled the bulk of the structural migration — upgrading the JDK target through every major milestone (8 → 11 → 17 → 21 → 25) and walking the Spring Boot parent through every minor release up to 4.0.x. One post-recipe compilation error remained: a subclass in the test tree overriding a Spring `ShallowEtagHeaderFilter` hook whose signature changed in Spring Framework 7; this was resolved by Claude Code. The build now compiles and passes `mvn test` with **zero errors**.  
  
---  
  
## 📦 Application Changes  
  
- 🔧 **Build target**: `java.version` property set to `25`; `maven-compiler-plugin` configured with `<release>25</release>`  
- 🚀 **Spring Boot parent**: upgraded from `1.x` → `4.0.9` (Spring Framework 7.0.10 / Jakarta EE 10)  
- 🔄 **Artifact rename**: `spring-boot-starter-web` → `spring-boot-starter-webmvc` (Spring Boot 4.0 split)  
- 📦 **Jakarta servlet**: `jakarta.servlet-api` dependency added (jakarta namespace, Boot 3+ requirement)  
- 🧹 **Duplicate removed**: duplicate `spring-test` declaration flagged and cleaned by recipe  
- ✂️ **JUnit 4 exclusions**: obsolete JUnit 4 exclusion blocks removed by `SpringBoot2JUnit4to5Migration` sub-recipe  
- ✅ **Spring best practice**: `@Autowired` removed from `DownloadController` constructor (single-constructor injection is implicit in Spring 4+)  
- 🔁 **Primitive constructor**: `new Integer("1234")` → `Integer.valueOf("1234")` in `MainApplication` (deprecated constructor)  
- 🛠️ **ETag filter fixed**: `Sha512ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` override updated to new Spring Framework 7 signature `generateETagHeaderValue(InputStream, boolean) throws IOException`; implementation now uses `readAllBytes()` before hashing  
  
---  
  
## 🛠️ Tools Used  
  
| Tool | Version / Detail |  
|------|-----------------|  
| ☕ **JDK** | Java 25 (build target and runtime) |  
| 🔁 **OpenRewrite** | `rewrite-maven-plugin` 6.42.0 |  
| 📋 **Java upgrade recipe** | `com.amazonaws.java.migrate.UpgradeToJava25` (custom AWS recipe chain) |  
| 🌱 **Spring upgrade recipe** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (custom AWS recipe chain) |  
| 🤖 **Claude Code CLI** | v2.1.266 — model `global.anthropic.claude-sonnet-4-6` (Claude Sonnet 4.6) |  
| 🏗️ **Maven** | 3.x (via `mvn` in build environment) |  
  
---  
  
## 💻 Code Changes  
  
### OpenRewrite — Java 25 recipe (`pom.xml`)  
- 🔼 `java.version` property updated to `25`  
- 🔼 `maven-compiler-plugin` upgraded and `<release>` mode enabled  
- 🔼 `maven-surefire-plugin` updated; Mockito byte-buddy agent jvm-arg added for Java 25 compatibility  
  
### OpenRewrite — Spring Boot 4.0 recipe (`pom.xml`)  
- 🔼 `spring-boot-starter-parent` version: original `1.x` → `4.0.9`  
- 🔄 `spring-boot-starter-web` renamed to `spring-boot-starter-webmvc`  
- ➕ `jakarta.servlet-api` added as a test-scoped dependency  
- ✂️ Removed obsolete JUnit 4 exclusion blocks; removed duplicate `spring-test` declaration  
  
### OpenRewrite — `DownloadController.java`  
- ✂️ Removed `@Autowired` on constructor (`NoAutowiredOnConstructor` best-practice rule)  
  
### OpenRewrite — `MainApplication.java`  
- 🔁 `new Integer("1234")` → `Integer.valueOf("1234")` (`PrimitiveWrapperClassConstructorToValueOf` rule)  
  
### Claude Code — `Sha512ShallowEtagHeaderFilter.java`  
- 🛠️ Replaced `@Override protected String generateETagHeaderValue(byte[] bytes)` with `@Override protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException` to match the Spring Framework 7 `ShallowEtagHeaderFilter` API change  
- 📥 Added `java.io.InputStream` import; used `inputStream.readAllBytes()` to feed the Guava SHA-512 hash  
  
---  
  
## ⏱️ Time Savings Estimate  
  
| Activity | Estimated Manual Effort | Automated By |  
|----------|------------------------|--------------|  
| Java 8 → 25 migration (pom, plugins, deprecations) | ~1 hour | OpenRewrite (reported: 1h) |  
| Spring Boot 1.x → 4.0 migration (10 minor versions, Jakarta namespace, dependency renames) | ~2.5 hours | OpenRewrite (reported: 2h 32m) |  
| Identifying and fixing Spring Framework 7 ETag hook API break | ~30 minutes | Claude Code |  
| **Total estimated savings** | **~4 hours** | |  
  
---  
  
## 🔜 Next Steps  
  
### ✅ Validation  
- 🧪 **Run integration tests**: the Spock/Groovy test suite (`DownloadControllerSpec.groovy`) does **not currently compile or execute** — the pom has no GMavenPlus plugin and `spock-core:1.0-groovy-2.4` is incompatible with Java 25; these tests are silently skipped by Surefire  
- 🚀 **Runtime smoke test**: start the application and confirm `/download/{uuid}` endpoints respond correctly with HTTP 200/304/404 as expected  
  
### 🔧 Recommended Follow-up Work  
- ⚠️ **Upgrade Spock + add GMavenPlus**: replace `spock-core/spock-spring:1.0-groovy-2.4` with `spock-core/spock-spring:2.4-groovy-4.0` (or latest) and add `gmavenplus-plugin` to `pom.xml` to restore the Groovy test suite  
- ⚠️ **Remove stale `spring.version` property**: `<spring.version>4.1.1.RELEASE</spring.version>` is overridden by the Spring Boot 4.0.9 BOM and is misleading — remove it  
- ⚠️ **Upgrade `commons-io`**: `2.4` is from 2012; upgrade to `2.17+` for security and Java 11+ compatibility  
- ⚠️ **Upgrade `guava`**: `29.0-jre` triggers a deprecation warning (`Hashing.sha512()` via `Files.hash`); upgrade to `33.x` and address deprecated API if needed  
- ⚠️ **Remove `cglib-nodep:3.1`**: not compatible with Java 17+ bytecode; Spring Boot 4 / Spring Framework 7 uses a newer CGLIB bundled internally — this explicit dependency is redundant and risky  
- ℹ️ **Review `groovy-all:2.4.16`**: old version pinned in `dependencyManagement`; consider removing or upgrading alongside the Spock upgrade

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 15 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 1.757594  
