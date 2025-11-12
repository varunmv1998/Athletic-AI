package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.ProgramDayCompletion
import com.athleticai.app.data.database.entities.CompletionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDayCompletionDao {
    
    @Query("SELECT * FROM program_day_completions WHERE enrollmentId = :enrollmentId ORDER BY programDayNumber ASC")
    fun getCompletionsForEnrollment(enrollmentId: String): Flow<List<ProgramDayCompletion>>
    
    @Query("SELECT * FROM program_day_completions WHERE enrollmentId = :enrollmentId ORDER BY programDayNumber ASC")
    suspend fun getCompletionsForEnrollmentSync(enrollmentId: String): List<ProgramDayCompletion>
    
    @Query("SELECT * FROM program_day_completions WHERE enrollmentId = :enrollmentId AND programDayNumber = :dayNumber")
    suspend fun getCompletionForDay(enrollmentId: String, dayNumber: Int): ProgramDayCompletion?
    
    @Query("SELECT * FROM program_day_completions WHERE enrollmentId = :enrollmentId AND status = :status ORDER BY programDayNumber ASC")
    fun getCompletionsByStatus(enrollmentId: String, status: CompletionStatus): Flow<List<ProgramDayCompletion>>
    
    @Query("SELECT COUNT(*) FROM program_day_completions WHERE enrollmentId = :enrollmentId")
    suspend fun getTotalCompletionCount(enrollmentId: String): Int
    
    @Query("SELECT COUNT(*) FROM program_day_completions WHERE enrollmentId = :enrollmentId AND status = :status")
    suspend fun getCompletionCountByStatus(enrollmentId: String, status: CompletionStatus): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: ProgramDayCompletion)
    
    @Update
    suspend fun updateCompletion(completion: ProgramDayCompletion)
    
    @Delete
    suspend fun deleteCompletion(completion: ProgramDayCompletion)
    
    @Query("DELETE FROM program_day_completions WHERE enrollmentId = :enrollmentId")
    suspend fun deleteAllCompletionsForEnrollment(enrollmentId: String)
    
    // Mark a day as completed
    @Transaction
    suspend fun markDayCompleted(
        enrollmentId: String,
        programDayId: String,
        programDayNumber: Int,
        workoutSessionId: String? = null,
        notes: String? = null
    ) {
        val completion = ProgramDayCompletion(
            id = java.util.UUID.randomUUID().toString(),
            enrollmentId = enrollmentId,
            programDayId = programDayId,
            programDayNumber = programDayNumber,
            completionDate = System.currentTimeMillis(),
            status = CompletionStatus.COMPLETED,
            workoutSessionId = workoutSessionId,
            notes = notes
        )
        insertCompletion(completion)
    }
    
    // Mark a day as skipped
    @Transaction
    suspend fun markDaySkipped(
        enrollmentId: String,
        programDayId: String,
        programDayNumber: Int,
        reason: String? = null
    ) {
        val completion = ProgramDayCompletion(
            id = java.util.UUID.randomUUID().toString(),
            enrollmentId = enrollmentId,
            programDayId = programDayId,
            programDayNumber = programDayNumber,
            completionDate = System.currentTimeMillis(),
            status = CompletionStatus.SKIPPED,
            skippedReason = reason
        )
        insertCompletion(completion)
    }
    
    // Get last completed workout day
    @Query("""
        SELECT * FROM program_day_completions 
        WHERE enrollmentId = :enrollmentId 
        AND status = 'COMPLETED'
        ORDER BY completionDate DESC 
        LIMIT 1
    """)
    suspend fun getLastCompletedDay(enrollmentId: String): ProgramDayCompletion?
    
    // Calculate current streak
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT * FROM program_day_completions 
            WHERE enrollmentId = :enrollmentId 
            AND status = 'COMPLETED'
            AND completionDate >= :streakStartDate
            ORDER BY completionDate DESC
        )
    """)
    suspend fun getCurrentStreak(enrollmentId: String, streakStartDate: Long): Int
    
    // Get completion rate
    @Query("""
        SELECT 
            CAST(COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS FLOAT) / 
            CAST(COUNT(*) AS FLOAT) * 100 as completionRate
        FROM program_day_completions 
        WHERE enrollmentId = :enrollmentId
    """)
    suspend fun getCompletionRate(enrollmentId: String): Float?
    
    // Get completions in date range
    @Query("""
        SELECT * FROM program_day_completions 
        WHERE enrollmentId = :enrollmentId 
        AND completionDate BETWEEN :startDate AND :endDate
        ORDER BY completionDate ASC
    """)
    fun getCompletionsInDateRange(enrollmentId: String, startDate: Long, endDate: Long): Flow<List<ProgramDayCompletion>>
}