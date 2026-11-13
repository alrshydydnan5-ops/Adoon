package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.preferences.UserPrefsDataStore
import com.example.data.repository.HealthRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.NutriPulseTopBar
import com.example.ui.i18n.ArabicStrings
import com.example.ui.i18n.EnglishStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MealsScreen
import com.example.ui.screens.ProfileAndSubscriptionScreen
import com.example.ui.screens.ProgressAndTipsScreen
import com.example.ui.screens.WorkoutsScreen
import com.example.ui.theme.NutriPulseTheme
import com.example.ui.viewmodel.HealthViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriPulseAppRoot()
        }
    }
}

@Composable
fun NutriPulseAppRoot() {
    val context = LocalContext.current
    val prefs = remember(context) { UserPrefsDataStore(context.applicationContext) }
    val language by prefs.languageFlow.collectAsStateWithLifecycle(initialValue = "ar")
    val themeMode by prefs.themeModeFlow.collectAsStateWithLifecycle(initialValue = "system")
    val scope = rememberCoroutineScope()

    val isArabic = language == "ar"
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val appStrings = if (isArabic) ArabicStrings else EnglishStrings
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalAppStrings provides appStrings,
        LocalLayoutDirection provides layoutDirection
    ) {
        NutriPulseTheme(darkTheme = isDark, isArabic = isArabic) {
            AppNavigation(
                isDark = isDark,
                currentLanguage = language,
                currentThemeMode = themeMode,
                onSetLanguage = { lang ->
                    scope.launch { prefs.setLanguage(lang) }
                },
                onSetThemeMode = { mode ->
                    scope.launch { prefs.setThemeMode(mode) }
                }
            )
        }
    }
}

@Composable
private fun AppNavigation(
    isDark: Boolean,
    currentLanguage: String,
    currentThemeMode: String,
    onSetLanguage: (String) -> Unit,
    onSetThemeMode: (String) -> Unit
) {
    val auth = Firebase.auth
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            isDark = isDark,
            onToggleLanguage = {
                onSetLanguage(if (currentLanguage == "ar") "en" else "ar")
            },
            onToggleTheme = {
                onSetThemeMode(if (isDark) "light" else "dark")
            },
            onAuthSuccess = {
                currentUser = auth.currentUser
            }
        )
    } else {
        val uid = user.uid
        val displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Athlete"
        key(uid) {
            MainHealthScaffold(
                currentUserId = uid,
                initialDisplayName = displayName,
                isDark = isDark,
                currentLanguage = currentLanguage,
                currentThemeMode = currentThemeMode,
                onSetLanguage = onSetLanguage,
                onSetThemeMode = onSetThemeMode
            )
        }
    }
}

private enum class MainTab(
    val index: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(0, Icons.Filled.Home, Icons.Outlined.Home),
    MEALS(1, Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    WORKOUTS(2, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    PROGRESS(3, Icons.Filled.Analytics, Icons.Outlined.Analytics),
    PROFILE(4, Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
private fun MainHealthScaffold(
    currentUserId: String,
    initialDisplayName: String,
    isDark: Boolean,
    currentLanguage: String,
    currentThemeMode: String,
    onSetLanguage: (String) -> Unit,
    onSetThemeMode: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: HealthViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                HealthViewModel(
                    repository = HealthRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName,
                    initialLanguage = currentLanguage
                )
            }
        }
    )

    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val dateOffset by viewModel.selectedDateOffset.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(MainTab.HOME.index) }
    var progressSubTab by remember { mutableIntStateOf(0) }

    if (selectedTab != MainTab.HOME.index) {
        BackHandler { selectedTab = MainTab.HOME.index }
    }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val tabLabels = listOf(
        strings.tabHome,
        strings.tabMeals,
        strings.tabWorkouts,
        strings.tabProgress,
        strings.tabProfile
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        MainTab.entries.forEach { tab ->
                            val label = tabLabels[tab.index]
                            val selected = selectedTab == tab.index
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedTab = tab.index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        MainTab.entries.forEach { tab ->
                            val label = tabLabels[tab.index]
                            val selected = selectedTab == tab.index
                            NavigationRailItem(
                                selected = selected,
                                onClick = { selectedTab = tab.index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label, maxLines = 1) }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (useRail) Modifier else Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        is UiState.Success -> {
                            val data = state.data
                            NutriPulseTopBar(
                                displayName = data.profile.displayName,
                                isPro = data.profile.subscriptionActive,
                                isDark = isDark,
                                onToggleLanguage = {
                                    val nextLang = if (currentLanguage == "ar") "en" else "ar"
                                    onSetLanguage(nextLang)
                                    viewModel.updateLanguageAndTheme(nextLang, currentThemeMode)
                                },
                                onToggleTheme = {
                                    val nextTheme = if (isDark) "light" else "dark"
                                    onSetThemeMode(nextTheme)
                                    viewModel.updateLanguageAndTheme(currentLanguage, nextTheme)
                                },
                                onOpenPro = { selectedTab = MainTab.PROFILE.index }
                            )

                            when (selectedTab) {
                                MainTab.HOME.index -> DashboardScreen(
                                    data = data,
                                    isToday = dateOffset == 0,
                                    onShiftDate = viewModel::shiftDate,
                                    onAddWater = { delta -> viewModel.adjustWater(delta, data.statForDay) },
                                    onAddSteps = { delta -> viewModel.addSteps(delta, data.statForDay) },
                                    onGoToMeals = { selectedTab = MainTab.MEALS.index },
                                    onGoToWorkouts = { selectedTab = MainTab.WORKOUTS.index },
                                    onGoToTips = {
                                        progressSubTab = 1
                                        selectedTab = MainTab.PROGRESS.index
                                    },
                                    onGoToReminders = {
                                        progressSubTab = 2
                                        selectedTab = MainTab.PROGRESS.index
                                    }
                                )

                                MainTab.MEALS.index -> MealsScreen(
                                    data = data,
                                    onAddCatalogMeal = viewModel::addCatalogMeal,
                                    onAddCustomMeal = viewModel::addCustomMeal,
                                    onDeleteMeal = viewModel::deleteMeal
                                )

                                MainTab.WORKOUTS.index -> WorkoutsScreen(
                                    data = data,
                                    onAddCatalogWorkout = { item, minutes ->
                                        viewModel.addCatalogWorkout(item, minutes, data.profile.weightKg)
                                    },
                                    onAddCustomWorkout = viewModel::addCustomWorkout,
                                    onToggleWorkout = viewModel::toggleWorkout,
                                    onDeleteWorkout = viewModel::deleteWorkout,
                                    onAddSteps = { delta -> viewModel.addSteps(delta, data.statForDay) }
                                )

                                MainTab.PROGRESS.index -> ProgressAndTipsScreen(
                                    data = data,
                                    initialSection = progressSubTab,
                                    onLogWeight = { w -> viewModel.logWeight(w, data.statForDay, data.profile) },
                                    onAddReminder = viewModel::addReminder,
                                    onToggleReminder = viewModel::toggleReminder,
                                    onDeleteReminder = viewModel::deleteReminder,
                                    onShowMessage = { msg ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                )

                                MainTab.PROFILE.index -> ProfileAndSubscriptionScreen(
                                    data = data,
                                    currentThemeMode = currentThemeMode,
                                    onRecalculateTargets = { age, gender, h, w, tw, act, goal ->
                                        viewModel.recalculateAndSaveTargets(
                                            data.profile,
                                            age,
                                            gender,
                                            h,
                                            w,
                                            tw,
                                            act,
                                            goal
                                        )
                                    },
                                    onSelectLanguage = { lang ->
                                        onSetLanguage(lang)
                                        viewModel.updateLanguageAndTheme(lang, currentThemeMode)
                                    },
                                    onSelectThemeMode = { mode ->
                                        onSetThemeMode(mode)
                                        viewModel.updateLanguageAndTheme(currentLanguage, mode)
                                    },
                                    onActivateSubscription = viewModel::activateSubscription,
                                    onAddPaymentMethod = viewModel::addPaymentMethod,
                                    onSetDefaultPaymentMethod = viewModel::setDefaultPaymentMethod,
                                    onDeletePaymentMethod = viewModel::deletePaymentMethod,
                                    onShowMessage = { msg ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                    },
                                    onSignOut = {
                                        signOutUser(credentialManager, coroutineScope)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
