package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.ExerciseUsageHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseUsageHistoryDao {
    @Query("SELECT * FROM exercise_usage_history ORDER BY usageCount DESC, lastUsedDate DESC LIMIT :limit")
    fun getMostUsedExercises(limit: Int = 20): Flow<List<ExerciseUsageHistory>>
    
    @Query("SELECT * FROM exercise_usage_history ORDER BY lastUsedDate DESC LIMIT :limit")
    fun getRecentlyUsedExercises(limit: Int = 10): Flow<List<ExerciseUsageHistory>>
    
    @Query("SELECT * FROM exercise_usage_history WHERE exerciseId = :exerciseId")
    suspend fun getUsageHistoryForExercise(exerciseId: String): ExerciseUsageHistory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistory(history: ExerciseUsageHistory)
    
    @Update
    suspend fun updateUsageHistory(history: ExerciseUsageHistory)
    
    @Query("UPDATE exercise_usage_history SET usageCount = usageCount + 1, lastUsedDate = :currentTime WHERE exerciseId = :exerciseId")
    suspend fun incrementUsageCount(exerciseId: String, currentTime: Long)
    
    @Query("DELETE FROM exercise_usage_history WHERE exerciseId = :exerciseId")
    suspend fun deleteUsageHistory(exerciseId: String)
    
    @Query("DELETE FROM exercise_usage_history")
    suspend fun deleteAll()
}