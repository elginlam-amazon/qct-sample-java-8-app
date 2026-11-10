# Code Intelligence Report

## Build System
- Maven only (`pom.xml` at root, single module). No Gradle files.
- Wrapper: `mvnw` present; Maven 3.9.16 on host.

## Spring Boot Version
- 3.5.16 (spring-boot-starter-parent)

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25; build JDK: Corretto 25.0.5

## Size Metrics
- modules: 1
- main java files: 8, test files: 3 (1 Groovy Spock spec with 12 feature methods)
- estimated_loc: ~900

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.spockframework:spock-core
  org.spockframework:spock-spring
  com.google.guava:guava
  cglib:cglib-nodep
  org.springframework:spring-test
  org.apache.groovy:groovy
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson.*` imports, no custom serializers)

## Spring Security
- none detected (no security dependency, 0 filter chains)

## Testing Infrastructure
- Spock 2.5 + spring-boot-starter-test; `@MockBean`/`@SpyBean`: 0; Testcontainers: none
- Custom test-source filter `org.springframework.web.filter.Sha512ShallowEtagHeaderFilter`

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry/tracing dependencies

## Reactive / WebFlux
- none

## Other signals
- Spring AI / Batch / Kafka / AMQP / Cloud / Native-AOT / Jersey / Retry / gRPC / SpringDoc / jOOQ: none
- Undertow / Jetty: none (default Tomcat)
