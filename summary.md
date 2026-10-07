# Java 25 upgrade summary

## Changes
- OpenRewrite: Spring Boot 1.2.3 -> 4.0.8 (Spring Framework 7.0.x, jakarta), Java release 25, compiler plugin 3.16.0, starter-web -> starter-webmvc, Guava 29.0-jre, `new Integer(..)` -> `Integer.valueOf(..)`.
- Manual fix: `Sha512ShallowEtagHeaderFilter` (test source) now overrides the Spring 6+/7 signature
  `generateETagHeaderValue(InputStream, boolean)`. It still produces a SHA-512 ETag and adds the `W/` prefix when a weak ETag is requested.

## Verification
- `mvn clean verify` on JDK 25: BUILD SUCCESS, with zero compilation errors.

## Next steps
- The Spock/Groovy test `src/test/groovy/.../DownloadControllerTest.groovy` is not compiled or run. The build has no Groovy compiler plugin (gmavenplus), and that was already true before the upgrade. Spock 1.0-groovy-2.4 and Groovy 2.4 do not work with JDK 25 or Boot 4. To run it again, move to Spock 2.4 with Groovy 4 and add gmavenplus-plugin, or rewrite it as a JUnit 5 test.
- `cglib-nodep 3.1` and `groovy-all 2.4.16` are still declared and probably are not needed. Consider removing them.
- The unused `spring.version` property (4.1.1.RELEASE) and the `http://repo.spring.io/milestone/` repository are leftovers. Maven blocks plain-HTTP repositories, so consider removing it.
- `commons-io 2.4` is old. Consider moving to a current 2.x release.
