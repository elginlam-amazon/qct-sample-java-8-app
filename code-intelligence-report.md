# Code Intelligence Report

Generated via LAST RESORT minimal path (grep-based; no generator available).

## Build System
- Maven (pom.xml), single module. Maven wrapper present (mvnw). No Gradle.

## Spring Boot Version
- 3.5.16 (spring-boot-starter-parent)

## Java Version
- 25 (`java.version`, `maven.compiler.release`)

## Size Metrics
- modules: 1
- main Java files: 8
- test files: 3 Java + 1 Groovy (Spock spec, 12 feature methods)
- estimated_loc: ~600

## Jackson Usage
- none (no com.fasterxml.jackson imports)

## Spring Security
- none

## Testing Infrastructure
- Spock 2.5 (spock-spring), MockMvc via MockMvcBuilders.webAppContextSetup
- @MockBean: 0, @SpyBean: 0, Testcontainers: none

## Observability
- spring-boot-starter-actuator (no actuator package imports in source)

## Reactive / WebFlux
- none

## Build Dependencies
  org.springframework.boot:spring-boot-starter-parent
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
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
