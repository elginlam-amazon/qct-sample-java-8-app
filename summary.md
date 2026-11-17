# Java 25 upgrade summary

## Changes
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 7 replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`.
  The override uses the new signature, still hashes the body with SHA-512, and adds `W/` when a weak ETag is requested.
- `pom.xml`:
  - Removed the stale `spring.version` property (4.1.1.RELEASE); the Spring Boot 4.0.9 parent manages Spring (7.0.10).
  - Spock spec now compiles and runs: replaced Spock `1.0-groovy-2.4` with the `spock-bom` `2.5-groovy-5.0` import,
    replaced `org.codehaus.groovy:groovy-all` 2.4.16 with Boot-managed `org.apache.groovy:groovy` 5.0.8 (test scope, since
    main code does not use Groovy), added `gmavenplus-plugin` 4.2.1 (`compileTests`, `targetBytecode` = `${java.version}`),
    and added `**/*Spec.java` to the Surefire includes so `DownloadControllerSpec` is picked up.
    Dropped the junit/groovy-all exclusions that only applied to Spock 1.0.
- `src/main/resources/logback.xml`: the console appender was nested in `<root>` without a name, which Logback 1.5 rejects
  (`Missing attribute [name]`, `Ignoring unknown property [encoder]`), so nothing was logged to the console. It is now a
  top-level appender named `CONSOLE` referenced from `<root>`. Pattern and levels are unchanged.

## Result
- `mvn clean verify` on JDK 25 (Corretto 25.0.5): BUILD SUCCESS, zero compilation errors.
- `DownloadControllerSpec`: 12 tests run, 0 failures, 0 errors, 0 skipped. This covers ETag (`If-None-Match`),
  `If-Modified-Since`, HEAD, 404, Content-Length and Content-Type behaviour through MockMvc.
- No Logback errors or warnings at test startup.
- Dependency tree: jakarta only (servlet 6.1, annotation 3.0, JAXB 4.0), all Spring Framework artifacts on 7.0.10,
  JUnit Platform 6.0.3, Spock 2.5-groovy-5.0, Groovy 5.0.8. No `javax.*` Java EE artifacts.

## Re-verification after additional migration steps
- `mvn clean verify` re-run: BUILD SUCCESS, 0 compilation errors, 12/12 tests pass. No code changes were needed.
- No `javax.*` imports in sources; dependency tree is jakarta only and all Spring artifacts are on 7.0.10 (Boot BOM).

## Carry-over items checked
- Caffeine / `sun.misc.Unsafe`: Caffeine is not in the dependency tree and no `Unsafe` warning appears in the build. Nothing to change.
- Non-Boot Spring artifacts: all resolve to 7.0.10 via the Boot BOM; no mixed versions.
- `jakarta.servlet-api` (test scope): no main code imports the servlet API; at runtime it comes from embedded Tomcat. Left as is.
- Actuator paths: nothing in this repository references `/health` or other management paths.

## Next steps
- `Sha512ShallowEtagHeaderFilter` is not registered or used anywhere (the ETags come from `FileSystemPointer`), so no test
  exercises it. Register it or remove it in a later cycle.
- External monitoring that calls Boot 1.x paths (`/health`, `/info`) must move to `/actuator/health`, `/actuator/info`;
  Boot 4 also exposes only `health` over HTTP by default.
- Spock 2.5 on JUnit Platform 6 works here, but the combination is new; watch for test framework issues.
- `cglib-nodep` 3.1 (compile scope) was used by Spock 1.0 for mocking and is now unused; `commons-io` 2.4 is old.
- The `http://repo.spring.io/milestone/` repository uses HTTP, which Maven 3.8+ blocks; it is not needed.
- End-to-end check against a running server with a real HTTP client was not done (non-interactive cycle; MockMvc tests cover the logic).
