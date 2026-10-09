# 📊 Executive Report: Java 25 + Spring Boot 4.0 Upgrade (download-server)  
  
## 1. 🧭 Executive Summary  
- 🚀 We upgraded the `download-server` module from Java 8 / Spring Boot 1.2.3 / Spring Framework 4.1.6 to **Java 25 / Spring Boot 4.0.8 / Spring Framework 7.0.9**.  
- 🤖 Two OpenRewrite passes did the bulk of the work: `UpgradeToJava25` (BUILD SUCCESS in 40.9 s) and `UpgradeSpringBoot_4_0` (BUILD SUCCESS in 11:01 min).  
- 🛠️ Kiro CLI then fixed the one remaining test compile error.  
- ✅ Final result: `mvn clean verify` reports **BUILD SUCCESS** on JDK 25, with zero compilation errors.  
- 🌱 **The Spring upgrade is complete.** The log has no "Verified: effective Spring Framework version..." line and no "still < 5.3" warning. Instead, I confirmed the version from Maven's resolved dependency list: `spring-core` and `spring-web` resolve to **7.0.9** and `spring-boot` to **4.0.8**.  
- ⚠️ **No tests ran.** The only test suite is a Groovy Spock spec that the build doesn't compile, and that was already true before the upgrade.  
  
## 2. 🧩 Application Changes  
- ☕ **Java baseline:** `source/target 1.8` → `<release>${java.version}</release>` with `java.version=25`.  
- 🌱 **Spring Boot parent:** `1.2.3.RELEASE` → `4.0.8`. OpenRewrite stepped through every intermediate Boot version (2.0 → 2.7 → 3.0 → 3.5 → 4.0).  
- 🔁 **Starter rename (Boot 4):** `spring-boot-starter-web` → `spring-boot-starter-webmvc`.  
- 🏷️ **Namespace:** moved to Jakarta EE (`jakarta.servlet-api` added as a test dependency). This is correct here because Spring Boot 4 uses `jakarta.*`.  
- 📦 **Dependency management:** OpenRewrite removed hard-coded Spring `4.1.6.RELEASE` and Boot `1.2.3.RELEASE` versions so the Boot BOM manages them.  
  - It removed the duplicate `spring-test` declaration that Maven had flagged as malformed.  
  - It removed explicit versions for `commons-codec` and `groovy-all`. `groovy-all` is now pinned to 2.4.16 in `dependencyManagement`.  
  - It bumped `guava` from 18.0 to 29.0-jre.  
- 🧪 **Test setup:**  
  - JUnit 4 was excluded from `spock-core`.  
  - A Mockito/ByteBuddy `-javaagent` was added to the surefire configuration (`AddMockitoJavaAgentToMavenSurefirePlugin`).  
  - The `maven-dependency-plugin` `properties` goal was added so that agent path can be resolved.  
- 🔧 **Build plugins:** `maven-compiler-plugin` 3.0 → 3.16.0.  
  
## 3. 🧰 Tools Used  
- ☕ **Java SDK:** Amazon Corretto 25.0.4.1 (25.0.4.8.1 LTS), with Apache Maven 3.9.13.  
- 🔄 **OpenRewrite recipes:**  
  - `com.amazonaws.java.migrate.UpgradeToJava25`, which wraps the official `UpgradeToJava25`, chained through Java 8 → 11 → 17 → 21.  
  - `com.amazonaws.java.spring.boot4.UpgradeSpringBoot_4_0`, which wraps the official `UpgradeSpringBoot_4_0` and includes `UpgradeSpringFramework_5_0` through `_6_0`, `JakartaEE10` and `JUnit4to5Migration`.  
- 🤖 **Amazon Kiro CLI:** `kiro-cli 2.14.2`, model **claude-opus-5.5**, run non-interactively.  
  - It used CodeKB to look up how the pre-upgrade code worked.  
  - It then fixed the build and wrote this report. The fix session took about 1m 51s.  
  
## 4. 💻 Code Changes  
- 📝 **`MainApplication.java`** (OpenRewrite, `PrimitiveWrapperClassConstructorToValueOf`): `new Integer("1234")` → `Integer.valueOf("1234")`. The `Integer(String)` constructor is deprecated for removal.  
- 📝 **`DownloadController.java`** (OpenRewrite, `NoAutowiredOnConstructor`): removed the redundant `@Autowired` on the single constructor and its unused import.  
- 📝 **`pom.xml`** (OpenRewrite): all the build and dependency changes listed in section 2. These came from 15 `UpgradeParentVersion` results, 8 `UpgradeDependencyVersion` results, plus `ChangeDependency`, `AddDependency`, `ExcludeDependency`, `RemoveExclusion` and `RemoveDuplicateDependencies`.  
- 🛠️ **`src/test/java/org/springframework/web/filter/Sha512ShallowEtagHeaderFilter.java`** (Kiro):  
  - **Problem:** it no longer compiled. Spring removed `generateETagHeaderValue(byte[])` and replaced it with `generateETagHeaderValue(InputStream, boolean isWeak)`.  
  - **Fix:** the override now uses the new signature, still hashes with SHA-512, and adds the `W/` prefix when Spring asks for a weak ETag.  
- 📂 **Scope:** 3 files changed by OpenRewrite and 1 by Kiro. No business logic changed. The download, ETag/If-Modified-Since caching and throttling code is untouched.  
  
## 5. ⏱️ Time Savings Estimate  
- 🤖 **OpenRewrite's own estimate:** about 1h for Java 25 plus about 2h 32m for Spring Boot 4.0, so roughly **3.5h**.  
- 🧠 **My estimate for doing it by hand:** about **1–2 working days**. That covers researching a four-major-version Boot jump (1.x → 4.0), the Jakarta switch, the starter renames, reconciling BOM-managed versions, and tracking down the ETag filter API change.  
- ⚡ **Actual automated runtime:** about 13 minutes (recipes 11.7 min plus Kiro about 2 min).  
  
## 6. 🔜 Next Steps  
- 🧪 **Get the Spock tests running again (highest priority).**  
  - Add `gmavenplus-plugin` to the build.  
  - Move from Spock `1.0-groovy-2.4` / Groovy 2.4.16 to Spock 2.x (groovy-4.0) with `org.apache.groovy:groovy` 4.x.  
  - Spock 2 runs on the JUnit Platform, so the surefire configuration needs to match.  
  - Until this is done, nothing verifies the download behaviour after the upgrade.  
- 🔍 **Check that the app works at runtime.** Run integration or smoke tests in dev/test for:  
  - `GET`/`HEAD /download/{uuid}/{name}`  
  - 304 responses for ETag and If-Modified-Since  
  - the 301 redirect  
  - Actuator endpoints. Boot 4 changed their default exposure and paths.  
- 🧹 **Clean up `pom.xml`:**  
  - Remove the unused `spring.version=4.1.1.RELEASE` property.  
  - Remove the `http://repo.spring.io/milestone/` repository, which Maven's HTTP blocker rejects.  
  - Remove the empty `<argLine></argLine>` placeholder if it isn't needed.  
- 📦 **Update old dependencies:**  
  - Drop `cglib-nodep` 3.1. Spring repackages CGLIB internally.  
  - Consider upgrading `commons-io` 2.4 and `guava` 29.0-jre.  
- ⚠️ **Fix the startup warning:** a dependency (Caffeine 2.9.3) calls deprecated `sun.misc.Unsafe` methods on JDK 25. Check this on the runtime classpath.  
- 🧱 **Fix a pre-existing bug:** `FileSystemStorage.findFile` always serves `/logback.xml`, whatever the requested UUID. This was in the code before the upgrade. Look into it before using the service in production.  
- 📋 **Code review:** review the OpenRewrite diff and commit it to the `main-upgrade` branch.  

## Credit usage

💳 Kiro CLI credits used: 12.32  

## Codebase index refresh

- Outcome: Index current
- Reason: Index current at 73556bed1b36e07af017a930ee46d5f994aa51cc.
- Previously indexed commit: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Source commit at transformation start: 73556bed1b36e07af017a930ee46d5f994aa51cc
- Duration: 0.0s
