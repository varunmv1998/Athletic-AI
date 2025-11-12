package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.RoutineExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getExercisesForRoutine(routineId: String): Flow<List<RoutineExercise>>
    
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesForRoutineSync(routineId: String): List<RoutineExercise>
    
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesByRoutine(routineId: String): List<RoutineExercise>
    
    @Query("SELECT * FROM routine_exercises WHERE id = :exerciseId")
    suspend fun getRoutineExerciseById(exerciseId: String): RoutineExercise?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: RoutineExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(exercise: RoutineExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<RoutineExercise>)
    
    @Update
    suspend fun updateExercise(exercise: RoutineExercise)
    
    @Update
    suspend fun updateRoutineExercise(exercise: RoutineExercise)
    
    @Delete
    suspend fun deleteExercise(exercise: RoutineExercise)
    
    @Delete
    suspend fun deleteRoutineExercise(exercise: RoutineExercise)
    
    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteAllExercisesForRoutine(routineId: String)
    
    @Query("UPDATE routine_exercises SET orderIndex = :newIndex WHERE id = :exerciseId")
    suspend fun updateExerciseOrder(exerciseId: String, newIndex: Int)
    
    @Transaction
    suspend fun replaceRoutineExercises(routineId: String, exercises: List<RoutineExercise>) {
        deleteAllExercisesForRoutine(routineId)
        insertExercises(exercises)
    }
}