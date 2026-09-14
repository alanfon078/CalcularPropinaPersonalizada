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

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat

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

    @Test
    fun calculateTip_zeroPercentTipNoRoundup() {
        val amount = 50.00
        val tipPercent = 0.0
        val expectedTip = NumberFormat.getCurrencyInstance().format(0.00)
        val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = false)
        assertEquals(expectedTip, actualTip)
    }

    @Test
    fun calculateTip_zeroPercentTipWithRoundup() {
        val amount = 50.00
        val tipPercent = 0.0
        val expectedTip = NumberFormat.getCurrencyInstance().format(0.00)
        val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
        assertEquals(expectedTip, actualTip)
    }

    @Test
    fun calculateTip_floatingPointExactBoundarySevenPercentRoundup() {
        val amount = 100.00
        val tipPercent = 7.0
        val expectedTip = NumberFormat.getCurrencyInstance().format(7.00)
        val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
        assertEquals(expectedTip, actualTip)
    }

    @Test
    fun calculateTip_floatingPointExactBoundaryFourteenPercentRoundup() {
        val amount = 50.00
        val tipPercent = 14.0
        val expectedTip = NumberFormat.getCurrencyInstance().format(7.00)
        val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = true)
        assertEquals(expectedTip, actualTip)
    }

    @Test
    fun calculateTip_fractionalNoRoundup() {
        val amount = 10.50
        val tipPercent = 20.0
        val expectedTip = NumberFormat.getCurrencyInstance().format(2.10)
        val actualTip = calculateTip(amount = amount, tipPercent = tipPercent, roundUp = false)
        assertEquals(expectedTip, actualTip)
    }
}
