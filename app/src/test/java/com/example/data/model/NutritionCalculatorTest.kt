package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionCalculatorTest {

    @Test
    fun calculateTargets_maleLoseFat_computesValidMacrosAndWater() {
        val result = NutritionCalculator.calculateTargets(
            age = 28,
            gender = "male",
            heightCm = 178.0,
            weightKg = 80.0,
            activityLevel = "moderate",
            goalType = "lose"
        )
        assertTrue(result.bmr in 1650..1850)
        assertTrue(result.tdee in 2500..2900)
        assertTrue(result.calories < result.tdee)
        assertTrue(result.carbsG > 100L)
        assertTrue(result.proteinG > 100L)
        assertTrue(result.fatsG > 40L)
        assertTrue(result.waterMl >= 2800L)
        assertEquals(25.2, result.bmi, 0.2)
    }

    @Test
    fun calculateTargets_keto_keepsCarbsLowAndFatsHigh() {
        val result = NutritionCalculator.calculateTargets(
            age = 30,
            gender = "female",
            heightCm = 165.0,
            weightKg = 65.0,
            activityLevel = "light",
            goalType = "keto"
        )
        assertTrue(result.carbsG < 65L)
        assertTrue(result.fatsG > 80L)
    }

    @Test
    fun calculateBurnedCalories_running30Min_returnsPositiveBurn() {
        val burned = NutritionCalculator.calculateBurnedCalories(met = 9.8, weightKg = 75.0, durationMinutes = 30)
        assertTrue(burned in 350L..390L)
    }
}
