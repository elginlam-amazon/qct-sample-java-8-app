# Validation Summary — Spring Boot 3.5.16 → 4.0.9

## Scope
Single-module Maven app `com.nurkiewicz.download:download-server`, Java 25, Groovy/Spock test suite.
Changes applied: `pom.xml` parent `spring-boot-starter-parent` `3.5.16` → `4.0.9`; dependency
`spring-boot-starter-web` → `spring-boot-starter-webmvc`. No `src/` changes were required.

**Criterion:** Full build and tests pass (`mvn clean verify`)
**Verification Method:** Ran `mvn clean verify -B`
**Status:** PASS
**Evidence:** `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS` on JDK 25.0.5.
**Observations:** None.

**Criterion:** Project is on Spring Boot 4.x (4.0 exactly per steering context)
**Verification Method:** Read `pom.xml`; build resolution of the parent
**Status:** PASS
**Evidence:** `<artifactId>spring-boot-starter-parent</artifactId><version>4.0.9</version>`; resolved successfully during the build.
**Observations:** 4.1-only steps deferred by design.

**Criterion:** No remaining deprecated Boot 3 patterns
**Verification Method:** Repo-wide `grep -rn`
**Status:** PASS
**Evidence:** No matches for Jackson 2 core/databind, `@MockBean`/`@SpyBean`, `antMatchers`, `authorizeRequests`, `spring-boot-starter-web<`, `PropertyMapper.alwaysApplyingNotNull`, `javax.`, `org.springframework.boot.actuate.*`.
**Observations:** None.

**Criterion:** Test integrity vs `.migration-test-baseline.tsv`
**Verification Method:** Baseline comparison and `git diff --name-status`
**Status:** PASS
**Evidence:** Baseline 12 features; `DownloadControllerTest.groovy` unmodified, no `@Disabled`/`@Ignore`, no test file deleted.
**Observations:** None.

**Criterion:** Tests actually execute (discovered count > 0)
**Verification Method:** Surefire output
**Status:** PASS
**Evidence:** 12 tests executed in `com.nurkiewicz.download.DownloadControllerSpec`.
**Observations:** None.

**Criterion:** Router artifacts present at project root
**Verification Method:** Directory listing
**Status:** PASS
**Evidence:** `code-intelligence-report.md`, `migration-plan.json`, `.migration-test-baseline.tsv`, `migration-results.yaml` present and committed.
**Observations:** None.

**Criterion:** Conditional TDs applied per detections; no dependency dropped
**Verification Method:** `migration-plan.json` detections vs `migration-results.yaml`; pom dependency diff
**Status:** PASS
**Evidence:** `tds_applied: [core-migration, observability, jspecify, verification]`; all other TDs skipped per `false` detections. No dependency removed, no `<exclusions>` added.
**Observations:** Observability/JSpecify were no-ops (no micrometer/OTel coordinates, no nullability annotations).

**Criterion:** `migration-results.yaml` at project root with valid schema and honest counts
**Verification Method:** Read file; compared counts with build log
**Status:** PASS
**Evidence:** Valid YAML with `results:` containing `compilation: PASS`, `test_integrity: PASS`, `unit_tests` 12/12/0/0 and nested `deprecated_patterns` (`jackson2_imports`, `mockbean_annotations`, `security_legacy_dsl`, `old_property_keys`, `old_starter_names` all 0).
**Observations:** None.

## OVERALL STATUS: COMPLETE

## Manual review / follow-up
- Spring Boot 4.1-only migration steps deferred by design (target pinned to 4.0).
- No `spring-boot-maven-plugin`, so no executable jar; runtime smoke tests on Boot 4.0.9
  (app boot, `/actuator/health`, download `GET`/`HEAD`, `304` on `If-None-Match`/`If-Modified-Since`,
  `301` redirect, 1 MB/s throttle, ETag acceptance including weak `W/` ETags) must be performed manually.
