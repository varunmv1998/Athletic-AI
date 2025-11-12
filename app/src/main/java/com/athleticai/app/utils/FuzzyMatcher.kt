package com.athleticai.app.utils

import android.util.Log
import com.athleticai.app.data.database.dao.ExerciseDao
import com.athleticai.app.data.database.dao.ExerciseMigrationDao
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ExerciseMigration
import com.athleticai.app.data.repository.ExerciseDbRepository
import kotlin.math.max
import kotlin.math.min

/**
 * Utility for fuzzy matching exercises during migration
 */
object FuzzyMatcher {
    
    private const val TAG = "FuzzyMatcher"
    private const val MIN_CONFIDENCE_THRESHOLD = 0.7f
    
    /**
     * Find the best match for a local exercise in the ExerciseDB dataset
     */
    fun findBestMatch(
        localExercise: Exercise,
        exerciseDbExercises: List<Exercise>
    ): ExerciseMigration? {
        
        var bestMatch: Exercise? = null
        var bestScore = 0f
        
        exerciseDbExercises.forEach { edbExercise ->
            val score = calculateSimilarityScore(localExercise, edbExercise)
            if (score > bestScore && score >= MIN_CONFIDENCE_THRESHOLD) {
                bestScore = score
                bestMatch = edbExercise
            }
        }
        
        return bestMatch?.let { match ->
            ExerciseMigration(
                oldId = localExercise.id,
                newId = match.id,
                confidenceScore = bestScore,
                isManualMapping = false
            )
        }
    }
    
    /**
     * Calculate similarity score between two exercises
     */
    private fun calculateSimilarityScore(local: Exercise, edb: Exercise): Float {
        var score = 0f
        var weights = 0f
        
        // Name similarity (highest weight)
        val nameWeight = 0.5f
        val nameSimilarity = stringSimilarity(
            local.name.lowercase().trim(),
            edb.name.lowercase().trim()
        )
        score += nameSimilarity * nameWeight
        weights += nameWeight
        
        // Equipment similarity
        val equipmentWeight = 0.2f
        if (local.equipment != null && edb.equipment != null) {
            val equipmentSimilarity = stringSimilarity(
                local.equipment.lowercase(),
                edb.equipment.lowercase()
            )
            score += equipmentSimilarity * equipmentWeight
            weights += equipmentWeight
        }
        
        // Primary muscle similarity
        val muscleWeight = 0.2f
        val localPrimary = local.primaryMuscles.firstOrNull()?.lowercase() ?: ""
        val edbPrimary = edb.targetMuscle?.lowercase() ?: ""
        if (localPrimary.isNotEmpty() && edbPrimary.isNotEmpty()) {
            val muscleSimilarity = stringSimilarity(localPrimary, edbPrimary)
            score += muscleSimilarity * muscleWeight
            weights += muscleWeight
        }
        
        // Category/body part similarity
        val categoryWeight = 0.1f
        if (edb.bodyPart != null) {
            val categorySimilarity = stringSimilarity(
                local.category.lowercase(),
                edb.bodyPart.lowercase()
            )
            score += categorySimilarity * categoryWeight
            weights += categoryWeight
        }
        
        return if (weights > 0) score / weights else 0f
    }
    
    /**
     * Calculate string similarity using Levenshtein distance
     */
    private fun stringSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1f
        if (s1.isEmpty() || s2.isEmpty()) return 0f
        
        val distance = levenshteinDistance(s1, s2)
        val maxLength = max(s1.length, s2.length)
        
        return 1f - (distance.toFloat() / maxLength)
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) {
            dp[i][0] = i
        }
        
        for (j in 0..len2) {
            dp[0][j] = j
        }
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                
                dp[i][j] = min(
                    min(
                        dp[i - 1][j] + 1,      // deletion
                        dp[i][j - 1] + 1       // insertion
                    ),
                    dp[i - 1][j - 1] + cost    // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Perform fuzzy matching for all local exercises
     */
    suspend fun performExerciseMigration(
        exerciseDao: ExerciseDao,
        migrationDao: ExerciseMigrationDao
    ): Result<List<ExerciseMigration>> {
        return try {
            Log.d(TAG, "Starting exercise migration with fuzzy matching...")
            
            // Get all local exercises
            val localExercises = exerciseDao.getExercisesBySource("LOCAL")
            
            // Get all ExerciseDB exercises  
            val edbExercises = exerciseDao.getExercisesBySource("EXERCISE_DB")
            
            if (edbExercises.isEmpty()) {
                Log.w(TAG, "No ExerciseDB exercises found. Perform initial sync first.")
                return Result.failure(Exception("No ExerciseDB exercises found"))
            }
            
            val mappings = mutableListOf<ExerciseMigration>()
            
            localExercises.forEach { localExercise ->
                val mapping = findBestMatch(localExercise, edbExercises)
                mapping?.let { mappings.add(it) }
            }
            
            // Insert mappings into database
            migrationDao.insertMappings(mappings)
            
            Log.d(TAG, "Created ${mappings.size} exercise mappings out of ${localExercises.size} local exercises")
            Result.success(mappings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during exercise migration", e)
            Result.failure(e)
        }
    }
    
}