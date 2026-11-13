package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.catalog.HealthCatalog
import com.example.data.model.NutritionCalculator
import com.example.data.model.ReminderItem
import com.example.notifications.ReminderScheduler
import com.example.ui.components.WeeklyCaloriesBarChart
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.MacroFatsColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.viewmodel.HealthDashboardData
import java.util.Locale

@Composable
fun ProgressAndTipsScreen(
    data: HealthDashboardData,
    initialSection: Int = 0,
    onLogWeight: (Double) -> Unit,
    onAddReminder: (String, String, String, Long, Long) -> Unit,
    onToggleReminder: (ReminderItem) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var selectedSection by remember(initialSection) { mutableIntStateOf(initialSection) }

    val sections = listOf(
        strings.progressTitle,
        strings.nutritionTipsTitle,
        strings.remindersTitle
    )

    // Keep system AlarmManager synchronized with user's active reminders
    LaunchedEffect(data.reminders, strings.isArabic) {
        ReminderScheduler.syncReminders(context, data.reminders, strings.isArabic)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            sections.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size),
                    modifier = Modifier.testTag("progress_segment_$index")
                ) {
                    Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        when (selectedSection) {
            0 -> AnalyticsSection(data = data, onLogWeight = onLogWeight)
            1 -> TipsSection()
            else -> RemindersSection(
                data = data,
                onAddReminder = onAddReminder,
                onToggleReminder = onToggleReminder,
                onDeleteReminder = onDeleteReminder,
                onShowMessage = onShowMessage
            )
        }
    }
}

@Composable
private fun AnalyticsSection(
    data: HealthDashboardData,
    onLogWeight: (Double) -> Unit
) {
    val strings = LocalAppStrings.current
    var showWeightDialog by remember { mutableStateOf(false) }

    val targets = remember(data.profile) {
        NutritionCalculator.calculateTargets(
            age = data.profile.age.toInt(),
            gender = data.profile.gender,
            heightCm = data.profile.heightCm,
            weightKg = data.profile.weightKg,
            activityLevel = data.profile.activityLevel,
            goalType = data.profile.goalType
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Streak & Metabolic Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = strings.streakDaysLabel,
                    value = "${data.streakDays}",
                    icon = Icons.Default.EmojiEvents
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = strings.bmiLabel,
                    value = "${targets.bmi}",
                    icon = Icons.Default.MonitorWeight
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = strings.bmrLabel,
                    value = "${targets.bmr} ${strings.kcalUnit}",
                    icon = null
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = strings.tdeeLabel,
                    value = "${targets.tdee} ${strings.kcalUnit}",
                    icon = null
                )
            }
        }

        // Weekly Calorie Chart
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.weeklyCaloriesChart,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${strings.targetLabel}: ${data.profile.targetCalories} ${strings.kcalUnit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MacroFatsColor
                    )
                    Spacer(Modifier.height(8.dp))
                    WeeklyCaloriesBarChart(
                        points = data.weeklyHistory,
                        targetCalories = data.profile.targetCalories
                    )
                }
            }
        }

        // Weight Tracker Card
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
                            text = strings.weightTrackerTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = { showWeightDialog = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("log_weight_button")
                        ) {
                            Text(strings.logWeightBtn)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeightStatColumn(
                            label = strings.startWeightLabel,
                            value = "${data.profile.startWeightKg} ${strings.kgUnit}"
                        )
                        WeightStatColumn(
                            label = strings.currentWeightLabel,
                            value = "${data.profile.weightKg} ${strings.kgUnit}",
                            highlight = true
                        )
                        WeightStatColumn(
                            label = strings.targetWeightLabel,
                            value = "${data.profile.targetWeightKg} ${strings.kgUnit}"
                        )
                    }
                }
            }
        }
    }

    if (showWeightDialog) {
        var weightInput by remember { mutableStateOf(data.profile.weightKg.toString()) }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text(strings.logWeightBtn) },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("${strings.currentWeightLabel} (${strings.kgUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        weightInput.toDoubleOrNull()?.let { onLogWeight(it) }
                        showWeightDialog = false
                    }
                ) {
                    Text(strings.saveBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }
}

@Composable
private fun MetricSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WeightStatColumn(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TipsSection() {
    val strings = LocalAppStrings.current
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to strings.allCategories,
        "protein" to strings.proteinLabel,
        "carbs" to strings.carbsLabel,
        "hydration" to if (strings.isArabic) "ترطيب" else "Hydration",
        "weight_loss" to if (strings.isArabic) "تنشيف" else "Fat Loss",
        "workout" to if (strings.isArabic) "تمارين" else "Recovery"
    )

    val tips = remember(selectedCategory) {
        if (selectedCategory == "all") HealthCatalog.tips
        else HealthCatalog.tips.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (k, label) ->
                    FilterChip(
                        selected = selectedCategory == k,
                        onClick = { selectedCategory = k },
                        label = { Text(label) }
                    )
                }
            }
        }

        items(tips, key = { it.id }) { tip ->
            Card(
                shape = RoundedCornerShape(22.dp),
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
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MacroFatsColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (strings.isArabic) tip.titleAr else tip.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (strings.isArabic) tip.badgeAr else tip.badgeEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (strings.isArabic) tip.bodyAr else tip.bodyEn,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RemindersSection(
    data: HealthDashboardData,
    onAddReminder: (String, String, String, Long, Long) -> Unit,
    onToggleReminder: (ReminderItem) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.sendTestNotification(context, strings.isArabic)
            onShowMessage(strings.notificationSentMsg)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = strings.remindersTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = strings.enableNotificationsHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    ReminderScheduler.sendTestNotification(context, strings.isArabic)
                                    onShowMessage(strings.notificationSentMsg)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("test_notification_button")
                        ) {
                            Text(strings.testNotificationBtn, maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_reminder_button")
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.addBtn)
                        }
                    }
                }
            }
        }

        // Preset Quick Reminders
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        onAddReminder("شرب الماء (الصباح)", "Morning Hydration", "water", 9L, 0L)
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (strings.isArabic) "+ تنبيه ماء" else "+ Water", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = {
                        onAddReminder("تذكير التمرين اليومي", "Daily Workout Time", "workout", 18L, 0L)
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (strings.isArabic) "+ تنبيه تمرين" else "+ Workout", maxLines = 1)
                }
            }
        }

        items(data.reminders, key = { it.id }) { reminder ->
            val timeFormatted = String.format(Locale.US, "%02d:%02d", reminder.hour, reminder.minute)
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
                            text = if (strings.isArabic) reminder.titleAr else reminder.titleEn,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MacroProteinColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = reminder.enabled,
                            onCheckedChange = { onToggleReminder(reminder) }
                        )
                        IconButton(onClick = { onDeleteReminder(reminder.id) }) {
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
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var hour by remember { mutableStateOf("14") }
        var minute by remember { mutableStateOf("30") }
        var category by remember { mutableStateOf("meal") }

        val cats = listOf(
            "water" to if (strings.isArabic) "ماء" else "Water",
            "meal" to if (strings.isArabic) "وجبة" else "Meal",
            "workout" to if (strings.isArabic) "تمرين" else "Workout",
            "weigh_in" to if (strings.isArabic) "وزن" else "Weight"
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.addReminderTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (strings.isArabic) "عنوان التنبيه" else "Reminder Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cats) { (k, l) ->
                            FilterChip(
                                selected = category == k,
                                onClick = { category = k },
                                label = { Text(l) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hour,
                            onValueChange = { hour = it.filter(Char::isDigit).take(2) },
                            label = { Text(if (strings.isArabic) "الساعة (0-23)" else "Hour (0-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minute,
                            onValueChange = { minute = it.filter(Char::isDigit).take(2) },
                            label = { Text(if (strings.isArabic) "الدقيقة (0-59)" else "Min (0-59)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = (hour.toLongOrNull() ?: 9L).coerceIn(0L, 23L)
                        val m = (minute.toLongOrNull() ?: 0L).coerceIn(0L, 59L)
                        val fallback = if (strings.isArabic) "تنبيه مخصص" else "Custom Alert"
                        val finalTitle = title.trim().ifBlank { fallback }
                        onAddReminder(finalTitle, finalTitle, category, h, m)
                        showAddDialog = false
                    }
                ) {
                    Text(strings.saveBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }
}
