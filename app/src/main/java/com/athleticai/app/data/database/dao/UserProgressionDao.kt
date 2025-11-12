package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.athleticai.app.data.database.entities.UserProgression
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressionDao {
    
    @Query("SELECT * FROM user_progression WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getProgressionForExercise(exerciseId: String): UserProgression?
    
    @Query("SELECT * FROM user_progression WHERE exerciseId = :exerciseId LIMIT 1")
    fun getProgressionForExerciseFlow(exerciseId: String): Flow<UserProgression?>
    
    @Query("SELECT * FROM user_progression ORDER BY lastUpdateDate DESC")
    fun getAllProgressions(): Flow<List<UserProgression>>
    
    @Query("SELECT * FROM user_progression WHERE lastUpdateDate >= :since ORDER BY lastUpdateDate DESC")
    fun getRecentProgressions(since: String): Flow<List<UserProgression>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgression(progression: UserProgression)
    
    @Update
    suspend fun updateProgression(progression: UserProgression)
    
    @Query("UPDATE user_progression SET currentWeight = :newWeight, lastUpdateDate = :updateDate, lastRpe = :rpe, sessionCount = sessionCount + 1 WHERE exerciseId = :exerciseId")
    suspend fun updateProgressionWeight(exerciseId: String, newWeight: Double, updateDate: String, rpe: Double?)
    
    @Query("DELETE FROM user_progression WHERE exerciseId = :exerciseId")
    suspend fun deleteProgressionForExercise(exerciseId: String)
    
    @Query("SELECT COUNT(*) FROM user_progression")
    suspend fun getProgressionCount(): Int
}