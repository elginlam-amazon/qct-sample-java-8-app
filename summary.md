# Java 17 upgrade summary

## Changes
- `pom.xml`
  - Java release 17, maven-compiler-plugin 3.16.0, commons-codec 1.17.2 (from OpenRewrite).
  - Spring Boot parent 1.2.3.RELEASE -> 2.7.18 (last javax Boot line). Spring 4.1 cannot read Java 17 class files at runtime, so Boot 2.7 / Spring 5.3 is the minimum for Java 17 without moving to jakarta.
  - Spring Framework pinned to 5.3.39 via the Boot `spring-framework.version` property (replaces the unused `spring.version` property).
  - Removed hard-coded `1.2.3.RELEASE` / `4.1.6.RELEASE` versions from Boot starters and Spring artifacts so the Boot BOM manages them.
  - Removed the duplicate `spring-test` declaration and the standalone `cglib-nodep` dependency (Spring ships its own repackaged CGLIB).
  - Spring milestone repository URL switched from http to https (Maven 3.8+ blocks http repositories).
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: overrides the Spring 5 hook `generateETagHeaderValue(InputStream, boolean)` instead of the removed `generateETagHeaderValue(byte[])`. It still uses SHA-512 and adds the `W/` prefix for weak ETags, the same as the Spring 5 base class.

## Verification
- `mvn clean test`: BUILD SUCCESS, zero compilation errors.
- `mvn dependency:list`: every `org.springframework` artifact is at 5.3.39 and Boot at 2.7.18. No jakarta artifacts.

## Next steps
- The Spock spec `src/test/groovy/.../DownloadControllerTest.groovy` has never been compiled or run by this build, before or after the upgrade, because there is no Groovy compiler plugin (for example gmavenplus). Enabling it on Java 17 needs Spock 2.x with Groovy 3+ (Spock 1.0/Groovy 2.4.3 does not support Java 17, and Spock 1.x needs the JUnit 4 vintage engine). This was left unchanged to stay in scope.
- `groovy-all:2.4.3` is declared with compile scope even though only tests use Groovy. Consider moving it to test scope or upgrading it together with Spock.
- Guava 18.0 and commons-io 2.4 work on Java 17 but are old. Upgrading them is optional.
