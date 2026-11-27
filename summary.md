# Upgrade summary — download-server → Java 25 / Spring Boot 4.0.10

## Result

`mvn clean install` → **BUILD SUCCESS**, **0 compilation errors**, **16 tests run / 0 failures / 0 errors
/ 0 skipped**, jar produced and installed.

| Suite | Tests | Environment |
| --- | --- | --- |
| `DownloadControllerSpec` (Groovy/Spock) | 12 | MOCK servlet |
| `Sha512ShallowEtagHeaderFilterTest` | 3 | unit |
| `MainApplicationTest` | 1 | **real embedded Tomcat, RANDOM_PORT** |

Final stack (verified via `mvn dependency:tree`): Spring Boot 4.0.10, Spring Framework 7.0.11,
Jakarta Servlet 6.1.0 / Jakarta Annotation 3.0.0 / Jakarta XML Bind 4.0.5, Tomcat Embed 11.0.27,
JUnit 6.0.3 (Platform 6.0.3), Mockito 5.20.0, Groovy 5.0.8, Spock 2.5-groovy-5.0, Guava 33.8.0-jre,
compiled and targeted at `release 25`. **Zero `javax.*` artifacts and zero `javax.*` imports remain.**

## Compilation errors fixed

| File | Error | Fix |
| --- | --- | --- |
| `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java:8` | `method does not override or implement a method from a supertype` | Spring's `ShallowEtagHeaderFilter.generateETagHeaderValue` changed from `(byte[])` to `(InputStream, boolean isWeak) throws IOException`. Overrode the new signature, kept the SHA-512 ETag logic (`inputStream.readAllBytes()` feeds the same `Hashing.sha512().hashBytes(...)`), and honoured the new `isWeak` flag with the `W/` prefix the parent contract expects — output is byte-identical to before under the filter's default (`writeWeakETag=false`) configuration. |

That was the only compilation error. The rest of the work below was dependency/stack reconciliation
required to leave the project internally consistent on Boot 4 / Jakarta / JDK 25.

## Dependency reconciliation (finishing what the recipe left)

Removed / replaced, all of which were stale javax-era or JDK-25-incompatible pins:

- **`org.codehaus.groovy:groovy-all:2.4.16`** (pinned in a local `dependencyManagement` block the recipe
  added, overriding the Boot BOM) → **`org.apache.groovy:groovy`**, version managed by Boot's
  `groovy-bom` import (5.0.8). Groovy 2.4 cannot compile or run on JDK 25; the `org.codehaus.groovy`
  coordinates were abandoned at Groovy 4.
- **`spock-core` / `spock-spring:1.0-groovy-2.4`** → **`2.5-groovy-5.0`** (new `spock.version` property).
  Spock 1.0 is a JUnit 4 runner and is unusable under Boot 4's JUnit Platform. Spock 2.5 resolves its
  `junit-platform-engine` through Boot's `junit-bom`, so there is no split JUnit Platform on the classpath.
  The now-meaningless `groovy-all` and `junit:junit` exclusions on those two dependencies were dropped.
- **`cglib:cglib-nodep:3.1`** → **removed**. It existed only for Spock 1 class mocking; Spock 2 uses
  ByteBuddy/Objenesis, no source file imports cglib, and cglib 3.1 (2014) cannot generate classes on
  JDK 25.
- **`com.google.guava:guava:29.0-jre`** → **33.8.0-jre**. Verified against the jar that every Guava API
  this codebase uses still exists: `Files.asByteSource(...).hash`, `MediaType.charset()`,
  `base.Optional.transform/or`, `RateLimiter.create/acquire`.
- **`commons-io:2.4`** → **2.22.0** (not BOM-managed; only `FileUtils.ONE_MB` is used).
- **`<spring.version>4.1.1.RELEASE</spring.version>`** property → **removed**. Dead pin: no dependency
  referenced it any more, and the Boot parent supplies Spring 7.0.11.
- **`http://repo.spring.io/milestone/` repository** → **removed**. Maven 3.8+ blocks plaintext-HTTP
  external repositories, and Boot 4.0.10 / Spring 7.0.11 are GA on Maven Central.
- Left alone deliberately: `commons-codec`, `jakarta.servlet-api` and all `org.springframework:spring-*`
  entries stay version-less so the Boot BOM manages them (it resolves them to 1.19.0, 6.1.0 and 7.0.11).

## Tests: the Groovy Spock spec is now actually executed

`src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy` (class `DownloadControllerSpec`,
12 feature methods) was **never compiled or run by Maven before this cycle** — the POM had no Groovy
compiler plugin, and the class name does not match Surefire's default includes. Rather than leave the
project's only test dead, it is now wired in:

- added `gmavenplus-plugin:4.2.1` with the `compileTests` goal and `<targetBytecode>25</targetBytecode>`
  (Groovy 5 rejects the plugin's default 1.8 target);
- added Surefire `<includes>` for `**/*Test.java`, `**/*Tests.java`, `**/*Spec.java` (keeping the default
  patterns alongside the `*Spec` one Spock needs).

The spec was **not modified in any way** — no assertion was relaxed, nothing was skipped — and all 12
features pass against Spring MVC 7, covering the behaviour most at risk in this upgrade: 200/404 routing,
byte-for-byte body, `ETag` generation and `If-None-Match` 304s, `If-Modified-Since` handling,
`Content-Length`, `Content-Type`, and bodyless `HEAD` responses.

## Other defects surfaced by the new stack

- `src/main/resources/logback.xml`: Logback 1.5 reported
  `ERROR in ch.qos.logback.core.joran.action.AppenderAction - Missing attribute [name]` — the console
  appender was declared inline inside `<root>` with no name, so **no appender was attached to the root
  logger at all** (the app would have logged nothing). Restructured to a named top-level
  `<appender name="CONSOLE">` plus `<appender-ref ref="CONSOLE"/>`, preserving the original pattern,
  levels and per-package logger configuration verbatim. Build output confirms the appender now works.
- `FileSystemPointer`: replaced the deprecated `Files.hash(File, HashFunction)` with Guava's documented
  replacement `Files.asByteSource(file).hash(HashFunction)` — identical `HashCode`, and it clears the
  last deprecation warning in the build (`-Xlint:deprecation` is now clean).

## Carry-over from iteration 64 — disposition

- ✅ **`mvn clean install` on JDK 25** — run, green. Note the carry-over expected **16/16**; this cycle
  started from a tree that had been reset to 12 (the Groovy spec only), so the four extra tests it
  referred to did not exist here. They have been re-created (below), so the count is **16/16** again.
- ✅ **Exercise a real Tomcat bind** — done. Added `MainApplicationTest`:
  `@SpringBootTest(classes = MainApplication.class, webEnvironment = RANDOM_PORT)`, injects
  `@LocalServerPort`, and GETs `/actuator/health` over a **real socket** with Spring 7's `RestClient`,
  asserting `200` and `"status":"UP"`. This closes the "startup is only proven in a MOCK environment"
  gap and also proves the Boot 4 actuator default exposure and the Jackson 3 render path.
  (`TestRestTemplate` is gone from the Boot 4 modules on this classpath, hence `RestClient`.)
- ✅ **`Sha512ShallowEtagHeaderFilter` is behaviour-tested again** — added
  `Sha512ShallowEtagHeaderFilterTest` (3 tests) covering the `(InputStream, boolean)` override this
  cycle rewrote, which was otherwise **completely untested**: the strong quoted SHA-512, the `W/` weak
  prefix, and the invariant that the filter's ETag equals `FileSystemPointer`'s for identical content.
  The expected digest is a literal, independently confirmed with `sha512sum` rather than recomputed with
  the same Guava call under test.
- ⏭️ **Smoke-test the packaged jar** — still not performed; executing the application remains forbidden by
  this cycle's instructions. The `RANDOM_PORT` test now covers servlet-container startup and binding, so
  what genuinely remains un-smoke-tested is only **fat-jar packaging** (see note 4 below) and
  `FileSystemStorage`'s jar-classpath bug (note 5).
- ⏸️ **Fate of `Sha512ShallowEtagHeaderFilter`** — left in place, unchanged in placement. It is still a
  test-scoped class registered as a filter nowhere; wiring it into `src/main` with a
  `FilterRegistrationBean` or deleting it is an owner call, not an upgrade step. Agreed with iteration 64.
- ⏸️ **The 8 redundant `org.springframework:*` declarations** — left in place. They are versionless and
  BOM-managed, therefore consistent, and `spring-boot-starter-webmvc` would supply all of them
  transitively, so deleting them is cosmetic. The upgrade rule is to make the smallest change that yields
  a consistent stack and not to churn dependencies that already build correctly, so this stays an
  owner-review item.
- ℹ️ **Non-BOM pins** — unchanged from this cycle's §"Dependency reconciliation": `commons-io 2.22.0`,
  `guava 33.8.0-jre`, `spock 2.5-groovy-5.0`, `gmavenplus 4.2.1`, `maven-compiler-plugin 3.16.0`.
  Nothing deviates from a mandated version, so there is no deviation table.
- ℹ️ **Trailing-slash matching and Jackson 3** — confirmed not applicable: no route in
  `DownloadController` relies on trailing-slash equivalence, and no source file references Jackson
  directly. The actuator JSON asserted by `MainApplicationTest` is in fact rendered by Jackson 3
  (`tools.jackson`), so that path is now exercised.
- ℹ️ **Actuator exposure / Security** — untouched, still Boot 4's conservative default (`health` only, no
  `application.properties` in the project). Adding more endpoints should be paired with Spring Security,
  and only if genuinely wanted.

## Next steps / notes for the next cycle

1. **CodeKB was unavailable** in this cycle — no `mcp__codekb__*` tools were exposed, so the pre-change
   mapping of the old code was done by reading the working tree directly. Nothing was blocked by this
   (the module is 8 main + 3 test classes), but a future cycle with CodeKB available could cross-check
   the `ExistingFile` caching flow against the indexed original.
2. **`org.apache.groovy:groovy` is still `compile`-scoped**, inherited from the original POM's
   `groovy-all` declaration. Nothing in `src/main` uses Groovy; moving it to `<scope>test</scope>` would
   shrink the published artifact. Left as-is to avoid changing packaging semantics in an upgrade cycle.
3. **`MainApplication.main` still has a dead local** (`Integer temp = Integer.valueOf("1234");`,
   OpenRewrite's rewrite of `new Integer("1234")`). Harmless and compiles; it is pre-existing dead code
   that could simply be deleted.
4. **No `spring-boot-maven-plugin`** is configured (also true before the upgrade), so `mvn package`
   produces a plain jar, not an executable fat jar. Add the plugin if an executable artifact is expected.
   Per instructions the application was not launched; verification was build + unit tests only.
5. **`FileSystemStorage` remains a stub** that returns `/logback.xml` for every UUID (documented as such
   in its own Javadoc). Worse, it does `new File(getClass().getResource(...).getFile())`, which **cannot
   work from a packaged jar** — fixing that means moving `FilePointer` off `java.io.File` onto a Spring
   `Resource`/Guava `ByteSource`, an architecture change to a class whose own comment says *"Replace with
   real implementation"*. Owner decision, not an upgrade step. It stays masked in tests because the
   `test` profile substitutes `FileStorageStub`, and `MainApplicationTest` only hits `/actuator/health`.
6. **Spock 2.5 + JUnit Platform 6** is not an officially documented combination (Spock 2.5 publishes
   against junit-bom 5.14). It resolves cleanly here because Boot's `junit-bom` wins and all 12 specs
   pass, but if a future JUnit Platform bump breaks the Spock engine, pinning `junit-bom` or moving the
   spec to JUnit 5 + Spring `MockMvcTester` are the escape hatches.
