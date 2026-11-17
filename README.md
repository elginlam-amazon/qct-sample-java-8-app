# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 21 upgrade notes

Upgraded from Java 1.8 / Spring Boot 1.2.3 to Java 21 (`maven.compiler.release=21`) / Spring Boot 3.5.16.

### Skipped or deviating updates
- **Spring Boot 3.5.16 instead of the mandated 3.2.12**: 3.2.12 (Spring Framework 6.1.15) carries HIGH/CRITICAL CVEs (e.g. CVE-2025-22235, CVE-2025-41249) that have no fix in the 3.2 / 6.1 line. 3.5.x manages Spring Framework 6.2.x.
- **Jackson 2.21.8 instead of 2.15.2**: the Boot 3.5 BOM manages Jackson 2.21.x. It is pinned to 2.21.8 through `jackson-bom.version` because the default version is CVE-flagged.
- **Tomcat 10.1.62 / Log4j API 2.25.5**: these override the Boot BOM defaults (10.1.55 / 2.24.3), which have known CVEs.
- **spring-webmvc 6.2.19 still flags CVE-2026-47890 and CVE-2026-47884 (CRITICAL)**: these are fixed only in Spring Framework 7 / Spring Boot 4, a major upgrade outside this migration. The app does not use the affected features (XsltView and SSE fragment rendering).
- Jakarta EE, database/ORM, Lombok and Gradle steps did not apply: the project has no `javax.*` imports, no persistence layer, no Lombok and no Gradle build.
- The Spock test suite (`src/test/groovy`) had never been compiled. It now runs through gmavenplus-plugin 5.1.0 with Groovy 5.0.8 and Spock 2.5-groovy-5.0. Groovy 5 is needed because the build may run on a JDK newer than 21.
