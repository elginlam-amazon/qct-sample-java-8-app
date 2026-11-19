# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 17 upgrade notes

The project was upgraded from Java 8 bytecode / Spring Boot 1.2.3 to Java 17 (`maven.compiler.release=17`).

### Deviations from mandated versions / skipped updates

| Item | Mandated | Chosen | Reason |
|------|----------|--------|--------|
| Spring Boot | 3.2.12 | 3.5.16 | 3.2.12 resolves Spring Framework 6.1.15 with HIGH/CRITICAL CVEs (e.g. CVE-2025-41249, CVE-2026-41850, CVE-2026-41845) that have no fix in the 6.1 line; Boot 3.5.16 manages Spring Framework 6.2.19. |
| Jackson | 2.15.2 | 2.21.8 (`jackson-bom.version`) | 2.15.x carries HIGH CVEs (e.g. CVE-2026-91777, CVE-2026-54512); 2.21.8 is CVE-clean and compatible with Boot 3.5. |
| Tomcat (transitive) | - | 10.1.62 (`tomcat.version`) | Boot 3.5.16 default 10.1.55 has CRITICAL CVEs (e.g. CVE-2026-65905). |
| Log4j API (transitive) | - | 2.25.5 (`log4j2.version`) | Boot default 2.24.3 has CVE-2026-49844. |
| Groovy | - | org.apache.groovy:groovy 5.0.8 (replaces org.codehaus.groovy:groovy-all 2.4.3) | Groovy 2.4 fails on JDK 17+ (module reflective access); Groovy 5 supports the JDK 25 used to launch Maven. |
| Spock | - | 2.5-groovy-5.0 (from 1.0-groovy-2.4) | Required for Groovy 5 and Spring 6 context injection. |
| Lombok / Gradle wrapper / Jakarta APIs | as listed | not applicable | Not used by this project (no Lombok, Maven build, no javax.* EE imports). |

### Known remaining issue

- `org.springframework:spring-webmvc` 6.2.19 is still flagged with CVE-2026-47884 (CRITICAL, XsltView path limitation) and CVE-2026-47890 (CRITICAL, SSE fragment rendering). No 6.x fix exists. The fix is in Spring Framework 7.x, which needs Spring Boot 4 and is outside the scope of this upgrade. The application does not use `XsltView` or SSE fragment rendering.

### Test suite

The Spock specification (`src/test/groovy`) was never compiled or run before. It is now compiled by `gmavenplus-plugin` 5.1.0 (`targetBytecode` 17) and run by Surefire (`**/*Spec.java` include): 12 of 12 specs pass.
