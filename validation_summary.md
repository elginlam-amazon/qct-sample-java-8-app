# Validation Summary — Spring Boot 3.5.16 → 4.0.9

## Overall status: PASS

## Scope
Single-module Maven app `com.nurkiewicz.download:download-server`, Java 25, Groovy/Spock test suite.

## Changes applied
- `pom.xml`: parent `spring-boot-starter-parent` `3.5.16` → `4.0.9`
- `pom.xml`: dependency `spring-boot-starter-web` → `spring-boot-starter-webmvc`
- Artifacts added: `migration-plan.json`, `code-intelligence-report.md`,
  `.migration-test-baseline.tsv`, `migration-results.yaml`
- No `src/` changes were required — no Boot-3-only APIs present.

## Criteria results
| Criterion | Status | Evidence |
|---|---|---|
| Full build and tests (`mvn clean verify`) | PASS | `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS` |
| Project on Spring Boot 4.x (4.0 exactly per steering) | PASS | parent version `4.0.9` |
| No remaining deprecated Boot 3 patterns | PASS | repo-wide greps for Jackson 2 core/databind, `@MockBean`/`@SpyBean`, `antMatchers`, `authorizeRequests`, `spring-boot-starter-web`, `PropertyMapper.alwaysApplyingNotNull`, `javax.*`, `org.springframework.boot.actuate.*` → no matches |
| Test integrity vs baseline | PASS | baseline 12 features; current file has 12 `def` features, no `@Disabled`/`@Ignore` |
| Tests actually execute | PASS | 12 executed in `DownloadControllerSpec` |
| Router artifacts present | PASS | `code-intelligence-report.md`, `migration-plan.json` at project root |
| Conditional TDs applied (core-migration, observability, jspecify) | PASS | observability/jspecify were no-ops (no micrometer/otel coordinates, no nullability annotations) |
| `migration-results.yaml` at project root | PASS | written with observed build results |

## Manual review / follow-up
- Spring Boot 4.1-only migration steps deferred by design (target pinned to 4.0).
- No `spring-boot-maven-plugin`, so no executable jar; runtime smoke tests on
  Boot 4.0.9 (app boot, `/actuator/health`, download `GET`/`HEAD`, `304` on
  `If-None-Match`/`If-Modified-Since`, `301` redirect, 1 MB/s throttle, ETag
  acceptance including weak `W/` ETags) must be performed manually.
