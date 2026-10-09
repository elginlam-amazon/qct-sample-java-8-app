# Java 25 upgrade summary

OpenRewrite moved the project to Java 25 and Spring Boot 4.0.8 (Spring Framework 7.0.x, which uses jakarta.*). After that, `mvn clean verify` failed with one test-compile error. It now builds cleanly.

## Fix applied
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 5+ removed `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])`. The override now uses `generateETagHeaderValue(InputStream, boolean isWeak)`. It still produces a SHA-512 ETag and adds the `W/` prefix when a weak ETag is requested.

## Next steps
- The Spock/Groovy test `src/test/groovy/.../DownloadControllerTest.groovy` is not compiled or run. This was already true before the upgrade: no Groovy compiler plugin is configured. Spock `1.0-groovy-2.4` and Groovy `2.4.16` do not work with Java 25 or Spring 7. To bring the test back, add `gmavenplus-plugin` and move to Spock `2.4-groovy-4.0` with `org.apache.groovy:groovy` 4.x.
- Cleanup candidates in `pom.xml`:
  - The `spring.version` (4.1.1.RELEASE) property is not used.
  - `cglib:cglib-nodep:3.1` is not needed with Spring 7, which bundles its own cglib.
  - The `http://repo.spring.io/milestone/` repository uses plain http, which Maven blocks, and is not needed.
  - The explicit `spring-*` and `jakarta.servlet-api` entries could be left to the Boot starters.
- `commons-io` 2.4 and `guava` 29.0-jre are old but still work; consider upgrading them.
