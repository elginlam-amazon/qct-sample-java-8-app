# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 21 upgrade notes

The project was upgraded from Java 8 (Spring Boot 1.2.3) to Java 21 with Spring Boot 3.2.12.

Main changes:
- `maven-compiler-plugin` 3.13.0 with `<release>21</release>`. The plain-HTTP `repo.spring.io/milestone` repository was removed because Maven 3.9+ blocks it. All dependencies now resolve from Maven Central.
- Spring Boot parent 3.2.12. The Spring Boot BOM now manages the Spring versions; the old explicit 4.1.x pins were removed.
- These BOM-managed versions are overridden to CVE-patched releases: Spring Framework 6.1.21, Jackson 2.18.12, Tomcat 10.1.62 and Logback 1.5.38.
- Groovy `org.codehaus.groovy:groovy-all` 2.4.3 was replaced with `org.apache.groovy:groovy` 5.0.8. Spock was moved to 2.5-groovy-5.0, and the gmavenplus-plugin 5.1.0 was added so the Spock specs are compiled and run. Before this, they never ran.
- Library updates: Guava 33.8.0-jre, commons-io 2.22.0, commons-codec 1.23.0, cglib-nodep 3.3.0.
- Code changes:
  - `Sha512ShallowEtagHeaderFilter` matches the new Spring 6 `generateETagHeaderValue(InputStream, boolean)` signature.
  - The spec uses `@SpringBootTest`.
  - Test fixtures use fixed UUIDs.
  - `logback.xml` declares a named appender, which Logback 1.5 requires.
- No `javax.*` imports were present, so no Jakarta source changes were needed.

### Skipped / deferred updates

| Item | Reason |
|------|--------|
| Jackson stays at 2.18.12 instead of the mandated 2.15.2 | 2.15.x has several HIGH CVEs (for example CVE-2026-91777 and CVE-2026-54512) that are fixed only in 2.18.11+. 2.18.12 works with Spring Boot 3.2. |
| Spring Framework 6.1.21: CVE-2026-47884 (CRITICAL), CVE-2025-41249, CVE-2026-41845 and CVE-2026-41842 (HIGH), plus others | No fix exists in the 6.1.x line. Fixing them needs Spring Boot 3.5.x (Spring 6.2.x) or later, which is beyond the mandated Spring Boot 3.2.12 target. Security review needed. |
| Spring Boot 3.2.12: CVE-2025-22235 (HIGH) | No fix in 3.2.x. Needs Spring Boot 3.5.x or later. The app does not use Spring Security `EndpointRequest`. |
| Micrometer 1.12.13: CVE-2026-40984 (HIGH) | No fix in 1.12.x. Needs Micrometer 1.15+, which comes with Spring Boot 3.5.x or later. |
| SpotBugs / Checkstyle | Skipped through the build flags `-Dspotbugs.skip=true -Dcheckstyle.skip=true`, as the build command requires. |
| `cglib-nodep` | Kept and updated to 3.3.0 to avoid removing a declared dependency. Spring 6 no longer needs it. |
