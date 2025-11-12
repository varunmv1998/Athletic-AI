package com.athleticai.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.ExerciseDbSyncCard
import com.athleticai.app.ui.components.ThemeGridSelector
import com.athleticai.app.ui.components.ThemePreviewSection
import com.athleticai.app.ui.components.ImageColorExtractorDialog
import com.athleticai.app.ui.components.parseCustomThemeColors
import com.athleticai.app.ui.components.toJson
import com.athleticai.app.ui.theme.ThemePreset
import com.athleticai.app.ui.theme.ColorExtractor
import com.athleticai.app.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToAchievementGuide: () -> Unit = {},
    onNavigateToExerciseDictionary: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val apiTestResult by viewModel.apiTestResult.collectAsState()
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearApiKeyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showImageExtractorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        MaterialSymbol(symbol = MaterialSymbols.ARROW_BACK, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme & Colors Section
            item {
                SettingsSectionHeader("Theme & Colors")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "App Theme",
                            subtitle = ThemePreset.valueOf(state.themePreset).displayName,
                            icon = MaterialSymbols.DARK_MODE,
                            onClick = { showThemeDialog = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsItem(
                            title = "Dark Mode",
                            subtitle = "Choose light or dark appearance",
                            icon = MaterialSymbols.DARK_MODE,
                            onClick = { }
                        ) {
                            ThemeSelector(
                                currentTheme = state.theme,
                                onThemeChange = viewModel::setTheme
                            )
                        }
                    }
                }
            }

            // Measurement Section
            item {
                SettingsSectionHeader("Measurements")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsItem(
                            title = "Weight Units",
                            subtitle = "Choose between kg or lbs",
                            icon = MaterialSymbols.SETTINGS,
                            onClick = { }
                        ) {
                            UnitsSelector(
                                currentUnits = state.units,
                                onUnitsChange = viewModel::setUnits
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsItem(
                            title = "Default Rest Timer",
                            subtitle = "${state.defaultRestTime} seconds",
                            icon = MaterialSymbols.TIMER,
                            onClick = { }
                        ) {
                            RestTimerSelector(
                                currentTime = state.defaultRestTime,
                                onTimeChange = viewModel::setDefaultRestTime
                            )
                        }
                    }
                }
            }

            // AI & Data Section
            item {
                SettingsSectionHeader("AI & Data")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "OpenAI API Key",
                            subtitle = if (state.hasApiKey) "Configured" else "Not configured",
                            icon = MaterialSymbols.KEY,
                            onClick = { showApiKeyDialog = true }
                        )
                        
                        if (state.hasApiKey) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            // API Key Test Section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.testApiKey() },
                                        modifier = Modifier.weight(1f),
                                        enabled = !apiTestResult.isLoading
                                    ) {
                                        if (apiTestResult.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(if (apiTestResult.isLoading) "Testing..." else "Test API Key")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { showClearApiKeyDialog = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Clear Key")
                                    }
                                }
                                
                            }
                            
                            // API Test Result Display
                            if (apiTestResult.message.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (apiTestResult.isSuccess) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = apiTestResult.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (apiTestResult.isSuccess) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.clearApiTestResult() },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            MaterialSymbol(
                                                symbol = MaterialSymbols.CLOSE,
                                                contentDescription = "Dismiss",
                                                tint = if (apiTestResult.isSuccess) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsToggleItem(
                            title = "AI Coaching",
                            subtitle = "Enable AI-powered workout suggestions",
                            icon = MaterialSymbols.SMART_TOY,
                            checked = state.aiCoachingEnabled,
                            onCheckedChange = viewModel::setAiCoachingEnabled
                        )
                    }
                }
            }

            // Notifications Section
            item {
                SettingsSectionHeader("Notifications")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsToggleItem(
                            title = "Workout Reminders",
                            subtitle = "Daily workout notifications",
                            icon = MaterialSymbols.NOTIFICATIONS,
                            checked = state.workoutReminders,
                            onCheckedChange = viewModel::setWorkoutReminders
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsClickableItem(
                            title = "Notification Schedule",
                            subtitle = "Set your preferred reminder times",
                            icon = MaterialSymbols.SCHEDULE,
                            onClick = { showNotificationDialog = true }
                        )
                    }
                }
            }

            // Data Management Section
            item {
                SettingsSectionHeader("Data Management")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "Export Data",
                            subtitle = "Export your workout data",
                            icon = MaterialSymbols.DOWNLOAD,
                            onClick = { viewModel.exportData() }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsClickableItem(
                            title = "Clear All Data",
                            subtitle = "Remove all workout and measurement data",
                            icon = MaterialSymbols.DELETE,
                            onClick = { viewModel.clearAllData() },
                            isDestructive = true
                        )
                    }
                }
            }

            // Exercise Database Section
            item {
                SettingsSectionHeader("Exercise Database")
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = "Exercise Dictionary",
                        subtitle = "Browse all available exercises with instructions",
                        icon = MaterialSymbols.LIST,
                        onClick = onNavigateToExerciseDictionary
                    )
                }
            }
            
            // Exercise Database Sync
            item {
                val syncStatus by viewModel.exerciseDbSyncStatus.collectAsState()
                val hasExercises = state.exerciseDbSyncStatus.exerciseCount > 0
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Exercise Database Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (hasExercises) {
                            "${state.exerciseDbSyncStatus.exerciseCount} exercises synced from ExerciseDB"
                        } else {
                            "Download 1,500+ exercises from ExerciseDB"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Button(
                        onClick = { viewModel.triggerExerciseDbSync() },
                        enabled = !syncStatus.isInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasExercises) 
                                MaterialTheme.colorScheme.secondary 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (syncStatus.isInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing...")
                        } else {
                            MaterialSymbol(
                                symbol = if (hasExercises) MaterialSymbols.REPEAT else MaterialSymbols.DOWNLOAD,
                                size = IconSizes.SMALL,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (hasExercises) "Re-Sync Exercises" else "Download Exercises")
                        }
                    }
                    
                    // Progress indicator
                    if (syncStatus.isInProgress && syncStatus.progress > 0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { syncStatus.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "${(syncStatus.progress * 100).toInt()}% complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Error message
                    syncStatus.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Developer Section
            item {
                SettingsSectionHeader("Developer")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsToggleItem(
                            title = "Test Mode",
                            subtitle = "Enable with sample data for testing UI and features",
                            icon = MaterialSymbols.SETTINGS,
                            checked = state.testModeEnabled,
                            onCheckedChange = viewModel::setTestModeEnabled
                        )
                    }
                }
            }

            // Achievements Section
            item {
                SettingsSectionHeader("Achievements")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "View All Achievements",
                            subtitle = "Track your progress and unlock badges",
                            icon = MaterialSymbols.STAR,
                            onClick = onNavigateToAchievements
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsClickableItem(
                            title = "Achievement Guide",
                            subtitle = "Learn how to unlock all achievements",
                            icon = MaterialSymbols.EMOJI_EVENTS,
                            onClick = onNavigateToAchievementGuide
                        )
                    }
                }
            }

            // About Section
            item {
                SettingsSectionHeader("About")
            }
            
            item {
                SettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "About Athletic AI",
                            subtitle = "Version info and credits",
                            icon = MaterialSymbols.INFO,
                            onClick = { showAboutDialog = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsClickableItem(
                            title = "Privacy Policy",
                            subtitle = "How we handle your data",
                            icon = MaterialSymbols.SECURITY,
                            onClick = { viewModel.openPrivacyPolicy() }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        SettingsClickableItem(
                            title = "Help & Support",
                            subtitle = "Get help using the app",
                            icon = MaterialSymbols.HELP,
                            onClick = { viewModel.openSupport() }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Dialogs
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = state.apiKey,
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                viewModel.setApiKey(key)
                showApiKeyDialog = false
            }
        )
    }
    
    if (showNotificationDialog) {
        NotificationScheduleDialog(
            currentTime = state.notificationTime,
            onDismiss = { showNotificationDialog = false },
            onSave = { time ->
                viewModel.setNotificationTime(time)
                showNotificationDialog = false
            }
        )
    }
    
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    if (showClearApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showClearApiKeyDialog = false },
            title = { Text("Clear API Key") },
            text = {
                Text(
                    text = "Are you sure you want to clear your OpenAI API key? This will disable all AI coaching features until you add a new key.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearApiKey()
                        showClearApiKeyDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Theme selection dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { 
                Text(
                    "Choose Theme",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThemeGridSelector(
                        currentPreset = state.themePreset,
                        onPresetSelected = { preset ->
                            viewModel.setThemePreset(preset.name)
                            showThemeDialog = false
                        },
                        onCustomThemeRequested = {
                            showThemeDialog = false
                            showImageExtractorDialog = true
                        }
                    )
                    
                    // Preview section for current selection
                    val currentPreset = try {
                        ThemePreset.valueOf(state.themePreset)
                    } catch (e: Exception) {
                        ThemePreset.DYNAMIC
                    }
                    
                    ThemePreviewSection(
                        preset = currentPreset
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
    
    // Image color extractor dialog
    if (showImageExtractorDialog) {
        ImageColorExtractorDialog(
            onDismiss = { showImageExtractorDialog = false },
            onColorsExtracted = { colors ->
                viewModel.setCustomThemeColors(colors.toJson())
                viewModel.setThemePreset(ThemePreset.CUSTOM.name)
                showImageExtractorDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialSymbol(
                symbol = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MaterialSymbol(
            symbol = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MaterialSymbol(
            symbol = MaterialSymbols.ARROW_FORWARD,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MaterialSymbol(
            symbol = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("system" to "Auto", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
            FilterChip(
                selected = currentTheme == value,
                onClick = { onThemeChange(value) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UnitsSelector(
    currentUnits: String,
    onUnitsChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("kg" to "Kilograms", "lb" to "Pounds").forEach { (value, label) ->
            FilterChip(
                selected = currentUnits == value,
                onClick = { onUnitsChange(value) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RestTimerSelector(
    currentTime: Int,
    onTimeChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(30, 60, 90, 120, 180).forEach { seconds ->
            FilterChip(
                selected = currentTime == seconds,
                onClick = { onTimeChange(seconds) },
                label = { Text("${seconds}s") }
            )
        }
    }
}

@Composable
private fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }
    var showPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("OpenAI API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Your API key is stored securely and only used for AI coaching features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            MaterialSymbol(
                                symbol = if (showPassword) MaterialSymbols.CLOSE else MaterialSymbols.ADD,
                                contentDescription = if (showPassword) "Hide" else "Show"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                
                Text(
                    text = "Get your API key from OpenAI Platform → API Keys",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(apiKey) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NotificationScheduleDialog(
    currentTime: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedTime by remember { mutableStateOf(currentTime) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Schedule") },
        text = {
            Column {
                Text(
                    text = "Choose when to receive workout reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                listOf("08:00", "12:00", "18:00", "20:00").forEach { time ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTime = time }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTime == time,
                            onClick = { selectedTime = time }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(time)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedTime) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Athletic AI") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Athletic AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Your intelligent fitness companion powered by AI. Track workouts, monitor progress, and achieve your fitness goals with personalized coaching.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Built with Jetpack Compose & Material3",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
