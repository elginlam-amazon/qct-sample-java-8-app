# 📊 Java Version Upgrade — Executive Report  
  
**Project:** `com.nurkiewicz.download:download-server` (Spring MVC file-download service)  
**Iteration:** 40 · **Date:** 2026-11-13 · **Engine:** `OPENREWRITE_WITH_CLAUDE_CLI`  
**Source of truth for this report:** `/data/iteration-40/code-upgrade.log` (3,445 lines) + direct verification of the working tree  
  
---  
  
## 1. 🎯 Executive Summary  
  
This iteration migrated a legacy Spring Boot 1.2 / Java 8 file-download service to **Java 25** and **Spring Boot 4.0.9** in a two-stage automated pipeline, followed by agent-driven remediation of everything the recipes could not finish. Both OpenRewrite legs completed with `BUILD SUCCESS` and the log records concrete, verified file changes rather than intent only: the Java 25 recipe (`com.amazonaws.java.migrate.UpgradeToJava25`) rewrote `pom.xml` and `MainApplication.java`, and the Spring recipe (`com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`) rewrote `pom.xml` and `DownloadController.java`. The automated stages left **one compilation error** and a cluster of dependency-resolution problems (Groovy 2.4, Spock 1.0, cglib and ASM 9.7.1 all incompatible with class file major version 69), which were then resolved manually. The project now builds clean — `mvn clean verify` exits 0 with **zero compilation errors and zero compiler warnings**, producing **Java 25 bytecode (major version 69)** verified via `javap`. A latent quality problem was also found and fixed: a 12-spec Spock test suite that had **never been compiled or executed** by this build is now wired in and passing 12/12, which is what gives the migration real behavioural assurance rather than compile-only assurance. Final state: ✅ **upgrade complete and verified**, with the outstanding items being governance (a dependency-version mandate that no longer matches reality) rather than code.  
  
**Overall outcome: ✅ SUCCESS — Java 25 + Spring Boot 4.0.9 / Spring Framework 7.0.10, zero errors, 12/12 tests green, zero known CVEs.**  
  
### ⚠️ Accuracy note on the requested Spring verification lines  
  
- 🔎 The report request asked for the outcome from the `Verified: effective Spring Framework version…` / `WARNING: …still < 5.3` lines. **Neither line exists anywhere in this log** — verified by grep; the only matches were quotations of the request itself inside the transcript.  
- 📌 Those lines belong to the **non-Boot "Spring 5.3 on javax" upgrade path**, which **did not run** this iteration. The Spring leg that actually ran was the Boot 4.0 recipe chain.  
- ✅ The Spring upgrade is therefore reported from what the log *does* contain (recipe tree, changed files, `BUILD SUCCESS`) plus independent verification of the resolved artifact versions.  
- ✅ **The Spring target was reached, so this is NOT reported as an incomplete Spring upgrade.** Resolved Spring Framework = **7.0.10** (via `spring-boot-starter-parent:4.0.9`), confirmed by `mvn dependency:list` and by 12 specs running through a real `DispatcherServlet` — far above the 5.3 threshold the requested warning line would have flagged.  
  
---  
  
## 2. 🔧 Application Changes  
  
### Platform & framework  
- ☕ **Java 8 → Java 25** — `<java.version>25</java.version>`; toolchain Amazon **Corretto 25.0.5+10-LTS** (log line 1401).  
- 🌱 **Spring Boot 1.2.x → 4.0.9** — parent upgraded by `org.openrewrite.maven.UpgradeParentVersion: {artifactId=spring-boot-starter-parent, newVersion=4.0.x}`.  
- 🍃 **Spring Framework → 7.0.10** (transitively, BOM-managed).  
- 🔄 **Starter rename applied by recipe** — `org.openrewrite.java.dependencies.ChangeDependency: {oldArtifactId=spring-boot-starter-web, newArtifactId=spring-boot-starter-webmvc}` (Boot 4.0 split).  
- 📦 **javax EE → Jakarta EE 11** — applied by `org.openrewrite.java.migrate.jakarta.JakartaEE10` → `JavaxServletToJakartaServlet` inside the Boot 3.0 step. Final tree: `jakarta.servlet-api` 6.1.0, `jakarta.annotation-api` 3.0.0, `jakarta.xml.bind-api` 4.0.5.  
- 🐱 **Tomcat 11.0.27** (Servlet 6.1); **Jackson 3.1.7** (`tools.jackson.*`); **Micrometer 1.16.8**; **JUnit Platform 6.0.3**.  
  
### Build & test infrastructure  
- 🧪 **JUnit 4 → JUnit 5** migration applied by recipe (`SpringBoot2JUnit4to5Migration` → `JUnit4to5Migration` → `ExcludeJUnit4UnlessUsingTestcontainers`).  
- 🐘 **Groovy 2.4.16 → Apache Groovy 5.0.8**; coordinate moved `org.codehaus.groovy:groovy-all` → `org.apache.groovy:groovy` (in Groovy 4+ `groovy-all` is a `pom`-type aggregator, not a jar).  
- 🔬 **Spock 1.0-groovy-2.4 → 2.5-groovy-5.0** (JUnit 5 Platform instead of JUnit 4 + cglib).  
- 🛠️ **Groovy test compilation wired in** — added `gmavenplus-plugin:5.1.0` (`compileTests`) and a Surefire `<includes>` list for `*Test` / `*Tests` / `*Spec`.  
- 🧷 **ASM pinned to 9.11.1** via `<dependencyManagement>` — test-scope ASM **9.7.1** (`spring-boot-starter-test` → `json-path` → `json-smart` → `accessors-smart`) predates Java 25 support and rejects major version 69; not BOM-managed, so an explicit pin was required.  
- 🤖 **Mockito Java agent** `argLine` added by recipe (`org.openrewrite.java.migrate.AddMockitoJavaAgentToMavenSurefirePlugin`) — required because JDK 25 restricts dynamic agent attachment.  
- 🧹 **Removed** `cglib:cglib-nodep:3.1` (only existed for Spock 1.0 mocking), the stale `<spring.version>4.1.1.RELEASE</spring.version>` property, the recipe-added `groovy-all:2.4.16` pin, the hard-coded `maven-compiler-plugin` version, and the plain-HTTP `repo.spring.io/milestone` repository (blocked by `maven-default-http-blocker`).  
  
### Security  
- 🔐 **Guava 29.0-jre → 33.8.0-jre** — 33.4.0-jre (the version the knowledge base recommends) carries **GHSA-xxph-c9ww-hj94 / CVE-2026-102554**; 33.8.0 is past the 33.7.2 fix.  
- 🛡️ **Jackson CVE exposure eliminated structurally** — `com.fasterxml.jackson.core:jackson-databind` is no longer on the classpath at all; Boot 4 ships `tools.jackson.core:jackson-databind:3.1.7`.  
- ✅ **OSV.dev scan of all 92 resolved artifacts → 0 known advisories.**  
  
---  
  
## 3. 🧰 Tools Used  
  
| Tool | Version | Role |  
| --- | --- | --- |  
| ☕ **Amazon Corretto JDK** | **25.0.5+10-LTS** (`25.0.5 2026-10-20`) | Target runtime & compiler; emits class file **major version 69** |  
| 🏗️ **Apache Maven** | 3.9.16 | Build orchestration (`clean verify`, `dependency:list`, `help:effective-pom`) |  
| 🔁 **OpenRewrite `rewrite-maven-plugin`** | **6.42.0** | Executed both automated migration legs |  
| 📜 **Recipe — Java** | `com.amazonaws.java.migrate.UpgradeToJava25` → `OfficialUpgradeToJava25WithoutIfElseIfToSwitch` | Java 8→11→17→21→25 ladder, plugin upgrades, static analysis |  
| 📜 **Recipe — Spring** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` | Full Boot 1.x→2.0→…→3.5→4.0 chain incl. `JakartaEE10`, `UpgradeSpringFramework_6_0`, `JUnit4to5Migration`, `UpgradeApacheHttpClient_5` |  
| 🤖 **Claude CLI** | **Claude Opus 5** — model ID `global.anthropic.claude-opus-5` (`canonicalModel: "claude-opus-5"`, provider `bedrock`, 200K context / 64K max output) | Compilation-error remediation, dependency reconciliation, test wiring, CVE triage, verification |  
| 📚 **Java migration knowledge base** | `/app/knowledge` (25 domain docs) | Pattern lookup (KB 11 dependencies, KB 13 logging, KB 19 Spring Boot 3/4, KB 24/25 Java 21/25) |  
| 🔍 **OSV.dev batch API** | — | Vulnerability scan of all 92 resolved artifacts |  
| 🧾 **`javap`** | JDK 25 | Verified emitted bytecode `major version: 69` and the new `ShallowEtagHeaderFilter` signature |  
  
> 💡 Claude CLI cost recorded in the log for the two completed invocations: **$2.40** (21 turns, 5m 35s) and **$0.85** (9 turns, 2m 02s).  
  
---  
  
## 4. 💻 Code Changes  
  
**7 files changed · +83 / −63 lines** (`git diff --stat`)  
  
### 🤖 Applied automatically by OpenRewrite (log-confirmed)  
- 📄 **`pom.xml`** — changed by *both* legs; parent version, starter rename, Java version property, compiler `<release>`, plugin upgrades, Surefire agent config, JUnit 4 exclusions.  
- 📄 **`MainApplication.java`** — `new Integer("1234")` → `Integer.valueOf("1234")` (recipe `org.openrewrite.staticanalysis.PrimitiveWrapperClassConstructorToValueOf`).  
- 📄 **`DownloadController.java`** — redundant `@Autowired` on the single constructor removed, plus its import (recipe `org.openrewrite.java.spring.NoAutowiredOnConstructor`, via `SpringBoot2BestPractices`).  
  
### 🧑‍🔧 Applied manually by Claude CLI (recipe gaps)  
  
| File | Problem | Fix |  
| --- | --- | --- |  
| `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` | ❌ **The only compilation error:** `method does not override or implement a method from a supertype`. `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` was removed in Spring Framework 7 (confirmed with `javap` on `spring-web-7.0.10.jar`). | ✅ Overrode the current hook `generateETagHeaderValue(InputStream, boolean isWeak)`, hashing `inputStream.readAllBytes()` with `Hashing.sha512()` and honouring the new weak-ETag flag (`W/"…"`). SHA-512 semantics preserved. |  
| `src/main/java/com/nurkiewicz/download/ExistingFile.java` | ⚠️ `method == HttpMethod.GET` — `HttpMethod` stopped being an `enum` in Spring Framework 6, so reference equality is no longer contractual. | ✅ `HttpMethod.GET.equals(method)` |  
| `src/main/java/com/nurkiewicz/download/FileSystemPointer.java` | ⚠️ `Files.hash(target, Hashing.sha512())` deprecated since Guava 30. | ✅ `Files.asByteSource(target).hash(Hashing.sha512())` — identical digest, build now warning-free. |  
| `src/main/resources/logback.xml` | 🐛 **Latent runtime bug:** `<appender>` was nested *inside* `<root>` with no `name`. Logback 1.5's stricter Joran reports `ERROR … Missing attribute [name]` and attaches **no appender** — the application logged nothing. | ✅ Moved `ConsoleAppender` to configuration level with `name="CONSOLE"` + `<appender-ref>` (KB 13). Pattern unchanged. Restores `NFR-OBSERVABILITY-9f46ce8c`. |  
| `pom.xml` | 🧪 **Dead test suite:** `DownloadControllerTest.groovy` (`DownloadControllerSpec`, 12 specs) had no Groovy compiler plugin and `*Spec` isn't a default Surefire include → Surefire reported **0 tests**; nothing verified the migration. | ✅ Added `gmavenplus-plugin` + Surefire `<includes>` → **0 → 12 executed tests, 12/12 pass**. |  
| `pom.xml` | 🔗 9 further dependency reconciliations (Groovy, Spock, cglib, Guava, commons-io, ASM pin, compiler-plugin version, stale property, HTTP repo) — see §2. | ✅ All versions BOM-managed except the three the BOM genuinely does not manage (`commons-io`, `spock-*`, `org.ow2.asm:asm`). |  
  
### ✅ Verification performed  
- 🟢 `mvn clean verify` → **exit 0, 0 `[ERROR]`, 0 `[WARNING]`**; 8 main + 3 test Java sources + 1 Groovy source compiled; jar built.  
- 🟢 `mvn -o clean verify` reproduces green **offline** (no hidden network-resolved state).  
- 🟢 `javap -verbose` → `major version: 69`.  
- 🟢 12/12 Spock specs pass through a real `WebApplicationContext` + `DispatcherServlet`, covering `If-Modified-Since` binding, ETag/304, HEAD-without-body, `Content-Length` and content-type negotiation.  
- 🟢 Namespace consistency: **no `javax.*` EE artifacts, no mixed javax/jakarta, zero `javax.*` imports** in any source file.  
- 🟢 No business logic altered — every change is an API-compatibility or build-configuration change.  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
### 📈 OpenRewrite's own attribution (from the log)  
- ⚙️ Java 25 leg — `Estimate time saved: **1h**` (build time: 45.3 s)  
- 🌱 Spring Boot 4.0 leg — `Estimate time saved: **2h 32m**` (build time: 11 m 35 s)  
- 🧮 **Recipe subtotal: 3 h 32 m**, delivered in **~12 minutes of wall clock**  
  
### 🧑‍🔧 Agent-side remediation (work the recipes could not do)  
  
| Work item | Manual estimate | Basis |  
| --- | --- | --- |  
| 🔍 Diagnose + fix the `ShallowEtagHeaderFilter` signature change | 1.5 – 3 h | Undocumented removal; requires decompiling `spring-web-7.0.10` to find the replacement hook and preserving SHA-512 + new weak-ETag semantics |  
| 🔗 Reconcile 9 dependency/plugin issues on Java 25 | 4 – 8 h | Groovy/Spock/cglib all fail on major 69; BOM-vs-explicit-version reasoning; `groovy-all` → `groovy` coordinate change |  
| 🧷 Trace ASM 9.7.1 through 4 levels of transitive test deps and pin it | 1 – 2 h | Only surfaces at runtime, inside an optional code path |  
| 🧪 Discover & wire the dead 12-spec Groovy suite | 1.5 – 3 h | Silent failure — a green build with 0 tests looks identical to a green build with passing tests |  
| 🐛 Find the logback `Missing attribute [name]` appender bug | 0.5 – 1 h | Logged as `ERROR` on stderr, easy to miss; app "works" but emits no logs |  
| 🔐 CVE scan of 92 artifacts + Guava triage/fix | 1.5 – 3 h | Includes catching that the KB's own recommended Guava version ships a CVE |  
| 🔁 Iterative rebuild / verify / offline-reproduce cycles | 2 – 4 h | Multiple full `clean verify` rounds |  
| 📋 Mandate-conflict analysis & carry-over disposition | 1 – 2 h | Established the Jackson 3 coordinate move makes the mandate unsatisfiable by construction |  
| **Agent subtotal** | **13 – 26 h** | |  
  
### 🏁 Bottom line  
- ⏳ **Estimated manual effort:** ~**17 – 30 engineer-hours** (≈ 2 – 4 working days), skewing high because Java-8-era Groovy/Spock test stacks on a current JDK are a notorious time sink.  
- ⚡ **Actual automated effort:** ~**12 min** of OpenRewrite + ~**8 min** of Claude CLI across the recorded invocations (**$3.25** of model spend for the two logged runs).  
- 💰 **Net saving: roughly 16 – 29 hours, i.e. a ~95%+ reduction in elapsed engineering time** for this module — and the pipeline additionally surfaced three defects (dead test suite, broken logging config, Guava CVE) that a hand migration focused only on "make it compile" would very likely have shipped.  
- 📐 *Caveat:* a single-module, 8-class, one-package project. The ratio will compress on larger codebases where cross-module coordination and manual review dominate.  
  
---  
  
## 6. 🔜 Next Steps  
  
### ✅ Validation steps (recommended before production)  
1. 🚀 **Run the application once, manually.** This pipeline is prohibited from launching the built artifact, so confidence rests entirely on MockMvc-level specs. Start it and confirm `Tomcat started on port 8080` + `GET /actuator/health` → 200 on the Boot 4.0.9 tree. *(Iteration 39 did this on its Boot 3.5.16 tree; it has **not** been done on this one.)*  
2. 📥 **Smoke-test the real download path end-to-end** — `GET /download/{uuid}`, the 301 redirect to `/download/{uuid}/{filename}`, a `HEAD` request, an `If-None-Match` 304, and a `Range`-style large-file read to confirm `ThrottlingInputStream` + Guava `RateLimiter` still behave on virtual-thread-capable JDK 25.  
3. 🧾 **Confirm logging actually emits** after the `logback.xml` fix — the previous config attached no appender, so any monitoring built on these logs was silently receiving nothing.  
4. 🔀 **Trailing-slash behaviour changed in Spring Framework 6** (KB 19 §A.4): `/download/{uuid}/` no longer matches `/download/{uuid}`. No test covers this — check client call patterns and add an explicit mapping if needed.  
5. 📊 **Review `spring-boot-starter-actuator` exposure.** It is present but unconfigured; Boot 4 exposes only `/actuator/health` by default, whereas Boot 1.2 exposed many endpoints. If the old surface was relied on, set `management.endpoints.web.exposure.include` **and** add authentication first — there is no `spring-boot-starter-security` in the tree, so anything exposed is unauthenticated.  
6. 💾 **Commit the work.** 7 files are modified in the working tree on branch `main-upgrade` (HEAD `73556be`); no commit or branch was created, as none was requested.  
  
### 🛠️ Improvement recommendations  
7. ⛔ **[ESCALATION — cannot be fixed in-repo] Resolve the dependency-version mandate.** The carried-over mandate names Spring Boot **3.2.12** and `com.fasterxml.jackson.core:jackson-databind` **2.15.2**. Against this tree the Jackson entry is **not a version gap but a coordinate change** — Jackson 3 moved to `tools.jackson.core`, so the mandated artifact does not exist in a Boot 4 build. The mandate is **unsatisfiable by construction**; downgrading would reintroduce the ASM/major-69 failure. `analysis/requirements.json` contains **no** dependency mandate (25 functional + 4 NFR entries only), so the figures originate in the ATX skill outside this repository. **A reviewer must either sign off on Boot 4.0.9 / Jackson 3.1.7 or pin the pipeline to a mandate-compatible target line.**  
8. 🎯 **Pick one target line and hold it across iterations.** Iterations 38/39 drove toward Boot 3.5.x; iteration 40 toward Boot 4.0.9 — each from a reset `pom.xml`. This flip-flop is engine configuration, not code, and it is why the same findings keep reappearing in the carry-over list.  
9. 📚 **Fix the knowledge base:** `11-dependency-migrations.md` recommends Guava **33.4.0**, which ships CVE-2026-102554. Update the recommendation to **≥ 33.7.2**.  
10. ⚠️ **[PROMPT CONFLICT — flagged, not acted on]** A cycle instruction said *"Stay on the `javax.*` namespace."* Not applied, with evidence: (a) `grep -rn "javax\." src/` returns **zero** matches — the codebase never had a Java EE `javax.*` import; (b) the adopted target `spring-boot-starter-parent:4.0.9` is a **jakarta** line, which is exactly when the guardrail says jakarta is correct; (c) honouring it literally would require downgrading to Boot 2.7.x, which the hard rules forbid and which **cannot work on Java 25** (Boot 2.7 is supported only through Java 19; its bundled ASM cannot parse major version 69). A genuine javax target would also require dropping to Java 17 — a pipeline decision, not a repository change.  
11. 🧱 **`FileSystemStorage` is an acknowledged stub** (its own Javadoc says "Replace with real implementation") — it returns `logback.xml` for every UUID. Untouched by this upgrade; needs a real implementation before production use.  
12. 📦 **No `spring-boot-maven-plugin` is declared**, so `mvn package` produces a plain, non-executable jar. Pre-existing (the original pom had none either). Add it if an executable fat jar or `bootRun` is wanted.  
13. 🧵 **Consider virtual threads** (`spring.threads.virtual.enabled=true`, KB 19 §A.2) — attractive on Java 25 for an I/O-bound download service, but `ThrottlingInputStream` blocks on a Guava `RateLimiter`; benchmark before adopting.  
14. 🧪 **Broaden test coverage.** 12 specs is thin for a service this HTTP-semantics-heavy, and until this iteration **none of them ran**. Add a CI assertion that the test count is non-zero so a silently empty suite can never pass again.  
15. 🔐 **Application-level security is out of scope but unaddressed.** `NFR-SECURITY-742b03ce` is satisfied only in the narrow sense that `FileSystemPointer` SHA-512-hashes content for the ETag; there is no TLS configuration and no authentication on the download endpoints.  
16. 🗂️ **Data tables not available for inspection.** The OpenRewrite `SourcesFileResults` tables were written to `target/rewrite/datatables/2026-11-13_03-00-57-642` and `…_03-01-48-937`, but `target/` was removed by subsequent `mvn clean` runs. The equivalent per-file recipe attribution is preserved in the log's `Changes have been made to …` trees (lines 53–98 and 163–239) and is what §4 is based on. **Recommendation: archive `target/rewrite/datatables/` before any `clean`** so the structured CSVs survive for audit.  
17. 🔍 **CodeKB was unavailable** this cycle (no `mcp__codekb__*` tools exposed), so legacy-code mapping was done by direct file reads. Complete for a module this size (8 main classes, one package), but a cycle with CodeKB online could cross-check call flows.  
  
---  
  
*Report generated 2026-11-13 · Claude Opus 5 (`global.anthropic.claude-opus-5`) via Claude CLI · all claims traced to `/data/iteration-40/code-upgrade.log` or to commands re-run against the working tree.*

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 34 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 7.995960  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s

## Requirements register refresh

- Outcome: Refreshed
- Duration: 0.8s

## Feature Parity Verification

- Requirements evaluated: 29
- Satisfied: 29
- Regressions: 0
- Unverified: 0
- Behaviour-judged (changed requirements): 24

### Verdict coverage

Of 29 requirement(s): 0 test-backed, 24 behaviour-judged, 5 unchanged-by-construction, 0 unverified.

> **Test evidence.** No verdict is test-backed: this iteration's parity evidence carries no test results: the transformer that ran it did not report any.

No regressions were detected; every requirement is satisfied.

### Satisfied (29) — reasoning

- **FR-find-file-320ac64d**: WHEN a caller invokes findFile, the FileStorage SHALL return the file for the given uuid.
  - Acceptance criteria:
    - AC1: WHEN findFile completes normally, the FileStorage SHALL return a value of type Optional&lt;FilePointer>.
    - AC2: WHEN findFile is invoked, the FileStorage SHALL accept uuid of type UUID.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-get-etag-64a98481**: WHEN a caller invokes getEtag, the FilePointer SHALL return the etag.
  - Acceptance criteria:
    - AC1: WHEN getEtag completes normally, the FilePointer SHALL return a value of type String.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The BEFORE and AFTER code for getEtag are identical, with no changes to the implementation or interface.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-get-last-modified-18e5dfe8**: WHEN a caller invokes getLastModified, the FilePointer SHALL return the last modified.
  - Acceptance criteria:
    - AC1: WHEN getLastModified completes normally, the FilePointer SHALL return a value of type Instant.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The BEFORE and AFTER code for getLastModified are identical, with no changes to the implementation or interface.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-get-media-type-068ff4c3**: WHEN a caller invokes getMediaType, the FilePointer SHALL return the media type.
  - Acceptance criteria:
    - AC1: WHEN getMediaType completes normally, the FilePointer SHALL return a value of type Optional&lt;MediaType>.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The BEFORE and AFTER code for getMediaType are identical, with no changes to the implementation or interface.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-get-original-name-a43e4662**: WHEN a caller invokes getOriginalName, the FilePointer SHALL return the original name.
  - Acceptance criteria:
    - AC1: WHEN getOriginalName completes normally, the FilePointer SHALL return a value of type String.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The getOriginalName implementation is byte-for-byte identical between BEFORE and AFTER, returning target.getName() unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-get-size-c2289cab**: WHEN a caller invokes getSize, the FilePointer SHALL return the size.
  - Acceptance criteria:
    - AC1: WHEN getSize completes normally, the FilePointer SHALL return a value of type long.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The getSize implementation is identical between BEFORE and AFTER, returning target.length() unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-build-resource-03dab7d6**: WHEN a caller invokes buildResource, the ExistingFile SHALL build the resource.
  - Acceptance criteria:
    - AC1: WHEN buildResource runs, the ExistingFile SHALL call open.
    - AC2: WHEN buildResource completes normally, the ExistingFile SHALL return new InputStreamResource(throttlingInputStream).
    - AC3: WHEN buildResource is invoked, the ExistingFile SHALL accept filePointer of type FilePointer.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The buildResource method is identical in both versions, constructing the same ThrottlingInputStream and InputStreamResource with no change in logic.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-download-controller-a5b5b8a3**: WHEN a caller creates a DownloadController, the DownloadController SHALL initialize the DownloadController with the given storage.
  - Acceptance criteria:
    - AC1: WHEN DownloadController is constructed, the DownloadController SHALL accept storage of type FileStorage.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The AFTER code removes the @Autowired annotation on the constructor. In Spring 4.3+, a single constructor is implicitly autowired, so the dependency injection behavior (initializing DownloadController with the given storage) is preserved even without the explicit annotation.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-download-retrieval-15e14161**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN download runs, the DownloadController SHALL call findExistingFile and handle.
    - AC2: WHEN download is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The download method implementation is identical in BEFORE and AFTER, with no behavioral changes to request handling, headers, or response logic.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-find-existing-file-9efb6079**: WHEN a caller invokes findExistingFile, the DownloadController SHALL return the existing file for the given method and uuid.
  - Acceptance criteria:
    - AC1: WHEN findExistingFile runs, the DownloadController SHALL call findFile.
    - AC2: WHEN findExistingFile completes normally, the DownloadController SHALL return a value of type Optional&lt;ExistingFile>.
    - AC3: WHEN findExistingFile is invoked, the DownloadController SHALL accept method of type HttpMethod and uuid of type UUID.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The findExistingFile method implementation is unchanged between BEFORE and AFTER, preserving the same logic for locating files via storage.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handle-configuration-5f6121fd**: WHEN a caller invokes handle, the ExistingFile SHALL handle the request etag opt.
  - Acceptance criteria:
    - AC1: WHEN handle runs, the ExistingFile SHALL call serveWithCaching.
    - AC2: WHEN handle completes normally, the ExistingFile SHALL return serveWithCaching(requestEtagOpt, ifModifiedSinceOpt, this::serveDownload).
    - AC3: WHEN handle is invoked, the ExistingFile SHALL accept requestEtagOpt of type Optional&lt;String> and ifModifiedSinceOpt of type Optional&lt;Date>.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The handle method implementation is byte-for-byte identical before and after, delegating to serveWithCaching with the same arguments and method reference.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-configuration-fo-9109a9b9**: WHEN a caller invokes cached, the ExistingFile SHALL perform cached.
  - Acceptance criteria:
    - AC1: WHEN cached completes normally, the ExistingFile SHALL return matchingEtag || notModifiedSince.
    - AC2: WHEN cached is invoked, the ExistingFile SHALL accept requestEtagOpt of type Optional&lt;String> and ifModifiedSinceOpt of type Optional&lt;Date>.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The cached method implementation is unchanged between before and after, preserving the same etag matching and modified-since logic.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-configuration-fo-6b216cd9**: WHEN a caller invokes redirect, the ExistingFile SHALL perform redirect.
  - Acceptance criteria:
    - AC1: WHEN redirect runs, the ExistingFile SHALL call serveWithCaching.
    - AC2: WHEN redirect completes normally, the ExistingFile SHALL return serveWithCaching(requestEtagOpt, ifModifiedSinceOpt, this::redirectDownload).
    - AC3: WHEN redirect is invoked, the ExistingFile SHALL accept requestEtagOpt of type Optional&lt;String> and ifModifiedSinceOpt of type Optional&lt;Date>.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The redirect method implementation is identical before and after, delegating to serveWithCaching with the same arguments and method reference.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-retrieval-for-fi-6aab06fd**: WHEN a caller creates a FileSystemPointer, the FileSystemPointer SHALL initialize the FileSystemPointer with the given target.
  - Acceptance criteria:
    - AC1: WHEN FileSystemPointer is constructed, the FileSystemPointer SHALL accept target of type File.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The change replaces Guava's deprecated Files.hash(target, Hashing.sha512()) with Files.asByteSource(target).hash(Hashing.sha512()), which is the standard upgrade recipe for newer Guava versions. Both compute the same SHA-512 hash of the file content, and the rest of the constructor (setting target, probing content type) is unchanged. Observable behaviour is preserved.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-retrieval-for-re-121f3aac**: WHEN a caller invokes redirectDownload, the ExistingFile SHALL perform redirectDownload.
  - Acceptance criteria:
    - AC1: WHEN redirectDownload runs, the ExistingFile SHALL call getOriginalName.
    - AC2: WHEN redirectDownload completes normally, the ExistingFile SHALL return a value of type ResponseEntity&lt;Resource>.
    - AC3: WHEN redirectDownload is invoked, the ExistingFile SHALL accept filePointer of type FilePointer.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The BEFORE and AFTER code for redirectDownload are identical, no changes at all.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-retrieval-for-re-e2c6b1f6**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN redirect runs, the DownloadController SHALL call findExistingFile.
    - AC2: WHEN redirect is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The BEFORE and AFTER code for the redirect endpoint are identical (only line numbers differ), no behavioural change.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-retrieval-for-se-a7ea24aa**: WHEN a caller invokes serveDownload, the ExistingFile SHALL perform serveDownload.
  - Acceptance criteria:
    - AC1: WHEN serveDownload runs, the ExistingFile SHALL call resourceToReturn and response.
    - AC2: WHEN serveDownload completes normally, the ExistingFile SHALL return response(filePointer, OK, resource).
    - AC3: WHEN serveDownload is invoked, the ExistingFile SHALL accept filePointer of type FilePointer.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The serveDownload method implementation is byte-for-byte identical between BEFORE and AFTER. No behavioural change.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-retrieval-for-th-51ba29c2**: WHEN a caller creates a ThrottlingInputStream, the ThrottlingInputStream SHALL initialize the ThrottlingInputStream with the given target and max bytes per second.
  - Acceptance criteria:
    - AC1: WHEN ThrottlingInputStream is constructed, the ThrottlingInputStream SHALL accept target of type InputStream and maxBytesPerSecond of type RateLimiter.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-provide-download-control-2f528472**: WHEN an HTTP request is received on /download, the DownloadController SHALL provide the operations redirect, download and findExistingFile.
  - Acceptance criteria:
    - AC1: The DownloadController SHALL expose the operations redirect, download and findExistingFile.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: DownloadController's redirect, download, and findExistingFile methods are unchanged in logic. The only difference is removal of @Autowired annotation on the constructor, which is a cosmetic/framework-recommended change (Spring supports implicit constructor injection with a single constructor), not an observable behaviour change.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-provide-existing-file-ca-1dacd5f3**: WHEN a caller invokes an operation of ExistingFile, the ExistingFile SHALL provide the operations redirect, handle, serveWithCaching, cached and redirectDownload (+6 more).
  - Acceptance criteria:
    - AC1: The ExistingFile SHALL expose the operations redirect, handle, serveWithCaching, cached and redirectDownload (+6 more).
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: All methods of ExistingFile remain functionally equivalent. The only change is in resourceToReturn, switching from `method == HttpMethod.GET` to `HttpMethod.GET.equals(method)`. Both produce the same result for non-null HttpMethod values, and equals() guards against NPE if method is null (though method is always set via constructor). This is a semantically equivalent, non-breaking change.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-provide-file-pointer-cap-95d2dd09**: WHEN a caller invokes an operation of FileSystemPointer, the FileSystemPointer SHALL provide the operations open, getSize, getOriginalName, getEtag and getMediaType (+4 more).
  - Acceptance criteria:
    - AC1: The FileSystemPointer SHALL implement FilePointer.
    - AC2: The FileSystemPointer SHALL expose the operations open, getSize, getOriginalName, getEtag and getMediaType (+4 more).
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The only change is replacing Files.hash(target, Hashing.sha512()) with Files.asByteSource(target).hash(Hashing.sha512()), which is Guava's recommended non-deprecated equivalent producing the same hash result. All interface methods (open, getSize, getOriginalName, getEtag, getMediaType, matchesEtag, getLastModified, modifiedAfter) remain implemented identically.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-provide-file-storage-cap-d1379f4d**: WHEN a caller invokes an operation of FileSystemStorage, the FileSystemStorage SHALL provide the operation findFile.
  - Acceptance criteria:
    - AC1: The FileSystemStorage SHALL expose the operation findFile.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-provide-main-application-20938697**: WHEN a caller invokes an operation of MainApplication, the MainApplication SHALL provide the operation main.
  - Acceptance criteria:
    - AC1: The MainApplication SHALL expose the operation main.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The change replaces the deprecated 'new Integer("1234")' constructor with 'Integer.valueOf("1234")', which produces an equivalent Integer value. The main method still calls SpringApplication.run identically, so observable behavior of the main operation is unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-provide-throttling-input-59596463**: WHEN a caller invokes an operation of ThrottlingInputStream, the ThrottlingInputStream SHALL provide the operations read, skip, available, mark and reset (+2 more).
  - Acceptance criteria:
    - AC1: The ThrottlingInputStream SHALL expose the operations read, skip, available, mark and reset (+2 more).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-read-as-part-workflow-20e38878**: WHEN a caller invokes read, the ThrottlingInputStream SHALL return the throttling input stream for the given b, off and len.
  - Acceptance criteria:
    - AC1: WHEN read completes normally, the ThrottlingInputStream SHALL return target.read(b, off, len).
    - AC2: WHEN read is invoked, the ThrottlingInputStream SHALL accept b of type byte[], off of type int and len of type int.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **NFR-OBSERVABILITY-9f46ce8c**: WHEN redirectDownload, serveDownload, notModified or another supporting component handles a request or an error, the system SHALL emit logs, metrics, or traces for its operations.
  - Acceptance criteria:
    - AC1: WHEN ExistingFile.redirectDownload is invoked, the system SHALL record a trace-level log entry (evidence: log.trace in ExistingFile.redirectDownload, ExistingFile.java:67).
    - AC2: WHEN ExistingFile.serveDownload is invoked, the system SHALL record a debug-level log entry (evidence: log.debug in ExistingFile.serveDownload, ExistingFile.java:78).
    - AC3: WHEN ExistingFile.notModified is invoked, the system SHALL record a trace-level log entry (evidence: log.trace in ExistingFile.notModified, ExistingFile.java:98).
    - AC4: WHEN FileSystemStorage.findFile is invoked, the system SHALL record a debug-level log entry (evidence: log.debug in FileSystemStorage.findFile, FileSystemStorage.java:25).
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: No code changes are shown between BEFORE and AFTER for ExistingFile.java or FileSystemStorage.java; all logging statements (log.trace, log.debug) remain identical in both versions.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **NFR-PERFORMANCE-92c573e7**: WHEN cached serves repeated or bulk work, the system SHALL use caching or batching to bound response latency.
  - Acceptance criteria:
    - AC1: WHEN ExistingFile.cached serves repeated or bulk work, the system SHALL use caching or batching (evidence: cached in ExistingFile.cached, ExistingFile.java:54).
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The cached() method implementation is byte-for-byte identical between BEFORE and AFTER, so caching-based latency bounding behaviour is unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **NFR-RELIABILITY-49e8cb2f**: WHEN an exception is raised in redirectDownload, FileSystemPointer or open, the system SHALL catch and handle exceptions raised during its operations.
  - Acceptance criteria:
    - AC1: IF URISyntaxException is thrown in ExistingFile.redirectDownload, THEN the system SHALL catch it and throw IllegalArgumentException (evidence: catch (URISyntaxException e) in ExistingFile.redirectDownload, ExistingFile.java:72).
    - AC2: IF IOException is thrown in FileSystemPointer.FileSystemPointer, THEN the system SHALL catch it and throw IllegalArgumentException (evidence: catch (IOException e) in FileSystemPointer.FileSystemPointer, FileSystemPointer.java:27).
    - AC3: IF FileNotFoundException is thrown in FileSystemPointer.open, THEN the system SHALL catch it and throw IllegalArgumentException (evidence: catch (FileNotFoundException e) in FileSystemPointer.open, FileSystemPointer.java:36).
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The exception handling structure is unchanged in redirectDownload and open(). In FileSystemPointer constructor, the hashing call changed from Files.hash(target, Hashing.sha512()) to Files.asByteSource(target).hash(Hashing.sha512()), which is the standard Guava API migration (Files.hash was deprecated/removed in favor of asByteSource().hash()). Both are wrapped in the same try/catch IOException block, preserving the exception handling behaviour.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **NFR-SECURITY-742b03ce**: WHEN sensitive data is handled by an operation configured by tag, the system SHALL protect data in transit and at rest using cryptographic controls.
  - Acceptance criteria:
    - AC1: WHEN sensitive data is handled by an operation configured by FileSystemPointer.tag, the system SHALL apply cryptographic protection (evidence: HashCode in FileSystemPointer.tag, FileSystemPointer.java:16).
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The tag field declaration is identical in both versions. The underlying hash computation API changed (as seen in requirement 1) but this is a Guava API rename, not a change in cryptographic algorithm (still Hashing.sha512()), so the security control is preserved.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
