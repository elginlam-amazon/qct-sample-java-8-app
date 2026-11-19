# Java 17 upgrade summary

## Changes
- OpenRewrite (already applied): `maven-compiler-plugin` 3.16.0 with `<release>17</release>`, `commons-codec` 1.17.2, `new Integer(String)` replaced by `Integer.valueOf` in `MainApplication`.
- `pom.xml`:
  - Spring Boot parent 1.2.3.RELEASE -> 2.7.18 (last javax line). Spring Framework 4.1.x cannot read Java 17 class files (its ASM is too old), so the app would compile but fail at startup.
  - Spring Framework pinned to 5.3.39 through the Boot property `spring-framework.version`. The unused `spring.version` property was removed.
  - Hard-coded `4.1.6.RELEASE` / `1.2.3.RELEASE` versions removed from the `org.springframework*` dependencies, so the Boot parent manages them.
  - Duplicate `spring-test` declaration removed (Maven warned about it).
  - Standalone `cglib:cglib-nodep` 3.1 removed (Spring 5.3 bundles its own repackaged cglib/asm).
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 5 replaced `generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`. The override now uses the new signature and still produces a quoted SHA-512 ETag (prefixed with `W/` only if weak ETags are enabled; the default is off, so the output is unchanged).

## Result
- `mvn clean test` reports BUILD SUCCESS on JDK 17. The effective Spring Framework version is 5.3.39, the namespace is still javax, and Tomcat is 9.0.83.

## Next steps
- `src/test/groovy/.../DownloadControllerTest.groovy` (Spock spec `DownloadControllerSpec`) has never been compiled or run, before or after this upgrade: the POM has no Groovy compiler plugin (e.g. gmavenplus). To run it on Java 17, add gmavenplus and move to Spock 2.x (`spock-core`/`spock-spring` 2.3-groovy-3.0) with Groovy 3.0.x. Spock 1.0 / Groovy 2.4.3 do not support Java 17.
- `org.codehaus.groovy:groovy-all` 2.4.3 is a compile-scope dependency that main code does not use. Consider making it test scope or upgrading it together with Spock.
- Guava 18.0 and commons-io 2.4 still work but are old. Upgrade them if required (no code changes are expected for commons-io; Guava `Optional.transform/or` in `ExistingFile` is still present in current Guava).
- The milestone repository URL uses `http://`, which Maven 3.8+ blocks. It is not needed for the current dependencies.
