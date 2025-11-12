package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.ExerciseDao
import com.athleticai.app.data.database.dao.ExerciseUsageHistoryDao
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ExerciseUsageHistory
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class ExerciseSearchFilters(
    val query: String? = null,
    val muscleGroup: String? = null,
    val equipment: String? = null,
    val category: String? = null
)

data class ExerciseWithUsage(
    val exercise: Exercise,
    val usageHistory: ExerciseUsageHistory? = null
)

class ExerciseSearchRepository(
    private val exerciseDao: ExerciseDao,
    private val usageHistoryDao: ExerciseUsageHistoryDao
) {
    
    // Basic Exercise Operations
    fun getAllExercisesFlow(): Flow<List<Exercise>> = exerciseDao.getAllExercises()
    
    suspend fun getAllExercisesList(): List<Exercise> = exerciseDao.getAllExercisesList()
    
    suspend fun getExerciseById(id: String): Exercise? = exerciseDao.getExerciseById(id)
    
    // Advanced Search Functions
    suspend fun searchExercises(
        query: String,
        limit: Int = 100
    ): List<Exercise> {
        return if (query.isBlank()) {
            emptyList()
        } else {
            exerciseDao.searchExercisesAdvanced(query, limit)
        }
    }
    
    suspend fun searchExercisesWithFilters(
        filters: ExerciseSearchFilters,
        limit: Int = 100
    ): List<Exercise> {
        return exerciseDao.searchExercisesWithFilters(
            query = filters.query?.takeIf { it.isNotBlank() },
            muscleGroup = filters.muscleGroup?.takeIf { it.isNotBlank() },
            equipment = filters.equipment?.takeIf { it.isNotBlank() },
            category = filters.category?.takeIf { it.isNotBlank() },
            limit = limit
        )
    }
    
    suspend fun getExercisesByMuscleGroup(
        muscleGroup: String,
        limit: Int = 100
    ): List<Exercise> {
        return exerciseDao.getExercisesByMuscleAndEquipment(muscleGroup, null, limit)
    }
    
    suspend fun getExercisesByEquipment(
        equipment: String,
        limit: Int = 100
    ): List<Exercise> {
        return exerciseDao.getExercisesByEquipment(equipment, limit)
    }
    
    // Filter Options
    suspend fun getAllMuscleGroups(): List<String> {
        return try {
            exerciseDao.getAllMuscleGroups()
        } catch (e: Exception) {
            // Fallback to predefined list if SQL JSON functions don't work
            listOf(
                "chest", "back", "shoulders", "biceps", "triceps", 
                "quadriceps", "hamstrings", "glutes", "calves", 
                "abdominals", "forearms", "neck", "traps"
            )
        }
    }
    
    suspend fun getAllEquipmentTypes(): List<String> = exerciseDao.getAllEquipmentTypes()
    
    suspend fun getAllCategories(): List<String> = exerciseDao.getAllCategories()
    
    // Usage History Functions
    fun getMostUsedExercises(limit: Int = 20): Flow<List<ExerciseUsageHistory>> = 
        usageHistoryDao.getMostUsedExercises(limit)
    
    fun getRecentlyUsedExercises(limit: Int = 10): Flow<List<ExerciseUsageHistory>> = 
        usageHistoryDao.getRecentlyUsedExercises(limit)
    
    suspend fun getExercisesWithUsageHistory(exerciseIds: List<String>): List<ExerciseWithUsage> {
        return exerciseIds.mapNotNull { exerciseId ->
            val exercise = exerciseDao.getExerciseById(exerciseId)
            if (exercise != null) {
                val usage = usageHistoryDao.getUsageHistoryForExercise(exerciseId)
                ExerciseWithUsage(exercise, usage)
            } else null
        }
    }
    
    suspend fun recordExerciseUsage(
        exerciseId: String,
        rpe: Float? = null,
        sets: Int? = null
    ) {
        val currentTime = System.currentTimeMillis()
        val existing = usageHistoryDao.getUsageHistoryForExercise(exerciseId)
        
        if (existing != null) {
            // Update existing record
            val newUsageCount = existing.usageCount + 1
            val newAverageRpe = if (rpe != null && existing.averageRpe != null) {
                (existing.averageRpe * existing.usageCount + rpe) / newUsageCount
            } else {
                rpe ?: existing.averageRpe
            }
            val newAverageSets = if (sets != null && existing.averageSets != null) {
                (existing.averageSets * existing.usageCount + sets) / newUsageCount
            } else {
                sets?.toFloat() ?: existing.averageSets
            }
            
            val updated = existing.copy(
                usageCount = newUsageCount,
                lastUsedDate = currentTime,
                averageRpe = newAverageRpe,
                averageSets = newAverageSets
            )
            usageHistoryDao.updateUsageHistory(updated)
        } else {
            // Create new record
            val newHistory = ExerciseUsageHistory(
                id = UUID.randomUUID().toString(),
                exerciseId = exerciseId,
                usageCount = 1,
                lastUsedDate = currentTime,
                averageRpe = rpe,
                averageSets = sets?.toFloat()
            )
            usageHistoryDao.insertUsageHistory(newHistory)
        }
    }
    
    // Utility functions for UI
    suspend fun getPopularExercisesForMuscleGroup(
        muscleGroup: String,
        limit: Int = 10
    ): List<ExerciseWithUsage> {
        val exercises = getExercisesByMuscleGroup(muscleGroup, limit)
        return exercises.map { exercise ->
            val usage = usageHistoryDao.getUsageHistoryForExercise(exercise.id)
            ExerciseWithUsage(exercise, usage)
        }.sortedByDescending { it.usageHistory?.usageCount ?: 0 }
    }
    
    suspend fun getExerciseCount(): Int = exerciseDao.getExerciseCount()
    
    // For debugging/testing
    suspend fun clearUsageHistory() {
        usageHistoryDao.deleteAll()
    }
}