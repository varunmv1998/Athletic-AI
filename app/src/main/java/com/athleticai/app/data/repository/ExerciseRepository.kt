package com.athleticai.app.data.repository

import android.util.Log
import com.athleticai.app.data.database.dao.ExerciseDao
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.ExerciseDataLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val exerciseDataLoader: ExerciseDataLoader,
    private val exerciseDbRepository: ExerciseDbRepository? = null,
    private val migrationDao: com.athleticai.app.data.database.dao.ExerciseMigrationDao? = null
) {
    
    private val TAG = "ExerciseRepository"
    
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()
    
    suspend fun getExerciseById(id: String): Exercise? {
        // Try to get mapped exercise ID first
        val mappedId = migrationDao?.getMigrationMapping(id)?.newId ?: id
        return exerciseDao.getExerciseById(mappedId) ?: exerciseDao.getExerciseById(id)
    }
    
    fun searchExercises(query: String): Flow<List<Exercise>> = exerciseDao.searchExercises(query)
    
    fun getExercisesByCategory(category: String): Flow<List<Exercise>> = 
        exerciseDao.getExercisesByCategory(category)
    
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>> = 
        exerciseDao.getExercisesByMuscle(muscle)
    
    suspend fun getExercisesByMuscleSync(muscle: String): List<Exercise> =
        exerciseDao.getExercisesByMuscleSync(muscle)
    
    suspend fun getAllExercisesSync(): List<Exercise> = getAllExercises().first()
    
    suspend fun insertExercises(exercises: List<Exercise>) = exerciseDao.insertExercises(exercises)
    
    suspend fun getExerciseCount(): Int = exerciseDao.getExerciseCount()
    
    // Enhanced search methods for ExerciseDB integration
    suspend fun searchExercisesEnhanced(
        query: String? = null,
        bodyPart: String? = null,
        target: String? = null,
        equipment: String? = null,
        limit: Int = 100
    ): List<Exercise> = exerciseDao.searchExercisesEnhanced(query, bodyPart, target, equipment, limit)
    
    suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise> = 
        exerciseDao.getExercisesByBodyPart(bodyPart)
    
    suspend fun getExercisesByTarget(target: String): List<Exercise> = 
        exerciseDao.getExercisesByTarget(target)
    
    suspend fun getAllBodyParts(): List<String> = exerciseDao.getAllBodyParts()
    
    suspend fun getAllTargets(): List<String> = exerciseDao.getAllTargets()
    
    // ExerciseDB sync methods
    suspend fun triggerExerciseDbSync(): Result<Int> {
        return exerciseDbRepository?.performInitialSync() 
            ?: Result.failure(Exception("ExerciseDB repository not available"))
    }
    
    suspend fun isExerciseDbSyncNeeded(): Boolean {
        return exerciseDbRepository?.isInitialSyncNeeded() ?: false
    }
    
    suspend fun getExerciseDbCount(): Int {
        return exerciseDao.getExercisesBySource("EXERCISE_DB").size
    }
    
    suspend fun clearExerciseDbData() {
        exerciseDao.deleteExercisesBySource("EXERCISE_DB")
        exerciseDbRepository?.clearSyncMetadata()
    }
    
    suspend fun performExerciseMigration(): Result<List<com.athleticai.app.data.database.entities.ExerciseMigration>> {
        return if (migrationDao != null) {
            com.athleticai.app.utils.FuzzyMatcher.performExerciseMigration(exerciseDao, migrationDao)
        } else {
            Result.failure(Exception("Migration DAO not available"))
        }
    }
    
    suspend fun initializeExerciseData(): Boolean {
        return try {
            Log.d(TAG, "Starting exercise data initialization...")
            val exerciseCount = exerciseDao.getExerciseCount()
            Log.d(TAG, "Current exercise count in database: $exerciseCount")
            
            if (exerciseCount == 0) {
                Log.d(TAG, "No exercises found, loading from assets...")
                val exercises = exerciseDataLoader.loadExercisesFromAssets()
                Log.d(TAG, "Loaded ${exercises.size} exercises from assets")
                
                if (exercises.isNotEmpty()) {
                    Log.d(TAG, "Inserting exercises into database...")
                    exerciseDao.insertExercises(exercises)
                    Log.d(TAG, "Exercise data initialization completed successfully")
                    true
                } else {
                    Log.e(TAG, "No exercises loaded from assets")
                    false
                }
            } else {
                Log.d(TAG, "Exercises already exist in database, skipping initialization")
                true // Already initialized
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during exercise initialization: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}
