# Java 25 / Spring Boot 4.0.9 upgrade

## Changes

### Earlier cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 7 replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`.
  The override now uses the new signature. It still hashes with SHA-512 and adds `W/` for weak ETags.

### This cycle
- **Spock tests restored** (`pom.xml`):
  - Spock `1.0-groovy-2.4` changed to `2.5-groovy-5.0` (`spock-core`, `spock-spring`). Spring Boot 4.0.9 manages Groovy 5.0.8.
  - `org.codehaus.groovy:groovy-all 2.4.16` (compile scope, pinned in `dependencyManagement`) replaced by Boot-managed `org.apache.groovy:groovy` in test scope. The main code does not use Groovy.
  - Removed the `junit`/`groovy-all` exclusions, which only applied to Spock 1.
  - Added `gmavenplus-plugin 5.1.0` (`addTestSources`, `compileTests`) so `src/test/groovy` is compiled.
  - Surefire now includes `**/*Spec`. The test class is `DownloadControllerSpec`, which the default includes miss.
  - The test file needed no changes.
- **POM cleanup**: removed the unused `spring.version` property, the plain-HTTP `http://repo.spring.io/milestone/` repository, and `cglib-nodep 3.1`. Neither main nor test code uses cglib, and Spring 7 ships its own repackaged copy.
- **Deprecated API**: in `FileSystemPointer`, `Files.hash(file, Hashing.sha512())` became `Files.asByteSource(file).hash(Hashing.sha512())`. The hash is the same, so ETags are unchanged.
- **Logging**: `logback.xml` declared its console appender inline inside `<root>` with no `name`. Logback 1.5 (Boot 4) rejects this with `Missing attribute [name]`, so nothing was written to the console. The appender now has a name (`CONSOLE`) and `<root>` refers to it with `<appender-ref>`. The pattern and logger levels are unchanged.

## Verification
- `mvn clean verify` on JDK 25: BUILD SUCCESS. `DownloadControllerSpec` ran 12 tests with 0 failures. They cover 200, 404 with an empty body, ETag 304 (GET and HEAD), `If-Modified-Since` 304 and 200, HEAD without a body, `Content-Length`, and `Content-Type`.
- Compiling with `-Xlint:all` gives no warnings.
- Smoke start (`spring-boot:run` with a time limit, stopped afterwards): the app started in about 2 s. `/actuator/health` returned `UP`. `HEAD /download/{uuid}/f.xml` returned 200 with `ETag`, `Last-Modified`, `Content-Type: application/xml` and `Content-Length`.

## Next steps
- The explicit `org.springframework:*` dependencies (beans, context, context-support, webmvc, web, aop, core, expression) and `spring-test` are BOM-managed and mostly redundant with the starters. I kept them to keep this change small. They can be removed in a later cycle.
- `spring-boot-starter-test` excludes `spring-test`, which is then declared again directly. This works but adds nothing. Consider simplifying it.
- `guava 29.0-jre` and `commons-io 2.4` are not managed by Boot. They work on JDK 25 and were left alone. Consider moving to current releases (for example guava 33.x and commons-io 2.x latest) in a dependency-refresh cycle.
- Actuator uses Boot 4 defaults: only `health` is exposed over HTTP. No `application.properties` exists, so there is nothing to migrate.
- `FileSystemStorage` is still the original stub that serves `logback.xml` for every UUID. It does not affect the upgrade.

## Re-verification after additional migration steps
- `mvn clean verify` on JDK 25: BUILD SUCCESS, with 12/12 tests passing in `DownloadControllerSpec`. There were no compilation errors to fix and no code changes were needed.
- No Java EE `javax.*` imports or artifacts remain. The dependency tree contains only jakarta artifacts (servlet-api 6.1.0, annotation-api 3.0.0, xml.bind-api 4.0.5), all managed by Boot 4.0.9, and Spring Framework 7.0.10 throughout.
