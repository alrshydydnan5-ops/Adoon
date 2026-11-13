package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import com.example.data.catalog.HealthCatalog
import com.example.data.model.UserProfile
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.MacroCarbsColor
import com.example.ui.theme.MacroFatsColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.viewmodel.HealthDashboardData

@Composable
fun ProfileAndSubscriptionScreen(
    data: HealthDashboardData,
    currentThemeMode: String,
    onRecalculateTargets: (Int, String, Double, Double, Double, String, String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectThemeMode: (String) -> Unit,
    onActivateSubscription: (String, String, String) -> Unit,
    onAddPaymentMethod: (String, String, String, Boolean) -> Unit,
    onSetDefaultPaymentMethod: (String) -> Unit,
    onDeletePaymentMethod: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    val profile = data.profile

    var ageInput by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var gender by remember(profile.gender) { mutableStateOf(profile.gender) }
    var heightInput by remember(profile.heightCm) { mutableStateOf(profile.heightCm.toInt().toString()) }
    var weightInput by remember(profile.weightKg) { mutableStateOf(profile.weightKg.toString()) }
    var targetWeightInput by remember(profile.targetWeightKg) { mutableStateOf(profile.targetWeightKg.toString()) }
    var activityLevel by remember(profile.activityLevel) { mutableStateOf(profile.activityLevel) }
    var goalType by remember(profile.goalType) { mutableStateOf(profile.goalType) }

    var selectedTier by remember(profile.subscriptionTier) {
        mutableStateOf(if (profile.subscriptionTier == "free") "annual" else profile.subscriptionTier)
    }
    var numericSubId by remember(profile.subscriptionNumber) { mutableStateOf(profile.subscriptionNumber) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }

    val paymentProviders = remember(strings.isArabic) {
        listOf(
            "mada" to if (strings.isArabic) "مدى (Mada)" else "Mada Bank Card",
            "apple_google_pay" to if (strings.isArabic) "Apple / Google Pay" else "Apple / Google Pay",
            "visa_master" to if (strings.isArabic) "فيزا / ماستركارد" else "Visa / Mastercard",
            "stc_pay" to if (strings.isArabic) "STC Pay" else "STC Pay Wallet",
            "tabby" to if (strings.isArabic) "تابي (4 دفعات)" else "Tabby Split (4x)",
            "tamara" to if (strings.isArabic) "تمارا (تقسيط)" else "Tamara Pay Later",
            "vodafone_cash" to if (strings.isArabic) "فودافون كاش / محفظة" else "E-Wallet / Cash",
            "numeric_voucher" to if (strings.isArabic) "قسيمة رقمية" else "Numeric Voucher"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Subscription & Simple Numeric ID Card
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.subscriptionTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (profile.subscriptionActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        val badgeText = if (profile.subscriptionActive) {
                            "${strings.activeMemberBadge} • #${profile.subscriptionNumber}"
                        } else {
                            strings.freeMemberBadge
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (profile.subscriptionActive) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = strings.subscriptionSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    // Plans horizontal selector
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(HealthCatalog.plans, key = { it.tier }) { plan ->
                            val isSelected = selectedTier == plan.tier
                            Card(
                                onClick = { selectedTier = plan.tier },
                                shape = RoundedCornerShape(18.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else null,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier
                                    .width(200.dp)
                                    .testTag("plan_${plan.tier}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (strings.isArabic) plan.titleAr else plan.titleEn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (strings.isArabic) plan.periodAr else plan.periodEn,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val saving = if (strings.isArabic) plan.savingsAr else plan.savingsEn
                                    if (saving != null) {
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MacroFatsColor.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = saving,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MacroFatsColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    val features = if (strings.isArabic) plan.featuresAr else plan.featuresEn
                                    features.take(3).forEach { feat ->
                                        Text(
                                            text = "• $feat",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Simple Numeric Subscription ID Input
                    OutlinedTextField(
                        value = numericSubId,
                        onValueChange = { numericSubId = it.filter(Char::isDigit).take(15) },
                        leadingIcon = { Icon(Icons.Default.Dialpad, contentDescription = null) },
                        label = { Text(strings.numericIdLabel) },
                        placeholder = { Text(strings.numericIdHint) },
                        supportingText = { Text(strings.numericIdHelper) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("numeric_subscription_input")
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (selectedTier == "free") {
                                onActivateSubscription("free", numericSubId, strings.subscriptionActivatedMsg)
                            } else if (numericSubId.length in 4..15) {
                                onActivateSubscription(selectedTier, numericSubId, strings.subscriptionActivatedMsg)
                            } else {
                                onShowMessage(strings.invalidNumericIdMsg)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("activate_subscription_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.activateSubscriptionBtn, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        // Multiple Payment Methods Card
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.paymentMethodsTitle,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        FilledTonalButton(
                            onClick = { showAddPaymentDialog = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_payment_method_button")
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.addBtn)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Quick Badges for Supported Payment Channels
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(paymentProviders) { (key, title) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                onClick = { showAddPaymentDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = title,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = title, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    if (data.paymentMethods.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        data.paymentMethods.forEach { method ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = method.label,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            if (method.isDefault) {
                                                Spacer(Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = strings.defaultBadge,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${method.provider.uppercase()} • ${method.maskedIdentifier}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!method.isDefault) {
                                            TextButton(onClick = { onSetDefaultPaymentMethod(method.id) }) {
                                                Text(strings.setDefaultBtn)
                                            }
                                        }
                                        IconButton(onClick = { onDeletePaymentMethod(method.id) }) {
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
                }
            }
        }

        // Macro & Calorie Calculator Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = strings.calculatorGoalsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Active Targets Pill Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${profile.targetCalories} ${strings.kcalUnit}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "C ${profile.targetCarbsG}g",
                            style = MaterialTheme.typography.labelLarge,
                            color = MacroCarbsColor
                        )
                        Text(
                            text = "P ${profile.targetProteinG}g",
                            style = MaterialTheme.typography.labelLarge,
                            color = MacroProteinColor
                        )
                        Text(
                            text = "F ${profile.targetFatsG}g",
                            style = MaterialTheme.typography.labelLarge,
                            color = MacroFatsColor
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageInput,
                            onValueChange = { ageInput = it.filter(Char::isDigit).take(3) },
                            label = { Text(strings.ageLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it.filter(Char::isDigit).take(3) },
                            label = { Text(strings.heightLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(strings.currentWeightLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetWeightInput,
                            onValueChange = { targetWeightInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(strings.targetWeightLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = gender == "male",
                            onClick = { gender = "male" },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text(strings.genderMale) }
                        SegmentedButton(
                            selected = gender == "female",
                            onClick = { gender = "female" },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text(strings.genderFemale) }
                    }

                    val goals = listOf(
                        "lose" to strings.goalLose,
                        "maintain" to strings.goalMaintain,
                        "gain" to strings.goalGain,
                        "high_protein" to strings.goalHighProtein,
                        "keto" to strings.goalKeto
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(goals) { (k, label) ->
                            FilterChip(
                                selected = goalType == k,
                                onClick = { goalType = k },
                                label = { Text(label) }
                            )
                        }
                    }

                    val activities = listOf(
                        "sedentary" to strings.activitySedentary,
                        "light" to strings.activityLight,
                        "moderate" to strings.activityModerate,
                        "active" to strings.activityActive,
                        "athlete" to strings.activityAthlete
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(activities) { (k, label) ->
                            FilterChip(
                                selected = activityLevel == k,
                                onClick = { activityLevel = k },
                                label = { Text(label) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onRecalculateTargets(
                                (ageInput.toIntOrNull() ?: 28).coerceIn(12, 100),
                                gender,
                                (heightInput.toDoubleOrNull() ?: 175.0).coerceIn(100.0, 250.0),
                                (weightInput.toDoubleOrNull() ?: 75.0).coerceIn(30.0, 300.0),
                                (targetWeightInput.toDoubleOrNull() ?: 70.0).coerceIn(30.0, 300.0),
                                activityLevel,
                                goalType
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("recalculate_targets_button")
                    ) {
                        Text(strings.recalculateBtn)
                    }
                }
            }
        }

        // Language & Theme Settings Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.languageLabel, style = MaterialTheme.typography.titleMedium)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = strings.isArabic,
                            onClick = { onSelectLanguage("ar") },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            modifier = Modifier.testTag("lang_ar_button")
                        ) { Text("العربية") }
                        SegmentedButton(
                            selected = !strings.isArabic,
                            onClick = { onSelectLanguage("en") },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            modifier = Modifier.testTag("lang_en_button")
                        ) { Text("English") }
                    }

                    Text(strings.themeLabel, style = MaterialTheme.typography.titleMedium)
                    val themes = listOf(
                        "system" to strings.themeSystem,
                        "light" to strings.themeLight,
                        "dark" to strings.themeDark
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themes.forEachIndexed { idx, (key, label) ->
                            SegmentedButton(
                                selected = currentThemeMode == key,
                                onClick = { onSelectThemeMode(key) },
                                shape = SegmentedButtonDefaults.itemShape(idx, themes.size),
                                modifier = Modifier.testTag("theme_${key}_button")
                            ) { Text(label) }
                        }
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.signOut)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAddPaymentDialog) {
        var selectedProvider by remember { mutableStateOf("mada") }
        var label by remember { mutableStateOf("") }
        var identifier by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPaymentDialog = false },
            title = { Text(strings.addPaymentMethodBtn) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(paymentProviders) { (key, title) ->
                            FilterChip(
                                selected = selectedProvider == key,
                                onClick = { selectedProvider = key },
                                label = { Text(title) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(strings.paymentLabelHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it.filter(Char::isDigit).take(12) },
                        label = { Text(strings.paymentIdentifierHint) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val providerName = paymentProviders.firstOrNull { it.first == selectedProvider }?.second
                            ?: selectedProvider
                        val finalLabel = label.trim().ifBlank { providerName }
                        val finalId = identifier.trim().ifBlank { "4821" }
                        onAddPaymentMethod(selectedProvider, finalLabel, finalId, true)
                        showAddPaymentDialog = false
                    }
                ) {
                    Text(strings.saveBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPaymentDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }
}
