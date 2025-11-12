package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.CustomProgramDao
import com.athleticai.app.data.database.dao.CustomWorkoutDao
import com.athleticai.app.data.database.dao.WorkoutExerciseDao
import com.athleticai.app.data.database.entities.CustomProgram
import com.athleticai.app.data.database.entities.CustomWorkout
import com.athleticai.app.data.database.entities.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CustomProgramRepository(
    private val customProgramDao: CustomProgramDao,
    private val customWorkoutDao: CustomWorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao
) {
    
    // Program Management
    fun getAllPrograms(): Flow<List<CustomProgram>> = customProgramDao.getAllPrograms()
    
    fun getActiveProgram(): Flow<CustomProgram?> = customProgramDao.getActiveProgram()
    
    suspend fun getProgramById(programId: String): CustomProgram? = 
        customProgramDao.getProgramById(programId)
    
    suspend fun createProgram(
        name: String,
        description: String = ""
    ): CustomProgram {
        val program = CustomProgram(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            isActive = false,
            createdDate = System.currentTimeMillis()
        )
        customProgramDao.insertProgram(program)
        return program
    }
    
    suspend fun activateProgram(programId: String) {
        customProgramDao.deactivateAllPrograms()
        customProgramDao.activateProgram(programId)
    }
    
    suspend fun updateProgram(program: CustomProgram) {
        customProgramDao.updateProgram(program)
    }
    
    suspend fun deleteProgram(program: CustomProgram) {
        customProgramDao.deleteProgram(program)
    }
    
    // Workout Management
    fun getWorkoutsForProgram(programId: String): Flow<List<CustomWorkout>> =
        customWorkoutDao.getWorkoutsForProgram(programId)
    
    suspend fun getWorkoutById(workoutId: String): CustomWorkout? =
        customWorkoutDao.getWorkoutById(workoutId)
    
    suspend fun createWorkout(
        programId: String,
        name: String,
        description: String = "",
        exercises: List<WorkoutExercise>
    ): CustomWorkout {
        val existingWorkouts = customWorkoutDao.getWorkoutsForProgram(programId)
        
        val workout = CustomWorkout(
            id = UUID.randomUUID().toString(),
            programId = programId,
            name = name,
            description = description,
            dayNumber = 1, // Will be updated based on existing workouts
            estimatedDurationMinutes = calculateEstimatedDuration(exercises),
            exerciseCount = exercises.size
        )
        
        // Insert workout first
        customWorkoutDao.insertWorkout(workout)
        
        // Insert exercises with correct workout ID
        if (exercises.isNotEmpty()) {
            val exercisesWithWorkoutId = exercises.map { exercise ->
                exercise.copy(workoutId = workout.id)
            }
            workoutExerciseDao.insertWorkoutExercises(exercisesWithWorkoutId)
        }
        
        // Update program's total workouts
        customProgramDao.updateTotalWorkouts(programId)
        
        return workout
    }
    
    suspend fun updateWorkout(workout: CustomWorkout) {
        customWorkoutDao.updateWorkout(workout)
    }
    
    suspend fun deleteWorkout(workoutId: String) {
        val workout = customWorkoutDao.getWorkoutById(workoutId)
        if (workout != null) {
            // This will cascade delete the exercises due to foreign key
            customWorkoutDao.deleteWorkout(workout)
            // Update program's total workouts
            customProgramDao.updateTotalWorkouts(workout.programId)
        }
    }
    
    suspend fun deleteWorkout(workout: CustomWorkout) {
        // This will cascade delete the exercises due to foreign key
        customWorkoutDao.deleteWorkout(workout)
        // Update program's total workouts
        customProgramDao.updateTotalWorkouts(workout.programId)
    }
    
    // Exercise Management
    fun getWorkoutExercises(workoutId: String): Flow<List<WorkoutExercise>> =
        workoutExerciseDao.getExercisesForWorkout(workoutId)
        
    fun getExercisesForWorkout(workoutId: String): Flow<List<WorkoutExercise>> =
        workoutExerciseDao.getExercisesForWorkout(workoutId)
    
    suspend fun addExerciseToWorkout(exercise: WorkoutExercise) {
        workoutExerciseDao.insertWorkoutExercise(exercise)
        customWorkoutDao.updateExerciseCount(exercise.workoutId)
        
        // Recalculate duration
        val exercises = workoutExerciseDao.getExercisesForWorkout(exercise.workoutId)
        // Note: This is a simplified approach - in a real app you'd collect the flow
        val estimatedDuration = 45 // Placeholder - would calculate based on exercises
        customWorkoutDao.updateEstimatedDuration(exercise.workoutId, estimatedDuration)
    }
    
    suspend fun updateExercise(exercise: WorkoutExercise) {
        workoutExerciseDao.updateWorkoutExercise(exercise)
    }
    
    suspend fun deleteExercise(exercise: WorkoutExercise) {
        workoutExerciseDao.deleteWorkoutExercise(exercise)
        customWorkoutDao.updateExerciseCount(exercise.workoutId)
    }
    
    suspend fun reorderExercises(workoutId: String, exerciseIds: List<String>) {
        exerciseIds.forEachIndexed { index, exerciseId ->
            workoutExerciseDao.updateExerciseOrder(exerciseId, index)
        }
    }
    
    // Utility Functions
    private fun calculateEstimatedDuration(exercises: List<WorkoutExercise>): Int {
        if (exercises.isEmpty()) return 0
        
        return exercises.sumOf { exercise ->
            // Base time per set (60 seconds) + rest time between sets
            val setTime = 60 // seconds per set
            val restTime = exercise.restSeconds
            val totalSetTime = exercise.targetSets * setTime
            val totalRestTime = (exercise.targetSets - 1) * restTime
            (totalSetTime + totalRestTime) / 60 // Convert to minutes
        }
    }
    
    // Batch operations for testing/cleanup
    suspend fun deleteAllCustomData() {
        workoutExerciseDao.deleteAll()
        customWorkoutDao.deleteAll()
        customProgramDao.deleteAll()
    }
}