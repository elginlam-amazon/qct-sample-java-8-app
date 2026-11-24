# Java 25 upgrade summary

## Result
`mvn clean verify` succeeds on JDK 25 (Corretto 25.0.5.1) with Spring Boot 4.0.10 / Spring Framework 7.0.11. Main and test Java sources compile with `--release 25`. No Java EE `javax.*` imports or artifacts remain.

## Changes in this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 6+ replaced `generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`. The override now uses the new signature, still hashes the body with SHA-512, and adds the `W/` prefix when the filter is configured for weak ETags.
- `pom.xml`: removed the unused, stale `spring.version` property (4.1.1.RELEASE). Spring versions come from the Boot parent BOM.

## Next steps (non-blocking)
- **Groovy/Spock tests never run.** `DownloadControllerTest.groovy` is not compiled because the POM has no Groovy compiler plugin. This was already the case before the upgrade. Surefire reports no tests. To run it, add `gmavenplus-plugin` and move to Spock 2.4 (`spock-core`/`spock-spring` `2.4-groovy-4.0` or newer) with `org.apache.groovy:groovy` (managed by Boot). Groovy 2.4 / Spock 1.0 cannot run on JDK 25.
- **`groovy-all` 2.4.16 has compile scope**, so it ships in the application even though no main code uses Groovy. Consider `test` scope or removing it as part of the Spock migration.
- **Old, non-BOM-managed libraries** that still compile: Guava 29.0-jre, commons-io 2.4, cglib-nodep 3.1. Consider upgrading them (Guava 33.x, commons-io 2.x latest) because of known CVEs. cglib-nodep looks unused, since Spring uses its own repackaged CGLIB.
- **The `repo.spring.io/milestone` repository uses `http://`**, which Maven 3.8+ blocks. The build does not need it, so it can be removed.
- `MainApplication.main` has an unused local `temp` variable from the original code. It was left unchanged.
