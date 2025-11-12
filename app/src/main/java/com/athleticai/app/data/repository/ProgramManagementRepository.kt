package com.athleticai.app.data.repository

import android.util.Log
import com.athleticai.app.data.database.dao.*
import com.athleticai.app.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.Calendar

/**
 * Repository for managing the Program feature - structured workout programs
 * separate from the existing 90-day PPL program.
 */
class ProgramManagementRepository(
    private val programDao: ProgramDao,
    private val programDayDao: ProgramDayDao,
    private val enrollmentDao: UserProgramEnrollmentDao,
    private val completionDao: ProgramDayCompletionDao,
    private val routineDao: WorkoutRoutineDao,
    private val programDayExerciseDao: ProgramDayExerciseDao? = null
) {
    private val TAG = "ProgramManagementRepo"
    
    // ====== Program Day and Exercise Management ======
    
    suspend fun getProgramDays(programId: String): List<ProgramDay> =
        programDayDao.getProgramDaysSync(programId)
    
    suspend fun getExercisesForDay(programDayId: String): List<ProgramDayExercise> =
        programDayExerciseDao?.getExercisesForDay(programDayId) ?: emptyList()
    
    suspend fun getCompletionsForEnrollment(enrollmentId: String): List<ProgramDayCompletion> =
        completionDao.getCompletionsForEnrollmentSync(enrollmentId)
    
    // ====== Program Discovery & Browsing ======
    
    fun getAllPrograms(): Flow<List<Program>> = programDao.getAllPrograms()
    
    fun getPreBuiltPrograms(): Flow<List<Program>> = programDao.getPreBuiltPrograms()
    
    fun getCustomPrograms(userId: String = "default_user"): Flow<List<Program>> = 
        programDao.getCustomPrograms(userId)
    
    fun getProgramsByGoal(goal: ProgramGoal): Flow<List<Program>> = 
        programDao.getProgramsByGoal(goal)
    
    fun getProgramsByExperienceLevel(level: ExperienceLevel): Flow<List<Program>> = 
        programDao.getProgramsByExperienceLevel(level)
    
    fun searchPrograms(query: String): Flow<List<Program>> = 
        programDao.searchPrograms(query)
    
    suspend fun getProgramById(programId: String): Program? = 
        programDao.getProgramById(programId)
    
    fun getProgramWithDays(programId: String): Flow<Pair<Program?, List<ProgramDay>>> = 
        programDao.getProgramByIdFlow(programId).combine(
            programDayDao.getProgramDays(programId)
        ) { program, days ->
            program to days
        }
    
    // ====== Enrollment Management ======
    
    fun getActiveEnrollment(userId: String = "default_user"): Flow<UserProgramEnrollment?> = 
        enrollmentDao.getActiveEnrollment(userId)
        
    suspend fun getActiveEnrollmentSync(userId: String = "default_user"): UserProgramEnrollment? =
        enrollmentDao.getActiveEnrollmentSync(userId)
    
    fun getActiveEnrollmentWithProgram(userId: String = "default_user"): Flow<UserProgramEnrollmentWithProgram?> = 
        enrollmentDao.getActiveEnrollmentWithProgram(userId)
    
    suspend fun enrollInProgram(programId: String, userId: String = "default_user"): Result<UserProgramEnrollment> {
        return try {
            // Check if program exists
            val program = programDao.getProgramById(programId)
                ?: return Result.failure(Exception("Program not found"))
            
            // Deactivate any existing enrollment
            enrollmentDao.deactivateAllEnrollments(userId)
            
            // Calculate estimated completion date
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, program.durationWeeks)
            val estimatedCompletionDate = calendar.timeInMillis
            
            // Create new enrollment
            val enrollment = UserProgramEnrollment(
                id = UUID.randomUUID().toString(),
                userId = userId,
                programId = programId,
                enrolledAt = System.currentTimeMillis(),
                currentDay = 0, // Not started yet
                status = EnrollmentStatus.ENROLLED,
                estimatedCompletionDate = estimatedCompletionDate,
                totalDaysSkipped = 0,
                totalDaysCompleted = 0
            )
            
            enrollmentDao.insertEnrollment(enrollment)
            Log.d(TAG, "Successfully enrolled in program: $programId")
            Result.success(enrollment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enroll in program", e)
            Result.failure(e)
        }
    }
    
    suspend fun unenrollFromProgram(enrollmentId: String): Result<Unit> {
        return try {
            enrollmentDao.cancelEnrollment(enrollmentId)
            Log.d(TAG, "Successfully unenrolled from program")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unenroll from program", e)
            Result.failure(e)
        }
    }
    
    suspend fun pauseProgram(enrollmentId: String): Result<Unit> {
        return try {
            enrollmentDao.pauseEnrollment(enrollmentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resumeProgram(enrollmentId: String): Result<Unit> {
        return try {
            enrollmentDao.resumeEnrollment(enrollmentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ====== Day Management & Progress ======
    
    suspend fun getCurrentProgramDay(enrollmentId: String): ProgramDay? {
        val enrollment = enrollmentDao.getEnrollmentById(enrollmentId) ?: return null
        return if (enrollment.currentDay > 0) {
            programDayDao.getProgramDay(enrollment.programId, enrollment.currentDay)
        } else null
    }
    
    suspend fun getNextProgramDay(enrollmentId: String): ProgramDay? {
        val enrollment = enrollmentDao.getEnrollmentById(enrollmentId) ?: return null
        return programDayDao.getProgramDay(enrollment.programId, enrollment.currentDay + 1)
    }
    
    suspend fun startProgramDay(enrollmentId: String): Result<ProgramDay> {
        return try {
            val enrollment = enrollmentDao.getEnrollmentById(enrollmentId)
                ?: return Result.failure(Exception("Enrollment not found"))
            
            // If first day, mark program as started
            if (enrollment.currentDay == 0) {
                enrollmentDao.startProgram(enrollmentId)
            }
            
            // Move to next day
            val nextDay = enrollment.currentDay + 1
            val programDay = programDayDao.getProgramDay(enrollment.programId, nextDay)
                ?: return Result.failure(Exception("Program day not found"))
            
            enrollmentDao.updateCurrentDay(enrollmentId, nextDay)
            Log.d(TAG, "Started program day $nextDay")
            Result.success(programDay)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start program day", e)
            Result.failure(e)
        }
    }
    
    suspend fun completeCurrentDay(
        enrollmentId: String, 
        workoutSessionId: String? = null,
        notes: String? = null
    ): Result<Unit> {
        return try {
            val enrollment = enrollmentDao.getEnrollmentById(enrollmentId)
                ?: return Result.failure(Exception("Enrollment not found"))
            
            val currentDay = programDayDao.getProgramDay(enrollment.programId, enrollment.currentDay)
                ?: return Result.failure(Exception("Current day not found"))
            
            // Mark day as completed
            completionDao.markDayCompleted(
                enrollmentId = enrollmentId,
                programDayId = currentDay.id,
                programDayNumber = enrollment.currentDay,
                workoutSessionId = workoutSessionId,
                notes = notes
            )
            
            // Update enrollment stats
            enrollmentDao.incrementCompletedDays(enrollmentId)
            
            // Check if program is complete
            val totalDays = programDayDao.getTotalDaysCount(enrollment.programId)
            if (enrollment.currentDay >= totalDays) {
                enrollmentDao.completeProgram(enrollmentId)
                Log.d(TAG, "Program completed!")
            }
            
            Log.d(TAG, "Completed day ${enrollment.currentDay}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete current day", e)
            Result.failure(e)
        }
    }
    
    suspend fun skipCurrentDay(
        enrollmentId: String,
        reason: String? = null
    ): Result<Unit> {
        return try {
            val enrollment = enrollmentDao.getEnrollmentById(enrollmentId)
                ?: return Result.failure(Exception("Enrollment not found"))
            
            val currentDay = programDayDao.getProgramDay(enrollment.programId, enrollment.currentDay)
                ?: return Result.failure(Exception("Current day not found"))
            
            // Mark day as skipped
            completionDao.markDaySkipped(
                enrollmentId = enrollmentId,
                programDayId = currentDay.id,
                programDayNumber = enrollment.currentDay,
                reason = reason
            )
            
            // Update enrollment stats
            enrollmentDao.incrementSkippedDays(enrollmentId)
            
            // Update estimated completion date (add one day)
            enrollment.estimatedCompletionDate?.let { date ->
                val newDate = date + (24 * 60 * 60 * 1000) // Add one day in milliseconds
                enrollmentDao.updateEstimatedCompletionDate(enrollmentId, newDate)
            }
            
            Log.d(TAG, "Skipped day ${enrollment.currentDay}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to skip current day", e)
            Result.failure(e)
        }
    }
    
    // ====== Progress Tracking ======
    
    fun getProgramProgress(enrollmentId: String): Flow<ProgramProgressSummary?> = flow {
        val enrollment = enrollmentDao.getEnrollmentById(enrollmentId)
        if (enrollment == null) {
            emit(null)
            return@flow
        }
        
        val totalDays = programDayDao.getTotalDaysCount(enrollment.programId)
        val completions = completionDao.getCompletionsForEnrollmentSync(enrollmentId)
        
        val completedDays = completions.count { it.status == CompletionStatus.COMPLETED }
        val skippedDays = completions.count { it.status == CompletionStatus.SKIPPED }
        val partialDays = completions.count { it.status == CompletionStatus.PARTIAL }
        
        // Calculate streak
        val currentStreak = calculateCurrentStreak(completions)
        val longestStreak = calculateLongestStreak(completions)
        
        // Calculate average workouts per week
        val daysSinceStart = if (enrollment.startedAt != null) {
            (System.currentTimeMillis() - enrollment.startedAt) / (24 * 60 * 60 * 1000)
        } else 0L
        
        val weeksElapsed = (daysSinceStart / 7.0).coerceAtLeast(1.0)
        val averageWorkoutsPerWeek = (completedDays / weeksElapsed).toFloat()
        
        val progressPercentage = if (totalDays > 0) {
            (enrollment.currentDay.toFloat() / totalDays) * 100
        } else 0f
        
        emit(ProgramProgressSummary(
            totalDays = totalDays,
            completedDays = completedDays,
            skippedDays = skippedDays,
            partialDays = partialDays,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            averageWorkoutsPerWeek = averageWorkoutsPerWeek,
            estimatedCompletionDate = enrollment.estimatedCompletionDate,
            progressPercentage = progressPercentage
        ))
    }
    
    private fun calculateCurrentStreak(completions: List<ProgramDayCompletion>): Int {
        if (completions.isEmpty()) return 0
        
        val sortedCompletions = completions
            .filter { it.status == CompletionStatus.COMPLETED }
            .sortedByDescending { it.completionDate }
        
        if (sortedCompletions.isEmpty()) return 0
        
        var streak = 1
        val oneDayMillis = 24 * 60 * 60 * 1000
        
        for (i in 0 until sortedCompletions.size - 1) {
            val current = sortedCompletions[i].completionDate
            val next = sortedCompletions[i + 1].completionDate
            
            // Allow for one rest day between workouts
            if (current - next <= 2 * oneDayMillis) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateLongestStreak(completions: List<ProgramDayCompletion>): Int {
        // Implementation similar to calculateCurrentStreak but tracking max
        return calculateCurrentStreak(completions) // Simplified for now
    }
    
    // ====== Custom Program Creation ======
    
    suspend fun createCustomProgram(
        name: String,
        description: String,
        goal: ProgramGoal,
        experienceLevel: ExperienceLevel,
        durationWeeks: Int,
        workoutsPerWeek: Int,
        equipmentRequired: List<String>,
        programDays: List<ProgramDay>,
        userId: String = "default_user"
    ): Result<Program> {
        return try {
            val programId = UUID.randomUUID().toString()
            
            val program = Program(
                id = programId,
                name = name,
                description = description,
                goal = goal,
                experienceLevel = experienceLevel,
                durationWeeks = durationWeeks,
                workoutsPerWeek = workoutsPerWeek,
                equipmentRequired = equipmentRequired,
                isCustom = true,
                createdBy = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Insert program
            programDao.insertProgram(program)
            
            // Insert program days with updated programId
            val updatedDays = programDays.map { day ->
                day.copy(programId = programId)
            }
            programDayDao.insertProgramDays(updatedDays)
            
            Log.d(TAG, "Successfully created custom program: $name")
            Result.success(program)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create custom program", e)
            Result.failure(e)
        }
    }
    
    suspend fun duplicateProgram(
        sourceProgramId: String,
        newName: String,
        userId: String = "default_user"
    ): Result<Program> {
        return try {
            val sourceProgram = programDao.getProgramById(sourceProgramId)
                ?: return Result.failure(Exception("Source program not found"))
            
            val sourceDays = programDayDao.getProgramDaysSync(sourceProgramId)
            
            val newProgramId = UUID.randomUUID().toString()
            val newProgram = sourceProgram.copy(
                id = newProgramId,
                name = newName,
                isCustom = true,
                createdBy = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Create day ID mapping
            val dayIdMapping = sourceDays.associate {
                it.id to UUID.randomUUID().toString()
            }
            
            // Insert new program
            programDao.insertProgram(newProgram)
            
            // Copy program days
            programDayDao.copyProgramDays(sourceProgramId, newProgramId, dayIdMapping)
            
            Log.d(TAG, "Successfully duplicated program: $newName")
            Result.success(newProgram)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to duplicate program", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteCustomProgram(programId: String): Result<Unit> {
        return try {
            // Check if it's a custom program
            val program = programDao.getProgramById(programId)
            if (program == null || !program.isCustom) {
                return Result.failure(Exception("Cannot delete non-custom program"))
            }
            
            programDao.deleteProgramById(programId)
            Log.d(TAG, "Successfully deleted custom program")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete custom program", e)
            Result.failure(e)
        }
    }
    
    // ====== Statistics ======
    
    suspend fun getProgramStatistics(userId: String = "default_user"): ProgramStatistics {
        val completedCount = enrollmentDao.getCompletedProgramCount(userId)
        val customCount = programDao.getCustomProgramCount(userId)
        val enrollmentHistory = enrollmentDao.getUserEnrollmentHistory(userId).first()
        
        return ProgramStatistics(
            totalProgramsCompleted = completedCount,
            totalCustomProgramsCreated = customCount,
            totalEnrollments = enrollmentHistory.size,
            favoriteGoal = calculateFavoriteGoal(enrollmentHistory),
            averageProgramDuration = calculateAverageDuration(enrollmentHistory)
        )
    }
    
    private suspend fun calculateFavoriteGoal(enrollments: List<UserProgramEnrollment>): ProgramGoal? {
        if (enrollments.isEmpty()) return null
        
        val goalCounts = mutableMapOf<ProgramGoal, Int>()
        enrollments.forEach { enrollment ->
            programDao.getProgramById(enrollment.programId)?.let { program ->
                goalCounts[program.goal] = (goalCounts[program.goal] ?: 0) + 1
            }
        }
        
        return goalCounts.maxByOrNull { it.value }?.key
    }
    
    private fun calculateAverageDuration(enrollments: List<UserProgramEnrollment>): Int {
        val completedEnrollments = enrollments.filter { 
            it.status == EnrollmentStatus.COMPLETED && 
            it.startedAt != null && 
            it.actualCompletionDate != null 
        }
        
        if (completedEnrollments.isEmpty()) return 0
        
        val totalDays = completedEnrollments.sumOf { enrollment ->
            val days = (enrollment.actualCompletionDate!! - enrollment.startedAt!!) / (24 * 60 * 60 * 1000)
            days.toInt()
        }
        
        return totalDays / completedEnrollments.size
    }
}

/**
 * Program statistics data class
 */
data class ProgramStatistics(
    val totalProgramsCompleted: Int,
    val totalCustomProgramsCreated: Int,
    val totalEnrollments: Int,
    val favoriteGoal: ProgramGoal?,
    val averageProgramDuration: Int // in days
)