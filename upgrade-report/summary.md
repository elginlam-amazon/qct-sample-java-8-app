# 📊 Executive Report: download-server Java 25 / Spring Boot 4.0 Upgrade  
  
## 1. 🧭 Executive Summary  
  
The `download-server` application is a single-module Maven Spring Boot service. It serves files at `/download/{uuid}` and `/download/{uuid}/{filename}`, with ETag and If-Modified-Since caching and throttled streaming at 1 MB/s. AWS Transform Custom ran two automated transformations in sequence. The first, `AWS/java-version-upgrade`, moved the project from a Java 8 source level and Spring Boot 1.2.3 to Java 25 and Spring Boot 3.5.16. The second, `AWS/spring-boot-version-upgrade`, moved it from Boot 3.5.16 to Spring Boot 4.0.10 on Spring Framework 7.0.11. Both runs exited with code 0, and both validation summaries end with **OVERALL STATUS: COMPLETE**. The final build passes on JDK 25 with **13/13 tests**. The baseline ran **0 tests**, because the Spock suite had never been compiled. None of the eight main production classes needed changes for Boot 4.  
  
- ✅ **Outcome:** build succeeds (`mvn -B clean install`) on Corretto 25.0.5.1 with Maven 3.9.16  
- 🧪 **Tests:** 0 executed before the upgrade, 12 after step 1, 13 after step 2, with 0 failures  
- 🔐 **Security:** the final CVE scan of the resolved dependencies is clean. The critical spring-webmvc CVEs left open after step 1 were closed by the move to Spring 7.  
- 🌿 **Branch:** `atx-result-staging-20261120_080117_009f2943`. The base commit is `73556be` and there are 6 transformation commits.  
- ⏱️ **Automation effort:** 132.9 agent minutes in total (58.22 for Java, 74.65 for Spring Boot)  
  
## 2. 🏗️ Application Changes  
  
How the old code worked, mapped with CodeKB on the pre-upgrade code:  
- 🌐 `DownloadController` serves `GET`/`HEAD` on `/download/{uuid}` (301 redirect to the original file name) and on `/download/{uuid}/{filename}` (the download itself).  
- 📦 `ExistingFile` decides between a 304 response (matching ETag or not modified since) and a full response. It builds `ResponseEntity` objects with the ETag, Content-Length, Last-Modified and media type, and wraps the body in a `ThrottlingInputStream` driven by a Guava `RateLimiter`.  
- 🗂️ `FileSystemStorage.findFile` is a stub. It returns `logback.xml` from the classpath for any UUID.  
- 🚫 The project has no `javax.*` imports, no database, no Spring Security and no direct Jackson usage, so the Jakarta, persistence and security migration steps did not apply.  
  
| Area | Before | After |  
|------|--------|-------|  
| ☕ Java | source/target 1.8 (compiler plugin 3.0) | release 25 (compiler plugin 3.13.0) |  
| 🌱 Spring Boot | 1.2.3.RELEASE | 4.0.10 (step 1 went to 3.5.16) |  
| 🌱 Spring Framework | 4.1.x, pinned explicitly | 7.0.11, managed by the BOM |  
| 🐱 Tomcat | Boot 1.2 default | 11.0.27 |  
| 🔣 Jackson | Boot 1.2 default | 3.1.8 (`tools.jackson`) |  
| 🧪 Groovy / Spock | groovy-all 2.4.3 / Spock 1.0-groovy-2.4 (never compiled) | Groovy 5.0.8 / Spock 2.5-groovy-5.0 |  
| 📚 Guava / commons-io / commons-codec / cglib | 18.0 / 2.4 / 1.10 / 3.1 | 33.8.0-jre / 2.22.0 / 1.23.0 / 3.3.0 |  
  
## 3. 🛠️ Tools Used  
  
- ☕ **Java SDK:** Amazon Corretto 25 (25.0.5.1 LTS) for the target build. Corretto 8 was used for the baseline build.  
- 🤖 **AWS Transform Custom CLI (`atx`):** managed transformations `AWS/java-version-upgrade` and `AWS/spring-boot-version-upgrade`, run in `ap-southeast-2` with `--non-interactive --trust-all-tools`  
- 🧠 **CodeKB (MCP):** both transformations used it to map the old classes before planning  
- 📘 **Learnings from earlier runs** were applied: dependency version verification, Spock test enablement, Boot/Java compatibility, Boot 4 target version and Spock test detection  
- 🔨 **Maven 3.9.16** with `mvn -B clean install -Dspotbugs.skip=true -Dcheckstyle.skip=true` as both the build and the validation command  
- 🔍 **A CVE scan** of the resolved dependency tree, plus `curl` smoke tests against the running app  
  
## 4. 💻 Code Changes  
  
**Step 1: Java upgrade (commits `03934ef`, `f93ac31`, `4b2e4e6`, `5ff061b`)**  
- ⚙️ `pom.xml`: maven-compiler-plugin 3.0 → 3.13.0 with `<release>25</release>` and `<parameters>true</parameters>`  
- 🔒 `pom.xml`: removed the plain-HTTP `repo.spring.io/milestone` repository, which Maven 3.9+ blocks  
- 📚 `pom.xml`: upgraded the core libraries. Explicit `spring-*` versions were dropped in favour of the BOM, and a duplicate `spring-test` declaration was removed.  
- 🧪 `pom.xml`: added gmavenplus-plugin 5.1.0 (`targetBytecode` 25) and surefire `**/*Spec.java` includes, which made the dormant Spock suite run  
- 🧹 `MainApplication.java`: replaced the deprecated `new Integer("1234")` with `Integer.valueOf("1234")`  
- 🏷️ `Sha512ShallowEtagHeaderFilter.java` (test): moved its override to the Spring 6 signature `generateETagHeaderValue(InputStream, boolean)` and kept the `W/` weak-ETag prefix  
- 📝 `logback.xml`: replaced the unnamed inline appender with a named `CONSOLE` appender, because Logback 1.5 rejects unnamed appenders  
- 🧪 `DownloadControllerTest.groovy`: `@ContextConfiguration` → `@SpringBootTest`  
- 🧪 `FileExamples.java`: replaced the random UUID fixtures with fixed `UUID.fromString(...)` values  
- 🛡️ `pom.xml`: added CVE overrides `tomcat.version` 10.1.62, `jackson-bom.version` 2.21.8 and `log4j2.version` 2.25.5  
- 📄 `README.md`: added a table of deviations from the mandated versions  
  
**Step 2: Spring Boot 4.0 upgrade (commits `d453019`, `2e9a255`)**  
- 🌱 `pom.xml`: parent 3.5.16 → 4.0.10, and `spring-boot-starter-web` → `spring-boot-starter-webmvc`  
- 🧹 `pom.xml`: removed the three stale version pins so the Boot 4 BOM supplies equal or newer, CVE-clean versions  
- 🧪 `DownloadControllerTest.groovy`: added a feature that tests the `/download/{uuid}` 301 redirect, bringing the total to 13  
- 📄 Added migration artifacts: `migration-plan.json`, `code-intelligence-report.md`, `.migration-test-baseline.tsv`, `migration-results.yaml` and `build.log`. `README.md` got Boot 4 notes.  
  
**⚠️ Deviations to note**  
- 🔼 The mandated Spring Boot 3.2.12 was replaced by 3.5.16 in step 1. The agent tested 3.2.12 on JDK 25, and it failed with `Unsupported class file major version 69` (12 errors in 12 tests).  
- 🔼 The mandated Jackson 2.15.2 was replaced by 2.21.8 because of HIGH CVEs. Jackson is now 3.1.8 under Boot 4.  
- 📏 Step 1 changed 7 files (+85 / −65 lines). Step 2 changed 7 files (+195 / −6 lines), mostly in the new artifacts.  
  
## 5. ⏱️ Time Savings Estimate  
  
| Activity | Manual estimate | Automated |  
|----------|-----------------|-----------|  
| 🔍 Analysis, plan and version research | 3–4 h | ✅ |  
| ☕ Java 8 → 25 build and API fixes | 3–4 h | ✅ |  
| 🌱 Boot 1.2 → 3.5 → 4.0, including the Spring 6 filter API and the Logback fix | 8–12 h | ✅ |  
| 🧪 Bringing back the dormant Groovy/Spock suite on Groovy 5 | 4–6 h | ✅ |  
| 🛡️ CVE triage and BOM overrides | 2–3 h | ✅ |  
| 🚀 Smoke testing and documentation | 2–3 h | ✅ |  
| **Total** | **~22–32 h (3–4 developer days)** | **~2.2 h of agent time** |  
  
- 💰 **Estimated saving: about 20–30 engineer hours (roughly 90%)**  
- 📌 The figures are estimates for a ~600 LOC codebase that jumped three Spring Boot major versions and 17 Java versions. The manual numbers assume an engineer who knows Spring but not this codebase.  
  
## 6. 🚀 Next Steps  
  
**✅ Validation**  
- 🔁 Re-run `mvn -B clean install` on JDK 25 in CI and confirm the 13/13 Spock result  
- 🌐 Smoke-test the packaged jar: 301 redirect, 200 download, 304 for `If-None-Match` and `If-Modified-Since`, `HEAD`, and `/actuator/health`  
- ❓ Check the step 2 runtime smoke test again. `migration-results.yaml` records it as PASS, but the learnings note in the same log says the app was never booted. Step 1 did record a successful boot on Boot 3.5.16 (health 200, 301, 200, 304, 304, HEAD 200).  
- 🏷️ Check ETag behaviour against real clients, because the test-only `Sha512ShallowEtagHeaderFilter` now uses a different Spring API  
- 🔍 Review the deviation tables in `README.md` with the project owners. They cover Boot 3.5.16 instead of 3.2.12 and the Jackson version choices.  
  
**💡 Improvements**  
- 🗂️ Replace the `FileSystemStorage` stub before production. It returns `logback.xml` for any UUID, and `new File(getResource(...).getFile())` fails when the app runs from a packaged jar.  
- 🧹 Remove `build.log` from version control and add it to `.gitignore`. Decide whether the migration artifacts (`migration-plan.json`, `code-intelligence-report.md`, `migration-results.yaml`, `.migration-test-baseline.tsv`) should stay in the repo.  
- 🧹 Remove the direct `spring-*` dependencies that the starters already provide, and the unused `Integer temp` in `MainApplication.main`  
- 🔐 Add explicit actuator exposure and security settings if more endpoints than `health` are needed  
- 🔭 Before any later upgrade to Boot 4.1, run the 4.1 deprecation gate and apply its property and logging renames  
- 🧰 Consider configuring SpotBugs and Checkstyle. The build skips them, and neither plugin is set up in the project.

## Verification results

Copied from this iteration's report.json, progress.json and runtime-validation.log after the final build. This section is not written by the AI.

- 🛠️ Post-upgrade verification build: SUCCESS
- 🧪 Automated tests: 13 tests run
- ⏱️ Transformation duration: 19 minute(s)
- ⏳ Estimated time saved: 0 minute(s)
- 🔎 CodeKB calls (atx): 3

## Kiro credit usage

💳 Kiro CLI credits used: 6.49  
Includes 0.32 for job setup (startup smoke test and knowledge-base indexing).  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s

## Requirements register refresh

- Outcome: Refreshed
- Duration: 1.8s

## Feature Parity Verification

- Requirements evaluated: 29
- Satisfied: 29
- Regressions: 0
- Unverified: 0
- Behaviour-judged (changed requirements): 1

### Verdict coverage

Of 29 requirement(s): 0 test-backed, 1 behaviour-judged, 28 unchanged-by-construction, 0 unverified.

> **Test evidence.** No verdict is test-backed: this iteration's parity evidence carries no test results: the transformer that ran it did not report any.

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
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-download-retrieval-15e14161**: WHEN an HTTP request is received, the DownloadController SHALL return the existing file.
  - Acceptance criteria:
    - AC1: WHEN download runs, the DownloadController SHALL call findExistingFile and handle.
    - AC2: WHEN download is invoked, the DownloadController SHALL accept method, uuid, requestEtagOpt and ifModifiedSinceOpt as input.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
- **FR-find-existing-file-9efb6079**: WHEN a caller invokes findExistingFile, the DownloadController SHALL return the existing file for the given method and uuid.
  - Acceptance criteria:
    - AC1: WHEN findExistingFile runs, the DownloadController SHALL call findFile.
    - AC2: WHEN findExistingFile completes normally, the DownloadController SHALL return a value of type Optional&lt;ExistingFile>.
    - AC3: WHEN findExistingFile is invoked, the DownloadController SHALL accept method of type HttpMethod and uuid of type UUID.
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
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
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
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
  - Verdict: satisfied (evidence: unchanged)
  - Reasoning: The implementing code did not change between the pre- and post-upgrade codebase, so the behaviour is preserved by construction.
  - Caveat: Not executed at runtime; rests on the implementing location being unchanged, not on a passing test.
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
  - Reasoning: Behaviour judge (confidence: high) ruled the change behaviour-preserving: The main method still performs the same operation: parsing "1234" into an Integer and starting the Spring application. Replacing 'new Integer("1234")' with 'Integer.valueOf("1234")' is a behavior-preserving change (avoids deprecated constructor, same parsed value), and SpringApplication.run is unchanged. The observable behaviour of invoking main is preserved.
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
