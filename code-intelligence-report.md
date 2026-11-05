# Code Intelligence Report

Project: download-server (`com.nurkiewicz.download:download-server:0.0.1-SNAPSHOT`)
Root: /data/code

## Build System

- Maven only (`pom.xml` at root, no `build.gradle*`)
- Modules: 1 (single module, no `<modules>`)
- Build command: `mvn clean verify`
- Plugins: maven-compiler-plugin 3.13.0 (`release=25`), gmavenplus-plugin 5.1.0 (`targetBytecode=25`, goal `compileTests`), maven-surefire-plugin (BOM-managed; includes `**/*Spec.java`, `**/*Test.java`), spring-boot-maven-plugin
- No test-skip flags present (`skipTests` / `<skip>true</skip>` / `maven.test.skip` / `testFailureIgnore`: none)

## Spring Boot Version

- Detected: 3.5.6 (`spring-boot-starter-parent`)

## Java / Kotlin Version

- `java.version` = 25, `maven.compiler.release` = 25, groovy `targetBytecode` = 25
- Environment JDK: Corretto 25.0.5 (LTS)
- No Kotlin sources, no Kotlin plugin

## Size Metrics

- Modules: 1
- Java + Groovy LOC: 657
- Main source files: 8 Java (`com.nurkiewicz.download`)
- Test source files: 3 Java + 1 Groovy (Spock)

## Build Dependencies

  org.springframework.boot:spring-boot-starter-parent (parent, 3.5.6)
  commons-io:commons-io (2.18.0)
  commons-codec:commons-codec (1.17.1)
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test (test)
  com.google.guava:guava (33.7.2-jre)
  org.apache.groovy:groovy (5.0.8, test)
  org.spockframework:spock-core (2.5-groovy-5.0, test)
  org.spockframework:spock-spring (2.5-groovy-5.0, test)
  org.apache.maven.plugins:maven-compiler-plugin (3.13.0)
  org.codehaus.gmavenplus:gmavenplus-plugin (5.1.0)
  org.apache.maven.plugins:maven-surefire-plugin
  org.springframework.boot:spring-boot-maven-plugin

## Jackson Usage

- none detected (no `com.fasterxml.jackson.*` / `tools.jackson.*` imports, no custom serializers/deserializers, no `ObjectMapper` usage)

## Spring Security

- none detected (no security starter, 0 `SecurityFilterChain` beans, no `WebSecurityConfigurerAdapter`)

## Testing Infrastructure

- `@MockBean`: 0, `@SpyBean`: 0, Testcontainers: none
- JUnit via `spring-boot-starter-test`; Spock 2.5-groovy-5.0 + spock-spring
- `src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy` — Spock spec, 12 feature methods
- `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` — subclass of Spring's `ShallowEtagHeaderFilter`
- Helpers: `FileExamples`, `FileStorageStub` (`@Component @Profile`)

## Observability

- `spring-boot-starter-actuator` present
- No micrometer / opentelemetry / zipkin coordinates
- No `org.springframework.boot.actuate.autoconfigure.*` or `org.springframework.boot.actuate.health` imports in source

## Reactive / WebFlux

- none detected (servlet stack only; no WebFlux/Reactor imports)

## Properties / Configuration

- No `application*.properties` / `application*.yml` anywhere
- `src/main/resources/logback.xml` only

## Other Signals

- `javax.*` imports: none; `jakarta.*` imports: none
- Spring AI, Batch, Kafka, AMQP, Cloud, Native/AOT, Jersey, Retry, JSpecify, Undertow/Jetty, SpringDoc/SBA, third-party compat frameworks, gRPC, jOOQ: all absent
- `spring.factories` / `META-INF/native-image`: absent
