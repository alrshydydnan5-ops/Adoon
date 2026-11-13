package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.CalorieHeroCard
import com.example.ui.components.DateSwitcherBar
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.MacroCarbsColor
import com.example.ui.theme.MacroFatsColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.theme.WaterBlueColor
import com.example.ui.theme.WorkoutPurpleColor
import com.example.ui.viewmodel.HealthDashboardData

@Composable
fun DashboardScreen(
    data: HealthDashboardData,
    isToday: Boolean,
    onShiftDate: (Int) -> Unit,
    onAddWater: (Long) -> Unit,
    onAddSteps: (Long) -> Unit,
    onGoToMeals: () -> Unit,
    onGoToWorkouts: () -> Unit,
    onGoToTips: () -> Unit,
    onGoToReminders: () -> Unit
) {
    val strings = LocalAppStrings.current
    val waterProgress by animateFloatAsState(
        targetValue = (data.statForDay.waterMl.toFloat() / data.profile.targetWaterMl.coerceAtLeast(1L).toFloat())
            .coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 280f),
        label = "water_progress"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DateSwitcherBar(
                dateLabel = data.selectedDateLabel,
                isToday = isToday,
                onPreviousDay = { onShiftDate(-1) },
                onNextDay = { onShiftDate(1) }
            )
        }

        item {
            CalorieHeroCard(
                targetCalories = data.profile.targetCalories,
                consumedCalories = data.consumedCalories,
                burnedCalories = data.burnedCalories,
                carbsG = data.carbsG,
                targetCarbsG = data.profile.targetCarbsG,
                proteinG = data.proteinG,
                targetProteinG = data.profile.targetProteinG,
                fatsG = data.fatsG,
                targetFatsG = data.profile.targetFatsG
            )
        }

        // Quick Actions Grid
        item {
            Text(
                text = strings.quickActions,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Restaurant,
                    label = strings.addMealBtn,
                    tint = MacroCarbsColor,
                    testTag = "quick_add_meal",
                    onClick = onGoToMeals
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FitnessCenter,
                    label = strings.logWorkoutBtn,
                    tint = WorkoutPurpleColor,
                    testTag = "quick_add_workout",
                    onClick = onGoToWorkouts
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Lightbulb,
                    label = strings.nutritionTipsBtn,
                    tint = MacroFatsColor,
                    testTag = "quick_tips",
                    onClick = onGoToTips
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.NotificationsActive,
                    label = strings.remindersBtn,
                    tint = MaterialTheme.colorScheme.primary,
                    testTag = "quick_reminders",
                    onClick = onGoToReminders
                )
            }
        }

        // Hydration & Steps Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(WaterBlueColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = strings.hydrationTitle,
                                    tint = WaterBlueColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.hydrationTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${data.statForDay.waterMl} / ${data.profile.targetWaterMl} ${strings.mlUnit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WaterBlueColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onAddWater(-250L) },
                                modifier = Modifier.testTag("water_minus_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Remove 250ml",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { waterProgress },
                        color = WaterBlueColor,
                        trackColor = WaterBlueColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onAddWater(250L) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("water_add_250"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.addWater250)
                        }
                        Button(
                            onClick = { onAddWater(500L) },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlueColor),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("water_add_500"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.addWater500, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Daily Steps Sub-row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                    contentDescription = strings.stepsLabel,
                                    tint = MacroProteinColor
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings.stepsLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${data.statForDay.steps}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = { onAddSteps(1000L) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("add_steps_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.logStepsBtn)
                            }
                        }
                    }
                }
            }
        }

        // Smart Contextual Nutrition Tip Card
        item {
            val tip = data.smartTip
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.smartTipHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = if (strings.isArabic) tip.badgeAr else tip.badgeEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (strings.isArabic) tip.titleAr else tip.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (strings.isArabic) tip.bodyAr else tip.bodyEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Meals Today Summary Preview
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.mealsToday} (${data.mealsForDay.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilledTonalButton(
                            onClick = onGoToMeals,
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.addMealBtn)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if (data.mealsForDay.isEmpty()) {
                        Text(
                            text = strings.emptyMealsSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        data.mealsForDay.take(4).forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (strings.isArabic) meal.nameAr else meal.nameEn,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "C: ${meal.carbsG.toInt()}g • P: ${meal.proteinG.toInt()}g • F: ${meal.fatsG.toInt()}g",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${meal.calories} ${strings.kcalUnit}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MacroCarbsColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Workouts Today Summary Preview
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.workoutsToday} (${data.workoutsForDay.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilledTonalButton(
                            onClick = onGoToWorkouts,
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.logWorkoutBtn)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if (data.workoutsForDay.isEmpty()) {
                        Text(
                            text = strings.emptyWorkoutsSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        data.workoutsForDay.take(3).forEach { w ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (strings.isArabic) w.titleAr else w.titleEn,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${w.durationMinutes} ${strings.minUnit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "-${w.caloriesBurned} ${strings.kcalUnit}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = WorkoutPurpleColor
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
