# Java 25 / Spring Boot 4.0 Upgrade — Cycle Summary (iteration 68)

## Result

`mvn -B clean install` → **BUILD SUCCESS**, zero compilation errors, **18/18 tests pass**,
stable across **3 consecutive clean runs**.

Final stack: Java 25 (Corretto 25.0.5.1) · Spring Boot 4.0.10 · Spring Framework 7.0.11 ·
Jakarta EE 11 (servlet-api 6.1.0) · Tomcat 11.0.27 · Groovy 5.0.8 · Spock 2.5 · JUnit 6.0.3 · logback 1.5.38.

> Note on iteration numbering: each iteration restarts from the OpenRewrite output, so the
> previous cycle's added tests were not present in this working tree and were re-established here.
> The Spring 7 filter fix below was derived independently this cycle and came out **byte-identical**
> to iteration 67's — a useful convergence signal on the correct fix.

## Compilation errors fixed

### `Sha512ShallowEtagHeaderFilter` — removed Spring hook method
`src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java:8`

> `method does not override or implement a method from a supertype`

Spring Framework 7 changed the `ShallowEtagHeaderFilter` extension point from
`generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean isWeak)`.
Ported the override to the new signature, preserving the SHA-512-over-content ETag behaviour
and additionally honouring the new `isWeak` flag with the standard `W/` prefix.

**This was the only compilation error in the project.**

## Two silent problems a green build would have hidden

### 1. The test suite was not running at all

The Spock specification `src/test/groovy/.../DownloadControllerTest.groovy` (class
`DownloadControllerSpec`) was **never compiled or executed** — in this cycle *or before the
upgrade*. The pom has never had a Groovy compiler plugin, so `src/test/groovy` was ignored and
surefire reported zero tests. The upgrade did not break this; it was pre-existing.

Wired up rather than left dormant:
- Added `gmavenplus-plugin:5.1.0` with the `compileTests` goal to compile `src/test/groovy`.
- Added `**/*Spec.java` to the surefire `<includes>` — the compiled class is
  `DownloadControllerSpec`, matching none of surefire's default `*Test*` patterns. All four
  default patterns are listed explicitly alongside it so nothing is silently dropped.

No test was deleted, skipped, disabled or weakened.

### 2. `logback.xml` was rejected by logback 1.5.x

`src/main/resources/logback.xml` nested an **unnamed** `<appender>` directly inside `<root>`:

> `ERROR in ch.qos.logback.core.joran.action.AppenderAction - Missing attribute [name]. See element [appender] near line 9`

The appender was silently discarded, so the application produced **no console output at all**.
Declared a top-level `<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">`
and referenced it from `<root>` via `<appender-ref ref="CONSOLE"/>`, preserving the original
encoder pattern verbatim.

## Dependency reconciliation

OpenRewrite bumped the parent to Boot 4.0.10 / Java 25 but left a javax-era,
Java-25-incompatible test and runtime dependency set behind:

| Item | Before | After | Reason |
| --- | --- | --- | --- |
| Groovy | `org.codehaus.groovy:groovy-all:2.4.16`, **compile** scope, pinned via `dependencyManagement` | `org.apache.groovy:groovy`, **test** scope, BOM-managed (5.0.8) | Groovy 2.4 cannot run on Java 25. Moved to the modern coordinate and let the Boot BOM manage the version. Scoped to `test` because no main source references Groovy; this also avoids pulling `groovy-templates`, which would activate `GroovyTemplateAutoConfiguration` and crash startup on JDK 17+ with `InaccessibleObjectException`. |
| Spock | `spock-core` / `spock-spring:1.0-groovy-2.4` | `2.5-groovy-5.0` (via `${spock.version}`) | Spock 1.0 is JUnit-4-based and targets Groovy 2.4. Spock 2.5 runs on the JUnit Platform, which the Boot 4 BOM already provides. Dropped the now-meaningless `groovy-all` and `junit` exclusions. |
| `cglib:cglib-nodep:3.1` | present, compile scope | **removed** | Zero references in `src/`. Existed only for Spock 1.x class mocking; Spock 2.x uses ByteBuddy. |
| `spring-test` | declared separately **and** excluded from `spring-boot-starter-test` | **both removed** | A Spring Boot 1.2-era workaround that had become a no-op. `starter-test` now supplies `spring-test` 7.0.11 directly (verified in the dependency tree). |
| `commons-io` | 2.4 | 2.22.0 | Stale (2014); only `FileUtils.ONE_MB` is used, so behaviour-neutral. |
| `guava` | 29.0-jre | 33.8.0-jre | Stale. `MediaType.charset().transform(..).or(..)` and `Files.hash` still compile unchanged. |
| `<spring.version>4.1.1.RELEASE</spring.version>` | present | **removed** | Dead property — nothing referenced it. Left in place it would mislead readers into thinking Spring 4.1 was in play. |
| `<repositories>` `http://repo.spring.io/milestone/` | present | **removed** | Plaintext HTTP (blocked by default in modern Maven) and unnecessary — everything resolves from Maven Central. |

## Test coverage added

Converted one-shot manual verification into repeatable tests (**12 → 18**):

- **`ApplicationStartupTest`** (3 tests) — `@SpringBootTest(webEnvironment = RANDOM_PORT)` boots the
  app on a **real embedded Tomcat**, which the MockMvc specs never do. Asserts
  `/actuator/health` → 200 `"status":"UP"`, a real streamed download (body + SHA-512 `ETag`), and
  404 for an unknown file. This is the test that proves the upgraded stack actually starts, and it
  also exercises the Jackson 3 (`tools.jackson`, new in Boot 4) serialization path.
- **`Sha512ShallowEtagHeaderFilterTest`** (3 tests) — pins the ETag format across the Spring 7
  signature change: strong ETag, `W/`-prefixed weak ETag, and full-stream consumption at 8 KiB.

## Verification performed

- `mvn -B clean install` on JDK 25 — BUILD SUCCESS, 18/18, jar installed. **Repeated 3×**, identical results.
- `mvn dependency:tree -Dverbose` — no version conflicts; `spring-test` 7.0.11, `assertj` 3.27.7.
- All Spring artifacts 7.0.11 / Boot 4.0.10; all Jakarta artifacts on the EE 11 line. No stale versions.
- `grep -rn "javax\." src/` → **no matches**. No javax EE API; no mixed javax/jakarta artifacts.
- Startup log clean: `Spring Boot v4.0.10, Spring v7.0.11`, `Tomcat/11.0.27`, both
  `DownloadController` mappings registered. No `ClassFormatException` / ASM "unsupported class file
  major version" — Spring Boot 4 / Spring 7 parse Java 25 bytecode correctly.
- Custom logback pattern confirmed rendering in the startup log (proves the appender fix).
- Ad-hoc HTTP check against a bounded manual boot: `GET /download/{uuid}` → `301` with the expected
  `Location`. (The 200/404 paths are now covered by `ApplicationStartupTest` instead.)
- No background processes left running — all boot checks were bounded and self-terminating.

## Next steps

Non-blocking. Nothing here blocks the build, the tests, or startup.

### Validation still outstanding

1. **Fat-jar packaging is the one genuinely unverified area.** `spring-boot-maven-plugin` is **not
   declared**, so `install` produces a plain, non-executable jar. This matches the original build
   exactly, so it was deliberately left alone. ⚠️ Note the interaction with item 4 below: adding
   `repackage` would surface a latent bug, because `FileSystemStorage` resolves its file via
   `new File(getResource(…).getFile())`, which **cannot work from inside a packaged jar**. Fix the
   storage implementation *before* or *together with* enabling fat-jar packaging, not after.
2. **Re-run `mvn -B clean install` on JDK 25 in CI** to confirm 18/18 in a clean CI environment
   rather than only in this container (local `~/.m2` is warm here).
3. **Confirm trailing-slash behaviour against real client traffic** before release. Framework 6+
   removed trailing-slash matching, so `/download/{uuid}/` no longer matches `/download/{uuid}`.
   Assessed as non-impacting — no route depends on it — but no test can prove what external
   callers actually send.
4. **No CVE scan was run** this cycle. All versions are current releases as of the upgrade date,
   but scan the *resolved* coordinates before release: prior upgrades of this type found HIGH
   findings in BOM-pinned `jackson-databind` and `spring-core` that were invisible in the
   *declared* versions.

### Improvement recommendations (owner decisions — deliberately not actioned)

5. **Replace the `FileSystemStorage` stub before production.** Its own javadoc says *"Replace with
   real implementation"*: it returns `logback.xml` for **any** UUID and builds its file via
   `new File(getResource(…).getFile())`, which breaks inside a jar and on paths containing spaces.
   A real fix means moving `FilePointer` off `java.io.File` onto a Spring `Resource` / Guava
   `ByteSource` — an architecture change, not a migration step. ⚠️ Masked in tests because the
   `test` profile substitutes `FileStorageStub`. Interacts directly with item 1.
6. **Decide the fate of `Sha512ShallowEtagHeaderFilter`.** Now behaviour-tested, but still a
   **test-scoped class registered as a filter nowhere**, living in a `org.springframework.*` package
   purely to reach a protected method. Either promote it to `src/main/java` with a
   `FilterRegistrationBean`, or delete it.
7. **Consider `spring.threads.virtual.enabled=true`** on JDK 25 for this blocking, download-heavy
   workload. Not enabled: `ThrottlingInputStream` blocks on a Guava `RateLimiter`, and
   carrier-thread pinning needs a load test first.
8. **Prune 8 redundant `org.springframework:*` declarations** — all supplied transitively by
   `spring-boot-starter-webmvc`, now versionless and BOM-managed, hence harmless. Purely cosmetic.
   ⚠️ `spring-context-support` **must stay** — it is genuinely not transitive.
9. **`commons-codec` is declared but unused** anywhere in `src/` — removal candidate.
10. **Deprecation warning in `FileSystemPointer.java`.** Guava's `Files.hash(File, HashFunction)` is
    deprecated (still functional); the modern form is
    `Files.asByteSource(target).hash(Hashing.sha512())`. Not a compilation error and outside the
    upgrade's scope.
11. **Test file name does not match its class name.** `DownloadControllerTest.groovy` declares
    `class DownloadControllerSpec`. Handled via the surefire `**/*Spec.java` include rather than a
    rename, to avoid touching the test file. Cosmetic.
