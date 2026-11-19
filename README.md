# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!

## Java 25 upgrade notes

The project builds and runs on Java 25 (`maven.compiler` `release` 25) with Spring Boot 4.0.10 (Spring Framework 7.0.11). It was first moved to Spring Boot 3.5.16 and then to the Spring Boot 4.0 line.

### Spring Boot 4.0 upgrade

- Parent `spring-boot-starter-parent` 3.5.16 -> 4.0.10 (latest 4.0.x patch; the target is pinned to the 4.0 line, so 4.1-only changes were not applied).
- `spring-boot-starter-web` -> `spring-boot-starter-webmvc` (Boot 4 starter rename).
- Removed the `jackson-bom.version`, `tomcat.version` and `log4j2.version` overrides. The Boot 4.0.10 BOM now manages Jackson 3.1.8 (`tools.jackson`), Tomcat 11.0.27 and Log4j API 2.25.5. None of them has a known CVE.
- No source changes were needed. The code has no Jackson, `javax.*`, Spring Security or relocated Boot package imports.

### Skipped or deferred updates

| Item | Mandated / original | Chosen | Reason |
|------|---------------------|--------|--------|
| Spring Boot | 3.2.12 (mandated) | 3.5.16 | Spring Framework 6.1 (Boot 3.2) has an ASM that cannot read Java 25 class files (major 69). Boot 3.5.x / Spring 6.2.19 can. It is also the last 3.x line before the planned Spring Boot 4.0 step. |
| Jackson | 2.15.2 (mandated) | 3.1.8 (Boot 4.0.10 BOM) | Was pinned to 2.21.8 on Boot 3.5.16 for CVE fixes. The pin was removed in the Boot 4.0 step. |
| Embedded Tomcat | 10.1.55 (Boot managed) | 11.0.27 (Boot 4.0.10 BOM) | Was pinned to 10.1.62 on Boot 3.5.16 for CVE fixes. The pin was removed in the Boot 4.0 step. |
| Log4j API bridge | 2.24.3 (Boot managed) | 2.25.5 (Boot 4.0.10 BOM) | Fixes CVE-2026-49844. Boot 4.0.10 manages this version, so the override was removed. |
| spring-webmvc | 6.2.19 | 7.0.11 (Boot 4.0.10) | CVE-2026-47890 and CVE-2026-47884 are fixed in Spring Framework 7.0.x. |
| cglib-nodep 3.1 | compile dependency | removed | Not used by the code. Spring ships its own repackaged CGLIB, and cglib 3.1 does not work with the Java 17+ module system. |
| groovy-all 2.4.3 | compile scope | `org.apache.groovy:groovy` 5.0.8, test scope | Groovy is used only by the Spock tests. Groovy 2.4 breaks app startup on Java 17+ through `GroovyTemplateAutoConfiguration`. |
| Lombok, Gradle wrapper, Jakarta EE APIs | - | n/a | Not used by the project. There are no `javax.*` imports to migrate. |
| Build flags | - | `-Dspotbugs.skip -Dcheckstyle.skip` | These come from the provided build command. Neither plugin is configured in the project. |
