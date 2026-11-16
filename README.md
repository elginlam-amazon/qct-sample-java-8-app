# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 21 upgrade notes

The project was upgraded from Java 8 / Spring Boot 1.2.3 to **Java 21 / Spring Boot 3.5.16**.

### Deviations from the mandated dependency versions (with reasons)

| Mandated | Applied | Reason |
|----------|---------|--------|
| Spring Boot 3.2.12 (LTS) | 3.5.16 | Boot 3.2.12 resolves Spring Framework 6.1.15, which carries HIGH CVE-2025-41249 (authorization bypass) with no fix in the 6.1 line. The fix is only in Spring Framework 6.2.x, which is managed and tested by the Boot 3.5.x line. `spring-framework.version` is pinned to 6.2.19 (CVE-clean). |
| Jackson 2.15.2 (LTS) | 2.21.8 (`jackson-bom.version`) | The Boot BOM manages Jackson; the versions it resolves for the 2.15.x line carry 5 HIGH jackson-databind CVEs (e.g. CVE-2026-91777, CVE-2026-54512) fixed only from 2.21.7 onwards. |

Additional CVE-driven overrides of BOM-managed, transitive dependencies:
`tomcat.version` = 10.1.62 (CRITICAL Tomcat CVEs in the BOM default), `logback.version` = 1.5.38.

### Items intentionally skipped / not applicable

- **Jakarta EE migration**: no `javax.*` usage existed in the sources, so only dependency-level changes were needed.
- **Database / ORM / caching / Swagger / Lombok / Gradle wrapper steps**: the project contains none of these (no JPA, no datasource, no Lombok, Maven-only build), so those groups were not applicable.
- **Build flags** `-Dspotbugs.skip=true -Dcheckstyle.skip=true` come from the supplied build command; neither plugin is configured in this project.
- Explicit per-artifact Spring version pins were removed in favour of Spring Boot BOM management, so Spring modules stay mutually consistent.
- Groovy/Spock toolchain moved to Groovy 5.0.8 + Spock 2.5-groovy-5.0 + gmavenplus-plugin 5.1.0 because older Groovy lines cannot parse class files from the JDK that launches Maven in this environment (Java 25).
