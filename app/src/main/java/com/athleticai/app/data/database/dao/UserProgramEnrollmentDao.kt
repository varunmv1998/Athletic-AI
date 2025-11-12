package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.UserProgramEnrollment
import com.athleticai.app.data.database.entities.UserProgramEnrollmentWithProgram
import com.athleticai.app.data.database.entities.EnrollmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgramEnrollmentDao {
    
    @Query("SELECT * FROM user_program_enrollments WHERE userId = :userId AND status IN ('ENROLLED', 'ACTIVE') LIMIT 1")
    fun getActiveEnrollment(userId: String = "default_user"): Flow<UserProgramEnrollment?>
    
    @Query("SELECT * FROM user_program_enrollments WHERE userId = :userId AND status IN ('ENROLLED', 'ACTIVE') LIMIT 1")
    suspend fun getActiveEnrollmentSync(userId: String = "default_user"): UserProgramEnrollment?
    
    @Transaction
    @Query("SELECT * FROM user_program_enrollments WHERE userId = :userId AND status IN ('ENROLLED', 'ACTIVE') LIMIT 1")
    fun getActiveEnrollmentWithProgram(userId: String = "default_user"): Flow<UserProgramEnrollmentWithProgram?>
    
    @Query("SELECT * FROM user_program_enrollments WHERE id = :enrollmentId")
    suspend fun getEnrollmentById(enrollmentId: String): UserProgramEnrollment?
    
    @Transaction
    @Query("SELECT * FROM user_program_enrollments WHERE id = :enrollmentId")
    fun getEnrollmentWithProgram(enrollmentId: String): Flow<UserProgramEnrollmentWithProgram?>
    
    @Query("SELECT * FROM user_program_enrollments WHERE userId = :userId ORDER BY enrolledAt DESC")
    fun getUserEnrollmentHistory(userId: String = "default_user"): Flow<List<UserProgramEnrollment>>
    
    @Transaction
    @Query("SELECT * FROM user_program_enrollments WHERE userId = :userId ORDER BY enrolledAt DESC")
    fun getUserEnrollmentHistoryWithPrograms(userId: String = "default_user"): Flow<List<UserProgramEnrollmentWithProgram>>
    
    @Query("SELECT * FROM user_program_enrollments WHERE programId = :programId AND userId = :userId")
    suspend fun getEnrollmentForProgram(programId: String, userId: String = "default_user"): UserProgramEnrollment?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: UserProgramEnrollment)
    
    @Update
    suspend fun updateEnrollment(enrollment: UserProgramEnrollment)
    
    @Delete
    suspend fun deleteEnrollment(enrollment: UserProgramEnrollment)
    
    @Query("UPDATE user_program_enrollments SET status = :status WHERE id = :enrollmentId")
    suspend fun updateEnrollmentStatus(enrollmentId: String, status: EnrollmentStatus)
    
    @Query("UPDATE user_program_enrollments SET currentDay = :day, lastActivityDate = :activityDate WHERE id = :enrollmentId")
    suspend fun updateCurrentDay(enrollmentId: String, day: Int, activityDate: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_program_enrollments SET startedAt = :startedAt, status = 'ACTIVE' WHERE id = :enrollmentId")
    suspend fun startProgram(enrollmentId: String, startedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_program_enrollments SET totalDaysSkipped = totalDaysSkipped + 1 WHERE id = :enrollmentId")
    suspend fun incrementSkippedDays(enrollmentId: String)
    
    @Query("UPDATE user_program_enrollments SET totalDaysCompleted = totalDaysCompleted + 1, lastActivityDate = :activityDate WHERE id = :enrollmentId")
    suspend fun incrementCompletedDays(enrollmentId: String, activityDate: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE user_program_enrollments 
        SET status = 'COMPLETED', actualCompletionDate = :completionDate 
        WHERE id = :enrollmentId
    """)
    suspend fun completeProgram(enrollmentId: String, completionDate: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE user_program_enrollments 
        SET status = 'CANCELLED' 
        WHERE id = :enrollmentId
    """)
    suspend fun cancelEnrollment(enrollmentId: String)
    
    @Query("""
        UPDATE user_program_enrollments 
        SET status = 'PAUSED' 
        WHERE id = :enrollmentId
    """)
    suspend fun pauseEnrollment(enrollmentId: String)
    
    @Query("""
        UPDATE user_program_enrollments 
        SET status = 'ACTIVE', lastActivityDate = :resumeDate 
        WHERE id = :enrollmentId
    """)
    suspend fun resumeEnrollment(enrollmentId: String, resumeDate: Long = System.currentTimeMillis())
    
    // Deactivate all active enrollments for user (used before enrolling in new program)
    @Query("""
        UPDATE user_program_enrollments 
        SET status = 'CANCELLED' 
        WHERE userId = :userId AND status IN ('ENROLLED', 'ACTIVE')
    """)
    suspend fun deactivateAllEnrollments(userId: String = "default_user")
    
    @Query("SELECT COUNT(*) FROM user_program_enrollments WHERE status = 'COMPLETED' AND userId = :userId")
    suspend fun getCompletedProgramCount(userId: String = "default_user"): Int
    
    @Query("""
        UPDATE user_program_enrollments 
        SET estimatedCompletionDate = :estimatedDate 
        WHERE id = :enrollmentId
    """)
    suspend fun updateEstimatedCompletionDate(enrollmentId: String, estimatedDate: Long)
}