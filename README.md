# Amazon Q Code Transformation: Sample Java 8 Project Built With Maven
Use this to test out Amazon Q Code Transformation!
## Java 25 upgrade notes

### Skipped / deferred updates

- **Spring Boot pinned to 3.5.16 instead of 3.2.12.** The 3.2.x line does not support
  Java 25 bytecode (ASM/CGLIB level) and resolves a Spring Framework 6.1.x version with
  known CVEs. 3.5.16 resolves Spring Framework 6.2.19, builds and runs on JDK 25, and is
  forward-compatible with the planned Spring Boot 4.0 upgrade in a later step.
- **Jackson left at the BOM-managed version (2.21.4) instead of 2.15.2.** Jackson is
  managed by the Spring Boot 3.5 parent BOM; pinning it to 2.15.2 would be a downgrade
  below the Boot baseline and is not done.
- **Jakarta EE migration not required.** The project contains no `javax.*` usage, so no
  javax -> jakarta namespace changes were needed.
- **Lombok / Gradle wrapper items not applicable.** This is a Maven build and Lombok is
  not a dependency.
