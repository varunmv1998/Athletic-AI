package com.athleticai.app.data.repository

import com.athleticai.app.data.ProgramDataLoader
import com.athleticai.app.data.ProgramParseResult
import com.athleticai.app.data.database.dao.ProgramEnrollmentDao
import com.athleticai.app.data.database.dao.ProgramTemplateDao
import com.athleticai.app.data.database.dao.ProgramExerciseDao
import com.athleticai.app.data.database.dao.UserProgressionDao
import com.athleticai.app.data.database.dao.ExerciseSubstitutionDao
import com.athleticai.app.data.database.entities.ProgramEnrollment
import com.athleticai.app.data.database.entities.ProgramTemplate
import com.athleticai.app.data.database.entities.ProgramExercise
import com.athleticai.app.data.database.entities.UserProgression
import com.athleticai.app.data.database.entities.ExerciseSubstitution
import com.athleticai.app.data.database.dao.DaySubstitutionDao
import com.athleticai.app.data.database.entities.DaySubstitution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import android.util.Log

class ProgramRepository(
    private val enrollmentDao: ProgramEnrollmentDao,
    private val templateDao: ProgramTemplateDao,
    private val exerciseDao: ProgramExerciseDao,
    private val progressionDao: UserProgressionDao,
    private val substitutionDao: ExerciseSubstitutionDao,
    private val daySubstitutionDao: DaySubstitutionDao,
    private val programDataLoader: ProgramDataLoader
) {
    
    private val TAG = "ProgramRepository"
    
    // Program initialization
    suspend fun initializeProgramData(): Boolean {
        return try {
            Log.d(TAG, "Starting program data initialization...")
            val templateCount = templateDao.getBuiltInTemplateCount()
            Log.d(TAG, "Current template count in database: $templateCount")
            
            // Check if we have both templates AND exercises
            val hasExercises = if (templateCount > 0) {
                val exerciseCount = exerciseDao.getExerciseCountForTemplate("push_a")
                Log.d(TAG, "Exercise count for push_a template: $exerciseCount")
                exerciseCount > 0
            } else {
                false
            }
            
            if (templateCount == 0 || !hasExercises) {
                Log.d(TAG, "Templates missing or exercises missing, loading from assets...")
                when (val result = programDataLoader.loadProgramFromAssets()) {
                    is ProgramParseResult.Success -> {
                        Log.d(TAG, "Successfully loaded program data from assets")
                        Log.d(TAG, "Inserting ${result.templates.size} templates...")
                        templateDao.insertTemplates(result.templates)
                        
                        Log.d(TAG, "Inserting ${result.exercises.size} program exercises...")
                        exerciseDao.insertProgramExercises(result.exercises)
                        
                        // Try to insert substitutions, but don't fail if they don't work
                        try {
                            Log.d(TAG, "Inserting ${result.substitutions.size} substitutions...")
                            substitutionDao.insertSubstitutions(result.substitutions)
                            Log.d(TAG, "Substitutions inserted successfully")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to insert substitutions (this is not critical): ${e.message}")
                            // Continue with initialization even if substitutions fail
                        }
                        
                        Log.d(TAG, "Program data initialization completed successfully")
                        true
                    }
                    is ProgramParseResult.Error -> {
                        Log.e(TAG, "Failed to load program data: ${result.message}")
                        false
                    }
                }
            } else {
                Log.d(TAG, "Templates and exercises already exist in database, skipping initialization")
                // Let's verify what templates exist
                val existingTemplates = templateDao.getAllTemplates().first()
                Log.d(TAG, "Existing templates: ${existingTemplates.map { "${it.templateKey}: ${it.id}" }}")
                
                true // Already initialized
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during program initialization: ${e.message}", e)
            false
        }
    }
    
    // Enrollment management
    fun getActiveEnrollment(): Flow<ProgramEnrollment?> = enrollmentDao.getActiveEnrollmentFlow()
    
    suspend fun getActiveEnrollmentSync(): ProgramEnrollment? = enrollmentDao.getActiveEnrollment()
    
    suspend fun enrollInProgram(programId: String = "90day_program_v1"): ProgramEnrollment {
        // Deactivate any existing enrollment
        getActiveEnrollmentSync()?.let { enrollment ->
            enrollmentDao.deactivateEnrollment(enrollment.enrollmentId)
        }
        
        val newEnrollment = ProgramEnrollment(
            enrollmentId = UUID.randomUUID().toString(),
            programId = programId,
            startDate = LocalDateTime.now(),
            currentDay = 1,
            isActive = true
        )
        
        enrollmentDao.insertEnrollment(newEnrollment)
        return newEnrollment
    }
    
    suspend fun updateEnrollmentProgress(enrollmentId: String, newDay: Int) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        enrollmentDao.updateProgress(enrollmentId, newDay, now)
    }
    
    // Template management
    fun getAllTemplates(): Flow<List<ProgramTemplate>> = templateDao.getAllTemplates()
    
    suspend fun getTemplateById(templateId: String): ProgramTemplate? = templateDao.getTemplateById(templateId)
    
    fun getTemplatesByPhase(phase: String): Flow<List<ProgramTemplate>> = templateDao.getBuiltInTemplates()
    
    // Exercise management
    fun getExercisesForTemplate(templateId: String): Flow<List<ProgramExercise>> = 
        exerciseDao.getExercisesByTemplate(templateId)
    
    suspend fun getExercisesForTemplateSync(templateId: String): List<ProgramExercise> = 
        exerciseDao.getExercisesByTemplateSync(templateId)
    
    // Progression management
    suspend fun getProgressionForExercise(exerciseId: String): UserProgression? = 
        progressionDao.getProgressionForExercise(exerciseId)
    
    suspend fun updateProgression(
        exerciseId: String, 
        newWeight: Double, 
        rpe: Double? = null
    ) {
        val existing = progressionDao.getProgressionForExercise(exerciseId)
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        if (existing != null) {
            progressionDao.updateProgressionWeight(exerciseId, newWeight, now, rpe)
        } else {
            // Create new progression entry
            val newProgression = UserProgression(
                progressionId = UUID.randomUUID().toString(),
                exerciseId = exerciseId,
                currentWeight = newWeight,
                lastUpdateDate = LocalDateTime.now(),
                lastRpe = rpe,
                sessionCount = 1
            )
            progressionDao.insertProgression(newProgression)
        }
    }
    
    // Substitution management
    fun getSubstitutionsForExercise(exerciseId: String): Flow<List<ExerciseSubstitution>> = 
        substitutionDao.getSubstitutionsForExercise(exerciseId)
    
    suspend fun getSubstitutionsForExerciseSync(exerciseId: String): List<ExerciseSubstitution> = 
        substitutionDao.getSubstitutionsForExerciseSync(exerciseId)
    
    // Program logic
    suspend fun getTodaysTemplate(): ProgramTemplate? {
        val enrollment = getActiveEnrollmentSync() ?: return null
        val templateId = calculateTemplateForDay(enrollment.currentDay)
        Log.d(TAG, "Getting today's template for day ${enrollment.currentDay}, calculated templateId: $templateId")
        
        val template = templateId?.let { getTemplateById(it) }
        Log.d(TAG, "Retrieved template: ${template?.templateKey ?: "null"}")
        return template
    }
    
    suspend fun getTodaysExercises(): List<ProgramExercise> {
        val template = getTodaysTemplate() ?: return emptyList()
        Log.d(TAG, "Getting exercises for template: ${template.templateKey} (${template.id})")
        
        val exercises = getExercisesForTemplateSync(template.templateKey)
        Log.d(TAG, "Retrieved ${exercises.size} exercises for template ${template.templateKey}")
        
        exercises.forEach { exercise ->
            Log.d(TAG, "  - Exercise: ${exercise.exerciseId} (order: ${exercise.orderIndex})")
        }
        // Apply per-day substitutions
        val day = getActiveEnrollmentSync()?.currentDay
        if (day != null) {
            val map = getSubstitutionsMapForDay(day)
            if (map.isNotEmpty()) {
                val mapped = exercises.map { pe ->
                    val sub = map[pe.exerciseId]
                    if (sub != null) {
                        pe.copy(
                            id = "${pe.templateId}_${sub}_${pe.orderIndex}_sub",
                            exerciseId = sub
                        )
                    } else pe
                }
                Log.d(TAG, "Applied ${map.size} substitutions for day $day")
                return mapped
            }
        }
        return exercises
    }
    
    private fun calculateTemplateForDay(day: Int): String? {
        // 90-day program with 6 days per week, 1 rest day
        val dayInCycle = ((day - 1) % 7)
        val templateId = when (dayInCycle) {
            0 -> "push_a"
            1 -> "pull_a" 
            2 -> "legs_a"
            3 -> "push_b"
            4 -> "pull_b"
            5 -> "legs_b"
            6 -> null // Rest day
            else -> null
        }
        
        Log.d(TAG, "Calculated template for day $day: dayInCycle=$dayInCycle, templateId=$templateId")
        return templateId
    }
    
    fun calculateCurrentPhase(day: Int): String {
        return when (day) {
            in 1..28 -> "foundation" // Weeks 1-4
            in 29..56 -> "strength"  // Weeks 5-8
            in 57..84 -> "intensity" // Weeks 9-12
            in 85..91 -> "deload"    // Week 13
            else -> "foundation"
        }
    }
    
    fun calculateWeekNumber(day: Int): Int {
        return ((day - 1) / 7) + 1
    }
    
    fun isRestDay(day: Int): Boolean {
        return ((day - 1) % 7) == 6
    }

    // Day substitution API
    suspend fun getSubstitutionsMapForDay(programDay: Int): Map<String, String> {
        return daySubstitutionDao.getAllForDay(programDay)
            .associate { it.originalExerciseId to it.substituteExerciseId }
    }

    suspend fun setDaySubstitution(programDay: Int, originalId: String, substituteId: String) {
        val sub = DaySubstitution(
            id = java.util.UUID.randomUUID().toString(),
            programDay = programDay,
            originalExerciseId = originalId,
            substituteExerciseId = substituteId,
            timestamp = java.time.LocalDateTime.now()
        )
        daySubstitutionDao.upsert(sub)
    }

    suspend fun resetDaySubstitution(programDay: Int, originalId: String) {
        daySubstitutionDao.delete(programDay, originalId)
    }

    suspend fun getDaySubstitution(programDay: Int, originalId: String): String? {
        return daySubstitutionDao.getSubstitute(programDay, originalId)
    }

    suspend fun clearDaySubstitutions(programDay: Int) {
        daySubstitutionDao.clearDay(programDay)
    }
    
    // Test data methods
    suspend fun createEnrollment(enrollment: ProgramEnrollment) {
        enrollmentDao.insertEnrollment(enrollment)
    }
    
    suspend fun clearEnrollments() {
        enrollmentDao.deleteAll()
    }
}
