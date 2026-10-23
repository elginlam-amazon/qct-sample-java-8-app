# 📊 Java 25 Upgrade – Iteration 22 Executive Report  
  
## 📝 Executive summary  
- 🎯 `download-server` was moved to Java 25 (`<java.version>25</java.version>`, compiler `<release>${java.version}</release>`). Its Spring Boot parent went from `1.2.3.RELEASE` to `4.0.9`.  
- 🔧 OpenRewrite ran two recipes, `com.amazonaws.java.migrate.UpgradeToJava25` and `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. Kiro CLI then fixed the one compilation error that remained, in a test-support class.  
- ✅ The post-upgrade verification build finished with **BUILD SUCCESS** (exit code 0).  
- 🧪 Test execution was not recorded. The verification log shows no Surefire, Failsafe or Gradle test totals.  
- 🌱 Runtime validation: "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)."  
- ⚠️ "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was never started, so its runtime behaviour is unverified.  
- 📌 After the Boot recipe, `code-upgrade.log` notes that "plain (non-Boot) Spring artifacts were also detected." In this build those artifacts have no explicit versions, so the Spring Boot 4.0.9 parent manages them.  
  
## 📦 Application changes  
- 📁 Five files changed (74 insertions, 32 deletions), committed as `ad5d0d1 Upgrade to Java 25 on Iteration 22`:  
  - 🛠️ `pom.xml`  
  - ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java`  
  - ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`  
  - 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  - 📄 `summary.md` (new file)  
- ➕ Dependencies and plugins added:  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, managed by the Boot parent). Default **compile** scope; it replaces `spring-boot-starter-web`.  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST, managed by the Boot parent). **test** scope.  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST, Boot-managed). Build plugin that runs the `properties` goal.  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST, Boot-managed). Build plugin that sets `argLine` to `@{argLine} -javaagent:${net.bytebuddy:byte-buddy-agent:jar}`.  
- ➖ Dependency removed:  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
- ⬆️ Dependencies upgraded:  
  - `org.springframework.boot:spring-boot-starter-parent`: 1.2.3.RELEASE -> 4.0.9  
  - `org.apache.maven.plugins:maven-compiler-plugin`: 3.0 -> 3.16.0  
  - `com.google.guava:guava`: 18.0 -> 29.0-jre  
  - `org.springframework:spring-test`: 4.1.6.RELEASE -> LATEST (Boot-managed; the duplicate declaration was removed)  
  - `org.springframework:spring-beans`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-context`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-context-support`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-webmvc`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-web`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-aop`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-core`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework:spring-expression`: 4.1.6.RELEASE -> LATEST  
  - `org.springframework.boot:spring-boot-starter-actuator`: 1.2.3.RELEASE -> LATEST  
  - `org.springframework.boot:spring-boot-starter-test`: 1.2.3.RELEASE -> LATEST  
  - `commons-codec:commons-codec`: 1.10 -> LATEST  
  - `org.codehaus.groovy:groovy-all`: 2.4.3 -> LATEST (in practice pinned to `2.4.16` through a new `<dependencyManagement>` entry)  
  
## 🧰 Tools used  
- ☕ Java SDK: Amazon Corretto 25 (javac `release 25`), Apache Maven 3.9.16  
- 🔁 OpenRewrite recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25` (applied through `OfficialUpgradeToJava25WithoutIfElseIfToSwitch`, which includes `PrimitiveWrapperClassConstructorToValueOf`, `UseMavenCompilerPluginReleaseConfiguration` and `AddMockitoJavaAgentToMavenSurefirePlugin`)  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (a chain from `UpgradeSpringBoot_2_5` up to `UpgradeSpringBoot_4_0`)  
- 🤖 Amazon Kiro CLI `kiro-cli 2.14.2` with model `claude-opus-5.5`, which handled the build fix and the report  
  
## 💻 Code changes  
- 🔢 `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")`  
- 💉 `DownloadController.java`: the `@Autowired` annotation on the single constructor and its `org.springframework.beans.factory.annotation.Autowired` import were removed.  
- 🏷️ `Sha512ShallowEtagHeaderFilter.java` (test): the override `generateETagHeaderValue(byte[] bytes)` became `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`. The ETag is now a SHA-512 hash of `inputStream.readAllBytes()`, with a `W/` prefix when `isWeak` is true. The `java.io.IOException` and `java.io.InputStream` imports were added.  
- 🛠️ `pom.xml`:  
  - Boot parent set to `4.0.9`, the `argLine` and `java.version=25` properties added, and compiler `source`/`target` 1.8 replaced by `<release>`.  
  - Explicit Spring, Boot and `commons-codec` versions removed, so the Boot BOM now manages them.  
  - The duplicate `spring-test` declaration removed.  
  - `spring-boot-starter-web` replaced by `spring-boot-starter-webmvc`, and a test-scoped `jakarta.servlet-api` added.  
  - `junit:junit` excluded from `spock-core`.  
  - `dependency` and `surefire` plugin configuration added.  
  
## ⚠️ Risks  
- 🚫 "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." Application startup and request handling under Spring Boot 4.0.9 are **unverified**.  
- 🧪 Test execution was not recorded. In the verification build, test compilation covered 3 Java sources and no Groovy compilation step appears. The Spock specification `DownloadControllerTest.groovy` therefore has no recorded results, and the controller behaviour (ETag/`If-Modified-Since` caching, HEAD/GET, 404) has no automated check from this run.  
- 🔀 The jump is large, from Spring Boot 1.2.3 to 4.0.9, which also moves the app from `javax.*` to `jakarta.*`. The static check reports "OK (static)", but behavioural changes in Spring MVC request mapping, `Optional` header binding and actuator endpoints can only be caught by running the app.  
- 📦 `spring-boot-starter-webmvc` is compile scope and packages an embedded servlet stack. That is expected for an executable Boot jar, but deploying the app to an external servlet container would create a classloader-conflict risk. `jakarta.servlet-api` is test scope, so it is not packaged.  
- 🧩 Test dependencies are legacy: Spock `1.0-groovy-2.4` and Groovy `2.4.16` predate Java 25 and JUnit 5. `cglib-nodep 3.1` is still declared.  
- 🕰️ The verification build reports that `FileSystemPointer.java` "uses or overrides a deprecated API."  
- 🌐 `pom.xml` still declares the `http://repo.spring.io/milestone/` repository over plain HTTP.  
  
## ⏱️ Time savings  
- ⚙️ Transformation duration: 14 minute(s)  
- 💰 Estimated time saved: 150 minute(s)  
  
## 🚀 Next steps  
- ▶️ Run the opt-in smoke start (`export ENABLE_RUNTIME_VALIDATION=true`) and test the download endpoint by hand: GET/HEAD, `If-None-Match`, `If-Modified-Since`, and a 404 for an unknown UUID.  
- 🧪 Bring the Spock tests back: move to Spock 2.x for Groovy 4 (`org.apache.groovy`), add a Groovy compiler plugin (for example gmavenplus), and confirm that Surefire reports test totals.  
- 🧹 Remove the unused `spring.version` (4.1.1.RELEASE) property, the `cglib-nodep` dependency, and the plain-HTTP milestone repository.  
- 🔍 Rebuild with `-Xlint:deprecation` and replace the deprecated API used in `FileSystemPointer.java`.  
- 📈 Review the Guava version (`29.0-jre`) and the actuator endpoint exposure defaults under Spring Boot 4.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 14 minute(s)
- ⏳ Estimated time saved: 150 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 7.50  
Includes 0.66 for job setup (startup smoke test and knowledge-base indexing).  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s

## Requirements register refresh

- Outcome: Refreshed
- Duration: 0.8s

## Feature Parity Verification

- Requirements evaluated: 29
- Satisfied: 29
- Regressions: 0
- Unverified: 0
- Behaviour-judged (changed requirements): 6

### Verdict coverage

Of 29 requirement(s): 0 test-backed, 6 behaviour-judged, 23 unchanged-by-construction, 0 unverified.

> **Test evidence.** No verdict is test-backed: neither the initial build nor the post-upgrade verification wrote any test reports, so no test ran that parity could use.

No regressions were detected; every requirement is satisfied.

### Satisfied (29) — reasoning

- **FR-find-file-320ac64d**: WHEN a caller invokes findFile, the FileStorage SHALL return the file for the given uuid.
  - Acceptance criteria:
    - AC1: WHEN findFile completes normally, the FileStorage SHALL return a value of type Optional&lt;FilePointer>.
    - AC2: WHEN findFile is invoked, the FileStorage SHALL accept uuid of type UUID.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-get-etag-64a98481**: WHEN a caller invokes getEtag, the FilePointer SHALL return the etag.
  - Acceptance criteria:
    - AC1: WHEN getEtag completes normally, the FilePointer SHALL return a value of type String.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-get-last-modified-18e5dfe8**: WHEN a caller invokes getLastModified, the FilePointer SHALL return the last modified.
  - Acceptance criteria:
    - AC1: WHEN getLastModified completes normally, the FilePointer SHALL return a value of type Instant.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-get-media-type-068ff4c3**: WHEN a caller invokes getMediaType, the FilePointer SHALL return the media type.
  - Acceptance criteria:
    - AC1: WHEN getMediaType completes normally, the FilePointer SHALL return a value of type Optional&lt;MediaType>.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-get-original-name-a43e4662**: WHEN a caller invokes getOriginalName, the FilePointer SHALL return the original name.
  - Acceptance criteria:
    - AC1: WHEN getOriginalName completes normally, the FilePointer SHALL return a value of type String.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-get-size-c2289cab**: WHEN a caller invokes getSize, the FilePointer SHALL return the size.
  - Acceptance criteria:
    - AC1: WHEN getSize completes normally, the FilePointer SHALL return a value of type long.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-build-resource-03dab7d6**: WHEN a caller invokes buildResource, the ExistingFile SHALL build the resource.
  - Acceptance criteria:
    - AC1: WHEN buildResource runs, the ExistingFile SHALL call open.
    - AC2: WHEN buildResource completes normally, the ExistingFile SHALL return new InputStreamResource(throttlingInputStream).
    - AC3: WHEN buildResource is invoked, the ExistingFile SHALL accept filePointer of type FilePointer.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-download-controller-a5b5b8a3**: WHEN a caller creates a DownloadController, the DownloadController SHALL initialize the DownloadController with the given storage.
  - Acceptance criteria:
    - AC1: WHEN DownloadController is constructed, the DownloadController SHALL accept storage of type FileStorage.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The only change is removal of the @Autowired annotation on the constructor. Since Spring 4.3+, a single constructor is implicitly used for autowiring, so behaviour (storage being injected and initialized) is preserved.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-download-retrieval-15e14161**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN download runs, the DownloadController SHALL call findExistingFile and handle.
    - AC2: WHEN download is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The download method code is identical between BEFORE and AFTER; behaviour is unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-find-existing-file-9efb6079**: WHEN a caller invokes findExistingFile, the DownloadController SHALL return the existing file for the given method and uuid.
  - Acceptance criteria:
    - AC1: WHEN findExistingFile runs, the DownloadController SHALL call findFile.
    - AC2: WHEN findExistingFile completes normally, the DownloadController SHALL return a value of type Optional&lt;ExistingFile>.
    - AC3: WHEN findExistingFile is invoked, the DownloadController SHALL accept method of type HttpMethod and uuid of type UUID.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: findExistingFile implementation is identical between BEFORE and AFTER; behaviour is unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handle-configuration-5f6121fd**: WHEN a caller invokes handle, the ExistingFile SHALL handle the request etag opt.
  - Acceptance criteria:
    - AC1: WHEN handle runs, the ExistingFile SHALL call serveWithCaching.
    - AC2: WHEN handle completes normally, the ExistingFile SHALL return serveWithCaching(requestEtagOpt, ifModifiedSinceOpt, this::serveDownload).
    - AC3: WHEN handle is invoked, the ExistingFile SHALL accept requestEtagOpt of type Optional&lt;String> and ifModifiedSinceOpt of type Optional&lt;Date>.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-handles-configuration-fo-9109a9b9**: WHEN a caller invokes cached, the ExistingFile SHALL perform cached.
  - Acceptance criteria:
    - AC1: WHEN cached completes normally, the ExistingFile SHALL return matchingEtag || notModifiedSince.
    - AC2: WHEN cached is invoked, the ExistingFile SHALL accept requestEtagOpt of type Optional&lt;String> and ifModifiedSinceOpt of type Optional&lt;Date>.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-handles-configuration-fo-6b216cd9**: WHEN a caller invokes redirect, the ExistingFile SHALL perform redirect.
  - Acceptance criteria:
    - AC1: WHEN redirect runs, the ExistingFile SHALL call serveWithCaching.
    - AC2: WHEN redirect completes normally, the ExistingFile SHALL return serveWithCaching(requestEtagOpt, ifModifiedSinceOpt, this::redirectDownload).
    - AC3: WHEN redirect is invoked, the ExistingFile SHALL accept requestEtagOpt of type Optional&lt;String> and ifModifiedSinceOpt of type Optional&lt;Date>.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-handles-retrieval-for-fi-6aab06fd**: WHEN a caller creates a FileSystemPointer, the FileSystemPointer SHALL initialize the FileSystemPointer with the given target.
  - Acceptance criteria:
    - AC1: WHEN FileSystemPointer is constructed, the FileSystemPointer SHALL accept target of type File.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-handles-retrieval-for-re-121f3aac**: WHEN a caller invokes redirectDownload, the ExistingFile SHALL perform redirectDownload.
  - Acceptance criteria:
    - AC1: WHEN redirectDownload runs, the ExistingFile SHALL call getOriginalName.
    - AC2: WHEN redirectDownload completes normally, the ExistingFile SHALL return a value of type ResponseEntity&lt;Resource>.
    - AC3: WHEN redirectDownload is invoked, the ExistingFile SHALL accept filePointer of type FilePointer.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-handles-retrieval-for-re-e2c6b1f6**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN redirect runs, the DownloadController SHALL call findExistingFile.
    - AC2: WHEN redirect is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The redirect method implementation is unchanged between BEFORE and AFTER; it still looks up the existing file and returns the resource or NOT_FOUND. Behavior is identical.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-handles-retrieval-for-se-a7ea24aa**: WHEN a caller invokes serveDownload, the ExistingFile SHALL perform serveDownload.
  - Acceptance criteria:
    - AC1: WHEN serveDownload runs, the ExistingFile SHALL call resourceToReturn and response.
    - AC2: WHEN serveDownload completes normally, the ExistingFile SHALL return response(filePointer, OK, resource).
    - AC3: WHEN serveDownload is invoked, the ExistingFile SHALL accept filePointer of type FilePointer.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-handles-retrieval-for-th-51ba29c2**: WHEN a caller creates a ThrottlingInputStream, the ThrottlingInputStream SHALL initialize the ThrottlingInputStream with the given target and max bytes per second.
  - Acceptance criteria:
    - AC1: WHEN ThrottlingInputStream is constructed, the ThrottlingInputStream SHALL accept target of type InputStream and maxBytesPerSecond of type RateLimiter.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-provide-download-control-2f528472**: WHEN an HTTP request is received on /download, the DownloadController SHALL provide the operations redirect, download and findExistingFile.
  - Acceptance criteria:
    - AC1: The DownloadController SHALL expose the operations redirect, download and findExistingFile.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The redirect, download, and findExistingFile methods remain functionally identical. The only change is removal of @Autowired on the constructor (expected Spring Boot upgrade transformation since single-constructor injection is implicit), which does not alter observable behavior.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-provide-existing-file-ca-1dacd5f3**: WHEN a caller invokes an operation of ExistingFile, the ExistingFile SHALL provide the operations redirect, handle, serveWithCaching, cached and redirectDownload (+6 more).
  - Acceptance criteria:
    - AC1: The ExistingFile SHALL expose the operations redirect, handle, serveWithCaching, cached and redirectDownload (+6 more).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-provide-file-pointer-cap-95d2dd09**: WHEN a caller invokes an operation of FileSystemPointer, the FileSystemPointer SHALL provide the operations open, getSize, getOriginalName, getEtag and getMediaType (+4 more).
  - Acceptance criteria:
    - AC1: The FileSystemPointer SHALL implement FilePointer.
    - AC2: The FileSystemPointer SHALL expose the operations open, getSize, getOriginalName, getEtag and getMediaType (+4 more).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-provide-file-storage-cap-d1379f4d**: WHEN a caller invokes an operation of FileSystemStorage, the FileSystemStorage SHALL provide the operation findFile.
  - Acceptance criteria:
    - AC1: The FileSystemStorage SHALL expose the operation findFile.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-provide-main-application-20938697**: WHEN a caller invokes an operation of MainApplication, the MainApplication SHALL provide the operation main.
  - Acceptance criteria:
    - AC1: The MainApplication SHALL expose the operation main.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The change from `new Integer("1234")` to `Integer.valueOf("1234")` is a deprecated-API replacement (PrimitiveWrapperClassConstructorToValueOf recipe), producing an equivalent Integer value with no change in observable behavior. The main method still starts the Spring application the same way.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-provide-throttling-input-59596463**: WHEN a caller invokes an operation of ThrottlingInputStream, the ThrottlingInputStream SHALL provide the operations read, skip, available, mark and reset (+2 more).
  - Acceptance criteria:
    - AC1: The ThrottlingInputStream SHALL expose the operations read, skip, available, mark and reset (+2 more).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-read-as-part-workflow-20e38878**: WHEN a caller invokes read, the ThrottlingInputStream SHALL return the throttling input stream for the given b, off and len.
  - Acceptance criteria:
    - AC1: WHEN read completes normally, the ThrottlingInputStream SHALL return target.read(b, off, len).
    - AC2: WHEN read is invoked, the ThrottlingInputStream SHALL accept b of type byte[], off of type int and len of type int.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **NFR-OBSERVABILITY-9f46ce8c**: WHEN redirectDownload, serveDownload, notModified or another supporting component handles a request or an error, the system SHALL emit logs, metrics, or traces for its operations.
  - Acceptance criteria:
    - AC1: WHEN ExistingFile.redirectDownload is invoked, the system SHALL record a trace-level log entry (evidence: log.trace in ExistingFile.redirectDownload, ExistingFile.java:67).
    - AC2: WHEN ExistingFile.serveDownload is invoked, the system SHALL record a debug-level log entry (evidence: log.debug in ExistingFile.serveDownload, ExistingFile.java:78).
    - AC3: WHEN ExistingFile.notModified is invoked, the system SHALL record a trace-level log entry (evidence: log.trace in ExistingFile.notModified, ExistingFile.java:98).
    - AC4: WHEN FileSystemStorage.findFile is invoked, the system SHALL record a debug-level log entry (evidence: log.debug in FileSystemStorage.findFile, FileSystemStorage.java:25).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **NFR-PERFORMANCE-92c573e7**: WHEN cached serves repeated or bulk work, the system SHALL use caching or batching to bound response latency.
  - Acceptance criteria:
    - AC1: WHEN ExistingFile.cached serves repeated or bulk work, the system SHALL use caching or batching (evidence: cached in ExistingFile.cached, ExistingFile.java:54).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **NFR-RELIABILITY-49e8cb2f**: WHEN an exception is raised in redirectDownload, FileSystemPointer or open, the system SHALL catch and handle exceptions raised during its operations.
  - Acceptance criteria:
    - AC1: IF URISyntaxException is thrown in ExistingFile.redirectDownload, THEN the system SHALL catch it and throw IllegalArgumentException (evidence: catch (URISyntaxException e) in ExistingFile.redirectDownload, ExistingFile.java:72).
    - AC2: IF IOException is thrown in FileSystemPointer.FileSystemPointer, THEN the system SHALL catch it and throw IllegalArgumentException (evidence: catch (IOException e) in FileSystemPointer.FileSystemPointer, FileSystemPointer.java:27).
    - AC3: IF FileNotFoundException is thrown in FileSystemPointer.open, THEN the system SHALL catch it and throw IllegalArgumentException (evidence: catch (FileNotFoundException e) in FileSystemPointer.open, FileSystemPointer.java:36).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **NFR-SECURITY-742b03ce**: WHEN sensitive data is handled by an operation configured by tag, the system SHALL protect data in transit and at rest using cryptographic controls.
  - Acceptance criteria:
    - AC1: WHEN sensitive data is handled by an operation configured by FileSystemPointer.tag, the system SHALL apply cryptographic protection (evidence: HashCode in FileSystemPointer.tag, FileSystemPointer.java:16).
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
