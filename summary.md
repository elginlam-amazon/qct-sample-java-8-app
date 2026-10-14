# Java 25 upgrade summary

The OpenRewrite pass did not change `pom.xml`: it was still on Java 1.8 and Spring Boot 1.2.3 / Spring 4.1. I did the upgrade by hand.

## Changes
- `pom.xml`
  - Spring Boot parent 1.2.3 -> 3.5.16 (Spring Framework 6.2.19, Jakarta). Java 25 has no javax option because Spring 5.3 cannot read class files newer than Java 17.
  - `maven-compiler-plugin`: `<release>25</release>`, with the version managed by Boot (it was 3.0). Boot's `-parameters` flag still applies, so `@PathVariable` works without explicit names.
  - The Spring Boot starters and Spring Framework artifacts no longer have hard-coded versions. The Boot BOM manages them, so they are all consistent.
  - Spock 1.0-groovy-2.4 -> 2.4-groovy-4.0. `org.codehaus.groovy:groovy-all:2.4.3` -> `org.apache.groovy:groovy` (Boot-managed 4.0.32).
  - Guava 18.0 -> 33.4.0-jre, commons-io 2.4 -> 2.18.0, commons-codec now Boot-managed (1.18.0).
  - Removed `cglib-nodep` (unused; Spring 6 repackages its own CGLIB).
  - Added `gmavenplus-plugin` 4.3.1 so the Spock spec under `src/test/groovy` is compiled and run. The old build never compiled it.
  - Surefire loads Mockito as `-javaagent`, which JDK 21+ requires for inline mocking. Surefire now also includes `*Spec` classes.
  - Spring milestone repository URL: http -> https.
- `MainApplication`: `new Integer(String)` (deprecated for removal) -> `Integer.valueOf`.
- `logback.xml`: the appender is now named and referenced with `appender-ref`. Logback 1.5 rejects the old unnamed inline appender, which meant no log output at all.
- `Sha512ShallowEtagHeaderFilter` (test): moved to the Spring 5+ hook `generateETagHeaderValue(InputStream, boolean)`.
- `DownloadControllerTest.groovy`: `TEXT_FILE` is no longer `final`. Groovy 4 constant-folded it at compile time, which baked one random UUID into the class file, so every request returned 404.

## Verification
`mvn clean verify -Dexec.skip=true` passes on Corretto 25.0.4: it compiles with zero errors and all 12 Spock tests pass. There are no javax, Spring 4, or cglib artifacts left in the resolved dependency list.

## Next steps
- The `exec-maven-plugin` VUTK-274 OOM fixture (`head -c 6G /dev/zero | tail` in `validate`) is unchanged. I skipped it locally with `-Dexec.skip=true`. Without that flag, every lifecycle build runs it on purpose.
- The build environment's root filesystem (`/`, which holds `/tmp` and `~/.m2`) was 100% full. I built with `-Dmaven.repo.local=/data/m2repo` and `-Djava.io.tmpdir` set to a directory on `/data`. If tmpdir is on a full disk, Mockito/Byte Buddy fail with "could not self-attach" (No space left on device).
- Guava `com.google.common.base.Optional` is still used in `ExistingFile.toMediaType`. It works on Guava 33, but you could move it to `java.util.Optional` later.
- Spring Boot 3.5 open-source support has ended or is ending soon. Plan a Boot 4.0 / Spring 7 upgrade (Spock `groovy-5.0` variant).
