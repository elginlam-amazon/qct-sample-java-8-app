# Java 25 upgrade – build fix summary

## Result
`mvn clean verify` succeeds on JDK 25 (Corretto 25.0.4) with Spring Boot 4.0.8 / Spring Framework 7.0.9 (jakarta line, as chosen by the OpenRewrite recipe).

## Changes made in this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 5.2+ replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`.
  The override now uses the new signature, keeps the SHA-512 hash, and honours the weak-ETag flag (`W/` prefix).

## Next steps
- The Spock spec `src/test/groovy/.../DownloadControllerTest.groovy` is not compiled or run: the POM has no Groovy
  compiler plugin, which was already the case before the upgrade. Spock 1.0-groovy-2.4 / Groovy 2.4.16 also does not support
  JDK 25 or JUnit Platform. To bring it back, add `gmavenplus-plugin` and move to Spock 2.4 (groovy-4.0) with
  `org.apache.groovy:groovy` 4.x, then remove the `org.codehaus.groovy:groovy-all` and `cglib-nodep` dependencies.
- The unused `<spring.version>4.1.1.RELEASE</spring.version>` property and the `http://repo.spring.io/milestone/`
  repository (blocked by Maven's HTTP blocker) in `pom.xml` are leftovers and can be removed.
- `commons-io` 2.4 and `guava` 29.0-jre are old but work. Consider upgrading them.
