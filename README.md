# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 upgrade notes

The project builds with `<release>25</release>` on Spring Boot 4.0.9 (Spring Framework 7.0.10, Tomcat 11.0.27,
Jackson 3.1.7). The Spock suite under `src/test/groovy` compiles and runs with gmavenplus-plugin 5.1.0,
Groovy 5.0.8 and Spock 2.5-groovy-5.0.

### Spring Boot 4.0 migration
- Parent `spring-boot-starter-parent` 3.5.16 -> 4.0.9 (latest 4.0.x patch; the target is the 4.0 line).
- `spring-boot-starter-web` -> `spring-boot-starter-webmvc` (Boot 4 starter rename).
- Added `spring-boot-starter-webmvc-test` (test scope) for the MockMvc-based specs (Boot 4 modular test starters).
- Removed the Boot 3-era `tomcat.version`, `jackson-bom.version` (Jackson 2.x) and `log4j2.version` overrides.
  The Boot 4 BOM now manages newer or equal versions (Tomcat 11.0.27, Jackson 3.1.7, Log4j 2.25.5).
- All Spring artifacts resolve to a single, Boot-managed Spring Framework version (7.0.10).
  This fixes CVE-2026-47890 and CVE-2026-47884 in `spring-webmvc` 6.2.x.
- The application code needed no changes: it has no Jackson, Security or actuator-package imports.

### Other changes
- Removed the plain-HTTP `repo.spring.io/milestone` repository. Maven 3.9+ blocks it, and every dependency resolves from Maven Central.
- Removed `cglib-nodep`. Nothing used it directly. Spock 1.0 used it for class mocking, and Spock 2.x uses Byte Buddy instead.
- Replaced `org.codehaus.groovy:groovy-all` 2.4.3 with `org.apache.groovy:groovy`. The old Groovy breaks under Java 17+ module access rules.
- Moved `logback.xml` to a named appender, because logback 1.5 rejects unnamed appenders.
- Surefire loads Mockito as a `-javaagent`, because Java 25 blocks dynamic agent self-attach.
- Test fixture UUIDs are now fixed values.
- `Sha512ShallowEtagHeaderFilter` is adapted to the Spring 6+ `generateETagHeaderValue(InputStream, boolean)` signature and is covered by `Sha512ShallowEtagHeaderFilterSpec`.
- The actuator health endpoint moved from `/health` (Boot 1.x) to `/actuator/health`. Update any external monitoring.
  Only `health` is exposed over HTTP (Boot default).
