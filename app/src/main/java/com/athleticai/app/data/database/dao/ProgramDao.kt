package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.Program
import com.athleticai.app.data.database.entities.ProgramGoal
import com.athleticai.app.data.database.entities.ExperienceLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    
    @Query("SELECT * FROM programs ORDER BY createdAt DESC")
    fun getAllPrograms(): Flow<List<Program>>
    
    @Query("SELECT * FROM programs ORDER BY createdAt DESC")
    suspend fun getAllProgramsSync(): List<Program>
    
    @Query("SELECT * FROM programs WHERE isCustom = 0 ORDER BY createdAt DESC")
    fun getPreBuiltPrograms(): Flow<List<Program>>
    
    @Query("SELECT * FROM programs WHERE isCustom = 1 AND createdBy = :userId ORDER BY createdAt DESC")
    fun getCustomPrograms(userId: String = "default_user"): Flow<List<Program>>
    
    @Query("SELECT * FROM programs WHERE goal = :goal ORDER BY createdAt DESC")
    fun getProgramsByGoal(goal: ProgramGoal): Flow<List<Program>>
    
    @Query("SELECT * FROM programs WHERE experienceLevel = :level ORDER BY createdAt DESC")
    fun getProgramsByExperienceLevel(level: ExperienceLevel): Flow<List<Program>>
    
    @Query("SELECT * FROM programs WHERE goal = :goal AND experienceLevel = :level ORDER BY createdAt DESC")
    fun getProgramsByGoalAndLevel(goal: ProgramGoal, level: ExperienceLevel): Flow<List<Program>>
    
    @Query("SELECT * FROM programs WHERE id = :programId")
    suspend fun getProgramById(programId: String): Program?
    
    @Query("SELECT * FROM programs WHERE id = :programId")
    fun getProgramByIdFlow(programId: String): Flow<Program?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: Program)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<Program>)
    
    @Update
    suspend fun updateProgram(program: Program)
    
    @Delete
    suspend fun deleteProgram(program: Program)
    
    @Query("DELETE FROM programs WHERE id = :programId")
    suspend fun deleteProgramById(programId: String)
    
    @Query("SELECT COUNT(*) FROM programs")
    suspend fun getProgramCount(): Int
    
    @Query("SELECT COUNT(*) FROM programs WHERE isCustom = 0")
    suspend fun getPreBuiltProgramCount(): Int
    
    @Query("SELECT COUNT(*) FROM programs WHERE isCustom = 1 AND createdBy = :userId")
    suspend fun getCustomProgramCount(userId: String = "default_user"): Int
    
    @Query("SELECT DISTINCT goal FROM programs")
    suspend fun getAvailableGoals(): List<ProgramGoal>
    
    @Query("SELECT DISTINCT experienceLevel FROM programs")
    suspend fun getAvailableExperienceLevels(): List<ExperienceLevel>
    
    // Search programs
    @Query("""
        SELECT * FROM programs 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchPrograms(query: String): Flow<List<Program>>
    
    // Filter programs by multiple criteria
    @Query("""
        SELECT * FROM programs 
        WHERE (:goal IS NULL OR goal = :goal)
        AND (:level IS NULL OR experienceLevel = :level)
        AND (:minWeeks IS NULL OR durationWeeks >= :minWeeks)
        AND (:maxWeeks IS NULL OR durationWeeks <= :maxWeeks)
        AND (:isCustom IS NULL OR isCustom = :isCustom)
        ORDER BY createdAt DESC
    """)
    fun filterPrograms(
        goal: ProgramGoal? = null,
        level: ExperienceLevel? = null,
        minWeeks: Int? = null,
        maxWeeks: Int? = null,
        isCustom: Boolean? = null
    ): Flow<List<Program>>
}