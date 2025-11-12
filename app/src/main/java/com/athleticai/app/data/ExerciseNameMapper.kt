package com.athleticai.app.data

import android.util.Log
import com.athleticai.app.data.database.entities.Exercise

/**
 * Maps exercise names from program templates to actual Exercise IDs
 * Handles common variations and synonyms
 */
class ExerciseNameMapper(private val exercises: List<Exercise>) {
    private val TAG = "ExerciseNameMapper"
    
    // Create mapping of normalized names to Exercise objects
    private val exerciseMap: Map<String, Exercise> by lazy {
        exercises.associateBy { normalizeExerciseName(it.name) }
    }
    
    // Create reverse lookup by common variations
    private val variationMap: Map<String, String> by lazy {
        buildVariationMap()
    }
    
    /**
     * Find exercise by name, handling common variations
     */
    fun findExercise(exerciseName: String): Exercise? {
        val normalized = normalizeExerciseName(exerciseName)
        
        // Direct match first
        exerciseMap[normalized]?.let { return it }
        
        // Try variations
        variationMap[normalized]?.let { canonicalName ->
            exerciseMap[canonicalName]?.let { return it }
        }
        
        // Fuzzy matching for close matches
        return findFuzzyMatch(normalized)
    }
    
    /**
     * Normalize exercise name for consistent matching
     */
    private fun normalizeExerciseName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }
    
    /**
     * Build map of common exercise name variations
     */
    private fun buildVariationMap(): Map<String, String> {
        val variations = mutableMapOf<String, String>()
        
        // For each exercise, create common variations
        exercises.forEach { exercise ->
            val canonical = normalizeExerciseName(exercise.name)
            
            // Add common variations based on exercise name patterns
            addVariations(canonical, exercise.name, variations)
        }
        
        // Add manual mappings for common template exercise names
        addTemplateVariations(variations)
        
        return variations
    }
    
    /**
     * Add variations for a specific exercise
     */
    private fun addVariations(canonical: String, originalName: String, variations: MutableMap<String, String>) {
        val lower = originalName.lowercase()
        
        // Handle dumbbell variations
        if (lower.contains("dumbbell")) {
            val withoutDumbbell = canonical.replace("dumbbell_", "")
            variations[withoutDumbbell] = canonical
            variations["db_$withoutDumbbell"] = canonical
        }
        
        // Handle barbell variations
        if (lower.contains("barbell")) {
            val withoutBarbell = canonical.replace("barbell_", "")
            variations[withoutBarbell] = canonical
            variations["bb_$withoutBarbell"] = canonical
        }
        
        // Handle machine variations
        if (lower.contains("machine")) {
            val withoutMachine = canonical.replace("machine_", "").replace("_machine", "")
            variations[withoutMachine] = canonical
        }
        
        // Handle cable variations
        if (lower.contains("cable")) {
            val withoutCable = canonical.replace("cable_", "")
            variations[withoutCable] = canonical
        }
    }
    
    /**
     * Add manual mappings for common template exercise names
     */
    private fun addTemplateVariations(variations: MutableMap<String, String>) {
        // Common exercise name mappings from templates
        val templateMappings = mapOf(
            // Push exercises
            "push_up" to listOf("pushup", "push_ups", "pushups", "bodyweight_push_up"),
            "push_ups" to listOf("push_up", "pushup", "pushups", "bodyweight_push_up"),
            "incline_push_up" to listOf("incline_pushup", "incline_push_ups", "elevated_push_up"),
            "knee_push_up" to listOf("knee_pushup", "modified_push_up", "assisted_push_up"),
            "diamond_push_up" to listOf("diamond_pushup", "close_grip_push_up", "tricep_push_up"),
            "wall_push_up" to listOf("wall_pushup", "standing_push_up"),
            
            "bench_press" to listOf("barbell_bench_press", "flat_bench_press", "chest_press"),
            "incline_bench_press" to listOf("incline_barbell_press", "incline_chest_press"),
            "dumbbell_press" to listOf("dumbbell_bench_press", "db_press", "dumbbell_chest_press"),
            "incline_dumbbell_press" to listOf("incline_db_press", "incline_dumbbell_chest_press"),
            
            "overhead_press" to listOf("shoulder_press", "military_press", "standing_press"),
            "dumbbell_shoulder_press" to listOf("db_shoulder_press", "seated_shoulder_press"),
            "arnold_press" to listOf("arnold_shoulder_press", "arnold_dumbbell_press"),
            
            "dips" to listOf("tricep_dips", "parallel_bar_dips", "bodyweight_dips"),
            "tricep_dips" to listOf("dips", "chair_dips", "bench_dips"),
            
            // Pull exercises  
            "pull_up" to listOf("pull_ups", "bodyweight_pull_up", "chin_up"),
            "pull_ups" to listOf("pull_up", "bodyweight_pull_up", "chin_up"),
            "chin_up" to listOf("chin_ups", "underhand_pull_up", "supinated_pull_up"),
            "assisted_pull_up" to listOf("band_assisted_pull_up", "machine_assisted_pull_up"),
            "lat_pulldown" to listOf("lat_pull_down", "wide_grip_pulldown", "cable_pulldown"),
            
            "bent_over_row" to listOf("barbell_row", "bb_row", "bent_over_barbell_row"),
            "dumbbell_row" to listOf("db_row", "one_arm_row", "single_arm_row"),
            "seated_row" to listOf("cable_row", "seated_cable_row", "cable_seated_row"),
            "t_bar_row" to listOf("tbar_row", "landmine_row"),
            
            "deadlift" to listOf("barbell_deadlift", "conventional_deadlift", "bb_deadlift"),
            "romanian_deadlift" to listOf("rdl", "stiff_leg_deadlift", "straight_leg_deadlift"),
            "sumo_deadlift" to listOf("sumo_deadlift_high_pull", "wide_stance_deadlift"),
            
            // Leg exercises
            "squat" to listOf("bodyweight_squat", "air_squat", "prisoner_squat"),
            "barbell_squat" to listOf("back_squat", "bb_squat", "high_bar_squat", "low_bar_squat"),
            "front_squat" to listOf("barbell_front_squat", "bb_front_squat"),
            "goblet_squat" to listOf("dumbbell_goblet_squat", "kettlebell_goblet_squat"),
            "jump_squat" to listOf("squat_jump", "jumping_squat", "plyometric_squat"),
            
            "lunge" to listOf("lunges", "forward_lunge", "bodyweight_lunge"),
            "walking_lunge" to listOf("walking_lunges", "forward_walking_lunge"),
            "reverse_lunge" to listOf("backward_lunge", "reverse_lunges"),
            "lateral_lunge" to listOf("side_lunge", "lateral_lunges"),
            "bulgarian_split_squat" to listOf("rear_foot_elevated_split_squat", "split_squat"),
            
            "calf_raise" to listOf("calf_raises", "standing_calf_raise", "bodyweight_calf_raise"),
            "seated_calf_raise" to listOf("seated_calf_raises", "machine_calf_raise"),
            
            // Core exercises
            "plank" to listOf("front_plank", "forearm_plank", "standard_plank"),
            "side_plank" to listOf("lateral_plank", "single_arm_plank"),
            "bicycle_crunch" to listOf("bicycle_crunches", "bicycle_kicks"),
            "russian_twist" to listOf("russian_twists", "seated_twist", "oblique_twist"),
            "mountain_climber" to listOf("mountain_climbers", "alternating_knee_to_chest"),
            "dead_bug" to listOf("dead_bugs", "dying_bug", "alternate_arm_leg_extension"),
            
            // Cardio exercises
            "jumping_jack" to listOf("jumping_jacks", "star_jump", "star_jumps"),
            "burpee" to listOf("burpees", "squat_thrust", "squat_thrusts"),
            "high_knee" to listOf("high_knees", "knee_raise", "running_in_place"),
            "butt_kick" to listOf("butt_kicks", "heel_kick", "heel_kicks"),
            "mountain_climber" to listOf("mountain_climbers", "alternating_knee_to_chest"),
            
            // Equipment variations
            "battle_rope" to listOf("battle_ropes", "training_rope", "heavy_rope"),
            "kettlebell_swing" to listOf("kb_swing", "russian_swing", "american_swing"),
            "medicine_ball_slam" to listOf("med_ball_slam", "ball_slam", "overhead_slam"),
            "box_jump" to listOf("box_jumps", "jump_ups", "platform_jump"),
            
            // Flexibility/Recovery
            "stretching" to listOf("stretching_routine", "static_stretch", "flexibility_work"),
            "yoga_flow" to listOf("yoga", "vinyasa_flow", "flow_yoga"),
            "foam_rolling" to listOf("foam_roll", "self_massage", "myofascial_release")
        )
        
        // Add all mappings to variations map
        templateMappings.forEach { (canonical, variants) ->
            val normalizedCanonical = normalizeExerciseName(canonical)
            variants.forEach { variant ->
                val normalizedVariant = normalizeExerciseName(variant)
                variations[normalizedVariant] = normalizedCanonical
            }
        }
    }
    
    /**
     * Find fuzzy match for exercises that are close but not exact
     */
    private fun findFuzzyMatch(targetName: String): Exercise? {
        val targetWords = targetName.split("_").filter { it.isNotEmpty() }
        
        if (targetWords.isEmpty()) return null
        
        var bestMatch: Exercise? = null
        var bestScore = 0
        
        exerciseMap.values.forEach { exercise ->
            val exerciseWords = normalizeExerciseName(exercise.name).split("_").filter { it.isNotEmpty() }
            val score = calculateMatchScore(targetWords, exerciseWords)
            
            if (score > bestScore && score >= targetWords.size * 0.6) { // At least 60% match
                bestScore = score
                bestMatch = exercise
            }
        }
        
        if (bestMatch != null) {
            Log.d(TAG, "Fuzzy matched '$targetName' to '${bestMatch.name}' (score: $bestScore)")
        }
        
        return bestMatch
    }
    
    /**
     * Calculate match score between two word lists
     */
    private fun calculateMatchScore(target: List<String>, candidate: List<String>): Int {
        return target.count { targetWord ->
            candidate.any { candidateWord ->
                targetWord == candidateWord || 
                targetWord.startsWith(candidateWord) || 
                candidateWord.startsWith(targetWord)
            }
        }
    }
    
    /**
     * Log mapping results for debugging
     */
    fun logMappingResults(templateExercises: List<String>) {
        Log.d(TAG, "=== Exercise Mapping Results ===")
        templateExercises.forEach { exerciseName ->
            val found = findExercise(exerciseName)
            if (found != null) {
                Log.d(TAG, "✓ '$exerciseName' -> '${found.name}' (ID: ${found.id})")
            } else {
                Log.w(TAG, "✗ '$exerciseName' -> NO MATCH FOUND")
            }
        }
        Log.d(TAG, "=== End Mapping Results ===")
    }
}