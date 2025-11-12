package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.athleticai.app.data.database.entities.ProgramEnrollment
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramEnrollmentDao {
    
    @Query("SELECT * FROM program_enrollment WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveEnrollment(): ProgramEnrollment?
    
    @Query("SELECT * FROM program_enrollment WHERE isActive = 1 LIMIT 1")
    fun getActiveEnrollmentFlow(): Flow<ProgramEnrollment?>
    
    @Query("SELECT * FROM program_enrollment WHERE programId = :programId")
    suspend fun getEnrollmentByProgramId(programId: String): ProgramEnrollment?
    
    @Query("SELECT * FROM program_enrollment ORDER BY startDate DESC")
    fun getAllEnrollments(): Flow<List<ProgramEnrollment>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: ProgramEnrollment)
    
    @Update
    suspend fun updateEnrollment(enrollment: ProgramEnrollment)
    
    @Query("UPDATE program_enrollment SET isActive = 0 WHERE enrollmentId = :enrollmentId")
    suspend fun deactivateEnrollment(enrollmentId: String)
    
    @Query("UPDATE program_enrollment SET currentDay = :currentDay, lastWorkoutDate = :lastWorkoutDate, completedWorkouts = completedWorkouts + 1 WHERE enrollmentId = :enrollmentId")
    suspend fun updateProgress(enrollmentId: String, currentDay: Int, lastWorkoutDate: String)
    
    @Query("DELETE FROM program_enrollment WHERE enrollmentId = :enrollmentId")
    suspend fun deleteEnrollment(enrollmentId: String)
    
    @Query("DELETE FROM program_enrollment")
    suspend fun deleteAll()
}