# Upgrade summary — download-server → Java 25 / Spring Boot 4.0.10

## Result

`mvn clean install` → **BUILD SUCCESS**, **0 compilation errors**, **0 compiler warnings**,
**18 tests run / 0 failures / 0 errors / 0 skipped**, jar built and installed.
Toolchain: Amazon Corretto **JDK 25.0.5.1**, Maven 3.9.8.

| Suite | Tests | Environment |
| --- | --- | --- |
| `DownloadControllerSpec` (Groovy/Spock) | 12 | MockMvc |
| `MainApplicationTest` | 3 | **real embedded Tomcat 11, RANDOM_PORT, real HTTP** |
| `Sha512ShallowEtagHeaderFilterTest` | 3 | unit |

Final stack (verified via `mvn dependency:tree`): Spring Boot 4.0.10, Spring Framework 7.0.11,
Jakarta Servlet 6.1.0 / Jakarta Annotation 3.0.0 / Jakarta XML Bind 4.0.5, Tomcat Embed 11.0.27,
JUnit Platform 6.0.3, Groovy 5.0.8, Spock 2.5-groovy-5.0, Guava 33.8.0-jre, commons-io 2.22.0,
compiled and targeted at `release 25`.
**Zero `javax.*` artifacts and zero `javax.*` imports remain.**

## Compilation errors fixed

There was exactly **one** compilation error after the OpenRewrite run.

| File | Error | Fix |
| --- | --- | --- |
| `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java:8` | `method does not override or implement a method from a supertype` | Spring's `ShallowEtagHeaderFilter.generateETagHeaderValue` changed from `(byte[])` to `(InputStream, boolean isWeak) throws IOException` in Framework 6/7. Overrode the new signature, kept the SHA-512 ETag logic intact (`inputStream.readAllBytes()` feeds the same `Hashing.sha512().hashBytes(...)`), and honoured the new `isWeak` flag with the `W/` prefix the parent contract expects. Under the filter's default (`writeWeakETag=false`) the emitted ETag is byte-identical to before, so it still matches the quoted-SHA-512 format produced by `FileSystemPointer.getEtag()`. |

The OpenRewrite recipe itself only made two trivial source changes (dropped a redundant
`@Autowired` on a sole constructor, and `new Integer("1234")` → `Integer.valueOf("1234")`).
Everything below is the dependency/stack reconciliation needed to leave the project
internally consistent on Boot 4 / Jakarta EE 11 / JDK 25.

## Dependency reconciliation (finishing what the recipe left)

All of the following were stale javax-era or JDK-25-incompatible pins the recipe did not reach:

- **`org.codehaus.groovy:groovy-all:2.4.16`** — pinned in a local `dependencyManagement` block that
  overrode the Boot BOM → replaced with **`org.apache.groovy:groovy`**, version managed by Boot's
  `groovy-bom` import (5.0.8). Groovy 2.4 cannot compile or run on JDK 25, and the
  `org.codehaus.groovy` coordinates were abandoned at Groovy 4. The local `dependencyManagement`
  block existed only for this pin and was removed entirely so the BOM governs the version.
- **`spock-core` / `spock-spring:1.0-groovy-2.4`** → **`2.5-groovy-5.0`** (new `spock.version`
  property). Spock 1.0 is a JUnit 4 runner and is unusable under Boot 4's JUnit Platform; 2.5 is the
  line built against Groovy 5. Spock 2.5 resolves `junit-platform-engine` through Boot's `junit-bom`,
  so there is no split JUnit Platform on the classpath. The now-meaningless `groovy-all` and
  `junit:junit` exclusions on these two dependencies were dropped.
- **`cglib:cglib-nodep:3.1`** → **removed**. No source file imports cglib; it existed only for
  Spock 1 class mocking (Spock 2 uses ByteBuddy/Objenesis), and cglib 3.1 (2014) ships an ASM too
  old to generate classes on JDK 25.
- **`com.google.guava:guava:29.0-jre`** → **33.8.0-jre**. Verified every Guava API this codebase
  uses still exists on the new line: `Files.asByteSource(...).hash`, `MediaType.parse/charset`,
  `base.Optional.transform/or`, `RateLimiter.create/acquire`, `MoreObjects`, `net.HttpHeaders`.
- **`commons-io:2.4`** (2012) → **2.22.0**. Not BOM-managed; only `FileUtils.ONE_MB` is used.
- **`<spring.version>4.1.1.RELEASE</spring.version>`** property → **removed**. Dead pin: no
  dependency referenced it any more and the Boot parent supplies Spring 7.0.11.
- **`http://repo.spring.io/milestone/` repository** → **removed**. Maven 3.8+ blocks plaintext-HTTP
  external repositories (it resolves to the `maven-default-http-blocker` mirror), and Boot 4.0.10 /
  Spring 7.0.11 are GA on Maven Central.

Every artifact the Boot parent manages is now declared versionless; the only explicit versions left
are for artifacts the BOM does not manage (guava, commons-io, spock).

## Dormant test suite restored (this upgrade had no verification without it)

`src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy` holds the project's only
real test suite — 12 Spock specs covering the download controller's ETag / `If-None-Match` /
`If-Modified-Since` / HEAD / Content-Length / Content-Type behaviour. The pom declared Groovy and
Spock but **had no Groovy compiler plugin**, so the file was never compiled and `surefire` ran
**zero tests**. This predates the upgrade, but it meant the Java 25 / Boot 4 migration was
completely unverified.

Added `gmavenplus-plugin` 5.1.0 (`addTestSources`, `compileTests`) and the `**/*Spec.java` surefire
include (the class is `DownloadControllerSpec`, which the default `*Test` patterns miss). All
**12 specs now compile and pass** against Spring Framework 7 MockMvc on JDK 25 — this is the
evidence that the upgraded controller's caching and content-negotiation behaviour is unchanged.
No test was modified, weakened, skipped or deleted.

## Runtime regression found and fixed: logback config

Enabling the tests surfaced a real, build-silent defect. `src/main/resources/logback.xml` nested
`<appender class="...">` *inside* `<root>` with no `name` attribute. Logback 1.5.38 (shipped by
Boot 4) rejects this:

```
ERROR in ch.qos.logback.core.joran.action.AppenderAction - Missing attribute [name]. See element [appender] near line 9
WARN  in ...ImplicitModelHandler - Ignoring unknown property [encoder] in [ch.qos.logback.classic.Logger]
```

The consequence is that **no appender was attached to the root logger, so the application emitted no
log output at all at runtime** — a silent failure a green compile would never catch. Restructured to
the canonical form (named top-level `<appender>` + `<appender-ref>`) per knowledge-base doc 13,
preserving the exact encoder pattern, logger levels and root level. Logback now initialises clean.

## Other cleanups

- `com.google.common.io.Files.hash(File, HashFunction)` is deprecated in modern Guava and was the
  build's only remaining compiler warning. Switched to the documented, semantically identical
  replacement `Files.asByteSource(target).hash(Hashing.sha512())`. The build is now warning-free.
- Removed a stale root-owned `target/` directory left by a previous cycle, which was failing the
  `maven-resources-plugin` with `FileSystemException: Operation not permitted`.

## Real-stack verification added (closing the previous cycle's main gap)

The 12 Spock specs run in MockMvc, so they never touch the real servlet container — the upgrade's
biggest residual risk was that nothing proved the app actually *boots* on Boot 4 / Tomcat 11 / JDK 25.
Added `MainApplicationTest` (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) which starts the real
embedded Tomcat and drives it over real HTTP with the JDK's own `java.net.http.HttpClient` —
no new dependency, and no violation of the "do not execute the application" rule, since the prompt
directs verification through unit tests.

Note that **Boot 4 removed `TestRestTemplate` from `spring-boot-starter-test`** (it moved to a separate
`spring-boot-restclient-test` module), which is why the JDK client is used instead.

It asserts: `/actuator/health` returns 200 `"status":"UP"`; `GET /download/{uuid}/file.txt` returns 200
with body `foobar` and an `ETag` matching `FileSystemPointer.getEtag()`; an unknown UUID returns 404.
Together that exercises context startup, Tomcat 11 / Servlet 6.1, the controller, the throttled
stream, ETag generation, and the Jackson 3 (`tools.jackson`) JSON render path.

`Sha512ShallowEtagHeaderFilterTest` covers the signature migrated above, including the **`isWeak`
branch the migration introduced**, against an independently computed SHA-512 vector
(`printf 'foobar' | sha512sum`) rather than a value read back from the implementation, plus a
cross-check that the filter and `FileSystemPointer` emit the identical ETag form (the invariant the
controller's 304 handling depends on).

Incidental confirmation of the logback fix: test output now carries the project's own configured
pattern (`16:27:04.342 | main | DEBUG | c.n.d.MainApplicationTest | ...`), proving the appender is
attached and log records actually flow.

## Next steps / notes for human review

Non-blocking items, assumptions and residual risks:

1. **The app was never launched as a process**, per the prompt's prohibition (no `java -jar`, no
   `spring-boot:run`). Routing, ETag/304, HEAD, Content-Type, 404, **real-Tomcat startup and
   `/actuator/health`** are now all test-covered, so the only genuinely unverified areas left are
   **fat-jar packaging** and the `FileSystemStorage` classpath bug in item 3.
2. **`spring-boot-maven-plugin` is not declared**, so `mvn package` produces a plain jar, not an
   executable fat jar. This matches the original build, so I left it alone — add it if an executable
   artifact is wanted.
3. **`FileSystemStorage` is a stub with a packaging bug.** It returns `logback.xml` for *any* UUID and
   uses `new File(getResource(...).getFile())`, which **cannot work from inside a packaged jar**. A
   real fix means moving `FilePointer` off `java.io.File` and onto a Spring `Resource` / Guava
   `ByteSource` — an architecture change to a class whose own javadoc says *"Replace with real
   implementation."* **Owner decision, not an upgrade step.** It is masked in tests because the `test`
   profile substitutes `FileStorageStub`.
4. **`Sha512ShallowEtagHeaderFilter` is behaviour-tested but registered nowhere.** It lives in test
   sources in the `org.springframework.web.filter` package and nothing `@Bean`-registers it. Either
   promote it to `src/main/java` with a `FilterRegistrationBean`, or delete it. **Owner call** —
   deleting a class whose tests pass is not an upgrade step, so I migrated rather than removed it.
5. **Spock 2.5 on JUnit Platform 6 is an undocumented pairing** (Spock 2.5 publishes against
   junit-bom 5.14). Verified empirically that it resolves cleanly: `mvn dependency:list` shows a
   **single, unified JUnit Platform 6.0.3** with no split versions — Boot's `junit-bom` wins — and
   both engines run side by side in one surefire pass (Spock engine: 12 specs; Jupiter engine: 6
   tests). If a future platform bump breaks the Spock engine, the escape hatches are pinning
   `junit-bom` or porting the spec to JUnit 5 + `MockMvcTester`.
6. **Non-BOM pins to review:** `guava 33.8.0-jre`, `commons-io 2.22.0`, `spock 2.5-groovy-5.0`, plus
   plugins `maven-compiler-plugin 3.16.0` and `gmavenplus-plugin 5.1.0`. None are BOM-managed and none
   deviate from a mandated version, so there is no deviation table.
7. **Two behavioural Framework 6+/Boot 4 changes that no build can prove**, both assessed as
   non-impacting here: trailing-slash matching is no longer automatic (this codebase has no
   trailing-slash routes), and Boot 4 moves to Jackson 3 / `tools.jackson` (no direct Jackson usage;
   the render path is now exercised by `MainApplicationTest`'s health assertion). Worth a confirmation
   against real clients if any exist.
8. **`spring.threads.virtual.enabled`** was deliberately *not* enabled. On JDK 25 it is available, but
   `ThrottlingInputStream` blocks on a Guava `RateLimiter` and the download path is deliberately
   throttled, so pinning behaviour needs a load test before switching. Flagging as an opt-in decision.
9. **Many Spring artifacts are declared individually** (`spring-beans`, `spring-context`, `spring-aop`,
   `spring-core`, `spring-expression`, `spring-webmvc`, `spring-web`) although
   `spring-boot-starter-webmvc` already brings them transitively. They are all versionless/BOM-managed
   so they are harmless and consistent; deliberately left alone under the smallest-consistent-change
   rule. Purely cosmetic cleanup for a future pass.
10. **`argLine` / byte-buddy javaagent.** The surefire `argLine` keeps the
   `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` wiring (resolved by `maven-dependency-plugin`'s
   `properties` goal). It resolves and tests pass. It is only needed if Mockito inline mocking is
   introduced later; no current test mocks anything.
11. **`commons-codec` is declared but unused** by any source file (it is BOM-managed, so version-safe).
   Left in place rather than removed, since it was not part of this task's scope.
