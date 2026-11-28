# 📊 Executive Report — Java 25 / Spring Boot 4.0 Upgrade  
  
**Project:** `com.nurkiewicz.download:download-server` (0.0.1-SNAPSHOT)  
**Iteration:** 68 · **Date:** 2026-11-28 · **Pipeline window:** 07:52 → 08:05 UTC (~13 min)  
**Source of truth:** `/data/iteration-68/code-upgrade.log` + OpenRewrite data tables  
  
---  
  
## 1. 🎯 Executive Summary  
  
- ✅ **The upgrade completed successfully.** A Spring Boot **1.2.3 / Java 8** application was carried to **Spring Boot 4.0.10 on Spring Framework 7.0.11, Jakarta EE 11, running on Java 25** — three Spring Boot major generations and four JDK LTS hops in a single automated pass.  
- 🤖 Two OpenRewrite recipe campaigns did the bulk mechanical work; an AI agent then resolved the residue and verified the result. **Final state: `mvn -B clean install` → BUILD SUCCESS, zero compilation errors, 18/18 tests passing, stable across 3 consecutive clean runs.**  
- 🔧 **Only one genuine compilation error** survived the recipes — a removed Spring Framework 7 extension point — making this a low-friction migration for its scope.  
- 🔍 **The two highest-value findings were invisible to the compiler.** The project's entire test suite was never being executed (no Groovy compiler plugin had ever been configured), and the logging config was being silently rejected by modern Logback, leaving the app with no console output. Both were pre-existing conditions that a green build would have concealed indefinitely.  
- 📈 Net test coverage moved from **0 tests actually executing → 18 passing**, including a real embedded-Tomcat startup test.  
- ⚠️ **One area remains genuinely unverified:** fat-jar packaging — and it is coupled to a latent resource-loading defect in the storage stub (see §6).  
  
---  
  
## 2. 🔄 Application Changes  
  
### Framework & platform  
  
| Component | Before | After | 📍 Status |  
|---|---|---|---|  
| ☕ Java | 8 (`source/target 1.8`) | **25** (`<release>`) | ✅ Applied |  
| 🍃 Spring Boot | 1.2.3.RELEASE | **4.0.10** | ✅ Applied |  
| 🌱 Spring Framework | 4.1.6.RELEASE | **7.0.11** | ✅ Applied |  
| 📦 Jakarta EE | Java EE (javax) | **Jakarta EE 11** | ✅ Applied |  
| 🐱 Servlet container | Tomcat 7/8 era | **Tomcat 11.0.27** (Servlet 6.1) | ✅ Applied |  
| 🧪 Test platform | JUnit 4 + Spock 1.0 | **JUnit 6.0.3 + Spock 2.5** | ✅ Applied |  
| 📝 Logback | 1.x legacy | **1.5.38** | ✅ Applied |  
  
### 🍃 Spring upgrade outcome — reported from effective evidence  
  
> 📋 **Accuracy note:** the log contains **no** `Verified: effective Spring Framework version…` line and **no** `WARNING: …still < 5.3` line (both greps return 0 matches). Those checks belong to the *non-Boot Spring 5.3 / javax* pipeline. This project ran the **Spring Boot 4.0 pipeline**, which emits no such assertion. The outcome below is therefore taken from the OpenRewrite `SourcesFileResults` tables and the resolved dependency graph, not from an intent line.  
  
- ✅ **Recipe actually run:** `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` → delegating to `org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0`, which chained the full cumulative ladder **2.0 → 2.7 → 3.0 → 3.5 → 4.0**.  
- ✅ **`UpgradeParentVersion`** applied with `{spring-boot-starter-parent, newVersion=4.0.x}` — recorded as a real `pom.xml` change in `SourcesFileResults`.  
- ✅ **Spring Framework ladder applied:** `UpgradeSpringFramework_5_0` → `5_1` → `5_2` → `5_3` → `6_0`, all present as effective pom changes.  
- ✅ **Starter rename applied:** `ChangeDependency` `spring-boot-starter-web` → **`spring-boot-starter-webmvc`** (the Boot 4 structural rename).  
- ✅ **Jakarta migration applied:** `JakartaEE10`, `JavaxMigrationToJakarta`, `JavaxServletToJakartaServlet`.  
- ✅ **Effective resolved versions confirmed independently** — `mvn dependency:list` reports **Spring Boot 4.0.10 / Spring Framework 7.0.11** across every artifact, and the runtime banner printed `Running with Spring Boot v4.0.10, Spring v7.0.11`.  
- 🎯 **Verdict: the Spring upgrade is COMPLETE, not partial.** It did not stall below target — it landed two major lines *above* the 5.3 threshold that the absent warning line would have policed.  
  
### 🧭 Namespace posture  
  
- ✅ Ended on **jakarta**, which is mandatory and the only valid option on Spring Boot 4 / Spring Framework 7 — the line has no javax variant.  
- ✅ `grep -rn "javax\." src/` → **zero matches**. No mixed javax/jakarta stack; no stale EE artifacts.  
- 📌 Resolved EE APIs: `jakarta.servlet-api` 6.1.0, `jakarta.annotation-api` 3.0.0, `jakarta.activation-api` 2.1.4, `jakarta.xml.bind-api` 4.0.5.  
  
---  
  
## 3. 🛠️ Tools Used  
  
| Tool | Version | Role |  
|---|---|---|  
| ☕ **Amazon Corretto JDK** | **25.0.5.1** (`25.0.5.1+2-LTS`, aarch64) | Target runtime & compiler |  
| 🏗️ **Apache Maven** | 3.9.16 (wrapper pins 3.9.8) | Build orchestration |  
| 🔧 **OpenRewrite Maven plugin** | **`rewrite:6.42.0`** | Automated refactoring engine |  
| 📜 **Recipe — JDK** | `com.amazonaws.java.migrate.UpgradeToJava25` | Java 8 → 25 ladder |  
| 📜 **Recipe — Spring** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` | Boot 1.2 → 4.0 ladder |  
| 🤖 **Claude CLI** | model **`claude-opus-5`** (Opus 5, via AWS Bedrock) | Residual error resolution, dependency reconciliation, verification |  
| 🧩 **gmavenplus-plugin** | 5.1.0 | Groovy test compilation (added) |  
| 🧪 **Spock / JUnit** | Spock 2.5-groovy-5.0 · JUnit 6.0.3 | Test execution |  
| 📚 **Migration knowledge base** | 38 domain docs under `/app/knowledge` | Pattern reference |  
  
- 📊 **Agent run cost:** $4.01 for the iteration (14 + 5 turns across two prompts), 0 subagents spawned, 0 permission denials.  
  
---  
  
## 4. 💻 Code Changes  
  
### 🤖 Applied automatically by OpenRewrite  
  
- 📄 **Textual changes landed in exactly 3 files:** `pom.xml`, `DownloadController.java`, `MainApplication.java`.  
  - 🧹 `DownloadController.java` — removed redundant `@Autowired` on a single-arg constructor (`NoAutowiredOnConstructor`).  
  - 🔢 `MainApplication.java` — `new Integer("1234")` → `Integer.valueOf("1234")` (deprecated boxing constructor, removal-flagged in modern JDKs).  
  - 📦 `pom.xml` — parent bump, starter rename, Java `<release>` 25, compiler plugin 3.0 → 3.16.0, removal of ~10 hard-coded `4.1.6.RELEASE` / `1.2.3.RELEASE` versions in favour of BOM management, `RemoveDuplicateDependencies` (fixing a real duplicate `spring-test` declaration that Maven had been warning about), and a Mockito `-javaagent` surefire argLine.  
- 📌 **Accuracy caveat:** the `SourcesFileResults` tables list **13 source paths**, but most entries are `UpgradeJavaVersion` *marker* updates that carry no source-text change. Only the 3 files above were actually rewritten.  
- 🔢 **127 unique (file, recipe) results** for the JDK campaign; **189** for the Spring campaign.  
  
### 🤖 Applied by the Claude agent  
  
- 🔴 **Fixed the sole compilation error** — `Sha512ShallowEtagHeaderFilter.java:8`: `method does not override or implement a method from a supertype`. Spring Framework 7 changed the `ShallowEtagHeaderFilter` hook from `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean isWeak)`. Ported to the new signature, preserving SHA-512 ETag semantics and honouring the new weak-ETag flag with the standard `W/` prefix.  
- 🧪 **Revived a dormant test suite.** `src/test/groovy` had **never** been compiled — the pom has never carried a Groovy plugin, so surefire reported **zero tests** both before and after the upgrade (confirmed: `initial-build.log` shows `surefire:2.17` with no `Tests run` line). Added `gmavenplus-plugin` (`compileTests`) and a `**/*Spec.java` surefire include, since the compiled class `DownloadControllerSpec` matched none of surefire's default patterns. → **12 tests revived.**  
- 📝 **Fixed silently-broken logging.** `logback.xml` nested an **unnamed** `<appender>` inside `<root>`; Logback 1.5.38 rejects this (`Missing attribute [name] … near line 9`) and **discarded the appender**, leaving the app with no console output. Declared a named `CONSOLE` appender and referenced it via `<appender-ref>`, preserving the original encoder pattern verbatim.  
- 📦 **Reconciled the dependency set** left stale by the recipes:  
  - `org.codehaus.groovy:groovy-all:2.4.16` (compile) → **`org.apache.groovy:groovy`, test scope, BOM-managed 5.0.8** — Groovy 2.4 cannot run on Java 25; test-scoping also avoids `groovy-templates` activating `GroovyTemplateAutoConfiguration`, a known JDK 17+ startup crash.  
  - `spock-core` / `spock-spring` **1.0-groovy-2.4 → 2.5-groovy-5.0** (JUnit Platform native).  
  - 🗑️ Removed `cglib-nodep:3.1` (zero source references; Spock 2 uses ByteBuddy).  
  - 🗑️ Removed the dead `<spring.version>4.1.1.RELEASE</spring.version>` property and the `spring-test` separate-declaration + exclusion pair (a Boot 1.2-era workaround, now a no-op).  
  - 🔒 Removed a plaintext-HTTP `repo.spring.io/milestone` repository the log shows was unreachable anyway (`Failed to access maven repository http://repo.spring.io/milestone/`).  
  - ⬆️ `commons-io` 2.4 → 2.22.0 · `guava` 29.0-jre → 33.8.0-jre.  
- ➕ **Added 6 regression tests** (12 → 18):  
  - `ApplicationStartupTest` (3) — `@SpringBootTest(RANDOM_PORT)` on **real embedded Tomcat**: `/actuator/health` → 200 `UP`, a streamed download with SHA-512 ETag, and 404 for unknown files. Also exercises the new Jackson 3 (`tools.jackson`) serialization path.  
  - `Sha512ShallowEtagHeaderFilterTest` (3) — pins strong ETag, weak `W/` ETag, and full-stream consumption at 8 KiB across the Spring 7 signature change.  
- ✅ **Business logic unchanged** — no behavioural edits to the download, caching, ETag or throttling paths.  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
### 📐 OpenRewrite's own measured figure  
  
- 🤖 Summed `estimatedTimeSaving` from the data tables, de-duplicated by `(file, recipe)` to avoid double-counting nested parent recipes:  
  - JDK campaign: **1,500 s ≈ 0.4 h**  
  - Spring campaign: **2,220 s ≈ 0.6 h**  
  - **Total credited by the tool: ~3,720 s ≈ 1.0 engineer-hour**  
- 📌 This figure counts only the *mechanical edits*. It materially understates reality, because it prices none of the diagnosis, dependency reconciliation or verification work.  
  
### 🧮 Realistic manual-effort comparison  
  
| Work item | Manual estimate | Actual |  
|---|---|---|  
| 🍃 Spring Boot 1.2 → 4.0 (3 majors, incl. Jakarta + starter renames) | 2–4 days | automated |  
| ☕ Java 8 → 25 (4 LTS hops, plugin/compiler modernization) | 0.5–1 day | automated |  
| 🔴 Diagnose + fix the Spring 7 ETag hook change | 1–2 h | ~minutes |  
| 📦 Groovy 2.4 → 5 / Spock 1 → 2.5 reconciliation | 0.5–1 day | ~minutes |  
| 🧪 Discover + fix the never-running test suite | 2–4 h (easily missed entirely) | ~minutes |  
| 📝 Diagnose the silently-dropped Logback appender | 1–3 h (easily missed entirely) | ~minutes |  
| ✅ Verification (build, boot, endpoint, dependency audit) | 2–4 h | ~minutes |  
| **Total** | **≈ 4–7 engineer-days (32–56 h)** | **~13 min wall clock** |  
  
- 💰 **Estimated saving: ~4–7 engineer-days (≈ 85–95% reduction)** for this module.  
- 🎖️ The disproportionate value sat in the **two silent defects**. Neither produces a compiler error, so a conventional "make it compile" migration would have shipped a service with no logging and a test suite that asserts nothing — a far costlier outcome than the hours saved on mechanical edits.  
- ⚖️ *Caveat:* this is a single small module (8 main + 3 test sources). Per-module savings will not scale linearly to a large estate, and the warm local `~/.m2` cache flatters the wall-clock figure.  
  
---  
  
## 6. 🚀 Next Steps  
  
### 🔍 Validation still outstanding  
  
- 📦 **Fat-jar packaging is the one genuinely unverified area.** `spring-boot-maven-plugin` is **not declared**, so `install` produces a plain, non-executable jar. This matches the original build exactly, so it was deliberately left alone.  
  > ⚠️ **Ordering constraint — do not reverse.** Adding `repackage` would *expose* a latent defect: `FileSystemStorage` resolves its file via `new File(getResource(…).getFile())`, which **cannot work from inside a packaged jar**. Fix the storage implementation **before or together with** enabling fat-jar packaging, never after.  
- 🔁 **Re-run `mvn -B clean install` on JDK 25 in clean CI** to confirm 18/18 without this container's warm `~/.m2`.  
- 🔭 **Confirm trailing-slash behaviour against real client traffic.** Spring Framework 6+ removed trailing-slash matching, so `/download/{uuid}/` no longer matches `/download/{uuid}`. Assessed as non-impacting (no route depends on it), but no test can prove what external callers actually send.  
- 🔐 **Run a CVE scan against the *resolved* coordinates** (not the declared ones) before release. Prior upgrades of this type surfaced HIGH findings in BOM-pinned `jackson-databind` and `spring-core` that were invisible in declared versions.  
  
### 💡 Improvement recommendations (owner decisions — deliberately not actioned)  
  
- 🗂️ **Replace the `FileSystemStorage` stub before production.** Its own javadoc says *"Replace with real implementation"*: it returns `logback.xml` for **any** UUID and breaks inside a jar and on paths containing spaces. A real fix means moving `FilePointer` off `java.io.File` onto a Spring `Resource` / Guava `ByteSource` — an architecture change, not a migration step. ⚠️ Masked in tests because the `test` profile substitutes `FileStorageStub`.  
- 🏷️ **Decide the fate of `Sha512ShallowEtagHeaderFilter`.** Now behaviour-tested, but still a **test-scoped class registered as a filter nowhere**, living in an `org.springframework.*` package purely to reach a protected method. Either promote it to `src/main/java` with a `FilterRegistrationBean`, or delete it.  
- 🧵 **Consider `spring.threads.virtual.enabled=true`** on JDK 25 for this blocking, download-heavy workload. Not enabled: `ThrottlingInputStream` blocks on a Guava `RateLimiter`, and carrier-thread pinning needs a load test first.  
- 🧽 **Prune 8 redundant `org.springframework:*` declarations** — all supplied transitively by `spring-boot-starter-webmvc`, now versionless and BOM-managed, hence harmless. Purely cosmetic. ⚠️ **`spring-context-support` must stay** — it is genuinely not transitive.  
- 🧹 **`commons-codec` is declared but unused** anywhere in `src/` — removal candidate.  
- ⚠️ **Guava deprecation:** `Files.hash(File, HashFunction)` in `FileSystemPointer.java` is deprecated (still functional); modern form is `Files.asByteSource(target).hash(Hashing.sha512())`.  
- 🏷️ **Filename/class mismatch:** `DownloadControllerTest.groovy` declares `class DownloadControllerSpec`. Handled via the surefire include rather than a rename. Cosmetic.  
  
---  
  
## ✅ Final Status  
  
| Metric | Result |  
|---|---|  
| 🔨 Build | **BUILD SUCCESS** (`mvn -B clean install`) |  
| 🐛 Compilation errors | **0** |  
| 🧪 Tests | **18 / 18 passing** (from 0 actually executing) |  
| 🔁 Reproducibility | **3 consecutive clean runs, identical** |  
| 🌐 Runtime | Boots on Java 25; `/actuator/health` → 200 `UP`; download + redirect paths verified |  
| 🧭 Namespace | **100% jakarta**, zero `javax.*` EE references |  
| 📦 Dependency hygiene | No version conflicts; no stale or mixed artifacts |  
| 🍃 Spring upgrade | **Complete** — Boot 4.0.10 / Framework 7.0.11 (not partial) |

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 18 tests run
- ⏱️ Transformation duration: 16 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 6.116109  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
