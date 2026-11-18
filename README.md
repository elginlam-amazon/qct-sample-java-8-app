# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 / Spring Boot 4.0 upgrade notes

- **Spring Boot 4.0.9** (latest 4.0.x patch; the 4.1 line was intentionally not adopted). Brings Spring Framework 7.0.10, Tomcat 11.0.27, Jackson 3.1.7.
- `spring-boot-starter-web` replaced by its Boot 4 name `spring-boot-starter-webmvc`.
- Boot 3-era BOM version pins (`tomcat.version`, `jackson-bom.version`, `log4j2.version`, `groovy.version`) removed; the Boot 4 BOM now manages these at equal or newer versions.
- **spring-webmvc CVEs (CVE-2026-47890, CVE-2026-47884)** that were deferred on Spring Framework 6.2.x are resolved by Spring Framework 7.0.10.
- **SpotBugs / Checkstyle**: still skipped in the build command (`-Dspotbugs.skip=true -Dcheckstyle.skip=true`); re-enable and address findings separately.
