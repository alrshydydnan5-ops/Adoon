package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.HealthCatalog
import com.example.data.model.NutritionCalculator
import com.example.data.model.WorkoutCatalogItem
import com.example.data.model.WorkoutLog
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.WorkoutPurpleColor
import com.example.ui.viewmodel.HealthDashboardData
import kotlin.math.roundToInt

@Composable
fun WorkoutsScreen(
    data: HealthDashboardData,
    onAddCatalogWorkout: (WorkoutCatalogItem, Int) -> Unit,
    onAddCustomWorkout: (String, String, String, Long, Long) -> Unit,
    onToggleWorkout: (WorkoutLog) -> Unit,
    onDeleteWorkout: (String) -> Unit,
    onAddSteps: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedCatalogItem by remember { mutableStateOf<WorkoutCatalogItem?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "all" to strings.allCategories,
        "strength" to if (strings.isArabic) "مقاومة" else "Strength",
        "cardio" to if (strings.isArabic) "كارديو" else "Cardio",
        "hiit" to "HIIT",
        "sports" to if (strings.isArabic) "رياضة" else "Sports",
        "flexibility" to if (strings.isArabic) "مرونة" else "Mobility"
    )

    val filteredCatalog = remember(selectedCategory) {
        if (selectedCategory == "all") HealthCatalog.workouts
        else HealthCatalog.workouts.filter { it.category == selectedCategory }
    }

    val totalMinutes = data.workoutsForDay.filter { it.completed }.sumOf { it.durationMinutes }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomDialog = true },
                containerColor = WorkoutPurpleColor,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_custom_workout")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.customWorkoutTitle)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Athletic Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_workout_banner),
                        contentDescription = strings.tabWorkouts,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xDD0B1312), Color(0x660B1312))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFF34D399)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${data.burnedCalories} ${strings.kcalUnit} • $totalMinutes ${strings.minUnit}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (strings.isArabic) "السعرات المحروقة تزيد رصيدك اليومي تلقائياً"
                            else "Burned calories automatically boost today's energy budget",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD1FAE5)
                        )
                    }
                }
            }

            // Steps Quick Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = strings.stepsLabel,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.stepsLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${data.statForDay.steps}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onAddSteps(1000L) },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("+1K")
                            }
                            Button(
                                onClick = { onAddSteps(2500L) },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("+2.5K")
                            }
                        }
                    }
                }
            }

            // Today's Workouts
            item {
                Text(
                    text = "${strings.workoutsToday} (${data.workoutsForDay.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (data.workoutsForDay.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = WorkoutPurpleColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(strings.emptyWorkoutsTitle, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    strings.emptyWorkoutsSubtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(data.workoutsForDay, key = { it.id }) { workout ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { onToggleWorkout(workout) },
                                modifier = Modifier.testTag("toggle_workout_${workout.id}")
                            ) {
                                Icon(
                                    imageVector = if (workout.completed) Icons.Default.CheckCircle
                                    else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = strings.completedLabel,
                                    tint = if (workout.completed) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (strings.isArabic) workout.titleAr else workout.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${workout.durationMinutes} ${strings.minUnit} • -${workout.caloriesBurned} ${strings.kcalUnit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WorkoutPurpleColor
                                )
                            }
                            IconButton(onClick = { onDeleteWorkout(workout.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = strings.deleteBtn,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter & Exercise Catalog
            item {
                Text(
                    text = strings.workoutCatalogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            items(filteredCatalog, key = { it.id }) { item ->
                val previewBurn = NutritionCalculator.calculateBurnedCalories(
                    item.metValue,
                    data.profile.weightKg,
                    item.defaultMinutes
                )
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.isArabic) item.titleAr else item.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WorkoutPurpleColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = if (strings.isArabic) item.intensityAr else item.intensityEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WorkoutPurpleColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${item.defaultMinutes} ${strings.minUnit} • ~$previewBurn ${strings.kcalUnit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { selectedCatalogItem = item },
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoutPurpleColor),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("log_workout_${item.id}")
                        ) {
                            Text(strings.addBtn, color = Color.White)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    selectedCatalogItem?.let { item ->
        var duration by remember(item) { mutableFloatStateOf(item.defaultMinutes.toFloat()) }
        val estimatedBurn = NutritionCalculator.calculateBurnedCalories(
            item.metValue,
            data.profile.weightKg,
            duration.roundToInt()
        )
        AlertDialog(
            onDismissRequest = { selectedCatalogItem = null },
            title = { Text(if (strings.isArabic) item.titleAr else item.titleEn) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${strings.durationMinutesLabel}: ${duration.roundToInt()} ${strings.minUnit}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = duration,
                        onValueChange = { duration = it },
                        valueRange = 10f..120f
                    )
                    Text(
                        text = "${strings.estimatedBurnLabel}: $estimatedBurn ${strings.kcalUnit}",
                        style = MaterialTheme.typography.titleMedium,
                        color = WorkoutPurpleColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCatalogWorkout(item, duration.roundToInt())
                        selectedCatalogItem = null
                    }
                ) {
                    Text(strings.saveBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCatalogItem = null }) { Text(strings.cancelBtn) }
            }
        )
    }

    if (showCustomDialog) {
        var title by remember { mutableStateOf("") }
        var minutes by remember { mutableStateOf("40") }
        var burned by remember { mutableStateOf("280") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text(strings.customWorkoutTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (strings.isArabic) "اسم التمرين" else "Workout Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit) },
                        label = { Text(strings.durationMinutesLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = burned,
                        onValueChange = { burned = it.filter(Char::isDigit) },
                        label = { Text(strings.estimatedBurnLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fallback = if (strings.isArabic) "تمرين مخصص" else "Custom Workout"
                        val finalTitle = title.trim().ifBlank { fallback }
                        onAddCustomWorkout(
                            finalTitle,
                            finalTitle,
                            "strength",
                            (minutes.toLongOrNull() ?: 30L).coerceIn(1L, 600L),
                            (burned.toLongOrNull() ?: 200L).coerceIn(0L, 5000L)
                        )
                        showCustomDialog = false
                    }
                ) {
                    Text(strings.saveBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }
}
