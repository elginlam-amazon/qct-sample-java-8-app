# 📊 Executive Report — `download-server` Java 8 → Java 25 / Spring Boot 4.0 Upgrade  
  
**Iteration:** 63 · **Base commit:** `73556be` · **Engine:** `OPENREWRITE_WITH_CLAUDE_CLI` · **Date:** 2026-11-20  
  
---  
  
## 1. 🧭 Executive Summary  
  
- 🎯 `download-server` is a single-module Maven Spring Boot service that streams files from  
  `/download/{uuid}` and `/download/{uuid}/{filename}` with ETag / `If-Modified-Since` caching and  
  Guava-throttled streaming at 1 MB/s. It started on **Java 8 source level and Spring Boot 1.2.3.RELEASE**.  
- 🤖 Two OpenRewrite recipe aggregates ran first, both `BUILD SUCCESS`:  
  `com.amazonaws.java.migrate.UpgradeToJava25` (34.5 s) and  
  `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (1 min 07 s). Between them they  
  **actually modified 3 files**: `pom.xml`, `MainApplication.java`, `DownloadController.java`.  
- 🛠️ A Claude CLI build-fix leg then closed the gaps the recipes left. The recipes moved the build  
  *coordinates* but left the project **non-compiling** (one hard error) and carrying a test stack that  
  cannot run on JDK 25 at all.  
- ✅ **Final outcome: `mvn -B clean install` on JDK 25 is green — zero compilation errors, zero compiler  
  warnings, 16/16 tests passing.**  
- 🌱 **The Spring upgrade did reach its target**, verified from the resolved model, not from intent lines:  
  parent **`spring-boot-starter-parent` 4.0.10**, every `org.springframework:spring-*` artifact at  
  **7.0.11**, Tomcat 11.0.27, Jakarta EE 11.  
- ⚠️ **Accuracy note on the Spring verification:** this log contains **no**  
  `Verified: effective Spring Framework version…` line and **no** `WARNING: …still < 5.3` line. Those  
  checks belong to the non-Boot / javax Spring 5.3 path, which was not used here. The Spring outcome  
  below is therefore evidenced by the applied recipe parameters in the OpenRewrite data tables  
  (`UpgradeParentVersion {newVersion=4.0.x}`, `ChangeDependency {…starter-web → …starter-webmvc, 4.0.x}`)  
  plus independent `help:effective-pom` and `dependency:tree` runs. **No "still below target" warning was  
  suppressed or reinterpreted — none exists.**  
- 🧪 **Biggest single finding: the test suite was dormant before this upgrade.**  
  `test-inventory/before.json` records `{"files":0,"tests":[]}` and the baseline build log shows  
  `surefire:2.17:test` running with no `Tests run:` line. The Spock spec had never been compiled.  
  It now compiles and runs: **16 tests, 0 failures.**  
  
---  
  
## 2. 🏗️ Application Changes  
  
| Area | Before | After | Who changed it |  
| --- | --- | --- | --- |  
| ☕ Java level | `source/target 1.8`, compiler plugin 3.0 | `<release>25</release>`, compiler plugin 3.16.0 | 🤖 OpenRewrite |  
| 🌱 Spring Boot | 1.2.3.RELEASE | **4.0.10** (15 staged `UpgradeParentVersion` hops) | 🤖 OpenRewrite |  
| 🌱 Spring Framework | 4.1.6.RELEASE, pinned by hand | **7.0.11**, BOM-managed | 🤖 OpenRewrite + 🛠️ Claude (pin removal) |  
| 🧩 Web starter | `spring-boot-starter-web` | `spring-boot-starter-webmvc` (Boot 4 rename) | 🤖 OpenRewrite |  
| 📜 Servlet API | none declared | `jakarta.servlet-api` 6.1.0 (test, BOM) | 🤖 OpenRewrite |  
| 🐱 Tomcat | Boot 1.2 default | 11.0.27 (embedded) | 🤖 via BOM |  
| 🔣 Jackson | Boot 1.2 default | `tools.jackson` 3.1.8 | 🤖 via BOM |  
| 🪵 Logback / SLF4J | Boot 1.2 default | 1.5.38 / 2.0.20 | 🤖 via BOM |  
| 🧪 Groovy | `org.codehaus.groovy:groovy-all` 2.4.3 → **pinned 2.4.16** by the recipe | `org.apache.groovy:groovy` **5.0.8**, BOM-managed, test scope | 🛠️ Claude |  
| 🧪 Spock | 1.0-groovy-2.4 | **2.5-groovy-5.0** | 🛠️ Claude |  
| 🧪 Test execution | **0 tests ran** (no Groovy compiler plugin) | **16 tests run** (13 Spock + 3 JUnit 5) | 🛠️ Claude |  
| 📚 Guava / commons-io | 18.0 / 2.4 | 33.8.0-jre / 2.22.0 | 🤖 + 🛠️ |  
| 🧹 cglib-nodep 3.1 | compile scope, unused | removed | 🛠️ Claude |  
  
- 🚫 **No Jakarta source migration was needed.** The codebase has **zero** `javax.*` and zero `jakarta.*`  
  imports — it only touches `java.*`, Spring, Guava, SLF4J and commons-io. The jakarta move was entirely  
  a dependency-coordinate change.  
- 🧠 **Business logic is unchanged.** All eight main classes keep their behaviour; the only main-source  
  edits were a deprecated-constructor swap, a redundant annotation removal, a deprecated-Guava-call swap  
  and the deletion of an unused local.  
  
---  
  
## 3. 🛠️ Tools Used  
  
- ☕ **Java SDK — Amazon Corretto 25.0.5.1** (`25.0.5.1+2-LTS`, aarch64) as both the compile and test JVM.  
- 🔨 **Apache Maven 3.9.16**; validation command `mvn -B clean install`.  
- 🤖 **OpenRewrite `rewrite-maven-plugin` 6.42.0**, two aggregate recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25` → `OfficialUpgradeToJava25WithoutIfElseIfToSwitch` →  
    `UpgradeToJava17` → `Java8toJava11`, plus `UpgradeBuildToJava{11,17,21,25}`,  
    `UpgradePluginsForJava{11,17,21,25}`. Self-reported **estimated time saved: 1 h**.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` → `UpgradeSpringBoot_3_5` → … →  
    `UpgradeSpringBoot_2_0`, including `UpgradeSpringFramework_5_0…6_0`, `JakartaEE10` /  
    `JavaxMigrationToJakarta` / `JavaxServletToJakartaServlet`, `SpringBoot2JUnit4to5Migration`,  
    `UpgradeApacheHttpClient_5`. Self-reported **estimated time saved: 2 h 32 m**.  
  - 📉 Recipe leaf-level effects confirmed from `org.openrewrite.table.SourcesFileResults.csv`  
    (508 rows, Spring run) and `RecipeRunStats.csv` in both `*-stats` folders.  
- 🧠 **Claude CLI 2.1.266 (Claude Code)**, model **`global.anthropic.claude-opus-5` (Claude Opus 5)**, on  
  Bedrock — ran the automated build-fix and reporting leg across **3 recorded invocations: 59 turns,  
  ~11.6 min wall, $5.06**.  
- 🔍 Supporting verification: `mvn help:effective-pom`, `mvn dependency:tree`, `javap` against  
  `spring-web-7.0.11.jar` to read the changed filter signature, and OS `sha512sum` as an independent  
  hashing oracle for the new ETag test.  
- 📚 **Migration knowledge base** consulted: `README.md` (index) and `19-spring-boot-latest-migration.md`  
  (Section B — Boot 4.0 starter renames, Framework 7 follow-ups).  
- 🚫 **CodeKB was not available** in this iteration (no `mcp__codekb__*` tools exposed), so the old code  
  was mapped by direct reads instead.  
  
---  
  
## 4. 💻 Code Changes  
  
### 🤖 Applied by OpenRewrite (3 files — kept as-is)  
  
- 📄 **`pom.xml`** — leaf recipes that actually fired:  
  - `UseMavenCompilerPluginReleaseConfiguration` (`source/target 1.8` → `<release>${java.version}</release>`),  
    `UpdateMavenProjectPropertyJavaVersion` (`java.version=25`), `UpgradePluginVersion` (compiler 3.0 → 3.16.0).  
  - `AddMockitoJavaAgentToMavenSurefirePlugin` — added the empty `<argLine>` property, the  
    `maven-dependency-plugin:properties` goal and the surefire  
    `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` (JDK 25 agent requirement).  
  - `UpgradeParentVersion` ×15 — the staged 1.2.3 → 4.0.x parent walk.  
  - `ChangeDependency` — `spring-boot-starter-web` → `spring-boot-starter-webmvc`.  
  - `AddDependency` (via `JavaxServletToJakartaServlet`) — test-scoped `jakarta.servlet:jakarta.servlet-api`.  
  - `RemoveDuplicateDependencies` — removed the duplicate `spring-test` 4.1.6.RELEASE declaration that the  
    baseline build had been warning about (`must be unique … @ line 144`).  
  - `ExcludeDependency` / `RemoveExclusion` ×2 each (JUnit 4 → 5 migration), `UpgradeDependencyVersion` ×7.  
- ☕ **`MainApplication.java`** — `PrimitiveWrapperClassConstructorToValueOf`:  
  `new Integer("1234")` → `Integer.valueOf("1234")`.  
- 🌱 **`DownloadController.java`** — `NoAutowiredOnConstructor`: dropped the redundant `@Autowired` on the  
  single constructor.  
- ℹ️ The other source files appear in `SourcesFileResults.csv` only via `UpgradeJavaVersion` markers and a  
  classpath-level `RemoveDependency`, **not** as textual edits — the final recipe diff is 3 files.  
  
### 🛠️ Applied by Claude (the recipes' gaps)  
  
- 🔴 **Hard compile error fixed — `Sha512ShallowEtagHeaderFilter`.** Spring Framework 7's hook is  
  `generateETagHeaderValue(InputStream, boolean)`, not `generateETagHeaderValue(byte[])`  
  (confirmed with `javap` on `spring-web-7.0.11.jar`). Moved the override, kept SHA-512 strong-ETag  
  semantics, honour the framework's `isWeak` flag with a `W/` prefix. **The recipes did not touch this.**  
- 🧪 **Test stack rescued from the Groovy 2.4 line.** The Spring recipe *pinned* `groovy-all` to **2.4.16**  
  in `dependencyManagement` — still Groovy 2.4, which cannot run on JDK 25. Replaced with BOM-managed  
  `org.apache.groovy:groovy` 5.0.8 (test scope) and Spock **2.5-groovy-5.0**; deleted the dead  
  `dependencyManagement` block and the unused compile-scoped `cglib-nodep` 3.1.  
- 🧪 **Dormant suite activated.** Added `gmavenplus-plugin` 5.1.0 (`addTestSources`, `compileTests`) and a  
  surefire `**/*Spec.java` include — the class inside `DownloadControllerTest.groovy` is named  
  `DownloadControllerSpec`, which the default includes miss. The 12 existing features run **unchanged**:  
  no test, feature or assertion was deleted, disabled or weakened.  
- ➕ **Coverage added** (2 new items, 16 tests total):  
  - `should redirect to original file name` — asserts `301` + `Location: /download/{uuid}/download.txt`,  
    the one controller method with no test.  
  - `Sha512ShallowEtagHeaderFilterTest` (3 JUnit 5 tests) — strong ETag matches the OS `sha512sum`  
    oracle `0a50261e…bf2425`, weak variant carries `W/`, and the value agrees with the ETag  
    `FileSystemPointer` computes from the file on disk. That cross-check is what de-risks the  
    `byte[]` → `InputStream` signature change.  
- 🧹 **Stale build config removed:** the orphan `<spring.version>4.1.1.RELEASE</spring.version>` property,  
  the plain-HTTP `repo.spring.io/milestone` repository (Maven 3.9 blocks it; the log shows  
  `Failed to access maven repository http://repo.spring.io/milestone/` on **both** recipe runs), and the  
  eight redundant direct `spring-*` declarations plus the `spring-test` exclusion — all now resolve  
  transitively at 7.0.11.  
- 📚 **Deprecated API swapped:** Guava's deprecated `Files.hash(File, HashFunction)` →  
  `Files.asByteSource(target).hash(...)` in `FileSystemPointer` (identical digest; clears the last  
  compiler warning).  
- 🪵 **`logback.xml` repaired:** the appender was nested inside `<root>` with no `name`, so Logback 1.5  
  logged `Missing attribute [name]` and `Ignoring unknown property [encoder]` on every run and the  
  configured pattern never applied. Now a named `CONSOLE` appender referenced by `<root>`.  
- 🧰 **Environment blocker cleared:** a stale root-owned `target/` made `resources:resources` fail with  
  `FileSystemException: … Operation not permitted` before any compilation could happen.  
- 🧹 Removed the unused `Integer temp` local from `MainApplication.main`.  
  
### 🧾 Resolved-stack verification (final)  
  
- ✅ Boot **4.0.10** · Framework **7.0.11** · Tomcat **11.0.27** · Jackson **3.1.8** (`tools.jackson`) ·  
  Logback **1.5.38** · Micrometer **1.16.9** · JUnit Jupiter **6.0.3** · Byte Buddy **1.17.8** ·  
  Groovy **5.0.8** · Spock **2.5-groovy-5.0** · Guava **33.8.0-jre** · commons-io **2.22.0** ·  
  commons-codec **1.19.0** (BOM) · `jakarta.servlet-api` **6.1.0**.  
- ✅ `dependency:tree` carries no `javax` artifact, no `org.codehaus.groovy`, no `cglib`, no  
  `spring-boot-starter-web`; `grep -rn "javax\.\|jakarta\." src/` returns nothing.  
- ℹ️ The log's line-254 note about a *"mixed Boot/non-Boot reactor"* was a **false positive**: the plain  
  `org.springframework:spring-*` artifacts it detected were direct declarations inside this Boot project,  
  not separate non-Boot modules. They have since been removed.  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
| Activity | Manual estimate | What actually happened |  
| --- | --- | --- |  
| 🔍 Analysis, version research, upgrade plan | 3–4 h | 🤖 + 🛠️ automated |  
| ☕ Java 8 → 25 build/plugin/API work | 2–3 h | 🤖 OpenRewrite (self-estimate 1 h) |  
| 🌱 Boot 1.2 → 3.5 → 4.0, 15 parent hops, starter rename | 8–12 h | 🤖 OpenRewrite (self-estimate 2 h 32 m) |  
| 🔴 Framework 7 filter API break (diagnose + fix + test) | 1–2 h | 🛠️ Claude |  
| 🧪 Groovy 2.4 → 5, Spock 1.0 → 2.5, reviving a dormant suite | 4–6 h | 🛠️ Claude |  
| 🧹 Stale pins, HTTP repo, Logback, deprecated Guava, dep pruning | 2–3 h | 🛠️ Claude |  
| 🧾 Verification + executive reporting | 1–2 h | 🛠️ Claude |  
| **Total** | **≈ 21–32 h (3–4 developer days)** | **≈ 13–14 min of machine time** |  
  
- 🤖 **OpenRewrite self-reported saving: 3 h 32 m** (1 h + 2 h 32 m), from the two `Estimate time saved` lines.  
- 🛠️ **Claude leg: 59 turns across 3 invocations, ~11.6 min wall, $5.06** (Opus 5, list-price basis).  
- ⚙️ **Recipe wall time: 1 min 42 s total** (34.5 s + 1 min 07 s).  
- 💰 **Estimated net saving ≈ 20–31 engineer hours (~95 % of the elapsed effort).**  
- 📌 Figures are estimates for a ~600 LOC service that crossed **17 Java versions and 3 Spring Boot majors**.  
  The manual baseline assumes an engineer fluent in Spring but new to this codebase. The honest caveat:  
  roughly **two-thirds of the residual risk sat in the ~40 % of work the recipes could not do** — the  
  Framework 7 API break and the Groovy/Spock stack — which is where a human would also have spent the time.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation  
  
- 🔁 **Re-run `mvn -B clean install` on JDK 25 in CI** and confirm **16/16** (13 Spock + 3 JUnit 5).  
  Nothing in the build depends on local state any more, but the stale root-owned `target/` that blocked  
  the first attempt is worth a `clean` guard in the pipeline.  
- 🌐 **Smoke-test the packaged jar** — not done here, because this cycle's instructions forbid executing  
  the application. Check `/download/{uuid}` (301), `/download/{uuid}/{filename}` (200), both 304 paths  
  (`If-None-Match`, `If-Modified-Since`), `HEAD`, and `/actuator/health`. Routing, the exact `Location`  
  value and the ETag/304 behaviour are now covered by MockMvc, so a smoke test is really about  
  **servlet-container and packaging** behaviour.  
- ⚠️ **The open question carried over from iteration 62 — whether the app was ever actually booted on  
  Boot 4 — remains open.** No runtime boot happened in iteration 63 either.  
- 🏷️ **ETag behaviour against real clients** is now test-backed rather than compile-verified (3 tests,  
  including an OS-`sha512sum` oracle), but `Sha512ShallowEtagHeaderFilter` is still a **test-only** class  
  that is not registered as a filter anywhere. Decide whether it is meant to be wired in or deleted.  
- 🧾 **Version choices for owner review** — this tree has no deviation table because nothing deviates from  
  a mandated version. The only non-BOM pins are: Spock `2.5-groovy-5.0`, gmavenplus `5.1.0`,  
  Guava `33.8.0-jre`, commons-io `2.22.0`, plus the recipe's compiler-plugin `3.16.0`.  
  
### 💡 Improvements  
  
- 🗂️ **Replace the `FileSystemStorage` stub before production.** It returns `logback.xml` for *any* UUID and  
  uses `new File(getResource(...).getFile())`, which fails when the app runs from a packaged jar. Fixing the  
  jar case means moving `FilePointer` off `java.io.File` onto a `Resource`/`ByteSource` — an architecture  
  change to a class whose own comment says *"Replace with real implementation"*, so it needs the real  
  storage backend chosen first. **Owner decision, not an upgrade step.**  
- 🧹 **`commons-codec` is compile-scoped and unused** (hashing goes through Guava) — the next obvious  
  pruning candidate. The test-scoped `jakarta.servlet-api` is kept deliberately so MockMvc does not lean on  
  `tomcat-embed-core` for the servlet API.  
- 🔭 **Behavioural items no build can prove:** trailing-slash matching is no longer automatic (Framework 6+),  
  and Boot 4 moves to Jackson 3 (`tools.jackson`). Neither affects this codebase — no direct Jackson usage,  
  no trailing-slash routes — but confirm with real clients.  
- 🔐 **Actuator exposure is already at Boot 4's conservative default** (`health` only; the project has no  
  `application.properties`). Add `management.endpoints.web.exposure.include` **and** Spring Security  
  together, only if more endpoints are genuinely wanted.  
- 🔼 **Before any move to Boot 4.1**, run the 4.1 deprecation gate and apply its property/logging renames.  
- 🧰 **SpotBugs and Checkstyle are still unconfigured** (the `-Dspotbugs.skip` / `-Dcheckstyle.skip` flags  
  used in earlier iterations were no-ops). Worth adding as a **separate** change with its own baseline — a  
  first run on a codebase that just jumped 17 Java versions would bury the upgrade diff in unrelated findings.  
- 🧪 **Raise the safety net further:** `ThrottlingInputStream` (rate limiting) and `FileSystemPointer`  
  (hashing, media-type probing) still have no direct unit tests; they are only exercised through MockMvc.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 16 tests run
- ⏱️ Transformation duration: 18 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 9.190632  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
