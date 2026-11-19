# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 25 upgrade notes

The project now builds and runs on Java 25 (`maven-compiler-plugin` 3.14.1, `<release>25</release>`).

### Deviations from mandated versions / skipped or deferred updates

| Item | Mandated / original | Chosen | Reason |
|------|---------------------|--------|--------|
| Spring Boot | 3.2.12 | 3.5.16 | Spring Boot 3.2.x (Spring 6.1 / ASM) cannot parse Java 25 class files (major 69); 3.5.x is the latest 3.x line and is a stepping stone to the planned Spring Boot 4.0 upgrade. |
| Jackson | 2.15.2 | 2.21.8 (`jackson-bom.version`) | 2.15.x / BOM default 2.21.4 carry HIGH CVEs; 2.21.8 is CVE-clean and compatible with Boot 3.5. |
| Embedded Tomcat | BOM 10.1.55 | 10.1.62 (`tomcat.version`) | BOM default carries CRITICAL CVEs (e.g. CVE-2026-65905, CVE-2026-55276). |
| Log4j API (bridge) | BOM 2.24.3 | 2.25.5 (`log4j2.version`) | CVE-2026-49844. |
| spring-webmvc | 6.2.19 | 6.2.19 (deferred) | CVE-2026-47890 / CVE-2026-47884 (CRITICAL) are fixed only in Spring Framework 7.x, which requires Spring Boot 4.0; deferred to the planned Spring Boot 4.0 step. The app does not use SSE fragment rendering or `XsltView`. |
| Groovy | groovy-all 2.4.3 | org.apache.groovy:groovy 5.0.8 | Groovy 2.4 fails on JDK 17+ (module access); Groovy 5 supports Java 25 bytecode. |
| Spock | 1.0-groovy-2.4 | 2.5-groovy-5.0 | Required for Groovy 5 and Spring 6 context injection. |
| Jakarta EE | n/a | via Spring Boot 3.5 (servlet 6.0, annotation 2.1.1) | The source had no `javax.*` imports; validation/persistence APIs are not used, so they were not added. |
| Build flags | - | `-Dspotbugs.skip -Dcheckstyle.skip` | Neither plugin is configured in this project; flags have no effect. |

The Spock suite (`DownloadControllerSpec`) previously never ran (no Groovy compiler was configured). It is now compiled by gmavenplus-plugin 5.1.0 and run by Surefire.
