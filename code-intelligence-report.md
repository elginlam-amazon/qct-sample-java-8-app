# Code Intelligence Report

## Build System
- Maven only (`/data/code/pom.xml`), single module. No Gradle files.
- Maven wrapper present (`mvnw`).

## Spring Boot Version
- 3.5.16 (parent `spring-boot-starter-parent`)

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25
- Environment JDK: Corretto 25.0.5

## Size Metrics
- modules: 1
- estimated_loc: 655 (java + groovy)
- source files: 8 main java, 2 test java, 1 test groovy (Spock)

## Jackson Usage
- none detected (no `com.fasterxml.jackson.*` imports or coordinates)
- custom serializers: 0

## Spring Security
- not present; security filter chains: 0

## Testing Infrastructure
- Spock 2.5-groovy-5.0 (`spock-core`, `spock-spring`), gmavenplus-plugin 5.1.0
- spring-boot-starter-test, spring-test
- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none
- 1 Spock spec with 12 feature methods

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry coordinates

## Reactive / WebFlux
- not present

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin
  org.springframework:spring-test
  org.spockframework:spock-spring
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.spockframework:spock-core
  com.google.guava:guava
  cglib:cglib-nodep
  org.springframework:spring-beans
  org.springframework:spring-context
  org.springframework:spring-context-support
  org.springframework:spring-webmvc
  org.springframework:spring-web
  org.springframework:spring-aop
  org.springframework:spring-core
  org.springframework:spring-expression
  org.apache.groovy:groovy-all

## Properties
- No `application*.properties` / `application*.yml` (only `logback.xml`)
