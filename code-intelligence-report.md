# Code Intelligence Report

## Build System
- Maven only (`pom.xml` at root, single module). No Gradle files.
- Wrapper: `mvnw` present.

## Spring Boot Version
- 3.5.16 (parent `spring-boot-starter-parent`)

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25
- Build JDK available: Corretto 25.0.5

## Size Metrics
- modules: 1
- java/groovy source files: 11
- estimated_loc: ~800

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent (parent, 3.5.16)
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.apache.groovy:groovy
  org.spockframework:spock-core
  org.spockframework:spock-spring
  com.google.guava:guava
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-dependency-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson.*` imports or coordinates)
- jackson_custom_serializers: 0

## Spring Security
- none detected; security_filter_chain_count: 0

## Testing Infrastructure
- Spock 2.5 (`DownloadControllerTest.groovy`, class `DownloadControllerSpec`, 12 feature methods)
- JUnit via spring-boot-starter-test; no `@MockBean` / `@SpyBean`
- No Testcontainers

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry coordinates, no actuator imports in source

## Reactive / WebFlux
- none

## Properties
- No `application*.properties` / `application*.yml` files. Only `src/main/resources/logback.xml`.

## Other signals
- Spring AI / Batch / Kafka / AMQP / Cloud / Native-AOT / Jersey / Retry / gRPC / SpringDoc / SBA / jOOQ / Undertow / Jetty: none detected
- JSpecify relevant: no (no Kotlin, no nullability annotations, SpotBugs/Checkstyle only skipped via CLI flags)
