# 🚀 Java 25 Upgrade: Executive Report (Iteration 24)  
  
**Project:** `com.nurkiewicz.download:download-server`, a Spring Boot file download server with ETag/Last-Modified caching and throttled streaming  
**Run ID:** `20261102_044402_b1ae49d5` · **Region:** ap-southeast-2 · **Branch:** `atx-result-staging-20261102_044402_b1ae49d5`  
  
---  
  
## 📋 1. Executive Summary  
  
AWS Transform Custom (`AWS/java-version-upgrade`) moved the download server from Java 8 / Spring Boot 1.2.3 to Java 25 / Spring Boot 3.5.16 in about 44 agent minutes, using 2 commits that touched 5 files. With Corretto 25.0.5 the project builds (`BUILD SUCCESS`, `javac [release 25]`) and the application starts. A real download request returned HTTP 200, and `/actuator/health` returned 200. The validation verdict is **PARTIAL**, and the CLI run ended with a `FAILURE` status event. The one unmet criterion is mandated versions: Spring Boot 3.2.12 cannot read Java 25 class files, so the agent moved up to 3.5.16, which also brings Jackson 2.21.4 instead of the mandated 2.15.2. Neither change is a downgrade.  
  
- ✅ Compiles and installs on Java 25  
- ✅ App boots on Java 25 (Tomcat on 8080, started in about 4.3 s)  
- ✅ No `javax.*` usage, so no Jakarta migration was needed  
- ⚠️ Spring Boot 3.5.16 and Jackson 2.21.4 replace the mandated 3.2.12 and 2.15.2 (documented)  
- ⚠️ 0 tests run before and after the upgrade because the Spock spec is never compiled  
  
---  
  
## 🏗️ 2. Application Changes  
  
**Before the upgrade, per CodeKB and the baseline commit `73556be`:**  
- 🌐 `DownloadController` serves `GET`/`HEAD /download/{uuid}/{filename}` and reads the `If-None-Match` and `If-Modified-Since` headers  
- 📁 `ExistingFile` returns a 304 for cached content, a 200 with a throttled stream (Guava `RateLimiter`, 1 MB/s), or a 301 redirect  
- 🔑 `FileSystemPointer` builds the ETag from a SHA-512 hash of the file (Guava) and detects the media type  
- 🧪 The test helper `Sha512ShallowEtagHeaderFilter` extends Spring's `ShallowEtagHeaderFilter` to produce SHA-512 ETags  
  
**What changed:**  
  
| Area | Before | After |  
|---|---|---|  
| ☕ Java | 1.8 (`source`/`target`) | 25 (`release`) |  
| 🌱 Spring Boot | 1.2.3.RELEASE | 3.5.16 |  
| 🌱 Spring Framework | 4.1.6.RELEASE (pinned) | 6.x (managed by the Boot BOM) |  
| 🔧 maven-compiler-plugin | 3.0 | 3.13.0 |  
| 📦 Guava | 18.0 | 33.7.2-jre |  
| 📦 commons-io | 2.4 | 2.17.0 |  
| 📦 commons-codec | 1.10 | 1.17.1 |  
| 📦 cglib-nodep | 3.1 | 3.3.0 |  
| 🧪 Spock | 1.0-groovy-2.4 | 2.3-groovy-4.0 |  
| 🧪 Groovy | `org.codehaus.groovy:groovy-all` 2.4.3 | `org.apache.groovy:groovy` 4.0.24 |  
| 🔒 Repositories | plaintext `http://repo.spring.io/milestone` | removed (everything resolves from Maven Central) |  
  
- 🧾 Files changed: `pom.xml`, `FileSystemPointer.java`, `MainApplication.java`, `logback.xml`, `Sha512ShallowEtagHeaderFilter.java` (33 lines added, 55 removed)  
  
---  
  
## 🛠️ 3. Tools Used  
  
- ☕ **Java SDK:** Amazon Corretto 25.0.5 is the target JDK. The Java 8 JDK (`java-1.8.0`) ran the baseline build.  
- 🤖 **AWS Transform Custom CLI (`atx`):** `atx custom def exec --transformation-name AWS/java-version-upgrade --non-interactive --trust-all-tools`. This is an AWS managed transformation. It planned the work (`plan.json`), made the edits, committed each step to a staging branch, and wrote `worklog.log` and `validation_summary.md`.  
- 🧱 **Apache Maven 3.9.16:** used for builds, `dependency:tree` and `dependency:build-classpath`  
- 🔍 **Maven Central metadata via `curl`:** used to pick the latest clean versions (Guava 33.3.1-jre was rejected because of a flagged HIGH CVE)  
- 🌐 **`curl` smoke tests:** used to check the download endpoint and the actuator health endpoint  
- 🗺️ **CodeKB:** used to map the pre-upgrade download, caching and ETag flow for this report  
  
---  
  
## 💻 4. Code Changes  
  
- 🔑 **`FileSystemPointer`:** Guava removed `Files.hash(File, HashFunction)`.  
  - Before: `Files.hash(target, Hashing.sha512())`  
  - After: `Files.asByteSource(target).hash(Hashing.sha512())`  
  - ETag values stay the same.  
- 🔢 **`MainApplication`:** the deprecated boxing constructor `new Integer("1234")` became `Integer.valueOf("1234")`.  
- 🧪 **`Sha512ShallowEtagHeaderFilter` (test):** updated for the Spring 6 API.  
  - Before: `generateETagHeaderValue(byte[])`  
  - After: `generateETagHeaderValue(InputStream, boolean isWeak)`, which reads the stream with `ByteStreams.toByteArray`  
  - Adds the `W/` prefix when the ETag is weak.  
- 📝 **`logback.xml`:** Logback 1.5.x rejects an unnamed appender nested inside `<root>`.  
  - Replaced it with a named `CONSOLE` appender and `<appender-ref>`.  
  - Kept the original log pattern.  
- 📦 **`pom.xml`:**  
  - Set `maven.compiler.release` and `java.version` to 25, and dropped the old `spring.version` property.  
  - Removed 13 hard-coded Spring and Boot version pins so the Boot parent BOM manages them.  
  - Removed the duplicate `spring-test` dependency and the `groovy-all` exclusion.  
  - Removed the insecure HTTP repository.  
- 🐞 **Issue found and resolved during the run:** Spring Boot 3.2.12 compiled but failed at startup with ASM `Unsupported class file major version 69`. Moving to 3.5.16 fixed it (recorded in commit `33b49d1`).  
  
---  
  
## ⏱️ 5. Time Savings Estimate  
  
- 🤖 **Automated effort:** 43.8 agent minutes. This covered the baseline, the plan, 2 verified commits, runtime smoke tests and the validation report.  
- 👩‍💻 **Estimated manual effort:** about 1.5 to 2 developer-days (12 to 16 hours):  
  - 🔍 Researching the jump from Spring Boot 1.x to 3.x and choosing versions: about 3 to 4 h  
  - 🧩 Fixing Guava and Spring 6 API breaks: about 2 h  
  - 🐞 Diagnosing the ASM major-version-69 startup failure and the Logback config error: about 3 to 4 h  
  - 🔒 Checking dependency CVEs: about 1 to 2 h  
  - ✅ Builds, smoke tests and documentation: about 3 to 4 h  
- 📈 **Net savings:** roughly 11 to 15 hours (about 90%) for this small codebase of 8 main and 4 test source files. The tool saved the most time on runtime-only failures that a successful compile does not reveal.  
  
---  
  
## 🔜 6. Next Steps  
  
**Validation:**  
- 🧪 Add `gmavenplus-plugin` so `DownloadControllerTest.groovy` actually compiles and runs on Spock 2.3 / JUnit 5. Today no tests execute.  
- 🔁 Test the HTTP caching behaviour end to end on Java 25:  
  - an `If-None-Match` request returns 304  
  - an `If-Modified-Since` request returns 304  
  - a `HEAD` request returns no body  
  - the 301 redirect works  
  - throttled downloads of large files work  
- 🔑 Confirm that ETag values match the values from the Java 8 build, so clients and CDNs keep their cache hits.  
- 🩺 Look at the actuator changes between Boot 1.x and 3.x (endpoints now live under `/actuator`, and exposure defaults changed). Update monitoring and probes to match.  
- 📦 Package the app with `spring-boot-maven-plugin` so it runs as an executable fat jar. The smoke tests ran from the classpath.  
  
**Improvements:**  
- 📜 Get the transformation owners to approve Spring Boot 3.5.16 and Jackson 2.21.4, or to update the mandated-version table for Java 25 targets.  
- 🧹 Remove explicit `spring-*` dependencies that the starters already bring in, and remove `cglib-nodep` if nothing uses it (Spring 6 ships its own repackaged CGLIB).  
- 🗑️ Remove the unused `Integer temp` variable from `MainApplication`.  
- 🛡️ Add a CVE scan to CI (for example OWASP Dependency-Check) and schedule regular dependency updates.  
- ⚡ Consider Java 21+ features such as virtual threads (`spring.threads.virtual.enabled=true`) for the blocking file-streaming I/O.  

## Kiro credit usage

💳 Kiro CLI credits used: 3.51  
Includes 0.09 for job setup (startup smoke test and knowledge-base indexing).  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
