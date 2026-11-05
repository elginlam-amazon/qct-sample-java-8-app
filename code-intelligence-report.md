# Code Intelligence Report

## Build System
- Maven only (`pom.xml` at root, no `build.gradle*`)
- Single module: `com.nurkiewicz.download:download-server:0.0.1-SNAPSHOT`
- Maven wrapper present (`mvnw`), Maven 3.9.16, build JDK 25 (Corretto 25.0.5)

## Spring Boot Version
- 3.5.16 (spring-boot-starter-parent)

## Java/Kotlin Version
- `maven.compiler.release` = 25
- Groovy 5.0.8 test sources (Spock 2.5-groovy-5.0), no Kotlin

## Size Metrics
- modules: 1
- estimated_loc: 656 (8 main Java files, 3 test Java files, 1 Groovy spec)

## Build Dependencies
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-parent
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

## Jackson Usage
- none detected (no `com.fasterxml.jackson` imports, no custom serializers)

## Spring Security
- not present (0 filter chains)

## Testing Infrastructure
- Spock specification (`DownloadControllerTest.groovy`, 12 feature methods), MockMvc via `MockMvcBuilders.webAppContextSetup`
- no `@MockBean` / `@SpyBean`, no Testcontainers

## Observability
- `spring-boot-starter-actuator` present; no micrometer/opentelemetry artifacts declared

## Reactive / WebFlux
- none

## Property Files
- none (`application*.properties|yml` absent); `src/main/resources/logback.xml` only
