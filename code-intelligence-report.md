# Code Intelligence Report

## Build System
- Maven only (`pom.xml` at project root, single module, no `build.gradle*`)
- Maven wrapper present (`mvnw`)

## Spring Boot Version
- 3.5.16 (spring-boot-starter-parent)

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25
- Environment JDK: Corretto 25.0.5

## Size Metrics
- modules: 1
- java/groovy source files: 12
- estimated_loc: ~800

## Jackson Usage
- No `com.fasterxml.jackson.*` imports; no custom serializers/deserializers

## Spring Security
- Not present (no security starter, 0 SecurityFilterChain beans)

## Testing Infrastructure
- Spock 2.5-groovy-5.0 (`DownloadControllerTest.groovy`, 12 feature methods), spring-boot-starter-test, spring-test
- 0 `@MockBean`, 0 `@SpyBean`, no Testcontainers

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry direct deps

## Reactive / WebFlux
- Not present

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
  org.springframework:spring-beans
  org.springframework:spring-context
  org.springframework:spring-context-support
  org.springframework:spring-webmvc
  org.springframework:spring-web
  org.springframework:spring-aop
  org.springframework:spring-core
  org.springframework:spring-expression
  org.springframework:spring-test
  org.apache.groovy:groovy
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin
