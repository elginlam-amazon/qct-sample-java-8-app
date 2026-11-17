# Java 21 Migration Summary

## Status: COMPLETE — Build passes with zero errors, all 12 tests pass

---

## Changes Made

### 1. `pom.xml` — Spring Boot upgrade + Groovy/Spock upgrade
- **Spring Boot parent**: 1.2.3.RELEASE → 3.3.6 (supports Java 21 LTS)
- **Removed**: Explicit Spring Framework 4.1.6.RELEASE dependency declarations (now managed by Spring Boot BOM)
- **Removed**: `spring.version` property (no longer needed)
- **Removed**: `cglib-nodep:3.1` (Spring Boot 3.x uses ByteBuddy; cglib 3.x is incompatible with Java 17+)
- **Removed**: Duplicate `spring-test` dependency declaration
- **Removed**: Milestone repository (not needed for Spring Boot 3.3.6 stable release)
- **Updated**: `commons-io` 2.4 → 2.18.0 (Java 21 compatible)
- **Updated**: `guava` 29.0-jre → 33.4.0-jre (Java 17+ requires 31.0+)
- **Updated**: `commons-codec` — now version-managed by Spring Boot BOM
- **Updated**: Spring Boot starter dependencies — versions now managed by BOM
- **Updated**: `groovy-all` org.codehaus.groovy:2.4.3 → `org.apache.groovy:groovy:4.0.23` (Java 21 requires Groovy 4.x; groupId changed in Groovy 4.0)
- **Updated**: `spock-core` 1.0-groovy-2.4 → 2.3-groovy-4.0 (Spock 2.x uses JUnit Platform, required for Java 21)
- **Updated**: `spock-spring` 1.0-groovy-2.4 → 2.3-groovy-4.0
- **Added**: `gmavenplus-plugin:3.0.2` — Groovy test sources were silently ignored (not compiled or run) because there was no Groovy compiler plugin configured
- **Added**: Maven Surefire configuration to include `*Spec` test classes (Surefire defaults to `*Test.java` patterns)

### 2. `src/main/resources/logback.xml` — Fix Logback 1.4+ compatibility
- Moved inline `<appender>` from inside `<root>` to top-level with a `name` attribute
- Changed `<root>` to reference the appender via `<appender-ref ref="CONSOLE"/>`
- Required because Logback 1.4+ (used in Spring Boot 3.x) rejects unnamed inline appenders

### 3. `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` — Spring Framework 5+ API
- Updated overridden method signature from `generateETagHeaderValue(byte[] bytes)` (Spring 4.x) to `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException` (Spring 5+/6.x)
- Used `inputStream.readAllBytes()` (available since Java 9) to get bytes for hashing

### 4. `src/test/java/com/nurkiewicz/download/FileExamples.java` — Fixed UUID constants
- Changed `UUID.randomUUID()` to fixed `UUID.fromString(...)` constants for `TXT_FILE_UUID` and `NOT_FOUND_UUID`
- Required because `SpringBootContextLoader` (used by Spring Boot 3.x test infrastructure) initializes the Spring application context in a way that caused `FileExamples` to be loaded with a fresh static initialization, producing different random UUIDs than the test expected

### 5. `src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy` — Spring Boot 3.x test setup
- Replaced `@ContextConfiguration(classes = [MainApplication])` + `@WebAppConfiguration` with `@ContextConfiguration(loader = SpringBootContextLoader, classes = [MainApplication])` + `@WebAppConfiguration`
- Used `SpringBootContextLoader` explicitly to ensure Spring Boot 3.x's context loading mechanism is used
- Changed from `@Autowired WebApplicationContext` setter method injection to field injection + `setup()` method
- Removed unused imports (`MockMvcBuilders` was retained for `webAppContextSetup`)

---

## Root Causes Fixed

| Issue | Root Cause | Fix |
|-------|-----------|-----|
| Groovy/Spock tests not running | No Groovy compiler plugin; Spock 1.0 incompatible with Java 21 | Added gmavenplus-plugin; upgraded to Spock 2.3-groovy-4.0 |
| Spring context startup failure | Logback 1.4+ requires appender `name` attribute | Fixed logback.xml |
| `Sha512ShallowEtagHeaderFilter` compile error | Spring Framework 5+ changed method signature | Updated override method signature |
| Test 404s for TXT_FILE_UUID | Spring Boot 3.x test context re-initializes static fields | Used fixed UUID constants |
| `cglib-nodep` incompatibility | cglib 3.1 uses internal JDK APIs removed in Java 17 | Removed (Spring Boot 3.x uses ByteBuddy) |

---

### 6. `src/main/java/com/nurkiewicz/download/FileSystemPointer.java` — Remove deprecated Guava API
- Changed `Files.hash(target, Hashing.sha512())` → `Files.asByteSource(target).hash(Hashing.sha512())`
- `com.google.common.io.Files.hash()` was deprecated in Guava 25.1; `asByteSource().hash()` is the modern equivalent

---

## Next Steps

None — all 12 Spock tests pass, zero compilation errors, zero deprecation warnings.
