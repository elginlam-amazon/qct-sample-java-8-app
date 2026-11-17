# Java Version Upgrade — Executive Report  
  
**Project:** `com.nurkiewicz` download-server (Spring Boot Maven application)  
**Date:** November 17, 2026  
**Iteration:** 32  
  
---  
  
## 📋 Executive Summary  
  
This upgrade successfully modernised a Spring Boot 1.2.3 / Java 8 download-server application to **Java 21** with **Spring Boot 3.5.16** and a fully passing test suite. The baseline compiled but executed **zero tests** (the Groovy/Spock suite was silently dormant due to a missing compiler plugin). After the upgrade, all **12/12 Spock tests pass** on both JDK 21 and JDK 25. CVE scanning was applied to resolved dependency versions, resulting in several targeted version overrides that go beyond the originally mandated versions — all deviations are documented in the repository. The build is green, the application boots and serves files on JDK 21, and the working tree is clean.  
  
---  
  
## 🏗️ Application Changes  
  
- 🔄 **Spring Boot parent** upgraded `1.2.3.RELEASE` → `3.5.16` (Spring Framework 6.2.19)  
- ☕ **Java compile target** moved from `source/target 1.8` to `release 21`  
- 📦 **Spring dependency management** migrated from explicit per-artifact version pins (`4.1.6.RELEASE`) to Spring Boot BOM management — 9 explicit Spring version pins removed  
- 🔒 **CVE-clean dependency overrides** applied: Tomcat `10.1.62`, Jackson `2.21.8`, Log4j API `2.25.5`  
- 🧪 **Groovy/Spock test suite** activated — previously compiled by no plugin, now wired and running (`12/12` pass)  
- 📝 **README.md** extended with upgrade deviations, skipped items, and residual CVE documentation  
- ⚠️ **Residual CVEs noted**: `spring-webmvc` 6.2.19 carries CVE-2026-47884 and CVE-2026-47890, fixable only in Spring 7.x / Boot 4.x (application uses neither XsltView nor SSE fragments)  
  
---  
  
## 🛠️ Tools Used  
  
- ☕ **Java SDK — Amazon Corretto JDK 21** (`java-21-amazon-corretto.aarch64`)  
  - Used for compilation (`maven.compiler.release=21`) and runtime validation  
  - Build also verified with the environment's default JDK 25 to confirm forward compatibility  
  - Maven 3.9.16 (`mvnw`) used throughout  
  
- 🤖 **AWS Transform Custom CLI (`atx`)**  
  - Transformation: `AWS/java-version-upgrade`, region `ap-southeast-2`  
  - Executed in `--non-interactive --trust-all-tools` mode  
  - Applied three learnings from prior runs: `dependency-version-verification`, `spock-groovy-test-execution`, `spring-boot-java-compatibility`  
  - Agent minutes consumed: **55.88**  
  - Produced structured plan, worklog, and validation summary artifacts  
  
---  
  
## 💻 Code Changes  
  
**7 files changed · 91 lines added · 65 lines deleted · 156 lines modified**  
  
### `pom.xml`  
- ✏️ `spring-boot-starter-parent` `1.2.3.RELEASE` → `3.5.16`  
- ✏️ `maven-compiler-plugin` `3.0` with `<source>/<target>` → `3.13.0` with `<release>${java.version}</release>`  
- ➕ `java.version=21`, `groovy.version=5.0.8`, `spock.version=2.5-groovy-5.0` properties  
- ➕ CVE override properties: `tomcat.version`, `jackson-bom.version`, `log4j2.version`  
- ➕ `gmavenplus-plugin 5.1.0` with `targetBytecode 21`, `addTestSources` + `compileTests` goals  
- ➕ `maven-surefire-plugin` `**/*Spec.java` include to run Spock specs  
- 🔄 `commons-io` `2.4` → `2.22.0` · `commons-codec` `1.10` → `1.23.0` · `guava` `18.0` → `33.8.0-jre` · `cglib-nodep` `3.1` → `3.3.0`  
- 🔄 `org.codehaus.groovy:groovy-all 2.4.3` → `org.apache.groovy:groovy 5.0.8`  
- 🔄 `spock-core` / `spock-spring` `1.0-groovy-2.4` → `2.5-groovy-5.0`  
- 🗑️ Removed explicit Spring `4.1.6.RELEASE` version pins (9 removed, now BOM-managed)  
- 🗑️ Removed insecure `http://repo.spring.io/milestone` repository (unused; blocked by Maven 3.8+)  
- 🗑️ Removed duplicate `spring-test` dependency  
  
### `src/main/java/com/nurkiewicz/download/MainApplication.java`  
- ✏️ `new Integer("1234")` → `Integer.valueOf("1234")` (deprecated constructor removed in newer Java)  
  
### `src/main/resources/logback.xml`  
- ✏️ Unnamed inline appender inside `<root>` → named `CONSOLE` appender with `<appender-ref>` (required by logback 1.5+)  
  
### `src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy`  
- ✏️ `@ContextConfiguration(classes = [MainApplication])` → `@SpringBootTest(classes = [MainApplication])`  
- 🗑️ Removed `import org.springframework.test.context.ContextConfiguration`  
- ➕ `import org.springframework.boot.test.context.SpringBootTest`  
  
### `src/test/java/com/nurkiewicz/download/FileExamples.java`  
- ✏️ `UUID.randomUUID()` fixtures replaced with deterministic `UUID.fromString(...)` values to prevent test flakiness across separate class initialisations  
  
### `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
- ✏️ Method signature updated from `generateETagHeaderValue(byte[])` → `generateETagHeaderValue(InputStream, boolean)` to match the Spring 6 `ShallowEtagHeaderFilter` API  
- ➕ `ByteStreams.toByteArray(inputStream)` to read the stream; `isWeak` flag honoured in returned ETag value  
  
### `README.md`  
- ➕ "Java 21 upgrade notes" section: deviations table, skipped/non-applicable items, known residual CVEs  
  
---  
  
## ⏱️ Time Savings Estimate  
  
| Task | Manual Estimate | Automated |  
|------|----------------|-----------|  
| Dependency research & version selection | 3–5 hrs | ✅ included |  
| `pom.xml` restructuring (BOM migration, 9 version pins) | 2–3 hrs | ✅ included |  
| CVE scanning of resolved dependency tree | 3–5 hrs | ✅ included |  
| Java compatibility fixes (deprecated APIs) | 1–2 hrs | ✅ included |  
| Groovy/Spock test suite diagnosis & wiring | 3–5 hrs | ✅ included |  
| Spring 6 API migration (`@SpringBootTest`, ETag filter, logback) | 2–4 hrs | ✅ included |  
| Validation & documentation | 1–2 hrs | ✅ included |  
| **Total estimated manual effort** | **15–26 hrs** | **~1 hr agent time** |  
  
- 🚀 **Estimated time saved: ~15–25 developer-hours** (roughly 2–3 working days)  
- 🎯 Complexity factors: multi-major-version Spring jump (1.x → 3.x), dormant test suite requiring diagnosis, Groovy compiler toolchain upgrade, CVE-driven deviations from mandated versions  
  
---  
  
## 🔜 Next Steps  
  
### ✅ Validation Steps  
- 🧪 Run the full test suite with the CI pipeline and confirm `12/12` Spock tests pass in the target environment  
- 🏗️ Perform an integration / smoke test: start the application and exercise `/download/<uuid>/<filename>`, ETag `304` caching, and `/actuator/health`  
- 🔍 Review the two residual `spring-webmvc` CVEs (CVE-2026-47884, CVE-2026-47890) with the security team and confirm the application does not use `XsltView` or SSE fragment rendering  
- 📋 Merge the `atx-result-staging-20261117_052855_8a34ae7f` branch and raise a PR per the generated `git_instructions.md`  
  
### 🔧 Improvement Recommendations  
- ⬆️ **Plan a Spring Boot 4.x / Spring Framework 7.x upgrade** to fully remediate the residual `spring-webmvc` CVEs — this is the next logical major-version step  
- 🔒 **Add a dependency vulnerability scanner** (e.g. OWASP Dependency-Check or Trivy) to the CI pipeline to catch new CVEs on each build  
- 🧹 **Remove the now-unused `cglib-nodep`** dependency — Spring Boot 3+ uses CGLIB from `spring-core`; the explicit dependency may be vestigial  
- 📦 **Evaluate removing the explicit Guava dependency** — confirm it is used directly in production code and not just a transitive test aid  
- 🧪 **Expand the Spock test suite** — only 12 tests existed; the previously dormant suite suggests coverage debt worth addressing  
- 📌 **Pin the Maven wrapper** to a specific Maven version in `maven-wrapper.properties` to avoid build drift across environments (JDK 25 vs JDK 21)

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 10 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 0

## Kiro credit usage

💳 Kiro CLI credits used: 1.32  
Includes 0.09 for job setup (startup smoke test and knowledge-base indexing).  
