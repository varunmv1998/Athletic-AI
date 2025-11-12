package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.athleticai.app.data.database.entities.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {
    
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>>
    
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsForExerciseInSession(sessionId: String, exerciseId: String): Flow<List<WorkoutSet>>
    
    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSetsForExercise(exerciseId: String, limit: Int): Flow<List<WorkoutSet>>
    
    @Query("SELECT COUNT(*) FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetCountForSession(sessionId: String): Int 
    
    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getPersonalRecord(exerciseId: String): Double?
    
    @Insert
    suspend fun insertSet(workoutSet: WorkoutSet)
    
    @Query("DELETE FROM workout_sets WHERE setId = :setId")
    suspend fun deleteSet(setId: String)
    
    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: String)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSetsForSessionSync(sessionId: String): List<WorkoutSet>

    @Query("SELECT * FROM workout_sets WHERE timestamp BETWEEN :from AND :to")
    suspend fun getSetsBetween(from: String, to: String): List<WorkoutSet>
    
    @Query("DELETE FROM workout_sets")
    suspend fun deleteAll()
}
