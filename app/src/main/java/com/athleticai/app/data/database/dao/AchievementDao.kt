package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.AchievementEntity
import com.athleticai.app.data.database.entities.UserAchievementEntity
import com.athleticai.app.data.database.entities.MonthlyStatsEntity
import com.athleticai.app.data.database.entities.CumulativeStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    
    // Achievement operations
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements ORDER BY category, name")
    fun getAllAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY name")
    fun getAchievementsByCategory(category: String): Flow<List<AchievementEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)
    
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
    
    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockDate, currentProgress = :progress WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: String, unlockDate: String, progress: Int)
    
    // User achievement operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievement(userAchievement: UserAchievementEntity)
    
    @Query("SELECT * FROM user_achievements WHERE achievementId = :achievementId")
    suspend fun getUserAchievement(achievementId: String): UserAchievementEntity?
    
    // Monthly stats operations
    @Query("SELECT * FROM monthly_stats WHERE month = :month AND year = :year")
    suspend fun getMonthlyStats(month: String, year: Int): MonthlyStatsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyStats(monthlyStats: MonthlyStatsEntity)
    
    @Query("SELECT * FROM monthly_stats ORDER BY year DESC, month DESC")
    fun getAllMonthlyStats(): Flow<List<MonthlyStatsEntity>>
    
    // Cumulative stats operations
    @Query("SELECT * FROM cumulative_stats WHERE id = 'user_stats'")
    suspend fun getCumulativeStats(): CumulativeStatsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCumulativeStats(cumulativeStats: CumulativeStatsEntity)
    
    @Query("UPDATE cumulative_stats SET totalWorkoutDays = :totalDays, lastUpdated = :lastUpdated WHERE id = 'user_stats'")
    suspend fun updateTotalWorkoutDays(totalDays: Int, lastUpdated: String)
    
    @Query("UPDATE cumulative_stats SET totalProgramsCompleted = :totalPrograms, lastUpdated = :lastUpdated WHERE id = 'user_stats'")
    suspend fun updateTotalProgramsCompleted(totalPrograms: Int, lastUpdated: String)
    
    @Query("UPDATE cumulative_stats SET currentStreak = :currentStreak, longestStreak = :longestStreak, lastUpdated = :lastUpdated WHERE id = 'user_stats'")
    suspend fun updateStreaks(currentStreak: Int, longestStreak: Int, lastUpdated: String)
}

