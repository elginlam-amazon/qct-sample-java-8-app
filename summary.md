# Java 21 Migration Summary

## Migration Path

Spring Boot 1.2.3.RELEASE (Spring Framework 4.1.6, Java 8) → Spring Boot 3.5.16 (Spring Framework 6.2.x, Java 21)

Per the namespace guardrail in the migration guidelines, Java 21 has **no javax-preserving option** (Spring Boot 2.7 / Spring 5.3 requires Java 17 only). The migration targets Spring Boot 3.5 (jakarta namespace) as the correct path.

## Build Result

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Changes Made

### pom.xml
- **Spring Boot parent**: `1.2.3.RELEASE` → `3.5.16`
- **Removed** stale `spring.version` property and all explicit Spring Framework dependency overrides (`spring-beans`, `spring-context`, `spring-context-support`, `spring-web`, `spring-webmvc`, `spring-aop`, `spring-core`, `spring-expression`) — now managed by the Boot BOM
- **Removed** `cglib-nodep:3.1` — Spring 6 uses ByteBuddy internally; standalone cglib is no longer needed
- **Removed** duplicate `spring-test` dependency and old `http://repo.spring.io/milestone/` repository
- **Removed** explicit versions from Boot-managed starters (`spring-boot-starter-web`, `-actuator`, `-test`)
- **Updated** `commons-io` to `2.18.0` (not managed by Boot BOM; must be explicit)
- **Updated** `commons-codec` — version removed, now managed by Boot BOM at `1.18.0`
- **Updated** Guava `29.0-jre` → `33.8.0-jre` (required for Java 21; avoids `sun.misc.Unsafe` usages in older Guava)
- **Replaced** `org.codehaus.groovy:groovy-all:2.4.3` with `org.apache.groovy:groovy:4.0.33` (groupId changed in Groovy 4.x; Java 21 compatible)
- **Replaced** `spock-core` and `spock-spring` `1.0-groovy-2.4` → `2.5-groovy-4.0` (JUnit 5 based; Java 21 compatible)
- **Added** GMavenPlus 4.1.1 plugin to compile Groovy test sources (was absent; Groovy tests were silently uncompiled)
- **Added** `junit-platform-launcher` to enable JUnit Platform test discovery by Surefire
- **Added** Surefire `*Spec` include pattern so `DownloadControllerSpec` is discovered by default

### src/main/resources/logback.xml
- Fixed invalid configuration: inline `<appender>` without a `name` attribute was silently accepted by Logback 1.2.x but became a **fatal error** in Logback 1.5+ (bundled with Spring Boot 3.5), preventing context startup
- Restructured to named `<appender name="CONSOLE">` with an `<appender-ref>` in `<root>`

### src/main/java/…/FileSystemPointer.java
- Replaced deprecated `com.google.common.io.Files.hash(File, HashFunction)` with `Files.asByteSource(file).hash(hashFunction)` (Guava 23+ replacement)

### src/test/java/…/Sha512ShallowEtagHeaderFilter.java
- Updated `generateETagHeaderValue` override signature from the Spring 4.x `(byte[] bytes)` form to the Spring 5+/6.x form `(InputStream inputStream, boolean isWeak) throws IOException`; reads the stream with `readAllBytes()` before hashing

### src/test/groovy/…/DownloadControllerTest.groovy
- Replaced `@WebAppConfiguration + @ContextConfiguration(classes=[MainApplication])` with `@SpringBootTest(webEnvironment=MOCK) + @AutoConfigureMockMvc` — the old test pattern does not trigger Spring Boot 3.x's `DispatcherServlet` handler mapping registration, causing all endpoints to return 404
- Switched from manual `MockMvcBuilders.webAppContextSetup(wac).build()` setter injection to injected `@Autowired MockMvc` (auto-configured by `@AutoConfigureMockMvc`)

### src/test/java/…/FileExamples.java
- Changed `TXT_FILE_UUID` and `NOT_FOUND_UUID` from `UUID.randomUUID()` to `UUID.fromString("fixed-string")` constants
- **Root cause**: Spring Boot 3.x `@SpringBootTest` uses an isolated classloader for the application context beans. `FileExamples` was loaded by **two separate classloaders**, generating different random UUIDs. The test URL used UUID from the test classloader; `FileStorageStub` checked UUID from the Spring context classloader — always a mismatch, always returning 404

## Next Steps

- **Smoke-test at runtime**: `java -jar target/download-server-0.0.1-SNAPSHOT.jar` and exercise `GET /download/{uuid}/{filename}`; not performed here per migration guidelines (compile + unit test only)
- **CVE scan**: run `mvn org.owasp:dependency-check-maven:check` against the final dependency set
- **Review Guava Optional usage** in `ExistingFile.java` — `com.google.common.base.Optional.transform()` and `.or()` are deprecated; migrate to `java.util.Optional` idioms in a follow-up
- **Merge branch** after team review
