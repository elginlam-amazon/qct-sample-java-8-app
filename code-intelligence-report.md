# Code Intelligence Report

## Build System
- Maven only (`pom.xml` at root, single module). No `build.gradle*`.
- Maven wrapper present (`mvnw`), Maven 3.9.16 on PATH.

## Spring Boot Version
- 3.5.16 (`spring-boot-starter-parent`)

## Java / Kotlin Version
- `java.version` = 25, `maven.compiler.release` = 25, compiler plugin `<release>25</release>`, gmavenplus `targetBytecode` 25
- Build JDK available: Corretto 25.0.5
- Groovy 5.0.8 + Spock 2.5-groovy-5.0 (test only)

## Size Metrics
- modules: 1
- estimated_loc: 659 (main + test Java/Groovy)
- source files: 9 main Java, 3 test Java, 1 Groovy spec

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
  org.apache.groovy:groovy-all
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin

## Jackson Usage
- none detected (no `com.fasterxml.jackson.*` imports, no custom serializers/deserializers)

## Spring Security
- none detected (no security dependency, 0 SecurityFilterChain beans)

## Testing Infrastructure
- Spock 2.5 (`DownloadControllerTest.groovy`, class `DownloadControllerSpec`, 12 `def` feature methods)
- `@SpringBootTest` + `@WebAppConfiguration` + MockMvcBuilders
- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry/registry artifacts declared

## Reactive / WebFlux
- none detected

## Properties
- no `application*.properties` / `application*.yml`; only `src/main/resources/logback.xml`

## Other Signals
- Spring AI / Batch / Kafka / AMQP / Cloud / Native-AOT / Jersey / Retry / gRPC / SpringDoc / jOOQ / Undertow-Jetty / third-party-compat: none detected
- Guava `com.google.common.*` and commons-io used in main sources
- Test-scope class in `org.springframework.web.filter` package (`Sha512ShallowEtagHeaderFilter` extends `ShallowEtagHeaderFilter`)
