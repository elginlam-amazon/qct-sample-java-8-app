# Java 25 upgrade summary

Target: JDK 25, Spring Boot 4.0.9 (Spring Framework 7.0.10, Jakarta EE 11).

## Changes
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: fixed the compile error from
  iteration 25. Spring Framework replaced `generateETagHeaderValue(byte[])` with
  `generateETagHeaderValue(InputStream, boolean isWeak)`. The override uses the new signature, still hashes with SHA-512,
  and adds the `W/` prefix for weak ETags, as the base class does.
- `pom.xml`:
  - Removed the unused `spring.version` property (4.1.1.RELEASE).
  - Spock 1.0-groovy-2.4 to `2.4-groovy-5.0` (spock-core, spock-spring). Groovy 2.4 does not run on JDK 25.
  - `org.codehaus.groovy:groovy-all` 2.4.16 (compile scope, pinned in dependencyManagement) replaced by
    `org.apache.groovy:groovy` (version 5.0.8 from the Boot BOM), test scope. Main code does not use Groovy.
  - Removed `cglib-nodep` 3.1. Nothing in the code used it; Spock 2 uses Byte Buddy for mocks.
  - Added `gmavenplus-plugin` 5.1.0 (`targetBytecode` = `${java.version}`) so the Spock spec is compiled.
    Before this, the spec was never compiled or run, which is why the build reported 0 tests.
  - Surefire now also includes `**/*Spec.java`, because the spec class is named `DownloadControllerSpec`.
  - Removed the explicit `maven-compiler-plugin` version (3.16.0) so the Spring Boot parent manages it (3.14.1).
- `src/main/java/.../FileSystemPointer.java`: replaced the deprecated `Files.hash(File, HashFunction)` with
  `Files.asByteSource(File).hash(HashFunction)`. Same result.
- `src/main/resources/logback.xml`: newer Logback rejects an inline `<appender>` without a `name`. The appender is now
  named `CONSOLE` and referenced from root. Pattern and logger levels are unchanged.

## Verification
- `mvn clean verify` on Corretto 25.0.5.1: BUILD SUCCESS, no compiler or deprecation warnings.
- `DownloadControllerSpec`: 12 tests run, 0 failures, 0 errors, 0 skipped.
- No `javax.*` Java EE imports or artifacts. All Spring artifacts resolve to 7.0.10.

## Next steps
- The `http://repo.spring.io/milestone/` repository uses plain HTTP, which Maven 3.8+ blocks. The build does not need it;
  consider removing it.
- `guava` 29.0-jre and `commons-io` 2.4 work on JDK 25 but are old; consider updating them.
- The spec file is `DownloadControllerTest.groovy` but the class is `DownloadControllerSpec`. Consider renaming the file
  to match the class.
- Runtime startup was not checked (the instructions say not to run the jar). The MockMvc spec loads the full Spring
  context with the `test` profile, which covers context startup.
