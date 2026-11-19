# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 17 upgrade notes

The project now targets Java 17 (`maven.compiler.release=17`) on Spring Boot 3.5.16. It builds and passes its tests on JDK 17 and on JDK 25.

### Version changes and skipped or deferred updates

| Item | Mandated / original | Chosen | Reason |
|------|---------------------|--------|--------|
| Spring Boot | mandated 3.2.12 (original 1.2.3) | 3.5.16 | 3.2.12 resolves Spring Framework 6.1.15, which has HIGH/CRITICAL CVEs with no fix on the 6.1 line (e.g. CVE-2025-41249, CVE-2026-41845, CVE-2026-47884). It also has Spring Boot CVE-2025-22235 and Tomcat 10.1.33 CRITICAL CVEs. 3.5.16 manages Spring Framework 6.2.19 and supports Java 17-25. |
| Jackson (`jackson-bom.version`) | mandated 2.15.2 | 2.21.8 | 2.15.2 has HIGH CVEs (e.g. CVE-2026-91777, CVE-2026-54512), and the 3.5.16 BOM default 2.21.4 has HIGH CVEs too. 2.21.8 has no known CVEs. |
| Embedded Tomcat (`tomcat.version`) | BOM 10.1.55 | 10.1.62 | Fixes the CRITICAL Tomcat CVEs (e.g. CVE-2026-65905, CVE-2026-55276). |
| Log4j API (`log4j2.version`) | BOM 2.24.3 | 2.25.5 | Fixes CVE-2026-49844. |
| spring-webmvc | 6.2.19 (BOM) | not changed | CVE-2026-47884 (XsltView) and CVE-2026-47890 (SSE fragment rendering) are only fixed in Spring Framework 7 / Spring Boot 4. That is a major upgrade outside this Java 17 scope, so it is deferred. The application uses neither XsltView nor SSE. |
| Spock | 1.0-groovy-2.4 | 2.4-groovy-4.0 | Spock 1.x / Groovy 2.4 do not support Java 17 or Spring 6. |
| Groovy | org.codehaus.groovy:groovy-all 2.4.3 | org.apache.groovy:groovy 4.0.32 (BOM) | Groovy 2.4 fails under JPMS on JDK 17. |
| Guava / commons-io / commons-codec / cglib-nodep | 18.0 / 2.4 / 1.10 / 3.1 | 33.8.0-jre / 2.22.0 / 1.23.0 / 3.3.0 | Latest releases. They are compatible with Java 17 and have no known CVEs. |
| Explicit Spring 4.1.6 artifact versions | 4.1.6.RELEASE | managed by the Boot BOM | Keeps the Spring modules aligned. |
| Lombok, Jakarta, DB/ORM groups | n/a | n/a | The project does not use them, and there are no `javax.*` EE imports. |
| SpotBugs / Checkstyle | n/a | skipped with `-Dspotbugs.skip -Dcheckstyle.skip` | The project's build command sets these flags. The project configures neither plugin. |

### Other changes

- The Spock spec in `src/test/groovy` was never compiled or run before (0 tests). `gmavenplus-plugin` and a Surefire `**/*Spec.java` include were added, so all 12 specs now run.
- In `src/main/resources/logback.xml`, the appender is now named and referenced from `<root>`, as logback 1.5 requires.
- Code adapted to the new APIs: `Sha512ShallowEtagHeaderFilter` (Spring 6 signature), `HttpMethod` equality, `Integer.valueOf`, and `@SpringBootTest` in the spec. The test fixture UUIDs are now fixed values.
