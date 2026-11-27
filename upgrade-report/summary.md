# 📊 Executive Report — Java Version Upgrade: `download-server`  
  
**Iteration:** 65 &nbsp;|&nbsp; **Date:** 2026-11-27 &nbsp;|&nbsp; **Base commit:** `73556be`  
**Engine:** `OPENREWRITE_WITH_CLAUDE_CLI` &nbsp;|&nbsp; **Module:** `com.nurkiewicz.download:download-server:0.0.1-SNAPSHOT`  
  
---  
  
## 1. 🎯 Executive Summary  
  
- ✅ **Outcome: COMPLETE AND GREEN.** A Spring Boot 1.2.3 / Spring Framework 4.1 / Java 8 file-download  
  service was upgraded to **Java 25, Spring Boot 4.0.10 and Spring Framework 7.0.11**, finishing with  
  `mvn clean install` → **BUILD SUCCESS, 0 compilation errors, 0 warnings, 16/16 tests passing**.  
- 🤖 The work was done in two automated legs: **OpenRewrite** applied the mechanical build/dependency  
  and JDK-API transformations, then the **Claude CLI (Opus 5)** leg acted as the backstop — it fixed the  
  one compilation error the recipes could not resolve, reconciled five stale dependency pins the recipes  
  left behind, and expanded test coverage.  
- 🌉 This was a **13-year, 3-major-version jump** (Boot 1.2 → 2.x → 3.x → 4.0; Spring 4.1 → 7.0;  
  Java 8 → 11 → 17 → 21 → 25), crossing the **javax → jakarta** boundary and the JUnit 4 → JUnit 6  
  platform change in a single pass.  
- 🧪 **The headline finding is a test-coverage one.** The pre-upgrade test inventory  
  (`test-inventory/before.json`) recorded **`{"files":0,"tests":[]}`** — *zero* tests. The project's only  
  test, a 12-feature Groovy/Spock spec, was never compiled or run by Maven (no Groovy compiler plugin,  
  and the class name did not match Surefire's includes). It is now wired in and passing, and 4 further  
  tests were added, taking the suite from **0 → 16 executing tests**.  
- ⚠️ **One accuracy caveat on the recipe legs:** both OpenRewrite runs report `BUILD SUCCESS`, but that  
  refers to the `rewrite:run` *goal* completing — **not** to the transformed code compiling. The  
  post-recipe tree did **not** compile; the Claude leg was the first step to compile it and found the  
  error. OpenRewrite alone would have left a red build.  
- 📉 Zero `javax.*` API usages and zero `javax.*` artifacts remain; the stack is internally consistent on  
  the Jakarta line with no mixed-namespace or stale-version residue.  
  
---  
  
## 2. 🔧 Application Changes  
  
### 2.1 Build & platform  
  
- ⬆️ **Parent POM:** `spring-boot-starter-parent` **1.2.3.RELEASE → 4.0.10** (recipe targeted `4.0.x`).  
- ⬆️ **Java release:** `source/target 1.8` → **`<release>25</release>`**; `java.version` property added as `25`.  
- ⬆️ **maven-compiler-plugin:** 3.0 → **3.16.0**.  
- 🔁 **Starter rename:** `spring-boot-starter-web` → **`spring-boot-starter-webmvc`** (Boot 4.0 breaking change).  
- 🧹 **Removed 10 explicit `org.springframework:*` version pins** (4.1.6.RELEASE) so the Boot BOM governs  
  them; all now resolve to **7.0.11**.  
- 🧹 **Fixed a pre-existing malformed POM:** Maven warned  
  `'dependencies.dependency...' must be unique: org.springframework:spring-test:jar -> duplicate  
  declaration of version 4.1.6.RELEASE`. Resolved by `RemoveDuplicateDependencies`.  
- 🛡️ **Removed the plaintext-HTTP `repo.spring.io/milestone` repository** — the log shows  
  `Failed to access maven repository http://repo.spring.io/milestone/`; Maven 3.8+ blocks HTTP repos and  
  all required artifacts are GA on Central.  
  
### 2.2 Namespace (javax → jakarta)  
  
- 🅹 **Jakarta transition was clean and required no source edits.** The codebase never touched the  
  servlet API directly, so `JakartaEE10` / `JavaxMigrationToJakarta` produced **no import rewrites** —  
  its only action was `AddDependency` for **`jakarta.servlet:jakarta.servlet-api`** (test scope, → 6.1.0).  
- ✔️ Verified post-build: **0 `javax.*` imports, 0 `javax.*` artifacts**. Jakarta artifacts in use:  
  `jakarta.servlet-api 6.1.0`, `jakarta.annotation-api 3.0.0`, `jakarta.xml.bind-api 4.0.5`,  
  `jakarta.activation-api 2.1.4`.  
  
### 2.3 Test stack modernisation  
  
- ⬆️ **Groovy:** `org.codehaus.groovy:groovy-all 2.4.3` → **`org.apache.groovy:groovy 5.0.8`** (BOM-managed).  
- ⬆️ **Spock:** `1.0-groovy-2.4` → **`2.5-groovy-5.0`** (JUnit 4 runner → JUnit Platform).  
- ⬆️ **JUnit:** 4.x → **6.0.3**; **Mockito:** `1.10.19` (transitive, per `DependenciesInUse.csv`) → **5.20.0**.  
- ➕ **Added `gmavenplus-plugin 4.2.1`** (`compileTests`) — the Groovy source set had **never** been compiled.  
- ➕ **Added Surefire `<includes>`** for `**/*Spec.java` alongside the default `*Test`/`*Tests` patterns.  
- 🧹 **Removed `cglib:cglib-nodep 3.1`** — Spock-1-only, unused by any source file, and cannot generate  
  classes on JDK 25.  
  
### 2.4 Library versions  
  
| Library | Before | After | Managed by |  
|---|---|---|---|  
| Spring Boot | 1.2.3.RELEASE | **4.0.10** | parent |  
| Spring Framework | 4.1.6.RELEASE | **7.0.11** | BOM |  
| Tomcat (embed) | 7.x (Boot 1.2) | **11.0.27** | BOM |  
| JUnit | 4.x | **6.0.3** | BOM |  
| Mockito | 1.10.19 | **5.20.0** | BOM |  
| Logback | 1.1.x | **1.5.38** | BOM |  
| Jackson | 2.x (`com.fasterxml`) | **3.1.8 (`tools.jackson`)** | BOM |  
| commons-codec | 1.10 | **1.19.0** | BOM |  
| Groovy | 2.4.3 | **5.0.8** | BOM |  
| Spock | 1.0-groovy-2.4 | **2.5-groovy-5.0** | explicit |  
| Guava | 18.0 | **33.8.0-jre** | explicit |  
| commons-io | 2.4 | **2.22.0** | explicit |  
  
---  
  
## 3. 🛠️ Tools Used  
  
- ☕ **Java SDK:** **Amazon Corretto 25.0.5.1** (OpenJDK 25.0.5.1+2-LTS, `aarch64`) — both build JDK and  
  compile target (`release 25`).  
- 🧰 **Apache Maven 3.9.16** (project wrapper pins 3.9.8) — 16 CPUs, 4 GB cgroup limit,  
  **peak 1.11 GB, 0 OOM kills**.  
- 🔄 **OpenRewrite `rewrite-maven-plugin` 6.42.0**, two recipe legs:  
  - 🅰️ `com.amazonaws.java.migrate.UpgradeToJava25` — chained `UpgradeToJava8` → `Java8toJava11` →  
    `UpgradeToJava17` → `…Java21` → `UpgradeBuildToJava25`. Leaf recipes that actually fired:  
    `UseMavenCompilerPluginReleaseConfiguration`, `UpdateMavenProjectPropertyJavaVersion`,  
    `UpgradePluginVersion`, `UpgradeDependencyVersion`, `AddMockitoJavaAgentToMavenSurefirePlugin`,  
    `PrimitiveWrapperClassConstructorToValueOf`. ⏱️ 34s, self-reported saving **1h**.  
  - 🅱️ `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` — chained every hop from  
    `UpgradeSpringBoot_2_0` through `_3_5` to `_4_0`, including `UpgradeSpringFramework_5_0…6_0`,  
    `JakartaEE10`, and `SpringBoot2JUnit4to5Migration`. Leaf recipes that fired: `UpgradeParentVersion`,  
    `ChangeDependency`, `AddDependency`, `UpgradeDependencyVersion`, `ExcludeDependency`,  
    `RemoveExclusion`, `RemoveDuplicateDependencies`, `NoAutowiredOnConstructor`.  
    ⏱️ 1m 12s, self-reported saving **2h 32m**.  
- 🤖 **Claude CLI 2.1.266 (Claude Code)** — model **`global.anthropic.claude-opus-5`** (**Claude Opus 5**),  
  Bedrock, 200K context. Ran the build-fix + verification + reporting leg across **3 invocations /  
  63 turns**, ⏱️ **~12.2 min** of model time, **$5.23** total list cost. 0 permission denials,  
  0 subagents, all 3 invocations `terminal_reason: completed`.  
- 📚 **Migration knowledge base** (`/app/knowledge`, 38 docs) — consulted for the Boot 4 starter rename,  
  jakarta guardrails and build-error triage.  
- ℹ️ **CodeKB was NOT available** this run (no `mcp__codekb__*` tools exposed); old-code mapping was done  
  by direct source reads.  
  
---  
  
## 4. 💻 Code Changes  
  
### 4.1 Applied by OpenRewrite (per `SourcesFileResults.csv`)  
  
- 📄 **`pom.xml`** — the overwhelming majority of recipe activity; all 12 leaf recipes above landed here.  
- 📄 **`MainApplication.java`** — `PrimitiveWrapperClassConstructorToValueOf`:  
  `new Integer("1234")` → `Integer.valueOf("1234")`. *(The only Java source change from the entire Java-25 leg.)*  
- 📄 **`DownloadController.java`** — `NoAutowiredOnConstructor`: dropped the redundant `@Autowired` from  
  the single-constructor bean.  
- ⚙️ **Surefire hardening** — `AddMockitoJavaAgentToMavenSurefirePlugin` added an `argLine` with  
  `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` plus a `maven-dependency-plugin:properties`  
  execution, required for Mockito on JDK 25 (no self-attach).  
- 📊 **Net recipe reach: 2 Java files touched, 0 Groovy files, 1 POM.** Everything else in the  
  data tables is composite-recipe bookkeeping with `estimatedTimeSaving=0`.  
  
### 4.2 Applied by the Claude CLI leg (the backstop)  
  
- 🐛 **The one compilation error — `Sha512ShallowEtagHeaderFilter.java:8`**  
  `method does not override or implement a method from a supertype`.  
  Spring 7 changed `ShallowEtagHeaderFilter.generateETagHeaderValue` from `(byte[])` to  
  **`(InputStream, boolean isWeak) throws IOException`**. Rewritten to the new signature, feeding  
  `inputStream.readAllBytes()` into the same `Hashing.sha512().hashBytes(...)` call and honouring the new  
  `isWeak` flag with the `W/` prefix. **Output is byte-identical under the filter's default config.**  
- 🧹 **Dependency reconciliation the recipes left behind** (5 items): replaced the recipe-added  
  `dependencyManagement` pin of `groovy-all:2.4.16` (which *overrode* the Boot BOM) with BOM-managed  
  `org.apache.groovy:groovy`; Spock 1.0 → 2.5; removed `cglib-nodep`; removed the dead  
  `<spring.version>4.1.1.RELEASE</spring.version>` property; removed the HTTP milestone repo.  
  Guava 29 → 33.8.0-jre and commons-io 2.4 → 2.22.0, each verified API-by-API against the jar.  
- 🔇 **`logback.xml` silent defect** — Logback 1.5 reported  
  `Missing attribute [name]. See element [appender]`: the console appender was declared inline inside  
  `<root>` with no name, so **no appender was attached to the root logger at all** (the app logged  
  nothing). Restructured to a named `<appender name="CONSOLE">` + `<appender-ref>`, preserving the  
  pattern and all levels verbatim.  
- ♻️ **`FileSystemPointer`** — deprecated `Files.hash(File, HashFunction)` → `Files.asByteSource(file).hash(...)`.  
  Identical `HashCode`; clears the last deprecation warning (`-Xlint:deprecation` now clean).  
- 🧪 **Test coverage 0 → 16** (no test was deleted, skipped, or weakened):  
  - `DownloadControllerSpec` (12, Groovy/Spock, **unmodified**) — now actually executing: routing,  
    body bytes, ETag/`If-None-Match` 304s, `If-Modified-Since`, `Content-Length`, `Content-Type`,  
    bodyless `HEAD`.  
  - `Sha512ShallowEtagHeaderFilterTest` (3, **new**) — covers the rewritten override: strong quoted  
    SHA-512, `W/` weak prefix, and ETag parity with `FileSystemPointer`. Expected digest is a literal  
    independently confirmed with `sha512sum`, not recomputed with the API under test.  
  - `MainApplicationTest` (1, **new**) — `@SpringBootTest(webEnvironment = RANDOM_PORT)`: real embedded  
    Tomcat on a real socket, `GET /actuator/health` → `200` + `"status":"UP"`.  
- 🚫 **Business logic unchanged.** No controller route, caching rule, throttling behaviour or ETag format  
  was altered.  
  
### 4.3 📌 Spring upgrade outcome — stated precisely  
  
- ⚠️ **The log contains NO `Verified: effective Spring Framework version…` line and NO  
  `WARNING: …still < 5.3` line.** Those gates belong to the *non-Boot Spring 5.3 / javax* path; this run  
  took `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which does not emit them. **No Spring  
  verification gate was recorded for this iteration.**  
- ✅ Because that line is absent, the outcome is asserted from the **OpenRewrite data tables** plus a  
  **direct build verification**, not from intent lines:  
  - `SourcesFileResults.csv` confirms `UpgradeParentVersion` and `ChangeDependency`  
    (`spring-boot-starter-web` → `spring-boot-starter-webmvc`, `newVersion=4.0.x`) were **applied** to `pom.xml`.  
  - `mvn dependency:tree` on the finished tree resolves **`spring-boot-* 4.0.10`** and  
    **`org.springframework:* 7.0.11`** across all 8 declarations, with **0 javax artifacts**.  
- 🟢 **Conclusion: the Spring upgrade DID reach its target** (Boot 4.0 line / Framework 7.0.11) and is  
  **not** incomplete. This is an evidence-backed statement, independent of the missing gate line.  
- 📝 One log advisory to note: *"Spring Boot upgrade path was used for the whole reactor, but plain  
  (non-Boot) Spring artifacts were also detected."* Those 8 explicit `org.springframework:*`  
  declarations are now versionless and BOM-managed — consistent, though redundant (see §6).  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
### 5.1 Tool-reported (OpenRewrite's own figures)  
  
| Leg | Self-reported saving |  
|---|---|  
| 🅰️ `UpgradeToJava25` | **1h 00m** |  
| 🅱️ `UpgradeSpringBoot_4_0` | **2h 32m** |  
| **OpenRewrite total** | **3h 32m** |  
  
### 5.2 Assessed end-to-end saving (incl. the Claude backstop)  
  
OpenRewrite's figure covers only the mechanical rewrites. Realistic manual effort for the whole job:  
  
| Task | Manual estimate |  
|---|---|  
| 🏗️ Boot 1.2 → 4.0 / Spring 4.1 → 7.0 POM surgery (parent, starter rename, 10 pins, duplicate dep) | 3.0 – 4.5 h |  
| ☕ JDK 8 → 25 build config + API sweep across 5 version hops | 0.5 – 1.0 h |  
| 🔍 Diagnosing the `ShallowEtagHeaderFilter` signature change (undocumented in release notes) | 0.5 – 1.0 h |  
| 🧪 Groovy 2.4 → 5 + Spock 1 → 2.5, incl. the `targetBytecode` trap | 1.5 – 2.5 h |  
| 🕵️ Discovering the spec never ran + wiring gmavenplus/Surefire | 1.0 – 1.5 h |  
| 🔇 Finding the silent logback appender defect | 0.5 h |  
| ✅ Guava/commons API-compatibility verification | 0.5 h |  
| ✍️ Writing the 4 new tests | 1.0 h |  
| **Total manual** | **≈ 8.5 – 12.5 h** (1 – 1.5 engineer-days) |  
  
- ⚡ **Actual elapsed:** ~**14.8 min** wall clock (OpenRewrite 1.8 min + Claude 12.2 min + builds).  
- 📈 **Net saving ≈ 8 – 12 engineer-hours**, i.e. a **~35–50× compression** of elapsed time.  
- 💰 **Automation cost: $5.23** in model spend against ~1–1.5 engineer-days displaced.  
- 🎁 **Uncosted bonus:** the 0 → 16 test coverage jump and two latent defects (dead logback appender,  
  never-running test suite) that a purely manual upgrade would most likely have shipped unnoticed.  
- 🔸 *Caveat:* this is a single-module, 8-class project. The **ratio** will not scale linearly to large  
  multi-module reactors, where per-module reconciliation dominates.  
  
---  
  
## 6. 🧭 Next Steps  
  
### ✅ Validation  
  
- 🌐 **Smoke-test the packaged jar** — *not performed; this cycle's instructions forbid executing the  
  application.* Scope is now narrow: routing, ETag/304s, `HEAD`, `Content-Type` **and real-Tomcat  
  startup + `/actuator/health`** are all test-covered, so only **fat-jar packaging** and the  
  `FileSystemStorage` classpath bug (below) remain genuinely unverified.  
- 🔁 **Re-run `mvn -B clean install` on JDK 25 in CI** and confirm **16/16**. Already run three times  
  locally, green each time, with a checksum-identical dependency tree between runs.  
- 🏷️ **Decide the fate of `Sha512ShallowEtagHeaderFilter`.** Now behaviour-tested, but still a  
  **test-scoped class registered as a filter nowhere**. Either promote it to `src/main/java` with a  
  `FilterRegistrationBean`, or delete it. **Owner call** — deleting a class with passing tests is not an  
  upgrade step.  
- 🧾 **Review the 3 non-BOM pins:** `guava 33.8.0-jre`, `commons-io 2.22.0`, `spock 2.5-groovy-5.0`  
  (plus plugins `maven-compiler 3.16.0`, `gmavenplus 4.2.1`). None are BOM-managed; none deviate from a  
  mandated version, so there is **no deviation table**.  
- ⚠️ **Spock 2.5 on JUnit Platform 6 is an undocumented pairing** (Spock 2.5 publishes against  
  junit-bom 5.14). It resolves cleanly because Boot's `junit-bom` wins, and all 12 specs pass — but if a  
  future platform bump breaks the Spock engine, the escape hatches are pinning `junit-bom` or porting the  
  spec to JUnit 5 + `MockMvcTester`.  
- 🔭 **Confirm with real clients** two behavioural changes no build can prove: trailing-slash matching is  
  no longer automatic (Framework 6+), and Boot 4 moves to **Jackson 3** (`tools.jackson`). Neither  
  affects this codebase (no trailing-slash routes, no direct Jackson usage), and the Jackson 3 render  
  path is now exercised by `MainApplicationTest`.  
  
### 💡 Improvements  
  
- 🗂️ **Replace the `FileSystemStorage` stub before production.** It returns `logback.xml` for *any* UUID  
  and uses `new File(getResource(...).getFile())`, which **cannot work from a packaged jar**. A real fix  
  means moving `FilePointer` off `java.io.File` onto a `Resource`/`ByteSource` — an architecture change  
  to a class whose own comment says *"Replace with real implementation."* **Owner decision, not an  
  upgrade step.** (Masked in tests: the `test` profile substitutes `FileStorageStub`.)  
- 📦 **`spring-boot-maven-plugin` is not declared**, so `mvn package` yields a plain jar, not an  
  executable fat jar. This matches the original build; add it if an executable artifact is wanted.  
- 🧽 **Delete the 8 redundant `org.springframework:*` declarations.** Versionless and BOM-managed, hence  
  consistent — but `spring-boot-starter-webmvc` already supplies all of them transitively. Purely  
  cosmetic, deliberately left alone under the smallest-consistent-change rule.  
- 🧹 **Remove the dead local in `MainApplication.main`** (`Integer temp = Integer.valueOf("1234");`,  
  OpenRewrite's rewrite of `new Integer("1234")`). Harmless, compiles, pre-existing.  
- 🎚️ **Narrow `groovy` to `<scope>test</scope>`.** Inherited as compile-scope from the original  
  `groovy-all` declaration; nothing in `src/main` uses Groovy, so this would shrink the published artifact.  
- 🔐 **Actuator sits at Boot 4's conservative default** (`health` only; no `application.properties`  
  exists). Add `management.endpoints.web.exposure.include` **together with** Spring Security, and only if  
  more endpoints are genuinely wanted.  
- 🗄️ **Restore CodeKB for future iterations** — unavailable this run, so old-code mapping fell back to  
  direct source reads. Tolerable at 8 classes; a real constraint on larger modules.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 16 tests run
- ⏱️ Transformation duration: 21 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 7.951292  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
