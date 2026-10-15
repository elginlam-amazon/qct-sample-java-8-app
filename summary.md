# Java 25 upgrade – build fix summary

## Changes (this cycle)
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`: Spring Framework 7 (via Spring Boot 4.0.8) replaced
  `ShallowEtagHeaderFilter.generateETagHeaderValue(byte[])` with `generateETagHeaderValue(InputStream, boolean isWeak)`.
  Override updated to the new signature; still SHA-512 based, and now prefixes `W/` when a weak ETag is requested (matches the Spring default).

## Verification
- `mvn -Dexec.skip=true clean package` → BUILD SUCCESS on JDK 25 (Corretto 25.0.4), zero compilation errors (main + test).
- `-Dexec.skip=true` was used to skip the deliberate `vutk-274-oom-hog` exec-plugin fixture bound to `validate`
  (it intentionally OOM-kills the container). It was left untouched.

## Next steps
- Spock specs in `src/test/groovy` are not compiled or run: there is no Groovy compiler plugin (gmavenplus) in the POM, as in the original
  Java 8 build. Spock 1.0-groovy-2.4 / Groovy 2.4.16 do not support Java 25 or JUnit Platform; enabling them requires Spock 2.4 + Groovy 4.x
  (`org.apache.groovy`) and gmavenplus-plugin.
- `cglib:cglib-nodep:3.1` is not used by the code and is incompatible with modern JDKs; consider removing it.
- `<spring.version>4.1.1.RELEASE</spring.version>` property is unused (versions come from the Boot 4 BOM); can be removed.
- `http://repo.spring.io/milestone/` repository uses HTTP (blocked by Maven 3.8+) and is not needed for GA dependencies; consider removing.
- `guava` is pinned to 29.0-jre; consider aligning to a current release.
