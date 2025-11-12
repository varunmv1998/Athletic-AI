package com.athleticai.app.data

import com.athleticai.app.data.database.dao.AchievementDao
import com.athleticai.app.data.database.entities.*
import com.athleticai.app.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AchievementService(
    private val achievementDao: AchievementDao
) {
    
    suspend fun initializeAchievements() {
        val existingAchievements = achievementDao.getAllAchievements().first()
        if (existingAchievements.isEmpty()) {
            val achievements = AchievementData.getAllAchievements()
            achievements.forEach { achievement ->
                val entity = convertToEntity(achievement)
                achievementDao.insertAchievement(entity)
            }
        }
    }
    
    suspend fun checkWorkoutCompletionAchievements(workoutDate: LocalDate) {
        // Update monthly stats
        updateMonthlyStats(workoutDate)
        
        // Update cumulative stats
        updateCumulativeStats(workoutDate)
        
        // Check for new achievements
        checkMonthlyAchievements(workoutDate)
        checkCumulativeAchievements(workoutDate)
        checkStreakAchievements(workoutDate)
    }
    
    suspend fun checkProgramCompletionAchievements() {
        val cumulativeStats = achievementDao.getCumulativeStats()
        cumulativeStats?.let { stats ->
            val totalPrograms = stats.totalProgramsCompleted + 1
            
            // Check program completion achievements
            when (totalPrograms) {
                1 -> unlockAchievement("program_first_complete", totalPrograms)
                2 -> unlockAchievement("program_second_complete", totalPrograms)
                3 -> unlockAchievement("program_third_complete", totalPrograms)
                5 -> unlockAchievement("program_veteran", totalPrograms)
                10 -> unlockAchievement("program_master", totalPrograms)
            }
            
            // Update cumulative stats
            achievementDao.updateTotalProgramsCompleted(
                totalPrograms,
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        }
    }
    
    suspend fun checkPersonalRecordAchievements() {
        // This would be called when a new PR is set
        // For now, we'll implement basic PR tracking
        unlockAchievement("performance_first_pr", 1)
    }
    
    private suspend fun updateMonthlyStats(workoutDate: LocalDate) {
        val month = workoutDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val year = workoutDate.year
        
        val existingStats = achievementDao.getMonthlyStats(month, year)
        val currentStats = existingStats ?: MonthlyStatsEntity(
            id = UUID.randomUUID().toString(),
            month = month,
            year = year,
            totalWorkouts = 0,
            totalWorkoutDays = 0,
            missedDays = 0,
            completionRate = 0f,
            lastUpdated = workoutDate
        )
        
        val updatedStats = currentStats.copy(
            totalWorkouts = currentStats.totalWorkouts + 1,
            totalWorkoutDays = currentStats.totalWorkoutDays + 1,
            lastUpdated = workoutDate
        )
        
        achievementDao.insertMonthlyStats(updatedStats)
    }
    
    private suspend fun updateCumulativeStats(workoutDate: LocalDate) {
        val existingStats = achievementDao.getCumulativeStats()
        val currentStats = existingStats ?: CumulativeStatsEntity(
            totalWorkoutDays = 0,
            totalProgramsCompleted = 0,
            currentStreak = 0,
            longestStreak = 0,
            lastUpdated = workoutDate
        )
        
        val updatedStats = currentStats.copy(
            totalWorkoutDays = currentStats.totalWorkoutDays + 1,
            lastUpdated = workoutDate
        )
        
        achievementDao.insertCumulativeStats(updatedStats)
    }
    
    private suspend fun checkMonthlyAchievements(workoutDate: LocalDate) {
        val month = workoutDate.monthValue
        val year = workoutDate.year
        val monthKey = workoutDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        
        val monthlyStats = achievementDao.getMonthlyStats(monthKey, year)
        monthlyStats?.let { stats ->
            // Check monthly workout count achievements
            when (month) {
                1 -> if (stats.totalWorkouts >= 12) unlockAchievement("monthly_january_warrior", stats.totalWorkouts)
                2 -> if (stats.totalWorkouts >= 20) unlockAchievement("monthly_february_fighter", stats.totalWorkouts)
                3 -> if (stats.totalWorkouts >= 25) unlockAchievement("monthly_march_madness", stats.totalWorkouts)
                4 -> if (stats.totalWorkouts >= 22) unlockAchievement("monthly_april_power", stats.totalWorkouts)
                5 -> if (stats.totalWorkouts >= 28) unlockAchievement("monthly_may_master", stats.totalWorkouts)
                6 -> if (stats.totalWorkouts >= 30) unlockAchievement("monthly_june_juggernaut", stats.totalWorkouts)
                7 -> if (stats.totalWorkouts >= 25) unlockAchievement("monthly_july_beast", stats.totalWorkouts)
                8 -> if (stats.totalWorkouts >= 27) unlockAchievement("monthly_august_assassin", stats.totalWorkouts)
                9 -> if (stats.totalWorkouts >= 24) unlockAchievement("monthly_september_savage", stats.totalWorkouts)
                10 -> if (stats.totalWorkouts >= 26) unlockAchievement("monthly_october_overlord", stats.totalWorkouts)
                11 -> if (stats.totalWorkouts >= 23) unlockAchievement("monthly_november_ninja", stats.totalWorkouts)
                12 -> if (stats.totalWorkouts >= 29) unlockAchievement("monthly_december_destroyer", stats.totalWorkouts)
            }
        }
    }
    
    private suspend fun checkCumulativeAchievements(workoutDate: LocalDate) {
        val cumulativeStats = achievementDao.getCumulativeStats()
        cumulativeStats?.let { stats ->
            val totalDays = stats.totalWorkoutDays
            
            // Check cumulative day achievements
            when {
                totalDays >= 1000 -> unlockAchievement("cumulative_1000_days", totalDays)
                totalDays >= 500 -> unlockAchievement("cumulative_500_days", totalDays)
                totalDays >= 300 -> unlockAchievement("cumulative_300_days", totalDays)
                totalDays >= 250 -> unlockAchievement("cumulative_250_days", totalDays)
                totalDays >= 200 -> unlockAchievement("cumulative_200_days", totalDays)
                totalDays >= 100 -> unlockAchievement("cumulative_100_days", totalDays)
                totalDays >= 50 -> unlockAchievement("cumulative_50_days", totalDays)
                totalDays >= 25 -> unlockAchievement("cumulative_25_days", totalDays)
                totalDays >= 10 -> unlockAchievement("cumulative_10_days", totalDays)
            }
        }
    }
    
    private suspend fun checkStreakAchievements(workoutDate: LocalDate) {
        val cumulativeStats = achievementDao.getCumulativeStats()
        cumulativeStats?.let { stats ->
            val currentStreak = stats.currentStreak
            
            // Check streak achievements
            when {
                currentStreak >= 90 -> unlockAchievement("consistency_90_day_streak", currentStreak)
                currentStreak >= 30 -> unlockAchievement("consistency_30_day_streak", currentStreak)
                currentStreak >= 7 -> unlockAchievement("consistency_7_day_streak", currentStreak)
            }
        }
    }
    
    private suspend fun unlockAchievement(achievementId: String, progress: Int) {
        val achievement = achievementDao.getAllAchievements().first().find { it.id == achievementId }
        achievement?.let { entity ->
            if (!entity.isUnlocked) {
                // Unlock the achievement
                achievementDao.unlockAchievement(
                    achievementId,
                    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    progress
                )
                
                // Record user achievement
                val userAchievement = UserAchievementEntity(
                    id = UUID.randomUUID().toString(),
                    achievementId = achievementId,
                    unlockedAt = LocalDate.now(),
                    progress = progress
                )
                achievementDao.insertUserAchievement(userAchievement)
            }
        }
    }
    
    private fun convertToEntity(achievement: Achievement): AchievementEntity {
        return AchievementEntity(
            id = achievement.id,
            name = achievement.name,
            description = achievement.description,
            category = achievement.category.name,
            icon = achievement.icon,
            requirementType = getRequirementType(achievement.requirement),
            requirementValue = getRequirementValue(achievement.requirement),
            rarity = achievement.rarity.name,
            points = achievement.points,
            isRepeatable = achievement.isRepeatable,
            isUnlocked = achievement.isUnlocked,
            unlockedAt = achievement.unlockedAt,
            currentProgress = achievement.currentProgress,
            totalRequired = achievement.totalRequired
        )
    }
    
    private fun getRequirementType(requirement: AchievementRequirement): String {
        return when (requirement) {
            is AchievementRequirement.StreakDays -> "STREAK_DAYS"
            is AchievementRequirement.TotalCount -> "TOTAL_COUNT"
            is AchievementRequirement.ConsecutiveCount -> "CONSECUTIVE_COUNT"
            is AchievementRequirement.MonthlyWorkouts -> "MONTHLY_WORKOUTS"
            is AchievementRequirement.MonthlyPerfectStreak -> "MONTHLY_PERFECT_STREAK"
            is AchievementRequirement.CumulativeWorkoutDays -> "CUMULATIVE_WORKOUT_DAYS"
            is AchievementRequirement.ProgramsCompleted -> "PROGRAMS_COMPLETED"
            is AchievementRequirement.TimeRange -> "TIME_RANGE"
            is AchievementRequirement.Combined -> "COMBINED"
        }
    }
    
    private fun getRequirementValue(requirement: AchievementRequirement): String {
        return when (requirement) {
            is AchievementRequirement.StreakDays -> requirement.days.toString()
            is AchievementRequirement.TotalCount -> requirement.count.toString()
            is AchievementRequirement.ConsecutiveCount -> requirement.count.toString()
            is AchievementRequirement.MonthlyWorkouts -> requirement.count.toString()
            is AchievementRequirement.MonthlyPerfectStreak -> "${requirement.month}-${requirement.year}"
            is AchievementRequirement.CumulativeWorkoutDays -> requirement.days.toString()
            is AchievementRequirement.ProgramsCompleted -> requirement.count.toString()
            is AchievementRequirement.TimeRange -> "${requirement.startDate}-${requirement.endDate}"
            is AchievementRequirement.Combined -> requirement.requirements.size.toString()
        }
    }
    
    // Public methods for UI
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.getUnlockedAchievements()
    }
    
    fun getAllAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.getAllAchievements()
    }
    
    fun getAchievementsByCategory(category: String): Flow<List<AchievementEntity>> {
        return achievementDao.getAchievementsByCategory(category)
    }
    
    fun getMonthlyStats(): Flow<List<MonthlyStatsEntity>> {
        return achievementDao.getAllMonthlyStats()
    }
}
