# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 25 upgrade notes

### Skipped / deferred updates

- **Spring Boot pinned to 3.5.16 instead of 3.2.12.** The 3.2.x line does not support
  Java 25 bytecode (ASM/CGLIB level) and resolves a Spring Framework 6.1.x version with
  known CVEs. 3.5.16 resolves Spring Framework 6.2.19, builds and runs on JDK 25, and is
  forward-compatible with the planned Spring Boot 4.0 upgrade in a later step.
- **Jackson pinned to 2.21.7 instead of 2.15.2.** Jackson is managed by the Spring Boot 3.5
  parent BOM; pinning it to 2.15.2 would be a downgrade below the Boot baseline and is not
  done. Instead `jackson-bom.version` is set to 2.21.7, the first patch clearing the HIGH
  CVEs in the BOM default 2.21.4.
- **Tomcat pinned to 10.1.59.** `tomcat.version` overrides the Boot 3.5 BOM default
  (10.1.55), which carries CRITICAL CVEs; 10.1.59 is the fixed patch release.
- **Jakarta EE migration not required.** The project contains no `javax.*` usage, so no
  javax -> jakarta namespace changes were needed.
- **Lombok / Gradle wrapper items not applicable.** This is a Maven build and Lombok is
  not a dependency.
