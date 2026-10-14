# Upgrade summary: Java 25 / Spring Boot 4.0

## Result
`mvn clean verify` builds successfully on JDK 25 (Corretto 25.0.4) with zero compilation errors.

## Changes made in this cycle
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 5.0 removed
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])`. Spring 7.0.9 has
  `generateETagHeaderValue(InputStream, boolean isWeak)` instead. The override now uses that signature,
  still hashes the body with SHA-512 (Guava), and adds the `W/` prefix when Spring asks for a weak ETag.

The OpenRewrite changes already in place (Boot parent 4.0.8, `spring-boot-starter-webmvc`, `<release>25</release>`,
`Integer.valueOf`, dropped `@Autowired` on the single constructor) were left as they were.

## Next steps
- No tests run. `DownloadControllerTest.groovy` (Spock) was never compiled, not even before the upgrade, because the
  POM has no Groovy compiler plugin (e.g. gmavenplus). Spock `1.0-groovy-2.4` / Groovy 2.4 also does not work on
  JDK 25 or Spring 7. To bring the spec back: add gmavenplus-plugin, move to Spock 2.x (`spock-spring`, Groovy 4,
  JUnit Platform), and check that the `test` profile wiring (`FileStorageStub`) still works with Boot 4.
- `pom.xml` still has items that nothing uses or that are out of date: the `spring.version` property (4.1.1.RELEASE),
  the standalone `cglib:cglib-nodep:3.1` (Spring ships its own repackaged CGLIB), `commons-io 2.4`, `guava 29.0-jre`,
  and the `http://repo.spring.io/milestone` repository (Maven 3.8+ blocks plain HTTP). These were left alone to keep
  the change small. Remove or upgrade them in a later cycle.
