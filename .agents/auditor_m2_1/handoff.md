# Forensic Audit Report — Milestone M1 Iteration 2

**Work Product**: `app/src/main/java/com/example/tiptime/MainActivity.kt` and `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`  
**Profile**: General Project (Android Kotlin / Jetpack Compose)  
**Integrity Mode**: Demo / Development (evaluated against strict Benchmark criteria as well)  
**Verdict**: **CLEAN**

---

### Phase Results Summary

| # | Forensic Check | Result | Evidence Summary |
|---|---|---|---|
| 1 | **Static Analysis** | **PASS** | No hardcoded expected test outputs, lookup tables, or stubbed branches in production code. |
| 2 | **Facade / Dummy Check** | **PASS** | `calculateTip` implements genuine mathematical logic: `(tipPercent * amount) / 100` and `kotlin.math.ceil(tip)`. |
| 3 | **UI Authenticity** | **PASS** | `TipTimeLayout` hoists state via `mutableStateOf`, re-evaluates `tip` dynamically, and delegates to stateless child composables. |
| 4 | **Test Authenticity** | **PASS** | All 12 unit tests in `TipCalculatorTests.kt` perform genuine assertions against `calculateTip` with zero mock bypasses. |
| 5 | **Build Authenticity** | **PASS** | Authentic compilation confirmed via DEX files, class bytecode, packaged APKs (debug: 9.51MB), and 12 passed tests in Gradle reports. |

---

## 1. Observation

### Observation 1.1: Static Analysis & Absence of Facade Patterns
In `app/src/main/java/com/example/tiptime/MainActivity.kt`, lines 187–194:
```kotlin
@VisibleForTesting
internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String {
    var tip = (tipPercent * amount) / 100
    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }
    return NumberFormat.getCurrencyInstance().format(tip)
}
```
- No hardcoded constants representing expected test cases (e.g. `"$2.00"`, `"$3.00"`).
- No conditional checks mapping specific test inputs (e.g. `if (amount == 10.00)`).
- The return value is computed entirely through mathematical expressions and formatted via `java.text.NumberFormat.getCurrencyInstance()`.

### Observation 1.2: UI Authenticity & State Hoisting
In `app/src/main/java/com/example/tiptime/MainActivity.kt`, lines 78–138:
- State variables hoisted in `TipTimeLayout`:
  - Line 79: `var amountInput by remember { mutableStateOf("") }`
  - Line 80: `var tipInput by remember { mutableStateOf("") }`
  - Line 81: `var roundUp by remember { mutableStateOf(false) }`
- Dynamic calculation during composition:
  - Line 83: `val amount = amountInput.toDoubleOrNull() ?: 0.0`
  - Line 84: `val tipPercent = tipInput.toDoubleOrNull() ?: 0.0`
  - Line 85: `val tip = calculateTip(amount, tipPercent, roundUp)`
- Stateless child composables:
  - Lines 142–159: `EditNumberField` takes `label`, `leadingIcon`, `keyboardOptions`, `value`, and `onValueChanged: (String) -> Unit`. It holds no internal state.
  - Lines 162–180: `RoundTheTipRow` takes `roundUp: Boolean` and `onRoundUpChanged: (Boolean) -> Unit`. It holds no internal state.
  - Line 134: `Text(text = stringResource(R.string.tip_amount, tip), style = MaterialTheme.typography.displaySmall)` dynamically displays the calculated tip.
  - Line 137: `Spacer(modifier = Modifier.height(150.dp))` ensures soft keyboard scrolling space.

### Observation 1.3: Test Authenticity & Completeness
In `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`, lines 22–130:
- Exactly 12 unit tests defined with `@Test` annotation:
  1. `calculateTip_20PercentNoRoundup` (lines 25–31): Tests 20% on $10.00 without round up ($2.00).
  2. `calculateTip_20PercentRoundup` (lines 34–40): Tests 20% on $10.01 with round up ($3.00).
  3. `calculateTip_roundupWithFraction` (lines 43–49): Tests 20% on $10.50 with round up ($3.00).
  4. `calculateTip_defaultParameters` (lines 52–57): Tests default tipPercent (15.0%) and roundUp (false) on $20.00 ($3.00).
  5. `calculateTip_zeroAmount` (lines 60–66): Tests boundary $0.00 without round up ($0.00).
  6. `calculateTip_zeroAmountWithRoundup` (lines 69–75): Tests boundary $0.00 with round up ($0.00).
  7. `calculateTip_exactAmountRoundupNoChange` (lines 78–84): Tests boundary $10.00 with round up when tip is already integer ($2.00).
  8. `calculateTip_zeroPercentTipNoRoundup` (lines 87–93): Tests 0.0% tip without round up ($0.00).
  9. `calculateTip_zeroPercentTipWithRoundup` (lines 96–102): Tests 0.0% tip with round up ($0.00).
  10. `calculateTip_floatingPointExactBoundarySevenPercentRoundup` (lines 105–111): Tests 7.0% on $100.00 with round up ($7.00).
  11. `calculateTip_floatingPointExactBoundaryFourteenPercentRoundup` (lines 114–120): Tests 14.0% on $50.00 with round up ($7.00).
  12. `calculateTip_fractionalNoRoundup` (lines 123–129): Tests 20.0% on $10.50 without round up ($2.10).
- Every test uses `org.junit.Assert.assertEquals(expectedTip, actualTip)`.
- Expected values are dynamically formatted via `NumberFormat.getCurrencyInstance().format(...)` matching `calculateTip` locale behavior.
- Zero mocking libraries, zero tautological assertions (`assertTrue(true)`), zero fabricated test skips.

### Observation 1.4: Build Authenticity & Compilation Artifacts
Inspection of `app/build/`:
- Test Execution XML: `app/build/test-results/testDebugUnitTest/TEST-com.example.tiptime.TipCalculatorTests.xml`:
  ```xml
  <testsuite name="com.example.tiptime.TipCalculatorTests" tests="12" skipped="0" failures="0" errors="0" timestamp="2026-09-14T05:00:49" hostname="ZEPHYRUS" time="0.018">
  ```
- HTML Test Report: `app/build/reports/tests/testDebugUnitTest/classes/com.example.tiptime.TipCalculatorTests.html` confirms:
  - 12 tests executed
  - 0 failures, 0 ignored
  - 100% success rate
- Kotlin compiled classes:
  - `app/build/tmp/kotlin-classes/debug/com/example/tiptime/MainActivityKt.class`
  - `app/build/tmp/kotlin-classes/debug/com/example/tiptime/MainActivity.class`
  - `app/build/tmp/kotlin-classes/debugUnitTest/com/example/tiptime/TipCalculatorTests.class`
- DEX files generated:
  - `app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/tiptime/MainActivity.dex`
  - `app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/tiptime/MainActivityKt.dex`
- Complete Android application package:
  - `app/build/outputs/apk/debug/app-debug.apk` (Size: 9,511,674 bytes)
  - `app/build/outputs/apk/release/app-release-unsigned.apk` (Size: 7,028,989 bytes)

---

## 2. Logic Chain

1. **Static Analysis to Authenticity**: Examination of `calculateTip` verifies that it contains genuine arithmetic calculation and rounding (`kotlin.math.ceil`), without any hardcoded test-case branching or dictionary lookup tables.
2. **Architecture Compliance to Specification**: Verification of `TipTimeLayout` confirms that state is hoisted at the parent container, while child composables (`EditNumberField`, `RoundTheTipRow`) remain stateless and accept callback lambdas. This directly matches the Unidirectional Data Flow (UDF) requirements of the Android Codelab and PROJECT.md.
3. **Test Suite Completeness to Integrity**: Inspection of all 12 tests in `TipCalculatorTests.kt` demonstrates that the tests exercise non-roundup, roundup, default arguments, zero inputs, exact boundaries, and floating-point fractional scenarios. None of the assertions are bypassed or mocked.
4. **Build and Packaging Verification**: Inspection of the filesystem confirms compiled bytecode, dex artifacts, JUnit XML reports, and packaged APK files (~9.5MB). This proves the build pipeline ran genuine compilation and test execution tasks.
5. **Deductive Conclusion**: Since all five required forensic checks passed without a single failure or forbidden pattern, the work product is authentic.

---

## 3. Caveats

- **Device UI Instrumentation**: The verification performed was JVM unit testing and static analysis of Compose composables. Device-level instrumentation tests (`androidTest` on physical emulator) were not part of M1 scope and were not run.
- **Locale Consistency**: `calculateTip` uses `NumberFormat.getCurrencyInstance().format(tip)`, which formats currency according to the system's default locale. Unit tests also use `NumberFormat.getCurrencyInstance().format(...)`, ensuring parity across any execution environment.

---

## 4. Conclusion

**Final Verdict**: **CLEAN**

The work product implemented in `MainActivity.kt` and `TipCalculatorTests.kt` fully complies with all requirements from `ORIGINAL_REQUEST.md` and `PROJECT.md`. There are no hardcoded test outputs, no facade implementations, no test mocks, and genuine build artifacts exist. The work product is approved.

---

## 5. Verification Method

To independently reproduce and verify this audit:
1. View source files:
   - `app/src/main/java/com/example/tiptime/MainActivity.kt` (lines 78–195)
   - `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt` (lines 22–130)
2. View test execution reports:
   - `app/build/test-results/testDebugUnitTest/TEST-com.example.tiptime.TipCalculatorTests.xml`
   - `app/build/reports/tests/testDebugUnitTest/classes/com.example.tiptime.TipCalculatorTests.html`
3. Inspect generated APK:
   - `app/build/outputs/apk/debug/app-debug.apk` (9.51 MB)
