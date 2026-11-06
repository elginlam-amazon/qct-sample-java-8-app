# Executive Report: Java 21 Migration — download-server  
  
## Executive Summary  
  
The `download-server` Spring Boot application has been successfully migrated to Java 21 with all 12 tests passing and zero build errors. The migration was completed in two phases: an automated OpenRewrite pass that set the Java compiler target to 21 and applied one static-analysis fix, followed by an extensive manual phase (Claude CLI) that performed the full Spring Boot upgrade from 1.2.3 to 3.5.16, modernized the test stack, and resolved all resulting compilation and runtime errors. The Spring Boot upgrade was **not performed by OpenRewrite** — it was explicitly skipped (`springBootTargetVersion=none`) and completed manually.  
  
---  
  
## Application Changes  
  
- ✅ **Java version**: 8 → 21 (compiler release flag set to 21)  
- ✅ **Spring Boot**: 1.2.3.RELEASE → 3.5.16 (Spring Framework 6.2.x) — **manual upgrade only; OpenRewrite skipped this step**  
- ✅ **Groovy/Spock test stack**: Groovy 2.4.3 / Spock 1.0-groovy-2.4 → Groovy 4.0 / Spock 2.4-groovy-4.0 (Java 21 requires Groovy 4+)  
- ✅ **commons-io**: 2.4 → 2.22.0  
- ✅ **Virtual threads**: enabled via `spring.threads.virtual.enabled=true` (Spring Boot 3.2+ feature)  
- ✅ **Deprecated API removed**: `com.google.common.io.Files.hash()` replaced with `Files.asByteSource().hash()`  
- ✅ **Build**: `mvn verify` passes — 12 tests, 0 failures, 0 errors  
  
---  
  
## Tools Used  
  
| Tool | Version | Role |  
|------|---------|------|  
| Amazon OpenRewrite | rewrite-maven-plugin 6.42.0 | Automated Java 21 recipe application |  
| OpenRewrite recipe | `com.amazonaws.java.migrate.UpgradeToJava21` | Compiler version + static-analysis fixes |  
| Claude CLI | 2.1.266 | Automated build-fix and manual upgrade agent |  
| Claude model | `global.anthropic.claude-sonnet-4-6` | LLM driving Claude CLI |  
| Maven | (project wrapper `./mvnw`) | Build system |  
| Java SDK | 21 | Compile and test runtime |  
  
---  
  
## Code Changes  
  
### Applied by OpenRewrite (automated)  
  
- **`pom.xml`**: set `java.version=21`; upgraded `maven-compiler-plugin` to 3.16.0; configured `<release>${java.version}</release>`  
- **`src/main/java/com/nurkiewicz/download/MainApplication.java`**: replaced `new Integer("1234")` with `Integer.valueOf("1234")` (recipe: `PrimitiveWrapperClassConstructorToValueOf`)  
  
> ⚠️ Spring Boot upgrade was **not applied by OpenRewrite** — log shows `Skipping Spring Boot upgrade: not requested (springBootTargetVersion=none)`.  
  
### Applied manually by Claude CLI  
  
- **`pom.xml`**:  
  - Spring Boot parent: `1.2.3.RELEASE` → `3.5.16`  
  - Removed: `spring.version` property, Spring milestones repository, `cglib-nodep:3.1`, individual Spring Framework 4.1.6.RELEASE dependency overrides, duplicate `spring-test`  
  - Replaced: `groovy-all:2.4.3` + `spock-*:1.0-groovy-2.4` with `spock-*:2.4-groovy-4.0`  
  - Added: `junit-platform-launcher` (test scope), `gmavenplus-plugin:3.0.2`, Surefire `**/*Spec*` include, `-XX:+EnableDynamicAgentLoading` argLine  
  - Upgraded: `commons-io` 2.4 → 2.22.0  
- **`src/main/java/com/nurkiewicz/download/FileSystemPointer.java`**: replaced deprecated `Files.hash(file, fn)` with `Files.asByteSource(file).hash(fn)`  
- **`src/main/resources/logback.xml`**: fixed Logback 1.3+ format (`<appender>` moved to configuration level; referenced via `<appender-ref>`)  
- **`src/main/resources/application.properties`** *(new)*: added `spring.threads.virtual.enabled=true`  
- **`src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`**: updated overridden method from `generateETagHeaderValue(byte[])` (Spring 4.x) to `generateETagHeaderValue(InputStream, boolean) throws IOException` (Spring 5+/6+ API)  
- **`src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy`**: replaced `@ContextConfiguration` + `@WebAppConfiguration` + manual `MockMvcBuilders` setup with `@SpringBootTest(webEnvironment=MOCK)` + `@AutoConfigureMockMvc` + `@Autowired MockMvc`  
- **`src/test/java/com/nurkiewicz/download/FileExamples.java`**: replaced `UUID.randomUUID()` with fixed UUIDs (`UUID.fromString("11111111-...")`) to prevent classloader-isolation failures under Surefire 3.x JUnit Platform  
  
---  
  
## Time Savings Estimate  
  
| Phase | Estimated manual effort | Automated by |  
|-------|------------------------|--------------|  
| Java 21 compiler target + `PrimitiveWrapperClassConstructorToValueOf` | ~50 min | OpenRewrite (`com.amazonaws.java.migrate.UpgradeToJava21`) — per log |  
| Spring Boot 1.x → 3.5 upgrade, Groovy/Spock stack, test framework, API fixes | ~4–6 hours | Claude CLI (`global.anthropic.claude-sonnet-4-6`) |  
| **Total estimated savings** | **~5–7 hours** | |  
  
---  
  
## Next Steps  
  
- 🔲 **Spring Boot upgrade was not part of the automated OpenRewrite pass** — if the pipeline should automate this in future runs, set `springBootTargetVersion=3.5` (or current latest) rather than `none`  
- 🔲 **Guava 29.0-jre** is pinned in `pom.xml`; Spring Boot 3.5 BOM provides a newer version — consider removing the explicit pin and letting the BOM manage it  
- 🔲 **Actuator endpoint**: health check is now at `/actuator/health` (changed from `/health` in Spring Boot 1.x) — update any monitoring or deployment probes accordingly  
- 🔲 **Virtual threads** are enabled; validate under load that downstream resources (file I/O, any future database connections) are non-blocking or pinning-safe

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 21 minute(s)
- ⏳ Estimated time saved: 10 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Claude CLI usage

💳 Estimated Claude CLI usage (USD): 4.942779  
