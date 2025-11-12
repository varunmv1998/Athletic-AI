package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.WorkoutExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getExercisesForWorkout(workoutId: String): Flow<List<WorkoutExercise>>
    
    @Query("SELECT * FROM workout_exercises WHERE id = :exerciseId")
    suspend fun getWorkoutExerciseById(exerciseId: String): WorkoutExercise?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>)
    
    @Update
    suspend fun updateWorkoutExercise(exercise: WorkoutExercise)
    
    @Delete
    suspend fun deleteWorkoutExercise(exercise: WorkoutExercise)
    
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: String)
    
    @Query("UPDATE workout_exercises SET orderIndex = :newIndex WHERE id = :exerciseId")
    suspend fun updateExerciseOrder(exerciseId: String, newIndex: Int)
    
    @Query("SELECT COUNT(*) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun getExerciseCountForWorkout(workoutId: String): Int
    
    @Query("DELETE FROM workout_exercises")
    suspend fun deleteAll()
}