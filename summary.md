# Java 25 / Spring Boot 4.0.9 upgrade: build fix

## Build status
`mvn clean verify` passes on JDK 25 (Corretto 25.0.5.1).

## Change made this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 7 replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`.
  The override now uses the new signature. It still returns a quoted SHA-512 ETag and adds the `W/` prefix when Spring
  asks for a weak ETag, as the base implementation does.

## Next steps
- Zero tests run. The only test, `src/test/groovy/.../DownloadControllerTest.groovy` (Spock), is never compiled
  because the POM has no Groovy compiler plugin (this was already true before the upgrade). To run it, add
  `gmavenplus-plugin` and move to Groovy 4 (`org.apache.groovy`) with Spock `2.x-groovy-4.0`. Spock 1.0 and
  Groovy 2.4 do not run on JDK 25 or JUnit Platform.
- Stale dependencies to review: `commons-io` 2.4, `cglib-nodep` 3.1 (probably unneeded), `groovy-all` 2.4.16,
  `spock-*` 1.0-groovy-2.4, and the unused `spring.version` property.
- The `http://repo.spring.io/milestone/` repository uses plain HTTP, which Maven 3.8+ blocks. It is not needed for
  the current dependencies and can be removed.
