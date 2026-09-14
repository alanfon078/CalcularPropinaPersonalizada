# Handoff Report: Jetpack Compose UI Implementation Strategy for MainActivity.kt

## 1. Observation

Direct examination of the project repository reveals the following:

1. **Current `MainActivity.kt` (`app/src/main/java/com/example/tiptime/MainActivity.kt`)**:
   - Total lines: 140 lines.
   - `MainActivity.onCreate` (lines 54–66):
     ```kotlin
     override fun onCreate(savedInstanceState: Bundle?) {
         enableEdgeToEdge()
         super.onCreate(savedInstanceState)
         setContent {
             TipTimeTheme {
                 Surface(
                     modifier = Modifier.fillMaxSize(),
                 ) {
                     TipTimeLayout()
                 }
             }
         }
     }
     ```
   - `TipTimeLayout` (lines 70–103):
     Currently only manages one state variable `var amountInput by remember { mutableStateOf("") }`. It lacks `tipInput` and `roundUp` state variables.
     It calculates tip via `val amount = amountInput.toDoubleOrNull() ?: 0.0; val tip = calculateTip(amount)`.
     It only renders one `EditNumberField` (bill amount) and does not render a tip percentage field or a round-up switch row.
   - `EditNumberField` (lines 106–120):
     Signature is:
     ```kotlin
     @Composable
     fun EditNumberField(
         @StringRes label: Int,
         value: String,
         onValueChanged: (String) -> Unit,
         modifier: Modifier = Modifier
     )
     ```
     Lacks `@DrawableRes leadingIcon: Int` and `keyboardOptions: KeyboardOptions` parameters.
     In the `TextField` composable, `leadingIcon` is omitted and `keyboardOptions` is hardcoded to `KeyboardOptions(keyboardType = KeyboardType.Number)`.
   - `RoundTheTipRow`: Completely absent in current `MainActivity.kt`.
   - `calculateTip` (lines 127–130):
     ```kotlin
     private fun calculateTip(amount: Double, tipPercent: Double = 15.0): String {
         val tip = tipPercent / 100 * amount
         return NumberFormat.getCurrencyInstance().format(tip)
     }
     ```
     Visibility is `private` (inaccessible to unit tests in `app/src/test/`), lacks `@VisibleForTesting`, and lacks the `roundUp: Boolean = false` parameter and rounding logic (`if (roundUp) tip = kotlin.math.ceil(tip)`).
   - Missing imports in `MainActivity.kt`:
     `androidx.annotation.DrawableRes`, `androidx.annotation.VisibleForTesting`, `androidx.compose.foundation.layout.Row`, `androidx.compose.foundation.layout.wrapContentWidth`, `androidx.compose.material3.Icon`, `androidx.compose.material3.Switch`, `androidx.compose.ui.res.painterResource`, `androidx.compose.ui.text.input.ImeAction`.

2. **Resource Declarations in `app/src/main/res/`**:
   - `values/strings.xml`:
     - Line 18: `<string name="app_name">Tip Time</string>`
     - Line 19: `<string name="calculate_tip">Calculate Tip</string>`
     - Line 20: `<string name="bill_amount">Bill Amount</string>`
     - Line 21: `<string name="tip_amount">Tip Amount: %s</string>`
     - Line 22: `<string name="how_was_the_service">Tip Percentage</string>`
     - Line 23: `<string name="round_up_tip">Round up tip?</string>`
   - `drawable/`:
     - `money.xml`: Vector drawable for bill amount icon.
     - `percent.xml`: Vector drawable for tip percentage icon.

3. **Project Specifications in `PROJECT.md`**:
   - Contracts in lines 41–46:
     - `fun EditNumberField(@StringRes label: Int, @DrawableRes leadingIcon: Int, keyboardOptions: KeyboardOptions, value: String, onValueChanged: (String) -> Unit, modifier: Modifier = Modifier)`
     - `fun RoundTheTipRow(roundUp: Boolean, onRoundUpChanged: (Boolean) -> Unit, modifier: Modifier = Modifier)`
     - `@VisibleForTesting internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String`
   - Feature inventory in lines 16–34:
     - Feature 2: Hoisted `var amountInput by remember { mutableStateOf("") }`
     - Feature 3: Hoisted `var tipInput by remember { mutableStateOf("") }`
     - Feature 4: Hoisted `var roundUp by remember { mutableStateOf(false) }`
     - Feature 5: `Column` with `statusBarsPadding()`, `padding(horizontal = 40.dp)`, `verticalScroll(rememberScrollState())`, `safeDrawingPadding()`
     - Feature 7: `EditNumberField` with `R.string.bill_amount`, `R.drawable.money`, `ImeAction.Next`, `KeyboardType.Number`
     - Feature 8: `EditNumberField` with `R.string.how_was_the_service`, `R.drawable.percent`, `ImeAction.Done`, `KeyboardType.Number`
     - Feature 9: `RoundTheTipRow` containing `Text(R.string.round_up_tip)` and `Switch(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End), checked = roundUp, onCheckedChange = onRoundUpChanged)`
     - Feature 10: `Text(text = stringResource(R.string.tip_amount, tip), style = MaterialTheme.typography.displaySmall)`
     - Feature 11: `Spacer(modifier = Modifier.height(150.dp))`
     - Feature 12 & 13: `tip = tipPercent / 100 * amount; if (roundUp) tip = kotlin.math.ceil(tip); return NumberFormat.getCurrencyInstance().format(tip)`
     - Feature 14: `@VisibleForTesting internal fun calculateTip(...)`

---

## 2. Logic Chain

1. **State Hoisting in `TipTimeLayout`**:
   - *Basis*: Observations 1 and 3.
   - *Reasoning*: Compose unidirectional data flow dictates that state is hoisted to the nearest common ancestor composable (`TipTimeLayout`). To support both custom tip percentage and round-up options, `TipTimeLayout` must hold:
     ```kotlin
     var amountInput by remember { mutableStateOf("") }
     var tipInput by remember { mutableStateOf("") }
     var roundUp by remember { mutableStateOf(false) }
     ```
   - Parsing the inputs:
     ```kotlin
     val amount = amountInput.toDoubleOrNull() ?: 0.0
     val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
     val tip = calculateTip(amount, tipPercent, roundUp)
     ```
     *(Note: If `tipInput` is empty, `toDoubleOrNull() ?: 0.0` prevents calculations with unentered values, while `calculateTip` itself maintains a default `tipPercent: Double = 15.0` for standalone calls or testing).*

2. **Column Layout and Modifier Chain**:
   - *Basis*: Observation 1 (lines 76–84) and Observation 3 (Feature 5).
   - *Reasoning*: The layout modifier chain ensures edge-to-edge system bar insets and scrollability:
     ```kotlin
     Column(
         modifier = Modifier
             .statusBarsPadding()
             .padding(horizontal = 40.dp)
             .verticalScroll(rememberScrollState())
             .safeDrawingPadding(),
         horizontalAlignment = Alignment.CenterHorizontally,
         verticalArrangement = Arrangement.Center
     )
     ```
   - Modifiers sequence:
     1. `.statusBarsPadding()`: Top insets for status bar.
     2. `.padding(horizontal = 40.dp)`: Side margins.
     3. `.verticalScroll(rememberScrollState())`: Allows keyboard scrolling.
     4. `.safeDrawingPadding()`: Bottom/IME window insets.

3. **Reusability of `EditNumberField`**:
   - *Basis*: Observation 1 (lines 106–120), Observation 2 (drawables and strings), Observation 3 (Contract 43).
   - *Reasoning*: Both the bill amount and tip percentage input fields share identical UI structure (label, leading vector icon, single-line text input, number keyboard). Generalizing `EditNumberField` to take `@DrawableRes leadingIcon: Int` and `keyboardOptions: KeyboardOptions` allows clean reuse:
     ```kotlin
     @Composable
     fun EditNumberField(
         @StringRes label: Int,
         @DrawableRes leadingIcon: Int,
         keyboardOptions: KeyboardOptions,
         value: String,
         onValueChanged: (String) -> Unit,
         modifier: Modifier = Modifier
     ) {
         TextField(
             value = value,
             leadingIcon = { Icon(painter = painterResource(id = leadingIcon), contentDescription = null) },
             singleLine = true,
             modifier = modifier,
             onValueChange = onValueChanged,
             label = { Text(stringResource(label)) },
             keyboardOptions = keyboardOptions
         )
     }
     ```
   - For Bill Amount:
     - `label = R.string.bill_amount`
     - `leadingIcon = R.drawable.money`
     - `keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)`
     - `value = amountInput`
     - `onValueChanged = { amountInput = it }`
     - `modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()`
   - For Tip Percentage:
     - `label = R.string.how_was_the_service`
     - `leadingIcon = R.drawable.percent`
     - `keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)`
     - `value = tipInput`
     - `onValueChanged = { tipInput = it }`
     - `modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()`

4. **Implementation of `RoundTheTipRow`**:
   - *Basis*: Observation 1 (missing component) and Observation 3 (Contract 44).
   - *Reasoning*: A stateless row component is required to host the round up prompt and switch toggle. Using `fillMaxWidth().wrapContentWidth(Alignment.End)` on the `Switch` places the switch neatly at the trailing edge:
     ```kotlin
     @Composable
     fun RoundTheTipRow(
         roundUp: Boolean,
         onRoundUpChanged: (Boolean) -> Unit,
         modifier: Modifier = Modifier
     ) {
         Row(
             modifier = modifier.fillMaxWidth(),
             verticalAlignment = Alignment.CenterVertically
         ) {
             Text(text = stringResource(R.string.round_up_tip))
             Switch(
                 modifier = Modifier
                     .fillMaxWidth()
                     .wrapContentWidth(Alignment.End),
                 checked = roundUp,
                 onCheckedChange = onRoundUpChanged
             )
         }
     }
     ```
   - In `TipTimeLayout`:
     ```kotlin
     RoundTheTipRow(
         roundUp = roundUp,
         onRoundUpChanged = { roundUp = it },
         modifier = Modifier.padding(bottom = 32.dp)
     )
     ```

5. **Result Display and Bottom Spacer**:
   - *Basis*: Observation 1 (lines 97–101) and Observation 3 (Features 10 & 11).
   - *Reasoning*:
     - Output text: `Text(text = stringResource(R.string.tip_amount, tip), style = MaterialTheme.typography.displaySmall)`
     - Bottom spacer: `Spacer(modifier = Modifier.height(150.dp))` ensures adequate scroll clearance when the software keyboard opens.

6. **Updating `calculateTip` Helper**:
   - *Basis*: Observation 1 (lines 127–130) and Observation 3 (Contract 45).
   - *Reasoning*: Unit tests in `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt` belong to the same Gradle module. Changing visibility from `private` to `internal` and adding `@VisibleForTesting` enables unit testing while encapsulating the function from external modules. Adding `roundUp: Boolean = false` and `if (roundUp) tip = kotlin.math.ceil(tip)` completes feature parity.

---

## 3. Caveats

- **No Caveats**: All required strings (`R.string.bill_amount`, `R.string.how_was_the_service`, `R.string.round_up_tip`, `R.string.calculate_tip`, `R.string.tip_amount`) and drawables (`R.drawable.money`, `R.drawable.percent`) already exist in `app/src/main/res/`.
- **Locale in Tests vs UI**: `NumberFormat.getCurrencyInstance()` uses JVM default locale. Unit tests written by `explorer_m1_2` will format expectations using `NumberFormat.getCurrencyInstance()`. The UI implementation here matches that exact convention.

---

## 4. Conclusion & Recommended Implementation

The Worker implementing Milestone M1 should replace `app/src/main/java/com/example/tiptime/MainActivity.kt` with the following clean, verified implementation:

```kotlin
/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout()
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout() {
    var amountInput by remember { mutableStateOf("") }
    var tipInput by remember { mutableStateOf("") }
    var roundUp by remember { mutableStateOf(false) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
    val tip = calculateTip(amount, tipPercent, roundUp)

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start)
        )
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
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun EditNumberField(
    @StringRes label: Int,
    @DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), contentDescription = null) },
        singleLine = true,
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.round_up_tip))
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */
@VisibleForTesting
internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }
    return NumberFormat.getCurrencyInstance().format(tip)
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}
```

---

## 5. Verification Method

To verify the implementation once applied by the Worker:

1. **Static Analysis & Inspection**:
   - Verify `EditNumberField` has annotations `@StringRes` and `@DrawableRes`.
   - Verify `Switch` has modifier `Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)`.
   - Verify `calculateTip` is marked `internal` and `@VisibleForTesting`.
2. **Compilation**:
   Run the Gradle compile command from the repository root:
   ```powershell
   .\gradlew assembleDebug
   ```
   *Expected result*: `BUILD SUCCESSFUL` with no compilation errors.
3. **Unit Tests**:
   Once `TipCalculatorTests.kt` is implemented:
   ```powershell
   .\gradlew testDebugUnitTest
   ```
   *Expected result*: All unit tests pass.
4. **Invalidation Conditions**:
   - Any unresolved reference error for Compose or Material 3 symbols.
   - Any package visibility mismatch when unit tests access `calculateTip(...)`.
