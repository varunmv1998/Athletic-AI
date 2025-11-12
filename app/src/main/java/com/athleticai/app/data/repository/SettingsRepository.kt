package com.athleticai.app.data.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.athleticai.app.data.api.models.SyncStatus

data class SettingsState(
    val theme: String = "system",
    val themePreset: String = "DYNAMIC",
    val customThemeColors: String? = null,
    val units: String = "kg",
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val defaultRestTime: Int = 90,
    val aiCoachingEnabled: Boolean = true,
    val workoutReminders: Boolean = true,
    val notificationTime: String = "18:00",
    val testModeEnabled: Boolean = false,
    val exerciseDbSyncStatus: SyncStatus = SyncStatus()
)

class SettingsRepository(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "athletic_ai_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _settingsState = MutableStateFlow(loadCurrentSettings())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private fun loadCurrentSettings(): SettingsState {
        Log.d("SettingsRepository", "=== LOAD CURRENT SETTINGS ===")
        val apiKey = prefs.getString(KEY_API, "") ?: ""
        val hasApiKey = apiKey.isNotBlank()
        Log.d("SettingsRepository", "API key loaded: ${if (apiKey.isBlank()) "EMPTY" else "EXISTS (length: ${apiKey.length})"}")
        Log.d("SettingsRepository", "hasApiKey calculated as: $hasApiKey")
        
        // Load Exercise Database sync status
        val exerciseDbSyncStatus = SyncStatus(
            isInProgress = false,
            lastSyncTime = prefs.getLong(KEY_EXERCISE_DB_SYNC_TIME, 0L).takeIf { it > 0 },
            exerciseCount = prefs.getInt(KEY_EXERCISE_DB_COUNT, 0),
            error = null,
            progress = 0f
        )
        
        val settings = SettingsState(
            theme = prefs.getString(KEY_THEME, "system") ?: "system",
            themePreset = prefs.getString(KEY_THEME_PRESET, "DYNAMIC") ?: "DYNAMIC",
            customThemeColors = prefs.getString(KEY_CUSTOM_THEME_COLORS, null),
            units = prefs.getString(KEY_UNITS, "kg") ?: "kg",
            apiKey = apiKey,
            hasApiKey = hasApiKey,
            defaultRestTime = prefs.getInt(KEY_REST_TIME, 90),
            aiCoachingEnabled = prefs.getBoolean(KEY_AI_COACHING, true),
            workoutReminders = prefs.getBoolean(KEY_WORKOUT_REMINDERS, true),
            notificationTime = prefs.getString(KEY_NOTIFICATION_TIME, "18:00") ?: "18:00",
            testModeEnabled = prefs.getBoolean(KEY_TEST_MODE, false),
            exerciseDbSyncStatus = exerciseDbSyncStatus
        )
        Log.d("SettingsRepository", "Settings state created with hasApiKey: ${settings.hasApiKey}")
        return settings
    }

    private fun updateState() {
        _settingsState.value = loadCurrentSettings()
    }

    fun getApiKey(): String? {
        Log.d("SettingsRepository", "=== GET API KEY ===")
        val apiKey = prefs.getString(KEY_API, null)
        Log.d("SettingsRepository", "API key status: ${if (apiKey.isNullOrBlank()) "NULL/EMPTY" else "EXISTS (length: ${apiKey.length})"}")
        Log.d("SettingsRepository", "API key value: ${if (apiKey.isNullOrBlank()) "null" else "sk-${apiKey.substring(3, 8)}...${apiKey.takeLast(4)}"}")
        return apiKey
    }
    
    fun setApiKey(key: String?) {
        Log.d("SettingsRepository", "=== SET API KEY ===")
        Log.d("SettingsRepository", "New API key status: ${if (key.isNullOrBlank()) "NULL/EMPTY" else "EXISTS (length: ${key.length})"}")
        prefs.edit().putString(KEY_API, key).apply()
        updateState()
        Log.d("SettingsRepository", "API key saved and state updated")
    }

    fun getUnits(): String = prefs.getString(KEY_UNITS, "kg") ?: "kg"
    fun setUnits(units: String) {
        prefs.edit().putString(KEY_UNITS, units).apply()
        updateState()
    }

    fun getTheme(): String = prefs.getString(KEY_THEME, "system") ?: "system"
    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        updateState()
    }

    fun getDefaultRestTime(): Int = prefs.getInt(KEY_REST_TIME, 90)
    fun setDefaultRestTime(time: Int) {
        prefs.edit().putInt(KEY_REST_TIME, time).apply()
        updateState()
    }

    fun getAiCoachingEnabled(): Boolean = prefs.getBoolean(KEY_AI_COACHING, true)
    fun setAiCoachingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_COACHING, enabled).apply()
        updateState()
    }

    fun getWorkoutReminders(): Boolean = prefs.getBoolean(KEY_WORKOUT_REMINDERS, true)
    fun setWorkoutReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WORKOUT_REMINDERS, enabled).apply()
        updateState()
    }

    fun getNotificationTime(): String = prefs.getString(KEY_NOTIFICATION_TIME, "18:00") ?: "18:00"
    fun setNotificationTime(time: String) {
        prefs.edit().putString(KEY_NOTIFICATION_TIME, time).apply()
        updateState()
    }
    
    fun getTestModeEnabled(): Boolean = prefs.getBoolean(KEY_TEST_MODE, false)
    fun setTestModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TEST_MODE, enabled).apply()
        updateState()
    }
    
    // Theme preset methods
    fun getThemePreset(): String = prefs.getString(KEY_THEME_PRESET, "DYNAMIC") ?: "DYNAMIC"
    fun setThemePreset(preset: String) {
        prefs.edit().putString(KEY_THEME_PRESET, preset).apply()
        updateState()
    }
    
    fun getCustomThemeColors(): String? = prefs.getString(KEY_CUSTOM_THEME_COLORS, null)
    fun setCustomThemeColors(colors: String?) {
        prefs.edit().putString(KEY_CUSTOM_THEME_COLORS, colors).apply()
        updateState()
    }
    
    // Methods to update Exercise Database sync status
    fun updateExerciseDbSyncStatus(count: Int, lastSyncTime: Long) {
        prefs.edit()
            .putInt(KEY_EXERCISE_DB_COUNT, count)
            .putLong(KEY_EXERCISE_DB_SYNC_TIME, lastSyncTime)
            .apply()
        updateState()
    }
    
    fun clearExerciseDbSyncStatus() {
        prefs.edit()
            .remove(KEY_EXERCISE_DB_COUNT)
            .remove(KEY_EXERCISE_DB_SYNC_TIME)
            .apply()
        updateState()
    }

    companion object {
        private const val KEY_API = "api_key"
        private const val KEY_UNITS = "units"
        private const val KEY_THEME = "theme"
        private const val KEY_THEME_PRESET = "theme_preset"
        private const val KEY_CUSTOM_THEME_COLORS = "custom_theme_colors"
        private const val KEY_REST_TIME = "rest_time"
        private const val KEY_AI_COACHING = "ai_coaching"
        private const val KEY_WORKOUT_REMINDERS = "workout_reminders"
        private const val KEY_NOTIFICATION_TIME = "notification_time"
        private const val KEY_TEST_MODE = "test_mode"
        private const val KEY_EXERCISE_DB_COUNT = "exercise_db_count"
        private const val KEY_EXERCISE_DB_SYNC_TIME = "exercise_db_sync_time"
    }
}

