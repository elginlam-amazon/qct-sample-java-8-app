# Java 25 / Spring Boot 4 upgrade: summary (iteration 56)

## Stack
Java 25 (`<release>25</release>`), Spring Boot 4.0.9 (Spring Framework 7.0.10, Jakarta Servlet, embedded Tomcat), Groovy 5 (Boot-managed `org.apache.groovy:groovy`), Spock 2.5-groovy-5.0 on the JUnit Platform. The dependency list and the source tree have no Java EE `javax.*` artifacts or imports, and no legacy `org.codehaus.groovy` artifacts.

## Starting point of this cycle
The `main-upgrade` working tree had only the raw OpenRewrite output: Spock 1.0 / Groovy 2.4 with no Groovy compiler, so no test ran. Iteration 55's work was on `main-upgrade-itr-55` (commit `4dd361d`) and had not been merged.

## Changes in this cycle
- **Iteration 55 commit `4dd361d` reviewed and applied** to the working tree (as file content, not as a git merge), for `pom.xml`, `FileSystemPointer.java` and `logback.xml`:
  - The pom adds `gmavenplus-plugin` 4.3.1, Spock 2.5-groovy-5.0 and Groovy 5, and Surefire picks up `*Spec` classes. It drops the redundant `org.springframework:*` declarations, `cglib-nodep`, the unused `spring.version` property and the plain-HTTP milestone repository. It bumps `guava` to 33.8.0-jre and `commons-io` to 2.22.0 (CVE fixes).
  - `FileSystemPointer` uses `Files.asByteSource(file).hash(sha512())` in place of the deprecated `Files.hash`. The hash and the ETag values are the same.
  - `logback.xml` uses a named top-level `CONSOLE` appender. Logback 1.5 rejects the unnamed inline appender. The pattern and logger levels are unchanged.
  - The `Sha512ShallowEtagHeaderFilter` fix from the previous cycle is kept, using `InputStream.readAllBytes()`.
- **Redirect endpoint tests** added to `DownloadControllerSpec` for `GET /download/{uuid}`:
  - 301 with `Location: /download/{uuid}/{originalName}`
  - 404 for an unknown UUID
  - 304 with the `ETag` echoed when `If-None-Match` matches
- **pom clean-up (carry-over):**
  - Removed the test-scoped `jakarta.servlet-api`, because `tomcat-embed-core` from the webmvc starter provides the Servlet API.
  - Removed `spring-context-support`, because nothing in `src` uses its classes (cache, mail, scheduling or UI support).

## Verification
`mvn clean test` on JDK 25 (Corretto 25.0.5) gives BUILD SUCCESS, with 15 tests run, 0 failures, 0 errors and 0 skipped.
- **Logging:** console output during the test-context startup uses the configured pattern (`HH:mm:ss.SSS | thread | level | logger | msg`). No Logback configuration errors appear.
- **Actuator:** startup logs `Exposing 1 endpoint beneath base path '/actuator'`, which is the Boot 4 default of health only.

## Carry-over items status
- **Smoke start:** not run. `ENABLE_RUNTIME_VALIDATION` is not set in this environment, and the cycle rules forbid starting the jar.
- **Integration checks against a running instance:** not run, for the same reason. The Spock spec checks the same behaviours through MockMvc on the full `MainApplication` context: 200, 301, 304 by ETag and by `If-Modified-Since`, HEAD, 404, `Content-Length` and `Content-Type`. The embedded Tomcat is not exercised.
- **Redirect test:** done (see above).
- **Actuator:** Boot 1.2 exposed every endpoint over HTTP at the root (`/health`, `/env`, `/metrics` and so on). Boot 4 exposes only `/actuator/health`. I kept the secure default. Confirm with the monitoring owners. If they need more, set `management.endpoints.web.exposure.include` explicitly.
- **Deployment model:** the packaging is `jar` (the Maven default), there is no `SpringBootServletInitializer`, and the README describes a sample project. This is an executable jar, so the embedded Tomcat stays at compile scope.
- **`jakarta.servlet-api` / `spring-context-support`:** both removed (see above).
- **Log output:** checked during the test-context startup (see Verification).
- **Commit `4dd361d`:** reviewed and applied to the working tree. The changes are not committed. They are left for the iteration harness or a human to commit.

## Next steps
- Run the smoke start and the live HTTP checks when `ENABLE_RUNTIME_VALIDATION=true` is available.
- `commons-io` and `commons-codec` are declared, but nothing in `src` imports them. Consider removing both.
- `FileSystemStorage` is still the upstream stub that serves `logback.xml` for every UUID. This behaviour is unchanged.
- `Sha512ShallowEtagHeaderFilter` (a test helper) is not referenced by any test.
- gmavenplus 5.x is available. 4.3.1 works, so I did not upgrade it.

## Re-validation after additional migration steps (iteration 56)
I rebuilt after the additional migration steps. `mvn clean test` on JDK 25 gives BUILD SUCCESS with 15 tests run, 0 failures, 0 errors and 0 skipped, and no compilation errors or deprecation warnings. I made no code changes. The source tree has no `javax.*` imports, and the resolved dependency list has no `javax.*` or `org.codehaus.groovy` artifacts.
