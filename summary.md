# Java 25 / Spring Boot 4.0.9 upgrade

## Result
`mvn clean test` on JDK 25 (Corretto 25.0.5): BUILD SUCCESS, **12 tests run, 0 failures, 0 errors** (`DownloadControllerSpec`).
The dependency tree contains no `javax.*` artifacts, no `org.codehaus.groovy`, and no JUnit 4.

## Carry-over items from iteration 9
- **Failed `AWS/java-version-upgrade` step / Spring Boot 3.2.12 vs 3.5.16 conflict:** these don't apply to the current working tree. The project is on `spring-boot-starter-parent` 4.0.9 (Spring Framework 7.0.10), which builds JDK 25 bytecode and runs on it without problems. No 3.x Boot version appears anywhere in the build.
- **Testing / re-run 12 tests:** before this iteration, the Groovy Spock tests were never compiled, so Surefire ran 0 tests. They are now compiled and all 12 pass.

## Changes
### Cycle 1
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 7 removed `generateETagHeaderValue(byte[])`. The class now overrides `generateETagHeaderValue(InputStream, boolean isWeak)`. It still returns a SHA-512 ETag and adds the `W/` prefix when Spring asks for a weak ETag.

### Cycle 2 (this iteration)
- `pom.xml`:
  - Replaced Spock `1.0-groovy-2.4` with `2.4-groovy-5.0` (`spock.version` property) for `spock-core` and `spock-spring`. The old version cannot run on JDK 25 or Spring 7.
  - Replaced `org.codehaus.groovy:groovy-all` 2.4.16 (compile scope, plus its `dependencyManagement` pin) with `org.apache.groovy:groovy` at test scope. Its version (5.0.8) is managed by the Boot BOM. The main code does not use Groovy.
  - Added `gmavenplus-plugin` 4.3.1 (`addTestSources`, `compileTests`, `targetBytecode=${java.version}`) so `src/test/groovy` is compiled.
  - Surefire now includes `**/*Spec` as well as the default `*Test` and `*Tests` patterns. The Spock class is named `DownloadControllerSpec`.
  - Removed the unused `spring.version` property (4.1.1.RELEASE).
- `src/main/resources/logback.xml`: Logback 1.5 rejects an `<appender>` declared inline inside `<root>` ("Missing attribute [name]"), which left the app with no console appender. It is now a named `CONSOLE` appender referenced from `<root>`, with the same pattern and levels.

## Next steps (non-blocking)
- `FileSystemStorage` is a stub that serves `logback.xml` for every UUID. Editing `logback.xml` therefore changes the ETag and size it serves. This doesn't matter for the stub, but it should get a real implementation.
- Dependencies that may be stale or unused: `cglib-nodep` 3.1 (no usages found), `commons-io` 2.4 (works, but old), and `guava` 29.0-jre (works; it could be bumped to a current `-jre` release).
- The `repo.spring.io/milestone` repository uses plain `http://`, and Boot 4 GA doesn't need it. Consider removing it.
- The explicit `spring-*` and `jakarta.servlet-api` dependencies duplicate what `spring-boot-starter-webmvc` already provides. They are BOM-managed and could be trimmed.
- `MainApplication.main` still has the leftover `Integer temp = Integer.valueOf("1234");` line, which is unused.
