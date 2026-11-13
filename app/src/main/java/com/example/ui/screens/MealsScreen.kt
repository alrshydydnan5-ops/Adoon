package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.HealthCatalog
import com.example.data.model.FoodCatalogItem
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.MacroCarbsColor
import com.example.ui.theme.MacroFatsColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.viewmodel.HealthDashboardData

@Composable
fun MealsScreen(
    data: HealthDashboardData,
    onAddCatalogMeal: (FoodCatalogItem, String) -> Unit,
    onAddCustomMeal: (String, String, String, Long, Double, Double, Double, String) -> Unit,
    onDeleteMeal: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("all") }
    var showCustomDialog by remember { mutableStateOf(false) }

    val mealTypes = listOf(
        "all" to strings.allCategories,
        "breakfast" to strings.breakfast,
        "lunch" to strings.lunch,
        "dinner" to strings.dinner,
        "snack" to strings.snack
    )

    val filteredCatalog = remember(searchQuery, selectedMealType) {
        HealthCatalog.foods.filter { item ->
            val matchesType = selectedMealType == "all" || item.mealType == selectedMealType
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                item.nameAr.lowercase().contains(q) ||
                item.nameEn.lowercase().contains(q)
            matchesType && matchesQuery
        }
    }

    val filteredLogs = remember(data.mealsForDay, selectedMealType) {
        if (selectedMealType == "all") data.mealsForDay
        else data.mealsForDay.filter { it.mealType == selectedMealType }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_custom_meal")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.customMealTitle)
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
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(strings.searchFoodHint) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_search_field")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mealTypes) { (key, label) ->
                        FilterChip(
                            selected = selectedMealType == key,
                            onClick = { selectedMealType = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Logged Meals Today
            item {
                Text(
                    text = "${strings.mealsToday} (${filteredLogs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (filteredLogs.isEmpty()) {
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
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.emptyMealsTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = strings.emptyMealsSubtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { meal ->
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
                                    text = if (strings.isArabic) meal.nameAr else meal.nameEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MacroBadge("C ${meal.carbsG.toInt()}g", MacroCarbsColor)
                                    MacroBadge("P ${meal.proteinG.toInt()}g", MacroProteinColor)
                                    MacroBadge("F ${meal.fatsG.toInt()}g", MacroFatsColor)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${meal.calories} ${strings.kcalUnit}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { onDeleteMeal(meal.id) },
                                    modifier = Modifier.testTag("delete_meal_${meal.id}")
                                ) {
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

            // Food Catalog
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (strings.isArabic) "مكتبة الأطعمة الصحية" else "Healthy Food Library",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredCatalog, key = { it.id }) { item ->
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
                                text = if (strings.isArabic) item.nameAr else item.nameEn,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${if (strings.isArabic) item.servingAr else item.servingEn} • ${item.calories} ${strings.kcalUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MacroBadge("${strings.carbsLabel.take(4)} ${item.carbsG.toInt()}g", MacroCarbsColor)
                                MacroBadge("${strings.proteinLabel.take(4)} ${item.proteinG.toInt()}g", MacroProteinColor)
                                MacroBadge("${strings.fatsLabel.take(4)} ${item.fatsG.toInt()}g", MacroFatsColor)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val targetType = if (selectedMealType == "all") item.mealType else selectedMealType
                                onAddCatalogMeal(item, targetType)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_catalog_food_${item.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = strings.addBtn, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.addBtn)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showCustomDialog) {
        CustomMealDialog(
            defaultMealType = if (selectedMealType == "all") "lunch" else selectedMealType,
            onDismiss = { showCustomDialog = false },
            onSave = { nameAr, nameEn, mealType, cal, carbs, protein, fats, serving ->
                onAddCustomMeal(nameAr, nameEn, mealType, cal, carbs, protein, fats, serving)
                showCustomDialog = false
            }
        )
    }
}

@Composable
private fun MacroBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CustomMealDialog(
    defaultMealType: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long, Double, Double, Double, String) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("350") }
    var carbs by remember { mutableStateOf("35") }
    var protein by remember { mutableStateOf("28") }
    var fats by remember { mutableStateOf("10") }
    var mealType by remember { mutableStateOf(defaultMealType) }

    val types = listOf(
        "breakfast" to strings.breakfast,
        "lunch" to strings.lunch,
        "dinner" to strings.dinner,
        "snack" to strings.snack
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.customMealTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (strings.isArabic) strings.mealNameArHint else strings.mealNameEnHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { (k, label) ->
                        FilterChip(
                            selected = mealType == k,
                            onClick = { mealType = k },
                            label = { Text(label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it.filter(Char::isDigit) },
                        label = { Text(strings.caloriesLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(strings.carbsLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(strings.proteinLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(strings.fatsLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val calVal = calories.toLongOrNull() ?: 250L
                    val cVal = carbs.toDoubleOrNull() ?: 20.0
                    val pVal = protein.toDoubleOrNull() ?: 20.0
                    val fVal = fats.toDoubleOrNull() ?: 8.0
                    val fallbackName = if (strings.isArabic) "وجبة صحية" else "Healthy Meal"
                    val finalName = name.trim().ifBlank { fallbackName }
                    onSave(finalName, finalName, mealType, calVal, cVal, pVal, fVal, "1 serving")
                }
            ) {
                Text(strings.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelBtn) }
        }
    )
}
