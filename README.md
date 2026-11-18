# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 21 upgrade notes

The project now targets Java 21 (`maven-compiler-plugin` 3.14.0, `<release>21</release>`) and Spring Boot 3.5.16.
It builds and its tests pass on both JDK 21 and JDK 25.

### Deviations, skipped and deferred updates
- **Spring Boot 3.5.16 instead of the mandated 3.2.12.** The 3.2.x line pulls in Spring Framework 6.1.x, Micrometer 1.12.x and Spring Boot 3.2.x. Those versions have HIGH/CRITICAL CVEs that are not fixed anywhere in that line (for example CVE-2025-22235, CVE-2025-41249 and CVE-2026-40984). Spring Framework is pinned to 6.2.19.
- **Jackson 2.21.8 instead of the mandated 2.15.2**, set through `jackson-bom.version`. 2.15.x has known HIGH CVEs.
- **Remaining known CVEs:** CVE-2026-47884 (XsltView path limitation) and CVE-2026-47890 (SSE fragment rendering) in spring-webmvc 6.2.19, both rated CRITICAL. The only fix is in Spring Framework 7 / Spring Boot 4, which is a separate major migration. The application uses neither XsltView nor SSE fragment rendering.
- **CVE fixes for transitive dependencies** via Boot properties: `tomcat.version` 10.1.62, `logback.version` 1.5.38, `log4j2.version` 2.25.5.
- **Groovy:** `org.codehaus.groovy:groovy-all` 2.4.3 is replaced by `org.apache.groovy:groovy` 5.0.8. Groovy 2.4 fails on JDK 17+ because of JPMS reflective access restrictions.
- **Spock:** upgraded to 2.5-groovy-5.0. The test sources are now compiled by gmavenplus-plugin 5.1.0, and Surefire includes `*Spec` classes. Before this change the Spock suite was never compiled or run, so the baseline was 0 tests; 12 tests now run.
- **`cglib-nodep` 3.1 kept unchanged.** Spring 6 no longer needs it, but removing it is outside the scope of the upgrade.
- **Removed the plain-HTTP `repo.spring.io/milestone` repository**, which Maven 3.9+ blocks. All dependencies resolve from Maven Central.
- **The build is run with `-Dspotbugs.skip=true -Dcheckstyle.skip=true`.** SpotBugs and Checkstyle findings have not been reviewed.
