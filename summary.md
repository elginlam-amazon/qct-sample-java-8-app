# Java 21 Migration Summary

## Build Status
**BUILD SUCCESS** — 12 tests, 0 failures, 0 errors (`./mvnw verify`)

## Changes Made

### Iteration 1

#### `pom.xml` — Major Spring Boot and dependency upgrade
- Spring Boot parent: `1.2.3.RELEASE` → `3.5.16` (Spring Framework 6.2.x)
- Removed: individual Spring Framework 4.1.6.RELEASE dependency overrides (managed by Boot)
- Removed: `cglib-nodep:3.1` (not needed — Spring 6.x inlines CGLIB)
- Removed: duplicate `spring-test` dependency and Spring milestones repository
- Upgraded: `commons-io` 2.4 → 2.22.0
- Replaced: `org.codehaus.groovy:groovy-all:2.4.3` + `spock-*:1.0-groovy-2.4` with `spock-*:2.4-groovy-4.0` (Groovy 2.4.x is incompatible with Java 21+)
- Added: `junit-platform-launcher` (required for Surefire 3.x JUnit Platform test discovery)
- Added: `gmavenplus-plugin:3.0.2` to compile Groovy test sources
- Added: Surefire include pattern for `**/*Spec*` classes

#### `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`
- Updated overridden method from `generateETagHeaderValue(byte[])` (Spring 4.x) to `generateETagHeaderValue(InputStream, boolean)` (Spring 5+/6+ API)

#### `src/main/resources/logback.xml`
- Fixed Logback 1.3+ format: moved `<appender>` definition out of `<root>`, added `<appender-ref>`

#### `src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy`
- Replaced `@ContextConfiguration` + `@WebAppConfiguration` with `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`
- Changed MockMvc from setter injection to `@Autowired` field injection

#### `src/test/java/com/nurkiewicz/download/FileExamples.java`
- Changed `UUID.randomUUID()` → fixed UUIDs (`UUID.fromString("11111111-...")`)
- **Root cause fixed**: Surefire 3.x JUnit Platform uses a different context classloader, causing `FileExamples` to initialize twice with different random UUIDs, breaking `FileStorageStub`'s UUID comparison

### Iteration 2

#### `pom.xml`
- Spring Boot `3.3.13` → `3.5.16` (latest 3.5.x; better Java 25 JVM support, Spring Framework 6.2.x)
- Added `-XX:+EnableDynamicAgentLoading` to Surefire `argLine` (suppresses ByteBuddy dynamic agent warning on Java 21+)

#### `src/main/java/com/nurkiewicz/download/FileSystemPointer.java`
- Replaced deprecated `com.google.common.io.Files.hash(File, HashFunction)` (deprecated since Guava 26) with `Files.asByteSource(file).hash(hashFunction)` (non-deprecated equivalent)

#### `src/main/resources/application.properties` (new file)
- Added `spring.threads.virtual.enabled=true` to enable Java 21 virtual threads via Spring Boot 3.2+ integration (beneficial for I/O-bound file serving)

## Architecture Notes
- Actuator health endpoint is available at `/actuator/health` (Spring Boot 3.x path; was `/health` in 1.x)
- Virtual threads are now enabled for all server threads via Tomcat's virtual thread executor
- The `@Profile("!test")` / `@Profile("test")` pattern for `FileStorage` implementations works correctly with Spring Boot 3.x `@SpringBootTest`

## Next Steps
- None: migration is complete and all tests pass.
