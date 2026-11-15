# Java 25 upgrade: build fix summary

## Stack
JDK 25 (`release 25`), Spring Boot 4.0.9 (Spring Framework 7.0.x, Jakarta EE 11), Groovy 5.0.8 (Boot-managed), Spock 2.5-groovy-5.0.

## Changes made
### Previous cycle
- `Sha512ShallowEtagHeaderFilter.java` (test): Spring Framework 7 removed `generateETagHeaderValue(byte[])`. The override now implements `generateETagHeaderValue(InputStream, boolean isWeak)`, still producing a quoted SHA-512 ETag, `W/`-prefixed when weak.
- `pom.xml`: removed the stale, unused `spring.version` (4.1.1.RELEASE) property.

### This cycle
- `pom.xml`, Spock test toolchain. The Spock spec was not compiled or run before this cycle:
  - Replaced Spock `1.0-groovy-2.4` with `2.5-groovy-5.0` (`spock-core`, `spock-spring`) and dropped their old Groovy/JUnit 4 exclusions.
  - Replaced compile-scope `org.codehaus.groovy:groovy-all:2.4.16` (and its `dependencyManagement` pin) with test-scope `org.apache.groovy:groovy`, with the version managed by the Boot BOM. Groovy no longer ships in the application jar.
  - Added `gmavenplus-plugin` 5.1.0 (`addTestSources`, `compileTests`, bytecode target 25).
  - Surefire now includes `**/*Spec.java` alongside the default `*Test`/`*Tests` patterns.
- `pom.xml`: removed unused `cglib:cglib-nodep:3.1`. No code references it, and Spring uses its own repackaged CGLIB.
- `pom.xml`: removed the plain-HTTP `repo.spring.io/milestone` repository. Maven 3.8+ blocks HTTP repositories, and no dependency needs it.
- `pom.xml`: added `spring-boot-maven-plugin` (version and `repackage` goal come from the Boot parent). `target/download-server-0.0.1-SNAPSHOT.jar` is now an executable Boot jar (`Start-Class: com.nurkiewicz.download.MainApplication`).
- Renamed `src/test/groovy/.../DownloadControllerTest.groovy` to `DownloadControllerSpec.groovy` to match its class.
- `src/main/resources/logback.xml`: Logback 1.5 rejects an unnamed `<appender>` ("Missing attribute [name]"), so the console appender was not attached and logging was lost. It is now a named `CONSOLE` appender referenced from root, with the same pattern and levels.

## Result
`mvn -B clean verify` on JDK 25 (Corretto 25.0.5): BUILD SUCCESS, zero compilation errors, `DownloadControllerSpec` 12 tests run, 0 failures, 0 errors, 0 skipped. No Logback configuration errors. No `javax.*` Java EE APIs, no `org.codehaus.groovy`, and no JUnit 4 in the dependency tree.

### Re-validation after additional migration steps
Rebuilt with `mvn -B clean verify`: BUILD SUCCESS, 12/12 specs pass, no code changes needed. The dependency tree is jakarta-only (servlet-api 6.1.0, annotation-api 3.0.0, xml.bind-api 4.0.5). Spring Framework is 7.0.10 and Spring Boot is 4.0.9, both managed by the Boot BOM. There are no `javax.*` Java EE artifacts and no `org.codehaus.groovy` or JUnit 4.

## Next steps
- Carry-over items that need people or external systems, not done here: merging the staging branches through a PR, running CI on JDK 25, functional/regression testing of downloads, ETag/`If-None-Match` and throttled streaming, reviewing actuator endpoint exposure under Boot 4 defaults, running an SCA scan, and updating container/runtime images to Java 25.
- The carry-over mentions `spotbugs`/`checkstyle` skip flags. This POM declares neither plugin, so there is nothing to un-skip in this project. CI should still run without any skip flags.
- `commons-io` 2.4 and `guava` 29.0-jre still work but are old. Consider upgrading them to current releases (e.g. commons-io 2.2x, guava 33.x-jre).
- The explicit `spring-*` dependencies, the `spring-test` exclusion on `spring-boot-starter-test`, and the test-scope `jakarta.servlet-api` are redundant with the Boot starters. They are BOM-managed and consistent, so they were left in place. They are candidates for cleanup.
- `MainApplication.main` contains an unused `Integer temp = Integer.valueOf("1234")` (left over from the original `new Integer(...)`). It can be removed.
- The Surefire `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}` argLine is kept so Mockito/Byte Buddy can attach on JDK 21+ without warnings.
- Track Spring Boot 4.0.x patch releases and evaluate 4.1.x.
