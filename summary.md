# Java 25 / Spring Boot 4 Upgrade — Summary

## Result

`mvn clean install` — **BUILD SUCCESS** on JDK 25, zero compilation errors, zero
compiler warnings, zero Logback configuration errors.

```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
  com.nurkiewicz.download.DownloadControllerSpec ............ 13  (Spock)
  org.springframework.web.filter.Sha512…FilterTest ..........  2  (JUnit 5)
  com.nurkiewicz.download.MainApplicationContextTest ........  1  (JUnit 5)
```

| | Before | After |
| --- | --- | --- |
| JDK (`release`) | 1.8 | 25 |
| Spring Boot | 1.2.3.RELEASE | 4.0.10 |
| Spring Framework | 4.1.6.RELEASE | 7.0.11 |
| Namespace | javax (Servlet 3.0) | jakarta (Servlet 6.1) |
| Groovy | `org.codehaus.groovy:groovy-all` 2.4.3 | `org.apache.groovy:groovy` 5.0.8 (BOM-managed) |
| Spock | 1.0-groovy-2.4 (JUnit 4) | 2.5-groovy-5.0 (JUnit Platform 6) |
| Guava | 18.0 | 33.8.0-jre |
| commons-io | 2.4 | 2.22.0 |
| Tests actually executed | **0** | **16** |

No `javax.*` Java EE imports and no javax-era artifacts remain on any classpath
(`dependency:list` is clean of `javax`, `org.codehaus.groovy` and `cglib`).

---

## ⚠️ Critical finding this iteration: the app could not start on Boot 4

The open question carried over from iterations 62 and 63 — *was the application ever
actually booted on Spring Boot 4?* — is now **closed, and the answer was no.**

Adding a context-load test surfaced a hard startup failure on the first run:

```
java.lang.IllegalStateException: Logback configuration error detected:
ERROR in ch.qos.logback.core.joran.action.AppenderAction - Missing attribute [name].
  See element [appender] near line 9
  at LogbackLoggingSystem.reportConfigurationErrorsIfNecessary(LogbackLoggingSystem.java:288)
```

`src/main/resources/logback.xml` declared its `ConsoleAppender` **nested inside
`<root>`** and without a `name` attribute. Logback has always rejected that, but under
Spring Boot 1.2 it was only *printed* — under Boot 3+/4, `LogbackLoggingSystem`
escalates any Logback configuration error into a fatal `IllegalStateException`, so the
application aborted during environment preparation, before any bean was created.

Earlier iterations classified this as "pre-existing and cosmetic, out of scope". That
was wrong in consequence: the file was always malformed, but the upgrade turned a
harmless console message into a **boot-blocking failure**. A green compile and 13
green MockMvc tests could not detect it, because `@ContextConfiguration` does not run
Boot's `LoggingApplicationListener`.

Fixed by giving the appender a name at `<configuration>` level and referencing it from
`<root>`, preserving the original pattern and logger levels exactly:

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder><pattern>%d{HH:mm:ss.SSS} | %thread | %-5level | %logger{1} | %msg%n</pattern></encoder>
</appender>
...
<root level="DEBUG"><appender-ref ref="CONSOLE"/></root>
```

This also cleared a second symptom (`Ignoring unknown property [encoder] in
[ch.qos.logback.classic.Logger]`) and restored application logging, which the broken
config had silently disabled — the root logger previously had **no appender at all**.

Confirmed working: the build log now shows the context starting and using the
configured pattern.

```
03:12:34.237 | main | INFO  | c.n.d.MainApplicationContextTest | Starting … using Java 25.0.5.1
03:12:37.453 | main | INFO  | c.n.d.MainApplicationContextTest | Started … in 3.608 seconds
```

---

## What the OpenRewrite recipe had already done

- Parent → `spring-boot-starter-parent:4.0.10`; `maven-compiler-plugin` → `<release>25</release>`.
- `spring-boot-starter-web` → `spring-boot-starter-webmvc` (Boot 4.0 starter rename).
- Dropped explicit versions on BOM-managed artifacts; removed a duplicate `spring-test`.
- Added `jakarta.servlet:jakarta.servlet-api` (test), the `maven-dependency-plugin:properties`
  goal and the Surefire `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` argLine
  (JDK 25 forbids self-attaching agents, so ByteBuddy must be pre-loaded).
- `new Integer("1234")` → `Integer.valueOf("1234")`; dropped the redundant `@Autowired`
  on the single-constructor `DownloadController`.

## Compilation error fixed

**`Sha512ShallowEtagHeaderFilter` — changed supertype signature** (the only compile error)

```
src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java:8
  method does not override or implement a method from a supertype
```

Spring Framework 5 replaced `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])`
with `generateETagHeaderValue(InputStream, boolean isWeak)`. Ported the override to the
current signature, keeping the SHA-512 ETag and honouring the new weak-ETag flag.

## Dependency reconciliation (recipe did not reach these)

The recipe left artifacts that *resolved* but cannot run on JDK 25:

1. **Groovy 2.4.16 → 5.0.8.** The recipe pinned `org.codehaus.groovy:groovy-all:2.4.16`
   in `dependencyManagement` at compile scope; Groovy 2.4 cannot run on JDK 25.
   Replaced with `org.apache.groovy:groovy` at test scope (its only consumer is the
   Spock spec); removed the pin so Boot 4's BOM manages the version.
2. **Spock 1.0-groovy-2.4 → 2.5-groovy-5.0** (`${spock.version}`) for `spock-core` and
   `spock-spring`. Spock 1.0 is a JUnit 4 runner bound to Groovy 2.4. The `junit` /
   `groovy-all` workaround exclusions the recipe added are no longer needed and were
   removed. Verified Spock 2.5 (built against JUnit Platform 1.x) runs on the JUnit
   Platform 6.0.3 that Boot 4.0.10 manages.
3. **Removed `cglib:cglib-nodep:3.1`** — Spock 1.0's class-mocking backend (Spock 2 uses
   ByteBuddy + Objenesis), referenced by no source, and unable to instrument on a modern JDK.
4. **Removed `commons-codec`** — compile-scoped, a leaf of the dependency graph, and used
   by no source file (hashing goes through Guava). Verified with
   `dependency:tree -Dincludes=commons-codec` before removal.
5. **`commons-io` 2.4 → 2.22.0** and **Guava 29.0-jre → 33.8.0-jre** (neither BOM-managed).
   Confirmed by `javap` that every API in use still exists: `FileUtils.ONE_MB`,
   `MediaType.charset()` returning `com.google.common.base.Optional` with `transform`/`or`,
   `RateLimiter.create/acquire`, `MoreObjects.toStringHelper`, `Files.asByteSource`.
6. **Removed the stale `<spring.version>4.1.1.RELEASE</spring.version>` property** and the
   **`http://repo.spring.io/milestone/` repository** (plain-HTTP repos are blocked by
   Maven 3.8+, and everything resolves from Central).

## Test suite: from 0 executed to 16

`DownloadControllerTest.groovy` was **never compiled or run by the original build** — the
pom had no Groovy compiler plugin, so `mvn test` reported no tests at all, before *or*
after the recipe. The upgrade was therefore compile-verified only.

- Added `gmavenplus-plugin:5.1.0` (`compileTests`) and a Surefire `**/*Spec.java` include
  (the class is `DownloadControllerSpec`, which the default `*Test`/`*Tests` includes miss).
  All 12 pre-existing features passed unmodified.
- **+1 Spock feature:** the `/download/{uuid}` → 301 redirect path had no coverage at all;
  it now asserts `301` and the exact `Location` value.
- **+2 JUnit 5 tests** for `Sha512ShallowEtagHeaderFilter`, which was previously only
  compile-checked: the strong ETag is verified against an **independent oracle** (the JDK's
  own `MessageDigest.getInstance("SHA-512")`, not the Guava code the filter uses), and the
  weak-ETag `W/"…"` branch of the new signature is covered.
- **+1 JUnit 5 test** `MainApplicationContextTest` — `@SpringBootTest` (default MOCK servlet
  environment, no server socket) proving Boot 4 auto-configuration actually starts. This is
  what caught the Logback failure above.

No test was deleted, renamed, disabled, skipped or weakened at any point.

## Other source change

`FileSystemPointer`: `Files.hash(target, …)` → `Files.asByteSource(target).hash(…)`.
Guava deprecated `Files.hash` in 21, so the version bump surfaced a deprecation warning;
this is the documented equivalent. The build is now warning-free.

---

## Next steps (non-blocking notes for human review)

### Validation

- 🌐 **Smoke-test the packaged jar** — still not done, because this cycle's instructions
  forbid executing the application. Check `/download/{uuid}` (301), `/download/{uuid}/{filename}`
  (200), both 304 paths, `HEAD`, and `/actuator/health`. Routing, the exact `Location` value,
  the ETag/304 behaviour **and now context startup** are covered by tests, so a smoke test is
  really about **servlet-container and packaging** behaviour only.
- ✅ **"Was the app ever booted on Boot 4?" is resolved** — see the critical finding above. It
  now boots, and a regression test guards it. Note this used a mock servlet environment; a
  real Tomcat bind was not exercised.
- 🏷️ **`Sha512ShallowEtagHeaderFilter` is still a test-only class registered as a filter
  nowhere.** It is now behaviour-tested rather than merely compiled, but the open decision
  stands: wire it in (a `FilterRegistrationBean`, and move it to `src/main/java`) or delete it.
  Left in place — deleting a class with passing tests is an owner call, not an upgrade step.
- 🧾 **Non-BOM version pins for owner review:** Spock `2.5-groovy-5.0`, gmavenplus `5.1.0`,
  Guava `33.8.0-jre`, commons-io `2.22.0`, plus the recipe's compiler-plugin `3.16.0`.
  Nothing deviates from a mandated version, so there is no deviation table.
- 📌 **Carry-over discrepancy:** the iteration-63 notes described 16 tests (13 Spock + 3 JUnit 5)
  as already done, but **none of that work was present in this working tree** — it contained only
  the original 12 Spock features and no JUnit 5 tests. It has been redone here, and the test
  counts now genuinely match. Worth checking why that iteration's changes were lost.

### Improvements

- 🗂️ **Replace the `FileSystemStorage` stub before production.** It returns `logback.xml` for
  *any* UUID and uses `new File(getResource(...).getFile())`, which fails when running from a
  packaged jar. Fixing the jar case means moving `FilePointer` off `java.io.File` onto a
  `Resource`/`ByteSource` — an architecture change to a class whose own comment says *"Replace
  with real implementation"*, so the real storage backend must be chosen first. **Owner decision.**
  (Not hit by the tests because the `test` profile substitutes `FileStorageStub`.)
- 🧹 **Redundant explicit `org.springframework:spring-*` declarations** — `spring-beans`,
  `-context`, `-context-support`, `-webmvc`, `-web`, `-aop`, `-core`, `-expression` all arrive
  transitively via `spring-boot-starter-webmvc`. Now versionless and BOM-managed, so harmless
  and consistent, but deletable for clarity. Left in place to keep the diff minimal. The
  test-scoped `jakarta.servlet-api` is kept deliberately so MockMvc does not lean on
  `tomcat-embed-core` for the servlet API.
- 📦 **`spring-boot-maven-plugin` is not declared**, so `mvn package` produces a plain jar rather
  than an executable fat jar. This matches the original build; add the plugin if an executable
  artifact is wanted.
- 🔭 **Behavioural items no build can prove:** trailing-slash matching is no longer automatic
  (Framework 6+), and Boot 4 moves to Jackson 3 (`tools.jackson`). Neither affects this codebase
  — no direct Jackson usage, no trailing-slash routes — but confirm with real clients.
- 🔐 **Actuator exposure is already at Boot 4's conservative default** (`health` only; the project
  has no `application.properties`). Add `management.endpoints.web.exposure.include` **and** Spring
  Security together, only if more endpoints are genuinely wanted.
- 🚀 **Optional Boot 4 / Java 25 modernization not applied** (out of scope for a build-green
  cycle): virtual threads (`spring.threads.virtual.enabled=true`), `RestClient`, and replacing
  `java.util.Date` in `DownloadController`/`ExistingFile` with `java.time.Instant` end-to-end.
- 📄 **`README.md` still describes this as a "Sample Java 8 Project"** and was not updated.
- 🧊 The benign JVM notice `Sharing is only supported for boot loader classes because bootstrap
  classpath has been appended` comes from the Surefire `-javaagent` (ByteBuddy) argument; it is
  a CDS message, not a failure.
