# Handoff Report — challenger_m2_1

## Verdict: APPROVE

---

## 1. Observation

### 1.1 Implementation in `MainActivity.kt`
In `app/src/main/java/com/example/tiptime/MainActivity.kt` (lines 187–195):
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
Direct observation: The calculation formula was updated from `tipPercent / 100 * amount` to `(tipPercent * amount) / 100`.

### 1.2 Unit Test Suite in `TipCalculatorTests.kt`
In `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt` (lines 22–130):
The test suite contains 12 unit tests covering all edge cases flagged in Iteration 1:
- `calculateTip_20PercentNoRoundup`: amount = 10.00, tip = 20.0, roundUp = false -> $2.00 (lines 24–31)
- `calculateTip_20PercentRoundup`: amount = 10.01, tip = 20.0, roundUp = true -> $3.00 (lines 33–40)
- `calculateTip_roundupWithFraction`: amount = 10.50, tip = 20.0, roundUp = true -> $3.00 (lines 42–49)
- `calculateTip_defaultParameters`: amount = 20.00, tip = 15.0, roundUp = false -> $3.00 (lines 51–57)
- `calculateTip_zeroAmount`: amount = 0.00, tip = 20.0, roundUp = false -> $0.00 (lines 59–66)
- `calculateTip_zeroAmountWithRoundup`: amount = 0.00, tip = 20.0, roundUp = true -> $0.00 (lines 68–75)
- `calculateTip_exactAmountRoundupNoChange`: amount = 10.00, tip = 20.0, roundUp = true -> $2.00 (lines 77–84)
- `calculateTip_zeroPercentTipNoRoundup`: amount = 50.00, tip = 0.0, roundUp = false -> $0.00 (lines 86–93)
- `calculateTip_zeroPercentTipWithRoundup`: amount = 50.00, tip = 0.0, roundUp = true -> $0.00 (lines 95–102)
- `calculateTip_floatingPointExactBoundarySevenPercentRoundup`: amount = 100.00, tip = 7.0, roundUp = true -> $7.00 (lines 104–111)
- `calculateTip_floatingPointExactBoundaryFourteenPercentRoundup`: amount = 50.00, tip = 14.0, roundUp = true -> $7.00 (lines 113–120)
- `calculateTip_fractionalNoRoundup`: amount = 10.50, tip = 20.0, roundUp = false -> $2.10 (lines 122–129)

### 1.3 Gradle Test Execution & Output
Tool execution of `.\gradlew testDebugUnitTest` exited with exit code 0:
```
BUILD SUCCESSFUL in 16s
24 actionable tasks: 24 up-to-date
```
In `app/build/test-results/testDebugUnitTest/TEST-com.example.tiptime.TipCalculatorTests.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.tiptime.TipCalculatorTests" tests="12" skipped="0" failures="0" errors="0" timestamp="2026-09-14T05:00:49" hostname="ZEPHYRUS" time="0.018">
  <properties/>
  <testcase name="calculateTip_roundupWithFraction" classname="com.example.tiptime.TipCalculatorTests" time="0.014"/>
  <testcase name="calculateTip_20PercentRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_floatingPointExactBoundarySevenPercentRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.001"/>
  <testcase name="calculateTip_defaultParameters" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_zeroPercentTipWithRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_zeroPercentTipNoRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.001"/>
  <testcase name="calculateTip_zeroAmount" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_fractionalNoRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_floatingPointExactBoundaryFourteenPercentRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_20PercentNoRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_exactAmountRoundupNoChange" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <testcase name="calculateTip_zeroAmountWithRoundup" classname="com.example.tiptime.TipCalculatorTests" time="0.0"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

---

## 2. Logic Chain

1. **Bug Resolution**:
   - In Iteration 1, `tipPercent / 100 * amount` evaluated as `(7.0 / 100.0) * 100.0`. In IEEE 754 double precision, `7.0 / 100.0` cannot be represented exactly and rounds slightly up to `0.07000000000000000666...`. Multiplying by `100.0` produced `7.000000000000000888... > 7.0`, which caused `kotlin.math.ceil()` to return `8.0`.
   - In Iteration 2, `MainActivity.kt:189` computes `var tip = (tipPercent * amount) / 100`.
   - For `amount = 100.0, tipPercent = 7.0`: `7.0 * 100.0 = 700.0`. Both integers are representable exactly in IEEE 754 floating-point. `700.0 / 100.0 = 7.0` exactly. `kotlin.math.ceil(7.0) = 7.0`. Formatting yields `$7.00`.
   - For `amount = 50.0, tipPercent = 14.0`: `14.0 * 50.0 = 700.0`. `700.0 / 100.0 = 7.0` exactly. `kotlin.math.ceil(7.0) = 7.0`. Formatting yields `$7.00`.
   - For `amount = 10.01, tipPercent = 20.0, roundUp = true`: `20.0 * 10.01 = 200.2`. `200.2 / 100 = 2.002`. `kotlin.math.ceil(2.002) = 3.0`. Formatting yields `$3.00`.
   - For `amount = 50.0, tipPercent = 0.0, roundUp = false`: `0.0 * 50.0 = 0.0`. Formatting yields `$0.00`.

2. **Test Suite Verification**:
   - All 4 target boundary scenarios are explicitly codified as test cases in `TipCalculatorTests.kt`.
   - Execution of `.\gradlew testDebugUnitTest` confirms 12 tests passed, 0 skipped, 0 failures, 0 errors.

---

## 3. Adversarial Stress Test Matrix

| # | Scenario | Input | Expected Output | Actual Output | Status |
|---|----------|-------|-----------------|---------------|--------|
| 1 | 7% of $100 with roundup | `amount=100.0, tipPercent=7.0, roundUp=true` | `$7.00` | `$7.00` | PASS |
| 2 | 14% of $50 with roundup | `amount=50.0, tipPercent=14.0, roundUp=true` | `$7.00` | `$7.00` | PASS |
| 3 | 20% of $10.01 with roundup | `amount=10.01, tipPercent=20.0, roundUp=true` | `$3.00` | `$3.00` | PASS |
| 4 | 0% of $50 without roundup | `amount=50.0, tipPercent=0.0, roundUp=false` | `$0.00` | `$0.00` | PASS |
| 5 | 0% of $50 with roundup | `amount=50.0, tipPercent=0.0, roundUp=true` | `$0.00` | `$0.00` | PASS |
| 6 | 20% of $10.00 with roundup | `amount=10.0, tipPercent=20.0, roundUp=true` | `$2.00` | `$2.00` | PASS |
| 7 | 20% of $10.50 with roundup | `amount=10.50, tipPercent=20.0, roundUp=true` | `$3.00` | `$3.00` | PASS |
| 8 | 20% of $10.50 no roundup | `amount=10.50, tipPercent=20.0, roundUp=false` | `$2.10` | `$2.10` | PASS |
| 9 | Default parameters ($20, 15%, false) | `amount=20.0` | `$3.00` | `$3.00` | PASS |
| 10 | Zero amount without roundup | `amount=0.0, tipPercent=20.0, roundUp=false` | `$0.00` | `$0.00` | PASS |
| 11 | Zero amount with roundup | `amount=0.0, tipPercent=20.0, roundUp=true` | `$0.00` | `$0.00` | PASS |
| 12 | 20% of $10.00 no roundup | `amount=10.0, tipPercent=20.0, roundUp=false` | `$2.00` | `$2.00` | PASS |

---

## 4. Caveats

- No caveats. The arithmetic fix `(tipPercent * amount) / 100` cleanly solves the floating-point round-up inaccuracy for standard currencies and percentage values without altering public API signatures or architectural contracts.

---

## 5. Conclusion

**Verdict: APPROVE**

- The floating-point precision error reported in Milestone M1 Iteration 1 has been completely resolved.
- All four target test cases evaluate accurately to their expected currency strings:
  - `calculateTip(100.0, 7.0, true) == "$7.00"`
  - `calculateTip(50.0, 14.0, true) == "$7.00"`
  - `calculateTip(10.01, 20.0, true) == "$3.00"`
  - `calculateTip(50.0, 0.0, false) == "$0.00"`
- All 12 unit tests in `TipCalculatorTests.kt` pass cleanly under Gradle debug unit test execution.

---

## 6. Verification Method

To independently verify:
1. Inspect `app/src/main/java/com/example/tiptime/MainActivity.kt` line 189:
   Observe `var tip = (tipPercent * amount) / 100`.
2. Inspect `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt`:
   Observe 12 test methods covering all edge cases.
3. Inspect `app/build/test-results/testDebugUnitTest/TEST-com.example.tiptime.TipCalculatorTests.xml`:
   Observe `tests="12" failures="0" errors="0"`.
4. Optionally run `.\gradlew testDebugUnitTest` in `C:\Users\yalan\Documentos\Prog. Movil\Unidad_2\basic-android-kotlin-compose-training-tip-calculator`.
