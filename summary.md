# Java 25 upgrade summary

OpenRewrite moved the project to Java 25 and Spring Boot 4.0.8 (Spring Framework 7.0.9, jakarta namespace).

## Build fix
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 6+ replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`.
  The override now reads the stream, hashes it with SHA-512 as before, and adds the `W/` prefix when a weak ETag is requested.

`mvn clean verify` now passes on JDK 25 (Corretto 25.0.4).

## Next steps
- The Spock spec `src/test/groovy/.../DownloadControllerTest.groovy` is not compiled or run because the POM has no
  Groovy compiler plugin. This was already true before the upgrade. To enable it, add `gmavenplus-plugin` and move to
  Spock 2.x (`spock-spring` 2.4-groovy-4.0) with `org.apache.groovy:groovy` 4.x. Spock 1.0 and Groovy 2.4 do not run on Java 25.
  The spec also uses `@ContextConfiguration` on a Boot app, so it would likely need `@SpringBootTest`.
- `groovy-all` 2.4.16 (main scope), `cglib-nodep` 3.1, the unused `spring.version` property and the `http://repo.spring.io/milestone`
  repository are leftovers from before the upgrade. They are candidates for removal.
- `guava` 29.0-jre and `commons-io` 2.4 are old but still build. Consider upgrading them.
