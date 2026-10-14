# 📊 Executive Report: Java 25 and Spring Boot 4.0 Upgrade for `download-server`  
  
## 1. 🧭 Executive Summary  
`download-server` is a single-module Maven app: a Spring Boot file-download service that handles ETag and If-Modified-Since caching. It moved from **Java 8 / Spring Boot 1.2.3 / Spring Framework 4.1.6** to **Java 25 / Spring Boot 4.0.8 / Spring Framework 7.0.9**.  
  
- **OpenRewrite (Java leg):** changed only `pom.xml` and `MainApplication.java`.  
- **OpenRewrite (Spring Boot leg):** changed only `pom.xml` and `DownloadController.java`.  
- **Kiro CLI:** fixed the one compilation error left after the recipes.  
- **Build:** `mvn clean verify` reports ✅ **BUILD SUCCESS** on Corretto JDK 25.0.4.  
- ⚠️ **No tests ran.** The Spock spec is never compiled, which was also true before the upgrade, so runtime behaviour has not been checked.  
  
## 2. 🏗️ Application Changes  
- ☕ **Java baseline:** Java 8 (`source/target 1.8`) → **Java 25** (`<release>${java.version}</release>`, `java.version=25`).  
- 🌱 **Spring Boot:** parent `1.2.3.RELEASE` → **`4.0.8`**. The log shows `UpgradeParentVersion newVersion=4.0.x`, and the version was confirmed by Maven resolution.  
- 🌿 **Spring Framework (actual result):** `4.1.6.RELEASE` → **`7.0.9`**, on every resolved `org.springframework:*` artifact (core, beans, context, context-support, aop, expression, web, webmvc, test).  
  - ℹ️ The log has **no** `Verified: effective Spring Framework version...` line and **no** `WARNING: ...still < 5.3` line.  
  - ✅ The 7.0.9 version comes from `mvn dependency:list` run on the upgraded tree. That version is ≥ 5.3, and no stale Spring versions remain.  
  - ✅ The Spring upgrade is **complete**.  
- 🔁 **Namespace:** this is the jakarta line (Boot 4 / Spring 7). The recipe added a test-scoped `jakarta.servlet:jakarta.servlet-api`. No `javax.*` imports remain in `src`.  
- 🧩 **Starter rename:** `spring-boot-starter-web` → `spring-boot-starter-webmvc`. This is the Boot 4 modular starter, applied via `ChangeDependency`.  
- 📦 **Version management:** hard-coded Spring and Boot versions were removed so that the Boot BOM manages them. A duplicate `spring-test` declaration that Maven had warned about was removed. `groovy-all 2.4.16` is now pinned in `dependencyManagement`.  
  
## 3. 🛠️ Tools Used  
- ☕ **Java SDK:** Amazon Corretto **25.0.4.1** (LTS), with Apache Maven **3.9.16**.  
- 🔧 **OpenRewrite:** `rewrite-maven-plugin` **6.42.0**.  
  - **Java leg:** `com.amazonaws.java.migrate.UpgradeToJava25` (wraps the official Java 17 → 21 → 25 chain, without `IfElseIfConstructToSwitch`).  
  - **Spring leg:** `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0` (chains Boot 2.0 → 2.7 → 3.0 → 3.5 → 4.0 and Spring Framework 5.0 → 6.0, including `JakartaEE10`).  
- 🤖 **Amazon Kiro CLI:** version **2.14.2**, model **claude-opus-5.5**. It did the build-fix step, used CodeKB to look up the old code, and wrote this report.  
  
## 4. 💻 Code Changes  
These are the changes that were actually applied, based on the "Changes have been made to..." log lines and `git diff`.  
  
- 🤖 **OpenRewrite, Java 25 leg:**  
  - **`pom.xml`:**  
    - `maven-compiler-plugin` 3.0 → 3.16.0, using `<release>`  
    - added `java.version=25`  
    - added a `maven-dependency-plugin` `properties` goal  
    - added the Mockito/Byte Buddy agent to the surefire `argLine` (`AddMockitoJavaAgentToMavenSurefirePlugin`)  
  - **`MainApplication.java`:** `new Integer("1234")` → `Integer.valueOf("1234")` (`PrimitiveWrapperClassConstructorToValueOf`).  
- 🤖 **OpenRewrite, Spring Boot 4.0 leg:**  
  - **`pom.xml`:**  
    - Boot parent → 4.0.8  
    - starter renamed to `webmvc`  
    - explicit Spring and commons-codec versions removed  
    - `guava` 18.0 → 29.0-jre  
    - JUnit 4 excluded from `spock-core`  
    - `jakarta.servlet-api` added (test scope)  
    - duplicate dependencies removed  
  - **`DownloadController.java`:** removed the redundant `@Autowired` on the single constructor (`NoAutowiredOnConstructor`).  
- 🧑‍💻 **Kiro CLI build fix (1 compile error):**  
  - **`src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`:**  
    - **Problem:** the override of `generateETagHeaderValue(byte[])`, which Spring 5.0 removed, failed with "method does not override".  
    - **Fix:** it now overrides `generateETagHeaderValue(InputStream, boolean isWeak)`.  
    - **Behaviour kept:** the ETag is still a SHA-512 hash, and the `W/` prefix is added for weak ETags.  
- ℹ️ **About the data tables:** `SourcesFileResults` also lists the other `.java` and `.groovy` files. Those rows have an estimated saving of 0 and correspond to no edits in `git diff`. They were not counted as changes.  
  
## 5. ⏱️ Time Savings Estimate  
- 📈 **OpenRewrite's own estimate:** **~3h 32m** (Java leg 1h + Spring leg 2h 32m, per the log and the `SourcesFileResults` totals of 3,600 s and 8,820 s).  
- 🧮 **Realistic manual effort:** about **1–2 engineer-days**. That covers:  
  - researching four major Spring Boot jumps (1.2 → 4.0) and two Spring Framework generations  
  - jakarta alignment and the Boot 4 starter split  
  - plugin and JDK alignment  
  - diagnosing the removed `ShallowEtagHeaderFilter` API  
- ⚡ **Automated run time:** about **14 minutes** end to end (02:01 → 02:15 UTC). That includes the 11m 24s Spring recipe run and about 1m 45s of Kiro build fixing.  
- 💰 **Net:** roughly **90%+ effort reduction** for a small codebase (8 main and 3 test source files).  
  
## 6. 🚀 Next Steps  
- 🧪 **Restore the test suite (highest priority):** `DownloadControllerTest.groovy` is never compiled because the POM has no Groovy compiler plugin. Its stack, Spock `1.0-groovy-2.4` / Groovy 2.4, does not support JDK 25 or Spring 7.  
  - Add `gmavenplus-plugin`.  
  - Move to Spock 2.x (`spock-core` and `spock-spring` for Groovy 4) on the JUnit Platform.  
  - Check that the `test` profile with `FileStorageStub` still works under Boot 4.  
- ✅ **Functional validation:** once tests run, confirm the following:  
  - 200 / 304 / 404 responses  
  - ETag and If-Modified-Since handling  
  - HEAD with no body  
  - `Content-Length` and `Content-Type` headers  
  - 1 MB/s throttling in `ThrottlingInputStream`  
- 🧹 **POM cleanup:**  
  - remove the unused `spring.version=4.1.1.RELEASE` property  
  - remove the standalone `cglib-nodep 3.1`, since Spring bundles its own repackaged CGLIB  
- ⬆️ **Dependency hygiene:**  
  - upgrade `commons-io 2.4` and `guava 29.0-jre`  
  - replace or remove the `http://repo.spring.io/milestone` repository, which Maven 3.8+ blocks as plain HTTP  
- ⚠️ **Deprecations:** compile with `-Xlint:deprecation`. The OpenRewrite build flagged a deprecated API use in `FileSystemPointer.java`.  
- 🔍 **Actuator in Boot 4:** check the actuator endpoint exposure and security defaults before going to production.  

## Kiro credit usage

💳 Kiro CLI credits used: 13.64  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s

## Requirements register refresh

- Outcome: Refreshed
- Duration: 0.3s

## Feature Parity Verification

- Requirements evaluated: 29
- Satisfied: 29
- Regressions: 0
- Unverified: 0
- Behaviour-judged (changed requirements): 6

### Verdict coverage

Of 29 requirement(s): 0 test-backed, 6 behaviour-judged, 23 unchanged-by-construction, 0 unverified.

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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The constructor still takes FileStorage and assigns it; only the @Autowired annotation was removed, which is cosmetic for single-constructor Spring beans (implicit autowiring still applies in modern Spring). Observable behaviour of initializing with given storage is unchanged.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-download-retrieval-15e14161**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN download runs, the DownloadController SHALL call findExistingFile and handle.
    - AC2: WHEN download is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The download method signature and logic (findExistingFile, map to handle) are identical between BEFORE and AFTER aside from line number shifts. Behaviour preserved.
  - Judge confidence: high
  - Caveat: Static behavioural judgement of the before/after implementation, not an executed test.
- **FR-find-existing-file-9efb6079**: WHEN a caller invokes findExistingFile, the DownloadController SHALL return the existing file for the given method and uuid.
  - Acceptance criteria:
    - AC1: WHEN findExistingFile runs, the DownloadController SHALL call findFile.
    - AC2: WHEN findExistingFile completes normally, the DownloadController SHALL return a value of type Optional&lt;ExistingFile>.
    - AC3: WHEN findExistingFile is invoked, the DownloadController SHALL accept method of type HttpMethod and uuid of type UUID.
  - Verdict: satisfied (evidence: judge-preserved)
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: findExistingFile method signature and body (calling storage.findFile(uuid)) are unchanged between BEFORE and AFTER. Behaviour preserved.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The DownloadController's redirect method logic is unchanged between BEFORE and AFTER; only line numbers shifted due to the removal of @Autowired annotation elsewhere in the class (an expected recipe change, NoAutowiredOnConstructor). The behaviour of returning the existing file via findExistingFile and mapping to a redirect response is identical.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: All three operations redirect, download, and findExistingFile are present and functionally identical in AFTER. The only change is removal of the @Autowired annotation on the constructor, which is a standard Spring Boot upgrade recipe (NoAutowiredOnConstructor) since single-constructor autowiring is implicit. Observable behaviour is unchanged.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The main method still invokes SpringApplication.run with MainApplication.class and args, preserving the entry point behaviour. The change from 'new Integer("1234")' to 'Integer.valueOf("1234")' is a standard OpenRewrite recipe (PrimitiveWrapperClassConstructorToValueOf) due to deprecated constructor removal, and produces an equivalent value with no observable behavioural difference for this unused temp variable.
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
