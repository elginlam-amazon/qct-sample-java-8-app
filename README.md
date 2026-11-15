# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 21 upgrade — skipped / deferred updates

- **Spring Boot 3.5.16 instead of the mandated 3.2.12**: Boot 3.2.x resolves Spring Framework 6.1.x,
  which carries HIGH severity CVE-2025-41249 with no fix on the 6.1 line. Boot 3.5.16 + Spring
  Framework 6.2.19 is CVE-clean.
- **Jackson pinned to 2.22.4 instead of the mandated 2.15.2**: 2.15.x carries multiple HIGH
  jackson-databind CVEs; those are fixed only from 2.21.7 onwards.
- **Insecure `http://repo.spring.io/milestone` repository removed**: all artifacts resolve from
  Maven Central over HTTPS.
- **Groups 3 (database/ORM) and the Swagger/Spring Security parts of Group 5: not applicable** —
  this project has no database, ORM, Swagger or Spring Security usage.
- **Jakarta EE migration**: no `javax.*` imports existed in the sources; Jakarta APIs arrive
  transitively through the Spring Boot 3.5 starters.
- **Test toolchain**: the Spock specs were previously never compiled or executed. They now run via
  gmavenplus + Groovy 5 / Spock 2.5 (`Tests run: 12`).
