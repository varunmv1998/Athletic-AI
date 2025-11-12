package com.athleticai.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.api.AIService
import com.athleticai.app.data.repository.SettingsRepository
import com.athleticai.app.data.repository.SettingsState
import com.athleticai.app.data.repository.ExerciseRepository
import com.athleticai.app.data.api.models.SyncStatus
import com.athleticai.app.data.TestDataGenerator
import com.athleticai.app.data.TestDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApiTestResult(
    val isSuccess: Boolean = false,
    val message: String = "",
    val isLoading: Boolean = false
)

class SettingsViewModel(
    private val repo: SettingsRepository,
    private val aiService: AIService,
    private val testDataGenerator: TestDataGenerator? = null,
    private val exerciseRepository: ExerciseRepository,
    private val testDataManager: TestDataManager? = null
) : ViewModel() {
    val state: StateFlow<SettingsState> = repo.settingsState
    
    private val _apiTestResult = MutableStateFlow(ApiTestResult())
    val apiTestResult: StateFlow<ApiTestResult> = _apiTestResult.asStateFlow()
    
    private val _exerciseDbSyncStatus = MutableStateFlow(SyncStatus())
    val exerciseDbSyncStatus: StateFlow<SyncStatus> = _exerciseDbSyncStatus.asStateFlow()

    // Legacy methods for backward compatibility
    fun getApiKey(): String = repo.getApiKey() ?: ""
    fun setApiKey(key: String) = repo.setApiKey(key.ifBlank { null })

    fun getUnits(): String = repo.getUnits()
    fun setUnits(units: String) = repo.setUnits(units)

    fun getTheme(): String = repo.getTheme()
    fun setTheme(theme: String) = repo.setTheme(theme)
    
    fun getThemePreset(): String = repo.getThemePreset()
    fun setThemePreset(preset: String) = repo.setThemePreset(preset)
    
    fun getCustomThemeColors(): String? = repo.getCustomThemeColors()
    fun setCustomThemeColors(colors: String?) = repo.setCustomThemeColors(colors)

    // New reactive methods
    fun setDefaultRestTime(time: Int) = repo.setDefaultRestTime(time)
    fun setAiCoachingEnabled(enabled: Boolean) = repo.setAiCoachingEnabled(enabled)
    fun setWorkoutReminders(enabled: Boolean) = repo.setWorkoutReminders(enabled)
    fun setNotificationTime(time: String) = repo.setNotificationTime(time)
    
    // Test mode methods with improved data management
    fun setTestModeEnabled(enabled: Boolean) {
        val wasEnabled = repo.getTestModeEnabled()
        
        if (enabled == wasEnabled) {
            Log.d("SettingsViewModel", "Test mode already ${if (enabled) "enabled" else "disabled"}")
            return
        }
        
        viewModelScope.launch {
            try {
                if (testDataManager != null) {
                    // Use TestDataManager for seamless switching
                    val result = if (enabled) {
                        Log.d("SettingsViewModel", "Switching to test mode with data backup...")
                        testDataManager.switchToTestMode()
                    } else {
                        Log.d("SettingsViewModel", "Switching to production mode with data restore...")
                        testDataManager.switchToProductionMode()
                    }
                    
                    if (result.isSuccess) {
                        Log.d("SettingsViewModel", "Successfully switched to ${if (enabled) "test" else "production"} mode")
                    } else {
                        Log.e("SettingsViewModel", "Failed to switch mode", result.exceptionOrNull())
                    }
                } else {
                    // Fallback to old method if TestDataManager is not available
                    repo.setTestModeEnabled(enabled)
                    
                    if (enabled && !wasEnabled) {
                        Log.d("SettingsViewModel", "Enabling test mode - generating test data")
                        generateTestData()
                    } else if (!enabled && wasEnabled) {
                        Log.d("SettingsViewModel", "Disabling test mode - clearing test data")
                        clearTestData()
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error switching test mode", e)
            }
        }
    }
    
    // Debug method for manual test data generation
    fun generateTestDataManually() {
        Log.d("SettingsViewModel", "Manual test data generation requested")
        viewModelScope.launch {
            if (testDataManager != null && repo.getTestModeEnabled()) {
                // Use TestDataManager to regenerate test data
                val result = testDataManager.regenerateTestData()
                if (result.isSuccess) {
                    Log.d("SettingsViewModel", "Test data regenerated successfully")
                } else {
                    Log.e("SettingsViewModel", "Failed to regenerate test data", result.exceptionOrNull())
                }
            } else {
                // Fallback to direct generation
                generateTestData()
            }
        }
    }
    
    // Get backup information for UI display
    fun getBackupInfo() = testDataManager?.getBackupInfo()
    
    private fun generateTestData() {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Generating test data...")
                testDataGenerator?.generateTestData()
                Log.d("SettingsViewModel", "Test data generated successfully")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to generate test data", e)
            }
        }
    }
    
    private fun clearTestData() {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Clearing test data...")
                testDataGenerator?.clearTestData()
                Log.d("SettingsViewModel", "Test data cleared successfully")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear test data", e)
            }
        }
    }

    // API Key methods
    fun testApiKey() {
        Log.d("SettingsViewModel", "=== TEST API KEY START ===")
        viewModelScope.launch {
            _apiTestResult.value = ApiTestResult(isLoading = true, message = "Testing network connectivity...")
            
            try {
                // First test basic network connectivity
                Log.d("SettingsViewModel", "Testing basic network connectivity...")
                
                Log.d("SettingsViewModel", "Making test call to AI service...")
                val result = aiService.askQuestion(
                    question = "Hello, this is a test. Please respond with 'API key working correctly.'",
                    workoutHistory = emptyList(),
                    measurements = emptyList(),
                    goals = emptyList()
                )
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("SettingsViewModel", "API test SUCCESS: $response")
                        _apiTestResult.value = ApiTestResult(
                            isSuccess = true,
                            message = "✅ API key is working! Response: ${response.take(50)}${if (response.length > 50) "..." else ""}"
                        )
                    },
                    onFailure = { error ->
                        Log.e("SettingsViewModel", "API test FAILED: ${error.message}")
                        val errorMessage = when {
                            error.message?.contains("No OpenAI API key configured") == true -> 
                                "❌ No API key configured. Please add your OpenAI API key first."
                            error.message?.contains("Unable to resolve host") == true ||
                            error.message?.contains("No address associated with hostname") == true -> 
                                "❌ DNS resolution failed for api.openai.com\n\n• Check internet connection\n• Try mobile data instead of WiFi\n• Your network may block OpenAI\n• Try different DNS servers (8.8.8.8)"
                            error.message?.contains("api.openai.com") == true -> 
                                "❌ Network error: Unable to connect to OpenAI servers. Check internet connection."
                            error.message?.contains("401") == true || error.message?.contains("Unauthorized") == true -> 
                                "❌ Invalid API key. Please check your OpenAI API key."
                            error.message?.contains("quota") == true -> 
                                "❌ API quota exceeded. Check your OpenAI account billing."
                            else -> 
                                "❌ Test failed: ${error.message}"
                        }
                        
                        _apiTestResult.value = ApiTestResult(
                            isSuccess = false,
                            message = errorMessage
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Exception during API test", e)
                _apiTestResult.value = ApiTestResult(
                    isSuccess = false,
                    message = "❌ Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun clearApiKey() {
        Log.d("SettingsViewModel", "=== CLEAR API KEY ===")
        repo.setApiKey(null)
        _apiTestResult.value = ApiTestResult() // Reset test result
        Log.d("SettingsViewModel", "API key cleared")
    }
    
    fun clearApiTestResult() {
        _apiTestResult.value = ApiTestResult()
    }

    // Action methods for settings
    fun exportData() {
        viewModelScope.launch {
            // TODO: Implement data export functionality
            // This could export workout history, measurements, and settings to a file
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // TODO: Implement data clearing functionality
            // This should clear all user data with proper confirmation
        }
    }

    fun openPrivacyPolicy() {
        viewModelScope.launch {
            // TODO: Open privacy policy URL or show dialog
        }
    }

    fun openSupport() {
        viewModelScope.launch {
            // TODO: Open support/help system or external URL
        }
    }
    
    // ExerciseDB sync methods
    fun triggerExerciseDbSync() {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Starting ExerciseDB sync...")
                _exerciseDbSyncStatus.value = SyncStatus(isInProgress = true)
                
                val result = exerciseRepository.triggerExerciseDbSync()
                
                if (result.isSuccess) {
                    val exerciseCount = result.getOrNull() ?: 0
                    val syncTime = System.currentTimeMillis()
                    Log.d("SettingsViewModel", "ExerciseDB sync completed: $exerciseCount exercises")
                    
                    // Update persistent storage
                    repo.updateExerciseDbSyncStatus(exerciseCount, syncTime)
                    
                    _exerciseDbSyncStatus.value = SyncStatus(
                        isInProgress = false,
                        lastSyncTime = syncTime,
                        exerciseCount = exerciseCount,
                        progress = 1f
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("SettingsViewModel", "ExerciseDB sync failed: $error")
                    _exerciseDbSyncStatus.value = SyncStatus(
                        isInProgress = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "ExerciseDB sync error", e)
                _exerciseDbSyncStatus.value = SyncStatus(
                    isInProgress = false,
                    error = e.message ?: "Sync failed"
                )
            }
        }
    }
    
    fun clearExerciseDbData() {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Clearing ExerciseDB data...")
                exerciseRepository.clearExerciseDbData()
                _exerciseDbSyncStatus.value = SyncStatus()
                Log.d("SettingsViewModel", "ExerciseDB data cleared")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear ExerciseDB data", e)
                _exerciseDbSyncStatus.value = _exerciseDbSyncStatus.value.copy(
                    error = "Failed to clear data: ${e.message}"
                )
            }
        }
    }
    
    init {
        // Check ExerciseDB sync status on initialization
        viewModelScope.launch {
            try {
                val isNeeded = exerciseRepository.isExerciseDbSyncNeeded()
                if (!isNeeded) {
                    // Sync already completed, get current status
                    val exerciseCount = exerciseRepository.getExerciseDbCount()
                    _exerciseDbSyncStatus.value = SyncStatus(
                        isInProgress = false,
                        lastSyncTime = System.currentTimeMillis(), // TODO: Get actual last sync time
                        exerciseCount = exerciseCount
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to check ExerciseDB sync status", e)
            }
        }
    }
}

