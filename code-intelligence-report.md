# Code Intelligence Report

## Build System
- Maven only (single `pom.xml`, no Gradle build files)
- Spring Boot version (detected): 3.5.16 (spring-boot-starter-parent)

## Java / Kotlin Version
- `java.version` = 25, `maven.compiler.release` = 25
- Build JDK available: OpenJDK Corretto 25.0.5
- Groovy 5.0.8 test sources (Spock 2.5-groovy-5.0) via gmavenplus-plugin 5.1.0

## Size Metrics
- Modules: 1
- Estimated LOC: ~660 (8 main Java files, 2 test Java files, 1 Spock spec)

## Build Dependencies
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.spockframework:spock-core
  org.spockframework:spock-spring
  com.google.guava:guava
  org.apache.groovy:groovy
  org.springframework.boot:spring-boot-starter-parent (parent)
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson` imports or coordinates)
- custom serializers: 0

## Spring Security
- none detected; filter chains: 0

## Testing Infrastructure
- Spock (Groovy) + spring-boot-starter-test
- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry direct deps

## Reactive / WebFlux
- none detected

## Other signals
- Spring AI / Batch / Kafka / AMQP / Cloud / Jersey / Retry / gRPC / Native-AOT / SpringDoc / jOOQ: none
- Undertow / Jetty: none
- javax.* Jakarta-EE imports: none
