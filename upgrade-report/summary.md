# 📋 Executive Report — Java 25 / Spring Boot 4 Upgrade  
  
**Project:** `com.nurkiewicz.download:download-server` · **Iteration:** 64  
**Commit:** `73556be` · **Engine:** `OPENREWRITE_WITH_CLAUDE_CLI` · **Date:** 2026-11-24  
  
---  
  
## 1. 🎯 Executive Summary  
  
- ✅ **The upgrade completed successfully.** A Java 8 / Spring Boot 1.2.3 Maven service was  
  migrated to **Java 25 (Corretto 25.0.5.1 LTS)** and **Spring Boot 4.0.10 / Spring Framework  
  7.0.11**, finishing with `BUILD SUCCESS`, **zero compilation errors** and **zero compiler warnings**.  
- 🤖 **Two-leg pipeline:** OpenRewrite applied the bulk mechanical transformation (2 recipe runs,  
  both `BUILD SUCCESS`), then the Claude CLI build-fix leg resolved the residue the recipes could  
  not reach and validated the result.  
- 🔀 **Namespace:** the recipe moved the project onto the **jakarta** line  
  (`JavaxServletToJakartaServlet`, `JakartaEE10`, `UpgradeSpringFramework_6_0` all applied).  
  The finished tree has **zero `javax.*` Java EE references** and no mixed javax/jakarta artifacts.  
- 🧪 **The headline result is test coverage, not just a green compile.** The pre-upgrade inventory  
  recorded **`{"files":0,"tests":0}` — not a single test executed**. The finished build runs  
  **16 tests, all passing** (13 Spock + 3 JUnit 5).  
- 🚨 **A latent production blocker was found and fixed:** the application **could not boot** on  
  Spring Boot 4 at all. See §4.  
- ⚠️ **One caveat from the log:** `NOTE: Spring Boot upgrade path was used for the whole reactor,  
  but plain (non-Boot) Spring artifacts were also detected.` Those 8 explicit `org.springframework:*`  
  declarations were reconciled by the manual leg and are now BOM-managed, but this is worth an  
  owner glance.  
  
### 📌 Accuracy note on the Spring verification gate  
  
- 🔍 **The log contains no `Verified: effective Spring Framework version …` line and no  
  `WARNING: … still < 5.3` line** — `grep -c` returns **0** for both. Those gates belong to the  
  **non-Boot javax Spring 5.3 path** (knowledge docs 24/29); this run took the **Boot 4 jakarta  
  path**, so neither was emitted.  
- ✅ The Spring outcome below is therefore established from **two independent sources**, not from a  
  gate line: the OpenRewrite `SourcesFileResults` data table, and the resolved versions in  
  `mvn dependency:tree` on the finished tree.  
- ✅ **Conclusion: the Spring upgrade did reach its target.** `UpgradeSpringBoot_4_0` applied to  
  `pom.xml`, `UpgradeParentVersion` ×15, and resolution confirms **Boot 4.0.10 / Framework 7.0.11**.  
  This is **not** an incomplete Spring upgrade.  
  
---  
  
## 2. 🔧 Application Changes  
  
### Build & platform  
  
- ⬆️ **Parent POM:** `spring-boot-starter-parent` `1.2.3.RELEASE` → **`4.0.10`**  
- ⬆️ **Compiler:** `<source>/<target>1.8` → **`<release>25`** via  
  `UseMavenCompilerPluginReleaseConfiguration`; `maven-compiler-plugin` `3.0` → `3.16.0`  
- 🔁 **Starter rename (Boot 4 structural change):** `spring-boot-starter-web` →  
  **`spring-boot-starter-webmvc`**  
- 🧹 **Removed a duplicate `spring-test` declaration** that the initial build had flagged:  
  `'dependencies.dependency…' must be unique: org.springframework:spring-test:jar -> duplicate  
  declaration of version 4.1.6.RELEASE @ line 144`  
- 🤖 **Added `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` to Surefire** (via  
  `AddMockitoJavaAgentToMavenSurefirePlugin`) plus the `maven-dependency-plugin:properties` goal —  
  JDK 25 forbids self-attaching agents.  
- 🗑️ **Removed the plain-HTTP `http://repo.spring.io/milestone/` repository.** The log shows it  
  already failing: `Failed to access maven repository http://repo.spring.io/milestone/`. Blocked by  
  Maven 3.8+ and no longer needed.  
  
### Dependencies — recipe output reconciled by the manual leg  
  
The recipes produced a POM that *resolved* but still contained artifacts that **cannot run on JDK 25**:  
  
| Artifact | Before | After | Why it mattered |  
| --- | --- | --- | --- |  
| 🍸 Groovy | `codehaus:groovy-all` 2.4.16 *(recipe-pinned, compile)* | `org.apache.groovy:groovy` **5.0.8** *(BOM, test)* | Groovy 2.4 cannot run on JDK 25 |  
| 🧫 Spock | `1.0-groovy-2.4` | **`2.5-groovy-5.0`** | Spock 1.0 is a JUnit 4 runner bound to Groovy 2.4 |  
| 🧨 cglib | `cglib-nodep` 3.1 | **removed** | Spock 1's mock backend; cannot instrument a modern JDK |  
| 🔐 commons-codec | 1.10 | **removed** | Compile-scoped, graph leaf, used by no source |  
| 📦 Guava | 18.0 → *29.0-jre (recipe)* | **33.8.0-jre** | Stale; every used API re-verified via `javap` |  
| 📁 commons-io | 2.4 | **2.22.0** | 2012-era, not BOM-managed |  
| 🧾 `<spring.version>` | `4.1.1.RELEASE` | **removed** | Stale property contradicting managed 7.0.11 |  
  
- 🏛️ **BOM discipline maintained:** every artifact Boot 4's BOM manages is declared **versionless**.  
  The only explicit pins are on artifacts the BOM does *not* manage: Spock `2.5-groovy-5.0`,  
  gmavenplus `5.1.0`, Guava `33.8.0-jre`, commons-io `2.22.0`, compiler plugin `3.16.0`.  
  
---  
  
## 3. 🛠️ Tools Used  
  
| Tool | Version / Identifier |  
| --- | --- |  
| ☕ **Java SDK** | **Amazon Corretto 25.0.5.1.2** (build `25.0.5.1+2-LTS`), `aarch64` |  
| 📦 **Apache Maven** | **3.9.16** |  
| 🔄 **OpenRewrite** | **`rewrite-maven-plugin:6.42.0`** |  
| 🧩 **Recipe 1 — JDK** | `com.amazonaws.java.migrate.UpgradeToJava25` → `OfficialUpgradeToJava25WithoutIfElseIfToSwitch` (cumulative 8→11→17→21→25) |  
| 🌱 **Recipe 2 — Spring** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` → `org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0` (cumulative 2.0 → 2.7 → 3.0 → 3.5 → 4.0) |  
| 🤖 **Claude CLI** | model **`global.anthropic.claude-opus-5`** (canonical `claude-opus-5`, Bedrock), 200k context |  
| 🧱 **Engine** | `OPENREWRITE_WITH_CLAUDE_CLI` |  
| 💾 **Resources** | peak **1.05 GB** of a 4 GB cap, **0 OOM kills**, 16 CPUs |  
  
- 📚 A 38-document **Java migration knowledge base** was mounted read-only at `/app/knowledge` and  
  consulted for the Boot 4 starter-rename and troubleshooting patterns (docs 19, 21).  
  
---  
  
## 4. 💻 Code Changes  
  
### 🤖 Applied automatically by OpenRewrite  
  
- 🔢 `PrimitiveWrapperClassConstructorToValueOf` — `new Integer("1234")` → `Integer.valueOf("1234")`  
  (`MainApplication.java`); the removed boxing constructor was the initial build's only  
  deprecation warning.  
- 🧷 `NoAutowiredOnConstructor` — dropped the redundant `@Autowired` from the single-constructor  
  `DownloadController`.  
- 🔀 `JavaxServletToJakartaServlet` / `JakartaEE10` — namespace move; `AddDependency` added  
  `jakarta.servlet:jakarta.servlet-api` (test).  
- 🧹 `RemoveDuplicateDependencies`, `RemoveDependency` ×14, `ExcludeDependency` ×2,  
  `RemoveExclusion` ×2, `UpgradeDependencyVersion` ×7, `UpgradeParentVersion` ×15.  
- 🧪 `JUnit4to5Migration` / `SpringBoot2JUnit4to5Migration` / `ExcludeJUnit4UnlessUsingTestcontainers`  
  — JUnit 4 excluded in favour of the JUnit 5 platform.  
- 📊 **Reach:** `pom.xml` + **all 8 main** + **3 test** Java files + the Groovy spec were touched  
  across the two runs.  
  
### 🧑‍💻 Completed by the Claude CLI leg  
  
- 🏷️ **The single compilation error — `Sha512ShallowEtagHeaderFilter.java:8`:**  
  `method does not override or implement a method from a supertype`. Spring Framework 5 replaced  
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with  
  `generateETagHeaderValue(InputStream, boolean isWeak)`. Ported the override (signature confirmed  
  via `javap` against `spring-web-7.0.11`), keeping the SHA-512 ETag and honouring the new weak-ETag  
  flag. **No OpenRewrite recipe covers this.**  
- 🔑 **Guava deprecation:** `Files.hash(target, …)` → `Files.asByteSource(target).hash(…)` —  
  the documented equivalent; build is now warning-free.  
  
### 🚨 Critical finding — the application could not start on Boot 4  
  
- 💥 A newly added `@SpringBootTest` context-load test failed immediately:  
  `IllegalStateException: Logback configuration error detected: ERROR in AppenderAction - Missing  
  attribute [name]. See element [appender] near line 9`.  
- 🔍 **Root cause:** `logback.xml` declared its `ConsoleAppender` **nested inside `<root>`** with no  
  `name`. Logback always rejected that, but Boot 1.2 only *printed* it — **Boot 3+/4 escalates any  
  Logback config error to a fatal exception** during environment preparation, before any bean exists.  
- ⚠️ **Why nothing caught it earlier:** a green compile and 13 green MockMvc tests cannot detect it,  
  because `@ContextConfiguration` never runs Boot's `LoggingApplicationListener`. Earlier iterations  
  had classified this file as "pre-existing and cosmetic" — **correct about the cause, wrong about the  
  consequence.** The upgrade converted a harmless console message into a boot-blocking failure.  
- ✅ **Fixed** by naming the appender at `<configuration>` level and referencing it via  
  `<appender-ref>`, preserving the original pattern and logger levels. This also restored application  
  logging, which the broken config had silently disabled — the root logger previously had **no  
  appender at all**. Verified: `Started … in 3.608 seconds` using the configured pattern.  
  
### 🧪 Test suite: from 0 executed to 16 passing  
  
- 📉 **Before:** `test-inventory/before.json` = `{"files":0,"tests":[]}`. The Spock spec existed but the  
  POM had **no Groovy compiler plugin**, so `mvn test` ran nothing — before *or* after the recipes.  
  The upgrade was compile-verified only.  
- ➕ Added `gmavenplus-plugin:5.1.0` (`compileTests`) and a Surefire `**/*Spec.java` include (the class  
  is `DownloadControllerSpec`, which the default `*Test`/`*Tests` includes miss). **All 12 pre-existing  
  features passed unmodified.**  
- ➕ **+1 Spock feature** — the `/download/{uuid}` → **301 redirect** path had no coverage; now asserts  
  the status and the exact `Location` value.  
- ➕ **+2 JUnit 5 tests** for `Sha512ShallowEtagHeaderFilter`, previously only compile-checked: the  
  strong ETag is checked against an **independent oracle** (the JDK's own `MessageDigest`, *not* the  
  Guava code the filter uses), plus the new weak-`W/"…"` branch.  
- ➕ **+1 JUnit 5 test** `MainApplicationContextTest` — `@SpringBootTest` (MOCK servlet env, no server  
  socket) proving Boot 4 auto-configuration starts. **This is what caught the Logback failure.**  
- 🛡️ **No test was deleted, renamed, disabled, skipped or weakened at any point.**  
  
```  
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0  
  DownloadControllerSpec .............. 13  (Spock 2.5)  
  Sha512ShallowEtagHeaderFilterTest ...  2  (JUnit 5)  
  MainApplicationContextTest ..........  1  (JUnit 5)  
```  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
### 🤖 OpenRewrite's own attribution (`estimatedTimeSaving`, de-duplicated)  
  
| Recipe run | Unique (file, recipe) results | Attributed saving |  
| --- | --- | --- |  
| Java 25 | 128 | **30 min** |  
| Spring Boot 4.0 | 189 | **37 min** |  
| **Total** | **317** | **≈ 67 min** |  
  
### 🧮 Realistic end-to-end estimate  
  
| Work item | Manual estimate | Actual | Saved |  
| --- | --- | --- | --- |  
| 🔢 JDK 8→25 build/plugin/API sweep | 3–5 h | automated | ~4 h |  
| 🌱 Boot 1.2 → 4.0 (10 cumulative hops) + Framework 7 | 2–4 d | automated | ~2.5 d |  
| 🔀 Jakarta namespace migration | 2–4 h | automated | ~3 h |  
| 🧩 Dependency reconciliation (Groovy/Spock/cglib/Guava) | 3–5 h | agent-assisted | ~3 h |  
| 🏷️ `ShallowEtagHeaderFilter` API diagnosis | 1–2 h | agent-assisted | ~1 h |  
| 🚨 Logback boot-failure diagnosis | 2–6 h *(presents as "works locally, dies on deploy")* | agent-assisted | ~3 h |  
| 🧪 Standing up Groovy compilation + test gap closure | 3–5 h | agent-assisted | ~2 h |  
| **Total** | **≈ 5–7 engineer-days** | **≈ 16 min wall-clock** | **≈ 4.5–6 days (~85–90 %)** |  
  
- 💡 **Where the real leverage was:** the 10-hop cumulative Spring chain (2.0→2.7→3.0→3.5→4.0) is  
  the single largest saving — hand-walking those minors with their per-version property renames is  
  where upgrade projects usually stall.  
- 🔎 **Where human/agent judgement was irreplaceable:** every one of the four highest-risk items —  
  the ETag signature change, the JDK-25-incompatible Groovy/Spock/cglib residue, the **boot-blocking  
  Logback defect**, and the fact that *no tests were running at all* — fell **outside** recipe  
  coverage. The recipes' 67 minutes is real, but it is the easy 67 minutes.  
- ⚠️ **Caveat:** these are estimates calibrated to this codebase's scope (12 source files, single  
  module). Treat them as order-of-magnitude.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation  
  
- 🌐 **Smoke-test the packaged jar** — *not performed; this cycle's instructions forbid executing the  
  application.* Check `/download/{uuid}` (301), `/download/{uuid}/{filename}` (200), both 304 paths  
  (`If-None-Match`, `If-Modified-Since`), `HEAD`, and `/actuator/health`. Routing, the exact `Location`  
  value, ETag/304 behaviour **and context startup** are now test-covered, so a smoke test is really  
  about **servlet-container and packaging** behaviour only.  
- 🔌 **Exercise a real Tomcat bind.** Startup is proven in a **MOCK** servlet environment; no socket  
  was ever opened. Worth one `@SpringBootTest(webEnvironment = RANDOM_PORT)` run in CI.  
- 🔁 **Re-run `mvn -B clean install` on JDK 25 in CI** and confirm **16/16**. Nothing depends on local  
  state, but a `clean` guard in the pipeline is cheap insurance.  
- 🏷️ **Decide the fate of `Sha512ShallowEtagHeaderFilter`.** It is now behaviour-tested, but remains a  
  **test-scoped class registered as a filter nowhere**. Either wire it in (move to `src/main/java` +  
  a `FilterRegistrationBean`) or delete it. Left in place — deleting a class with passing tests is an  
  owner call, not an upgrade step.  
- 🧾 **Review the non-BOM pins** listed in §2. Nothing deviates from a mandated version, so there is  
  no deviation table.  
- ⚠️ **Review the mixed Boot/non-Boot note** (log line 254). The 8 explicit `org.springframework:*`  
  declarations are now versionless and BOM-managed, so they are consistent — but they are also  
  **redundant**, since `spring-boot-starter-webmvc` pulls all of them transitively. Deletable for clarity.  
  
### 💡 Improvements  
  
- 🗂️ **Replace the `FileSystemStorage` stub before production.** It returns `logback.xml` for *any*  
  UUID and uses `new File(getResource(...).getFile())`, which **fails from a packaged jar**. Fixing the  
  jar case means moving `FilePointer` off `java.io.File` onto a `Resource`/`ByteSource` — an  
  architecture change to a class whose own comment says *"Replace with real implementation"*.  
  **Owner decision, not an upgrade step.** (Masked in tests: the `test` profile substitutes  
  `FileStorageStub`.)  
- 📦 **`spring-boot-maven-plugin` is not declared**, so `mvn package` yields a plain jar, not an  
  executable fat jar. Matches the original build; add it if an executable artifact is wanted.  
- 🔭 **Behavioural changes no build can prove:** trailing-slash matching is no longer automatic  
  (Framework 6+), and Boot 4 moves to **Jackson 3** (`tools.jackson`). Neither affects this codebase —  
  no direct Jackson usage, no trailing-slash routes — but confirm with real clients.  
- 🔐 **Actuator is already at Boot 4's conservative default** (`health` only; no  
  `application.properties` exists). Add `management.endpoints.web.exposure.include` **together with**  
  Spring Security, only if more endpoints are genuinely wanted.  
- 🚀 **Optional Java 25 / Boot 4 modernization deliberately not applied** (out of scope for a  
  build-green cycle): virtual threads (`spring.threads.virtual.enabled=true`), `RestClient`, and  
  replacing `java.util.Date` in `DownloadController`/`ExistingFile` with `java.time.Instant`.  
- 📄 **`README.md` still says "Sample Java 8 Project"** — now inaccurate.  
- 🧊 **Benign noise, no action:** `Sharing is only supported for boot loader classes…` is a JVM CDS  
  notice caused by the Surefire `-javaagent`. The `dependency:analyze` "used undeclared" /  
  "unused declared" warnings are normal for starter-based builds (starters hold no classes;  
  `spock-spring` loads via Spock's extension mechanism).  
- 🗃️ **Caffeine 2.9.3 triggers a terminal-deprecation warning** in the OpenRewrite tooling JVM  
  (`sun.misc.Unsafe::objectFieldOffset … will be removed in a future release`). This is in the  
  **transformer's own toolchain, not this application** — no action for this project, but relevant  
  to the pipeline image.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 16 tests run
- ⏱️ Transformation duration: 20 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 8.336303  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
