# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 upgrade notes

The project builds and runs on Java 25 (`maven.compiler` `release` 25) with Spring Boot 3.5.16. Spring Boot 4.0 is planned as a separate, later step.

### Skipped or deferred updates

| Item | Mandated / original | Chosen | Reason |
|------|---------------------|--------|--------|
| Spring Boot | 3.2.12 (mandated) | 3.5.16 | Spring Framework 6.1 (Boot 3.2) has an ASM that cannot read Java 25 class files (major 69). Boot 3.5.x / Spring 6.2.19 can. It is also the last 3.x line before the planned Spring Boot 4.0 step. |
| Jackson | 2.15.2 (mandated) | 2.21.8 (`jackson-bom.version`) | Boot 3.5.16 manages Jackson 2.21.x. The 2.21.4 version it resolves by default has HIGH CVEs (for example CVE-2026-91777), which are fixed in 2.21.7 and later. |
| Embedded Tomcat | 10.1.55 (Boot managed) | 10.1.62 (`tomcat.version`) | Fixes CRITICAL/HIGH CVEs (for example CVE-2026-65905 and CVE-2026-55276). |
| Log4j API bridge | 2.24.3 (Boot managed) | 2.25.5 (`log4j2.version`) | Fixes CVE-2026-49844. |
| spring-webmvc 6.2.19 | - | unchanged | CVE-2026-47890 and CVE-2026-47884 are fixed only in Spring Framework 7.0.x. That upgrade is deferred to the Spring Boot 4.0 step. The app does not use SSE fragment rendering or `XsltView`. |
| cglib-nodep 3.1 | compile dependency | removed | Not used by the code. Spring ships its own repackaged CGLIB, and cglib 3.1 does not work with the Java 17+ module system. |
| groovy-all 2.4.3 | compile scope | `org.apache.groovy:groovy` 5.0.8, test scope | Groovy is used only by the Spock tests. Groovy 2.4 breaks app startup on Java 17+ through `GroovyTemplateAutoConfiguration`. |
| Lombok, Gradle wrapper, Jakarta EE APIs | - | n/a | Not used by the project. There are no `javax.*` imports to migrate. |
| Build flags | - | `-Dspotbugs.skip -Dcheckstyle.skip` | These come from the provided build command. Neither plugin is configured in the project. |
