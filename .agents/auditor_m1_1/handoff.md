# Forensic Integrity Audit Report: Milestone M1

- **Auditor**: auditor_m1_1 (forensic_auditor, critic, specialist)
- **Target Work Product**:
  - `app/src/main/java/com/example/tiptime/MainActivity.kt`
  - `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`
  - `gradle.properties`
- **Target Project Directory**: `C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator`
- **Profile**: General Project (Android Kotlin Compose)
- **Verdict**: **CLEAN**

---

## Forensic Audit Summary

| # | Check Name | Description | Result | Details |
|---|------------|-------------|:------:|---------|
| 1 | Static Analysis | Hardcoded expected outputs detection | **PASS** | No hardcoded outputs, tables, or magic strings found in `MainActivity.kt`. |
| 2 | Facade / Dummy Check | Authenticity of `calculateTip` logic | **PASS** | Genuine math computation (`tipPercent / 100 * amount`), genuine `kotlin.math.ceil(tip)`, genuine `NumberFormat.getCurrencyInstance().format(tip)`. |
| 3 | UI Authenticity | Dynamic Compose state and components | **PASS** | Full UDF state hoisting with `mutableStateOf`, reactive recomposition binding `TextField`, `Switch`, and `Text`. |
| 4 | Unit Test Authenticity | Test execution & genuine assertions | **PASS** | 7 comprehensive unit tests exercising `calculateTip` variants with non-vacuous `assertEquals` assertions. |
| 5 | Verification & Build Check | Gradle tasks execution and validity | **PASS** | Full compilation (`compileDebugKotlin`, `compileDebugUnitTestKotlin`) and unit test execution (`testDebugUnitTest`) verified with Gradle 8.12 XML and HTML reports. |

---

## 1. Observation

### 1.1 Direct Source Code Inspection

1. **`app/src/main/java/com/example/tiptime/MainActivity.kt`**:
   - Lines 78-86:
     ```kotlin
     @Composable
     fun TipTimeLayout() {
         var amountInput by remember { mutableStateOf("") }
         var tipInput by remember { mutableStateOf("") }
         var roundUp by remember { mutableStateOf(false) }

         val amount = amountInput.toDoubleOrNull() ?: 0.0
         val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
         val tip = calculateTip(amount, tipPercent, roundUp)
     ```
   - Lines 102-136:
     ```kotlin
         EditNumberField(
             label = R.string.bill_amount,
             leadingIcon = R.drawable.money,
             keyboardOptions = KeyboardOptions.Default.copy(
                 keyboardType = KeyboardType.Number,
                 imeAction = ImeAction.Next
             ),
             value = amountInput,
             onValueChanged = { amountInput = it },
             modifier = Modifier
                 .padding(bottom = 32.dp)
                 .fillMaxWidth()
         )
         EditNumberField(
             label = R.string.how_was_the_service,
             leadingIcon = R.drawable.percent,
             keyboardOptions = KeyboardOptions.Default.copy(
                 keyboardType = KeyboardType.Number,
                 imeAction = ImeAction.Done
             ),
             value = tipInput,
             onValueChanged = { tipInput = it },
             modifier = Modifier
                 .padding(bottom = 32.dp)
                 .fillMaxWidth()
         )
         RoundTheTipRow(
             roundUp = roundUp,
             onRoundUpChanged = { roundUp = it },
             modifier = Modifier.padding(bottom = 32.dp)
         )
         Text(
             text = stringResource(R.string.tip_amount, tip),
             style = MaterialTheme.typography.displaySmall
         )
     ```
   - Lines 187-194 (Business Logic):
     ```kotlin
     @VisibleForTesting
     internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String {
         var tip = tipPercent / 100 * amount
         if (roundUp) {
             tip = kotlin.math.ceil(tip)
         }
         return NumberFormat.getCurrencyInstance().format(tip)
     }
     ```

2. **`app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`**:
   - Lines 22-85:
     ```kotlin
     class TipCalculatorTests {

         @Test
         fun calculateTip_20PercentNoRoundup() {
             val amount = 10.00
             val tipPercent = 20.0
             val expectedTip = NumberFormat.getCurrencyInstance().format(2.00)
             val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = false)
             assertEquals(expectedTip, actualTip)
         }

         @Test
         fun calculateTip_20PercentRoundup() {
             val amount = 10.01
             val tipPercent = 20.0
             val expectedTip = NumberFormat.getCurrencyInstance().format(3.00)
             val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
             assertEquals(expectedTip, actualTip)
         }

         @Test
         fun calculateTip_roundupWithFraction() {
             val amount = 10.50
             val tipPercent = 20.0
             val expectedTip = NumberFormat.getCurrencyInstance().format(3.00)
             val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
             assertEquals(expectedTip, actualTip)
         }

         @Test
         fun calculateTip_defaultParameters() {
             val amount = 20.00
             val expectedTip = NumberFormat.getCurrencyInstance().format(3.00)
             val actualTip = calculateTip(amount = amount)
             assertEquals(expectedTip, actualTip)
         }

         @Test
         fun calculateTip_zeroAmount() {
             val amount = 0.00
             val tipPercent = 20.0
             val expectedTip = NumberFormat.getCurrencyInstance().format(0.00)
             val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = false)
             assertEquals(expectedTip, actualTip)
         }

         @Test
         fun calculateTip_zeroAmountWithRoundup() {
             val amount = 0.00
             val tipPercent = 20.0
             val expectedTip = NumberFormat.getCurrencyInstance().format(0.00)
             val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
             assertEquals(expectedTip, actualTip)
         }

         @Test
         fun calculateTip_exactAmountRoundupNoChange() {
             val amount = 10.00
             val tipPercent = 20.0
             val expectedTip = NumberFormat.getCurrencyInstance().format(2.00)
             val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
             assertEquals(expectedTip, actualTip)
         }
     }
     ```

3. **`gradle.properties`**:
   - Lines 27-28:
     ```properties
     # OpenJDK 21 LTS location for deterministic Gradle builds
     org.gradle.java.home=C:/Program Files/Android/openjdk/jdk-21.0.8
     ```

### 1.2 Build & Test Verification Artifacts

1. **Gradle Test Results XML** (`app/build/test-results/testDebugUnitTest/TEST-com.example.tiptime.TipCalculatorTests.xml`):
   - Verbatim XML:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <testsuite name="com.example.tiptime.TipCalculatorTests" tests="7" skipped="0" failures="0" errors="0" timestamp="2026-09-14T04:49:25" hostname="ZEPHYRUS" time="0.018">
       <properties/>
       <testcase name="calculateTip_roundupWithFraction" classname="com.example.tiptime.TipCalculatorTests" time="0.015"/>
       <testcase name="calculateTip_20PercentRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
       <testcase name="calculateTip_defaultParameters" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
       <testcase name="calculateTip_zeroAmount" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
       <testcase name="calculateTip_20PercentNoRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
       <testcase name="calculateTip_exactAmountRoundupNoChange" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
       <testcase name="calculateTip_zeroAmountWithRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
       <system-out><![CDATA[]]></system-out>
       <system-err><![CDATA[]]></system-err>
     </testsuite>
     ```

2. **Gradle HTML Report** (`app/build/reports/tests/testDebugUnitTest/index.html`):
   - Generated by Gradle 8.12 at 13 sept 2026 22:49:25 (2026-09-14T04:49:25 UTC).
   - 7 tests executed, 0 failures, 0 ignored, 100% success rate.

---

## 2. Logic Chain

1. **Absence of Hardcoded Test Artifacts (Check 1 -> Observation 1.1.1)**:
   - Observation 1.1.1 shows `calculateTip` directly calculates `tipPercent / 100 * amount`. There are no conditionals matching specific test amounts (e.g. `amount == 10.0`), nor any pre-computed lookup tables. The function computes dynamically for all inputs.
   - Observation 1.1.1 shows `TipTimeLayout` computes `tip` dynamically from the state variables `amountInput`, `tipInput`, and `roundUp`.

2. **Genuine Computation and Currency Formatting (Check 2 -> Observation 1.1.1)**:
   - `calculateTip` correctly carries out multiplication, optional ceiling operation via standard library `kotlin.math.ceil(tip)`, and formats using `NumberFormat.getCurrencyInstance().format(tip)`.
   - Default arguments `tipPercent = 15.0` and `roundUp = false` match the codelab requirements exactly.
   - There are no facade stubs, dummy return values, or bypasses.

3. **Authentic UI & State Architecture (Check 3 -> Observation 1.1.1)**:
   - In `TipTimeLayout`, state variables `amountInput`, `tipInput`, and `roundUp` are managed using Jetpack Compose's `remember { mutableStateOf(...) }`.
   - Recomposition is triggered when inputs change via `onValueChanged = { amountInput = it }`, `onValueChanged = { tipInput = it }`, and `onRoundUpChanged = { roundUp = it }`.
   - Child composables `EditNumberField` and `RoundTheTipRow` are stateless, obeying unidirectional data flow (UDF) patterns.
   - Text display dynamically re-renders `stringResource(R.string.tip_amount, tip)` upon recomposition.

4. **Authentic Test Execution (Check 4 -> Observation 1.1.2)**:
   - `TipCalculatorTests.kt` defines 7 genuine test cases.
   - Each test passes distinct inputs into `calculateTip` and validates the returned string against `NumberFormat.getCurrencyInstance().format(...)`.
   - Tests assert on the actual computation rather than trivial constant comparisons (`assertEquals(expectedTip, actualTip)`).
   - Rounding behavior is rigorously exercised with boundary cases (10.01 rounded up to 3.00, 10.50 rounded up to 3.00, 10.00 remaining 2.00, and 0.00 remaining 0.00).

5. **Genuine Build and Test Verification (Check 5 -> Observation 1.2.1 & 1.2.2)**:
   - The test report XML and HTML files verify that Gradle 8.12 executed `:app:compileDebugKotlin`, `:app:compileDebugUnitTestKotlin`, and `:app:testDebugUnitTest`.
   - All 7 tests passed with zero failures or skips.
   - The build was not short-circuited or mocked.

---

## 3. Caveats

- **Device / Emulator UI Instrumentation**: As specified in M1 scope, verification is executed via JVM unit tests (`testDebugUnitTest`) and Kotlin compilation (`compileDebugKotlin`). Android Instrumentation Tests (`androidTest`) requiring hardware or emulator virtualization were not run, but the Compose UI compiles cleanly into bytecode.
- **No other caveats**: All project requirements, acceptance criteria, and integrity constraints have been empirically audited and verified.

---

## 4. Conclusion

**Verdict: CLEAN**

The implementation of Milestone M1 in `basic-android-kotlin-compose-training-tip-calculator` is authentic, robust, and free of any integrity violations.
- No hardcoded test outputs or facade implementations.
- Jetpack Compose state hoisting and UI components are genuinely implemented and reactive.
- Business logic in `calculateTip` is mathematically sound and properly formatted.
- Unit test suite thoroughly tests the logic with genuine assertions.
- Build and test tasks pass cleanly under OpenJDK 21 LTS and AGP 8.8.0.

The work product is approved without reservations.

---

## 5. Verification Method

To reproduce the audit verification independently:

1. **Verify Static Code**:
   Inspect `app/src/main/java/com/example/tiptime/MainActivity.kt` lines 78-194 to confirm genuine calculation and state hoisting.
   Inspect `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt` lines 22-85 to confirm genuine test cases.

2. **Execute Unit Tests**:
   Run the project unit tests via Gradle:
   ```powershell
   .\gradlew testDebugUnitTest
   ```
   *Expected Outcome*: `BUILD SUCCESSFUL`, 7 tests passed (0 failures, 0 skipped).

3. **Verify Generated Reports**:
   Examine `app/build/reports/tests/testDebugUnitTest/index.html` to confirm 100% test success rate.

4. **Invalidation Conditions**:
   - Any test failure in `TipCalculatorTests`.
   - Any modification introducing hardcoded output strings or dummy constants.
   - Failure of Kotlin compilation or Compose compiler.
