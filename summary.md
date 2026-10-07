# Java 25 upgrade summary

Target: Java 25, Spring Boot 4.0.8 (Spring Framework 7.0.9, Jakarta namespace), applied by OpenRewrite.

## Fix applied in this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 5+ replaced
  `generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`. The override now reads
  the stream, computes the same SHA-512 hash with Guava and prefixes `W/` for weak ETags, matching the base class contract.

## Verification
- `mvn clean test` -> BUILD SUCCESS, zero compilation errors (main and test sources).

## Next steps
- Namespace: the adopted BOM is `spring-boot-starter-parent` 4.0.8, which is on the jakarta line. No application source
  imports `javax.*` or `jakarta.*`; the only jakarta artifact is the BOM-managed, test-scoped `jakarta.servlet-api`.
  A javax-only stack would need a downgrade to Spring Boot 2.7.x / Spring Framework 5.3.x. That downgrade was not done,
  because it would change the BOM that is already in place. Please confirm which target line you want.
- The Spock spec `src/test/groovy/.../DownloadControllerTest.groovy` is not compiled or run: the POM has no Groovy
  compiler plugin (this was already true before the upgrade). Enabling it needs `gmavenplus-plugin` plus Spock 2.x
  (`spock-core`/`spock-spring` 2.4-groovy-4.0) and Groovy 4 (`org.apache.groovy`), because Spock 1.0 / Groovy 2.4 do not run on Java 25.
- `groovy-all` 2.4.16 is a compile-scope dependency but no main code uses it; consider making it test-scope or removing it.
- `cglib-nodep` 3.1 is unused by the source and is not compatible with modern JDKs; consider removing it.
- The `spring.version` property (4.1.1.RELEASE) is unused, and the `http://repo.spring.io/milestone/` repository is plain HTTP, which Maven 3.8+ blocks. Both can be removed.
- `commons-io` 2.4 and `guava` 29.0-jre are old; consider upgrading them.
