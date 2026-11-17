# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade notes

Target: Java 21 (`maven.compiler` `release` 21), Spring Boot 3.5.16 (Spring Framework 6.2.19), Spock 2.5-groovy-5.0 / Groovy 5.0.8.

### Deviations from mandated versions / skipped or deferred updates
- **Spring Boot 3.5.16 instead of 3.2.12**: the 3.2.12 BOM resolves Spring Framework 6.1.15, Tomcat 10.1.33, logback 1.4.14
  and Spring Boot 3.2.12 itself with many HIGH/CRITICAL CVEs (e.g. CVE-2025-41249, CVE-2025-24813, CVE-2025-22235) that
  have no fix in the 6.1 / 3.2 lines. 3.5.x is the latest Spring Boot 3.x line and ships Spring Framework 6.2.x.
- **Jackson 2.21.8 instead of 2.15.2** (`jackson-bom.version`): 2.15.2 has several HIGH CVEs (e.g. CVE-2026-54512, CVE-2026-91777).
- **Tomcat 10.1.62** (`tomcat.version`): overrides Boot's managed 10.1.55, which has CRITICAL CVEs (e.g. CVE-2026-65905).
- **Deferred - Spring Boot 4.x / Spring Framework 7.x**: spring-webmvc 6.2.19 still reports CVE-2026-47884 (XsltView path
  limitation) and CVE-2026-47890 (SSE fragment rendering), both CRITICAL, fixed only in Spring Framework 7.0.x.
  This application uses neither XsltView nor SSE fragment rendering. Moving to Boot 4 (Jakarta EE 11) is a separate
  major upgrade that is out of scope for this Java version upgrade.
- **Groovy 5 / Spock 2.5 instead of Groovy 4 / Spock 2.4**: the Groovy compiler and Spock run on the JVM that starts Maven.
  Groovy 4 cannot read JDK 25 class files ("Unsupported class file major version 69"). Groovy bytecode is still targeted at 21.
- **Removed**: `cglib:cglib-nodep` (unused, Spock 2 mocks use byte-buddy). The duplicate `spring-test` declaration.
  The insecure `http://repo.spring.io/milestone` repository (not needed, and blocked by Maven 3.8+).
  `groovy-all` 2.4.3 is replaced by `org.apache.groovy:groovy` (test scope, only tests use Groovy).
- Explicit Spring Framework module versions were removed so the Spring Boot BOM manages them.
- No Jakarta, database, Lombok or Gradle changes were needed (no javax.* EE imports, no DB/ORM, Maven only).

### Test suite
The Spock suite (`src/test/groovy`) was never compiled before (0 tests ran at the Java 8 baseline). It is now wired via
gmavenplus-plugin 5.1.0 plus a surefire `*Spec` include. All 12 specs pass on JDK 21 and JDK 25.
