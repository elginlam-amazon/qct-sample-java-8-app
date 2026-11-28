# Java 25 upgrade: build fix summary

## Result
`mvn clean verify` passes on JDK 25 (Corretto 25.0.5.1) with Spring Boot 4.0.10 / Spring Framework 7.0.11.

## Changes in this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 6+ replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with
  `generateETagHeaderValue(InputStream, boolean isWeak)`. The override now uses the new signature.
  It still produces a quoted SHA-512 hex ETag and adds the `W/` prefix when a weak ETag is requested,
  which matches the base class.
- `pom.xml`: removed the unused, stale `spring.version` property (4.1.1.RELEASE). The Spring Boot parent manages Spring versions.

## Carry-over from iteration 35
- Iteration 35 failed on `Sha512ShallowEtagHeaderFilter.java:[8,9] method does not override or implement a method from a supertype`.
  The signature change above fixes it. Main (8 files) and test (3 files) sources now compile with `release 25`,
  and `mvn clean verify` ends in BUILD SUCCESS.
- Dependency check: every Spring artifact resolves to 7.0.11 through the Boot 4.0.10 parent, the servlet API is
  `jakarta.servlet-api` 6.1.0, and the sources contain no Java EE `javax.*` imports.
- Tests: Surefire still runs 0 tests, because the only test class is the Groovy spec, which is not compiled (see below).

## Next steps
- Security review: `commons-io` 2.4 has known CVEs (for example CVE-2021-29425, path traversal in `FileNameUtils.normalize`).
  2.14.0 or later fixes them. `guava` 29.0-jre has CVE-2020-8908 and CVE-2023-2976 (temp directory permissions),
  fixed in 32.0.0-jre or later. Neither version was bumped this cycle because the build works with both.
- The Groovy Spock spec `src/test/groovy/.../DownloadControllerTest.groovy` is **not compiled or run**.
  The original POM had no Groovy compiler plugin either, so 0 tests run. To run it, you would need to add
  `gmavenplus-plugin` and replace Spock `1.0-groovy-2.4` / Groovy 2.4.16 with Spock 2.x
  (`spock-spring` 2.4-groovy-4.0, `org.apache.groovy` 4.x). Groovy 2.4 does not run on JDK 25, and Spock 1.0
  does not support Spring 7 / JUnit Platform.
- `groovy-all` 2.4.16 is a compile-scope dependency, but production code does not use it. Consider removing it
  or moving it to test scope.
- `cglib-nodep` 3.1 is unused in source and does not work on modern JDKs. Consider removing it.
- `commons-io` 2.4 and `guava` 29.0-jre are old. The build works with them, but newer versions are recommended.
- The `http://repo.spring.io/milestone/` repository uses plain HTTP, which Maven 3.8.1+ blocks, and is not needed.
  Consider removing it.
