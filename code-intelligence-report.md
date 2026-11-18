# Code Intelligence Report

## Build System
- Maven (pom.xml, single module); Maven wrapper present (mvnw); no build.gradle*

## Spring Boot Version
- 3.5.16 (spring-boot-starter-parent)

## Java Version
- java.version=25 (build JDK: Corretto 25.0.5.1)

## Size Metrics
- modules: 1
- estimated_loc: 656 (Java + Groovy, src/)
- main sources: 8 Java files; test sources: 3 Java files, 1 Groovy Spock spec

## Jackson Usage
- No direct com.fasterxml.jackson imports; no custom serializers
- pom property jackson-bom.version=2.21.8 (Boot 3 CVE pin)

## Spring Security
- not present (0 SecurityFilterChain beans)

## Testing Infrastructure
- Spock 2.5-groovy-5.0 (spock-core, spock-spring), spring-boot-starter-test, spring-test
- @MockBean: 0, @SpyBean: 0, Testcontainers: none
- MockMvc built manually via MockMvcBuilders.webAppContextSetup

## Observability
- spring-boot-starter-actuator (no micrometer/otel customisation)

## Reactive / WebFlux
- not present

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
