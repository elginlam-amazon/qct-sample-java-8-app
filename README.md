# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 17 upgrade notes

The project now builds with `release 17` on Spring Boot 3.5.16 (Spring Framework 6.2.19, Jakarta EE 10).

### Skipped / deferred updates and deviations from mandated versions

| Item | Mandated | Chosen | Reason |
|------|----------|--------|--------|
| Spring Boot | 3.2.12 | 3.5.16 | 3.2.12 resolves Spring Framework 6.1.15 and Tomcat 10.1.33. Those carry many CRITICAL/HIGH CVEs, and some (for example CVE-2025-41249) have no fix in the 6.1 line. 3.5.16 manages Spring Framework 6.2.19. |
| Jackson | 2.15.2 | 2.21.8 (`jackson-bom.version`) | 2.15.2 and the 2.21.4 that Boot 3.5.16 manages both have HIGH CVEs (for example CVE-2026-91777). |
| Tomcat | BOM (10.1.55) | 10.1.62 (`tomcat.version`) | Fixes the CRITICAL CVEs in the Tomcat version managed by the BOM. |
| Log4j API | BOM (2.24.3) | 2.25.5 (`log4j2.version`) | Fixes CVE-2026-49844. |
| Groovy | - | `org.codehaus.groovy:groovy-all` 2.4.3 replaced by `org.apache.groovy:groovy` 5.0.8 | Groovy 2.4 fails on JDK 17+ because the module system blocks its reflective access. Groovy 5 is needed for the build to work on newer JDKs (24/25). |
| Spock | - | 2.5-groovy-5.0 | Works with Groovy 5 and Spring 6. |
| spring-webmvc 6.2.19 | - | not remediated | CVE-2026-47884 and CVE-2026-47890 (both CRITICAL; in XsltView and SSE fragment rendering) are only fixed in Spring Framework 7 / Spring Boot 4. That major upgrade is outside the scope of this Java 17 upgrade. The application does not use XsltView or SSE fragment rendering. |
| Lombok / Gradle wrapper | 1.18.36+ / 8.11+ | n/a | Not used by this project. |
| Jakarta servlet / validation / persistence APIs | 6.0.0 / 3.0.2 / 3.1.0 | n/a | The project has no `javax.*` imports and does not declare these APIs. Spring Boot provides Jakarta transitively, with jakarta.annotation-api resolved at 2.1.1 as mandated. |
| `-Dspotbugs.skip` / `-Dcheckstyle.skip` | - | - | Build flags only. Neither plugin is configured in this project. |

Other changes:
- Removed the plain-HTTP `repo.spring.io/milestone` repository, which Maven 3.9+ blocks. All dependencies resolve from Maven Central.
- The Spock spec (`src/test/groovy`) is now compiled (gmavenplus-plugin 5.1.0) and run (surefire includes `**/*Spec.java`). It was not running before.
