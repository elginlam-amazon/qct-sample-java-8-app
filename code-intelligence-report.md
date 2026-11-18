# Code Intelligence Report

## Build System
- Maven (single module, `pom.xml`); no Gradle build files. Maven wrapper present (`mvnw`).

## Spring Boot Version
- 3.5.16 (spring-boot-starter-parent)

## Java Version
- java.version=25 (build JDK: Corretto 25.0.5.1)

## Size Metrics
- modules: 1
- main Java files: 8, test Java files: 3, test Groovy (Spock) files: 2
- estimated_loc: ~600

## Jackson Usage
- none (no com.fasterxml.jackson imports, no custom serializers)

## Spring Security
- none

## Testing Infrastructure
- Spock 2.5-groovy-5.0 (spock-spring) with @SpringBootTest + MockMvcBuilders.webAppContextSetup
- @MockBean: 0, @SpyBean: 0, Testcontainers: none

## Observability
- spring-boot-starter-actuator (no micrometer/otel registries, no actuator package imports)

## Reactive / WebFlux
- none

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
