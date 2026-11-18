# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 upgrade notes

The project now builds with `<release>25</release>` on Spring Boot 3.5.16 (Spring Framework 6.2.19).
The Spock suite under `src/test/groovy` now compiles and runs. Before the upgrade it was never compiled
and 0 tests ran. It is compiled with gmavenplus-plugin 5.1.0, Groovy 5.0.8 and Spock 2.5-groovy-5.0.

### Deviations from the mandated versions
- **Spring Boot 3.5.16 instead of 3.2.12.** Spring Boot 3.2.x (Spring Framework 6.1) cannot parse Java 25
  class files during classpath scanning, so the app fails at startup. 3.5.x is the newest 3.x line and runs on Java 25.
  Spring Boot 4.0 is planned as a separate later step. The Spring artifacts no longer pin explicit versions
  and resolve from the Boot BOM, so that step only needs to change the parent version.
- **Jackson 2.21.8 instead of 2.15.2** (via `jackson-bom.version`). The Boot 3.5 BOM manages Jackson 2.21.x.
  2.15.2 and the BOM default 2.21.4 have known HIGH CVEs.
- **Other property overrides:** `log4j2.version=2.25.5` (the BOM default 2.24.3 has CVE-2026-49844) and
  `tomcat.version=10.1.62`.

### Known issues left open
- `spring-webmvc` 6.2.19 has CVE-2026-47890 (SSE fragment rendering) and CVE-2026-47884 (XsltView path limitation),
  both CRITICAL. The only fix is Spring Framework 7.0.9+, which needs Spring Boot 4.0 (the later step).
  This application uses neither SSE fragments nor `XsltView`.

### Other changes
- Removed the plain-HTTP `repo.spring.io/milestone` repository. Maven 3.9+ blocks it, and every dependency resolves from Maven Central.
- Removed `cglib-nodep`. Nothing used it directly. Spock 1.0 used it for class mocking, and Spock 2.x uses Byte Buddy instead.
- Replaced `org.codehaus.groovy:groovy-all` 2.4.3 with `org.apache.groovy:groovy`. The old Groovy breaks under Java 17+ module access rules.
- Moved `logback.xml` to a named appender, because logback 1.5 rejects unnamed appenders.
- Surefire loads Mockito as a `-javaagent`, because Java 25 blocks dynamic agent self-attach.
- Test fixture UUIDs are now fixed values.
- `Sha512ShallowEtagHeaderFilter` is adapted to the Spring 6 `generateETagHeaderValue(InputStream, boolean)` signature and is covered by `Sha512ShallowEtagHeaderFilterSpec`.
- The actuator health endpoint moved from `/health` (Boot 1.x) to `/actuator/health`. Update any external monitoring.
