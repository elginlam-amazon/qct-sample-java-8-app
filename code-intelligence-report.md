# Code Intelligence Report

## Build System
- Maven only (`./pom.xml`), single module. Wrapper: `mvnw` present.
- Spring Boot version (parent): 3.5.16

## Size Metrics
- Modules: 1
- Java files: 11 (main 8, test 3), Groovy specs: 1
- Estimated LOC: ~663

## Java / Kotlin Version
- `java.version` = 25, `maven.compiler.release` = 25
- Build JDK available: Corretto 25.0.5
- No Kotlin sources

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent (parent, 3.5.16)
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.springframework:spring-test
  org.spockframework:spock-core
  org.spockframework:spock-spring
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
  org.apache.groovy:groovy
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-dependency-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson` imports or coordinates)
- custom serializers: 0

## Spring Security
- not present; SecurityFilterChain beans: 0

## Testing Infrastructure
- Spock 2.5-groovy-5.0 (1 spec, 12 feature methods), JUnit via spring-boot-starter-test
- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none
- MockMvc via `MockMvcBuilders.webAppContextSetup`

## Observability
- `spring-boot-starter-actuator` present; no micrometer/otel coordinates, no actuator autoconfigure imports

## Reactive / WebFlux
- none

## Properties
- No `application*.properties` / `application*.yml` files; `src/main/resources/logback.xml` only
