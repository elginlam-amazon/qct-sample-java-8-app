# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade notes

- Java: compiled with `release 21` (maven-compiler-plugin 3.13.0). The build and tests also pass when Maven runs on JDK 25.
- Spring Boot 1.2.3 -> 3.2.12 (the mandated LTS line). Spring Framework is set to 6.1.21, the latest 6.1.x patch.
- Jackson is set to 2.15.2 (the mandated LTS) via `jackson-bom.version`.
- Tests: Spock 1.0/Groovy 2.4 -> Spock 2.5-groovy-5.0/Groovy 5.0.8, compiled by gmavenplus-plugin 5.1.0. Surefire now includes `*Spec` classes. The 12 Spock specs were never compiled or run before this upgrade, and now they all run and pass.
- `@ContextConfiguration` was replaced by `@SpringBootTest` so that Web MVC auto-configuration loads in tests.
- `Sha512ShallowEtagHeaderFilter` was adapted to the Spring 6 `generateETagHeaderValue(InputStream, boolean)` signature.
- `logback.xml` now uses a named appender, which logback 1.5 requires.
- The plain-HTTP `repo.spring.io/milestone` repository was removed. Maven 3.9 blocks it, and every dependency resolves from Maven Central.
- CVE-free overrides for libraries managed by the Spring Boot BOM: tomcat 10.1.62, logback 1.5.38, log4j2 2.25.5, json-smart 2.5.2, xmlunit 2.10.4, assertj 3.27.7.

### Skipped / deferred updates (with reasons)

- **Spring Boot kept at 3.2.12 / Spring Framework 6.1.21 (known CVEs).** The transformation mandates Spring Boot 3.2.12. Spring 6.1.21 has advisories with no fix in the 6.1 line, for example CVE-2026-47884 (CRITICAL, XsltView), CVE-2025-41249, CVE-2026-41845, CVE-2026-41842 and CVE-2026-41850 (HIGH). Spring Boot 3.2.12 itself has CVE-2025-22235 (HIGH). Spring 6.2.19 still has CRITICAL webmvc advisories, so a clean webmvc needs Spring Framework 7.0.9+ (Spring Boot 4.x). That would be a further major-version migration outside the mandated target, so it is deferred. This application does not use XsltView, SpEL user input, script templates or versioned static resources.
- **Jackson kept at 2.15.2 (known CVEs).** This is the mandated version. jackson-databind/jackson-core 2.15.2 have HIGH advisories that are fixed in 2.18.11. To remediate, set `jackson-bom.version` to 2.18.11 or later.
- **Micrometer 1.12.13 (CVE-2026-40984, HIGH, no 1.12.x fix).** Spring Boot 3.2 manages this version. The fix needs Micrometer 1.15+, which is outside what Spring Boot 3.2 supports. It is deferred together with the Spring Boot line upgrade.
