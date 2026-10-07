# Java 25 upgrade – build fix summary

## Changes (on top of the OpenRewrite output)
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring 7 changed
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` to `generateETagHeaderValue(InputStream, boolean isWeak)`.
  Ported the override; it still produces a quoted SHA-512 ETag and now honours the weak-ETag prefix (`W/`).
- `pom.xml`: removed the unused, stale `spring.version=4.1.1.RELEASE` property. Spring versions are managed
  by `spring-boot-starter-parent` 4.0.8 (Spring Framework 7.0.9 across all artifacts).

## Result
`mvn clean verify` on JDK 25 (Corretto 25.0.4): BUILD SUCCESS, zero compilation errors.

## Next steps
- The Spock tests in `src/test/groovy` are not compiled or run: the POM has no Groovy compiler plugin
  (this was already true before the upgrade). Spock `1.0-groovy-2.4` / Groovy 2.4.16 do not support Java 25
  or JUnit Platform. To bring them back, add `gmavenplus-plugin` and move to Spock 2.4 (`spock-spring`, groovy-4.0 variant)
  with `org.apache.groovy` 4.x, then drop the `org.codehaus.groovy:groovy-all` dependency (it is currently a compile-scope dependency
  that the main code does not use).
- `cglib:cglib-nodep:3.1` is unused by the source code and does not work on modern JDKs (Spring has its own repackaged cglib). Consider removing it.
- `guava` 29.0-jre and `commons-io` 2.4 are old but work; consider updating them.
- The `repo.spring.io/milestone` repository uses `http://`, which Maven 3.8+ blocks. Switch it to `https` or remove it, since no milestone artifacts are needed.
