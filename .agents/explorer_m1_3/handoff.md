# Handoff Report: Build Configuration & Verification Strategy (Milestone M1)

- **Agent**: explorer_m1_3 (teamwork_preview_explorer)
- **Working Directory**: `C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator\.agents\explorer_m1_3`
- **Target Project**: `C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator`
- **Milestone**: M1 (Iteration 1)

---

## 1. Observation

### 1.1 Existing Root Configuration & Java Environment
1. **`gradle.properties`** at root (`C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator\gradle.properties`):
   - Currently contains 26 lines.
   - Line 9 sets: `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`.
   - Lines 17, 19, 23-25 set: `android.useAndroidX=true`, `kotlin.code.style=official`, `android.nonTransitiveRClass=true`, `android.defaults.buildfeatures.buildconfig=true`, `android.nonFinalResIds=false`.
   - **`org.gradle.java.home` is currently absent**.
2. **OpenJDK 21 LTS Installation Directory** (`C:\Program Files\Android\openjdk\jdk-21.0.8`):
   - Directly verified directory structure: contains `bin/`, `conf/`, `include/`, `jmods/`, `legal/`, `lib/`, `release`, and `source.properties`.
   - Verbatim contents of `C:\Program Files\Android\openjdk\jdk-21.0.8\release`:
     ```properties
     IMPLEMENTOR="Microsoft"
     IMPLEMENTOR_VERSION="Microsoft-11933218"
     JAVA_RUNTIME_VERSION="21.0.8+9-LTS"
     JAVA_VERSION="21.0.8"
     JAVA_VERSION_DATE="2025-07-15"
     OS_NAME="Windows"
     ```
3. **Gradle Wrapper & Build Stack**:
   - `gradle/wrapper/gradle-wrapper.properties`: `distributionUrl=https\://services.gradle.org/distributions/gradle-8.12-bin.zip`.
   - Root `build.gradle.kts`: AGP `8.8.0`, Kotlin `2.1.0`, Compose compiler plugin `2.1.0`.
   - `app/build.gradle.kts`: `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`, `jvmTarget = JavaVersion.VERSION_17.toString()`, Compose BOM `2024.12.01`, JUnit `4.13.2`.
   - `local.properties`: `sdk.dir=C\:\\Users\\yalan\\AppData\\Local\\Android\\Sdk`.
4. **App Source & Resource Directories**:
   - `app/src/main/res/drawable`: `money.xml`, `percent.xml` are present.
   - `app/src/main/res/values/strings.xml`: `calculate_tip`, `bill_amount`, `tip_amount`, `how_was_the_service`, `round_up_tip` are present.
   - `app/src/test`: directory does **not** exist yet; must be created by the Worker (`app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`).

---

## 2. Logic Chain

1. **JDK 21 Determinism**:
   - AGP 8.8.0 requires JDK 17 minimum. OpenJDK 21 LTS is installed at `C:\Program Files\Android\openjdk\jdk-21.0.8`.
   - Gradle properties support `org.gradle.java.home` to define the JDK for the Gradle daemon.
   - In Java `.properties` file parsing, backslashes (`\`) are escape characters. If unescaped backslashes are written (e.g. `C:\Program...`), the properties parser can misinterpret escape sequences (`\P`, `\A`, `\o`, `\j`).
   - Forward slashes (`/`) are fully supported by Gradle and Java file APIs across Windows.
   - Therefore, the exact line to append to root `gradle.properties` is:
     ```properties
     org.gradle.java.home=C:/Program Files/Android/openjdk/jdk-21.0.8
     ```
   - This ensures the Gradle daemon automatically launches with OpenJDK 21 LTS regardless of user/system environment variables, terminal shells, or subagent process context.

2. **Escalating Verification Ladder**:
   - Compiling the whole application and packaging APKs before verifying unit tests slows down the feedback loop. An escalating 3-command ladder ensures fast fail and complete validation:
     - **Command 1: `.\gradlew testDebugUnitTest`**: Executes JVM unit tests (`TipCalculatorTests.kt`) via JUnit 4 on the host JVM. Runs in seconds, verifies calculation logic and boundary conditions before APK packaging.
     - **Command 2: `.\gradlew assembleDebug`**: Compiles Jetpack Compose UI code, processes XML resources/drawables/strings, and packages the debug APK (`app-debug.apk`). Verifies that the Compose compiler plugin and Android packaging succeed.
     - **Command 3: `.\gradlew build`**: Runs the complete lifecycle verification (unit tests, lint tasks, and variant assemblies) to guarantee production-grade project integrity.

3. **Acceptance Criteria Definition**:
   - Clear, verifiable criteria are necessary to prevent incomplete submissions from the Worker and give Reviewers (Code Reviewer, Test Reviewer, Challenger, Forensic Auditor) an unambiguous verification checklist.

---

## 3. Caveats

1. **Shell Syntax in Windows PowerShell**:
   - The user's shell is PowerShell. The Gradle wrapper must be invoked with relative path notation `.\gradlew` or `.\gradlew.bat` (not `gradlew` unless `.` is in `PATH`).
2. **JVM Test Scope vs Connected Tests**:
   - Android instrumentation tests (`androidTest/`) require an active emulator or device. Milestone M1 verification strictly targets JVM unit tests (`testDebugUnitTest`). Instrumentation tests are out of scope for M1.
3. **Locale-Aware Number Formatting**:
   - `calculateTip()` uses `NumberFormat.getCurrencyInstance().format(tip)`, which formats according to the host system locale (e.g. currency symbol position, space, comma/dot separators).
   - In `TipCalculatorTests.kt`, assertions must compare against `NumberFormat.getCurrencyInstance().format(...)` rather than a hardcoded string `"$2.00"` to prevent false test failures on machines with non-US regional settings.

---

## 4. Conclusion

### 4.1 Exact `gradle.properties` Configuration
In `C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator\gradle.properties`, append:
```properties
# OpenJDK 21 LTS location for deterministic Gradle builds
org.gradle.java.home=C:/Program Files/Android/openjdk/jdk-21.0.8
```

### 4.2 Exact Verification Command Sequence
Run from project root `C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator`:

```powershell
# Step 1: Run JVM unit tests
.\gradlew testDebugUnitTest

# Step 2: Compile Compose UI and build debug APK
.\gradlew assembleDebug

# Step 3: Run comprehensive full build and verification
.\gradlew build
```

### 4.3 Worker Acceptance Criteria Checklist
Before the Worker submits its handoff, all of the following criteria must be met:

| ID | Category | Check | Success Condition |
|---|---|---|---|
| **AC-ENV-1** | Environment | `gradle.properties` configuration | `org.gradle.java.home=C:/Program Files/Android/openjdk/jdk-21.0.8` is present. |
| **AC-CODE-1** | Implementation | `MainActivity.kt` Structure | Implements `enableEdgeToEdge()`, `TipTimeLayout`, `EditNumberField`, `RoundTheTipRow`, and `@VisibleForTesting internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String`. |
| **AC-CODE-2** | Implementation | Compose State Hoisting | State (`amountInput`, `tipInput`, `roundUp`) hoisted to `TipTimeLayout`; child composables are stateless. |
| **AC-CODE-3** | Implementation | Input Safety & Resources | Uses `toDoubleOrNull() ?: 0.0` (or `15.0` default for tip percent), references `R.string.*` and `R.drawable.*` correctly. |
| **AC-TEST-1** | Unit Tests | `TipCalculatorTests.kt` created | Located at `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`. |
| **AC-TEST-2** | Unit Tests | Test Coverage | Tests `calculateTip_20PercentNoRoundup` (10.00, 20.0, false), `calculateTip_20PercentRoundup` (10.01 or 10.00, 20.0, true), and zero amount edge case using locale-safe `NumberFormat`. |
| **AC-BUILD-1** | Verification | `testDebugUnitTest` | Exits with code 0 (`BUILD SUCCESSFUL`), all unit tests pass, report exists at `app/build/reports/tests/testDebugUnitTest/index.html`. |
| **AC-BUILD-2** | Verification | `assembleDebug` | Exits with code 0 (`BUILD SUCCESSFUL`), APK produced at `app/build/outputs/apk/debug/app-debug.apk`. |
| **AC-BUILD-3** | Verification | `build` | Exits with code 0 (`BUILD SUCCESSFUL`), no compilation or lint breakages. |
| **AC-DISC-1** | Layout Discipline | File Boundaries | Source files modified strictly in `gradle.properties`, `MainActivity.kt`, and `TipCalculatorTests.kt`. No source code or build outputs in `.agents/`. |

---

## 5. Verification Method

To independently verify the build configuration and verification commands:

1. **Verify `gradle.properties`**:
   ```powershell
   Select-String -Path "C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator\gradle.properties" -Pattern "org.gradle.java.home"
   ```
   *Expected*: `org.gradle.java.home=C:/Program Files/Android/openjdk/jdk-21.0.8`.

2. **Verify Unit Tests Execution**:
   ```powershell
   cd "C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator"
   .\gradlew testDebugUnitTest
   ```
   *Expected*: `BUILD SUCCESSFUL`, `app/build/test-results/testDebugUnitTest/*.xml` reports 0 failures.

3. **Verify Debug APK Generation**:
   ```powershell
   .\gradlew assembleDebug
   ```
   *Expected*: `BUILD SUCCESSFUL`, file `app/build/outputs/apk/debug/app-debug.apk` exists with size > 0.

4. **Verify Full Build**:
   ```powershell
   .\gradlew build
   ```
   *Expected*: `BUILD SUCCESSFUL`.

5. **Invalidation Conditions**:
   - `gradle.properties` uses unescaped backslashes or wrong JDK path.
   - Gradle daemon reports JDK version mismatch or unsupported class file version.
   - Any test failure in `testDebugUnitTest`.
   - `assembleDebug` or `build` fails due to un-imported Compose symbols or missing drawables.
