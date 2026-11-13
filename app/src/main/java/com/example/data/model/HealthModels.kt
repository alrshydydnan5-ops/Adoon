package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.roundToInt

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Athlete",
    val age: Long = 28L,
    val gender: String = "male",
    val heightCm: Double = 175.0,
    val weightKg: Double = 75.0,
    val startWeightKg: Double = 75.0,
    val targetWeightKg: Double = 70.0,
    val activityLevel: String = "moderate",
    val goalType: String = "lose",
    val targetCalories: Long = 2100L,
    val targetCarbsG: Long = 210L,
    val targetProteinG: Long = 158L,
    val targetFatsG: Long = 70L,
    val targetWaterMl: Long = 2700L,
    val preferredLanguage: String = "ar",
    val themeMode: String = "system",
    val subscriptionTier: String = "free",
    val subscriptionNumber: String = "",
    val subscriptionActive: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MealLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val mealType: String = "lunch",
    val calories: Long = 0L,
    val carbsG: Double = 0.0,
    val proteinG: Double = 0.0,
    val fatsG: Double = 0.0,
    val servingLabel: String = "1 serving",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WorkoutLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val category: String = "strength",
    val durationMinutes: Long = 30L,
    val caloriesBurned: Long = 200L,
    val completed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DailyStat(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val waterMl: Long = 0L,
    val steps: Long = 0L,
    val weightKg: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ReminderItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val category: String = "water",
    val hour: Long = 9L,
    val minute: Long = 0L,
    val enabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PaymentMethodItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val provider: String = "mada",
    val label: String = "",
    val maskedIdentifier: String = "",
    val isDefault: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class FoodCatalogItem(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val mealType: String,
    val calories: Long,
    val carbsG: Double,
    val proteinG: Double,
    val fatsG: Double,
    val servingAr: String,
    val servingEn: String,
    val categoryTag: String
)

data class WorkoutCatalogItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val category: String,
    val metValue: Double,
    val defaultMinutes: Int,
    val intensityAr: String,
    val intensityEn: String
)

data class NutritionTip(
    val id: String,
    val category: String,
    val titleAr: String,
    val titleEn: String,
    val bodyAr: String,
    val bodyEn: String,
    val badgeAr: String,
    val badgeEn: String
)

data class SubscriptionPlan(
    val tier: String,
    val titleAr: String,
    val titleEn: String,
    val priceSar: Int,
    val periodAr: String,
    val periodEn: String,
    val savingsAr: String?,
    val savingsEn: String?,
    val featuresAr: List<String>,
    val featuresEn: List<String>
)

data class MacroTargets(
    val calories: Long,
    val carbsG: Long,
    val proteinG: Long,
    val fatsG: Long,
    val waterMl: Long,
    val bmr: Int,
    val tdee: Int,
    val bmi: Double
)

object NutritionCalculator {
    fun calculateTargets(
        age: Int,
        gender: String,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        goalType: String
    ): MacroTargets {
        val safeWeight = weightKg.coerceIn(30.0, 300.0)
        val safeHeight = heightCm.coerceIn(100.0, 250.0)
        val safeAge = age.coerceIn(12, 100)

        // Mifflin-St Jeor Equation
        val baseBmr = (10.0 * safeWeight) + (6.25 * safeHeight) - (5.0 * safeAge) +
            if (gender == "male") 5.0 else -161.0

        val multiplier = when (activityLevel) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "athlete" -> 1.9
            else -> 1.55
        }
        val tdee = (baseBmr * multiplier).roundToInt()

        val targetCalories = when (goalType) {
            "lose" -> (tdee - 450).coerceAtLeast(1200)
            "gain" -> (tdee + 350).coerceAtMost(5500)
            "keto" -> (tdee - 300).coerceAtLeast(1200)
            "high_protein" -> tdee
            else -> tdee
        }

        // Macro split ratios (Carbs / Protein / Fats)
        val (carbsRatio, proteinRatio, fatsRatio) = when (goalType) {
            "lose" -> Triple(0.35, 0.35, 0.30)
            "gain" -> Triple(0.45, 0.30, 0.25)
            "keto" -> Triple(0.10, 0.30, 0.60)
            "high_protein" -> Triple(0.35, 0.40, 0.25)
            else -> Triple(0.42, 0.30, 0.28)
        }

        val carbsG = ((targetCalories * carbsRatio) / 4.0).roundToInt().toLong().coerceAtLeast(20L)
        val proteinG = ((targetCalories * proteinRatio) / 4.0).roundToInt().toLong().coerceAtLeast(40L)
        val fatsG = ((targetCalories * fatsRatio) / 9.0).roundToInt().toLong().coerceAtLeast(20L)

        // Water target ~ 35ml per kg + activity bonus
        val activityWaterBonus = if (activityLevel in listOf("active", "athlete")) 500 else 250
        val waterMl = ((safeWeight * 35.0).roundToInt() + activityWaterBonus).coerceIn(1500, 5000).toLong()

        val heightM = safeHeight / 100.0
        val bmi = ((safeWeight / (heightM * heightM)) * 10.0).roundToInt() / 10.0

        return MacroTargets(
            calories = targetCalories.toLong(),
            carbsG = carbsG,
            proteinG = proteinG,
            fatsG = fatsG,
            waterMl = waterMl,
            bmr = baseBmr.roundToInt(),
            tdee = tdee,
            bmi = bmi
        )
    }

    fun calculateBurnedCalories(met: Double, weightKg: Double, durationMinutes: Int): Long {
        val hours = durationMinutes.coerceAtLeast(1) / 60.0
        return (met * weightKg.coerceIn(35.0, 250.0) * hours).roundToInt().toLong().coerceAtLeast(10L)
    }
}
