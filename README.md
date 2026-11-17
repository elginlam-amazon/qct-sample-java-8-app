# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade notes

The project was upgraded from Java 8 / Spring Boot 1.2.3 to Java 21 (`<release>21</release>`) / Spring Boot 3.5.16.

### Deviations from mandated versions and skipped/deferred updates

| Item | Mandated | Used | Reason |
|------|----------|------|--------|
| Spring Boot | 3.2.12 | 3.5.16 | 3.2.12 resolves Spring Framework 6.1.15 / Tomcat 10.1.33 with CRITICAL/HIGH CVEs (e.g. CVE-2025-41249, CVE-2025-24813, CVE-2025-22235) that have no fix in the 6.1 / Boot 3.2 line. Boot 3.5.16 (Spring Framework 6.2.19) was verified with the full test suite. |
| Jackson | 2.15.2 | 2.21.8 (`jackson-bom.version`) | Jackson is not declared directly; it comes transitively from `spring-boot-starter-web`. 2.15.2 is older than the version managed by Boot 3.5.x and the managed 2.21.4 had HIGH CVEs (e.g. CVE-2026-91777), so 2.21.8 is used. |
| Embedded Tomcat | managed (10.1.55) | 10.1.62 (`tomcat.version`) | The managed version had CRITICAL CVEs (e.g. CVE-2026-65905). |
| Log4j API | managed (2.24.3) | 2.25.5 (`log4j2.version`) | CVE-2026-49844. |
| Lombok, Jakarta APIs (validation, persistence, servlet as direct deps), databases, Swagger, Spring Security | - | not applicable | Not used by the project. No `javax.*` imports were present; the Jakarta servlet API comes from the Spring Boot starters. |

### Known residual vulnerabilities

- `org.springframework:spring-webmvc` 6.2.19 (latest 6.2.x): CVE-2026-47884 (XsltView path limitation) and CVE-2026-47890 (SSE fragment rendering). These are fixed only in Spring Framework 7.x / Spring Boot 4.x, which is a separate major upgrade. The application uses neither `XsltView` nor SSE fragment rendering.

### Other build changes

- The Spock specs are now actually compiled and run (gmavenplus-plugin 5.1.0, Groovy 5.0.8, Spock 2.5-groovy-5.0, and Surefire includes `**/*Spec.java`). Before this, 0 tests ran; now 12/12 pass. The Groovy 5 toolchain is needed so the build also works when Maven is launched with a newer JDK (for example 25).
- The unused `http://repo.spring.io/milestone` repository was removed. It used insecure plain HTTP, which Maven 3.8+ blocks, and every dependency resolves from Maven Central.
- `logback.xml`: the unnamed appender nested inside `<root>` is now a named `CONSOLE` appender, because logback 1.5 rejects the old form.
