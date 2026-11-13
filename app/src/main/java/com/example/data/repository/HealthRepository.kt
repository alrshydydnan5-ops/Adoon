package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.DailyStat
import com.example.data.model.MealLog
import com.example.data.model.PaymentMethodItem
import com.example.data.model.ReminderItem
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutLog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HealthRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeProfile(uid: String): Flow<UserProfile?> = flow {
        val docRef = userDoc(uid)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreException(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureProfileExists(defaultDisplayName: String, preferredLang: String): UserProfile {
        val uid = requireUserId()
        val docRef = userDoc(uid)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE)
                if (existing != null) return existing
            }
            val safeName = defaultDisplayName.trim().ifBlank { "Athlete" }.take(80)
            val payload = mapOf(
                "userId" to uid,
                "displayName" to safeName,
                "age" to 28L,
                "gender" to "male",
                "heightCm" to 175.0,
                "weightKg" to 75.0,
                "startWeightKg" to 75.0,
                "targetWeightKg" to 70.0,
                "activityLevel" to "moderate",
                "goalType" to "lose",
                "targetCalories" to 2100L,
                "targetCarbsG" to 184L,
                "targetProteinG" to 184L,
                "targetFatsG" to 70L,
                "targetWaterMl" to 2875L,
                "preferredLanguage" to if (preferredLang == "en") "en" else "ar",
                "themeMode" to "system",
                "subscriptionTier" to "free",
                "subscriptionNumber" to "",
                "subscriptionActive" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return docRef.get().await()
                .toObject(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE)
                ?: UserProfile(id = uid, userId = uid, displayName = safeName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = userDoc(uid)
        val updates = mapOf(
            "displayName" to profile.displayName.trim().ifBlank { "Athlete" }.take(80),
            "age" to profile.age.coerceIn(12L, 100L),
            "gender" to if (profile.gender == "female") "female" else "male",
            "heightCm" to profile.heightCm.coerceIn(100.0, 250.0),
            "weightKg" to profile.weightKg.coerceIn(30.0, 300.0),
            "startWeightKg" to profile.startWeightKg.coerceIn(30.0, 300.0),
            "targetWeightKg" to profile.targetWeightKg.coerceIn(30.0, 300.0),
            "activityLevel" to profile.activityLevel,
            "goalType" to profile.goalType,
            "targetCalories" to profile.targetCalories.coerceIn(800L, 6000L),
            "targetCarbsG" to profile.targetCarbsG.coerceIn(10L, 900L),
            "targetProteinG" to profile.targetProteinG.coerceIn(20L, 500L),
            "targetFatsG" to profile.targetFatsG.coerceIn(10L, 300L),
            "targetWaterMl" to profile.targetWaterMl.coerceIn(500L, 6000L),
            "preferredLanguage" to if (profile.preferredLanguage == "en") "en" else "ar",
            "themeMode" to profile.themeMode,
            "subscriptionTier" to profile.subscriptionTier,
            "subscriptionNumber" to profile.subscriptionNumber.take(20),
            "subscriptionActive" to profile.subscriptionActive,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateLanguageAndTheme(preferredLanguage: String, themeMode: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid)
        try {
            docRef.update(
                mapOf(
                    "preferredLanguage" to if (preferredLanguage == "en") "en" else "ar",
                    "themeMode" to when (themeMode) {
                        "light", "dark", "system" -> themeMode
                        else -> "system"
                    },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun activateSubscription(tier: String, numericCode: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid)
        try {
            docRef.update(
                mapOf(
                    "subscriptionTier" to tier,
                    "subscriptionNumber" to numericCode.trim().take(20),
                    "subscriptionActive" to (tier != "free"),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- MEAL LOGS ---
    fun observeMealLogs(uid: String): Flow<List<MealLog>> = flow {
        val colRef = userDoc(uid).collection("mealLogs")
        emitAll(
            colRef.snapshots()
                .map { snap -> snap.toObjects(MealLog::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreException(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun addMealLog(meal: MealLog): String {
        val uid = requireUserId()
        val id = meal.id.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        val docRef = userDoc(uid).collection("mealLogs").document(id)
        val payload = mapOf(
            "userId" to uid,
            "dateKey" to meal.dateKey,
            "nameAr" to meal.nameAr.trim().ifBlank { "وجبة" }.take(100),
            "nameEn" to meal.nameEn.trim().ifBlank { "Meal" }.take(100),
            "mealType" to meal.mealType,
            "calories" to meal.calories.coerceIn(0L, 5000L),
            "carbsG" to meal.carbsG.coerceIn(0.0, 1000.0),
            "proteinG" to meal.proteinG.coerceIn(0.0, 500.0),
            "fatsG" to meal.fatsG.coerceIn(0.0, 500.0),
            "servingLabel" to meal.servingLabel.trim().ifBlank { "1 serving" }.take(60),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMealLog(mealId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("mealLogs").document(mealId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- WORKOUT LOGS ---
    fun observeWorkoutLogs(uid: String): Flow<List<WorkoutLog>> = flow {
        val colRef = userDoc(uid).collection("workoutLogs")
        emitAll(
            colRef.snapshots()
                .map { snap -> snap.toObjects(WorkoutLog::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreException(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun addWorkoutLog(workout: WorkoutLog): String {
        val uid = requireUserId()
        val id = workout.id.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        val docRef = userDoc(uid).collection("workoutLogs").document(id)
        val payload = mapOf(
            "userId" to uid,
            "dateKey" to workout.dateKey,
            "titleAr" to workout.titleAr.trim().ifBlank { "تمرين" }.take(100),
            "titleEn" to workout.titleEn.trim().ifBlank { "Workout" }.take(100),
            "category" to workout.category,
            "durationMinutes" to workout.durationMinutes.coerceIn(1L, 600L),
            "caloriesBurned" to workout.caloriesBurned.coerceIn(0L, 5000L),
            "completed" to workout.completed,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleWorkoutCompleted(workout: WorkoutLog) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("workoutLogs").document(workout.id)
        try {
            docRef.update(
                mapOf(
                    "completed" to !workout.completed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteWorkoutLog(workoutId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("workoutLogs").document(workoutId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- DAILY STATS (Water, Steps, Weight) ---
    fun observeDailyStats(uid: String): Flow<List<DailyStat>> = flow {
        val colRef = userDoc(uid).collection("dailyStats")
        emitAll(
            colRef.snapshots()
                .map { snap -> snap.toObjects(DailyStat::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreException(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun upsertDailyStat(dateKey: String, waterMl: Long, steps: Long, weightKg: Double) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("dailyStats").document(dateKey)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "waterMl" to waterMl.coerceIn(0L, 15000L),
                        "steps" to steps.coerceIn(0L, 100000L),
                        "weightKg" to weightKg.coerceIn(0.0, 300.0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "dateKey" to dateKey,
                        "waterMl" to waterMl.coerceIn(0L, 15000L),
                        "steps" to steps.coerceIn(0L, 100000L),
                        "weightKg" to weightKg.coerceIn(0.0, 300.0),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- REMINDERS ---
    fun observeReminders(uid: String): Flow<List<ReminderItem>> = flow {
        val colRef = userDoc(uid).collection("reminders")
        emitAll(
            colRef.snapshots()
                .map { snap -> snap.toObjects(ReminderItem::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreException(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun addReminder(reminder: ReminderItem): String {
        val uid = requireUserId()
        val id = reminder.id.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        val docRef = userDoc(uid).collection("reminders").document(id)
        try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "titleAr" to reminder.titleAr.trim().ifBlank { "تذكير" }.take(100),
                    "titleEn" to reminder.titleEn.trim().ifBlank { "Reminder" }.take(100),
                    "category" to reminder.category,
                    "hour" to reminder.hour.coerceIn(0L, 23L),
                    "minute" to reminder.minute.coerceIn(0L, 59L),
                    "enabled" to reminder.enabled,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleReminder(reminder: ReminderItem) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("reminders").document(reminder.id)
        try {
            docRef.update(
                mapOf(
                    "enabled" to !reminder.enabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("reminders").document(reminderId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- PAYMENT METHODS ---
    fun observePaymentMethods(uid: String): Flow<List<PaymentMethodItem>> = flow {
        val colRef = userDoc(uid).collection("paymentMethods")
        emitAll(
            colRef.snapshots()
                .map { snap -> snap.toObjects(PaymentMethodItem::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreException(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun addPaymentMethod(method: PaymentMethodItem): String {
        val uid = requireUserId()
        val id = method.id.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        val colRef = userDoc(uid).collection("paymentMethods")
        val docRef = colRef.document(id)
        try {
            if (method.isDefault) {
                val existing = colRef.get().await()
                existing.documents.forEach { doc ->
                    if (doc.getBoolean("isDefault") == true) {
                        doc.reference.update(
                            mapOf(
                                "isDefault" to false,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "provider" to method.provider,
                    "label" to method.label.trim().ifBlank { "Payment Method" }.take(80),
                    "maskedIdentifier" to method.maskedIdentifier.trim().ifBlank { "•••• 0000" }.take(30),
                    "isDefault" to method.isDefault,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun setDefaultPaymentMethod(methodId: String) {
        val uid = requireUserId()
        val colRef = userDoc(uid).collection("paymentMethods")
        try {
            val existing = colRef.get().await()
            existing.documents.forEach { doc ->
                val targetDefault = doc.id == methodId
                if (doc.getBoolean("isDefault") != targetDefault) {
                    doc.reference.update(
                        mapOf(
                            "isDefault" to targetDefault,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, colRef.path)
            throw e
        }
    }

    suspend fun deletePaymentMethod(methodId: String) {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("paymentMethods").document(methodId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    private fun unwrapFirestoreException(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }
}
