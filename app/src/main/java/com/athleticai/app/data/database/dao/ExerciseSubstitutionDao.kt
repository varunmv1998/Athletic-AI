package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athleticai.app.data.database.entities.ExerciseSubstitution
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSubstitutionDao {
    
    @Query("SELECT * FROM exercise_substitutions WHERE primaryExerciseId = :primaryExerciseId")
    fun getSubstitutionsForExercise(primaryExerciseId: String): Flow<List<ExerciseSubstitution>>
    
    @Query("SELECT * FROM exercise_substitutions WHERE primaryExerciseId = :primaryExerciseId")
    suspend fun getSubstitutionsForExerciseSync(primaryExerciseId: String): List<ExerciseSubstitution>
    
    @Query("SELECT * FROM exercise_substitutions WHERE muscleGroup = :muscleGroup")
    fun getSubstitutionsByMuscleGroup(muscleGroup: String): Flow<List<ExerciseSubstitution>>
    
    @Query("SELECT * FROM exercise_substitutions WHERE substituteExerciseId = :substituteExerciseId")
    fun getSubstitutionsWithExercise(substituteExerciseId: String): Flow<List<ExerciseSubstitution>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitution(substitution: ExerciseSubstitution)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitutions(substitutions: List<ExerciseSubstitution>)
    
    @Query("DELETE FROM exercise_substitutions WHERE id = :id")
    suspend fun deleteSubstitution(id: String)
    
    @Query("DELETE FROM exercise_substitutions WHERE primaryExerciseId = :primaryExerciseId")
    suspend fun deleteSubstitutionsForExercise(primaryExerciseId: String)
    
    @Query("SELECT COUNT(*) FROM exercise_substitutions")
    suspend fun getSubstitutionCount(): Int
}