# Iteration 38 — Java & Spring Boot Upgrade Executive Report  
  
**Project:** `com.nurkiewicz.download:download-server`  
**Date:** 2026-11-28  
**Branch:** `main-upgrade-itr-38`  
  
---  
  
## 1. 📋 Executive Summary  
  
The `download-server` application has been successfully modernised from **Spring Boot 1.2.3 / Java 8** to **Spring Boot 4.0.10 / Java 25**, with Spring Framework advancing from 4.1.x to **7.0.11**. Two OpenRewrite recipe runs automated the bulk of the structural transformation (JDK version bump + full Spring Boot upgrade chain), and Claude CLI then resolved all remaining compilation failures, restored the Groovy/Spock test suite, and applied a set of dependency hygiene improvements. The final build produces a **BUILD SUCCESS** with **12/12 Spock tests passing** and zero compilation errors.  
  
---  
  
## 2. 🔧 Application Changes  
  
### Starting State  
- 🏷️ Spring Boot **1.2.3.RELEASE** → parent BOM  
- ☕ Java **8** (compiler 3.0, no `release` flag)  
- 🌱 Spring Framework **4.1.6.RELEASE** (explicit dependency)  
- 🧪 Spock **1.0-groovy-2.4** / Groovy **2.4.16** (old Codehaus coordinate) — Groovy tests were compiled but **not executed** (no Groovy compiler plugin wired)  
- 📦 `cglib-nodep:3.1`, `commons-io:2.4`, `guava:29.0-jre` (all stale)  
- 🚫 No `spring-boot-maven-plugin` — no executable fat-jar produced  
  
### Final State  
- 🏷️ Spring Boot **4.0.10** (Spring Framework **7.0.11** via BOM)  
- ☕ Java **25** (compiler 3.16.0, `--release 25`)  
- 🌱 Spring Framework **7.0.11** (all explicit `spring-*` deps removed — transitive via starters)  
- 🧪 Spock **2.5-groovy-5.0** / Apache Groovy **5.1.3** — 12 tests now **compiled and executed**  
- 📦 `commons-io:2.22.0`, `guava:33.8.0-jre` (current releases)  
- ✅ `spring-boot-maven-plugin` added — fat-jar packaging enabled  
- 🚫 javax → **jakarta** namespace migration completed (servlet API, annotations)  
  
---  
  
## 3. 🛠️ Tools Used  
  
| Tool | Version / Detail |  
|------|-----------------|  
| ☕ **Java SDK** | OpenJDK 25 (target `--release 25`) |  
| 🔄 **OpenRewrite** | `rewrite:6.42.0` via Maven plugin |  
| 📦 **OpenRewrite Recipe — Java** | `com.amazonaws.java.migrate.UpgradeToJava25` (chain: Java 8 → 11 → 17 → 21 → 25) |  
| 🌱 **OpenRewrite Recipe — Spring Boot** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (chain: Boot 1.x → 2.0 → … → 3.5 → 4.0) |  
| 🤖 **Claude CLI** | Version **2.1.332** (Claude Code), model **`global.anthropic.claude-sonnet-4-6`** |  
| 🏗️ **Maven** | 3.9.8 (wrapper) |  
  
---  
  
## 4. 💻 Code Changes  
  
### OpenRewrite — Java Upgrade (`UpgradeToJava25`)  
Applied across **13 files** (all 8 main sources + 5 test sources):  
  
- 📝 `pom.xml` — set `java.version=25`; switched compiler plugin to `--release` configuration; upgraded `maven-compiler-plugin` to 3.16.0; added Mockito byte-buddy javaagent to Surefire `argLine`  
- 🔧 `MainApplication.java` — `Integer.valueOf("1234")` replaced for `PrimitiveWrapperClassConstructorToValueOf` rule (was `new Integer(...)` in the original)  
  
### OpenRewrite — Spring Boot Upgrade (`UpgradeSpringBoot_4_0`)  
Applied across **12 files**:  
  
- 📝 `pom.xml` — parent bumped `1.2.3.RELEASE` → `4.0.10`; `spring-boot-starter-web` → `spring-boot-starter-webmvc` (Boot 4 rename); `jakarta.servlet-api` dependency added (javax→jakarta migration); duplicate `spring-test` declaration removed; JUnit 4 → JUnit 5 exclusion/inclusion updated; `maven-dependency-plugin` added for Surefire byte-buddy agent resolution  
- 🔧 `DownloadController.java` — `@Autowired` removed from constructor (Spring best-practice, `NoAutowiredOnConstructor` recipe)  
- 🌐 All 8 Java source files touched — `javax.servlet.*` imports migrated to `jakarta.servlet.*` where present; Spring Framework API compatibility passes applied (Framework 5.0 → 5.1 → 5.2 → 5.3 → 6.0 → 7.0 chain)  
  
### Claude CLI — Build Fix & Hardening (Iteration 38)  
Manual fixes applied after OpenRewrite left the build in a broken state:  
  
- 🐛 **`Sha512ShallowEtagHeaderFilter.java`** — `generateETagHeaderValue(byte[] bytes)` signature no longer exists in Spring Framework 7; updated override to `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`, reading bytes via Guava `ByteStreams.toByteArray()`  
- 🧪 **Groovy/Spock test suite restored** — upgraded `spock-core` / `spock-spring` from `1.0-groovy-2.4` → `2.5-groovy-5.0`; replaced `org.codehaus.groovy:groovy-all:2.4.16` (main-scope, broken on JDK 17+) with `org.apache.groovy:groovy:5.1.3` (test-scope); added `gmavenplus-plugin:5.1.0` to compile `src/test/groovy`; added `junit-platform-launcher` so Surefire's JUnit Platform provider could discover Spock specs; added `**/*Spec.class` to Surefire `<includes>`  
- 🪵 **`logback.xml`** — anonymous `<appender>` nested inside `<root>` (rejected by Logback 1.5.x — missing `name` attribute) replaced with named `CONSOLE` appender + `<appender-ref>`  
- 🪵 **`src/test/resources/logback-test.xml`** — new file; sets WARN-level during tests for clean surefire output  
- 📦 **`FileSystemPointer.java`** — `Files.hash(target, Hashing.sha512())` (deprecated in Guava 25.1+) → `Files.asByteSource(target).hash(Hashing.sha512())`  
- 📦 **`pom.xml` dependency cleanup** — removed 8 redundant explicit `spring-{beans,context,context-support,webmvc,web,aop,core,expression}` declarations (all transitive via starter); removed `cglib-nodep:3.1` (Spring Framework 5+ bundles its own CGLIB); removed stale `spring.version=4.1.1.RELEASE` property; removed stale `repo.spring.io/milestone` repository  
- 🏗️ **`spring-boot-maven-plugin`** added — `mvn package` now produces a runnable fat-jar  
- ⬆️ `commons-io` **2.4 → 2.22.0**; `guava` **29.0-jre → 33.8.0-jre**  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
| Activity | Manual Estimate | Automated By |  
|----------|----------------|-------------|  
| Java 8 → 25 version bump + build tooling | ~1 hr | OpenRewrite (self-reported: 1h) |  
| Spring Boot 1.x → 4.0 full upgrade chain (10 minor releases) | ~8–12 hrs | OpenRewrite (self-reported: 2h 32m — conservative) |  
| javax → jakarta namespace migration across all sources | ~2 hrs | OpenRewrite |  
| `Sha512ShallowEtagHeaderFilter` API fix (Spring FW 7 method signature) | ~30 min | Claude CLI |  
| Groovy/Spock 1.x → 2.5 upgrade + GMavenPlus wiring + test discovery | ~2 hrs | Claude CLI |  
| Logback config fix, dependency hygiene, packaging | ~1 hr | Claude CLI |  
| **Total estimated manual effort avoided** | **~15–18 hrs** | |  
  
---  
  
## 6. ➡️ Next Steps  
  
### Validation  
- 🔍 **Boot smoke-test** — start the app on JDK 25 (`mvn spring-boot:run` or `java -jar target/download-server-0.0.1-SNAPSHOT.jar`) and hit `GET /actuator/health` to verify Spring context starts cleanly; the previous iteration note warns about startup-only failures invisible to `mvn test`  
- 🧪 **Extend test coverage** — `DownloadControllerSpec` covers the happy-path controller only; add tests for `FileSystemStorage` (non-test profile), actuator endpoints (`/actuator/health`, `/actuator/info`), and `Sha512ShallowEtagHeaderFilter` registration  
- 🔐 **CVE / dependency scan** — run OWASP Dependency-Check or `mvn dependency:analyze` against the resolved set; `guava:33.8.0-jre` and `commons-io:2.22.0` should now be clean, but the unmanaged versions should be confirmed  
  
### Improvement Recommendations  
- 🧹 **Remove `spring-test` exclusion pattern** — the pom excludes `spring-test` from `spring-boot-starter-test` and re-declares it separately; this is a leftover workaround that can be simplified to a single transitive dependency  
- 📂 **Replace `FileSystemStorage` stub** — the production `FileSystemStorage` currently returns `logback.xml` for every UUID; this placeholder needs a real storage implementation before production deployment  
- 🗑️ **Dead code in `MainApplication`** — `Integer.valueOf("1234")` (auto-converted by OpenRewrite from `new Integer(...)`) is left over from migration and serves no purpose; remove it  
- 📝 **Review commit `80ea78e`** on `main-upgrade-itr-37` and the current branch `main-upgrade-itr-38` against base `73556be` before raising a pull request

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 10 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 2.884764  
