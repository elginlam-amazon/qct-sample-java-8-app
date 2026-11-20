# Java 17 upgrade summary

## Result
`mvn clean test` succeeds on JDK 17 with zero compilation errors. The Spring stack is on the javax line: Spring Boot 2.7.18 with Spring Framework 5.3.39 across all artifacts.

## Changes
OpenRewrite had already set `<release>17</release>`, bumped maven-compiler-plugin to 3.16.0 and commons-codec to 1.17.2, and replaced `new Integer("1234")` with `Integer.valueOf` in `MainApplication`.

Changes made on top of that:
- `pom.xml`
  - Moved the Spring Boot parent from 1.2.3.RELEASE to 2.7.18. Spring 4.1 ships an ASM that cannot read Java 17 (v61) class files, so the app would compile but fail at startup during component scanning.
  - Pinned `spring-framework.version` to 5.3.39 and removed the hard-coded `4.1.6.RELEASE` / `1.2.3.RELEASE` versions so the Boot parent manages every Spring artifact.
  - Removed the unused `spring.version` (4.1.1.RELEASE) property.
  - Removed the duplicate `spring-test` declaration that Maven warned about.
  - Removed the standalone `cglib:cglib-nodep:3.1` dependency. Spring 5.3 ships its own repackaged CGLIB/ASM.
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 5 replaced `generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`. The override now uses the new signature, still hashes with SHA-512, and adds the `W/` prefix for weak ETags the way Spring's default implementation does.

## Next steps
- **The Spock tests are not compiled or run.** `DownloadControllerTest.groovy` (class `DownloadControllerSpec`) is never compiled because the POM has no Groovy compiler plugin, such as gmavenplus. This was already true before the upgrade. Turning these tests on needs gmavenplus, Groovy 3.x or 4.x (Groovy 2.4.3 does not support Java 17), and Spock 2.x with JUnit 5. Do this in a later cycle and check that all of the spec's assertions pass.
- Guava 18.0 and commons-io 2.4 compile and work, but they are old. Consider upgrading them separately. The code uses Guava's `Optional.transform/or` in `ExistingFile.toMediaType`.
- The `http://repo.spring.io/milestone/` repository uses plain HTTP, which Maven 3.8+ blocks. It is no longer needed because every dependency resolves from Maven Central, so it can be removed.
- Actuator moved from Boot 1.x to 2.x: endpoints are now under `/actuator` and only `health` is exposed over HTTP by default. The project has no actuator configuration. Check whether any monitoring depends on the old 1.x endpoint paths.
