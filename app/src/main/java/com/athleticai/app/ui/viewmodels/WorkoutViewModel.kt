package com.athleticai.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.WorkoutNotificationService
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ProgramExercise
import com.athleticai.app.data.database.entities.ProgramTemplate
import com.athleticai.app.data.database.entities.WorkoutSession
import com.athleticai.app.data.database.entities.WorkoutSet
import com.athleticai.app.data.database.entities.CustomWorkout
import com.athleticai.app.data.database.entities.WorkoutExercise
import com.athleticai.app.data.database.entities.ProgramDay
import com.athleticai.app.data.database.entities.ProgramDayExercise
import com.athleticai.app.data.database.entities.UserProgramEnrollment
import com.athleticai.app.data.repository.ExerciseRepository
import com.athleticai.app.data.repository.WorkoutRepository
import com.athleticai.app.data.repository.AnalyticsRepository
import com.athleticai.app.data.repository.ProgramRepository
import com.athleticai.app.data.repository.WorkoutRoutineRepository
import com.athleticai.app.data.repository.ProgramManagementRepository
import com.athleticai.app.data.database.entities.RoutineExercise
import com.athleticai.app.data.database.entities.SupersetGroup
import com.athleticai.app.data.database.entities.SupersetType
import com.athleticai.app.data.repository.SupersetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.time.LocalDateTime
import java.util.UUID

class WorkoutViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val programRepository: ProgramRepository,
    private val routineRepository: WorkoutRoutineRepository? = null,
    private val supersetRepository: SupersetRepository? = null,
    private val programManagementRepository: ProgramManagementRepository? = null,
    private val context: Context? = null
) : ViewModel() {
    
    private val _workoutState = MutableStateFlow(WorkoutUiState())
    val workoutState: StateFlow<WorkoutUiState> = _workoutState.asStateFlow()
    
    // Add a timer job that can be cancelled
    private var restTimerJob: Job? = null
    
    init {
        // Check for active session on startup
        viewModelScope.launch {
            checkForActiveSession()
        }
    }
    
    fun startWorkoutSessionFromCustom(
        customWorkout: CustomWorkout, 
        workoutExercises: List<WorkoutExercise>
    ) {
        viewModelScope.launch {
            // Cancel any existing rest timer
            restTimerJob?.cancel()
            
            _workoutState.value = _workoutState.value.copy(isLoading = true)
            
            try {
                // Check if exercises list is empty
                if (workoutExercises.isEmpty()) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "No exercises provided for workout session"
                    )
                    return@launch
                }
                
                // Create workout session
                val sessionId = UUID.randomUUID().toString()
                val session = WorkoutSession(
                    sessionId = sessionId,
                    sessionName = customWorkout.name,
                    startTime = LocalDateTime.now(),
                    isCompleted = false
                )
                workoutRepository.insertSession(session)
                
                // Convert WorkoutExercise to ProgramExercise format for compatibility
                val programExercises = workoutExercises.map { workoutExercise ->
                    // Parse reps range from string (e.g., "8-12" or "12")
                    val repsRange = workoutExercise.targetReps.split("-")
                    val minReps = repsRange.firstOrNull()?.toIntOrNull() ?: 8
                    val maxReps = repsRange.lastOrNull()?.toIntOrNull() ?: minReps
                    
                    ProgramExercise(
                        id = workoutExercise.id,
                        templateId = customWorkout.id, // Use workout ID as template ID
                        exerciseId = workoutExercise.exerciseId,
                        orderIndex = workoutExercise.orderIndex,
                        sets = workoutExercise.targetSets,
                        repRangeMin = minReps,
                        repRangeMax = maxReps,
                        rpeTarget = workoutExercise.rpeTarget.toDouble(),
                        restSeconds = workoutExercise.restSeconds,
                        progressionType = "linear"
                    )
                }
                
                // Load exercise details
                val exercisesWithDetails = programExercises.map { programExercise ->
                    val exerciseInfo = exerciseRepository.getExerciseById(programExercise.exerciseId)
                    ExerciseWithDetails(
                        programExercise = programExercise,
                        exerciseInfo = exerciseInfo,
                        suggestedWeight = 0.0, // TODO: Calculate from progression
                        previousBest = null // TODO: Load from workout history
                    )
                }
                
                _workoutState.value = _workoutState.value.copy(
                    sessionId = sessionId,
                    sessionName = customWorkout.name,
                    isActive = true,
                    exercises = exercisesWithDetails,
                    currentExerciseIndex = 0,
                    currentSetNumber = 1,
                    completedSets = emptyMap(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _workoutState.value = _workoutState.value.copy(
                    isLoading = false,
                    error = "Failed to start custom workout session: ${e.message}"
                )
            }
        }
    }
    
    fun startWorkoutSessionFromRoutine(routineId: String) {
        viewModelScope.launch {
            // Cancel any existing rest timer
            restTimerJob?.cancel()
            
            _workoutState.value = _workoutState.value.copy(isLoading = true)
            
            try {
                if (routineRepository == null) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "Routine repository not available"
                    )
                    return@launch
                }
                
                // Get routine with exercises
                val routineWithExercises = routineRepository.getRoutineWithExercises(routineId)
                if (routineWithExercises == null) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "Routine not found"
                    )
                    return@launch
                }
                
                if (routineWithExercises.exercises.isEmpty()) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "Routine has no exercises"
                    )
                    return@launch
                }
                
                // Create workout session
                val sessionId = UUID.randomUUID().toString()
                val session = WorkoutSession(
                    sessionId = sessionId,
                    sessionName = routineWithExercises.routine.name,
                    startTime = LocalDateTime.now(),
                    isCompleted = false
                )
                workoutRepository.insertSession(session)
                
                // Convert RoutineExercise to ProgramExercise format for compatibility
                val programExercises = routineWithExercises.exercises.sortedBy { it.orderIndex }.map { routineExercise ->
                    // Parse default sets from the routine exercise
                    val defaultSets = if (routineExercise.defaultSets.isNotEmpty()) {
                        routineExercise.defaultSets.size
                    } else {
                        3 // Default to 3 sets
                    }
                    
                    // Parse reps range from first set or use default
                    val firstSet = routineExercise.defaultSets.firstOrNull()
                    val repsRange = firstSet?.targetReps?.split("-") ?: listOf("8", "10")
                    val minReps = repsRange.firstOrNull()?.toIntOrNull() ?: 8
                    val maxReps = repsRange.lastOrNull()?.toIntOrNull() ?: minReps
                    
                    ProgramExercise(
                        id = routineExercise.id,
                        templateId = routineId,
                        exerciseId = routineExercise.exerciseId,
                        orderIndex = routineExercise.orderIndex,
                        sets = defaultSets,
                        repRangeMin = minReps,
                        repRangeMax = maxReps,
                        rpeTarget = 7.0, // Default RPE target
                        restSeconds = routineExercise.restSeconds,
                        progressionType = "linear"
                    )
                }
                
                // Load exercise details
                val exercisesWithDetails = programExercises.map { programExercise ->
                    val exerciseInfo = exerciseRepository.getExerciseById(programExercise.exerciseId)
                    ExerciseWithDetails(
                        programExercise = programExercise,
                        exerciseInfo = exerciseInfo,
                        suggestedWeight = 0.0, // TODO: Calculate from progression
                        previousBest = null // TODO: Load from workout history
                    )
                }
                
                // Update last performed timestamp for the routine
                routineRepository.updateLastPerformed(routineId)
                
                _workoutState.value = _workoutState.value.copy(
                    sessionId = sessionId,
                    sessionName = routineWithExercises.routine.name,
                    isActive = true,
                    exercises = exercisesWithDetails,
                    currentExerciseIndex = 0,
                    currentSetNumber = 1,
                    completedSets = emptyMap(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _workoutState.value = _workoutState.value.copy(
                    isLoading = false,
                    error = "Failed to start routine workout: ${e.message}"
                )
            }
        }
    }
    
    fun startWorkoutSession(template: ProgramTemplate, exercises: List<ProgramExercise>) {
        viewModelScope.launch {
            // Cancel any existing rest timer
            restTimerJob?.cancel()
            
            _workoutState.value = _workoutState.value.copy(isLoading = true)
            
            try {
                // Debug: Check if exercises list is empty
                if (exercises.isEmpty()) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "No exercises provided for workout session"
                    )
                    return@launch
                }
                
                // Create workout session
                val sessionId = UUID.randomUUID().toString()
                val session = WorkoutSession(
                    sessionId = sessionId,
                    sessionName = template.templateKey,
                    startTime = LocalDateTime.now(),
                    isCompleted = false
                )
                workoutRepository.insertSession(session)
                
                // Load exercise details
                val exercisesWithDetails = exercises.map { programExercise ->
                    val exerciseInfo = exerciseRepository.getExerciseById(programExercise.exerciseId)
                    ExerciseWithDetails(
                        programExercise = programExercise,
                        exerciseInfo = exerciseInfo,
                        suggestedWeight = 0.0, // TODO: Calculate from progression
                        previousBest = null // TODO: Load from workout history
                    )
                }
                
                _workoutState.value = _workoutState.value.copy(
                    sessionId = sessionId,
                    sessionName = template.templateKey,
                    isActive = true,
                    exercises = exercisesWithDetails,
                    currentExerciseIndex = 0,
                    currentSetNumber = 1,
                    completedSets = emptyMap(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _workoutState.value = _workoutState.value.copy(
                    isLoading = false,
                    error = "Failed to start workout session: ${e.message}"
                )
            }
        }
    }
    
    fun startWorkoutSessionFromProgramDay(
        programDay: ProgramDay,
        exercises: List<ProgramDayExercise>,
        enrollment: UserProgramEnrollment? = null
    ) {
        viewModelScope.launch {
            // Cancel any existing rest timer
            restTimerJob?.cancel()
            
            _workoutState.value = _workoutState.value.copy(isLoading = true)
            
            try {
                if (programManagementRepository == null) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "Program management repository not available"
                    )
                    return@launch
                }
                
                // Check if exercises list is empty
                if (exercises.isEmpty()) {
                    _workoutState.value = _workoutState.value.copy(
                        isLoading = false,
                        error = "No exercises provided for program day"
                    )
                    return@launch
                }
                
                // Create workout session with program information
                val sessionId = UUID.randomUUID().toString()
                val session = WorkoutSession(
                    sessionId = sessionId,
                    sessionName = programDay.name,
                    startTime = LocalDateTime.now(),
                    isCompleted = false,
                    programDayId = programDay.id,
                    programEnrollmentId = enrollment?.id,
                    sessionType = "PROGRAM"
                )
                workoutRepository.insertSession(session)
                
                // Convert ProgramDayExercise to ProgramExercise format for compatibility
                val programExercises = exercises.sortedBy { it.orderIndex }.map { dayExercise ->
                    ProgramExercise(
                        id = dayExercise.id,
                        templateId = programDay.id,
                        exerciseId = dayExercise.exerciseId,
                        orderIndex = dayExercise.orderIndex,
                        sets = dayExercise.sets,
                        repRangeMin = parseRepsMin(dayExercise.reps),
                        repRangeMax = parseRepsMax(dayExercise.reps),
                        rpeTarget = dayExercise.targetRPE?.toDouble() ?: 7.0,
                        restSeconds = dayExercise.restSeconds,
                        progressionType = "linear"
                    )
                }
                
                // Load exercise details
                val exercisesWithDetails = programExercises.map { programExercise ->
                    val exerciseInfo = exerciseRepository.getExerciseById(programExercise.exerciseId)
                    ExerciseWithDetails(
                        programExercise = programExercise,
                        exerciseInfo = exerciseInfo,
                        suggestedWeight = 0.0, // TODO: Calculate from progression
                        previousBest = null // TODO: Load from workout history
                    )
                }
                
                _workoutState.value = _workoutState.value.copy(
                    sessionId = sessionId,
                    sessionName = programDay.name,
                    isActive = true,
                    exercises = exercisesWithDetails,
                    currentExerciseIndex = 0,
                    currentSetNumber = 1,
                    completedSets = emptyMap(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _workoutState.value = _workoutState.value.copy(
                    isLoading = false,
                    error = "Failed to start program workout: ${e.message}"
                )
            }
        }
    }
    
    // Helper functions to parse reps from string format (e.g., "8-12", "30-60 seconds")
    private fun parseRepsMin(reps: String): Int {
        val cleanReps = reps.replace(Regex("[^0-9-]"), "")
        val parts = cleanReps.split("-")
        return parts.firstOrNull()?.toIntOrNull() ?: 8
    }
    
    private fun parseRepsMax(reps: String): Int {
        val cleanReps = reps.replace(Regex("[^0-9-]"), "")
        val parts = cleanReps.split("-")
        return if (parts.size > 1) {
            parts.lastOrNull()?.toIntOrNull() ?: parseRepsMin(reps)
        } else {
            parseRepsMin(reps)
        }
    }
    
    fun logSet(exerciseId: String, weight: Double, reps: Int, rpe: Double) {
        viewModelScope.launch {
            val state = _workoutState.value
            val sessionId = state.sessionId ?: return@launch
            
            try {
                // Create WorkoutSet entity
                val setId = UUID.randomUUID().toString()
                val workoutSet = WorkoutSet(
                    setId = setId,
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = state.currentSetNumber,
                    weight = weight,
                    reps = reps,
                    rpe = rpe,
                    timestamp = LocalDateTime.now()
                )
                
                // Save to database
                workoutRepository.insertSet(workoutSet)
                
                // Update local state
                val currentSets = state.completedSets[exerciseId] ?: emptyList()
                val updatedSets = currentSets + SetLog(
                    setNumber = state.currentSetNumber,
                    weight = weight,
                    reps = reps,
                    rpe = rpe
                )
                
                val exercise = state.exercises.getOrNull(state.currentExerciseIndex)
                val targetSets = exercise?.programExercise?.sets ?: 3
                
                if (updatedSets.size >= targetSets) {
                    // All sets completed for this exercise, move to next
                    _workoutState.value = state.copy(
                        completedSets = state.completedSets + (exerciseId to updatedSets),
                        currentExerciseIndex = state.currentExerciseIndex + 1,
                        currentSetNumber = 1
                    )
                } else {
                    // Start rest timer for next set
                    val restSeconds = exercise?.programExercise?.restSeconds ?: 90
                    _workoutState.value = state.copy(
                        completedSets = state.completedSets + (exerciseId to updatedSets),
                        currentSetNumber = state.currentSetNumber + 1,
                        isResting = true,
                        restTimeRemaining = restSeconds
                    )
                    startRestTimer(restSeconds)
                }
                
                // Auto-save after each set
                autoSaveSession()
                
            } catch (e: Exception) {
                _workoutState.value = _workoutState.value.copy(
                    error = "Failed to log set: ${e.message}"
                )
            }
        }
    }
    
    private fun startRestTimer(seconds: Int) {
        // Cancel any existing rest timer
        restTimerJob?.cancel()
        
        // Start background notification service for rest timer
        context?.let { ctx ->
            WorkoutNotificationService.startRestTimer(ctx, seconds)
        }
        
        restTimerJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _workoutState.value = _workoutState.value.copy(restTimeRemaining = remaining)
                delay(1000)
            }
            _workoutState.value = _workoutState.value.copy(
                isResting = false,
                restTimeRemaining = 0
            )
        }
    }
    
    fun skipRest() {
        // Cancel the rest timer
        restTimerJob?.cancel()
        
        // Stop notification service
        context?.let { ctx ->
            WorkoutNotificationService.stopRestTimer(ctx)
        }
        
        _workoutState.value = _workoutState.value.copy(
            isResting = false,
            restTimeRemaining = 0
        )
    }
    
    fun nextExercise() {
        // Cancel any existing rest timer
        restTimerJob?.cancel()
        
        val state = _workoutState.value
        if (state.currentExerciseIndex < state.exercises.size - 1) {
            _workoutState.value = state.copy(
                currentExerciseIndex = state.currentExerciseIndex + 1,
                currentSetNumber = 1,
                isResting = false,
                restTimeRemaining = 0
            )
        }
    }
    
    fun previousExercise() {
        // Cancel any existing rest timer
        restTimerJob?.cancel()
        
        val state = _workoutState.value
        if (state.currentExerciseIndex > 0) {
            _workoutState.value = state.copy(
                currentExerciseIndex = state.currentExerciseIndex - 1,
                currentSetNumber = 1,
                isResting = false,
                restTimeRemaining = 0
            )
        }
    }
    
    fun pauseWorkout() {
        // Cancel rest timer if running
        restTimerJob?.cancel()
        _workoutState.value = _workoutState.value.copy(
            isPaused = true,
            isResting = false
        )
        // Auto-save session state
        autoSaveSession()
    }
    
    fun resumeWorkout() {
        _workoutState.value = _workoutState.value.copy(isPaused = false)
        // Auto-save session state
        autoSaveSession()
    }
    
    private fun autoSaveSession() {
        viewModelScope.launch {
            val state = _workoutState.value
            val sessionId = state.sessionId ?: return@launch
            
            try {
                // Update session with current state to persist across app kills
                val session = WorkoutSession(
                    sessionId = sessionId,
                    sessionName = state.sessionName ?: "Workout",
                    startTime = LocalDateTime.now(), // TODO: Store actual start time
                    isCompleted = false,
                    // Store additional state in future enhancement
                    endTime = null
                )
                workoutRepository.updateSession(session)
            } catch (e: Exception) {
                // Silently handle auto-save errors
            }
        }
    }
    
    fun finishWorkout(onWorkoutCompleted: () -> Unit) {
        // Cancel any existing rest timer
        restTimerJob?.cancel()
        
        viewModelScope.launch {
            val state = _workoutState.value
            val sessionId = state.sessionId ?: return@launch
            
            try {
                // Compute PRs for this session before marking complete
                analyticsRepository.computeAndStorePRsForSession(sessionId)
                // Update progression per exercise (last used weight and average RPE)
                runCatching {
                    val sets = workoutRepository.getSetsForSessionSync(sessionId)
                    val byExercise = sets.groupBy { it.exerciseId }
                    byExercise.forEach { (exerciseId, exSets) ->
                        val avgRpe = exSets.map { it.rpe }.average()
                        val lastWeight = exSets.maxByOrNull { it.timestamp }?.weight ?: exSets.lastOrNull()?.weight ?: 0.0
                        if (lastWeight > 0) {
                            programRepository.updateProgression(exerciseId, lastWeight, avgRpe)
                        }
                    }
                }
                // Update session as completed
                val completedSession = WorkoutSession(
                    sessionId = sessionId,
                    sessionName = state.sessionName ?: "Workout",
                    startTime = LocalDateTime.now(), // TODO: Store actual start time
                    endTime = LocalDateTime.now(),
                    isCompleted = true
                )
                workoutRepository.updateSession(completedSession)
                
                // Reset state
                _workoutState.value = WorkoutUiState()
                
                // Notify completion
                onWorkoutCompleted()
                
            } catch (e: Exception) {
                _workoutState.value = _workoutState.value.copy(
                    error = "Failed to finish workout: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _workoutState.value = _workoutState.value.copy(error = null)
    }
    
    fun getCurrentExercise(): ExerciseWithDetails? {
        val state = _workoutState.value
        return state.exercises.getOrNull(state.currentExerciseIndex)
    }
    
    fun getCompletedSetsForExercise(exerciseId: String): List<SetLog> {
        return _workoutState.value.completedSets[exerciseId] ?: emptyList()
    }
    
    private suspend fun checkForActiveSession() {
        try {
            val activeSession = workoutRepository.getActiveSession()
            if (activeSession != null) {
                // For now, let's not auto-restore active sessions since we don't have
                // complete state restoration. Instead, we'll just clean up the orphaned session
                // to prevent it from interfering with new workouts.
                val updatedSession = activeSession.copy(isCompleted = true, endTime = LocalDateTime.now())
                workoutRepository.updateSession(updatedSession)
            }
        } catch (e: Exception) {
            // Handle errors silently for auto-recovery
        }
    }
    
    // Superset Execution Functions
    fun moveToNextInSuperset() {
        val state = _workoutState.value
        val currentGroup = state.currentSupersetGroup
        
        if (currentGroup != null && state.isInSuperset) {
            val groupExercises = state.exercises.filter { it.programExercise.id.startsWith("superset_${currentGroup.id}") }
            val nextExerciseInGroup = state.currentExerciseInSuperset + 1
            
            if (nextExerciseInGroup < groupExercises.size) {
                // Move to next exercise in superset with minimal rest
                _workoutState.value = state.copy(
                    currentExerciseInSuperset = nextExerciseInGroup,
                    restTimeRemaining = currentGroup.restBetweenExercises,
                    isResting = true
                )
                startRestTimer(currentGroup.restBetweenExercises)
            } else {
                // Completed all exercises in superset round
                val nextRound = state.currentSupersetRound + 1
                if (nextRound <= currentGroup.rounds) {
                    // Start next round
                    _workoutState.value = state.copy(
                        currentExerciseInSuperset = 0,
                        currentSupersetRound = nextRound,
                        restTimeRemaining = currentGroup.restAfterGroup,
                        isResting = true
                    )
                    startRestTimer(currentGroup.restAfterGroup)
                } else {
                    // Superset completely finished, move to next exercise/group
                    nextExercise()
                }
            }
        }
    }
    
    fun startSupersetExecution(group: SupersetGroup) {
        _workoutState.value = _workoutState.value.copy(
            currentSupersetGroup = group,
            currentExerciseInSuperset = 0,
            currentSupersetRound = 1,
            isInSuperset = true
        )
    }
    
    fun exitSuperset() {
        _workoutState.value = _workoutState.value.copy(
            currentSupersetGroup = null,
            currentExerciseInSuperset = 0,
            currentSupersetRound = 1,
            isInSuperset = false
        )
    }
    
    fun getSupersetProgress(): SupersetProgress? {
        val state = _workoutState.value
        val group = state.currentSupersetGroup
        
        return if (group != null && state.isInSuperset) {
            val groupExercises = state.exercises.filter { it.programExercise.id.startsWith("superset_${group.id}") }
            SupersetProgress(
                currentExercise = state.currentExerciseInSuperset + 1,
                totalExercises = groupExercises.size,
                currentRound = state.currentSupersetRound,
                totalRounds = group.rounds,
                groupType = group.groupType
            )
        } else null
    }
}

data class SupersetProgress(
    val currentExercise: Int,
    val totalExercises: Int,
    val currentRound: Int,
    val totalRounds: Int,
    val groupType: SupersetType
)

data class WorkoutUiState(
    val sessionId: String? = null,
    val sessionName: String? = null,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val exercises: List<ExerciseWithDetails> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSetNumber: Int = 1,
    val completedSets: Map<String, List<SetLog>> = emptyMap(),
    val restTimeRemaining: Int = 0,
    val isResting: Boolean = false,
    val currentSupersetGroup: SupersetGroup? = null,
    val currentExerciseInSuperset: Int = 0,
    val currentSupersetRound: Int = 1,
    val isInSuperset: Boolean = false
)

data class ExerciseWithDetails(
    val programExercise: ProgramExercise,
    val exerciseInfo: Exercise?, // Full exercise data with instructions
    val suggestedWeight: Double,
    val previousBest: WorkoutSet?
)

data class SetLog(
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double
)
