package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.athleticai.app.data.database.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WorkoutSessionDao {
    
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>
    
    @Query("SELECT * FROM workout_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): WorkoutSession?
    
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 LIMIT 1")
    suspend fun getActiveSession(): WorkoutSession?
    
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getCompletedSessions(limit: Int): Flow<List<WorkoutSession>>
    
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE isCompleted = 1")
    suspend fun getCompletedSessionCount(): Int
    
    @Insert
    suspend fun insertSession(session: WorkoutSession)
    
    @Update
    suspend fun updateSession(session: WorkoutSession)
    
    @Query("DELETE FROM workout_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
    
    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM workout_sessions WHERE startTime >= :startTime AND startTime < :endTime")
    suspend fun getSessionsInDateRange(startTime: LocalDateTime, endTime: LocalDateTime): List<WorkoutSession>
}