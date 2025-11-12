package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.database.entities.CustomProgram
import com.athleticai.app.data.database.entities.CustomWorkout
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.WorkoutExercise
import com.athleticai.app.data.repository.CustomProgramRepository
import com.athleticai.app.data.repository.ExerciseSearchRepository
import com.athleticai.app.data.repository.SupersetRepository
import com.athleticai.app.data.database.entities.SupersetGroup
import com.athleticai.app.data.database.entities.SupersetType
import com.athleticai.app.ui.screens.ExerciseConfiguration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class CustomWorkoutUiState(
    val programs: List<CustomProgram> = emptyList(),
    val activeProgram: CustomProgram? = null,
    val currentWorkouts: List<CustomWorkout> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationMessage: String? = null,
    val durationWarning: String? = null,
    val isCreatingProgram: Boolean = false,
    val isCreatingWorkout: Boolean = false,
    val selectedExercisesForSuperset: Set<String> = emptySet(),
    val showSupersetDialog: Boolean = false,
    val currentWorkoutId: String? = null
) {
    companion object {
        const val MAX_WORKOUTS_PER_PROGRAM = 50
        const val MAX_DURATION_MINUTES = 180
        const val WARNING_DURATION_MINUTES = 120
    }
}

class CustomWorkoutViewModel(
    private val customProgramRepository: CustomProgramRepository,
    private val exerciseSearchRepository: ExerciseSearchRepository,
    private val supersetRepository: SupersetRepository? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CustomWorkoutUiState())
    val uiState: StateFlow<CustomWorkoutUiState> = _uiState.asStateFlow()
    
    init {
        loadPrograms()
    }
    
    private fun loadPrograms() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                combine(
                    customProgramRepository.getAllPrograms(),
                    customProgramRepository.getActiveProgram()
                ) { programs, activeProgram ->
                    _uiState.value = _uiState.value.copy(
                        programs = programs,
                        activeProgram = activeProgram,
                        isLoading = false
                    )
                    
                    // Load workouts for active program
                    activeProgram?.let { program ->
                        loadWorkoutsForProgram(program.id)
                    }
                }.collect()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load programs: ${e.message}"
                )
            }
        }
    }
    
    private fun loadWorkoutsForProgram(programId: String) {
        viewModelScope.launch {
            customProgramRepository.getWorkoutsForProgram(programId).collect { workouts ->
                _uiState.value = _uiState.value.copy(currentWorkouts = workouts)
            }
        }
    }
    
    fun createProgram(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreatingProgram = true)
                customProgramRepository.createProgram(name, description)
                _uiState.value = _uiState.value.copy(isCreatingProgram = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingProgram = false,
                    errorMessage = "Failed to create program: ${e.message}"
                )
            }
        }
    }
    
    fun activateProgram(program: CustomProgram) {
        viewModelScope.launch {
            try {
                customProgramRepository.activateProgram(program.id)
                loadWorkoutsForProgram(program.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to activate program: ${e.message}"
                )
            }
        }
    }
    
    fun deleteProgram(program: CustomProgram) {
        viewModelScope.launch {
            try {
                customProgramRepository.deleteProgram(program)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete program: ${e.message}"
                )
            }
        }
    }
    
    fun createWorkout(
        programId: String,
        name: String,
        description: String = "",
        exercises: List<Exercise>,
        configurations: Map<String, ExerciseConfiguration>
    ) {
        viewModelScope.launch {
            try {
                // Check if program has reached maximum workouts
                val currentWorkoutCount = _uiState.value.currentWorkouts.size
                if (currentWorkoutCount >= CustomWorkoutUiState.MAX_WORKOUTS_PER_PROGRAM) {
                    _uiState.value = _uiState.value.copy(
                        validationMessage = "Maximum ${CustomWorkoutUiState.MAX_WORKOUTS_PER_PROGRAM} workouts allowed per program"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(isCreatingWorkout = true)
                
                // Convert configurations to WorkoutExercise entities
                val workoutExercises = exercises.mapIndexed { index, exercise ->
                    val config = configurations[exercise.id] ?: ExerciseConfiguration(exercise = exercise)
                    
                    WorkoutExercise(
                        id = UUID.randomUUID().toString(),
                        workoutId = "", // Will be set by repository
                        exerciseId = exercise.id,
                        orderIndex = index + 1,
                        targetSets = config.sets,
                        targetReps = "${config.minReps}-${config.maxReps}",
                        rpeTarget = config.rpe,
                        restSeconds = config.restSeconds
                    )
                }
                
                // Calculate estimated duration and show warning if needed
                val estimatedDuration = calculateWorkoutDuration(workoutExercises)
                val durationWarning = when {
                    estimatedDuration > CustomWorkoutUiState.MAX_DURATION_MINUTES -> 
                        "Warning: Workout duration (${estimatedDuration} min) exceeds recommended maximum of ${CustomWorkoutUiState.MAX_DURATION_MINUTES} minutes"
                    estimatedDuration > CustomWorkoutUiState.WARNING_DURATION_MINUTES ->
                        "Note: This is a long workout (${estimatedDuration} min). Consider splitting into multiple sessions."
                    else -> null
                }
                
                _uiState.value = _uiState.value.copy(durationWarning = durationWarning)
                
                val workout = customProgramRepository.createWorkout(
                    programId = programId,
                    name = name,
                    description = description,
                    exercises = workoutExercises
                )
                
                // Record exercise usage for recommendations
                recordExerciseUsage(exercises)
                
                _uiState.value = _uiState.value.copy(
                    isCreatingWorkout = false,
                    validationMessage = null,
                    durationWarning = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingWorkout = false,
                    errorMessage = "Failed to create workout: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateWorkoutDuration(exercises: List<WorkoutExercise>): Int {
        // Base time per set: 60 seconds
        // Total = (sets × 60s + (sets-1) × rest) for all exercises
        return exercises.sumOf { exercise ->
            val setTime = 60 // seconds per set
            val totalSetTime = exercise.targetSets * setTime
            val totalRestTime = (exercise.targetSets - 1) * exercise.restSeconds
            (totalSetTime + totalRestTime) / 60 // Convert to minutes
        }
    }
    
    fun deleteWorkout(workout: CustomWorkout) {
        viewModelScope.launch {
            try {
                customProgramRepository.deleteWorkout(workout.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete workout: ${e.message}"
                )
            }
        }
    }
    
    private fun recordExerciseUsage(exercises: List<Exercise>) {
        viewModelScope.launch {
            try {
                exercises.forEach { exercise ->
                    exerciseSearchRepository.recordExerciseUsage(exercise.id)
                }
            } catch (e: Exception) {
                // Silently handle usage recording errors
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearValidationMessages() {
        _uiState.value = _uiState.value.copy(
            validationMessage = null,
            durationWarning = null
        )
    }
    
    fun getWorkoutById(workoutId: String): Flow<CustomWorkout?> {
        return flow {
            emit(customProgramRepository.getWorkoutById(workoutId))
        }
    }
    
    fun getWorkoutExercises(workoutId: String): Flow<List<WorkoutExercise>> {
        return customProgramRepository.getWorkoutExercises(workoutId)
    }
    
    // Superset Management Functions
    fun selectExerciseForSuperset(exerciseId: String) {
        val currentSelected = _uiState.value.selectedExercisesForSuperset
        _uiState.value = _uiState.value.copy(
            selectedExercisesForSuperset = currentSelected + exerciseId
        )
    }
    
    fun deselectExerciseForSuperset(exerciseId: String) {
        val currentSelected = _uiState.value.selectedExercisesForSuperset
        _uiState.value = _uiState.value.copy(
            selectedExercisesForSuperset = currentSelected - exerciseId
        )
    }
    
    fun clearSupersetSelection() {
        _uiState.value = _uiState.value.copy(
            selectedExercisesForSuperset = emptySet()
        )
    }
    
    fun showSupersetCreationDialog() {
        val selectedCount = _uiState.value.selectedExercisesForSuperset.size
        if (selectedCount >= 2) {
            _uiState.value = _uiState.value.copy(showSupersetDialog = true)
        } else {
            _uiState.value = _uiState.value.copy(
                validationMessage = "Select at least 2 exercises to create a superset"
            )
        }
    }
    
    fun hideSupersetCreationDialog() {
        _uiState.value = _uiState.value.copy(showSupersetDialog = false)
    }
    
    fun createSuperset(
        workoutId: String,
        groupType: SupersetType = SupersetType.SUPERSET,
        restBetweenExercises: Int = 10,
        restAfterGroup: Int = 90,
        rounds: Int = 1
    ) {
        viewModelScope.launch {
            try {
                val selectedExercises = _uiState.value.selectedExercisesForSuperset.toList()
                
                if (supersetRepository != null && selectedExercises.size >= 2) {
                    supersetRepository.createSupersetGroup(
                        workoutId = workoutId,
                        exerciseIds = selectedExercises,
                        groupType = groupType,
                        restBetweenExercises = restBetweenExercises,
                        restAfterGroup = restAfterGroup,
                        rounds = rounds
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        selectedExercisesForSuperset = emptySet(),
                        showSupersetDialog = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to create superset: Invalid selection or repository unavailable"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to create superset: ${e.message}",
                    showSupersetDialog = false
                )
            }
        }
    }
    
    fun ungroupSuperset(groupId: String) {
        viewModelScope.launch {
            try {
                supersetRepository?.ungroupSuperset(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to ungroup superset: ${e.message}"
                )
            }
        }
    }
    
    fun updateSupersetConfiguration(
        groupId: String,
        restBetweenExercises: Int? = null,
        restAfterGroup: Int? = null,
        rounds: Int? = null
    ) {
        viewModelScope.launch {
            try {
                supersetRepository?.updateSupersetConfiguration(
                    groupId = groupId,
                    restBetweenExercises = restBetweenExercises,
                    restAfterGroup = restAfterGroup,
                    rounds = rounds
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update superset configuration: ${e.message}"
                )
            }
        }
    }
    
    fun getWorkoutStructure(workoutId: String) = 
        supersetRepository?.getWorkoutStructure(workoutId) ?: flow { 
            emit(com.athleticai.app.data.repository.WorkoutStructure(emptyList(), emptyList()))
        }
    
    fun reorderExercises(workoutId: String, exercises: List<Exercise>) {
        viewModelScope.launch {
            try {
                customProgramRepository.reorderExercises(workoutId, exercises.map { it.id })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to reorder exercises: ${e.message}"
                )
            }
        }
    }
    
    fun reorderSupersetGroups(workoutId: String, groupIds: List<String>) {
        viewModelScope.launch {
            try {
                supersetRepository?.reorderSupersetGroups(workoutId, groupIds)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to reorder superset groups: ${e.message}"
                )
            }
        }
    }
}