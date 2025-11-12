package com.athleticai.app.data.repository

import com.athleticai.app.data.AchievementService
import com.athleticai.app.data.database.entities.AchievementEntity
import com.athleticai.app.data.database.entities.MonthlyStatsEntity
import com.athleticai.app.data.models.AchievementCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class AchievementRepository(
    private val achievementService: AchievementService
) {
    
    suspend fun initializeAchievements() {
        achievementService.initializeAchievements()
    }
    
    suspend fun checkWorkoutCompletionAchievements(workoutDate: LocalDate) {
        achievementService.checkWorkoutCompletionAchievements(workoutDate)
    }
    
    suspend fun checkProgramCompletionAchievements() {
        achievementService.checkProgramCompletionAchievements()
    }
    
    suspend fun checkPersonalRecordAchievements() {
        achievementService.checkPersonalRecordAchievements()
    }
    
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>> {
        return achievementService.getUnlockedAchievements()
    }
    
    fun getAllAchievements(): Flow<List<AchievementEntity>> {
        return achievementService.getAllAchievements()
    }
    
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<AchievementEntity>> {
        return achievementService.getAchievementsByCategory(category.name)
    }
    
    fun getMonthlyStats(): Flow<List<MonthlyStatsEntity>> {
        return achievementService.getMonthlyStats()
    }
    
    fun getAchievementProgress(achievement: AchievementEntity): Int {
        return achievement.currentProgress
    }
    
    fun getTotalPoints(): Flow<Int> {
        return achievementService.getUnlockedAchievements().map { achievements ->
            achievements.sumOf { achievement -> achievement.points }
        }
    }
    
    fun getAchievementCount(): Flow<Int> {
        return achievementService.getUnlockedAchievements().map { achievements -> achievements.size }
    }
    
    fun getTotalAchievementCount(): Flow<Int> {
        return achievementService.getAllAchievements().map { achievements -> achievements.size }
    }
    
    // Test data methods - TODO: Implement when achievement system supports it
    suspend fun insertAchievement(achievement: AchievementEntity) {
        // TODO: Implement when AchievementService has insertTestAchievement method
    }
    
    suspend fun clearAllAchievements() {
        // TODO: Implement when AchievementService has clearAllAchievements method
    }
}
