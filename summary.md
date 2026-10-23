# Java 25 upgrade – build fix summary

## Result
`mvn clean verify` passes on JDK 25 (Corretto 25.0.4), Spring Boot 4.0.9 / Spring Framework 7.0.10.

## Changes made in this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 5.2+ removed
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])`. The override now targets
  `generateETagHeaderValue(InputStream, boolean isWeak)`. It still builds a SHA-512 ETag and adds the `W/` prefix when a weak ETag is requested.

OpenRewrite had already made these changes: Boot parent 1.2.3 -> 4.0.9, `spring-boot-starter-web` -> `spring-boot-starter-webmvc`,
explicit Spring versions removed (now BOM-managed), compiler `release` 25, Guava 29.0-jre, `new Integer(..)` -> `Integer.valueOf(..)`,
and the redundant `@Autowired` on the constructor removed.

## Next steps
- The Groovy/Spock test `src/test/groovy/.../DownloadControllerTest.groovy` is never compiled or run because no Groovy compiler plugin
  (for example gmavenplus) is configured. This was already true before the upgrade. Spock 1.0-groovy-2.4 and Groovy 2.4.16 do not support
  Java 25 or JUnit 5. To bring the test back, move to Spock 2.4 (groovy-4.0) with `org.apache.groovy:groovy` and add gmavenplus.
- The `spring.version` property (4.1.1.RELEASE) is no longer used anywhere and can be removed.
- `cglib:cglib-nodep:3.1` is not needed with Spring 7, which ships its own repackaged cglib. Consider removing it.
- The `repo.spring.io/milestone` repository uses plain `http`. Maven 3.8+ blocks it, and no dependencies need it. Consider removing it.
