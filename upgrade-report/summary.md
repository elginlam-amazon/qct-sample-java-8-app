# Java Version Upgrade — Executive Report  
**Project:** `com.nurkiewicz:download-server` | **Date:** 2026-11-11 | **Iteration:** 3  
  
---  
  
## 1. 📋 Executive Summary  
  
This iteration successfully upgraded the `download-server` application from **Java 8 / Spring Boot 1.2.3** to **Java 21 / Spring Boot 3.2.12** — a two-major-version leap spanning the full Spring 5→6 migration, the Jakarta EE namespace transition, and a dormant test toolchain that was never executing at baseline. The automated transformation resolved all mandatory dependency upgrades, remediated known CVEs, adapted two Spring API breaking changes, and enabled the previously inert Spock test suite. The final build exits `BUILD SUCCESS` with **12/12 tests passing** (vs. 0 tests executing at baseline), zero `javax.*` imports, and a confirmed runtime smoke test on Java 21. Changes were committed to branch `atx-result-staging-20261111_013449_4e339c68` with a clean working tree.  
  
---  
  
## 2. 🏗️ Application Changes  
  
- 🔵 **Single-module Maven application** — `com.nurkiewicz.download:download-server`  
- 🔵 **Role** — HTTP file-download server with ETag support and Spring Boot Actuator health endpoint  
- 🔵 **Baseline state** — Java 8 target, Spring Boot 1.2.3.RELEASE, 12 Spock specs present but **never compiled or executed** due to missing Groovy compilation plugin and surefire configuration  
- 🔵 **Post-upgrade state** — Java 21 target, Spring Boot 3.2.12 (LTS), all 12 Spock specs compiled and passing  
- 🔵 **Scope** — 6 source files modified across `pom.xml`, main source, test source, and configuration  
- 🔵 **Jakarta EE** — N/A; no `javax.*` imports existed and none were introduced  
- 🔵 **Database / ORM** — N/A; no JDBC, JPA, or connection-pool dependencies in this project  
  
---  
  
## 3. 🛠️ Tools Used  
  
### ☕ Java SDK — Amazon Corretto  
- **Source JVM:** Amazon Corretto **Java 8** (`/usr/lib/jvm/java-1.8.0`)  
- **Target JVM:** Amazon Corretto **Java 21** (`/usr/lib/jvm/java-21`)  
- Multiple SDKs available on the host (8, 11, 17, 21, 25) — upgrade targeted the mandated LTS release  
- **Build tool:** Apache Maven **3.9.16**  
  
### 🤖 AWS Transform Custom CLI (`atx`)  
- **Transformation:** `AWS/java-version-upgrade`  
- **Region:** `ap-southeast-2`  
- **Configuration:** `file:///data/iteration-3/atx-config-java.json`  
- **Command:** `atx custom def exec --transformation-name AWS/java-version-upgrade --non-interactive --trust-all-tools`  
- **Agent minutes consumed:** 49.75  
- **Conversation log:** `~/.aws/atx/custom/20261111_013449_4e339c68/logs/`  
- **Applied learnings** from prior iterations (dependency version verification, Spring Boot/Java compatibility)  
  
---  
  
## 4. 💻 Code Changes  
  
### `pom.xml` *(primary — 62 lines added, 106 deleted)*  
- ⬆️ Parent `spring-boot-starter-parent` **1.2.3.RELEASE → 3.2.12**  
- ⬆️ `maven-compiler-plugin` **3.0 → 3.13.0**; replaced `<source>/<target>1.8` with `<release>21</release>`  
- ➕ Properties: `<java.version>21</java.version>`, `<maven.compiler.release>21</maven.compiler.release>`  
- 🗑️ Removed `<spring.version>` property and all pinned `org.springframework:*` explicit versions (spring-core, spring-beans, spring-web, spring-webmvc, spring-aop, spring-context, spring-expression, spring-test) — now BOM-managed via Spring Boot 3.2.12 → resolves to **Spring 6.1.15**  
- 🗑️ Removed insecure `http://repo.spring.io/milestone/` repository block  
- 🗑️ Removed duplicate `spring-test` test-scoped declarations  
- ⬆️ `commons-io` **2.4 → 2.16.1** ✅ CVE PASS  
- ⬆️ `commons-codec` **1.10 → 1.16.1** ✅ CVE PASS  
- ⬆️ `com.google.guava:guava` **18.0 → 33.7.2-jre** ✅ CVE PASS *(33.2.1-jre had HIGH CVE-2026-102554)*  
- ⬆️ `cglib-nodep` **3.1 → 3.3.0**  
- ⬆️ `spock-core` / `spock-spring` **1.0-groovy-2.4 → 2.4-M4-groovy-4.0**  
- ⬆️ Groovy: `org.codehaus.groovy:groovy-all 2.4.3` → `org.apache.groovy:groovy 4.0.22` (test scope)  
- ➕ Added `gmavenplus-plugin 3.0.2` with `compileTests` goal — enables Groovy/Spock compilation  
- ➕ Added `maven-surefire-plugin` includes for `**/*Spec.java` and `**/*Test.java` — Spock specs now execute  
  
### `MainApplication.java`  
- 🗑️ Removed deprecated `new Integer("1234")` dead-code line (removed constructor on Java 21)  
  
### `logback.xml`  
- 🔧 Hoisted unnamed inline `<appender>` to top-level named `<appender name="CONSOLE">` with `<appender-ref ref="CONSOLE"/>` in `<root>` — required by logback **1.5.x** (bundled with Spring Boot 3.x)  
  
### `Sha512ShallowEtagHeaderFilter.java`  
- 🔧 Adapted Spring 6 API break: `generateETagHeaderValue(byte[])` → `generateETagHeaderValue(InputStream inputStream, boolean isWeak)`  
- 🔧 Reads stream via `ByteStreams.toByteArray()`, hashes with `Hashing.sha512()`, preserves `W/` weak-ETag prefix semantics  
  
### `DownloadControllerTest.groovy`  
- 🔧 Replaced `@ContextConfiguration(classes = [MainApplication])` with `@SpringBootTest(classes = [MainApplication])` — required for Spring 6 Web MVC autoconfiguration to apply correctly with Spock 2.4  
  
### `FileExamples.java`  
- 🔧 Replaced `UUID.randomUUID()` static fixture fields with fixed `UUID.fromString(...)` constants — eliminates race condition between Java and Groovy class-initialization contexts in a single test run  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
| Task | Manual Estimate | Automated |  
|------|----------------|-----------|  
| Dependency upgrade research & CVE scanning (8 deps) | ~4 hrs | ✅ Automated |  
| Spring Boot 1.x → 3.x migration guide analysis | ~3 hrs | ✅ Automated |  
| Spring 6 API break detection & fix (`ETagHeaderFilter`) | ~2 hrs | ✅ Automated |  
| logback 1.5 compatibility fix | ~1 hr | ✅ Automated |  
| Groovy/Spock 2.x toolchain migration | ~3 hrs | ✅ Automated |  
| Test suite diagnosis (dormant specs) & fix | ~2 hrs | ✅ Automated |  
| Jakarta EE namespace audit | ~1 hr | ✅ Automated |  
| Build validation & smoke testing | ~1 hr | ✅ Automated |  
| **Total estimated manual effort** | **~17 hrs** | **49.75 agent-minutes** |  
  
> 💡 **Estimated time savings: ~16–17 engineer-hours.** Complexity drivers include the full Spring 4→6 jump, an API-breaking filter override requiring `javap` introspection of the resolved jar, a dormant test toolchain requiring multi-layer diagnosis, and a CVE-driven Guava version selection (bypassing a flagged intermediate release).  
  
---  
  
## 6. ✅ Next Steps  
  
### 🔍 Validation  
- 🔲 Run `JAVA_HOME=/usr/lib/jvm/java-21 mvn clean verify` on a clean CI runner to confirm offline results hold with fresh dependency resolution  
- 🔲 Merge `atx-result-staging-20261111_013449_4e339c68` → feature branch and open a pull request; review `git diff 73556be..HEAD` for final sign-off  
- 🔲 Deploy to a staging environment and exercise `/actuator/health`, `/download/{uuid}`, and `/download/{uuid}/{filename}` endpoints under realistic load  
- 🔲 Verify ETag round-trip behaviour (`If-None-Match` → 304) with the new SHA-512 filter signature  
  
### 🔒 Security  
- 🔲 **Address jackson-databind CVEs** — `2.15.4` (managed by Spring Boot 3.2.12 BOM) carries 5 HIGH + 7 MEDIUM CVEs fixed in 2.18.11; options:  
  - Override `<jackson.version>2.18.11</jackson.version>` in `pom.xml`  
  - Or upgrade Spring Boot parent to `3.4.x` / `3.5.x` which manages a remediated Jackson version  
- 🔲 Confirm no additional transitive dependency CVEs via `mvn dependency-check:check` or equivalent SBOM scan  
  
### 🚀 Improvements  
- 🔲 **Fat-jar packaging** — `spring-boot-maven-plugin` was trialled and reverted; `FileSystemStorage` resolves a classpath resource as a `java.io.File` (pre-existing design limitation that fails inside a JAR). Refactor to use `ClassPathResource` stream access to unlock executable-jar deployment  
- 🔲 **Java 21 virtual threads** — consider enabling Spring Boot's virtual-thread executor (`spring.threads.virtual.enabled=true`) for throughput gains on the file-streaming workload  
- 🔲 **Upgrade `atx`** — version 3.23.0 is available (`atx update`) for future transformation runs  
- 🔲 **Groovy 4 / Spock 2.4-M4** — monitor for Spock 2.4 GA release and pin to stable once published

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 11 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 0

## Kiro credit usage

💳 Kiro CLI credits used: 1.25  
Includes 0.09 for job setup (startup smoke test and knowledge-base indexing).  
