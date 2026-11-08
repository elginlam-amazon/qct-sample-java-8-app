# Java 17 Migration Summary

## Result
✅ **BUILD SUCCESS** — zero compilation errors, all 12 tests pass.

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
```

---

## Changes made

### 1. `pom.xml` — Spring Boot upgrade + build toolchain

| Before | After | Reason |
|--------|-------|--------|
| `spring-boot-starter-parent:1.2.3.RELEASE` | `2.7.18` | Spring Boot 2.7 is the last javax line that supports Java 17. Spring 1.x bundles an ASM/CGLIB that cannot verify Java 17 class files (v61), causing `VerifyError` at runtime even though compilation passes. |
| Explicit Spring Framework 4.1.6.RELEASE pins (10 dependencies) | Removed — managed by Spring Boot 2.7 BOM (5.3.39) | BOM provides Spring Framework 5.3.39 which bundles an ASM compatible with Java 17 bytecode. |
| `spring.version` property (4.1.1.RELEASE) | Removed | Redundant and incorrect; BOM now manages Spring versions. |
| `cglib-nodep:3.1` | Removed | Spring 5.3 bundles a repackaged CGLIB internally; standalone cglib conflicts. |
| Duplicate `spring-test` dependency | Removed | Caused a Maven model warning. |
| `spock-core:1.0-groovy-2.4` | `spock-core:2.3-groovy-4.0` | Spock 1.x + Groovy 2.4 are not Java 17-compatible. Spock 2.x uses JUnit Platform. |
| `spock-spring:1.0-groovy-2.4` | `spock-spring:2.3-groovy-4.0` | Same — required for Spring context integration in Spock 2.x. |
| `groovy-all:2.4.3` (codehaus) | `org.apache.groovy:groovy:4.0.21` | Groovy 4 moved to the `org.apache.groovy` groupId; Groovy 2.4 is incompatible with Java 17. |
| No Groovy compilation plugin | Added `gmavenplus-plugin:3.0.2` | Without it, `.groovy` test sources are never compiled, so all Spock specs were silently skipped (0 tests running). |
| `maven-surefire-plugin:2.17` | `3.2.5` with `**/*Spec.java` include | Surefire 3.x supports JUnit Platform (required for Spock 2.x). Pattern added to discover `DownloadControllerSpec`. |
| `commons-io:2.4` | `2.15.1` | Updated to a release compatible with Java 17 (2.4 predates Java 9). |
| Milestone repository (`http://repo.spring.io/milestone/`) | Removed | Spring Boot 2.7.18 GA is in Maven Central; milestone repo no longer needed. |

### 2. `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`

Spring 5.1 changed the protected override point in `ShallowEtagHeaderFilter`:

```java
// Before (Spring 4.x)
protected String generateETagHeaderValue(byte[] bytes) { ... }

// After (Spring 5.1+)
protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException { ... }
```

Updated the method signature and implementation to use `ByteStreams.toByteArray(inputStream)` from Guava (already on the classpath).

### 3. `src/test/java/com/nurkiewicz/download/FileExamples.java`

Changed `TXT_FILE_UUID` and `NOT_FOUND_UUID` from `UUID.randomUUID()` to deterministic fixed values.

**Root cause:** Spring Boot 2.7's `SpringBootContextLoader` loads the application context through `SpringApplication.run()`, which resolves classes via a different classloader instance than the one Spock/Surefire uses to load the Groovy spec class. Since `UUID.randomUUID()` produces a different value each time the class is initialised, the UUID seen by `FileStorageStub` (Spring classloader) differed from the UUID embedded in the test URL (Spock classloader), causing all `FileStorageStub.findFile(uuid)` calls to return `Optional.empty()` and every test to get a 404.

Using a fixed, well-known UUID eliminates this classloader-boundary race condition without changing test semantics.

---

## Spring Framework version — post-upgrade confirmation

Spring Boot 2.7.18 BOM manages Spring Framework **5.3.39**, which:
- Bundles ASM 9.x — understands Java 17 class file format (v61)
- Stays on the **javax** namespace (no jakarta migration needed or performed)
- Is the last Spring Framework 5.x release (LTS on javax)

---

## What was NOT changed

- No javax → jakarta migration (correct for this target: Spring Boot 2.7 / Spring Framework 5.3)
- No changes to production source files (`DownloadController`, `ExistingFile`, `FileSystemPointer`, etc.)
- Guava remains at 18.0 (compiles and runs correctly on Java 17 classpath)
- No changes to `logback.xml` or resource files

---

## Next steps

- **Guava deprecations**: `Hashing.sha512()` and `Files.hash()` are deprecated in Guava 25+. The project still uses Guava 18.0. Upgrading Guava to 32.x would require addressing `Optional.transform()` / `Optional.or()` usages in `ExistingFile.java` (move to Java's `java.util.Optional`). Not required for a successful build on Java 17, but recommended for long-term maintainability.
- **`java.util.Date` in `DownloadController`**: `@RequestHeader Optional<Date>` uses the deprecated `java.util.Date`. Consider migrating to `Instant` or `ZonedDateTime` in a future iteration.
- **Spring Boot 3 / Jakarta**: If a future upgrade targets Java 21+, Spring 5.3 (javax) cannot proxy Java 21 bytecode. At that point, a full Spring Boot 3.x / jakarta namespace migration is required.
