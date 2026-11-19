# Code Intelligence Report (minimal, grep-based fallback)

## Build System
maven (pom.xml only; Maven wrapper present)

## Language Versions
Java 25 (`java.version`=25, maven-compiler-plugin `release` 25); Groovy 5.0.8 tests (Spock 2.5-groovy-5.0)

## Spring Boot Version
3.5.16 (spring-boot-starter-parent)

## Jackson Usage
none detected (no `com.fasterxml.jackson.*` imports, no custom serializers)

## Spring Security
none detected (security_filter_chain_count: 0)

## Testing Infrastructure
@MockBean: 0, @SpyBean: 0, Testcontainers: none. 1 Spock spec (12 feature methods), MockMvc via MockMvcBuilders.webAppContextSetup.

## Observability
spring-boot-starter-actuator (no micrometer/opentelemetry deps, no actuator package imports)

## Reactive / WebFlux
none detected

## Size Metrics
modules: 1
main source files: 8 (Java)
test source files: 4 (3 Java, 1 Groovy)
estimated_loc: 656

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

## Deprecated Boot 3.x APIs
- deprecated starter `spring-boot-starter-web` (rename to `spring-boot-starter-webmvc`): 1
- `javax.*` Jakarta-EE imports: 0
- Boot 3 actuator autoconfigure imports: 0
