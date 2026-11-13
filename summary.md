# Java 17 upgrade summary

## Result
`mvn clean verify` passes on JDK 17 with zero compilation errors. The effective Spring Framework version is 5.3.39 for every Spring artifact. The project stays on the `javax` namespace.

## Changes
- OpenRewrite changes (kept): `maven-compiler-plugin` 3.16.0 with `<release>17</release>`, commons-codec 1.17.2, `new Integer(String)` replaced by `Integer.valueOf` in `MainApplication`.
- `pom.xml`:
  - Spring Boot parent moved from 1.2.3.RELEASE to 2.7.18 (the last javax line). Spring Framework is pinned with `<spring-framework.version>5.3.39</spring-framework.version>`. Spring Framework 4.1 cannot parse Java 17 class files during component scanning.
  - Removed the explicit Spring 4.1.6 module dependencies and the duplicate `spring-test` declaration. Boot's dependency management supplies these versions now. Removed the explicit versions on the Boot starters, and the unused `spring.version` property.
  - Removed the standalone `cglib:cglib-nodep` dependency. Spring and Spock do not need it.
  - Replaced `groovy-all` 2.4.3 with `org.codehaus.groovy:groovy`, version managed by Boot (3.0.19). Groovy 2.4 fails on Java 17 with `InaccessibleObjectException` when Boot's Groovy template auto-configuration starts.
  - Spock `spock-core`/`spock-spring` moved from 1.0-groovy-2.4 to 2.3-groovy-3.0 to match Groovy 3.
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 5 removed `generateETagHeaderValue(byte[])`. The class now overrides `generateETagHeaderValue(InputStream, boolean)` and adds the `W/` prefix for weak ETags.

## Verification
A temporary `@SpringBootTest` + MockMvc test loaded the full application context on Java 17. `GET /download/{uuid}/file.txt` returned 200 with body `foobar`, `GET /download/{uuid}` returned 301, and an unknown UUID returned 404. The test was then removed.

## Next steps
- The Groovy Spock spec (`src/test/groovy/.../DownloadControllerTest.groovy`) has never been compiled or run, because the POM has no Groovy compiler plugin (`gmavenplus-plugin`). Consider adding one so the spec runs under Spock 2 (JUnit Platform).
- The `io.spring.repo.maven.milestone` repository uses plain `http://repo.spring.io/milestone/`. Maven 3.8+ blocks HTTP repositories and the endpoint now requires authentication. No dependency needs it. Consider removing it.
- Guava 18.0 and commons-io 2.4 are old but build and run on Java 17. They were left unchanged to keep the change set small.
- Spring Boot 3.x / Jakarta migration was intentionally not done (javax path).
