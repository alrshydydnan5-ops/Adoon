package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.MealLog
import com.example.data.model.PaymentMethodItem
import com.example.data.model.ReminderItem
import com.example.data.model.WorkoutLog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class HealthRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureProfileAndAddMealWorkoutStatReminderPayment_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = HealthRepository(firestore)

        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureProfileExists("Alice", "ar")
        }
        assertEquals(uid, profile.userId)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.activateSubscription("annual", "0559988776")
        }
        val updatedProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first { it?.subscriptionActive == true }
        }
        assertEquals("0559988776", updatedProfile?.subscriptionNumber)

        val mealId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addMealLog(
                MealLog(
                    id = UUID.randomUUID().toString().replace("-", ""),
                    dateKey = DATE_KEY,
                    nameAr = "بيض مسلوق",
                    nameEn = "Boiled Eggs",
                    mealType = "breakfast",
                    calories = 234L,
                    carbsG = 1.8,
                    proteinG = 18.9,
                    fatsG = 15.9,
                    servingLabel = "3 eggs"
                )
            )
        }
        val meals = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeMealLogs(uid).first { list -> list.any { it.id == mealId } }
        }
        assertTrue(meals.any { it.id == mealId })

        val workoutId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addWorkoutLog(
                WorkoutLog(
                    id = UUID.randomUUID().toString().replace("-", ""),
                    dateKey = DATE_KEY,
                    titleAr = "جري",
                    titleEn = "Running",
                    category = "cardio",
                    durationMinutes = 30L,
                    caloriesBurned = 320L,
                    completed = true
                )
            )
        }
        val workouts = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeWorkoutLogs(uid).first { list -> list.any { it.id == workoutId } }
        }
        assertTrue(workouts.any { it.id == workoutId })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertDailyStat(DATE_KEY, 1500L, 5000L, 74.2)
        }
        val stats = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeDailyStats(uid).first { list -> list.any { it.dateKey == DATE_KEY && it.waterMl == 1500L } }
        }
        assertTrue(stats.isNotEmpty())

        val reminderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addReminder(
                ReminderItem(
                    id = UUID.randomUUID().toString().replace("-", ""),
                    titleAr = "شرب الماء",
                    titleEn = "Drink Water",
                    category = "water",
                    hour = 10L,
                    minute = 0L,
                    enabled = true
                )
            )
        }
        val reminders = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeReminders(uid).first { list -> list.any { it.id == reminderId } }
        }
        assertTrue(reminders.isNotEmpty())

        val pmId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addPaymentMethod(
                PaymentMethodItem(
                    id = UUID.randomUUID().toString().replace("-", ""),
                    provider = "mada",
                    label = "Mada Card",
                    maskedIdentifier = "•••• 4242",
                    isDefault = true
                )
            )
        }
        val methods = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePaymentMethods(uid).first { list -> list.any { it.id == pmId } }
        }
        assertTrue(methods.isNotEmpty())
    }

    @Test
    fun crossUserAccess_readingAnotherUsersMeals_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = HealthRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addMealLog(
                MealLog(
                    id = UUID.randomUUID().toString().replace("-", ""),
                    dateKey = DATE_KEY,
                    nameAr = "وجبة أليس",
                    nameEn = "Alice Meal",
                    mealType = "lunch",
                    calories = 400L,
                    carbsG = 30.0,
                    proteinG = 35.0,
                    fatsG = 12.0,
                    servingLabel = "1 bowl"
                )
            )
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("mealLogs").get().await()
            }
            fail("Expected PERMISSION_DENIED for cross-user read")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }
    }

    @Test
    fun observeMealLogs_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val repo = HealthRepository(firestore)
        auth.signOut()

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeMealLogs("some_uid_123").first()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_nutri@test.com"
        const val BOB_EMAIL = "bob_nutri@test.com"
        const val DATE_KEY = "2026-11-13"
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
