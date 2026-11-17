# Java 21 Migration Summary

## Status: COMPLETE — BUILD SUCCESS, 12/12 tests passing

## Changes Made

### 1. `pom.xml` — Full dependency and build upgrade

- **Spring Boot parent**: `1.2.3.RELEASE` → `3.3.13` (Spring Framework 6.1.x, Jakarta EE 10)
- **Removed** stale `spring.version` property and all explicit Spring Framework dependency overrides (spring-beans, spring-context, spring-webmvc, etc.) — Spring Boot BOM now manages them
- **Removed** explicit Spring Boot starter versions — parent BOM manages them
- **Removed** duplicate `spring-test` dependency declaration
- **Removed** `cglib-nodep:3.1` — Spring 6.x bundles its own CGLIB
- **Removed** legacy Spring milestone repository — Spring Boot 3.x is on Maven Central
- **Updated** `commons-io:2.4` → `2.17.0`
- **Updated** `guava:29.0-jre` → `33.3.1-jre`
- **Updated** Spock: `spock-core:1.0-groovy-2.4` + `spock-spring:1.0-groovy-2.4` → `2.4-groovy-4.0` (Spock 2.x uses JUnit 5 platform)
- **Updated** Groovy: `org.codehaus.groovy:groovy-all:2.4.3` → `org.apache.groovy:groovy:4.0.33` (groupId change in Groovy 4.x)
- **Added** GMavenPlus plugin `5.1.0` to compile `src/test/groovy/` Spock tests (previously the Groovy tests were silently uncompiled and unexecuted)
- **Added** maven-surefire-plugin include for `**/*Spec.class` so Spock specification classes are discovered

### 2. `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`

Spring Framework 5.0+ changed `ShallowEtagHeaderFilter.generateETagHeaderValue` signature from `(byte[])` to `(InputStream, boolean)`. Updated accordingly:
- **Before**: `protected String generateETagHeaderValue(byte[] bytes)`
- **After**: `protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`
- Uses `inputStream.readAllBytes()` (available since Java 9) to read bytes before hashing with SHA-512

### 3. `src/test/java/com/nurkiewicz/download/FileExamples.java`

`TXT_FILE_UUID` and `NOT_FOUND_UUID` changed from `UUID.randomUUID()` to fixed constants:
- **Why**: Spring Boot 3.x's `SpringBootContextLoader` initializes the application context using a different class-loading path than the test class. This causes `FileExamples` to be loaded twice, each generating a different random UUID. `FileStorageStub` (loaded by Spring context) and the Spock test class (loaded by Surefire) end up with different UUID values, so `findFile(uuid)` always returns empty → 404.
- **Fix**: `UUID.fromString("11111111-1111-1111-1111-111111111111")` and `UUID.fromString("22222222-2222-2222-2222-222222222222")` — deterministic values survive class loader boundaries.

## Test Results

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

All 12 Spock specification tests now compile and pass, including:
- File download (200 OK)
- 404 for unknown UUIDs
- ETag-based conditional requests (304 Not Modified)
- If-Modified-Since conditional requests (304 Not Modified / 200 OK)
- HEAD requests with content-length and content-type
