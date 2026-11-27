# Java 17 upgrade — download-server

Target: **Java 17 on the javax-terminal Spring line** (Spring Boot 2.7 / Spring Framework 5.3), per
knowledge-base doc 25. No `javax.*` → `jakarta.*` migration was performed, and no Spring Boot 3 /
jakarta recipe was run.

Result: `mvn clean verify` succeeds with **zero compilation errors** and **12/12 tests passing**
(verified twice from clean).

## What the OpenRewrite pass had done

Only three things: added `java.version=17`, bumped `maven-compiler-plugin` to 3.16.0 with
`<release>17</release>`, bumped `commons-codec`, and rewrote `new Integer("1234")` →
`Integer.valueOf("1234")`. The whole Spring stack was left on Spring Boot 1.2.3 / Spring Framework
4.1.6, which cannot run on Java 17 (Spring 4.x's bundled CGLIB/ASM cannot read class-file v61).
Completing that version bump was the backstop work below.

## Changes

### `pom.xml`

- **Spring Boot parent 1.2.3.RELEASE → 2.7.18**, the last javax Boot line and the only Boot version
  supported on Java 17 without crossing into jakarta. This pins Spring Framework **5.3.31** and
  embedded **Tomcat 9.0.83**, so `javax.servlet` is preserved throughout.
- Dropped the hard-coded `4.1.6.RELEASE` / `1.2.3.RELEASE` versions from every Spring and Spring Boot
  dependency so the Boot BOM governs them. Effective Spring Framework version is 5.3.31 across the
  single module; no stale or mixed Spring versions remain.
- Removed the duplicate `org.springframework:spring-test` declaration (Maven was warning that
  `dependencies.dependency` must be unique) and the now-pointless `spring-test` exclusion on
  `spring-boot-starter-test`.
- Removed the unused `spring.version=4.1.1.RELEASE` property (nothing referenced it).
- Removed the standalone **`cglib:cglib-nodep:3.1`** dependency — unused in the codebase and an old
  CGLIB/ASM pairing that does not understand Java 17 bytecode.
- Removed the `http://repo.spring.io/milestone/` repository: Maven 3.9 blocks plain-HTTP repositories
  (`maven-default-http-blocker`), and every artifact resolves from Central.
- `guava 18.0 → 33.4.0-jre`, `commons-io 2.4 → 2.16.1`.
- **Test framework moved onto Java 17**: `groovy-all 2.4.3` → Boot-managed `org.codehaus.groovy:groovy`
  (3.0.19) at `test` scope, and Spock `1.0-groovy-2.4` → **`2.3-groovy-3.0`** for both `spock-core`
  and `spock-spring`. Groovy 2.4 and Spock 1.x cannot run on Java 17 at all.
- Added the **`gmavenplus-plugin`** (`addTestSources` + `compileTests`) and surefire `**/*Spec.java`
  includes. See "The Spock suite was never running" below.

### `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`

The only genuine compilation error. `ShallowEtagHeaderFilter`'s extension point changed in Spring 5:
`generateETagHeaderValue(byte[])` → `generateETagHeaderValue(InputStream, boolean isWeak)`, so the
`@Override` no longer resolved. Ported to the new signature, keeping the SHA-512 ETag logic. The
`isWeak` flag is now honoured with a `W/` prefix, matching the new contract; since `writeWeakETag`
defaults to `false`, default behaviour is byte-for-byte unchanged.

### `src/test/java/com/nurkiewicz/download/FileExamples.java`

`TXT_FILE_UUID` / `NOT_FOUND_UUID` changed from `UUID.randomUUID()` to fixed UUID literals. No
assertion was altered. Rationale under "Root cause" below.

## The Spock suite was never running

Before this cycle the build reported `BUILD SUCCESS` while executing **zero** tests:
`src/test/groovy` was not a compile source root (no Groovy compiler plugin was configured), so
`DownloadControllerTest.groovy` was never compiled, and the class it declares is named
`DownloadControllerSpec`, which matches none of surefire's default include patterns. Wiring in
gmavenplus and adding a `**/*Spec.java` include makes all 12 specs compile and execute, which is
what actually verifies the Boot 1.2.3 → 2.7 jump. The spec file itself was not modified.

### Root cause of the 11 initial test failures

Once the specs ran, 11 of 12 returned **404** where 200/304 was expected. The handler was mapped and
`FileStorageStub.findFile` was invoked with the UUID from the request URL, yet returned
`Optional.empty()`.

The Groovy compiler **constant-folds** the spec's
`private static final String TEXT_FILE = '/download/' + FileExamples.TXT_FILE_UUID + '/file.txt'`.
It loads `FileExamples` at *compile* time, reads the static field, and emits the result as a
`ConstantValue` attribute — the compiled spec has no `<clinit>` at all. With
`TXT_FILE_UUID = UUID.randomUUID()`, the UUID baked into the class file at build time can never
equal the fresh random UUID generated at run time, so the stub's `uuid.equals(TXT_FILE_UUID)` check
could never succeed.

Fixing this in the fixture (fixed UUID literals) makes compile-time and run-time values agree and
makes the suite deterministic. Every assertion is intact; nothing was disabled, skipped, relaxed or
deleted.

### Notable Spring 5.3 behaviour confirmed by the suite

`should not return file if wasn't modified recently` passes because Spring MVC 5.3 now applies
conditional-request handling to a returned `ResponseEntity` carrying `ETag` / `Last-Modified`, and
compares `If-Modified-Since` at **second** precision. The application's own
`FilePointer.modifiedAfter` compares at millisecond precision and therefore would have served 200;
the framework downgrades it to 304. Application code was deliberately left untouched here — the
behaviour is correct and deterministic, not a flake.

## Next steps (non-blocking)

- `FileSystemPointer:22` emits the build's only warning: `com.google.common.io.Files.hash(File,
  HashFunction)` is deprecated in Guava 33. It is still present and functional, so it was left alone
  to keep this change minimal. The documented replacement is
  `Files.asByteSource(target).hash(Hashing.sha512())`; the ETag assertions in `DownloadControllerSpec`
  would catch any behavioural difference if someone makes that swap.
- `FilePointer.modifiedAfter` compares `Instant`s at millisecond precision while HTTP dates carry
  only seconds. Today Spring MVC's own conditional-request check masks the mismatch. If that
  `ResponseEntity`-based caching is ever replaced with manual handling, truncate to seconds
  (`getLastModified().truncatedTo(ChronoUnit.SECONDS)`) or the 304 responses will regress to 200.
- `spring-context-support` and `commons-codec` are declared but unreferenced by any source file. They
  were kept to hold this change to the upgrade itself; they are safe to drop in a cleanup pass.
- `spring-boot-maven-plugin` is still not declared, so `mvn package` produces a plain (non-executable)
  jar, exactly as before the upgrade. Add the plugin's `repackage` goal if an executable fat jar is
  wanted.
- Going beyond Java 17 (21/25) would require leaving javax: Spring Boot 2.7 is supported only through
  Java 19, and Groovy below 4.0 is not Java 21-compatible. That is a Spring Boot 3.x / jakarta
  migration and is explicitly out of scope for this namespace-safe track.
- Verification is MockMvc-level only; the application was not started. `logback.xml` still sets
  `root level="DEBUG"` with `com.nurkiewicz` at `ALL`, which is very verbose for production.
