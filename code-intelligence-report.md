# Code Intelligence Report

## Build System
- Maven only (`/data/code/pom.xml`), single module. No Gradle files.
- Maven wrapper present (`mvnw`).
- Build JDK available: Corretto 25.0.5. Project `java.version`/`maven.compiler.release` = 25.

## Spring Boot Version
- Parent: `org.springframework.boot:spring-boot-starter-parent:3.5.16`

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25, gmavenplus `targetBytecode` = 25.

## Size Metrics
- Modules: 1
- Main Java files: 8
- Test files: 1 Groovy Spock spec, 2 Java test support classes, 1 test-package filter class
- Estimated LOC: ~700

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent (parent, 3.5.16)
  commons-io:commons-io:2.22.0
  commons-codec:commons-codec:1.23.0
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.spockframework:spock-core:2.5-groovy-5.0
  org.spockframework:spock-spring:2.5-groovy-5.0
  com.google.guava:guava:33.8.0-jre
  org.springframework:spring-beans
  org.springframework:spring-context
  org.springframework:spring-context-support
  org.springframework:spring-webmvc
  org.springframework:spring-web
  org.springframework:spring-aop
  org.springframework:spring-core
  org.springframework:spring-expression
  org.springframework:spring-test
  org.apache.groovy:groovy:5.0.8
  org.apache.maven.plugins:maven-compiler-plugin:3.13.0
  org.codehaus.gmavenplus:gmavenplus-plugin:5.1.0
  org.apache.maven.plugins:maven-surefire-plugin (managed)

## Jackson Usage
- No `com.fasterxml.jackson.*` imports in any source file. No custom serializers/deserializers (0).

## Spring Security
- Not present. SecurityFilterChain beans: 0.

## Testing Infrastructure
- Spock 2.5 (Groovy 5) + `spring-boot-starter-test`.
- `@MockBean` usage: 0. `@SpyBean` usage: 0. Testcontainers: absent.
- MockMvc via `MockMvcBuilders.webAppContextSetup`, `@SpringBootTest` + `@WebAppConfiguration`.

## Observability
- `spring-boot-starter-actuator` present. No micrometer/OpenTelemetry/tracing dependencies, no actuator autoconfigure imports in source.

## Reactive / WebFlux
- Not present (servlet stack only).

## Properties
- No `application*.properties` / `application*.yml` files. Only `src/main/resources/logback.xml`.

## Other signals
- Spring AI: absent. Batch: absent. Kafka: absent. AMQP: absent. Cloud: absent.
- Native/AOT: absent. Jersey: absent. Spring Retry: absent. gRPC: absent.
- SpringDoc / Spring Boot Admin: absent. jOOQ: absent. Undertow/Jetty: absent (default Tomcat).
- JSpecify-relevant: Groovy test sources present (Spock), no Kotlin, no nullability annotation tooling.
- Test-skip flags in build files: none.
