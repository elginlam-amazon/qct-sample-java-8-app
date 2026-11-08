# Java 21 Migration Summary

## What was done

### Spring Boot upgrade (Spring Boot 1.2.3.RELEASE → 3.5.16)

Spring Framework 4.x bundles an ASM that cannot verify Java 21 class files
(`Unsupported class file major version 65`). Spring Boot 3.5.16 (the last
patch of the 3.5.x line) ships Spring Framework 6.2.x which supports Java 21
bytecode natively.

No `javax.* → jakarta.*` namespace migration was required — the application
source only uses `org.springframework.*` annotations and the Java standard
library, neither of which moved to the Jakarta namespace.

### Build system

- `maven-compiler-plugin` 3.16.0 with `<release>21</release>` (was already set
  by OpenRewrite).
- `gmavenplus-plugin` 3.0.2 added to compile Groovy test sources from
  `src/test/groovy` (previously the Groovy spec was compiled but never run).
- `maven-surefire-plugin` include pattern extended with `**/*Spec.class` so
  Spock specifications are discovered (Surefire's default patterns only match
  `*Test` / `Test*`).

### Test framework (Spock/Groovy)

- `spock-core` and `spock-spring`: `1.0-groovy-2.4` → `2.4-groovy-4.0`
  (JUnit 5 Platform–based; Groovy 2.4 does not run on Java 21).
- `groovy-all`: `org.codehaus.groovy:groovy-all:2.4.3` (old Apache Codehaus
  groupId) → `org.apache.groovy:groovy-all:4.0.31` (pom BOM; the new Apache
  Software Foundation groupId since Groovy 4).

### Dependency cleanup

| Change | Reason |
|--------|--------|
| Removed all explicit `org.springframework:spring-*` pins | Spring Boot 3.5 BOM manages the full stack |
| Removed `cglib-nodep` | Spring 5+ bundles a repackaged CGLIB; standalone `cglib` conflicts |
| Removed duplicate `spring-test` declaration | Pulled in transitively by `spring-boot-starter-test` |
| `guava` 29.0-jre → 33.4.0-jre | Removes deprecated-API compile warnings |
| `commons-io` 2.4 → 2.17.0 | Current stable release |
| `commons-codec` version pin removed | Now managed by Spring Boot BOM |

### Source changes

**`FileSystemPointer.java`** — replaced deprecated
`com.google.common.io.Files.hash(file, fn)` with
`Files.asByteSource(file).hash(fn)`.

**`ExistingFile.java`** — replaced deprecated Guava `Optional.transform()` /
`.or()` with Java `Optional.map()` / `.orElse()`.

**`Sha512ShallowEtagHeaderFilter.java`** (test) — Spring 6 changed
`ShallowEtagHeaderFilter.generateETagHeaderValue` from `(byte[])` to
`(InputStream, boolean)`. Updated the override to match the new signature.

**`logback.xml`** — fixed a missing `name` attribute on the `<appender>`
element; without it Logback discarded all log output silently.

**`FileExamples.java`** (test) — changed `TXT_FILE_UUID` from
`UUID.randomUUID()` to a fixed UUID
(`00000000-0000-0000-0000-000000000001`).

*Root cause*: Spring Boot 3.5's test support loads application beans in an
isolated classloader (separate from the Groovy test classloader). With a
random UUID, `FileExamples` was initialised twice — once per classloader —
producing two different values.  The test URL used UUID *A* while
`FileStorageStub.TXT_FILE_UUID` held UUID *B*, so every lookup returned 404.
A fixed UUID compares equal regardless of which classloader holds the class.

**`application.properties`** (new) — enabled Java 21 virtual threads
(`spring.threads.virtual.enabled=true`).  The download server is entirely
I/O-bound (file reads, throttled streaming), so blocking operations park the
virtual thread rather than consuming a carrier thread.

## Build status

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Next steps

- **Pipeline configuration**: the automated OpenRewrite pass set
  `java.version=21` but left Spring Boot at `1.2.3.RELEASE`. Set
  `springBootTargetVersion=3.5` (or the current latest 3.x) in the pipeline
  so future runs upgrade the Spring Boot parent automatically.

- **Actuator endpoint path changed**: Spring Boot 3.x moved the health endpoint
  from `/health` to `/actuator/health`. Update any load-balancer, Kubernetes
  liveness/readiness probe, or monitoring configuration that still polls the
  old path.

- **Virtual threads and pinning**: `ThrottlingInputStream` uses Guava's
  `RateLimiter.acquire()`, which calls `Thread.sleep()` internally.
  `Thread.sleep()` on a virtual thread yields (does not pin) on Java 21, so
  this is safe.  If a `synchronized` block is ever added around I/O code in the
  future, verify it does not pin a carrier thread under high concurrency.

- **Groovy `groovy-all` BOM version drift**: `groovy-all:4.0.31` (declared)
  pulls in `groovy:4.0.32` transitively via `spock-core`. Align both to
  `4.0.32` in the next routine dependency update to eliminate the version
  mismatch in the dependency tree.
