# Code Intelligence Report

Generated via the SKILL.md LAST RESORT grep-based fallback (tree-sitter unavailable).

## Build System

Maven only (`/data/code/pom.xml`, single module). No `build.gradle*`.
Spring Boot version (parent): 3.5.16

## Language Versions

Java: 25 (`java.version` = 25, `maven.compiler.release` = 25). Environment JDK: Corretto 25.0.5.
Groovy 4.0.29 (test scope, Spock 2.4-groovy-4.0).

## Jackson Usage

none detected (no `com.fasterxml.jackson.*` imports; 0 custom serializers/deserializers)

## Spring Security

none detected (no security starter, 0 `SecurityFilterChain` methods, no `@EnableWebSecurity`)

## Testing Infrastructure

- `@MockBean`: 0
- `@SpyBean`: 0
- Testcontainers: none detected
- Frameworks: Spock 2.4-groovy-4.0 (1 spec: `src/test/groovy/com/nurkiewicz/download/DownloadControllerTest.groovy`, 12 feature methods), MockMvc, `@SpringBootTest`, `@WebAppConfiguration`
- Test support classes: `FileExamples.java`, `FileStorageStub.java`, `org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`

## Observability

- `spring-boot-starter-actuator` present
- micrometer: none declared directly
- opentelemetry: none

## Reactive / WebFlux

none detected

## Size Metrics

- modules: 1
- java/groovy source files: 11
- estimated LOC: 656

## Build Dependencies

  org.springframework.boot:spring-boot-starter-parent
  org.apache.maven.plugins:maven-compiler-plugin
  org.codehaus.gmavenplus:gmavenplus-plugin
  org.apache.maven.plugins:maven-surefire-plugin
  commons-io:commons-io
  commons-codec:commons-codec
  org.springframework.boot:spring-boot-starter-web
  org.springframework.boot:spring-boot-starter-actuator
  org.springframework.boot:spring-boot-starter-test
  org.apache.groovy:groovy
  org.spockframework:spock-core
  org.spockframework:spock-spring
  com.google.guava:guava

## Deprecated Boot 3.x APIs

- `com.fasterxml.jackson.core|databind` imports: 0
- `javax.` Jakarta-EE imports: 0
- `org.springframework.boot.actuate.autoconfigure.*`: 0
- `WebSecurityConfigurerAdapter` / `antMatchers` / legacy security DSL: 0
- `@MockBean` / `@SpyBean`: 0
- `spring-boot-starter-web` (renamed to `-webmvc` in Boot 4): 1 occurrence (pom.xml)
