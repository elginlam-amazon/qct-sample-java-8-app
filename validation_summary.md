# Spring Boot 4 Migration — Validation Summary

## OVERALL STATUS: COMPLETE

Project: `com.nurkiewicz.download:download-server` (single-module Maven)
Spring Boot: `1.2.3.RELEASE` → `4.0.9` (4.0 line exactly, per steering context)
Java: `maven.compiler.release=25`, maven-compiler-plugin 3.14.0

## Verification

| Check | Command / Method | Result |
|---|---|---|
| Full build + tests | `mvn -B clean verify` | PASS — BUILD SUCCESS |
| Test execution | surefire output | PASS — Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 |
| Test integrity | `git diff` on test sources vs `.migration-test-baseline.tsv` (12) | PASS — no tests removed or disabled |
| Deprecated Boot 3 patterns | repo-wide grep (Jackson 2 core/databind, @MockBean/@SpyBean, antMatchers, authorizeRequests, PropertyMapper.alwaysApplyingNotNull, javax.*) | PASS — 0 matches |
| Starter rename | grep pom.xml | PASS — `spring-boot-starter-webmvc` (no `spring-boot-starter-web`) |
| Target version | pom.xml parent | PASS — `4.0.9` |
| Results artifact | `migration-results.yaml` | PASS — valid, counts match build log |
| Logging pattern preserved (steering) | `git diff src/main/resources/logback.xml` | PASS — `<pattern>` text identical |

## Changes Applied

- Parent upgraded to `spring-boot-starter-parent` 4.0.9; explicit Spring/Boot artifact versions removed in favour of the Boot BOM.
- `spring-boot-starter-web` → `spring-boot-starter-webmvc`; `spring-boot-starter-actuator` retained.
- Insecure `http://repo.spring.io/milestone/` repository removed; `cglib-nodep` dropped.
- Dependency upgrades: commons-io 2.4→2.20.0, commons-codec 1.10→1.18.0, Guava 18.0→33.8.0-jre, Groovy 2.4.3→5.0.8, Spock 1.0→2.5-groovy-5.0 (gmavenplus 5.1.0).
- Surefire `<includes>` added so the previously dormant Spock suite executes.
- Test migrations: `@ContextConfiguration` → `@SpringBootTest`; `Sha512ShallowEtagHeaderFilter` override moved to `generateETagHeaderValue(InputStream, boolean)`; fixed UUID fixtures.
- `logback.xml` refactored to a named `CONSOLE` appender + `appender-ref`; logging pattern text unchanged.
- Removed a dead `new Integer("1234")` statement in `MainApplication.main`.

## Manual Review

- Target is Spring Boot 4.0.9 rather than the 4.1.x default, as required by the steering context.
- `DownloadController` behaviour and endpoints unchanged; its usage was reviewed before any controller-adjacent edits.
