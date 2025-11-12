package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.ProgramDay
import com.athleticai.app.data.database.entities.DayType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDayDao {
    
    @Query("SELECT * FROM program_days WHERE programId = :programId ORDER BY dayNumber ASC")
    fun getProgramDays(programId: String): Flow<List<ProgramDay>>
    
    @Query("SELECT * FROM program_days WHERE programId = :programId ORDER BY dayNumber ASC")
    suspend fun getProgramDaysSync(programId: String): List<ProgramDay>
    
    @Query("SELECT * FROM program_days WHERE programId = :programId AND dayNumber = :dayNumber")
    suspend fun getProgramDay(programId: String, dayNumber: Int): ProgramDay?
    
    @Query("SELECT * FROM program_days WHERE id = :dayId")
    suspend fun getProgramDayById(dayId: String): ProgramDay?
    
    @Query("SELECT * FROM program_days WHERE programId = :programId AND weekNumber = :weekNumber ORDER BY dayNumber ASC")
    fun getProgramWeek(programId: String, weekNumber: Int): Flow<List<ProgramDay>>
    
    @Query("SELECT * FROM program_days WHERE programId = :programId AND dayType = :dayType ORDER BY dayNumber ASC")
    fun getProgramDaysByType(programId: String, dayType: DayType): Flow<List<ProgramDay>>
    
    @Query("SELECT COUNT(*) FROM program_days WHERE programId = :programId")
    suspend fun getTotalDaysCount(programId: String): Int
    
    @Query("SELECT COUNT(*) FROM program_days WHERE programId = :programId AND dayType = :dayType")
    suspend fun getDaysCountByType(programId: String, dayType: DayType): Int
    
    @Query("SELECT MAX(weekNumber) FROM program_days WHERE programId = :programId")
    suspend fun getTotalWeeks(programId: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramDay(day: ProgramDay)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramDays(days: List<ProgramDay>)
    
    @Update
    suspend fun updateProgramDay(day: ProgramDay)
    
    @Delete
    suspend fun deleteProgramDay(day: ProgramDay)
    
    @Query("DELETE FROM program_days WHERE programId = :programId")
    suspend fun deleteProgramDays(programId: String)
    
    @Query("DELETE FROM program_days WHERE programId = :programId AND dayNumber = :dayNumber")
    suspend fun deleteProgramDay(programId: String, dayNumber: Int)
    
    // Get next workout day after a specific day
    @Query("""
        SELECT * FROM program_days 
        WHERE programId = :programId 
        AND dayNumber > :currentDay 
        AND dayType = 'WORKOUT'
        ORDER BY dayNumber ASC 
        LIMIT 1
    """)
    suspend fun getNextWorkoutDay(programId: String, currentDay: Int): ProgramDay?
    
    // Get program days for a specific week range
    @Query("""
        SELECT * FROM program_days 
        WHERE programId = :programId 
        AND weekNumber BETWEEN :startWeek AND :endWeek
        ORDER BY dayNumber ASC
    """)
    fun getProgramDaysInWeekRange(programId: String, startWeek: Int, endWeek: Int): Flow<List<ProgramDay>>
    
    // Update routine assignment for a program day
    @Query("UPDATE program_days SET routineId = :routineId WHERE id = :dayId")
    suspend fun updateDayRoutine(dayId: String, routineId: String?)
    
    // Copy program days for custom program creation
    @Transaction
    suspend fun copyProgramDays(sourceProgramId: String, targetProgramId: String, dayIdMapping: Map<String, String>) {
        val sourceDays = getProgramDaysSync(sourceProgramId)
        val targetDays = sourceDays.map { day ->
            day.copy(
                id = dayIdMapping[day.id] ?: java.util.UUID.randomUUID().toString(),
                programId = targetProgramId
            )
        }
        insertProgramDays(targetDays)
    }
}