# Code Intelligence Report

## Build System
- Maven only (`/data/code/pom.xml`), single module. No Gradle files.
- Maven wrapper present (`mvnw`).
- Build command: `mvn clean verify`

## Spring Boot Version
- `spring-boot-starter-parent` 3.5.16 (Boot 3.x)

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25
- Environment JDK: Corretto 25.0.5 (matches project target; no change needed)

## Size Metrics
- Modules: 1
- Main source files: 8 Java
- Test source files: 2 Java + 1 Groovy (Spock)
- Estimated LOC: ~900

## Build Dependencies
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-parent
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.springframework:spring-test
  org.springframework:spring-beans
  org.springframework:spring-context
  org.springframework:spring-context-support
  org.springframework:spring-webmvc
  org.springframework:spring-web
  org.springframework:spring-aop
  org.springframework:spring-core
  org.springframework:spring-expression
  com.google.guava:guava
  org.apache.groovy:groovy
  org.spockframework:spock-core
  org.spockframework:spock-spring
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson` imports, no `ObjectMapper` usage)

## Spring Security
- none detected (no security dependencies, 0 SecurityFilterChain beans)

## Testing Infrastructure
- Spock 2.5-groovy-5.0 + Groovy 5.1.3, gmavenplus 5.1.0
- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none
- MockMvc via `MockMvcBuilders.webAppContextSetup` in `@SpringBootTest`

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry/zipkin dependencies; no management.* properties

## Reactive / WebFlux
- none

## Other
- No `application*.properties`/`application*.yml` files (only `logback.xml`)
- No Spring Cloud / AI / Batch / Kafka / AMQP / Jersey / Retry / gRPC / native-AOT / SpringDoc / third-party compat signals
