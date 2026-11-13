package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.HealthCatalog
import com.example.data.model.DailyStat
import com.example.data.model.FoodCatalogItem
import com.example.data.model.MealLog
import com.example.data.model.NutritionCalculator
import com.example.data.model.NutritionTip
import com.example.data.model.PaymentMethodItem
import com.example.data.model.ReminderItem
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutCatalogItem
import com.example.data.model.WorkoutLog
import com.example.data.repository.HealthRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DailySummaryPoint(
    val dateKey: String,
    val shortLabel: String,
    val consumedCalories: Long,
    val burnedCalories: Long,
    val proteinG: Double,
    val carbsG: Double,
    val fatsG: Double,
    val waterMl: Long,
    val weightKg: Double
)

data class HealthDashboardData(
    val profile: UserProfile,
    val selectedDateKey: String,
    val selectedDateLabel: String,
    val mealsForDay: List<MealLog>,
    val workoutsForDay: List<WorkoutLog>,
    val statForDay: DailyStat,
    val consumedCalories: Long,
    val carbsG: Double,
    val proteinG: Double,
    val fatsG: Double,
    val burnedCalories: Long,
    val netCalories: Long,
    val weeklyHistory: List<DailySummaryPoint>,
    val streakDays: Int,
    val smartTip: NutritionTip,
    val reminders: List<ReminderItem>,
    val paymentMethods: List<PaymentMethodItem>
)

class HealthViewModel(
    private val repository: HealthRepository,
    private val currentUserId: String,
    initialDisplayName: String,
    initialLanguage: String
) : ViewModel() {

    private val _selectedDateOffset = MutableStateFlow(0)
    val selectedDateOffset: StateFlow<Int> = _selectedDateOffset.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.ensureProfileExists(initialDisplayName, initialLanguage)
            } catch (e: Exception) {
                Log.w(TAG, "Profile bootstrap failed", e)
            }
        }
    }

    private val dateFlow = _selectedDateOffset
    private val coreDataFlow = combine(
        repository.observeProfile(currentUserId),
        repository.observeMealLogs(currentUserId),
        repository.observeWorkoutLogs(currentUserId),
        repository.observeDailyStats(currentUserId),
        repository.observeReminders(currentUserId)
    ) { profile, meals, workouts, stats, reminders ->
        CoreBundle(profile, meals, workouts, stats, reminders)
    }

    val dashboardState: StateFlow<UiState<HealthDashboardData>> = combine(
        coreDataFlow,
        repository.observePaymentMethods(currentUserId),
        dateFlow
    ) { bundle, paymentMethods, offsetDays ->
        val profile = bundle.profile ?: UserProfile(id = currentUserId, userId = currentUserId)
        val selectedDateKey = dateKeyForOffset(offsetDays)
        val selectedDateLabel = formatDisplayDate(offsetDays)

        val mealsForDay = bundle.meals.filter { it.dateKey == selectedDateKey }
        val workoutsForDay = bundle.workouts.filter { it.dateKey == selectedDateKey }
        val statForDay = bundle.stats.firstOrNull { it.dateKey == selectedDateKey }
            ?: DailyStat(id = selectedDateKey, userId = currentUserId, dateKey = selectedDateKey)

        val consumedCalories = mealsForDay.sumOf { it.calories }
        val carbsG = mealsForDay.sumOf { it.carbsG }
        val proteinG = mealsForDay.sumOf { it.proteinG }
        val fatsG = mealsForDay.sumOf { it.fatsG }
        val burnedCalories = workoutsForDay.filter { it.completed }.sumOf { it.caloriesBurned }
        val netCalories = (consumedCalories - burnedCalories).coerceAtLeast(0L)

        // Build 7-day history ending at today (offset 0)
        val weeklyHistory = (-6..0).map { d ->
            val dk = dateKeyForOffset(d)
            val dayMeals = bundle.meals.filter { it.dateKey == dk }
            val dayWorkouts = bundle.workouts.filter { it.dateKey == dk && it.completed }
            val dayStat = bundle.stats.firstOrNull { it.dateKey == dk }
            DailySummaryPoint(
                dateKey = dk,
                shortLabel = shortDayLabel(d),
                consumedCalories = dayMeals.sumOf { it.calories },
                burnedCalories = dayWorkouts.sumOf { it.caloriesBurned },
                proteinG = dayMeals.sumOf { it.proteinG },
                carbsG = dayMeals.sumOf { it.carbsG },
                fatsG = dayMeals.sumOf { it.fatsG },
                waterMl = dayStat?.waterMl ?: 0L,
                weightKg = dayStat?.weightKg ?: 0.0
            )
        }

        // Streak calculation: consecutive days (backwards from today) with at least 1 meal or workout or water
        var streak = 0
        for (d in 0 downTo -30) {
            val dk = dateKeyForOffset(d)
            val hasActivity = bundle.meals.any { it.dateKey == dk } ||
                bundle.workouts.any { it.dateKey == dk } ||
                (bundle.stats.firstOrNull { it.dateKey == dk }?.waterMl ?: 0L) > 0L
            if (hasActivity) streak++ else if (d < 0) break
        }

        val smartTip = pickContextualTip(
            consumedCalories = consumedCalories,
            targetCalories = profile.targetCalories,
            proteinG = proteinG,
            targetProteinG = profile.targetProteinG,
            waterMl = statForDay.waterMl,
            targetWaterMl = profile.targetWaterMl,
            workoutsCount = workoutsForDay.size
        )

        UiState.Success(
            HealthDashboardData(
                profile = profile,
                selectedDateKey = selectedDateKey,
                selectedDateLabel = selectedDateLabel,
                mealsForDay = mealsForDay,
                workoutsForDay = workoutsForDay,
                statForDay = statForDay,
                consumedCalories = consumedCalories,
                carbsG = carbsG,
                proteinG = proteinG,
                fatsG = fatsG,
                burnedCalories = burnedCalories,
                netCalories = netCalories,
                weeklyHistory = weeklyHistory,
                streakDays = streak,
                smartTip = smartTip,
                reminders = bundle.reminders.sortedBy { it.hour * 60 + it.minute },
                paymentMethods = paymentMethods
            )
        ) as UiState<HealthDashboardData>
    }.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing health data", rootError)
        emit(UiState.Error(rootError.localizedMessage ?: "Failed to load health data"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading
    )

    fun shiftDate(deltaDays: Int) {
        val next = (_selectedDateOffset.value + deltaDays).coerceIn(-30, 0)
        _selectedDateOffset.value = next
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun addCatalogMeal(item: FoodCatalogItem, mealTypeOverride: String? = null) {
        val dateKey = dateKeyForOffset(_selectedDateOffset.value)
        viewModelScope.launch {
            runSafe {
                repository.addMealLog(
                    MealLog(
                        dateKey = dateKey,
                        nameAr = item.nameAr,
                        nameEn = item.nameEn,
                        mealType = mealTypeOverride ?: item.mealType,
                        calories = item.calories,
                        carbsG = item.carbsG,
                        proteinG = item.proteinG,
                        fatsG = item.fatsG,
                        servingLabel = item.servingEn
                    )
                )
            }
        }
    }

    fun addCustomMeal(
        nameAr: String,
        nameEn: String,
        mealType: String,
        calories: Long,
        carbsG: Double,
        proteinG: Double,
        fatsG: Double,
        servingLabel: String
    ) {
        val dateKey = dateKeyForOffset(_selectedDateOffset.value)
        val cleanAr = nameAr.trim().ifBlank { nameEn.trim() }.ifBlank { "وجبة مخصصة" }
        val cleanEn = nameEn.trim().ifBlank { nameAr.trim() }.ifBlank { "Custom Meal" }
        viewModelScope.launch {
            runSafe {
                repository.addMealLog(
                    MealLog(
                        dateKey = dateKey,
                        nameAr = cleanAr,
                        nameEn = cleanEn,
                        mealType = mealType,
                        calories = calories,
                        carbsG = carbsG,
                        proteinG = proteinG,
                        fatsG = fatsG,
                        servingLabel = servingLabel.ifBlank { "1 serving" }
                    )
                )
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            runSafe { repository.deleteMealLog(mealId) }
        }
    }

    fun addCatalogWorkout(item: WorkoutCatalogItem, durationMinutes: Int, userWeightKg: Double) {
        val dateKey = dateKeyForOffset(_selectedDateOffset.value)
        val burned = NutritionCalculator.calculateBurnedCalories(item.metValue, userWeightKg, durationMinutes)
        viewModelScope.launch {
            runSafe {
                repository.addWorkoutLog(
                    WorkoutLog(
                        dateKey = dateKey,
                        titleAr = item.titleAr,
                        titleEn = item.titleEn,
                        category = item.category,
                        durationMinutes = durationMinutes.toLong(),
                        caloriesBurned = burned,
                        completed = true
                    )
                )
            }
        }
    }

    fun addCustomWorkout(
        titleAr: String,
        titleEn: String,
        category: String,
        durationMinutes: Long,
        caloriesBurned: Long
    ) {
        val dateKey = dateKeyForOffset(_selectedDateOffset.value)
        val cleanAr = titleAr.trim().ifBlank { titleEn.trim() }.ifBlank { "تمرين مخصص" }
        val cleanEn = titleEn.trim().ifBlank { titleAr.trim() }.ifBlank { "Custom Workout" }
        viewModelScope.launch {
            runSafe {
                repository.addWorkoutLog(
                    WorkoutLog(
                        dateKey = dateKey,
                        titleAr = cleanAr,
                        titleEn = cleanEn,
                        category = category,
                        durationMinutes = durationMinutes,
                        caloriesBurned = caloriesBurned,
                        completed = true
                    )
                )
            }
        }
    }

    fun toggleWorkout(workout: WorkoutLog) {
        viewModelScope.launch {
            runSafe { repository.toggleWorkoutCompleted(workout) }
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            runSafe { repository.deleteWorkoutLog(workoutId) }
        }
    }

    fun adjustWater(deltaMl: Long, currentStat: DailyStat) {
        val nextWater = (currentStat.waterMl + deltaMl).coerceIn(0L, 12000L)
        viewModelScope.launch {
            runSafe {
                repository.upsertDailyStat(
                    dateKey = currentStat.dateKey,
                    waterMl = nextWater,
                    steps = currentStat.steps,
                    weightKg = currentStat.weightKg
                )
            }
        }
    }

    fun addSteps(deltaSteps: Long, currentStat: DailyStat) {
        val nextSteps = (currentStat.steps + deltaSteps).coerceIn(0L, 100000L)
        viewModelScope.launch {
            runSafe {
                repository.upsertDailyStat(
                    dateKey = currentStat.dateKey,
                    waterMl = currentStat.waterMl,
                    steps = nextSteps,
                    weightKg = currentStat.weightKg
                )
            }
        }
    }

    fun logWeight(newWeightKg: Double, currentStat: DailyStat, profile: UserProfile) {
        val safeWeight = newWeightKg.coerceIn(30.0, 300.0)
        viewModelScope.launch {
            runSafe {
                repository.upsertDailyStat(
                    dateKey = currentStat.dateKey,
                    waterMl = currentStat.waterMl,
                    steps = currentStat.steps,
                    weightKg = safeWeight
                )
                repository.updateProfile(profile.copy(weightKg = safeWeight))
            }
        }
    }

    fun recalculateAndSaveTargets(
        profile: UserProfile,
        age: Int,
        gender: String,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevel: String,
        goalType: String
    ) {
        val targets = NutritionCalculator.calculateTargets(
            age = age,
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg,
            activityLevel = activityLevel,
            goalType = goalType
        )
        viewModelScope.launch {
            runSafe {
                repository.updateProfile(
                    profile.copy(
                        age = age.toLong(),
                        gender = gender,
                        heightCm = heightCm,
                        weightKg = weightKg,
                        targetWeightKg = targetWeightKg,
                        activityLevel = activityLevel,
                        goalType = goalType,
                        targetCalories = targets.calories,
                        targetCarbsG = targets.carbsG,
                        targetProteinG = targets.proteinG,
                        targetFatsG = targets.fatsG,
                        targetWaterMl = targets.waterMl
                    )
                )
            }
        }
    }

    fun updateLanguageAndTheme(lang: String, themeMode: String) {
        viewModelScope.launch {
            runSafe { repository.updateLanguageAndTheme(lang, themeMode) }
        }
    }

    fun addReminder(titleAr: String, titleEn: String, category: String, hour: Long, minute: Long) {
        viewModelScope.launch {
            runSafe {
                repository.addReminder(
                    ReminderItem(
                        titleAr = titleAr.ifBlank { titleEn },
                        titleEn = titleEn.ifBlank { titleAr },
                        category = category,
                        hour = hour,
                        minute = minute,
                        enabled = true
                    )
                )
            }
        }
    }

    fun toggleReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            runSafe { repository.toggleReminder(reminder) }
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            runSafe { repository.deleteReminder(reminderId) }
        }
    }

    fun addPaymentMethod(provider: String, label: String, identifier: String, makeDefault: Boolean) {
        val digits = identifier.trim()
        val masked = if (digits.length >= 4) "•••• ${digits.takeLast(4)}" else "•••• $digits"
        viewModelScope.launch {
            runSafe {
                repository.addPaymentMethod(
                    PaymentMethodItem(
                        provider = provider,
                        label = label,
                        maskedIdentifier = masked,
                        isDefault = makeDefault
                    )
                )
            }
        }
    }

    fun setDefaultPaymentMethod(methodId: String) {
        viewModelScope.launch {
            runSafe { repository.setDefaultPaymentMethod(methodId) }
        }
    }

    fun deletePaymentMethod(methodId: String) {
        viewModelScope.launch {
            runSafe { repository.deletePaymentMethod(methodId) }
        }
    }

    fun activateSubscription(tier: String, numericId: String, onSuccessMsg: String) {
        viewModelScope.launch {
            runSafe {
                repository.activateSubscription(tier, numericId)
                _actionMessage.value = onSuccessMsg
            }
        }
    }

    private suspend fun runSafe(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Operation failed", e)
            _actionMessage.value = e.localizedMessage ?: "Operation failed"
        }
    }

    private fun pickContextualTip(
        consumedCalories: Long,
        targetCalories: Long,
        proteinG: Double,
        targetProteinG: Long,
        waterMl: Long,
        targetWaterMl: Long,
        workoutsCount: Int
    ): NutritionTip {
        val tips = HealthCatalog.tips
        return when {
            waterMl < targetWaterMl * 0.4 -> tips.first { it.id == "tip_hydration" }
            proteinG < targetProteinG * 0.5 -> tips.first { it.id == "tip_protein_pacing" }
            workoutsCount > 0 -> tips.first { it.id == "tip_post_workout" }
            consumedCalories > targetCalories -> tips.first { it.id == "tip_fiber_first" }
            else -> tips.first { it.id == "tip_smart_carbs" }
        }
    }

    private fun dateKeyForOffset(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun formatDisplayDate(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return SimpleDateFormat("EEE, dd MMM", Locale.US).format(cal.time)
    }

    private fun shortDayLabel(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return SimpleDateFormat("EE", Locale.US).format(Date(cal.timeInMillis)).take(2)
    }

    private data class CoreBundle(
        val profile: UserProfile?,
        val meals: List<MealLog>,
        val workouts: List<WorkoutLog>,
        val stats: List<DailyStat>,
        val reminders: List<ReminderItem>
    )

    private companion object {
        const val TAG = "HealthViewModel"
    }
}
