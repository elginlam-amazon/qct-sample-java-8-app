# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade notes

The project now targets Java 21 (`maven.compiler.release=21`) on Spring Boot 3.5.16 / Spring Framework 6.2.19,
and builds and passes its Spock suite on both JDK 21 and JDK 25.

### Deviations, skipped and deferred updates

| Item | Decision | Reason |
|------|----------|--------|
| Spring Boot (mandated 3.2.12) | Upgraded to 3.5.16 instead | 3.2.12 has CVE-2025-22235 (HIGH), and its Spring Framework 6.1.x line has HIGH CVEs with no fix in 6.1. 3.5.x is the newest 3.x line and manages Spring Framework 6.2.x. |
| Jackson (mandated 2.15.2) | Overridden to 2.21.8 with `jackson-bom.version` | 2.15.2, and the BOM default 2.21.4, both have HIGH CVEs in jackson-databind (for example CVE-2026-91777). |
| spring-webmvc 6.2.19 | Deferred | CVE-2026-47884 and CVE-2026-47890 (CRITICAL) are only fixed in Spring Framework 7 (Spring Boot 4), which is a separate major upgrade. The affected features (XsltView, SSE fragment rendering) are not used by this application. |
| Embedded Tomcat, Log4j bridge | Overridden to 10.1.62 and 2.25.5 | The versions the Spring Boot 3.5.16 BOM pins (Tomcat 10.1.55, log4j-api 2.24.3) have known CVEs, including CRITICAL ones in Tomcat. |
| Logback | Overridden to 1.5.38 | This is the latest 1.5.x patch release. The BOM default 1.5.34 has no known CVEs. |
| Groovy / Spock | `org.codehaus.groovy:groovy-all` 2.4.3 replaced by `org.apache.groovy:groovy` 5.0.8, Spock 2.5-groovy-5.0, gmavenplus-plugin 5.1.0 | Groovy 2.4 fails on JDK 17+ because of module access rules. Groovy 4.x cannot read JDK 25 class files. The Spock specs were not compiled or run before this upgrade; they are now wired in and run. |
| cglib-nodep 3.1 | Kept unchanged | Spring 6 does not need it. Removing it is left as a follow-up cleanup. |
| SpotBugs / Checkstyle | Skipped in the build command | They are skipped by the requested build flags (`-Dspotbugs.skip`, `-Dcheckstyle.skip`). |
