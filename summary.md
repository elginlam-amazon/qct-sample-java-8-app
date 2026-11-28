# Java 25 / Spring Boot 4.0.10 upgrade: iteration 37

## Result
`mvn clean test` gives BUILD SUCCESS on JDK 25 (Corretto 25.0.5.1). Compiling with `-Xlint:deprecation` shows no warnings.
Surefire now reports **12 tests run, 0 failures, 0 errors, 0 skipped** (`DownloadControllerSpec`). Before this iteration, no tests ran.

## Changes in this cycle
- **Test fix**: `Sha512ShallowEtagHeaderFilter.java` now overrides the Spring 7 signature
  `generateETagHeaderValue(InputStream, boolean isWeak)`. It still hashes with SHA-512 and adds the `W/` prefix for weak ETags.
- **Spock tests enabled** (`pom.xml`):
  - Spock `1.0-groovy-2.4` → `2.5-groovy-5.0` (`spock-core`, `spock-spring`), using the `spock.version` property.
  - `org.codehaus.groovy:groovy-all` 2.4.16 (compile scope) → `org.apache.groovy:groovy` (test scope). Spring Boot manages its version (5.0.8).
    The `groovy-all` dependencyManagement override and the old groovy-all/junit exclusions were removed.
  - Added `gmavenplus-plugin` 5.1.0 (`addTestSources`, `compileTests`) so that `src/test/groovy` is compiled.
  - Surefire includes now add `**/*Spec` next to the default `*Test`/`*Tests` patterns, because the spec class is `DownloadControllerSpec`.
  - The spec itself needed no changes, and all of its assertions are unchanged.
- **Dependency upgrades**: `commons-io` 2.4 → 2.22.0 and `guava` 29.0-jre → 33.8.0-jre. Both are past the CVE-fixed versions (2.14.0 and 32.0.0).
- **Cleanup**: removed the unused `cglib-nodep` 3.1 (nothing references it), the stale `spring.version` property and the
  `http://repo.spring.io/milestone/` repository (Boot 4 GA is on Maven Central).
- **Deprecated API**: `FileSystemPointer` now uses `Files.asByteSource(target).hash(Hashing.sha512())` instead of the deprecated
  `Files.hash(File, HashFunction)`. The ETag value is the same.
- **logback.xml**: the root logger had an anonymous inline `<appender>`. Logback 1.5 rejects this (`Missing attribute [name]`), so console logging
  was silently lost. The appender is now named (`CONSOLE`), and the root logger refers to it with `<appender-ref>`. The pattern and levels are unchanged.

## Dependency state
- Spring Framework 7.0.11, Spring Boot 4.0.10, Groovy 5.0.8 and JUnit Platform 6 come from the Boot BOM. The stack uses only jakarta (servlet 6.1,
  annotation 3.0, xml.bind 4.0.5), and the dependency tree has no Java EE `javax.*`, `org.codehaus.groovy`, `junit:junit` or cglib artifacts.

## Next steps (non-blocking)
- **Runtime validation**: in this cycle, `DownloadControllerSpec` checks runtime behaviour by starting the Spring context and calling the
  controller through MockMvc. It covers ETag, If-Modified-Since, HEAD, 404 and Content-Type. Following the cycle rules, the application was not started as a
  standalone process. For a real smoke start, set `ENABLE_RUNTIME_VALIDATION=true` in the pipeline.
- **Packaging / deployment**: the POM has no `spring-boot-maven-plugin`, which was also true before the upgrade. As a result, `mvn package` builds a plain jar, not an
  executable fat jar. If the target is an executable jar with embedded Tomcat, add `spring-boot-maven-plugin` (version managed by the parent).
  If the target is an external servlet container, switch to `war` packaging, mark `spring-boot-starter-tomcat` as `provided` and add a
  `SpringBootServletInitializer`. I could not confirm the deployment target from the repository.
- `FileSystemStorage` is still a stub that returns `logback.xml` for every UUID, as it was before the upgrade. It is not part of the upgrade, but it must be replaced before production.
- The test logback config comes from `src/main/resources/logback.xml` and logs at `ALL` for `com.nurkiewicz`. Consider a quieter
  `logback-test.xml` and environment-specific levels for prod.
- Review branch `main-upgrade-itr-37` against `main-upgrade-itr-36` (`3bf0d64`) before raising a PR.
