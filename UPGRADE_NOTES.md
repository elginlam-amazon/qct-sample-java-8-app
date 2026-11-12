# Java 21 Upgrade Notes

The project was upgraded from Java 8 to Java 21 (Spring Boot 1.2.3.RELEASE -> 3.2.12).
Build verified with `mvn clean install` on JDK 21: BUILD SUCCESS, 12/12 tests pass.

## Deliberate non-upgrades / deferrals

- **`cglib:cglib-nodep` kept at 3.1** — left at its original version intentionally.
  Spring Framework 6 repackages its own CGLIB inside `spring-core`, so this explicit
  dependency is not on the Spring proxying path. Upgrading or removing it is unrelated
  to the Java version upgrade and is deferred to a follow-up change so the upgrade diff
  stays focused; the build and tests pass with 3.1 on Java 21.

- **No `spring-boot-maven-plugin` fat-jar repackaging introduced** — the project builds a
  plain jar, as it did before the upgrade. `FileSystemStorage` resolves its sample content
  through `java.io.File`, which does not work for resources nested inside an executable
  (fat) jar. Enabling repackaging would therefore require refactoring resource loading to
  `ClassPathResource.getInputStream()`. That is a functional change beyond the scope of the
  Java version upgrade and has been deferred.

- **Spock/Groovy** upgraded to `2.4-groovy-4.0` with Groovy `4.0.33`; the previously dormant
  Groovy specs are now compiled (gmavenplus-plugin) and executed by Surefire.

- **Insecure repository removed** — `http://repo.spring.io/milestone` was dropped; all
  artifacts resolve from Maven Central.
