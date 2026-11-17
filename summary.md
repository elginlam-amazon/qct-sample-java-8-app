# Java 21 Migration Summary

## Status: ✅ COMPLETE — Build passes with zero errors, 12/12 tests pass

## Build verification
- Bytecode target: Java 21 (major version 65)  
- Runtime used: JDK 25 (backward-compatible execution)  
- Test results: 12 tests run, 0 failures, 0 errors, 0 skipped

## Changes made

### pom.xml
- **Spring Boot parent**: `1.2.3.RELEASE` → `3.3.5` (Spring Framework 6.1, fully Java 21-compatible)
- **Removed** all explicit Spring Framework artifact versions (`spring-beans`, `spring-context`, `spring-context-support`, `spring-webmvc`, `spring-web`, `spring-aop`, `spring-core`, `spring-expression`, `spring-test`) — now managed by the Spring Boot parent BOM
- **Removed** explicit versions from `spring-boot-starter-*` artifacts
- **Removed** `cglib-nodep:3.1` — Spring Framework 6.x uses ByteBuddy internally; standalone cglib is incompatible with Java 21
- **Removed** duplicate `spring-test` dependency entry
- **Removed** HTTP milestone repository (`http://repo.spring.io/milestone/`) — not needed for release versions
- **Groovy**: `org.codehaus.groovy:groovy-all:2.4.3` → `org.apache.groovy:groovy:4.0.21` (groupId changed in Groovy 4.x; Groovy 2.4 is incompatible with Java 21)
- **Spock**: `1.0-groovy-2.4` → `2.3-groovy-4.0` for both `spock-core` and `spock-spring`
- **Added GMavenPlus plugin 3.0.2** to compile `src/test/groovy` sources — previously the Groovy tests were silently skipped (not compiled, not run)
- **Added Surefire `*Spec.java` include** to discover compiled Groovy Specification classes

### src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java
Spring Framework 6.x changed the override point from `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean) throws IOException`. Updated to match.

### src/main/resources/logback.xml
Logback 1.4.x (bundled with Spring Boot 3.x) dropped support for inline appenders inside `<root>`. Converted to named appender + `<appender-ref>` pattern.

### src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy
- Replaced `@ContextConfiguration(classes = [MainApplication])` with `@ContextConfiguration(loader = SpringBootContextLoader, classes = [MainApplication])`.  
  *Why*: The plain `AnnotationConfigWebContextLoader` creates a `GenericWebApplicationContext` via a fresh class loader, while `SpringBootContextLoader` uses Spring Boot's bootstrapper which shares the same application class loader as the test code.

### src/test/java/com/nurkiewicz/download/FileExamples.java
**Root cause fix**: Groovy 4.0's compiler evaluates `private static final String TEXT_FILE = '/download/' + FileExamples.TXT_FILE_UUID + '...'` **at compile time** (GMavenPlus loads and runs `FileExamples.<clinit>` during compilation) and embeds the resulting UUID as a `ConstantValue` attribute in the class bytecode. At test runtime, `FileExamples.<clinit>` runs again in the fresh JVM, calling `UUID.randomUUID()` a second time and generating a *different* UUID — so the stub's `TXT_FILE_UUID` never matched the compile-time constant string in `TEXT_FILE`, causing all file-serving tests to return 404.

Fixed by replacing `UUID.randomUUID()` with deterministic `UUID.fromString("fixed-value")` for both `TXT_FILE_UUID` and `NOT_FOUND_UUID`, so compile-time evaluation and runtime evaluation produce identical values.

## Namespace note
No `javax.*` → `jakarta.*` migration was required in source code: none of the application or test source files directly imported `javax.*` APIs. The namespace transition is handled transparently by upgrading to Spring Boot 3.x (which uses `jakarta.*` internally) and to Spock 2.3 / Groovy 4.0.

## Next steps

- **Smoke-test the running app**: `GET /actuator/health` → 200, `GET /download/{uuid}` → 301 (requires starting the embedded Tomcat)
- **Pin Maven wrapper**: Update `.mvn/wrapper/maven-wrapper.properties` to lock the Maven version validated here
- **Re-enable static analysis**: SpotBugs and Checkstyle were not configured in the original build; consider adding them and resolving any Java 21 findings
- **Address deprecated Guava APIs**: `Hashing.sha512()` and `com.google.common.io.Files.hash()` are deprecated in Guava 29+; migrate to `MessageDigest.getInstance("SHA-512")` or `Files.asByteSource(file).hash(Hashing.sha512())` in a follow-up
- **Expand ETag test coverage**: Add a test for the `isWeak=true` branch in `Sha512ShallowEtagHeaderFilter.generateETagHeaderValue` now that the method signature changed
- **Consider Spring Boot 4.x**: The app has no `javax.*` source imports, so a future upgrade to Spring Boot 4.x / Spring Framework 7.x (Jakarta EE 11) would have minimal migration surface
