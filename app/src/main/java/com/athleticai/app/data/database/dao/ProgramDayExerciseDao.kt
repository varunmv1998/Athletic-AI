package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import com.athleticai.app.data.database.entities.ProgramDayExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDayExerciseDao {
    
    @Query("SELECT * FROM program_day_exercises WHERE programDayId = :programDayId ORDER BY orderIndex")
    suspend fun getExercisesForDay(programDayId: String): List<ProgramDayExercise>
    
    @Query("SELECT * FROM program_day_exercises WHERE programDayId = :programDayId ORDER BY orderIndex")
    fun getExercisesForDayFlow(programDayId: String): Flow<List<ProgramDayExercise>>
    
    @Query("""
        SELECT pde.* FROM program_day_exercises pde
        INNER JOIN program_days pd ON pd.id = pde.programDayId
        WHERE pd.programId = :programId
        ORDER BY pd.dayNumber, pde.orderIndex
    """)
    suspend fun getAllExercisesForProgram(programId: String): List<ProgramDayExercise>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ProgramDayExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ProgramDayExercise>)
    
    @Update
    suspend fun updateExercise(exercise: ProgramDayExercise)
    
    @Delete
    suspend fun deleteExercise(exercise: ProgramDayExercise)
    
    @Query("DELETE FROM program_day_exercises WHERE programDayId = :programDayId")
    suspend fun deleteAllExercisesForDay(programDayId: String)
    
    @Query("SELECT COUNT(*) FROM program_day_exercises WHERE programDayId = :programDayId")
    suspend fun getExerciseCountForDay(programDayId: String): Int
    
    @Query("""
        SELECT pde.* FROM program_day_exercises pde
        WHERE pde.programDayId = :programDayId AND pde.isCardio = 1
        ORDER BY pde.orderIndex
    """)
    suspend fun getCardioExercisesForDay(programDayId: String): List<ProgramDayExercise>
    
    @Query("""
        SELECT pde.* FROM program_day_exercises pde
        WHERE pde.programDayId = :programDayId AND pde.isCardio = 0
        ORDER BY pde.orderIndex
    """)
    suspend fun getStrengthExercisesForDay(programDayId: String): List<ProgramDayExercise>
}