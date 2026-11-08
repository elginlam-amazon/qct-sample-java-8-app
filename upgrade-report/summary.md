# Java 21 Upgrade — Iteration 4 Executive Report  
  
---  
  
## 1. Executive Summary  
  
- 🎯 This iteration upgraded **download-server** from Java 8 source-level (Spring Boot 1.2.3.RELEASE) to **Java 21**, replacing a decade-old Spring Boot 1.x stack with Spring Boot 3.5.16 and migrating the Groovy/Spock test infrastructure from the EOL Groovy 2.4 line to Apache Groovy 4.0  
- 🤖 **OpenRewrite** (`com.amazonaws.java.migrate.UpgradeToJava21`) set the compiler target, bumped the compiler plugin version, and eliminated one deprecated JDK API call in application code; it did **not** upgrade the Spring Boot parent (pipeline ran with `springBootTargetVersion=none`)  
- 🔧 **Amazon Kiro CLI** completed the upgrade: Spring Boot 1.2.3 → 3.5.16, full dependency cleanup, Spock/Groovy migration, Spring 6 API fixes, a two-classloader test-fixture defect, and virtual-thread enablement  
- ✅ Post-upgrade verification: **BUILD SUCCESS**, **12 tests run, 0 failures, 0 errors**  
- ✅ Static runtime validation passed: Spring version is governed by the Spring Boot BOM, not by individual pins, and no Java-17-incompatible Spring artefact was detected  
- ⚠️ **Application runtime behaviour is unverified**: the smoke-start tier was skipped (opt-in gate not enabled); only compilation and unit tests have been confirmed  
- 🕐 Transformation completed in **25 minutes**, estimated to save a developer **360 minutes** of manual effort  
  
---  
  
## 2. Application Changes  
  
### `pom.xml`  
- 🔼 **Spring Boot parent** `1.2.3.RELEASE` → `3.5.16` (first version with first-class Java 21 support; Spring Framework 4.x ships an ASM that cannot verify Java 21 class-file version 65)  
- ➕ `java.version` property set to `21`; `spring.version` property removed  
- 🔼 `maven-compiler-plugin` `3.0` → `3.16.0`; `<source>1.8><target>1.8>` replaced with `<release>21>` (OpenRewrite)  
- ➕ `gmavenplus-plugin:3.0.2` added to compile `src/test/groovy` sources (previously the Groovy spec compiled but was never executed)  
- ➕ `maven-surefire-plugin` configured with `**/*Spec.class` include pattern so Spock `*Spec` classes are discovered alongside `*Test` classes  
- 🔼 `spock-core` + `spock-spring`: `1.0-groovy-2.4` → `2.4-groovy-4.0` (JUnit 5 Platform–based; Groovy 2.4 does not run on Java 21)  
- 🔄 `org.codehaus.groovy:groovy-all:2.4.3` → `org.apache.groovy:groovy-all:4.0.31` (Apache relocated the groupId in Groovy 4; declared as `pom` BOM type, `test` scope)  
- 🔼 `com.google.guava:guava` `18.0` → `33.4.0-jre`  
- 🔼 `commons-io:commons-io` `2.4` → `2.17.0`  
- 🗑️ Removed: all explicit `org.springframework:spring-*` version pins (`4.1.6.RELEASE`); all are now managed by the Spring Boot 3.5 BOM  
- 🗑️ Removed: `cglib:cglib-nodep:3.1` (Spring 5+ bundles a repackaged CGLIB internally)  
- 🗑️ Removed: duplicate `org.springframework:spring-test` declaration  
- 🗑️ Removed: `commons-codec` explicit version pin (now BOM-managed)  
- 🗑️ Removed: Spring milestone repository declaration (Spring Boot 3.x artefacts are on Maven Central)  
  
### `src/main/resources/application.properties` *(new file)*  
- ➕ `spring.threads.virtual.enabled=true` — enables Java 21 virtual threads for the Tomcat request executor; appropriate for this I/O-bound download server  
  
### `src/main/resources/logback.xml`  
- 🐛 Fixed: inline anonymous `<appender>` element was missing a required `name` attribute; Logback silently discarded all log output; restructured to a named `CONSOLE` appender with an `<appender-ref>`  
  
### `summary.md` *(new file)*  
- 📄 Migration documentation committed to the repository  
  
---  
  
## 3. Code Changes  
  
**`src/main/java/com/nurkiewicz/download/MainApplication.java`** — OpenRewrite (`PrimitiveWrapperClassConstructorToValueOf`):  
```java  
// before  
Integer temp = new Integer("1234");  
// after  
Integer temp = Integer.valueOf("1234");  
```  
  
**`src/main/java/com/nurkiewicz/download/FileSystemPointer.java`** — deprecated Guava API replaced:  
```java  
// before  
this.tag = Files.hash(target, Hashing.sha512());  
// after  
this.tag = Files.asByteSource(target).hash(Hashing.sha512());  
```  
  
**`src/main/java/com/nurkiewicz/download/ExistingFile.java`** — deprecated Guava `Optional` API bridged to Java `Optional`:  
```java  
// before  
return input.charset()  
    .transform(c -> new MediaType(input.type(), input.subtype(), c))  
    .or(new MediaType(input.type(), input.subtype()));  
// after  
return input.charset()  
    .toJavaUtil()  
    .map(c -> new MediaType(input.type(), input.subtype(), c))  
    .orElse(new MediaType(input.type(), input.subtype()));  
```  
  
**`src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`** — Spring 6 removed the `generateETagHeaderValue(byte[])` override point; updated to the new signature:  
```java  
// before  
protected String generateETagHeaderValue(byte[] bytes) {  
    final HashCode hash = Hashing.sha512().hashBytes(bytes);  
    return "\"" + hash + "\"";  
}  
// after  
protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException {  
    final HashCode hash = Hashing.sha512().hashBytes(inputStream.readAllBytes());  
    return (isWeak ? "W/" : "") + "\"" + hash + "\"";  
}  
```  
  
**`src/test/java/com/nurkiewicz/download/FileExamples.java`** — fixed classloader-isolation defect:  
```java  
// before  
public static final UUID TXT_FILE_UUID = UUID.randomUUID();  
// after  
public static final UUID TXT_FILE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");  
```  
> Spring Boot 3.5's test context uses an isolated classloader for application beans. `FileExamples` was initialised twice — once per classloader — producing two different random UUIDs at runtime, so `FileStorageStub.findFile()` always returned `Optional.empty()` and every test that expected HTTP 200 received HTTP 404 instead. A deterministic UUID compares equal regardless of which classloader holds the class.  
  
---  
  
## 4. Tools Used  
  
- ☕ **Java SDK**: Amazon Corretto 21, target `release 21` (confirmed by `javac [debug parameters release 21]` in post-upgrade-verification.log)  
- 🔄 **OpenRewrite**: `rewrite-maven-plugin 6.42.0`, recipe `com.amazonaws.java.migrate.UpgradeToJava21` — applied compiler-plugin upgrade, `java.version` property, and `PrimitiveWrapperClassConstructorToValueOf` in `MainApplication.java`  
- 🤖 **Amazon Kiro CLI**: `kiro-cli 2.35.1`, model `claude-sonnet-4.6` — performed Spring Boot parent upgrade, dependency cleanup, Groovy/Spock migration, Spring 6 API fixes, classloader defect diagnosis and resolution, and carry-over items from iteration 3  
  
---  
  
## 5. Dependencies  
  
### Added  
| Dependency | Version | Scope | Notes |  
|---|---|---|---|  
| `org.apache.groovy:groovy-all` | `4.0.31` | `test` (pom BOM) | Test-only; no classloader-conflict risk with runtime |  
| `org.codehaus.gmavenplus:gmavenplus-plugin` | `3.0.2` | build plugin | Compile-time only; not bundled in the application |  
| `org.apache.maven.plugins:maven-surefire-plugin` | BOM-managed | build plugin | Compile-time only; not bundled in the application |  
  
> 🟢 No added dependency is packaged in the application at compile or runtime scope; no classloader-conflict risk applies.  
  
### Removed  
| Dependency | Version | Reason |  
|---|---|---|  
| `org.springframework:spring-test` | `4.1.6.RELEASE` | Now transitively provided by `spring-boot-starter-test` |  
| `cglib:cglib-nodep` | `3.1` | Spring 5+ bundles a repackaged CGLIB; standalone artefact conflicts |  
| `org.springframework:spring-beans` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-context` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-context-support` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-webmvc` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-web` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-aop` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-core` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.springframework:spring-expression` | `4.1.6.RELEASE` | Now BOM-managed via Spring Boot parent |  
| `org.codehaus.groovy:groovy-all` | `2.4.3` | Replaced by `org.apache.groovy:groovy-all:4.0.31` |  
  
### Upgraded  
| Dependency | From | To |  
|---|---|---|  
| `org.springframework.boot:spring-boot-starter-parent` | `1.2.3.RELEASE` | `3.5.16` |  
| `org.apache.maven.plugins:maven-compiler-plugin` | `3.0` | `3.16.0` |  
| `org.springframework.boot:spring-boot-starter-web` | `1.2.3.RELEASE` | BOM-managed (3.5.16) |  
| `org.springframework.boot:spring-boot-starter-actuator` | `1.2.3.RELEASE` | BOM-managed (3.5.16) |  
| `org.springframework.boot:spring-boot-starter-test` | `1.2.3.RELEASE` | BOM-managed (3.5.16) |  
| `org.spockframework:spock-core` | `1.0-groovy-2.4` | `2.4-groovy-4.0` |  
| `org.spockframework:spock-spring` | `1.0-groovy-2.4` | `2.4-groovy-4.0` |  
| `com.google.guava:guava` | `18.0` | `33.4.0-jre` |  
| `commons-io:commons-io` | `2.4` | `2.17.0` |  
| `commons-codec:commons-codec` | `1.10` | BOM-managed (3.5.16) |  
  
---  
  
## 6. Risks  
  
- ⚠️ **Runtime behaviour unverified**: the smoke-start tier was skipped (`Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).`). It is unknown whether the application starts, whether the embedded Tomcat binds its port, or whether the `/actuator/health` endpoint responds. Unit tests pass, but end-to-end behaviour requires a manual or CI smoke test.  
- ⚠️ **Groovy version micro-mismatch**: `groovy-all:4.0.31` (declared as the BOM) resolves its submodule JARs at `4.0.32` because `spock-core:2.4-groovy-4.0` carries a direct `groovy:4.0.32` dependency that wins Maven's nearest-definition resolution. Both are from the same Groovy 4.0 patch line; all 12 tests pass. Align both declarations to `4.0.32` in the next routine update to remove the mismatch from the dependency tree.  
- ⚠️ **Mockito self-attach warning**: the post-upgrade-verification log records `Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK.` Tests pass today but Mockito will require explicit `-javaagent` configuration in a future JDK release.  
- ⚠️ **Groovy template auto-configuration triggered**: the log shows `GroovyTemplateAutoConfiguration: Cannot find template location: classpath:/templates/`. Groovy is on the classpath (test dependency) and Spring Boot auto-configuration picks it up. This is benign but adds startup noise; set `spring.groovy.template.check-template-location=false` or exclude `GroovyTemplateAutoConfiguration` if desired.  
- ⚠️ **Virtual threads and pinning**: `ThrottlingInputStream` delegates to Guava's `RateLimiter.acquire()`, which internally calls `Thread.sleep()`. On Java 21, `Thread.sleep()` on a virtual thread yields (does not pin the carrier thread), so the current code is safe. Any future addition of a `synchronized` block around I/O in this path would pin a carrier thread and negate the benefit of virtual threads.  
- ⚠️ **Actuator endpoint path changed**: Spring Boot 3.x moved the health endpoint from `/health` to `/actuator/health`. Any monitoring probe, load-balancer health-check, or Kubernetes liveness/readiness configuration still pointing at the old path will start failing once the application is deployed.  
- ℹ️ **javax namespace**: the application has no `javax.*` EE imports; no `javax → jakarta` migration was required. All `org.springframework.*` usage is compatible with Spring Framework 6.x.  
- ℹ️ **Spring Boot upgrade not automated**: OpenRewrite ran with `springBootTargetVersion=none`; the entire Spring Boot parent upgrade was performed by the Kiro CLI. Set `springBootTargetVersion=3.5` in the pipeline configuration to automate this step in future iterations.  
  
---  
  
## 7. Time Savings  
  
- ⏱️ **Transformation duration**: 25 minutes  
- 💰 **Estimated developer time saved**: 360 minutes (6 hours)  
- 📊 The largest portion of the saving comes from diagnosing the classloader-isolation defect (~150 min estimated manual effort): Spring Boot 3.5's isolated test context classloader caused `FileExamples` to initialise twice, producing two different random UUIDs, which caused 11 of 12 tests to return HTTP 404 instead of 200. The Kiro CLI traced this through bytecode inspection and resolved it by replacing `UUID.randomUUID()` with a fixed deterministic UUID.  
  
---  
  
## 8. Next Steps  
  
- 🔲 **Enable smoke-start validation**: run `export ENABLE_RUNTIME_VALIDATION=true` in the pipeline to verify the application boots, the embedded Tomcat starts, and `/actuator/health` returns HTTP 200 before marking the iteration complete  
- 🔲 **Update monitoring probes**: change any health-check URL from `/health` to `/actuator/health` in load-balancer rules, Kubernetes liveness/readiness probes, and monitoring dashboards  
- 🔲 **Configure Mockito as a Java agent**: add `mockito-core` as a `-javaagent` in the Surefire `<argLine>` to eliminate the self-attach warning before it becomes a hard failure in a future JDK release  
- 🔲 **Align Groovy submodule version**: update `org.apache.groovy:groovy-all` from `4.0.31` to `4.0.32` to match the version already resolved transitively via `spock-core`, removing the mismatch from the dependency tree  
- 🔲 **Suppress spurious Groovy template warning**: add `spring.groovy.template.check-template-location=false` to `application.properties` or exclude `GroovyTemplateAutoConfiguration` since the project does not use Groovy templates  
- 🔲 **Automate Spring Boot upgrade in pipeline**: set `springBootTargetVersion=3.5` (or current latest) so OpenRewrite handles the `spring-boot-starter-parent` version bump in future iterations, eliminating the need for Kiro CLI to perform it manually  
- 🔲 **Load-test virtual-thread behaviour**: exercise the throttled download path under concurrent load to confirm `ThrottlingInputStream`'s `RateLimiter.acquire()` parks virtual threads correctly and does not introduce carrier-thread pinning

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 12 tests run
- ⏱️ Transformation duration: 25 minute(s)
- ⏳ Estimated time saved: 360 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 44.58  
Includes 0.54 for job setup (startup smoke test and knowledge-base indexing).  
