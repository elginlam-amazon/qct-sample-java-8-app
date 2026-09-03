# Java 25 Upgrade – Build Fix Summary

## Status
**BUILD: PASSING** — `mvn clean test` exits with `BUILD SUCCESS`.

## What Was Done

### 1. Removed duplicate `spring-test` dependency
The pom.xml had two identical `org.springframework:spring-test:4.1.6.RELEASE` declarations. The duplicate was removed to eliminate the Maven model warning.

### 2. Removed broken `byte-buddy-agent` argLine
OpenRewrite added a `maven-dependency-plugin` and a surefire `argLine` that referenced `${net.bytebuddy:byte-buddy-agent:jar}`, but `net.bytebuddy:byte-buddy-agent` was never declared as a dependency. The property would always be empty, leaving `-javaagent:` with no path — an invalid JVM flag that would crash surefire once any test executed. Both the `maven-dependency-plugin` configuration and the invalid argLine were removed. The `<argLine></argLine>` property (used by JaCoCo late-binding) was retained.

### 3. Added GMavenPlus plugin and upgraded Groovy/Spock for Groovy test compilation
The Groovy test `DownloadControllerSpec.groovy` was never compiled (no Groovy compiler was configured). This has been fixed:

| Component | Before | After |
|---|---|---|
| Groovy | `org.codehaus.groovy:groovy-all:2.4.3` (compile) | `org.apache.groovy:groovy:4.0.32` (compile) |
| spock-core | `1.0-groovy-2.4` | `2.3-groovy-4.0` |
| spock-spring | `1.0-groovy-2.4` | `2.3-groovy-4.0` |
| Groovy compiler | none | `gmavenplus-plugin:3.0.2` |

**Groovy target bytecode is Java 21**, not Java 25. Groovy 4.x's bundled ASM (9.x) cannot emit or parse Java 25 class files (major version 69). Compiling to Java 21 bytecode is fully valid — the compiled classes run without issue on the Java 25 JVM.

### 4. Configured Java test classes to compile to Java 21
Because Groovy's stub generator reads the compiled Java test helper classes (`FileExamples`, `FileStorageStub`, `Sha512ShallowEtagHeaderFilter`), those classes must also be Java ≤21 bytecode. The `default-testCompile` execution of `maven-compiler-plugin` is overridden with `<release>21</release>`. **Main source code continues to compile to Java 25** (`<release>25</release>` in `default-compile`).

## Compilation Output

```
8 source files compiled with javac [debug release 25]  → target/classes   (Java 25)
4 source files compiled with javac [debug release 21]  → target/test-classes (Java 21)
1 Groovy file compiled via GMavenPlus/Groovy 4.0.32    → target/test-classes (Java 21)
```

## Known Issues / Next Steps

### Spock tests compile but do not execute
`DownloadControllerSpec.groovy` compiles successfully, but it is **not run by surefire**. Two blockers prevent execution:

1. **Spring 4.x / Spock 2.x incompatibility** — `spock-spring 2.x` requires Spring Framework 5.2+ because it uses `SpringExtension` (JUnit 5 Jupiter), which does not exist in Spring 4.x. Attempting to run the spec would throw `ClassNotFoundException: org.springframework.test.context.junit.jupiter.SpringExtension` at runtime.

2. **Surefire 2.17 does not discover JUnit Platform tests** — Spock 2.x specs are JUnit Platform (Jupiter) tests. The inherited surefire 2.17 only knows about JUnit 4 and will not detect them, so no tests are reported and the build passes.

To make the Spock tests actually execute, a subsequent iteration should:
- Upgrade Spring Boot to 2.7.x (stays on `javax.*`) or 3.x (migrates to `jakarta.*`), which brings Spring Framework 5.x / 6.x
- Upgrade `maven-surefire-plugin` to 3.x and add `org.junit.platform:junit-platform-launcher` to enable JUnit Platform test discovery
- Verify that `Sha512ShallowEtagHeaderFilter.generateETagHeaderValue(byte[] bytes)` is still present in the upgraded Spring version (the signature changed in Spring 5.x)

### Deprecated API in FileSystemPointer.java
`com.google.common.io.Files.hash(File, HashFunction)` and `com.google.common.hash.Hashing.sha512()` are deprecated in Guava 29+. The compiler emits a deprecation warning. These should be replaced with `java.nio.file.Files.readAllBytes(path)` + `MessageDigest` or updated Guava equivalents in a follow-up.

### Spring Boot 1.2.3 runtime compatibility with Java 25
Spring Boot 1.2.3 (Spring Framework 4.1.x, embedded Tomcat 8.0.x, ASM 5.x, cglib 3.1) predates Java 9. While the application **compiles** against Java 25, it is **unlikely to start** at runtime: cglib 3.1's bundled ASM 5.x cannot proxy beans whose class files target Java 25 (major version 69). This is a fundamental framework-level limitation — resolving it requires upgrading Spring Boot (see above).
