package com.athleticai.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.QuoteLoader
import com.athleticai.app.data.models.MotivationalQuote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isRefreshing: Boolean = false,
    val currentQuote: MotivationalQuote? = null,
    val error: String? = null,
    val lastRefreshTime: Long = System.currentTimeMillis()
)

class HomeViewModel(
    private val quoteLoader: QuoteLoader,
    private val progressViewModel: ProgressViewModel
) : ViewModel() {
    
    private val TAG = "HomeViewModel"
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "HomeViewModel initialized")
        loadInitialQuote()
    }
    
    /**
     * Load initial quote when the screen first loads with context awareness
     */
    private fun loadInitialQuote() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading context-aware initial quote...")
                
                // Get user workout context data
                val progressState = progressViewModel.state.value
                val workoutStreak = progressState.currentStreak
                val lastWorkoutDays = progressState.daysSinceLastWorkout
                val hasRecentPR = progressState.recentPRs.isNotEmpty()
                val totalWorkouts = progressState.totalWorkouts
                
                val quote = quoteLoader.getContextAwareQuote(
                    workoutStreak = workoutStreak,
                    lastWorkoutDays = lastWorkoutDays,
                    hasRecentPR = hasRecentPR,
                    totalWorkouts = totalWorkouts
                )
                
                _uiState.value = _uiState.value.copy(
                    currentQuote = quote,
                    error = null
                )
                Log.d(TAG, "Context-aware quote loaded: ${quote.text}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial quote", e)
                // Fallback to simple time-based quote
                try {
                    val fallbackQuote = quoteLoader.getQuoteForCurrentTime()
                    _uiState.value = _uiState.value.copy(
                        currentQuote = fallbackQuote,
                        error = null
                    )
                } catch (fallbackError: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load motivational quote"
                    )
                }
            }
        }
    }
    
    /**
     * Refresh all home screen data including quotes
     */
    fun refreshData() {
        if (_uiState.value.isRefreshing) {
            Log.d(TAG, "Refresh already in progress, ignoring")
            return
        }
        
        Log.d(TAG, "Starting home screen refresh...")
        _uiState.value = _uiState.value.copy(
            isRefreshing = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                // Clear quote cache to force fresh load
                quoteLoader.clearCache()
                
                // Load new context-aware quote
                Log.d(TAG, "Loading new context-aware quote...")
                val progressState = progressViewModel.state.value
                val newQuote = quoteLoader.getContextAwareQuote(
                    workoutStreak = progressState.currentStreak,
                    lastWorkoutDays = progressState.daysSinceLastWorkout,
                    hasRecentPR = progressState.recentPRs.isNotEmpty(),
                    totalWorkouts = progressState.totalWorkouts
                )
                
                // Refresh progress data  
                Log.d(TAG, "Refreshing progress data...")
                progressViewModel.refreshProgressData()
                
                // Update UI state
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    currentQuote = newQuote,
                    error = null,
                    lastRefreshTime = System.currentTimeMillis()
                )
                
                Log.d(TAG, "Home screen refresh completed successfully")
                Log.d(TAG, "New quote: ${newQuote.text}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during refresh", e)
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Failed to refresh data"
                )
            }
        }
    }
    
    /**
     * Manually load a new context-aware quote without full refresh
     */
    fun refreshQuote() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing context-aware quote...")
                val progressState = progressViewModel.state.value
                val newQuote = quoteLoader.getContextAwareQuote(
                    workoutStreak = progressState.currentStreak,
                    lastWorkoutDays = progressState.daysSinceLastWorkout,
                    hasRecentPR = progressState.recentPRs.isNotEmpty(),
                    totalWorkouts = progressState.totalWorkouts
                )
                _uiState.value = _uiState.value.copy(
                    currentQuote = newQuote,
                    error = null
                )
                Log.d(TAG, "Context-aware quote refreshed: ${newQuote.text}")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing quote", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load new quote"
                )
            }
        }
    }
    
    /**
     * Get a pre-workout motivational quote
     */
    fun getPreWorkoutQuote() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading pre-workout quote...")
                val quote = quoteLoader.getPreWorkoutQuote()
                _uiState.value = _uiState.value.copy(
                    currentQuote = quote,
                    error = null
                )
                Log.d(TAG, "Pre-workout quote loaded: ${quote.text}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading pre-workout quote", e)
            }
        }
    }
    
    /**
     * Get a post-workout motivational quote
     */
    fun getPostWorkoutQuote() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading post-workout quote...")
                val quote = quoteLoader.getPostWorkoutQuote()
                _uiState.value = _uiState.value.copy(
                    currentQuote = quote,
                    error = null
                )
                Log.d(TAG, "Post-workout quote loaded: ${quote.text}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading post-workout quote", e)
            }
        }
    }
    
    /**
     * Clear any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get current quote text for display
     */
    fun getCurrentQuoteText(): String {
        return _uiState.value.currentQuote?.text 
            ?: "The iron never lies. You are what you lift."
    }
}