# Java 25 upgrade – build fix summary

## State after OpenRewrite
- Java 25 (`maven-compiler-plugin` 3.16.0, `<release>25</release>`), Spring Boot parent 4.0.9 (Spring Framework 7.0.x, jakarta namespace).
- Main sources compiled; test compilation failed.

## Fix applied
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 7 removed
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])`. Overrode the current
  `generateETagHeaderValue(InputStream, boolean isWeak)` instead, keeping the SHA-512 ETag and
  adding the `W/` prefix when a weak ETag is requested (same contract as the Spring default).

## Verification
- `mvn clean verify` → BUILD SUCCESS on Corretto 25.0.5.

## Next steps
- `src/test/groovy/.../DownloadControllerTest.groovy` (Spock) is not compiled or executed: the POM has no
  Groovy compiler plugin (this was already true before the upgrade). Spock `1.0-groovy-2.4` / Groovy 2.4
  cannot run on Java 25; to re-enable, move to Spock 2.4 (`groovy-4.0`), `org.apache.groovy:groovy`, and add
  `gmavenplus-plugin`.
- Unused leftovers to consider removing: `spring.version` property (4.1.1.RELEASE, not referenced),
  `cglib-nodep` 3.1 (Spring bundles its own repackaged cglib), and the `http://repo.spring.io/milestone/`
  repository (plain HTTP is blocked by Maven 3.8+).
