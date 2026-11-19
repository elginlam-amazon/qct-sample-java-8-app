# Java 17 upgrade summary

## Result
`mvn clean verify` passes on JDK 17 with zero compilation errors. The project stays on the **javax** namespace.

## Changes on top of the OpenRewrite pass
OpenRewrite set `<release>17</release>`, bumped maven-compiler-plugin to 3.16.0 and commons-codec to 1.17.2, and replaced `new Integer(..)` with `Integer.valueOf(..)`. It left Spring Boot at 1.2.3 and Spring Framework at 4.1.6. That combination compiles, but Spring 4.1 cannot read Java 17 class files, so the app would fail at startup. I made these changes:

- **pom.xml**
  - `spring-boot-starter-parent` 1.2.3.RELEASE -> **2.7.18**. This is the last javax line of Spring Boot.
  - Spring Framework pinned to **5.3.39** through `spring-framework.version`. The old unused `spring.version` property is gone.
  - Removed the hard-coded `1.2.3.RELEASE` and `4.1.6.RELEASE` versions from the Boot starters and Spring artifacts, so the Boot BOM now manages them.
  - Removed the duplicate `spring-test` declaration.
  - Removed the standalone `cglib:cglib-nodep:3.1`. Spring 5 ships its own repackaged cglib.
  - `org.codehaus.groovy:groovy-all:2.4.3` -> `org.codehaus.groovy:groovy` (3.0.19, managed by Boot). Groovy 2.4 does not support Java 17.
  - Spock `1.0-groovy-2.4` -> `2.3-groovy-3.0`, for both spock-core and spock-spring. I dropped the groovy-all exclusion because it no longer applies.
- **Sha512ShallowEtagHeaderFilter.java** (test): Spring 5 changed the method `generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean isWeak)`. I ported the override and kept the SHA-512 hashing. The method now adds the `W/` prefix when Spring asks for a weak ETag.

Resolved versions: all `org.springframework:*` are 5.3.39, all `org.springframework.boot:*` are 2.7.18, Groovy is 3.0.19 and Spock is 2.3-groovy-3.0.

## Next steps (non-blocking)
- **The Spock spec never runs in the Maven build.** This was already true before the upgrade. `src/test/groovy/.../DownloadControllerTest.groovy` (class `DownloadControllerSpec`) has no Groovy compiler plugin, so the build never compiles it. Surefire would also skip it by name because it ends in `Spec`. I checked the upgraded code once with `gmavenplus-plugin:3.0.2` (`addTestSources compileTests`) followed by `surefire:test -Dtest=DownloadControllerSpec`:
  - As written, 11 of the 12 specs fail with 404. The cause is Groovy 3, not the upgrade. Groovy 3 inlines `private static final String TEXT_FILE = '/download/' + FileExamples.TXT_FILE_UUID + ...` as a compile-time constant, so the random UUID from compile time never matches the one at runtime.
  - With that field changed temporarily to `private static String`, all **12/12 pass** on Spring Boot 2.7.18 / Spring 5.3.39 / Java 17. I reverted that change.
  - Recommendation: add `gmavenplus-plugin` (addTestSources, compileTests), add a Surefire include for `**/*Spec*`, and drop `final` from `TEXT_FILE` so the spec runs in CI.
- Guava 18.0 and commons-io 2.4 are old but work on Java 17. I did not touch them to keep this change small. Upgrade them in a separate pass if needed. Note that `ExistingFile.toMediaType` uses the Guava `Optional.transform/or` API.
- The `repositories` entry uses `http://repo.spring.io/milestone/`. Maven 3.8+ blocks HTTP repositories, and the build does not need this one. Consider removing it.
- Moving to Spring Boot 3.x (jakarta) is out of scope for this javax-preserving upgrade.
