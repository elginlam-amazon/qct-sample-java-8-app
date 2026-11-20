# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 upgrade notes

Build: `mvn -B clean install -Dspotbugs.skip=true -Dcheckstyle.skip=true` on JDK 25 (maven-compiler-plugin 3.13.0, `release` 25).

### Deviations from mandated versions / skipped or deferred updates

| Item | Mandated | Chosen | Reason |
|------|----------|--------|--------|
| Spring Boot | 3.2.12 | 3.5.16 (Spring Framework 6.2.19) | The Spring 6.1 / ASM shipped with Boot 3.2.x cannot read Java 25 class files (major version 69). Boot 3.5.x is also the last 3.x line before the planned Spring Boot 4.0 upgrade. |
| Jackson | 2.15.2 | 2.21.8 (`jackson-bom.version`) | The Boot 3.5.16 BOM manages Jackson. 2.15.2 and the Boot default 2.21.4 both have known HIGH CVEs. |
| Embedded Tomcat | n/a (BOM-managed) | 10.1.62 (`tomcat.version`) | The Boot default 10.1.55 has CRITICAL CVEs (for example CVE-2026-65905 and CVE-2026-55276). |
| Log4j API bridge | n/a (BOM-managed) | 2.25.5 (`log4j2.version`) | The Boot default 2.24.3 has CVE-2026-49844. |
| spring-webmvc 6.2.19 | n/a | Deferred | It has CVE-2026-47890 and CVE-2026-47884, both CRITICAL. They are fixed only in Spring Framework 7.0.x, which requires Spring Boot 4.0. That upgrade is planned as a separate step. 6.2.19 is the latest 6.2 release. |
| Groovy / Spock | n/a | Groovy 5.0.8 (`org.apache.groovy:groovy`), Spock 2.5-groovy-5.0, gmavenplus-plugin 5.1.0 | Groovy 2.4.3 cannot run on JDK 17+. The Spock spec was never compiled or run before (0 tests in the baseline). It now runs 12 specs. |
| Lombok, Gradle wrapper, Jakarta APIs, database/ORM | as listed | Not applicable | Not used by this project. There are no `javax.*` imports. |
| Explicit `spring-*` dependency versions | n/a | Removed, now BOM-managed | This keeps them consistent with the Spring Boot parent. A duplicate `spring-test` declaration was removed. |
| spotbugs / checkstyle | n/a | Skipped via build flags | The requested build command skips them. Neither plugin is configured in this project. |
