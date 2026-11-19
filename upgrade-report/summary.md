# 🚀 Java 25 Upgrade Report: download-server (Iteration 60)  
  
## 📋 Executive summary  
- 🎯 The `download-server` Maven project (a Spring Boot file-download service with ETag and If-Modified-Since caching) was moved from Java 1.8 and Spring Boot 1.2.3.RELEASE to Java 25 and Spring Boot 4.0.9.  
- 🛠️ OpenRewrite ran two recipes, `com.amazonaws.java.migrate.UpgradeToJava25` and `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`. Both ended in `BUILD SUCCESS` and changed `pom.xml`, `MainApplication.java` and `DownloadController.java`.  
- 🤖 The Amazon Kiro CLI build-fix pass then fixed one test-compile error in `Sha512ShallowEtagHeaderFilter.java`, caused by a Spring Framework 7 API change.  
- ✅ The post-upgrade verification build succeeded (`compiler:3.16.0` with `release 25`, then `BUILD SUCCESS`).  
- 🧪 Test execution was not recorded: the verification log shows a `surefire:3.5.6:test` step but no Surefire test totals.  
- 🌱 Spring compatibility: runtime validation reported "OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it)." The Spring Boot 4.0.9 parent manages the Spring Framework version.  
- ⚠️ Runtime behaviour is unverified: "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The application was not started in this iteration.  
- 📦 The changes are committed on branch `main-upgrade-itr-60` as commit `6818a35` ("Upgrade to Java 25 on Iteration 60").  
  
## 🧩 Application changes  
- 📁 5 files changed, with 74 insertions and 32 deletions (`git diff --stat 73556be..HEAD`):  
  - 📄 `pom.xml` (+/- 72 lines)  
  - ☕ `src/main/java/com/nurkiewicz/download/DownloadController.java` (2 lines removed)  
  - ☕ `src/main/java/com/nurkiewicz/download/MainApplication.java` (1 line changed)  
  - 🧪 `src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java` (+/- 10 lines)  
  - 📝 `summary.md` (new file, 20 lines, notes from the Kiro build-fix pass)  
- ➕ Dependencies and plugins added (report.json; scope taken from the `pom.xml` diff):  
  - 🌐 `org.springframework.boot:spring-boot-starter-webmvc` (version LATEST, managed by the Boot 4.0.9 parent). Compile scope, since no `<scope>` is declared. It replaces `spring-boot-starter-web`.  
  - 🔌 `jakarta.servlet:jakarta.servlet-api` (version LATEST, Boot-managed). **Test scope**, so the servlet API is not packaged with the application.  
  - 🔧 `org.apache.maven.plugins:maven-dependency-plugin` (version LATEST, Boot-managed; the verification log shows 3.9.0). It is a build plugin that runs the `properties` goal.  
  - 🔧 `org.apache.maven.plugins:maven-surefire-plugin` (version LATEST, Boot-managed; the verification log shows 3.5.6). It is a build plugin, configured with `-javaagent:${net.bytebuddy:byte-buddy-agent:jar}`.  
- ➖ Dependencies removed (report.json):  
  - 🗑️ `org.springframework.boot:spring-boot-starter-web` (version 1.2.3.RELEASE), replaced by `spring-boot-starter-webmvc`.  
  - 🗑️ The second, duplicate `org.springframework:spring-test` 4.1.6.RELEASE declaration was also removed from `pom.xml` (diff). Maven had warned about it as "must be unique" in `code-upgrade.log`.  
- ⬆️ Dependencies upgraded (report.json). "LATEST" means the explicit version was removed and the Spring Boot 4.0.9 parent now manages it:  
  - 🌱 `org.springframework:spring-test` 4.1.6.RELEASE -> LATEST (test scope)  
  - 🔐 `commons-codec:commons-codec` 1.10 -> LATEST  
  - 📊 `org.springframework.boot:spring-boot-starter-actuator` 1.2.3.RELEASE -> LATEST  
  - 🧪 `org.springframework.boot:spring-boot-starter-test` 1.2.3.RELEASE -> LATEST (test scope)  
  - 🧰 `com.google.guava:guava` 18.0 -> 29.0-jre  
  - 🌱 `org.springframework:spring-beans` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-context` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-context-support` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-webmvc` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-web` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-aop` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-core` 4.1.6.RELEASE -> LATEST  
  - 🌱 `org.springframework:spring-expression` 4.1.6.RELEASE -> LATEST  
  - 🟢 `org.codehaus.groovy:groovy-all` 2.4.3 -> LATEST. The `pom.xml` diff pins it to 2.4.16 in a new `<dependencyManagement>` block.  
  - 🏗️ `org.springframework.boot:spring-boot-starter-parent` 1.2.3.RELEASE -> 4.0.9  
  - 🔧 `org.apache.maven.plugins:maven-compiler-plugin` 3.0 -> 3.16.0  
- ⚙️ Other `pom.xml` changes (diff):  
  - Added the `java.version` property set to `25` and an empty `argLine` property.  
  - Replaced compiler `<source>1.8</source>`/`<target>1.8</target>` with `<release>${java.version}</release>`.  
  - Excluded `junit:junit` from `spock-core` (from OpenRewrite's `ExcludeJUnit4UnlessUsingTestcontainers` step).  
  
## 🧰 Tools used  
- ☕ **Java SDK:** JDK 25. The final build compiled with `javac [debug parameters release 25]` (post-upgrade-verification.log).  
- 🔄 **OpenRewrite:** `rewrite-maven-plugin` 6.42.0 running:  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which applied `UpgradeJavaVersion`, `UseMavenCompilerPluginReleaseConfiguration`, `UpgradePluginVersion`, `UpgradeDependencyVersion`, `PrimitiveWrapperClassConstructorToValueOf` and `AddMockitoJavaAgentToMavenSurefirePlugin`.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which applied the chained Spring Boot 2.0 -> 4.0 recipes, including `UpgradeParentVersion`, `ChangeDependency` (starter-web -> starter-webmvc), `JavaxServletToJakartaServlet`/`AddDependency`, `RemoveDuplicateDependencies`, `JUnit4to5Migration`/`ExcludeDependency` and `NoAutowiredOnConstructor`.  
- 🤖 **Amazon Kiro CLI:** `kiro-cli 2.35.1` with model `claude-opus-5.5`. It succeeded on the first of 5 allowed attempts, ran the automated build fix, and used CodeKB to map the old code.  
  
## 💻 Code changes  
- ☕ `MainApplication.java`: `new Integer("1234")` -> `Integer.valueOf("1234")`. This removes a deprecated boxing constructor (OpenRewrite `PrimitiveWrapperClassConstructorToValueOf`).  
- ☕ `DownloadController.java`: removed `@Autowired` from the single constructor `DownloadController(FileStorage storage)` and removed the now-unused `org.springframework.beans.factory.annotation.Autowired` import (OpenRewrite `NoAutowiredOnConstructor`). Spring still injects through the single constructor.  
- 🧪 `Sha512ShallowEtagHeaderFilter.java` (Kiro build fix, test code):  
  - Spring Framework 7's `ShallowEtagHeaderFilter` no longer has `generateETagHeaderValue(byte[])`. Its replacement is `generateETagHeaderValue(InputStream, boolean isWeak) throws IOException`.  
  - The override now reads the stream with `StreamUtils.copyToByteArray`, still hashes it with Guava `Hashing.sha512()`, and returns `"\"" + hash + "\""`, prefixed with `W/` when `isWeak` is true.  
- 🔍 No other application source changed. The download and caching logic in `ExistingFile`, `FileSystemPointer`, `FileSystemStorage` and `ThrottlingInputStream` is untouched.  
  
## ⚠️ Risks  
- 🚦 **Runtime unverified:** "Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check)." The service has not been started on Java 25 or Spring Boot 4.0.9. Controller binding (`Optional<Date>` `If-Modified-Since` headers, `HttpMethod` arguments) and the actuator endpoints are unverified at runtime.  
- 🧪 **Test execution not recorded:** the verification log has no Surefire totals. The parity check found 0 test classes before and 0 after, and wrote an after-upgrade test inventory of 0 test cases (code-upgrade.log). The Spock test `DownloadControllerTest.groovy` is not covered by the build, so the ETag and 304 behaviour has no automated check.  
- 🌱 **Spring:** the static check passed ("OK (static)..."), with the Spring version managed by the Boot 4.0.9 parent. `code-upgrade.log` also notes that plain (non-Boot) Spring artifacts were detected alongside the Boot upgrade path. These artifacts (`spring-core`, `spring-web` and others) are now declared without versions, so the Boot parent manages them.  
- 🧱 **Old test stack:** `spock-core`/`spock-spring` 1.0-groovy-2.4 and `groovy-all` 2.4.16 remain. They are from the Groovy 2.4 era and are not aligned with JUnit Platform or Java 25.  
- 📦 **Stale dependencies:** `commons-io` 2.4, `cglib-nodep` 3.1 and `guava` 29.0-jre are pinned outside the Boot BOM. The unused `spring.version` property (4.1.1.RELEASE) is still in `pom.xml`.  
- 🔌 **Classloader conflicts:** `jakarta.servlet-api` is test scope only, so it is not packaged. `spring-boot-starter-webmvc` (compile scope) brings the embedded web stack. That is expected for a Boot executable jar, but it is a conflict risk if the artifact is ever deployed to an external servlet container.  
- 🌐 **Repository:** `pom.xml` still declares `http://repo.spring.io/milestone/`. OpenRewrite could not reach it ("Failed to access maven repository"), and plain-HTTP repositories are blocked by modern Maven.  
- 🔁 **ETag filter change:** the test filter now hashes from a stream and supports weak ETags. Its output for strong ETags is unchanged, but this is unverified by tests.  
- ⚠️ **Deprecation:** the compiler reports that `FileSystemPointer.java` "uses or overrides a deprecated API". This is not an error today but could become one in a later JDK or library release.  
  
## ⏱️ Time savings  
- 🕒 Transformation duration: **4 minute(s)**  
- 💰 Estimated time saved: **45 minute(s)**  
  
## 🔜 Next steps  
- ▶️ Enable the smoke-start tier (`export ENABLE_RUNTIME_VALIDATION=true`) or boot the service in a dev environment. Exercise `GET`/`HEAD /download/{uuid}` and `/download/{uuid}/{filename}`, including `If-None-Match` and `If-Modified-Since` requests.  
- 🧪 Bring the Spock tests into the build: add `gmavenplus-plugin`, move to Groovy 4 (`org.apache.groovy`) and Spock 2.x for Groovy 4.0, then confirm that Surefire reports test totals.  
- 🧹 Remove the unused `spring.version` property and the `http://repo.spring.io/milestone/` repository.  
- ⬆️ Review `commons-io` 2.4, `cglib-nodep` 3.1 (probably unneeded) and `guava` 29.0-jre, and move them to current versions that support Java 25.  
- 🔍 Rebuild with `-Xlint:deprecation` and replace the deprecated API used in `FileSystemPointer.java`.  
- 👀 Review and merge commit `6818a35` on `main-upgrade-itr-60` once runtime and test checks pass.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: test execution not recorded (see post-upgrade-verification.log)
- ⏱️ Transformation duration: 4 minute(s)
- ⏳ Estimated time saved: 45 minute(s)
- 🚦 Runtime validation (runtime-validation.log):
  - OK (static): no Java-17-incompatible Spring detected (effective Spring n/a: no build file pins a Spring Framework version; a Spring Boot parent or imported BOM, if present, manages it).
  - Smoke-start tier skipped (opt-in: export ENABLE_RUNTIME_VALIDATION=true to enable an advisory app-boot check).

## Kiro credit usage

💳 Kiro CLI credits used: 8.71  
Includes 1.19 for job setup (startup smoke test and knowledge-base indexing).  

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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The only change is removal of the @Autowired annotation on the constructor. Since Spring 4.3+, a single constructor is implicitly used for autowiring without requiring the annotation, so the DownloadController still gets the storage injected and initialized the same way. This is an expected framework-related simplification, not a behavior change.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-download-retrieval-15e14161**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN download runs, the DownloadController SHALL call findExistingFile and handle.
    - AC2: WHEN download is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The download method is identical between BEFORE and AFTER, with the same mapping, parameters, and logic for returning the existing file or NOT_FOUND.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-find-existing-file-9efb6079**: WHEN a caller invokes findExistingFile, the DownloadController SHALL return the existing file for the given method and uuid.
  - Acceptance criteria:
    - AC1: WHEN findExistingFile runs, the DownloadController SHALL call findFile.
    - AC2: WHEN findExistingFile completes normally, the DownloadController SHALL return a value of type Optional&lt;ExistingFile>.
    - AC3: WHEN findExistingFile is invoked, the DownloadController SHALL accept method of type HttpMethod and uuid of type UUID.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The findExistingFile method is unchanged between BEFORE and AFTER, preserving the same lookup and mapping logic.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The redirect method logic and controller behaviour are unchanged between BEFORE and AFTER; only line numbers shifted slightly, likely due to unrelated edits elsewhere in the file (e.g., removal of @Autowired). The observable behaviour of returning the existing file via findExistingFile and redirect remains identical.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: All three operations (redirect, download, findExistingFile) are present and functionally identical in AFTER. The only difference is removal of the @Autowired annotation on the constructor, which is a stylistic/non-behavioural change since Spring auto-detects single constructors for injection. Observable behaviour is preserved.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The main method still calls SpringApplication.run with the same arguments; the only change is replacing 'new Integer("1234")' with 'Integer.valueOf("1234")', which is a deprecated-constructor-to-factory-method migration producing equivalent behavior (this matches the PrimitiveWrapperClassConstructorToValueOf recipe listed as expected). The observable behavior of invoking main() is preserved.
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
