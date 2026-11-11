# Code Intelligence Report

## Build System
- Maven only (`/data/code/pom.xml`), single module. No `build.gradle*`.
- Maven wrapper present (`mvnw`).
- Build command: `mvn clean verify`

## Spring Boot Version
- 3.5.16 (via `spring-boot-starter-parent`)

## Java Version
- `java.version` = 25, `maven.compiler.release` = 25
- Environment JDK: Amazon Corretto 25.0.5 (LTS)

## Size Metrics
- modules: 1
- estimated_loc: 657 (main + test Java/Groovy)
- main source files: 8 Java
- test source files: 2 Java + 1 Groovy (Spock)

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent (parent, 3.5.16)
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
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
  org.springframework:spring-test
  org.apache.groovy:groovy
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson.*` imports, no ObjectMapper usage, no custom serializers)
- jackson_custom_serializers: 0

## Spring Security
- not present. security_filter_chain_count: 0

## Testing Infrastructure
- Spock 2.5-groovy-5.0 (`DownloadControllerTest.groovy`, class `DownloadControllerSpec`, 12 `def` feature methods)
- `spring-boot-starter-test`, `spring-test`
- `@MockBean` usage: 0, `@SpyBean` usage: 0, Testcontainers: none
- MockMvc via `MockMvcBuilders.webAppContextSetup` with `@WebAppConfiguration` + `@SpringBootTest`

## Observability
- `spring-boot-starter-actuator` present
- no micrometer/opentelemetry explicit coordinates
- no `org.springframework.boot.actuate.autoconfigure.*` imports in source

## Reactive / WebFlux
- none

## Properties
- no `application*.properties` / `application*.yml` / `application*.yaml`
- only `src/main/resources/logback.xml`

## Other Module Signals
- Spring AI: no | Batch: no | Kafka: no | AMQP: no | Cloud: no | Native/AOT: no
- Jersey: no | Spring Retry: no | Undertow/Jetty: no | SpringDoc/SBA: no | gRPC: no | jOOQ: no
- JSpecify relevant: no (no Kotlin, no SpotBugs/NullAway/Checker/ErrorProne, no legacy nullability imports)
- Third-party compat signals: none
