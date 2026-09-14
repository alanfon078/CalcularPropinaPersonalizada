# Project: Tip Calculator (Tip Time)

## Architecture
- Single Activity (`MainActivity`) hosting Jetpack Compose UI with Material 3.
- Unidirectional Data Flow (UDF) & State Hoisting:
  - Top-level composable `TipTimeLayout` manages all state (`amountInput`, `tipInput`, `roundUp`).
  - Child composables `EditNumberField` and `RoundTheTipRow` are stateless, receiving state values and event callbacks.
- Business Logic:
  - `@VisibleForTesting internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String`
  - Encapsulated calculation with `(tipPercent * amount) / 100`, `kotlin.math.ceil`, and locale-aware `NumberFormat.getCurrencyInstance()`.
- Testing:
  - JVM Unit tests in `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt` testing 12 calculation variants, boundaries, and rounding edge cases.
- Build & Verification:
  - AGP 8.8.0, Kotlin 2.1.0, Compose BOM 2024.12.01, OpenJDK 21 LTS (`C:\Program Files\Android\openjdk\jdk-21.0.8`), Android SDK 35.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Edge-to-edge Activity Setup | `enableEdgeToEdge()` in `onCreate()` with `Surface(Modifier.fillMaxSize())` hosting `TipTimeLayout` | M1 | Survey (spec_miner_1, explorer_1) |
| 2 | Hoisted Bill Amount State | `var amountInput by remember { mutableStateOf("") }` in `TipTimeLayout` | M1 | Survey (spec_miner_1) |
| 3 | Hoisted Tip Percentage State | `var tipInput by remember { mutableStateOf("") }` in `TipTimeLayout` | M1 | Survey (spec_miner_1) |
| 4 | Hoisted Round-Up State | `var roundUp by remember { mutableStateOf(false) }` in `TipTimeLayout` | M1 | Survey (spec_miner_1) |
| 5 | Scrollable Layout Container | `Column` with `verticalScroll(rememberScrollState())`, `statusBarsPadding()`, `safeDrawingPadding()`, `padding(horizontal = 40.dp)` | M1 | Survey (spec_miner_1) |
| 6 | Header Title Text | `Text` showing `R.string.calculate_tip` ("Calculate Tip") aligned start with standard typography | M1 | Survey (spec_miner_1) |
| 7 | Stateless Bill Amount Field | `EditNumberField` with `R.string.bill_amount`, `R.drawable.money`, `ImeAction.Next`, `KeyboardType.Number` | M1 | Survey (spec_miner_1) |
| 8 | Stateless Tip Percentage Field | `EditNumberField` with `R.string.how_was_the_service`, `R.drawable.percent`, `ImeAction.Done`, `KeyboardType.Number` | M1 | Survey (spec_miner_1) |
| 9 | Round Tip Switch Row | `RoundTheTipRow` containing `Text(R.string.round_up_tip)` and `Switch(checked = roundUp, onCheckedChange = onRoundUpChanged)` | M1 | Survey (spec_miner_1) |
| 10 | Tip Result Display Text | `Text` displaying `stringResource(R.string.tip_amount, tip)` with `MaterialTheme.typography.displaySmall` | M1 | Survey (spec_miner_1) |
| 11 | Bottom Scroll Spacer | `Spacer(modifier = Modifier.height(150.dp))` ensuring inputs can be scrolled above soft keyboard | M1 | Survey (spec_miner_1) |
| 12 | Tip Calculation Logic | Calculation: `tip = (tipPercent * amount) / 100; if (roundUp) tip = kotlin.math.ceil(tip)` | M1 | Survey (spec_miner_1, explorer_m2_1) |
| 13 | Currency Formatting | `NumberFormat.getCurrencyInstance().format(tip)` | M1 | Survey (spec_miner_1) |
| 14 | VisibleForTesting Visibility | `@VisibleForTesting internal fun calculateTip(...)` accessible to JVM unit tests | M1 | Survey (spec_miner_1) |
| 15 | Unit Test Suite | `TipCalculatorTests.kt` in `app/src/test/java/com/example/tiptime/` with 12 comprehensive unit tests | M1 | Survey (spec_miner_1, explorer_m2_2) |
| 16 | Build Environment Configuration | OpenJDK 21 LTS configured in `gradle.properties` (`org.gradle.java.home`), verified clean `gradlew build` | M1 | Survey (explorer_2, explorer_m1_3) |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Complete Tip Calculator Implementation & Verification | All Features 1-16: `MainActivity.kt`, `TipCalculatorTests.kt`, `gradle.properties`, and clean `gradlew build` | none | DONE |

## Interface Contracts
### MainActivity.kt
- `fun EditNumberField(@StringRes label: Int, @DrawableRes leadingIcon: Int, keyboardOptions: KeyboardOptions, value: String, onValueChanged: (String) -> Unit, modifier: Modifier = Modifier)`
- `fun RoundTheTipRow(roundUp: Boolean, onRoundUpChanged: (Boolean) -> Unit, modifier: Modifier = Modifier)`
- `@VisibleForTesting internal fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean = false): String`

## Code Layout
- `app/src/main/java/com/example/tiptime/MainActivity.kt` (UI and calculateTip logic)
- `app/src/test/java/com/example/tiptime/TipCalculatorTests.kt` (Unit test suite with 12 tests)
- `gradle.properties` (JDK 21 configuration)
