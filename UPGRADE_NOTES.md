# Java 21 Upgrade Notes

The project was upgraded to Java 21 (`maven.compiler.release=21`) on Spring Boot 3.2.12.

## Deviations from the requested dependency targets

- **Jackson 2.15.4 instead of 2.15.2** — Jackson is not declared directly; it is managed by the
  `spring-boot-starter-parent:3.2.12` BOM, which resolves `jackson-databind` to 2.15.4. Pinning
  2.15.2 would be a downgrade relative to the BOM and would diverge from the versions Spring Boot
  3.2.x is tested against, so the BOM-managed version is kept.
- **Guava 33.4.8-jre** — the next version that fixes the reported advisory (33.7.2) is not
  available in the repositories configured for this build, so the highest resolvable version was
  used.

## Updates deliberately skipped

- **`cglib:cglib-nodep:3.1`** — left at its original version. Spring Framework 6 repackages CGLIB
  inside `spring-core`, so this explicit dependency is redundant; it is kept untouched here to
  avoid a behavioural change outside the scope of the version upgrade, and should be removed in a
  dedicated follow-up change.
