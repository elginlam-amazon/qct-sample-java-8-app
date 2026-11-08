# Java 21 Upgrade — Deferred / Skipped Updates

This note records updates that were intentionally **not** applied during the
Java 8 -> Java 21 / Spring Boot 1.2.3 -> 3.2.12 upgrade, and why.

## Spring Boot held at 3.2.12
The upgrade target mandated Spring Boot `3.2.12`. That parent pins:

- `tomcat-embed-core 10.1.33` — known CVEs (e.g. CVE-2025-24813, CVE-2026-43515;
  fixed in 10.1.35 / 10.1.55).
- `logback-core 1.4.14` — known CVEs (CVE-2024-12798, CVE-2025-11226;
  fixed in 1.5.13 / 1.5.19).

Overriding `tomcat.version` / `logback.version` above the parent's defaults, or
moving to a newer Spring Boot line, deviates from the mandated version and needs
an explicit owner decision. Not done here.

## Groovy pinned at 4.0.24
`org.apache.groovy:groovy-all` is pinned to `4.0.24` while
`spock-core 2.4-M4-groovy-4.0` resolves a slightly newer 4.0.x patch
transitively. The pin is compatible and the test suite passes; aligning the two
was deferred to avoid an unnecessary version bump in this upgrade.

## Mockito not configured as a Java agent
Mockito's self-attach emits a warning on modern JDKs. Adding `mockito-core` as a
`-javaagent` in the Surefire `<argLine>` was deferred; it is not required for the
build to pass on JDK 21.

## Runtime smoke-start validation not executed
Only `mvn clean verify` (compile + 12/12 tests) was run. Booting the app and
asserting `GET /actuator/health` returns 200 was not performed in this
environment. Note for operators: the Spring Boot 3 actuator path is
`/actuator/health` (previously `/health`), so load-balancer rules, Kubernetes
liveness/readiness probes, and dashboards must be updated.
