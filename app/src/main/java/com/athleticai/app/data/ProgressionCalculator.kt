package com.athleticai.app.data

import com.athleticai.app.data.database.entities.UserProgression
import com.athleticai.app.data.database.entities.WorkoutSet

class ProgressionCalculator {
    
    companion object {
        private const val LINEAR_PROGRESSION_INCREMENT = 2.5 // kg
        private const val DOUBLE_PROGRESSION_INCREMENT = 2.5 // kg per hand
        private const val RPE_THRESHOLD_FOR_PROGRESSION = 7.5
        private const val VOLUME_PROGRESSION_REP_INCREMENT = 1
        
        fun calculateProgression(
            progressionType: String,
            currentProgression: UserProgression?,
            recentSets: List<WorkoutSet>,
            targetRpe: Double
        ): ProgressionResult {
            
            if (recentSets.isEmpty()) {
                return ProgressionResult.NoChange("No sets completed")
            }
            
            val averageRpe = recentSets.map { it.rpe }.average()
            val allSetsCompleted = recentSets.all { it.reps > 0 }
            val currentWeight = currentProgression?.currentWeight ?: getDefaultStartingWeight(progressionType)
            
            return when (progressionType.lowercase()) {
                "linear" -> calculateLinearProgression(currentWeight, averageRpe, allSetsCompleted)
                "double" -> calculateDoubleProgression(currentWeight, averageRpe, recentSets, targetRpe)
                "volume" -> calculateVolumeProgression(currentWeight, averageRpe, recentSets, targetRpe)
                "bodyweight" -> calculateBodyweightProgression(currentWeight, averageRpe, recentSets)
                else -> ProgressionResult.NoChange("Unknown progression type: $progressionType")
            }
        }
        
        private fun calculateLinearProgression(
            currentWeight: Double,
            averageRpe: Double,
            allSetsCompleted: Boolean
        ): ProgressionResult {
            return if (averageRpe <= RPE_THRESHOLD_FOR_PROGRESSION && allSetsCompleted) {
                val newWeight = currentWeight + LINEAR_PROGRESSION_INCREMENT
                ProgressionResult.WeightIncrease(
                    newWeight = newWeight,
                    reason = "Average RPE ${averageRpe} ≤ ${RPE_THRESHOLD_FOR_PROGRESSION}, all sets completed"
                )
            } else {
                val reason = when {
                    averageRpe > RPE_THRESHOLD_FOR_PROGRESSION -> "Average RPE ${averageRpe} too high"
                    !allSetsCompleted -> "Not all sets completed"
                    else -> "Unknown reason"
                }
                ProgressionResult.NoChange(reason)
            }
        }
        
        private fun calculateDoubleProgression(
            currentWeight: Double,
            averageRpe: Double,
            recentSets: List<WorkoutSet>,
            targetRpe: Double
        ): ProgressionResult {
            val maxReps = recentSets.maxOfOrNull { it.reps } ?: 0
            val targetMaxReps = getTargetMaxReps(targetRpe)
            
            return if (averageRpe <= 7.0 && maxReps >= targetMaxReps) {
                val newWeight = currentWeight + DOUBLE_PROGRESSION_INCREMENT
                ProgressionResult.WeightIncrease(
                    newWeight = newWeight,
                    reason = "Hit top of rep range with good RPE"
                )
            } else if (averageRpe <= RPE_THRESHOLD_FOR_PROGRESSION) {
                ProgressionResult.RepIncrease(
                    reason = "Focus on adding reps before weight"
                )
            } else {
                ProgressionResult.NoChange("RPE too high or reps too low")
            }
        }
        
        private fun calculateVolumeProgression(
            currentWeight: Double,
            averageRpe: Double,
            recentSets: List<WorkoutSet>,
            targetRpe: Double
        ): ProgressionResult {
            return if (averageRpe <= RPE_THRESHOLD_FOR_PROGRESSION) {
                ProgressionResult.RepIncrease(
                    reason = "Add ${VOLUME_PROGRESSION_REP_INCREMENT} rep(s) per set"
                )
            } else {
                ProgressionResult.NoChange("RPE too high for volume increase")
            }
        }
        
        private fun calculateBodyweightProgression(
            currentWeight: Double,
            averageRpe: Double,
            recentSets: List<WorkoutSet>
        ): ProgressionResult {
            val totalReps = recentSets.sumOf { it.reps }
            
            return if (averageRpe <= RPE_THRESHOLD_FOR_PROGRESSION) {
                if (totalReps >= 30) { // Arbitrary threshold for adding weight
                    val newWeight = currentWeight + 2.5 // Add weight for bodyweight exercises
                    ProgressionResult.WeightIncrease(
                        newWeight = newWeight,
                        reason = "High rep count achieved, add external weight"
                    )
                } else {
                    ProgressionResult.RepIncrease(
                        reason = "Focus on rep progression before adding weight"
                    )
                }
            } else {
                ProgressionResult.NoChange("RPE too high")
            }
        }
        
        private fun getDefaultStartingWeight(progressionType: String): Double {
            return when (progressionType.lowercase()) {
                "linear" -> 60.0 // kg - typical starting weight for compound movements
                "double" -> 15.0 // kg per hand for dumbbells
                "volume" -> 20.0 // kg for accessory work
                "bodyweight" -> 0.0 // no external weight initially
                else -> 20.0
            }
        }
        
        private fun getTargetMaxReps(targetRpe: Double): Int {
            // Estimate target max reps based on RPE
            // This is a simplified heuristic
            return when {
                targetRpe <= 7.0 -> 15 // Higher reps for lower RPE
                targetRpe <= 8.0 -> 12
                else -> 8
            }
        }
        
        fun calculateNextWeight(
            currentProgression: UserProgression,
            programExercise: com.athleticai.app.data.database.entities.ProgramExercise
        ): Double {
            // Simple calculation for suggested starting weight for next session
            val baseWeight = currentProgression.currentWeight
            val lastRpe = currentProgression.lastRpe ?: programExercise.rpeTarget
            
            return when (programExercise.progressionType.lowercase()) {
                "linear" -> {
                    if (lastRpe <= RPE_THRESHOLD_FOR_PROGRESSION) {
                        baseWeight + LINEAR_PROGRESSION_INCREMENT
                    } else {
                        baseWeight
                    }
                }
                "double" -> {
                    if (lastRpe <= 7.0) {
                        baseWeight + DOUBLE_PROGRESSION_INCREMENT
                    } else {
                        baseWeight
                    }
                }
                else -> baseWeight
            }
        }
    }
}

sealed class ProgressionResult {
    data class WeightIncrease(val newWeight: Double, val reason: String) : ProgressionResult()
    data class RepIncrease(val reason: String) : ProgressionResult()
    data class NoChange(val reason: String) : ProgressionResult()
}