package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.WorkoutRoutine
import com.athleticai.app.data.database.entities.WorkoutRoutineWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRoutineDao {
    @Query("SELECT * FROM workout_routines ORDER BY lastPerformed DESC, createdDate DESC")
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>
    
    @Query("SELECT * FROM workout_routines WHERE folderId = :folderId ORDER BY name ASC")
    fun getRoutinesByFolder(folderId: String): Flow<List<WorkoutRoutine>>
    
    @Query("SELECT * FROM workout_routines WHERE folderId IS NULL ORDER BY name ASC")
    fun getUncategorizedRoutines(): Flow<List<WorkoutRoutine>>
    
    @Query("SELECT * FROM workout_routines WHERE id = :routineId")
    suspend fun getRoutineById(routineId: String): WorkoutRoutine?
    
    @Transaction
    @Query("SELECT * FROM workout_routines WHERE id = :routineId")
    suspend fun getRoutineWithExercises(routineId: String): WorkoutRoutineWithExercises?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutine)
    
    @Update
    suspend fun updateRoutine(routine: WorkoutRoutine)
    
    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutine)
    
    @Query("UPDATE workout_routines SET lastPerformed = :timestamp WHERE id = :routineId")
    suspend fun updateLastPerformed(routineId: String, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM workout_routines")
    suspend fun getRoutineCount(): Int
}