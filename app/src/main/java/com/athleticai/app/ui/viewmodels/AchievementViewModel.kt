package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.repository.AchievementRepository
import com.athleticai.app.data.database.entities.AchievementEntity
import com.athleticai.app.data.database.entities.MonthlyStatsEntity
import com.athleticai.app.data.models.AchievementCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AchievementUiState(
    val unlockedAchievements: List<AchievementEntity> = emptyList(),
    val allAchievements: List<AchievementEntity> = emptyList(),
    val monthlyStats: List<MonthlyStatsEntity> = emptyList(),
    val totalPoints: Int = 0,
    val achievementCount: Int = 0,
    val totalAchievementCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AchievementViewModel(
    private val achievementRepository: AchievementRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()
    
    init {
        loadAchievements()
        loadMonthlyStats()
    }
    
    private fun loadAchievements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Initialize achievements if needed
                achievementRepository.initializeAchievements()
                
                // Load all achievements data
                val unlocked = achievementRepository.getUnlockedAchievements().first()
                val all = achievementRepository.getAllAchievements().first()
                val points = achievementRepository.getTotalPoints().first()
                val count = achievementRepository.getAchievementCount().first()
                val total = achievementRepository.getTotalAchievementCount().first()
                
                _uiState.value = _uiState.value.copy(
                    unlockedAchievements = unlocked,
                    allAchievements = all,
                    totalPoints = points,
                    achievementCount = count,
                    totalAchievementCount = total,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load achievements",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadMonthlyStats() {
        viewModelScope.launch {
            try {
                val stats = achievementRepository.getMonthlyStats().first()
                _uiState.value = _uiState.value.copy(monthlyStats = stats)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load monthly stats"
                )
            }
        }
    }
    
    fun getAchievementsByCategory(category: AchievementCategory): List<AchievementEntity> {
        return _uiState.value.allAchievements.filter { it.category == category.name }
    }
    
    fun getUnlockedAchievementsByCategory(category: AchievementCategory): List<AchievementEntity> {
        return _uiState.value.unlockedAchievements.filter { it.category == category.name }
    }
    
    fun getAchievementProgress(achievementId: String): Int {
        val achievement = _uiState.value.allAchievements.find { it.id == achievementId }
        return achievement?.currentProgress ?: 0
    }
    
    fun getCategoryProgress(category: AchievementCategory): Pair<Int, Int> {
        val categoryAchievements = getAchievementsByCategory(category)
        val unlockedCount = getUnlockedAchievementsByCategory(category).size
        return Pair(unlockedCount, categoryAchievements.size)
    }
    
    fun getTotalProgress(): Pair<Int, Int> {
        return Pair(_uiState.value.achievementCount, _uiState.value.totalAchievementCount)
    }
    
    fun refreshAchievements() {
        loadAchievements()
        loadMonthlyStats()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
