# 📋 Executive Report — Java 17 Upgrade: `download-server`  
  
**Module:** `com.nurkiewicz.download:download-server:0.0.1-SNAPSHOT`  
**Iteration:** 2 · **Engine:** `OPENREWRITE_WITH_CLAUDE_CLI` · **Date:** 2026-11-27  
**Baseline commit:** `73556be` ("reset pom.xml to original version")  
**Source of record:** `/data/iteration-2/code-upgrade.log` (2,733 lines) + OpenRewrite data tables  
  
---  
  
## 1. 🎯 Executive Summary  
  
- ✅ **Outcome: upgrade complete and green.** The application now builds and tests cleanly on **Java 17** — `mvn clean verify` ends in `BUILD SUCCESS` with **zero compilation errors** and **12/12 tests passing**, reproduced across **4 consecutive clean runs** (log lines 2272, 2318, 2417, 2706).  
- 🤖 **Two-stage pipeline, very uneven split.** The automated OpenRewrite recipe (`com.amazonaws.java.migrate.UpgradeToJava17`) completed in 35.3 s but applied only **5 content edits across 2 files**. The Claude CLI agent stage then performed the substantive remediation — framework bump, dependency reconciliation, the one real compile error, and the test-harness repair.  
- ⚠️ **The critical finding: the recipe left an unrunnable stack behind.** OpenRewrite flipped the build to Java 17 while leaving **Spring Boot 1.2.3.RELEASE / Spring Framework 4.1.6.RELEASE** untouched — a combination that compiles but cannot start on Java 17 (Spring 4.x's bundled CGLIB/ASM cannot read class-file v61). A Java-17 flip alone would have shipped a broken artifact.  
- 🧩 **Spring upgrade was never attempted by the recipe.** Log line 91 is explicit: `Skipping Spring Boot upgrade: not requested (springBootTargetVersion=none)`. Consequently **no `Verified: effective Spring Framework version…` line and no `WARNING: …still < 5.3` line exist in this log** — the toolkit's Spring verification gate never executed. See §2 for how this was resolved and what remains unverified by the toolkit.  
- 🚨 **Second critical finding: the test suite was inert.** The pre-upgrade baseline reported `BUILD SUCCESS` while executing **zero tests** (`test-inventory/before.json` → `{"files":0,"tests":[]}`). The upgrade is now backed by 12 genuinely executing specs.  
- ⏱️ **Net effort:** ~16 minutes of automated agent work (58 turns, $5.22) replacing an estimated **6–9 hours** of manual engineering. Peak memory 1.02 GB of a 4 GB cap, **0 OOM kills**.  
  
---  
  
## 2. 🔧 Application Changes  
  
### 2.1 ⚙️ Automated stage — OpenRewrite (what it *actually* applied)  
  
Per the `[WARNING] Changes have been made to…` block (log lines 74–82) and the `SourcesFileResults` table, only **two files** were modified:  
  
| File | Recipe applied | Effect |  
|---|---|---|  
| 📄 `pom.xml` | `UpdateMavenProjectPropertyJavaVersion` | Added `<java.version>17</java.version>` |  
| 📄 `pom.xml` | `UseMavenCompilerPluginReleaseConfiguration` | `<source>/<target>1.8` → `<release>${java.version}</release>` |  
| 📄 `pom.xml` | `UpgradePluginVersion` | `maven-compiler-plugin` 3.0 → 3.16.0 |  
| 📄 `pom.xml` | `UpgradeDependencyVersion` | `commons-codec` 1.10 → 1.17.2 |  
| ☕ `MainApplication.java` | `PrimitiveWrapperClassConstructorToValueOf` | `new Integer("1234")` → `Integer.valueOf("1234")` |  
  
- 📊 Confirmed scope: **2 files changed, 5 insertions, 5 deletions** (git diff stat captured in log).  
- 🧾 **Data-table caveat (read carefully):** `SourcesFileResults` lists 115 rows over 13 files, but **107 of those rows carry `estimatedTimeSaving = 0`** and come from aggregate/marker recipes (`UpgradeJavaVersion`, `UpgradeToJava8`, `Java8toJava11`, `UpgradeBuildToJava11/17`, `UpgradePluginsForJava11/17`) that stamp a Java-version marker on every parsed source without editing it. Only the **5 non-zero rows above** represent real content changes.  
- 🧾 **`RecipeRunStats` is not usable as evidence:** it reports an identical `sourceFiles=22, sourceFilesChanged=13` for **all 165 recipes** — including Gradle, Docker, Scala, Kotlin, Lombok, JaCoCo and Cobertura recipes that are irrelevant to this module. This is a roll-up attribution artifact, **not** per-recipe proof of change.  
  
### 2.2 🧠 Agent stage — Claude CLI (the substantive remediation)  
  
- 🌱 **Spring Boot `1.2.3.RELEASE` → `2.7.18`** — the javax-terminal Boot line and the only Boot version supported on Java 17 without crossing into Jakarta. This transitively pins **Spring Framework 5.3.31** and **embedded Tomcat 9.0.83**.  
- 🛡️ **Namespace guardrail honoured.** `javax.*` preserved end-to-end; **no** `javax→jakarta` migration and **no** Spring Boot 3 / Jakarta recipe was run. Verified: zero `jakarta.*` imports in sources, Tomcat 9 (`javax.servlet`) on the classpath.  
- 🧪 **Test stack moved onto Java 17** — Groovy `2.4.3` → `3.0.19`, Spock `1.0-groovy-2.4` → `2.3-groovy-3.0` (`spock-core` + `spock-spring`). Neither old version can run on Java 17 at all.  
- 🔌 **Dormant test suite activated** — added `gmavenplus-plugin` (`addTestSources` + `compileTests`) so `src/test/groovy` is compiled, plus a surefire `**/*Spec.java` include (the spec class is named `DownloadControllerSpec` and matched none of surefire's default patterns).  
- 🧹 **Dependency hygiene** — removed the unused, Java-17-hostile `cglib:cglib-nodep:3.1`; removed a duplicate `spring-test` declaration flagged by Maven; removed an obsolete `spring-test` exclusion and the dead `spring.version=4.1.1.RELEASE` property; dropped the plain-HTTP `repo.spring.io/milestone` repository (blocked by Maven 3.9's `maven-default-http-blocker`, and it had already failed to resolve during the recipe run — log line 53).  
- 📦 **Library bumps** — Guava `18.0` → `33.4.0-jre`, commons-io `2.4` → `2.16.1`. All Spring artifacts de-versioned so the Boot BOM governs them.  
  
### 2.3 ✅ Spring upgrade status — plainly stated  
  
- ❌ **By the OpenRewrite recipe: NOT performed.** The step was skipped by configuration (`springBootTargetVersion=none`); the recipe left Spring at 1.2.3 / 4.1.6.  
- ✅ **By the agent stage: performed and effective.** `mvn dependency:tree` shows **Spring Framework 5.3.31 on every Spring artifact** (core, beans, context, context-support, web, webmvc, aop, expression, test) with **no stale or mixed versions** and no `4.1.6.RELEASE` remnants.  
- ⚠️ **Verification nuance for reviewers:** because the Spring step never ran, the toolkit's own static gate (`Verified: effective Spring Framework version…`) produced **no output in this log**. The ≥5.3 outcome is evidenced by the agent's `dependency:tree` plus a green `clean verify`, **not** by the pipeline's built-in Spring assertion. If your sign-off process depends on that gate line, re-run the iteration with `springBootTargetVersion=2.7` to obtain it.  
  
---  
  
## 3. 🛠️ Tools Used  
  
### ☕ Java SDK & build tooling  
- 🔹 **Build JDK: Amazon Corretto 17.0.21.1** (`/usr/lib/jvm/java-17-amazon-corretto.aarch64`) — the Maven JVM and the upgrade target.  
- 🔹 **Container default JDK: Amazon Corretto 25.0.5.1 LTS** — present on `PATH` but *not* the compile target; `<release>17</release>` governs output. (This mattered in practice: a scratch diagnostic compiled at default JDK 25 emitted class-file major 69 and broke Spring 5.3's ASM reader until recompiled with `--release 17`.)  
- 🔹 **Apache Maven 3.9.16** — on `linux/aarch64`, 16 CPUs, ext2/ext3 workspace.  
- 🔹 **Compiler plugin 3.16.0** with `<release>17</release>`; **surefire 2.22.2** (Boot-managed); **gmavenplus-plugin 3.0.2** for Groovy/Spock compilation.  
  
### 🔁 OpenRewrite  
- 🔹 **`rewrite-maven-plugin` 6.42.0**, goal `run`.  
- 🔹 Active recipe: **`com.amazonaws.java.migrate.UpgradeToJava17`** (AWS Transform migration catalogue), composing `Java8toJava11` → `UpgradeBuildToJava17` → `UpgradePluginsForJava17` → `staticanalysis` + `java.dependencies` leaves.  
- 🔹 Runtime: 1 cycle, **35.3 s**, `BUILD SUCCESS`; data tables emitted to `open-rewrite-upgrade-java-17-stats/`.  
- 🔹 Spring Boot recipe step: **skipped** (`springBootTargetVersion=none`).  
  
### 🤖 Claude CLI  
- 🔹 **Claude Code CLI v2.1.332**.  
- 🔹 **Model: Claude Opus 5** — `global.anthropic.claude-opus-5` (canonical `claude-opus-5`, provider Bedrock, 1,000,000-token context, 64,000 max output).  
- 🔹 Session: **58 turns**, 934 s wall-clock (~15.6 min), `stop_reason: end_turn`, `is_error: false`, 0 permission denials, 0 subagents.  
- 🔹 Token usage: 59,480 output (39,671 thinking), 5,319,848 cache-read, 172,024 cache-creation → **$5.22** at list rates.  
- 🔹 Knowledge base: `/app/knowledge` (38 migration docs) mounted read-only; docs 00 and 25 drove the javax-vs-jakarta target decision.  
  
---  
  
## 4. 💻 Code Changes  
  
### 📄 `pom.xml` — the bulk of the change  
- 🔸 Parent `spring-boot-starter-parent` **1.2.3.RELEASE → 2.7.18**.  
- 🔸 Removed hard-coded `4.1.6.RELEASE` / `1.2.3.RELEASE` versions from all 12 Spring/Boot dependencies → BOM-managed.  
- 🔸 Removed: `cglib-nodep:3.1`, duplicate `spring-test`, `spring-test` exclusion, `spring.version` property, `<repositories>` HTTP block.  
- 🔸 Added: `gmavenplus-plugin` executions, surefire `<includes>` (`*Test`, `*Tests`, `*Spec`), `spock.version=2.3-groovy-3.0` property.  
- 🔸 Bumped: guava `18.0 → 33.4.0-jre`, commons-io `2.4 → 2.16.1`, groovy `2.4.3 → 3.0.19` (test scope), spock `1.0 → 2.3-groovy-3.0`.  
  
### ☕ `MainApplication.java`  
- 🔸 `new Integer("1234")` → `Integer.valueOf("1234")` *(the only source edit made by OpenRewrite)*.  
  
### 🧵 `Sha512ShallowEtagHeaderFilter.java` — the one genuine compile error  
- 🔸 **Error:** `method does not override or implement a method from a supertype` (log line 1790, `BUILD FAILURE`).  
- 🔸 **Cause:** Spring 5 changed the extension point from `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean isWeak) throws IOException` (confirmed by `javap` against `spring-web-5.3.31.jar`).  
- 🔸 **Fix:** ported to the new signature, SHA-512 logic preserved via `ByteStreams.toByteArray`; `isWeak` now honoured with a `W/` prefix. Since `writeWeakETag` defaults to `false`, default output is byte-for-byte unchanged.  
  
### 🧪 `FileExamples.java` — test-fixture determinism fix  
- 🔸 **Symptom:** once the specs actually ran, **11 of 12 returned HTTP 404** where 200/304 was expected.  
- 🔸 **Root cause (non-obvious):** the Groovy 3 compiler **constant-folds** the spec's `private static final String TEXT_FILE = '/download/' + FileExamples.TXT_FILE_UUID + '/file.txt'`. It loads `FileExamples` at *compile* time, reads the static field, and emits the result as a `ConstantValue` attribute — the compiled spec has **no `<clinit>` at all**. With `UUID.randomUUID()`, the UUID baked into the class file at build time could never equal the fresh UUID generated at run time, so `uuid.equals(TXT_FILE_UUID)` in the storage stub could never succeed.  
- 🔸 **Fix:** `TXT_FILE_UUID` / `NOT_FOUND_UUID` changed from `UUID.randomUUID()` to fixed UUID literals, making compile-time and run-time values agree.  
- 🔸 🛡️ **Test integrity preserved:** no test was deleted, disabled, skipped or weakened; **no assertion was altered**; the `.groovy` spec file itself was **not modified**.  
  
### 📊 Build & test progression (as recorded in the log)  
| Stage | Result | Tests |  
|---|---|---|  
| 🅰️ Pre-upgrade baseline | ✅ `BUILD SUCCESS` | ⚠️ **0 run** (suite never compiled) |  
| 🅱️ After OpenRewrite | ✅ `BUILD SUCCESS` | ⚠️ **0 run** |  
| 🅲 After Boot 2.7 bump | ❌ `BUILD FAILURE` (line 1790) | 1 compile error |  
| 🅳 After filter fix + Spock wiring | ❌ 11 failures (line 1827) | 12 run / 1 pass |  
| 🅴 After fixture fix | ✅ `BUILD SUCCESS` ×4 | ✅ **12/12 pass** |  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
### 🤖 Tool-reported  
- 🔹 **OpenRewrite: 40 minutes** — stated in the log (`Estimate time saved: 40m`) and exactly reconciled against `SourcesFileResults` (2,400 s = 900 + 600 + 300 + 300 + 300).  
- 🔹 **Caveat:** this credit is for 5 mechanical edits. It does **not** cover the framework bump, the Spring 5 API break, or the test-harness repair — the work that actually made the module run on Java 17.  
  
### 🧠 Agent-stage savings (estimated by task complexity)  
| Task | Manual estimate | Why it is slow by hand |  
|---|---|---|  
| 🔍 Diagnose that Boot 1.2.3 can't run on Java 17; choose 2.7 over 3.x | 1.0–1.5 h | Requires the javax-vs-jakarta compatibility decision |  
| 🌱 Boot 1.2.3 → 2.7.18 + full dependency reconciliation | 1.5–2.5 h | 12 Spring artifacts, BOM adoption, 4 removals |  
| 🧵 Spring 5 `ShallowEtagHeaderFilter` API break | 0.5–1.0 h | Undocumented signature change; needs `javap` spelunking |  
| 🔌 Discover the suite ran 0 tests; wire Groovy/Spock onto Java 17 | 1.0–1.5 h | Silent failure — nothing in the build flags it |  
| 🧪 Root-cause the 11× 404 (Groovy constant-folding) | 2.0–3.0 h | Required bytecode disassembly + 5 scratch harnesses to isolate |  
| ✅ Verification, reproducibility, documentation | 0.5 h | — |  
| **Total** | **~6.5–10 h** | — |  
  
### 📈 Net result  
- 🔹 **Automated elapsed time: ~16.2 minutes** (35.3 s OpenRewrite + 934 s agent) at a compute cost of **$5.22**.  
- 🔹 **Estimated manual effort displaced: 7–10.5 hours** (40 min mechanical + ~6.5–10 h diagnostic/remediation).  
- 🔹 **Effective leverage: ≈ 25–40×** on wall-clock time.  
- 🔹 💡 **Highest-value contribution is non-mechanical:** the Groovy constant-folding defect and the inert test suite are failure modes a time-boxed manual upgrade would very likely have shipped — the first as a mysterious 404, the second as false confidence from a green build that tested nothing.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation before merge  
- 🔸 **Start the application.** Verification to date is MockMvc-level only; the app was never booted (per policy). Run it on Corretto 17 and confirm context startup, then exercise `GET`/`HEAD /download/{uuid}/{filename}` plus `/actuator` end-to-end.  
- 🔸 **Re-run the iteration with `springBootTargetVersion=2.7`** if your sign-off requires the toolkit's own `Verified: effective Spring Framework version…` gate line — it is absent from this log because the step was skipped.  
- 🔸 **Review the two test-tree edits explicitly** (`FileExamples.java`, `Sha512ShallowEtagHeaderFilter.java`) — both are legitimate upgrade consequences, but they are the only places test-side code changed.  
- 🔸 **Smoke-test caching behaviour under a real container.** `ETag` / `If-None-Match` / `If-Modified-Since` / `304` handling now partly relies on Spring 5.3's built-in `ResponseEntity` conditional-request support rather than the application's own check.  
- 🔸 **Confirm `Content-Length` and throttled streaming** (`ThrottlingInputStream`, Guava `RateLimiter`) behave correctly on Tomcat 9 under load — Guava jumped 15 major versions.  
  
### 🧭 Recommended improvements  
- 🔸 ⚠️ **Latent precision bug — track it.** `FilePointer.modifiedAfter` compares `Instant`s at **millisecond** precision while HTTP dates carry only **seconds**. Spring MVC's own conditional-request check currently masks this. If that `ResponseEntity`-based caching is ever replaced with manual handling, truncate to seconds (`getLastModified().truncatedTo(ChronoUnit.SECONDS)`) or 304 responses will silently regress to 200.  
- 🔸 🧹 **Clear the last build warning:** `FileSystemPointer:22` uses the deprecated Guava `Files.hash(File, HashFunction)`. Replacement is `Files.asByteSource(target).hash(Hashing.sha512())`; the existing ETag assertions will catch any behavioural drift.  
- 🔸 📦 **Drop dead dependencies:** `spring-context-support` and `commons-codec` are declared but referenced by no source file (`commons-codec` was bumped by OpenRewrite despite being unused).  
- 🔸 🏗️ **Add `spring-boot-maven-plugin`** if an executable fat jar is wanted — `mvn package` still produces a plain jar, exactly as pre-upgrade.  
- 🔸 📝 **Tighten logging for production:** `logback.xml` still sets `root level="DEBUG"` with `com.nurkiewicz` at `ALL`.  
- 🔸 🧪 **Guard against regression to zero tests.** Add a surefire minimum-test-count check (or `failIfNoTests`) so an inert suite can never again report `BUILD SUCCESS`.  
- 🔸 🔭 **Plan the Jakarta hop separately.** Java 21/25 is unreachable on this stack: Boot 2.7 is supported only through Java 19 and Groovy < 4.0 is not Java 21-compatible. Moving further means Spring Boot 3.x + `javax→jakarta` (KB docs 06/07/19) and a Groovy 4 / Spock 2.4 test migration — a distinct, larger project.  
- 🔸 🔁 **Feed back to the pipeline:** a Java-17 flip on a Spring Boot 1.x/2.x module should default `springBootTargetVersion` to `2.7` rather than `none`. As configured, the deterministic leg produced a build that compiles on Java 17 but cannot start — the exact failure mode the atomic-rule guidance in KB doc 25 is meant to prevent.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 20 minute(s)
- ⏳ Estimated time saved: 10 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 20.583602  
