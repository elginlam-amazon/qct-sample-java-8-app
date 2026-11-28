# 📊 Executive Report — Java Version Upgrade  
  
**Project:** `com.nurkiewicz.download:download-server` (Spring Boot download server, Maven, single module)  
**Iteration:** 67 · **Branch:** `main-upgrade-itr-67` · **Base commit:** `73556be`  
**Engine:** `OPENREWRITE_WITH_CLAUDE_CLI` · **Date:** 2026-11-28  
  
---  
  
## 1. 🎯 Executive Summary  
  
- ✅ **The upgrade completed successfully and the Spring upgrade did reach its target.** The application moved from **Java 8 / Spring Boot 1.2.3** to **Java 25 / Spring Boot 4.0.10**, resolving to **Spring Framework 7.0.11** on **Jakarta EE 11**. Final state: `mvn -B clean install` passes with **zero compilation errors and 18/18 tests green**.  
- 🤖 Work ran in two phases: **OpenRewrite** applied the mechanical version/parent/dependency moves, then the **Claude CLI agent** acted as backstop for everything the recipes could not reach — one hard compilation error, one build blocker, and a stale dependency set that no recipe touched.  
- ⚠️ **Accuracy note on the Spring verification gate:** the log contains **no** `Verified: effective Spring Framework version…` line and **no** `WARNING: …still < 5.3` line (grep count = **0** for both). That gate belongs to the *non-Boot Spring 5.3 / javax* path, which is not this project's path. The Spring outcome was therefore confirmed from the **OpenRewrite data tables** (`UpgradeParentVersion → 4.0.x` and `ChangeDependency spring-boot-starter-web → spring-boot-starter-webmvc` both recorded as applied) **plus the resolved effective POM** (Boot 4.0.10, Framework 7.0.11). This is a **complete** Spring upgrade — not an incomplete one.  
- 🔍 **The most consequential finding was not a compilation error.** The project's only real test coverage — 12 Spock specs — had **never been compiled or executed**, in the original build or after the recipes. Two independent pre-existing causes: no Groovy compiler was configured, and the class name (`DownloadControllerSpec`) did not match surefire's default `*Test` includes. The pre-upgrade baseline build confirms this: `surefire:2.17` ran and reported **zero tests**.  
- 📈 Coverage went from **0 tests actually executing → 18 passing**, including real embedded-Tomcat startup. The upgrade is now genuinely verified rather than merely compiling.  
  
---  
  
## 2. 🔧 Application Changes  
  
### Phase A — OpenRewrite (automated)  
  
- 📦 **Net textual edits: 3 files** — `pom.xml`, `MainApplication.java`, `DownloadController.java`. (The data tables record 279 + 505 recipe-attribution rows across 13/12 files, but those are nested parent→leaf attributions plus non-textual Java-version marker updates on every source; the log's own "Changes have been made to…" summary lists only these 3.)  
- ⬆️ `spring-boot-starter-parent` **1.2.3.RELEASE → 4.0.x** (resolved **4.0.10**).  
- 🔀 `spring-boot-starter-web` → **`spring-boot-starter-webmvc`** (the Boot 4.0 starter rename).  
- 🧹 Stripped hardcoded `4.1.6.RELEASE` / `1.2.3.RELEASE` pins so the Boot BOM manages them; removed a duplicate `spring-test` declaration that had been emitting a malformed-POM warning.  
- ⚙️ `maven-compiler-plugin` **3.0** (`source/target 1.8`) → **3.16.0** (`release 25`).  
- ➕ Added `jakarta.servlet-api`, the byte-buddy-agent surefire `argLine`, and `maven-dependency-plugin:properties`.  
  
### Phase B — Claude CLI agent (backstop)  
  
- 🚧 **Build blocker:** removed the `http://repo.spring.io/milestone/` repository. Maven's `maven-default-http-blocker` rejects plain-HTTP repos, which failed the build *before it could even read the POM*. The recipes had already logged `Failed to access maven repository http://repo.spring.io/milestone/` but left the declaration in place.  
- 🛠️ **The one compilation error** (detail in §4).  
- 🧬 **Dependency reconciliation** the recipes did not attempt (detail in §4).  
- 🧪 **Revived the dead test suite** and added 6 new verification tests (detail in §4).  
- 🐛 **Two latent defects** surfaced by newer library versions (detail in §4).  
  
---  
  
## 3. 🧰 Tools Used  
  
| Tool | Version / Detail |  
| --- | --- |  
| ☕ **Java SDK** | **Amazon Corretto 25.0.5.1.2** (OpenJDK `25.0.5.1`, build `25.0.5.1+2-LTS`) |  
| 🏗️ **Maven** | **3.9.8** via project wrapper (`mvnw`) |  
| 🔁 **OpenRewrite plugin** | `rewrite-maven-plugin` **6.42.0** (`rewrite:6.42.0:run`) |  
| 📜 **Recipe 1** | `com.amazonaws.java.migrate.UpgradeToJava25` → delegates through `UpgradeToJava17` → `Java8toJava11` → `UpgradeBuildToJava21/25` |  
| 📜 **Recipe 2** | `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` → cumulative `2.0 → 2.7 → 3.0 → 3.5 → 4.0`, incl. `UpgradeSpringFramework_5_0…6_0`, `JakartaEE10`, `JavaxServletToJakartaServlet`, `SpringBoot2JUnit4to5Migration` |  
| 🤖 **Claude CLI** | **Claude Code**, model **`global.anthropic.claude-opus-5`** (canonical `claude-opus-5`, provider **bedrock**, 1,000,000-token context, 64,000 max output) |  
| 📚 **Knowledge base** | `/app/knowledge` mounted read-only via `--add-dir` (38 migration docs) |  
| 💾 **Container** | 4 GB limit · peak **1.05 GB** · **0 OOM kills** |  
  
- 🧮 **Agent session cost/effort:** 3 sessions, **77 turns** total, **$4.93** cumulative, 42,076 output tokens (18,228 thinking), 5.03 M cache reads, **0 permission denials, 0 safety stops, 0 subagents**.  
  
---  
  
## 4. 💻 Code Changes  
  
### 🔴 Compilation error fixed (1)  
  
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` — Spring Framework 7 changed the ETag hook from `generateETagHeaderValue(byte[])` to **`generateETagHeaderValue(InputStream, boolean isWeak)`**, so the `@Override` no longer resolved (*"method does not override or implement a method from a supertype"*). Reimplemented against the new signature, preserving the SHA-512 digest and quoted format, and honouring the new `isWeak` flag with the standard `W/` prefix.  
  
### 🧬 Dependency reconciliation (recipes left this entirely untouched)  
  
- 🚫 **Groovy 2.4 cannot load on any JDK past 8.** Dropped the `dependencyManagement` pin of `org.codehaus.groovy:groovy-all:2.4.16`; switched to **`org.apache.groovy:groovy`**, versionless so the BOM manages it (**5.0.8**).  
- ⬆️ `spock-core` / `spock-spring` **1.0-groovy-2.4 → 2.5-groovy-5.0** (JUnit 5 Platform based). Removed the now-stale `groovy-all` and `junit:junit` exclusions.  
- 🗑️ Removed **`cglib:cglib-nodep:3.1`** — Spock 1.x-only (Spock 2 uses ByteBuddy), unreferenced in source, emits pre-Java-7 bytecode.  
- ⬆️ `commons-io` **2.4 → 2.22.0**; `guava` **29.0-jre → 33.8.0-jre** (neither is BOM-managed, so both are explicitly pinned).  
- ✅ **BOM discipline verified, not assumed:** of **1,774** BOM-managed artifacts, every one the POM declares is **versionless** and takes the BOM version (`jakarta.servlet-api` 6.1.0, `commons-codec` 1.19.0, `groovy` 5.0.8, all 8 `org.springframework:*` at 7.0.11). Only the 3 artifacts the BOM genuinely does not manage carry pins.  
  
### 🧪 Test infrastructure — 0 executing → 18 passing  
  
- ➕ Added **`gmavenplus-plugin` 5.1.0** (`compileTests`) so `src/test/groovy` compiles at all.  
- ➕ Added `**/*Spec.java` to surefire `<includes>` so the `DownloadControllerSpec` class is actually discovered.  
- ✅ **12 Spock specs** now compile against Groovy 5.0.8 and pass — covering ETag caching, `If-Modified-Since`, HEAD handling, `Content-Length`, `Content-Type` and 404.  
- ➕ **`ApplicationStartupTest`** (3 tests) — `@SpringBootTest(RANDOM_PORT)` boots **real embedded Tomcat 11.0.27** on Java 25 and drives it over real HTTP via the JDK `HttpClient`, asserting `/actuator/health` → `200 "status":"UP"`, an end-to-end download with correct `ETag`, and a 404. Note `TestRestTemplate` **no longer exists** in `spring-boot-test` 4.0.10, hence the JDK client.  
- ➕ **`Sha512ShallowEtagHeaderFilterTest`** (3 tests) — pins the strong/weak ETag format across the signature migration above.  
- 🔒 No test was deleted, disabled, skipped or weakened at any point.  
  
### 🐛 Latent defects surfaced by newer versions  
  
- `src/main/resources/logback.xml` declared `<appender>` **nested inside `<root>`, with no `name` and no `appender-ref`** — so the root logger had **no appender at all**. Logback 1.5 reports it as `ERROR … Missing attribute [name]`. Rewrote to the canonical form, preserving the exact pattern and every logger level.  
- `FileSystemPointer` used Guava's `Files.hash(File, HashFunction)`, deprecated and marked for removal → switched to the documented `Files.asByteSource(file).hash(…)`. Identical semantics; clears the build's last deprecation warning.  
  
### ✅ Final verification  
  
- 🟢 `mvn -B clean install` green across **3 consecutive clean runs**, 18/18 each time.  
- 🔗 **Byte-identical 92-artifact dependency tree** between runs (matching checksums) — safe for CI.  
- 🧭 **Zero `javax.*` artifacts**; 4 jakarta APIs, all BOM-sourced. **Zero `javax.*` *and* zero `jakarta.*` references in all 16 source files** — this app only touches Spring, JDK, Guava, SLF4J and commons-io APIs, so there was no namespace migration to perform in either direction.  
- 🔬 **Spock 2.5 on JUnit Platform 6** is an undocumented pairing (Spock 2.5 publishes against `junit-bom` 5.14). Verified empirically: a **single unified JUnit 6.0.3** resolves with no split versions, and both engines run in one surefire pass (12 Spock + 6 Jupiter).  
  
---  
  
## 5. ⏱️ Time Savings Estimate  
  
### Automated wall-clock  
  
- ⚡ OpenRewrite: **~1 m 55 s** (36.5 s Java 25 + 1 m 18 s Spring Boot 4.0)  
- ⚡ Claude CLI agent: **~13 m 56 s** across 3 sessions (7 m 41 s + 4 m 46 s + 1 m 29 s)  
- ⚡ **Total ≈ 16 minutes**  
  
### Manual-equivalent effort  
  
| Work item | Manual estimate | Basis |  
| --- | --- | --- |  
| 📜 OpenRewrite mechanical changes | **3 h 32 m** | **Self-reported by the tool** (1 h + 2 h 32 m in the data tables) |  
| 🚧 Diagnose HTTP-repo build blocker | ~0.5 h | Opaque failure — fails before the POM is read |  
| 🛠️ `ShallowEtagHeaderFilter` API migration | ~1 h | Locate new signature, preserve digest + weak semantics |  
| 🧬 Groovy/Spock/cglib reconciliation | ~3–4 h | Groovy↔Spock↔JUnit compatibility-matrix research |  
| 🧪 Find + fix the silently dead test suite | ~2–3 h | The filename/classname mismatch is genuinely hard to spot |  
| ➕ New verification tests | ~2–3 h | Incl. discovering `TestRestTemplate` was removed in Boot 4 |  
| 🐛 Latent logback + Guava defects | ~0.75 h | — |  
| ✅ BOM audit + repeatability verification | ~1 h | — |  
| **Total** | **≈ 14–17 h** | ≈ **2 working days** |  
  
- 💡 **Net saving ≈ 14–17 hours of engineer time compressed into ~16 minutes** — roughly a **55–65× speed-up**, at **$4.93** of model cost.  
- ⚖️ **Caveat on attribution:** the 3 h 32 m is OpenRewrite's own estimate for 3 edited files; the remaining ~10–13 h is *this report's* estimate of the agent-phase work, which is where the non-mechanical difficulty actually sat. Both figures are estimates, not measurements. The 16-minute wall-clock is measured.  
  
---  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation already completed (no action needed)  
  
- 🟢 Zero compilation errors; 18/18 tests passing; repeatable across 3 clean runs with a stable dependency tree.  
- 🌐 **Real servlet-container startup is test-covered** — Tomcat 11.0.27 boots on Java 25 and `/actuator/health` returns `UP`.  
- 🧭 Namespace consistency confirmed: no `javax.*` artifacts, no mixed stack, no stale versions.  
- 📋 **Jackson 3** (`tools.jackson`, new in Boot 4) needed no action — no direct Jackson usage, and the serialization path is now exercised by the health assertion.  
- ↪️ **Trailing-slash matching** (removed in Framework 6+) assessed as non-impacting — no route depends on it.  
  
### ⚠️ Validation still outstanding  
  
- 📦 **Fat-jar packaging is the one genuinely unverified area.** `spring-boot-maven-plugin` is **not declared**, so `install` produces a plain, non-executable jar. This matches the original build exactly, so it was deliberately left alone — but add the plugin with the `repackage` goal if an executable artifact is expected.  
- 🔁 **Re-run `mvn -B clean install` on JDK 25 in CI** to confirm 18/18 in a clean CI environment, not just this container.  
- 🔭 **Confirm trailing-slash behaviour against real client traffic** before release — no test can prove what external callers actually send.  
  
### 💡 Improvement recommendations (owner decisions, deliberately not actioned)  
  
- 🗂️ **Replace the `FileSystemStorage` stub before production.** Its own javadoc says *"Replace with real implementation"*: it returns `logback.xml` for **any** UUID and builds its file via `new File(getResource(…).getFile())`, which **cannot work from inside a packaged jar** and breaks on paths with spaces. A real fix means moving `FilePointer` off `java.io.File` onto a Spring `Resource` / Guava `ByteSource` — an architecture change, not a migration step. ⚠️ **Masked in tests** because the `test` profile substitutes `FileStorageStub`. This interacts directly with the fat-jar item above.  
- 🏷️ **Decide the fate of `Sha512ShallowEtagHeaderFilter`.** Now behaviour-tested, but still a **test-scoped class registered as a filter nowhere**, living in a `org.springframework.*` package purely to reach a protected method. Either promote it to `src/main/java` with a `FilterRegistrationBean`, or delete it.  
- 🧵 **Consider `spring.threads.virtual.enabled=true`** on JDK 25 for this blocking, download-heavy workload — **not** enabled, because `ThrottlingInputStream` blocks on a Guava `RateLimiter` and carrier-thread pinning needs a load test first.  
- 🧽 **Prune 8 redundant `org.springframework:*` declarations** — all supplied transitively by `spring-boot-starter-webmvc`, versionless and BOM-managed, hence harmless. Purely cosmetic. ⚠️ `spring-context-support` **must stay** — it is genuinely not transitive.  
- 🧹 **`commons-codec` is declared but unused** anywhere in `src/` — removal candidate.  
- 🔮 **Watch the Spock/JUnit pairing on future bumps.** If Boot's `junit-bom` and Spock diverge, the escape hatches are pinning `junit-bom` explicitly or porting the spec to JUnit 5 + `MockMvcTester`.  
- 📝 **No `application.properties`/`yml` exists**, so there were no Boot 3.x/4.0 property renames to apply — nothing to verify on that front.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 18 tests run
- ⏱️ Transformation duration: 20 minute(s)
- ⏳ Estimated time saved: 17 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 7.418456  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
