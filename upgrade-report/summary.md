# 📊 Executive Report: Java 8 → Java 25 Upgrade (`download-server`, iteration 4)  
  
## 1. 📝 Executive Summary  
  
The `download-server` application (branch `oom-repro-274-upgrade`, commit `9aa2da9`) now compiles on Java 25 (Amazon Corretto 25.0.4.1) and all 12 Spock tests pass. That result was reached only with the OOM test fixture skipped (`-Dexec.skip=true`). None of it came from OpenRewrite: both recipe runs (the Java 25 leg and the "Spring Boot Upgrade to 4.0" leg) failed with `No space left on device` on the full root filesystem, so no recipe changes were applied. Kiro CLI then made all the changes by hand. It moved the app from Spring Boot 1.2.3 / Spring Framework 4.1.6 to Spring Boot 3.5.16 / Spring Framework 6.2.19 (Jakarta). ⚠️ **The Spring upgrade is incomplete.** The pipeline targeted Spring Boot 4.0, and the result stops at Boot 3.5.16, so the Spring track needs follow-up. Nothing has been committed.  
  
- ✅ **Java:** 1.8 → 25. The build compiles with 0 errors.  
- ✅ **Tests:** 12/12 pass with `mvn clean verify -Dexec.skip=true`.  
- ⚠️ **Spring:** Boot 1.2.3 → 3.5.16 and Framework 4.1.6 → 6.2.19. **Incomplete:** the Boot 4.0 target was not reached.  
- ❌ **OpenRewrite:** 0 recipes applied. Both runs ended in `BUILD FAILURE` because the disk was full.  
- 🔍 **Missing evidence:** the log has no `Verified: effective Spring Framework version...` line and no `WARNING: ...still < 5.3` line. The OpenRewrite `SourcesFileResults` data tables were never produced, and both stats directories are empty. The effective Spring version above comes from Kiro's `mvn dependency:list` output.  
  
## 2. 🏗️ Application Changes  
  
- 📦 **Build (`pom.xml`):**  
  - The Spring Boot parent and the Java compiler level were upgraded.  
  - Dependency versions are now managed by the Spring Boot BOM.  
  - Test tooling was modernised.  
- ☕ **Runtime platform:** Java 8 → Java 25 (`<release>25</release>`).  
- 🌱 **Framework:** Spring 4.1 → Spring 6.2 / Jakarta. The application code uses no `javax.*` APIs, so no namespace rewrites were needed.  
- 🧪 **Test suite now runs:**  
  - Before: the Spock spec under `src/test/groovy` was never compiled or executed.  
  - Now: it compiles and all 12 tests run.  
- 🪵 **Logging:** the `logback.xml` configuration was fixed. Under Logback 1.5 the old config produced no log output at all.  
- 🧹 **Cleanup:**  
  - Removed the duplicate `spring-test` declaration that Maven warned about.  
  - Removed the unused `cglib-nodep`.  
  - Removed the stale `spring.version` property.  
- 📁 **Files modified:** 5 files, 62 insertions and 60 deletions (from `git diff --stat`).  
  
## 3. 🛠️ Tools Used  
  
- ☕ **Java SDK:** Amazon Corretto 25.0.4.1 (25.0.4.1+10-LTS) and Apache Maven 3.9.16.  
- 🔄 **OpenRewrite:**  
  - Tooling: `rewrite-maven-plugin` 6.42.0 (`rewrite-java` 8.85.0).  
  - Java 25 leg: ran for 06:08 min and ended in ❌ `BUILD FAILURE` (disk full). The template-marker check came back clean.  
  - Spring Boot 4.0 leg: ran for 01:46 min and ended in ❌ `BUILD FAILURE` (disk full).  
  - Applied changes: none. `parity-evidence.json` shows `"recipes": []`.  
- 🤖 **Amazon Kiro CLI:**  
  - Version and model: `kiro-cli` 2.14.2 running **claude-opus-5.5**.  
  - Work done: the build fixes, the manual Java and Spring upgrade, and the root-cause debugging.  
  - Session cost: 9 min 9 s and 17.59 credits.  
  - Knowledge base: queried for Java 25 / Spring Boot / Spock compatibility.  
  
## 4. 💻 Code Changes  
  
- 📄 **`pom.xml`:**  
  - 🔼 Parent `spring-boot-starter-parent` 1.2.3.RELEASE → **3.5.16**.  
  - 🔼 `maven-compiler-plugin`: `source/target 1.8` → `<release>25</release>`. The version is now managed by Boot (it was pinned at 3.0).  
  - 🔁 All hard-coded `4.1.6.RELEASE` and `1.2.3.RELEASE` versions were removed, so the Boot BOM manages them. Resolved versions: Spring 6.2.19, commons-codec 1.18.0, Groovy 4.0.32.  
  - 🔼 Spock 1.0-groovy-2.4 → **2.4-groovy-4.0**.  
  - 🔼 `org.codehaus.groovy:groovy-all` 2.4.3 → `org.apache.groovy:groovy` 4.0.32.  
  - 🔼 Guava 18.0 → 33.4.0-jre.  
  - 🔼 commons-io 2.4 → 2.18.0.  
  - ➕ `gmavenplus-plugin` 4.3.1 (`compileTests`) so the Spock spec gets compiled.  
  - ➕ `maven-dependency-plugin` `properties` goal, and a Surefire `argLine` that loads Mockito as `-javaagent`. JDK 21+ blocks Mockito's dynamic self-attach.  
  - ➕ Surefire now includes `**/*Spec.java`.  
  - 🔒 Spring milestone repository URL changed from `http` to `https`.  
  - ➖ Removed `cglib-nodep`, the duplicate `spring-test`, and `spring.version`.  
  - ⏸️ The VUTK-274 `exec-maven-plugin` OOM fixture was left **unchanged**.  
- ☕ **`MainApplication.java`:** `new Integer("1234")` → `Integer.valueOf("1234")`. The constructor is deprecated for removal.  
- 🧪 **`Sha512ShallowEtagHeaderFilter.java` (test):** now overrides the Spring 5+ hook `generateETagHeaderValue(InputStream, boolean)` and adds the weak-ETag prefix.  
- 🧪 **`DownloadControllerTest.groovy`:** removed `final` from `TEXT_FILE`.  
  - Groovy 4 constant-folded that field at compile time, which baked one random UUID into the class.  
  - As a result, 11 of the 12 tests got 404s.  
- 🪵 **`logback.xml`:** the console appender now has a name and `<root>` references it with `<appender-ref>`.  
  
## 5. ⏱️ Time Savings Estimate  
  
- 🧮 **Scope:**  
  - A major Java jump (8 → 25).  
  - Two Spring generations (Boot 1.x → 3.x).  
  - A Groovy/Spock major upgrade (2.4 → 4.0 / 1.0 → 2.4).  
  - Three non-obvious runtime failures: Mockito agent attach, Logback config, and Groovy constant folding.  
- 👩‍💻 **Manual estimate:** about **12–20 engineer-hours**. That covers version research, POM rework, and debugging the Mockito/JDK 25, Logback 1.5 and Groovy 4 issues, including bytecode inspection with `javap`.  
- 🤖 **Automated effort:**  
  - Kiro session: about 9 minutes.  
  - Whole pipeline: about 22 minutes (12:22 → 12:44 UTC).  
- 💰 **Estimated saving:** about **11–19 hours (~90%+)**.  
  - All of the saving comes from Kiro CLI.  
  - OpenRewrite saved no time in this iteration because it applied nothing.  
- ⚠️ **Caveat:** the Boot 4.0 follow-up work is not included and would add effort.  
  
## 6. 🚀 Next Steps  
  
### ✅ Validation  
- 🌱 **Finish the Spring upgrade (incomplete).**  
  - Move from Boot 3.5.16 to the **Spring Boot 4.0 / Spring Framework 7** target. This includes the Spock `groovy-5.0` variant.  
  - Boot 3.5 open-source support is ending.  
- 🔁 **Re-run OpenRewrite on a host with free disk space.** On this run the root filesystem `/` (which holds `/tmp` and `~/.m2`) was 100% full. Check the following:  
  - The `Verified: effective Spring Framework version...` line appears.  
  - The `SourcesFileResults` data tables are produced.  
  - The recipes don't conflict with the manual changes.  
- 🧪 **Run the full build without `-Dexec.skip=true`.** The VUTK-274 OOM fixture (`head -c 6G /dev/zero | tail` bound to `validate`) still runs on every lifecycle build. Confirm the behaviour you expect under the 12 GB container limit.  
- 🌐 **Smoke-test the running app on Java 25.** Check the download endpoints, ETag / `If-Modified-Since` handling, and the actuator endpoints. Actuator paths changed between Boot 1.x and 3.x.  
- 👀 **Review the diff and commit.** All 5 changed files are uncommitted. Don't commit the `target/` directory.  
  
### 💡 Improvements  
- 🔄 Replace the Guava `com.google.common.base.Optional` in `ExistingFile.toMediaType` with `java.util.Optional`.  
- 🧹 Drop the explicit `spring-*` artifact declarations that the Boot starters already bring in.  
- 💾 Give the CI and transform environments enough disk space, or point `maven.repo.local` and `java.io.tmpdir` at a volume with free space.  
- 🚫 Keep the OOM test fixture out of `main`, as its commit message requires.  

## Kiro credit usage

💳 Kiro CLI credits used: 41.57  
