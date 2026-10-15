# 📋 Executive Report: Java 25 Upgrade, Iteration 21 (`download-server`)  
  
## 🧭 Executive summary  
- 🎯 This iteration moved the `download-server` Spring Boot app from Java 8 / Spring Boot 1.2.3.RELEASE to Java 25 (`<release>25</release>`) and `spring-boot-starter-parent` 4.0.8.  
- 🔧 Two OpenRewrite recipes ran: `com.amazonaws.java.migrate.UpgradeToJava25`, then `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. Kiro CLI then fixed the one remaining test-compilation error.  
- ✅ The post-upgrade verification build succeeded. ⚠️ It discovered and ran no automated tests, so it verifies compilation only.  
- ⚠️ Runtime validation logged this WARNING: "WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required."  
- ❗ Because of that warning, the Spring upgrade is reported as **incomplete, needing follow-up**. code-upgrade.log has no "Verified: effective Spring Framework version..." line confirming that Spring reached the target.  
- 🧐 My own assessment, which is not verified: the warning may be a false positive. The committed `pom.xml` still has the leftover property `<spring.version>4.1.1.RELEASE</spring.version>`. Meanwhile every `org.springframework:*` dependency in the diff has had its explicit `4.1.6.RELEASE` version removed, so the Boot 4.0.8 parent now manages them. A static check may be reading the stale property. None of the artifacts contain a resolved dependency tree that would confirm this, so the warning stands as a risk.  
- 🚫 The smoke-start tier was skipped: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." Nothing has checked whether the application starts or how it behaves at runtime.  
  
## 🗂️ Application changes  
- 📦 5 files changed, 70 insertions, 32 deletions (`git diff --stat` / report.json):  
  - 🛠️ `pom.xml`  
  - ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java`  
  - ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java`  
  - 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`  
  - 📝 `summary.md` (the build-fix notes written by Kiro CLI in the repo)  
- ➕ Dependencies added (report.json; scope from the `pom.xml` diff):  
  - `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST): default **compile** scope. It replaces `spring-boot-starter-web`.  
  - `jakarta.servlet:jakarta.servlet-api` (version LATEST): **test** scope.  
  - `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST): build plugin, no scope. It runs the `properties` goal.  
  - `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST): build plugin, no scope. It configures `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}`.  
- ➖ Dependencies removed (report.json):  
  - `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE)  
- ⬆️ Dependencies upgraded (report.json):  
  - `org.springframework:spring-test` (4.1.6.RELEASE -> LATEST)  
  - `commons-codec:commons-codec` (1.10 -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-actuator` (1.2.3.RELEASE -> LATEST)  
  - `org.springframework.boot:spring-boot-starter-test` (1.2.3.RELEASE -> LATEST)  
  - `com.google.guava:guava` (18.0 -> 29.0-jre)  
  - `org.springframework:spring-beans` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-context-support` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-webmvc` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-web` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-aop` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-core` (4.1.6.RELEASE -> LATEST)  
  - `org.springframework:spring-expression` (4.1.6.RELEASE -> LATEST)  
  - `org.codehaus.groovy:groovy-all` (2.4.3 -> LATEST). In the diff, the version is actually pinned to `2.4.16` in a new `<dependencyManagement>` block.  
  - `org.springframework.boot:spring-boot-starter-parent` (1.2.3.RELEASE -> 4.0.8)  
  - `org.apache.maven.plugins:maven-compiler-plugin` (3.0 -> 3.16.0)  
- ℹ️ "LATEST" means the diff removed the explicit `<version>`, so the Spring Boot 4.0.8 parent now manages the version.  
  
## 🧰 Tools used  
- ☕ Java SDK: Amazon Corretto OpenJDK 25.0.4.1 (build 25.0.4.1+10-LTS), with compilation targeting `release 25`.  
- 🔁 OpenRewrite `rewrite-maven-plugin` 6.42.0, with these recipes:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`. It applied changes to `pom.xml` and `MainApplication.java`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. It applied changes to `pom.xml` and `DownloadController.java`.  
- 🤖 Amazon Kiro CLI `kiro-cli 2.14.2` on model `claude-opus-5.5`. It succeeded on attempt 1 of 5 and handled the automated build fix.  
  
## 💻 Code changes (as shown in the diff)  
- 🔢 `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")` (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`).  
- 💉 `DownloadController.java`: `@Autowired` removed from the constructor, along with its `org.springframework.beans.factory.annotation.Autowired` import (OpenRewrite `NoAutowiredOnConstructor`).  
- 🔐 `Sha512ShallowEtagHeaderFilter.java` (test source, fixed by Kiro CLI): the override `generateETagHeaderValue(byte[] bytes)` became `generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException`.  
  - The new override reads the stream with `StreamUtils.copyToByteArray`, hashes it with `Hashing.sha512()`, and adds a `W/` prefix when `isWeak` is true.  
- 🛠️ `pom.xml`:  
  - Java and build:  
    - Added `<java.version>25</java.version>` and an empty `<argLine>` property.  
    - The compiler plugin now uses `<release>${java.version}</release>` instead of `source/target 1.8`.  
  - Dependencies:  
    - `spring-boot-starter-web` replaced by `spring-boot-starter-webmvc`.  
    - Explicit Spring and Boot versions removed, so the parent manages them.  
    - Duplicate `spring-test` declaration removed.  
    - `junit:junit` excluded from `spock-core`.  
    - The new dependency plugin, surefire plugin and `groovy-all` 2.4.16 entries are listed under Application changes above.  
  - ⚠️ Not changed, still present: `<spring.version>4.1.1.RELEASE</spring.version>`, `cglib-nodep` 3.1, and the `http://repo.spring.io/milestone/` repository.  
  
## ⚠️ Risks  
- 🚨 **Runtime validation WARNING** (verbatim): "WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required."  
  - Until a resolved dependency tree disproves it, the Spring upgrade has to be treated as incomplete.  
- 🚫 **Runtime behaviour unverified**: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)."  
- 🧪 **No automated tests ran.**  
  - The Spock spec `DownloadControllerTest.groovy` is not compiled, because the pom has no Groovy compiler plugin.  
  - The parity evidence shows 0 test classes before and 0 after.  
  - Download, ETag, `If-Modified-Since` and `HEAD` behaviour is therefore unverified.  
- 🔄 **Major framework jump**: Boot 1.2.3 -> 4.0.8 moves the app to the jakarta namespace (OpenRewrite applied `JavaxMigrationToJakarta` / `JakartaEE10`).  
  - Behaviour changes in MVC, actuator endpoints and configuration properties have not been validated at runtime.  
- 📦 **Classloader-conflict check**:  
  - `jakarta.servlet-api` is test-scoped, so it is not packaged with the app.  
  - `spring-boot-starter-webmvc` is compile-scoped and brings an embedded servlet container and servlet API with it. That is expected for a Boot executable jar. It becomes a conflict risk only if the app is ever deployed as a WAR to an external application server that provides its own servlet API.  
- 🧱 **Stale test-stack dependencies**:  
  - `groovy-all` 2.4.16, `spock-core` / `spock-spring` `1.0-groovy-2.4` and `cglib-nodep` 3.1 are old.  
  - `cglib-nodep` 3.1 is compile-scoped and ships with the application.  
  - These libraries were not updated for Java 25 or Spring 7.  
- 🔐 **ETag semantics**: the test filter's ETag method now follows the newer signature and supports weak ETags. It is test-only code, and no test exercises it.  
- 🌐 **Insecure repository**: the `http://repo.spring.io/milestone/` repository entry is unchanged and uses plain HTTP.  
  
## ⏱️ Time savings  
- ⌛ Transformation duration: 14 minute(s)  
- 💰 Estimated time saved: 45 minute(s)  
  
## 🚀 Next steps  
- 🔍 Run `mvn dependency:tree -Dincludes=org.springframework` to confirm the effective Spring Framework version.  
- 🧹 Remove the stale `<spring.version>4.1.1.RELEASE</spring.version>` property, then re-run runtime validation to clear or confirm the WARNING.  
- ▶️ Re-run runtime validation with `ENABLE_RUNTIME_VALIDATION=true` so the smoke-start tier checks that the app boots.  
- 🧪 Restore automated tests:  
  - Add a Groovy compiler plugin (e.g. gmavenplus).  
  - Move to Groovy 4.x and Spock 2.x so `DownloadControllerTest.groovy` compiles and runs on Java 25 / Spring 7.  
- 🧱 Remove or replace `cglib-nodep` 3.1. Review whether the `groovy-all` 2.4.16 pin is still needed.  
- 🌐 Remove the plain-HTTP `repo.spring.io/milestone` repository, or switch it to HTTPS.  
- 🧭 Manually test the download endpoints (GET/HEAD, `If-None-Match`, `If-Modified-Since`) and the actuator endpoints after the Boot 4 move.  
- 🧾 Review the `FileSystemPointer.java` deprecated-API notice from the post-upgrade compile with `-Xlint:deprecation`.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: none discovered or run (compilation only)
- ⏱️ Transformation duration: 14 minute(s)
- ⏳ Estimated time saved: 45 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - ⚠️ WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required.
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 8.92  
Includes 0.91 for job setup (startup smoke test and knowledge-base indexing).  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s

## Requirements register refresh

- Outcome: Refreshed
- Duration: 3.7s

## Feature Parity Verification

- Requirements evaluated: 29
- Satisfied: 29
- Regressions: 0
- Unverified: 0
- Behaviour-judged (changed requirements): 6

### Verdict coverage

Of 29 requirement(s): 0 test-backed, 6 behaviour-judged, 23 unchanged-by-construction, 0 unverified.

> **Test evidence.** No verdict is test-backed: neither the initial build nor the post-upgrade verification wrote any test reports, so no test ran that parity could use.

> **Runtime validation warning.** These verdicts check the code against the requirements; they do not show that the upgraded application starts. This iteration's `runtime-validation.log` reports:
>
> - WARNING (static): lowest effective Spring Framework version is 4.1 (< 5.3) on a Java 25 build. Old Spring's ASM cannot read Java 17+ class files; the app will crash at startup during component scanning. The Spring upgrade did not fully take - Kiro/manual follow-up required.

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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The only change is removal of the @Autowired annotation from the constructor. Since Spring 4.3+, a single constructor is implicitly used for autowiring without the annotation, so behavior is preserved (likely a NoAutowiredOnConstructor-style simplification tied to framework upgrade norms).
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-download-retrieval-15e14161**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN download runs, the DownloadController SHALL call findExistingFile and handle.
    - AC2: WHEN download is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: Code is identical between BEFORE and AFTER aside from line numbers.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-find-existing-file-9efb6079**: WHEN a caller invokes findExistingFile, the DownloadController SHALL return the existing file for the given method and uuid.
  - Acceptance criteria:
    - AC1: WHEN findExistingFile runs, the DownloadController SHALL call findFile.
    - AC2: WHEN findExistingFile completes normally, the DownloadController SHALL return a value of type Optional&lt;ExistingFile>.
    - AC3: WHEN findExistingFile is invoked, the DownloadController SHALL accept method of type HttpMethod and uuid of type UUID.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: Code is identical between BEFORE and AFTER aside from line numbers.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The redirect method implementation is unchanged between BEFORE and AFTER; logic for finding the existing file and mapping to a redirect response is identical.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The redirect, download, and findExistingFile methods remain functionally identical. The only change is removal of @Autowired on the constructor, which is a stylistic/expected Spring Boot upgrade transformation (constructor injection remains implicit single-constructor autowiring), not a behavioral change.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The main method still calls SpringApplication.run with the same arguments. The change from 'new Integer("1234")' to 'Integer.valueOf("1234")' is a semantically equivalent refactor (matches the PrimitiveWrapperClassConstructorToValueOf expected recipe), producing an equal Integer value with no observable behavior change in the main method's effect.
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
