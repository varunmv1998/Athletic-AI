package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athleticai.app.data.database.entities.ProgramExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramExerciseDao {
    
    @Query("SELECT * FROM program_exercises WHERE templateId = :templateId ORDER BY orderIndex")
    fun getExercisesByTemplate(templateId: String): Flow<List<ProgramExercise>>
    
    @Query("SELECT * FROM program_exercises WHERE templateId = :templateId ORDER BY orderIndex")
    suspend fun getExercisesByTemplateSync(templateId: String): List<ProgramExercise>
    
    @Query("SELECT * FROM program_exercises WHERE exerciseId = :exerciseId")
    fun getExercisesByExerciseId(exerciseId: String): Flow<List<ProgramExercise>>
    
    @Query("SELECT * FROM program_exercises WHERE id = :id")
    suspend fun getProgramExerciseById(id: String): ProgramExercise?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramExercise(exercise: ProgramExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramExercises(exercises: List<ProgramExercise>)
    
    @Query("DELETE FROM program_exercises WHERE templateId = :templateId")
    suspend fun deleteExercisesByTemplate(templateId: String)
    
    @Query("SELECT COUNT(*) FROM program_exercises WHERE templateId = :templateId")
    suspend fun getExerciseCountForTemplate(templateId: String): Int
}