package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.CustomProgram
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomProgramDao {
    @Query("SELECT * FROM custom_programs ORDER BY createdDate DESC")
    fun getAllPrograms(): Flow<List<CustomProgram>>
    
    @Query("SELECT * FROM custom_programs WHERE isActive = 1 LIMIT 1")
    fun getActiveProgram(): Flow<CustomProgram?>
    
    @Query("SELECT * FROM custom_programs WHERE id = :programId")
    suspend fun getProgramById(programId: String): CustomProgram?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: CustomProgram)
    
    @Update
    suspend fun updateProgram(program: CustomProgram)
    
    @Delete
    suspend fun deleteProgram(program: CustomProgram)
    
    @Query("UPDATE custom_programs SET isActive = 0")
    suspend fun deactivateAllPrograms()
    
    @Query("UPDATE custom_programs SET isActive = 1 WHERE id = :programId")
    suspend fun activateProgram(programId: String)
    
    @Query("UPDATE custom_programs SET totalWorkouts = (SELECT COUNT(*) FROM custom_workouts WHERE programId = :programId) WHERE id = :programId")
    suspend fun updateTotalWorkouts(programId: String)
    
    @Query("DELETE FROM custom_programs")
    suspend fun deleteAll()
}