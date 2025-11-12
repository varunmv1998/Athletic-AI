package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.ActiveWorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveWorkoutSessionDao {
    @Query("SELECT * FROM active_workout_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): Flow<ActiveWorkoutSession?>
    
    @Query("SELECT * FROM active_workout_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): ActiveWorkoutSession?
    
    @Query("SELECT * FROM active_workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ActiveWorkoutSession?
    
    @Query("SELECT * FROM active_workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ActiveWorkoutSession>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ActiveWorkoutSession)
    
    @Update
    suspend fun updateSession(session: ActiveWorkoutSession)
    
    @Delete
    suspend fun deleteSession(session: ActiveWorkoutSession)
    
    @Query("UPDATE active_workout_sessions SET isActive = 0, endTime = :endTime WHERE id = :sessionId")
    suspend fun finishSession(sessionId: String, endTime: Long)
    
    @Query("UPDATE active_workout_sessions SET exercises = :exercises WHERE id = :sessionId")
    suspend fun updateSessionExercises(sessionId: String, exercises: String)
    
    @Query("DELETE FROM active_workout_sessions WHERE isActive = 0 AND startTime < :cutoffTime")
    suspend fun deleteOldSessions(cutoffTime: Long)
}