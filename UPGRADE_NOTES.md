# Java 21 Upgrade Notes

This project was upgraded from Java 8 / Spring Boot 1.2.3.RELEASE to Java 21 / Spring Boot 3.2.12.

## Deliberate deviations and skipped updates

- **Jackson pinned to 2.18.11** (instead of the mandated 2.15.2 floor). The Spring Boot 3.2.12
  BOM manages jackson-databind 2.15.4, which carries known HIGH/MEDIUM CVEs fixed in 2.18.11.
  The `jackson.version` / `jackson-bom.version` properties override the BOM for remediation.
- **Spock 2.4-M4-groovy-4.0 (pre-release)**. The last GA Spock release does not support
  Spring 6 / Spring Boot 3 context injection. Pin to 2.4 GA once it is published.
- **Fat-jar packaging (`spring-boot-maven-plugin`) deferred**. `FileSystemStorage` resolves a
  classpath resource as a `java.io.File`, which does not work from inside a JAR. Refactoring it
  to stream via `ClassPathResource` is a prerequisite and was out of scope for a version upgrade.

## Notable compatibility changes applied

- `Sha512ShallowEtagHeaderFilter` adapted to the Spring 6
  `generateETagHeaderValue(InputStream, boolean)` signature.
- `DownloadControllerTest.groovy` migrated from `@ContextConfiguration` to `@SpringBootTest`.
- `logback.xml` rewritten to the `appender` / `appender-ref` form required by Logback 1.4.
- Removed the deprecated `new Integer("1234")` usage in `MainApplication`.
- Removed the insecure `http://repo.spring.io/milestone` repository and the explicit Spring 4.x
  module pins (now BOM-managed).
- Groovy test compilation wired up via `gmavenplus-plugin` with surefire `**/*Spec.java` includes,
  so the Spock suite now actually executes (12 tests, all passing).
- Fixed UUIDs in `FileExamples` so the Groovy spec and the Java stub agree across separate
  class initialization in the surefire JVM.
