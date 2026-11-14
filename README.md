# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade: skipped / deferred updates

- **Spring Boot kept at 3.2.12** (mandated LTS target). This pins `spring-core 6.1.15`, which carries
  CVE-2025-41249. Remediation requires Spring Boot 3.5.x, which is outside the upgrade scope and shifts
  supported baselines — deferred as a repository-owner decision.
- **Explicit Spring 4.1.x artifact version pins removed** rather than individually bumped; version
  management is delegated to the Spring Boot BOM.
- **SpotBugs / Checkstyle** remain skipped only via the build command flags (`-Dspotbugs.skip=true
  -Dcheckstyle.skip=true`); no plugin configuration was removed. Re-enabling them and fixing any new
  violations is left to the owner.
- **Not applicable to this project:** Gradle wrapper upgrade (Maven-only build), Lombok upgrade (no
  Lombok dependency), `javax.*` → `jakarta.*` source migration (no `javax.*` imports; Jakarta APIs come
  transitively from Spring Boot 3.2.12).
