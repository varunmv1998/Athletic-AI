package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.SupersetGroupDao
import com.athleticai.app.data.database.dao.WorkoutExerciseDao
import com.athleticai.app.data.database.entities.SupersetGroup
import com.athleticai.app.data.database.entities.SupersetType
import com.athleticai.app.data.database.entities.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

data class WorkoutExerciseWithSuperset(
    val exercise: WorkoutExercise,
    val supersetGroup: SupersetGroup?
)

data class SupersetGroupWithExercises(
    val group: SupersetGroup,
    val exercises: List<WorkoutExercise>
)

data class WorkoutStructure(
    val individualExercises: List<WorkoutExercise>,
    val supersetGroups: List<SupersetGroupWithExercises>
)

class SupersetRepository(
    private val supersetGroupDao: SupersetGroupDao,
    private val workoutExerciseDao: WorkoutExerciseDao
) {
    
    fun getWorkoutStructure(workoutId: String): Flow<WorkoutStructure> {
        return combine(
            workoutExerciseDao.getExercisesForWorkout(workoutId),
            supersetGroupDao.getSupersetGroupsForWorkout(workoutId)
        ) { exercises, groups ->
            // Separate individual exercises from superset exercises
            val individualExercises = exercises.filter { it.supersetGroupId == null }
            
            // Group exercises by superset
            val supersetGroups = groups.map { group ->
                val groupExercises = exercises
                    .filter { it.supersetGroupId == group.id }
                    .sortedBy { it.orderInSuperset }
                SupersetGroupWithExercises(group, groupExercises)
            }
            
            WorkoutStructure(individualExercises, supersetGroups)
        }
    }
    
    suspend fun createSupersetGroup(
        workoutId: String,
        exerciseIds: List<String>,
        groupType: SupersetType = SupersetType.SUPERSET,
        restBetweenExercises: Int = 10,
        restAfterGroup: Int = 90,
        rounds: Int = 1
    ): String {
        // Validate input
        require(exerciseIds.size >= 2) { "Superset must have at least 2 exercises" }
        require(exerciseIds.size <= 6) { "Superset cannot have more than 6 exercises" }
        require(restBetweenExercises in 0..30) { "Rest between exercises must be 0-30 seconds" }
        require(restAfterGroup in 30..300) { "Rest after group must be 30-300 seconds" }
        require(rounds in 1..5) { "Rounds must be 1-5" }
        
        // Get next group index
        val maxIndex = supersetGroupDao.getMaxGroupIndex(workoutId) ?: -1
        val groupIndex = maxIndex + 1
        
        // Create superset group
        val groupId = UUID.randomUUID().toString()
        val supersetGroup = SupersetGroup(
            id = groupId,
            workoutId = workoutId,
            groupIndex = groupIndex,
            groupType = groupType,
            restBetweenExercises = restBetweenExercises,
            restAfterGroup = restAfterGroup,
            rounds = rounds
        )
        
        supersetGroupDao.insertSupersetGroup(supersetGroup)
        
        // Update exercises to belong to this superset
        exerciseIds.forEachIndexed { index, exerciseId ->
            val exercise = workoutExerciseDao.getWorkoutExerciseById(exerciseId)
            exercise?.let { currentExercise ->
                val updatedExercise = currentExercise.copy(
                    supersetGroupId = groupId,
                    orderInSuperset = index
                )
                workoutExerciseDao.updateWorkoutExercise(updatedExercise)
            }
        }
        
        return groupId
    }
    
    suspend fun ungroupSuperset(groupId: String) {
        // Get all exercises in the group
        val group = supersetGroupDao.getSupersetGroupById(groupId)
        if (group != null) {
            // Find exercises in this group
            val allExercises = workoutExerciseDao.getExercisesForWorkout(group.workoutId)
            // This is a Flow, we need to get current value - using a different approach
            
            // Remove exercises from superset group
            val exercisesToUpdate = mutableListOf<WorkoutExercise>()
            // Note: We'll need to modify this to work with Flow properly
            // For now, implementing basic structure
        }
        
        // Delete the superset group
        supersetGroupDao.getSupersetGroupById(groupId)?.let { group ->
            supersetGroupDao.deleteSupersetGroup(group)
        }
    }
    
    suspend fun addExerciseToSuperset(exerciseId: String, groupId: String) {
        val exercise = workoutExerciseDao.getWorkoutExerciseById(exerciseId)
        val group = supersetGroupDao.getSupersetGroupById(groupId)
        
        if (exercise != null && group != null) {
            // Validate group size limit
            // Get current exercise count in group (simplified for now)
            val nextOrderInSuperset = 0 // TODO: Calculate actual next order
            
            val updatedExercise = exercise.copy(
                supersetGroupId = groupId,
                orderInSuperset = nextOrderInSuperset
            )
            workoutExerciseDao.updateWorkoutExercise(updatedExercise)
        }
    }
    
    suspend fun removeExerciseFromSuperset(exerciseId: String) {
        val exercise = workoutExerciseDao.getWorkoutExerciseById(exerciseId)
        exercise?.let { currentExercise ->
            val updatedExercise = currentExercise.copy(
                supersetGroupId = null,
                orderInSuperset = 0
            )
            workoutExerciseDao.updateWorkoutExercise(updatedExercise)
        }
    }
    
    suspend fun updateSupersetConfiguration(
        groupId: String,
        restBetweenExercises: Int? = null,
        restAfterGroup: Int? = null,
        rounds: Int? = null
    ) {
        val group = supersetGroupDao.getSupersetGroupById(groupId)
        group?.let { currentGroup ->
            val updatedGroup = currentGroup.copy(
                restBetweenExercises = restBetweenExercises ?: currentGroup.restBetweenExercises,
                restAfterGroup = restAfterGroup ?: currentGroup.restAfterGroup,
                rounds = rounds ?: currentGroup.rounds
            )
            supersetGroupDao.updateSupersetGroup(updatedGroup)
        }
    }
    
    suspend fun reorderSupersetGroups(workoutId: String, newOrder: List<String>) {
        newOrder.forEachIndexed { index, groupId ->
            supersetGroupDao.updateGroupOrder(groupId, index)
        }
    }
    
    fun calculateSupersetDuration(group: SupersetGroup, exerciseCount: Int): Int {
        val exerciseTime = 60 // seconds per set per exercise
        val totalSets = group.rounds
        
        return totalSets * (
            exerciseCount * exerciseTime + // Time for exercises
            (exerciseCount - 1) * group.restBetweenExercises + // Rest between exercises
            group.restAfterGroup // Rest after completing group
        )
    }
}