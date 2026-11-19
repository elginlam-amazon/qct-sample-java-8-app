# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 25 upgrade notes

The project now builds and runs on Java 25 (`maven-compiler-plugin` 3.14.1, `<release>25</release>`).

### Deviations from mandated versions / skipped or deferred updates

| Item | Mandated / original | Chosen | Reason |
|------|---------------------|--------|--------|
| Spring Boot | 3.2.12 | 3.5.16 | Spring Boot 3.2.x (Spring 6.1 / ASM) cannot parse Java 25 class files (major 69); 3.5.x is the latest 3.x line and is a stepping stone to the planned Spring Boot 4.0 upgrade. |
| Jackson / Tomcat / Log4j API | 3.5-era CVE pins (`jackson-bom.version` 2.21.8, `tomcat.version` 10.1.62, `log4j2.version` 2.25.5) | Spring Boot 4.0.9 BOM (Jackson 3.1.7 + annotations 2.21, Tomcat 11.0.27, Log4j API 2.25.5) | Pins removed in the Spring Boot 4.0 step; BOM-managed versions are newer and CVE-clean. |
| spring-webmvc | 6.2.19 | 7.0.10 (via Spring Boot 4.0.9) | CVE-2026-47890 / CVE-2026-47884 are fixed in Spring Framework 7.x. |
| Groovy | groovy-all 2.4.3 | org.apache.groovy:groovy 5.0.8 | Groovy 2.4 fails on JDK 17+ (module access); Groovy 5 supports Java 25 bytecode. |
| Spock | 1.0-groovy-2.4 | 2.5-groovy-5.0 | Required for Groovy 5 and Spring 6 context injection. |
| Jakarta EE | n/a | via Spring Boot 3.5 (servlet 6.0, annotation 2.1.1) | The source had no `javax.*` imports; validation/persistence APIs are not used, so they were not added. |
| Build flags | - | `-Dspotbugs.skip -Dcheckstyle.skip` | Neither plugin is configured in this project; flags have no effect. |

The Spock suite (`DownloadControllerSpec`) previously never ran (no Groovy compiler was configured). It is now compiled by gmavenplus-plugin 5.1.0 and run by Surefire.

## Spring Boot 4.0 upgrade notes

- Spring Boot 3.5.16 -> 4.0.9 (latest 4.0.x patch; Spring Framework 7.0.10, Jakarta EE 11 / Servlet 6.1, Tomcat 11).
- `spring-boot-starter-web` renamed to `spring-boot-starter-webmvc`.
- No source changes were required (no Jackson 2 databind usage, no Spring Security, no `@MockBean`).
