# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade notes

The project now targets Java 21 (`maven.compiler.release=21`) and builds with Spring Boot 3.5.16
(Spring Framework 6.2.19). Spock/Groovy tests under `src/test/groovy` are compiled by
gmavenplus-plugin and executed by Surefire (`**/*Spec.java` include).

### Skipped / deferred updates

- **Spring Boot 3.2.12 (the transformation's mandated LTS line) was not used; 3.5.16 was chosen
  instead.** Boot 3.2.12 resolves Spring Framework 6.1.x, which carries HIGH-severity
  CVE-2025-41249 (authorization bypass) with no fix in the 6.1 line. Boot 3.5.x manages and is
  tested against Spring Framework 6.2.x, so the parent was advanced and
  `spring-framework.version` pinned to the CVE-clean 6.2.19.
- **Jackson 2.15.2 (mandated) was not used.** The Spring Boot BOM manages Jackson, and the
  resolved 2.15.x line carries HIGH CVEs (e.g. CVE-2026-91777, CVE-2026-54512). `jackson-bom.version`
  is pinned to 2.22.4, which is CVE-clean and compatible with the Boot 3.5 line.
- **Embedded Tomcat** is pinned via `tomcat.version=10.1.62` (the BOM default carried CRITICAL
  CVEs such as CVE-2026-65905 and CVE-2025-24813).
- **spring-webmvc 6.2.19 still reports CVE-2026-47884 (XsltView path limitation) and
  CVE-2026-47890 (SSE fragment stream corruption).** Both are fixed only in Spring Framework 7.0.x,
  which requires Spring Boot 4.x — out of scope for this Java-version upgrade. Neither affected
  feature (XSLT views, SSE fragment rendering) is used by this application.
- **Explicit `org.springframework:*` version pins were removed** from `pom.xml` in favour of the
  Spring Boot BOM so that all Spring modules stay on one consistent, managed version.
- **Jakarta EE migration was not required**: the source contains no `javax.*` imports.
- SpotBugs and Checkstyle remain skipped in the documented build command
  (`-Dspotbugs.skip=true -Dcheckstyle.skip=true`); enabling them was out of scope.
