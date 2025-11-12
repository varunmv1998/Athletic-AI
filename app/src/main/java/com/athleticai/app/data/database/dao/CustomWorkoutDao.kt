package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.CustomWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomWorkoutDao {
    @Query("SELECT * FROM custom_workouts WHERE programId = :programId ORDER BY dayNumber ASC")
    fun getWorkoutsForProgram(programId: String): Flow<List<CustomWorkout>>
    
    @Query("SELECT * FROM custom_workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: String): CustomWorkout?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: CustomWorkout)
    
    @Update
    suspend fun updateWorkout(workout: CustomWorkout)
    
    @Delete
    suspend fun deleteWorkout(workout: CustomWorkout)
    
    @Query("UPDATE custom_workouts SET exerciseCount = (SELECT COUNT(*) FROM workout_exercises WHERE workoutId = :workoutId) WHERE id = :workoutId")
    suspend fun updateExerciseCount(workoutId: String)
    
    @Query("UPDATE custom_workouts SET estimatedDurationMinutes = :duration WHERE id = :workoutId")
    suspend fun updateEstimatedDuration(workoutId: String, duration: Int)
    
    @Query("DELETE FROM custom_workouts WHERE programId = :programId")
    suspend fun deleteWorkoutsForProgram(programId: String)
    
    @Query("DELETE FROM custom_workouts")
    suspend fun deleteAll()
}