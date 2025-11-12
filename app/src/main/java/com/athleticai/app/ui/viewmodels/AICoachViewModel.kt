package com.athleticai.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.api.AIService
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal
import com.athleticai.app.data.database.entities.WorkoutSession
import com.athleticai.app.data.database.entities.WorkoutSet
import com.athleticai.app.data.repository.MeasurementsRepository
import com.athleticai.app.data.repository.WorkoutRepository
import com.athleticai.app.data.repository.AnalyticsRepository
import com.athleticai.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = false
)

data class AICoachUiState(
    val chatMessages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val isLoading: Boolean = false,
    val hasApiKey: Boolean = false,
    val lastAnalysis: String? = null,
    val weeklyReview: String? = null,
    val isOnline: Boolean = true,
    val errorMessage: String? = null,
    val quickActions: List<String> = listOf(
        "How's my progress this week?",
        "What should I focus on next workout?",
        "Am I recovering well?",
        "How close am I to my goals?",
        "Any concerns with my training?"
    )
)

class AICoachViewModel(
    private val aiService: AIService,
    private val workoutRepository: WorkoutRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AICoachUiState())
    val uiState: StateFlow<AICoachUiState> = _uiState.asStateFlow()

    init {
        checkApiKeyAvailability()
        loadLastAnalysis()
        
        // Observe settings changes to update API key status
        viewModelScope.launch {
            settingsRepository.settingsState.collect { settings ->
                Log.d("AICoachViewModel", "Settings changed - hasApiKey: ${settings.hasApiKey}")
                val currentState = _uiState.value
                if (currentState.hasApiKey != settings.hasApiKey) {
                    Log.d("AICoachViewModel", "API key status changed, refreshing...")
                    checkApiKeyAvailability()
                }
            }
        }
        
        // Add initial offline status message only if no API key
        val hasApiKey = !settingsRepository.getApiKey().isNullOrBlank()
        if (!hasApiKey) {
            addInitialOfflineMessage()
        }
    }
    
    private fun addInitialOfflineMessage() {
        val offlineMessage = ChatMessage(
            content = "I'm currently offline. For general training advice, focus on consistency, progressive overload, and adequate recovery between sessions.",
            isFromUser = false,
            timestamp = LocalDateTime.now()
        )
        
        _uiState.value = _uiState.value.copy(
            chatMessages = listOf(offlineMessage),
            isOnline = false
        )
    }

    private fun checkApiKeyAvailability() {
        Log.d("AICoachViewModel", "=== CHECK API KEY AVAILABILITY ===")
        
        // Simple check - does an API key exist in settings?
        val apiKey = settingsRepository.getApiKey()
        val hasApiKey = !apiKey.isNullOrBlank()
        
        Log.d("AICoachViewModel", "API key check - hasApiKey: $hasApiKey")
        
        _uiState.value = _uiState.value.copy(
            hasApiKey = hasApiKey,
            isOnline = hasApiKey // If we have a key, assume online mode
        )
        
        // If offline message was shown, remove it when API key is detected
        if (hasApiKey && _uiState.value.chatMessages.isNotEmpty()) {
            val messages = _uiState.value.chatMessages
            if (messages.size == 1 && messages.first().content.contains("I'm currently offline")) {
                Log.d("AICoachViewModel", "Removing offline message - API key now available")
                _uiState.value = _uiState.value.copy(chatMessages = emptyList())
            }
        }
    }

    private fun loadLastAnalysis() {
        viewModelScope.launch {
            // Load any cached analysis or previous insights
            // This could be stored in SharedPreferences or database
        }
    }

    fun updateInput(input: String) {
        _uiState.value = _uiState.value.copy(currentInput = input)
    }

    fun sendMessage(message: String = _uiState.value.currentInput) {
        Log.d("AICoachViewModel", "=== SEND MESSAGE START ===")
        Log.d("AICoachViewModel", "Input message: '$message'")
        Log.d("AICoachViewModel", "Current UI state - hasApiKey: ${_uiState.value.hasApiKey}, isOnline: ${_uiState.value.isOnline}")
        
        if (message.isBlank()) {
            Log.d("AICoachViewModel", "Message is blank, returning")
            return
        }

        val userMessage = ChatMessage(
            content = message,
            isFromUser = true
        )

        val loadingMessage = ChatMessage(
            content = "Analyzing your training data...",
            isFromUser = false,
            isLoading = true
        )

        _uiState.value = _uiState.value.copy(
            chatMessages = _uiState.value.chatMessages + userMessage + loadingMessage,
            currentInput = "",
            isLoading = true,
            errorMessage = null
        )
        
        Log.d("AICoachViewModel", "Added user message and loading message to chat")

        viewModelScope.launch {
            try {
                Log.d("AICoachViewModel", "Starting coroutine to process message")
                
                // Get recent workout data for context
                Log.d("AICoachViewModel", "Fetching workout data for context...")
                val measurements = measurementsRepository.getAllMeasurements().first()
                val goals = measurementsRepository.getActiveGoals().first()
                val recentSessions = emptyList<WorkoutSession>() // Simplified for now
                
                Log.d("AICoachViewModel", "Context data - measurements: ${measurements.size}, goals: ${goals.size}")
                
                val result = if (_uiState.value.hasApiKey) {
                    Log.d("AICoachViewModel", "Has API key - calling aiService.askQuestion()")
                    val apiResult = aiService.askQuestion(
                        question = message,
                        workoutHistory = recentSessions,
                        measurements = measurements,
                        goals = goals
                    )
                    Log.d("AICoachViewModel", "API result - success: ${apiResult.isSuccess}")
                    if (apiResult.isFailure) {
                        Log.e("AICoachViewModel", "API call failed: ${apiResult.exceptionOrNull()?.message}")
                    }
                    apiResult
                } else {
                    Log.d("AICoachViewModel", "No API key - using fallback response")
                    val fallback = aiService.getFallbackResponse(message)
                    Log.d("AICoachViewModel", "Fallback response: '$fallback'")
                    Result.success(fallback)
                }

                result.fold(
                    onSuccess = { aiResponse ->
                        Log.d("AICoachViewModel", "SUCCESS - AI Response: '$aiResponse'")
                        val responseMessage = ChatMessage(
                            content = aiResponse,
                            isFromUser = false
                        )

                        _uiState.value = _uiState.value.copy(
                            chatMessages = _uiState.value.chatMessages.dropLast(1) + responseMessage,
                            isLoading = false
                        )
                        Log.d("AICoachViewModel", "Updated UI with successful response")
                    },
                    onFailure = { error ->
                        Log.e("AICoachViewModel", "FAILURE - Error: ${error.message}")
                        Log.e("AICoachViewModel", "Error type: ${error.javaClass.simpleName}")
                        Log.e("AICoachViewModel", "Stack trace:", error)
                        
                        val fallbackResponse = aiService.getFallbackResponse(message)
                        Log.d("AICoachViewModel", "Using fallback response: '$fallbackResponse'")
                        
                        val responseMessage = ChatMessage(
                            content = fallbackResponse + "\n\n(Note: AI coach is currently offline)",
                            isFromUser = false
                        )

                        _uiState.value = _uiState.value.copy(
                            chatMessages = _uiState.value.chatMessages.dropLast(1) + responseMessage,
                            isLoading = false,
                            errorMessage = null, // Don't show technical errors
                            isOnline = false
                        )
                        Log.d("AICoachViewModel", "Updated UI with fallback response")
                    }
                )
            } catch (e: Exception) {
                Log.e("AICoachViewModel", "EXCEPTION in sendMessage: ${e.message}")
                Log.e("AICoachViewModel", "Exception type: ${e.javaClass.simpleName}")
                Log.e("AICoachViewModel", "Exception stack trace:", e)
                
                val fallbackResponse = aiService.getFallbackResponse(message)
                Log.d("AICoachViewModel", "Exception fallback response: '$fallbackResponse'")
                
                val responseMessage = ChatMessage(
                    content = fallbackResponse + "\n\n(Note: AI coach is currently offline)",
                    isFromUser = false
                )

                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages.dropLast(1) + responseMessage,
                    isLoading = false,
                    errorMessage = null, // Don't show technical errors
                    isOnline = false
                )
                Log.d("AICoachViewModel", "Updated UI after exception")
            }
        }
    }

    fun processWorkoutCompletion(session: WorkoutSession, sets: List<WorkoutSet>) {
        viewModelScope.launch {
            // Add event to AI service queue
            aiService.queueEvent(AIService.WorkoutEvent.SessionCompleted(session, sets))
            
            // Skip auto-analysis in offline mode to prevent errors
            // generatePostWorkoutAnalysis()
        }
    }

    fun generatePostWorkoutAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = aiService.processQueuedEvents()
                result.fold(
                    onSuccess = { analysis ->
                        _uiState.value = _uiState.value.copy(
                            lastAnalysis = analysis,
                            isLoading = false
                        )
                        
                        // Add analysis as a system message
                        val analysisMessage = ChatMessage(
                            content = "📊 **Post-Workout Analysis**\n\n$analysis",
                            isFromUser = false
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            chatMessages = _uiState.value.chatMessages + analysisMessage
                        )
                    },
                    onFailure = { error ->
                        // Don't show technical errors for offline mode
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                )
            } catch (e: Exception) {
                // Don't show technical errors for offline mode
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun generateWeeklyReview() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val now = LocalDateTime.now()
                val weekStart = now.minusDays(7)
                
                // Get week's data - using simplified approach for now
                val weekSessions = emptyList<WorkoutSession>() // Simplified
                val weekSets = emptyList<WorkoutSet>() // Simplified
                val measurements = measurementsRepository.getAllMeasurements().first()
                val goals = measurementsRepository.getActiveGoals().first()
                
                val result = aiService.generateWeeklyReview(
                    weekSessions = weekSessions,
                    weekSets = weekSets,
                    measurements = measurements,
                    goals = goals
                )
                
                result.fold(
                    onSuccess = { review ->
                        _uiState.value = _uiState.value.copy(
                            weeklyReview = review,
                            isLoading = false
                        )
                        
                        // Add review as a system message
                        val reviewMessage = ChatMessage(
                            content = "📈 **Weekly Review**\n\n$review",
                            isFromUser = false
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            chatMessages = _uiState.value.chatMessages + reviewMessage
                        )
                    },
                    onFailure = { error ->
                        // Don't show technical errors for offline mode
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                )
            } catch (e: Exception) {
                // Don't show technical errors for offline mode
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            chatMessages = emptyList(),
            lastAnalysis = null,
            weeklyReview = null,
            errorMessage = null
        )
    }
    
    fun refreshApiKeyStatus() {
        Log.d("AICoachViewModel", "=== REFRESH API KEY STATUS ===")
        checkApiKeyAvailability()
    }
    
    fun addOfflineFeatureMessage(message: String) {
        val offlineMessage = ChatMessage(
            content = "ℹ️ $message",
            isFromUser = false,
            timestamp = LocalDateTime.now()
        )
        
        _uiState.value = _uiState.value.copy(
            chatMessages = _uiState.value.chatMessages + offlineMessage
        )
    }
}