# Code Intelligence Report

Generated with the minimal grep-based fallback (no pipeline pre-step report was present).

## Build System

- Maven only (`pom.xml`), Maven wrapper present (`mvnw`). No `build.gradle*`.

## Spring Boot Version

- 3.5.16 (`spring-boot-starter-parent`)

## Java Version

- `java.version` = 25 (build JDK: Corretto 25.0.5.1)

## Size Metrics

- Modules: 1
- Estimated LOC (Java + Groovy under src/): 655

## Jackson Usage

- No direct Jackson imports in sources. Version pin `jackson-bom.version=2.21.8` in pom properties.
- Custom serializers: 0

## Spring Security

- Not present. SecurityFilterChain count: 0

## Testing Infrastructure

- Spock 2.5 (`spock-core`, `spock-spring`) specs via gmavenplus: 1 spec file (12 feature methods)
- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none

## Observability

- `spring-boot-starter-actuator` present. No micrometer/opentelemetry/zipkin dependencies, no OTLP properties.

## Reactive / WebFlux

- Not present (Spring MVC only).

## Build Dependencies

  org.springframework.boot:spring-boot-starter-parent
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-dependency-plugin
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
  org.springframework:spring-beans
  org.springframework:spring-context
  org.springframework:spring-context-support
  org.springframework:spring-webmvc
  org.springframework:spring-web
  org.springframework:spring-aop
  org.springframework:spring-core
  org.springframework:spring-expression
  org.apache.groovy:groovy
