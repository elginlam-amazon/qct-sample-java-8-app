# Java 25 / Spring Boot 4.0 Upgrade Summary

`com.nurkiewicz.download:download-server` — upgraded from Java 8 / Spring Boot 1.2.3 to
**Java 25 / Spring Boot 4.0.10** (Spring Framework 7.0.11, Jakarta EE 11).

**Result: `mvn -B clean install` passes with zero compilation errors. 18/18 tests pass.**
Verified across three consecutive clean runs, with a byte-identical 92-artifact dependency
tree between runs.

## What OpenRewrite had already landed

- `spring-boot-starter-parent` 1.2.3 → 4.0.10; `spring-boot-starter-web` → `spring-boot-starter-webmvc` (Boot 4.0 starter rename).
- Removed hardcoded `4.1.6.RELEASE` / `1.2.3.RELEASE` versions so the Boot BOM manages them.
- `maven-compiler-plugin` 3.0 `source/target 1.8` → 3.16.0 `release 25`.
- `new Integer("1234")` → `Integer.valueOf("1234")`; dropped a redundant `@Autowired` on a sole constructor.
- Added `jakarta.servlet-api`, the byte-buddy-agent surefire `argLine`, and `maven-dependency-plugin:properties`.

## Fixes completed in this cycle

### Build blocker

- **Removed the `http://repo.spring.io/milestone/` repository.** Maven's
  `maven-default-http-blocker` rejects plain-HTTP repositories, which failed the build before it
  could even read the POM. Spring Boot 4.0.10 is GA and resolves from Maven Central.
- Removed the unused `spring.version` = `4.1.1.RELEASE` property (no longer referenced anywhere).

### Compilation error (1)

- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` —
  `ShallowEtagHeaderFilter`'s ETag hook changed signature from
  `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean isWeak)`,
  so the `@Override` no longer resolved. Reimplemented against the new signature, keeping the
  SHA-512 digest and quoted-ETag format, and honouring the new `isWeak` flag with the standard
  `W/` prefix (matching Spring's own implementation).

### Dependency reconciliation (Groovy 2.4 / Spock 1.0 cannot run on Java 25)

The recipe left the Groovy test stack untouched and pinned to the pre-Java-9 line:

- Dropped the `dependencyManagement` pin of `org.codehaus.groovy:groovy-all:2.4.16` and moved the
  dependency to `org.apache.groovy:groovy`, versionless so the Boot BOM manages it (**Groovy 5.0.8**).
  Groovy 2.4 does not load on any JDK past 8.
- `spock-core` / `spock-spring` `1.0-groovy-2.4` → **`2.5-groovy-5.0`** (JUnit 5 Platform based,
  matching the BOM's Groovy 5.0). Removed the now-stale `groovy-all` and `junit:junit` exclusions —
  neither artifact appears in Spock 2.x's tree.
- Removed `cglib:cglib-nodep:3.1`. It existed only for Spock 1.x class mocking (Spock 2 uses
  ByteBuddy) and is unreferenced in source; cglib 3.1 emits pre-Java-7 bytecode.
- `commons-io` 2.4 → 2.22.0 and `guava` 29.0-jre → 33.8.0-jre (neither is BOM-managed, so both
  are pinned explicitly).
- Removed the redundant `spring-test` declaration plus its matching exclusion from
  `spring-boot-starter-test` — a no-op pairing now that the BOM manages the version.

Verified end state: `mvn dependency:tree` contains **no `javax.*` artifact, no `org.codehaus.groovy`,
and no cglib**. Jakarta EE 11 throughout (`jakarta.servlet-api` 6.1.0, `jakarta.annotation-api` 3.0.0,
`jakarta.xml.bind-api` 4.0.5), all Spring at 7.0.11, JUnit Jupiter 6.0.3, Mockito 5.20.0.

### Test suite was silently dead — now running

`DownloadControllerTest.groovy` (12 Spock specs, the project's **only** real test coverage) was never
being compiled or executed. Two independent causes, both pre-existing:

1. No Groovy compiler was configured, so `src/test/groovy` was skipped entirely. Added
   **`gmavenplus-plugin` 5.1.0** (`compileTests` goal).
2. The class is named `DownloadControllerSpec` (the filename does not match), so it fell outside
   surefire's default `*Test` / `*Tests` includes. Added `**/*Spec.java` to the surefire includes.

All 12 specs now compile against Groovy 5.0.8 and **pass** on Java 25 / Boot 4 — this is what
actually verifies the upgraded controller (ETag caching, `If-Modified-Since`, HEAD handling,
`Content-Length` and `Content-Type`), rather than just a green compile.

### Verification coverage added (6 new tests, 12 → 18)

The 12 Spock specs all run through `MockMvc`, which never starts a servlet container — so nothing
proved the upgraded stack actually *boots*. Since these instructions forbid executing the
application, two tests were added as the sanctioned substitute:

- **`ApplicationStartupTest`** (3 tests) — `@SpringBootTest(webEnvironment = RANDOM_PORT)` boots the
  app on **real embedded Tomcat 11.0.27** under Java 25 and drives it over real HTTP with the JDK
  `HttpClient`. Asserts `/actuator/health` returns 200 / `"status":"UP"`, that a download streams
  end-to-end with the right `ETag`, and that an unknown UUID returns 404. This covers real container
  startup, the full `InputStreamResource` + `ThrottlingInputStream` streaming path through Tomcat,
  and the actuator JSON render path. Note `TestRestTemplate` no longer exists in `spring-boot-test`
  4.0.10, hence the JDK client.
- **`Sha512ShallowEtagHeaderFilterTest`** (3 tests) — pins the strong/weak ETag format of the one
  class that needed a genuine API migration, so the `byte[]` → `(InputStream, boolean)` rewrite is
  behaviour-locked rather than merely compiling.

### Toolchain pairing verified

`spock-core`/`spock-spring` 2.5 publishes against `junit-bom` 5.14, so pairing it with Boot 4's
JUnit Platform 6 is an undocumented combination. Confirmed empirically rather than assumed:
`dependency:list` resolves a **single unified JUnit 6.0.3** across `junit-platform-*` and
`junit-jupiter-*` (Boot's `junit-bom` wins, no split versions), and the Spock and Jupiter engines
run side by side in one surefire pass (12 Spock + 6 Jupiter = 18). If a future bump breaks this,
the escape hatches are pinning `junit-bom` explicitly or porting the spec to JUnit 5 +
`MockMvcTester`.

### Latent defects surfaced by the upgrade

- `src/main/resources/logback.xml` declared `<appender>` **nested inside `<root>` without a `name`**
  and with no `appender-ref`. Logback 1.5 reports this as
  `ERROR ... Missing attribute [name]` and `Ignoring unknown property [encoder]`, and the root logger
  ended up with no appender at all. Rewrote to the canonical named-appender + `appender-ref` form,
  preserving the exact pattern and all logger levels.
- `FileSystemPointer` used Guava's `Files.hash(File, HashFunction)`, deprecated and marked for
  removal. Switched to the documented replacement `Files.asByteSource(file).hash(...)` — identical
  semantics, and clears the build's last deprecation warning.

## Next steps (non-blocking notes for human review)

The upgrade itself is complete. Everything below is either an owner decision or a check no build
can perform. Nothing here blocks the build.

### Closed out in this iteration

- **Real container startup is now test-covered**, not just asserted. Routing, ETag/304s, HEAD,
  `Content-Type`, 404, Tomcat 11 startup and `/actuator/health` all have tests.
- **Repeatability confirmed**: `mvn -B clean install` run three times consecutively, 18/18 each
  time, with an identical 92-artifact dependency tree (same checksum) between runs. Safe for CI.
- **Jackson 3 (`tools.jackson`)**, which Boot 4 moves to, needed no action: no source file
  references Jackson directly, and the serialization path is now exercised by the actuator health
  assertion.
- **Trailing-slash matching** (removed in Framework 6+): assessed as non-impacting. No route in
  this app depends on a trailing slash, and the 301 in `ExistingFile.redirectDownload` targets the
  explicitly mapped `/download/{uuid}/{filename}`. Still worth a glance at real client traffic
  before release, since no test can prove what external callers send.

### Owner decisions (deliberately not actioned)

1. **Fat-jar packaging is the one genuinely unverified area.** `spring-boot-maven-plugin` is not
   declared, so `install` produces a plain, non-executable jar — which matches the original build
   exactly, hence left alone. Add the plugin with the `repackage` goal if an executable artifact is
   expected. Related: see item 3, which only manifests from inside a packaged jar.
2. **Decide the fate of `Sha512ShallowEtagHeaderFilter`.** It is now behaviour-tested, but it is
   still a *test-scoped* class registered as a filter nowhere, sitting in a `org.springframework.*`
   package purely to reach a protected method. Either promote it to `src/main/java` with a
   `FilterRegistrationBean`, or delete it. Deleting a class whose tests pass is not an upgrade step,
   so this is an owner call.
3. **Replace the `FileSystemStorage` stub before production.** Its own javadoc says *"Replace with
   real implementation"*. It returns `logback.xml` for *any* UUID, and builds its file via
   `new File(getResource(...).getFile())`, which **cannot work from inside a packaged jar** and also
   breaks on paths containing spaces. A proper fix means moving `FilePointer` off `java.io.File`
   onto a Spring `Resource` / Guava `ByteSource` — an architecture change, not a migration step.
   Note this is masked in tests because the `test` profile substitutes `FileStorageStub`.
4. **Virtual threads** (`spring.threads.virtual.enabled=true`) are available on Java 25 and would
   suit this blocking, download-heavy workload — but `ThrottlingInputStream` blocks on a Guava
   `RateLimiter`, and carrier-thread pinning needs a load test first. Opt-in decision, not enabled.

### Cosmetic cleanups (left under the smallest-consistent-change rule)

5. **8 redundant `org.springframework:*` declarations** (`spring-beans`, `spring-context`,
   `spring-webmvc`, `spring-web`, `spring-aop`, `spring-core`, `spring-expression`) are all supplied
   transitively by `spring-boot-starter-webmvc`. They are versionless and BOM-managed, so they are
   harmless and consistent. `spring-context-support` must stay — it is genuinely not transitive.
6. **`commons-codec` is declared but unused** anywhere in `src/`. Removal candidate.
7. **No `application.properties` / `yml` exists**, so there were no Boot 3.x/4.0 property renames to
   apply and nothing to verify on that front.
