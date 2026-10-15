# Java 25 upgrade – build fix summary

## State after OpenRewrite
- Java 25 (`maven-compiler-plugin` 3.16.0, `<release>25</release>`), Spring Boot 1.2.3 -> 4.0.8 (Spring Framework 7.0.x, jakarta namespace).
- Main sources compiled cleanly after the recipe.

## Fix applied
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` no longer exists (since Spring 5.1). Changed the override to `generateETagHeaderValue(InputStream, boolean isWeak)`. It still uses SHA-512 hashing and now adds the `W/` prefix for weak ETags, as the parent class does.

## Verification
- `mvn clean test`: BUILD SUCCESS, zero compilation errors.

## Next steps
- The Spock spec `src/test/groovy/.../DownloadControllerTest.groovy` was never compiled, before or after the upgrade, because the pom has no Groovy compiler plugin (e.g. gmavenplus). That means surefire runs no tests. Turning it on would need Groovy 4.x + Spock 2.x (`spock-spring` 2.4-groovy-4.0), because Groovy 2.4 / Spock 1.0 do not run on Java 25 or Spring 7.
- `cglib-nodep` 3.1 and `groovy-all` 2.4.16 are stale dependencies. Spring 7 repackages cglib internally, so `cglib-nodep` can probably be removed.
- The unused `spring.version` property (4.1.1.RELEASE) and the `http://repo.spring.io/milestone/` repository (plain http, which Maven 3.8+ blocks) can be removed.
