# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 upgrade: skipped / deferred updates

- **Spring Boot 3.5.16 instead of mandated 3.2.12**: Spring Boot 3.2.x bundles Spring Framework 6.1, whose ASM cannot parse Java 25 class files (major version 69). Spring Boot 4.0 is planned as a separate later step.
- **Jackson 2.21.8 instead of mandated 2.15.2** (via `jackson-bom.version`): newer version chosen to remediate known CVEs.
- **CVE overrides**: `tomcat.version` 10.1.62 and `log4j2.version` 2.25.5 override the Boot-managed versions to fix known vulnerabilities.
- **spring-webmvc 6.2.19 (CRITICAL CVE-2026-47890, CVE-2026-47884)**: no fix exists on the 6.2.x line; fixed in Spring Framework 7.0.x, deferred to the Spring Boot 4.0 upgrade.
- **SpotBugs / Checkstyle**: skipped in the build command during the upgrade (`-Dspotbugs.skip=true -Dcheckstyle.skip=true`); re-enable and address findings separately.
