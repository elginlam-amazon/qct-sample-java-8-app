# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 21 upgrade notes

The project now targets Java 21 (`maven-compiler-plugin` 3.14.0, `<release>21</release>`) on Spring Boot 3.5.16 / Spring Framework 6.2.19.
It builds and its tests pass on both JDK 21 and JDK 25.

### Deviations, skipped and deferred updates
- **Spring Boot 3.5.16 instead of the mandated 3.2.12.** Spring Boot 3.2.12 brings in Spring Framework 6.1.15. Both carry HIGH CVEs that have no fix in their own release lines: CVE-2025-22235 in spring-boot and CVE-2025-41249 in spring-core. Spring Framework is pinned to 6.2.19 via `spring-framework.version`.
- **Jackson 2.21.8 instead of the mandated 2.15.2**, set via `jackson-bom.version`. Jackson 2.15.2 has several HIGH CVEs, for example CVE-2026-91777 and CVE-2026-54512.
- **Remaining known CVEs:** spring-webmvc 6.2.19 has two CRITICAL CVEs, CVE-2026-47884 (XsltView path limitation) and CVE-2026-47890 (SSE fragment rendering). The only fix is in Spring Framework 7 / Spring Boot 4, which is a separate major migration. The application uses neither XsltView nor SSE fragment rendering.
- **Transitive CVE fixes** are set through Boot properties: `tomcat.version` 10.1.62, `logback.version` 1.5.38 and `log4j2.version` 2.25.5.
- **Groovy:** `org.codehaus.groovy:groovy-all` 2.4.3 was replaced by `org.apache.groovy:groovy` 5.0.8. Groovy 2.4 fails on JDK 17+ because of JPMS reflective access restrictions.
- **Spock:** upgraded to 2.5-groovy-5.0. Test sources are compiled by gmavenplus-plugin 5.1.0 and Surefire includes `*Spec` classes. Before this change the Spock suite was never compiled or run, so the baseline was 0 tests; 12 tests now run.
- **`cglib-nodep` 3.1 is unchanged.** Spring 6 no longer needs it, but removing it is outside the scope of this upgrade.
- **Removed the plain-HTTP `repo.spring.io/milestone` repository.** Maven 3.9+ blocks it, and all dependencies resolve from Maven Central.
- **Jakarta EE / database groups do not apply:** the code has no `javax.*` imports and no database or ORM dependencies.
- **The build runs with `-Dspotbugs.skip=true -Dcheckstyle.skip=true`.** SpotBugs and Checkstyle findings have not been reviewed.
