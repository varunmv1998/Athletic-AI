package com.athleticai.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.ProgressionCalculator
import com.athleticai.app.data.database.entities.ProgramEnrollment
import com.athleticai.app.data.database.entities.ProgramTemplate
import com.athleticai.app.data.database.entities.ProgramExercise
import com.athleticai.app.data.database.entities.UserProgression
import com.athleticai.app.data.repository.ProgramRepository
import com.athleticai.app.data.repository.WorkoutRepository
import com.athleticai.app.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ProgramViewModel(
    private val programRepository: ProgramRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val TAG = "ProgramViewModel"
    
    private val _programState = MutableStateFlow(ProgramUiState())
    val programState: StateFlow<ProgramUiState> = _programState.asStateFlow()
    
    private val _enrollmentState = MutableStateFlow<ProgramEnrollment?>(null)
    val enrollmentState: StateFlow<ProgramEnrollment?> = _enrollmentState.asStateFlow()
    
    private val _todaysWorkout = MutableStateFlow<TodaysWorkout?>(null)
    val todaysWorkout: StateFlow<TodaysWorkout?> = _todaysWorkout.asStateFlow()
    
    init {
        Log.d(TAG, "ProgramViewModel initialized")
        initializeProgram()
        // Don't observe enrollment until program is initialized
    }
    
    private fun initializeProgram() {
        viewModelScope.launch {
            Log.d(TAG, "Starting program initialization...")
            _programState.value = _programState.value.copy(isLoading = true)
            
            try {
                // Initialize exercises first
                Log.d(TAG, "Initializing exercise data...")
                val exercisesInitialized = exerciseRepository.initializeExerciseData()
                if (!exercisesInitialized) {
                    Log.e(TAG, "Failed to initialize exercise data")
                    _programState.value = _programState.value.copy(
                        isLoading = false,
                        error = "Failed to load exercise database"
                    )
                    return@launch
                }
                Log.d(TAG, "Exercise data initialized successfully")
                
                // Initialize program data
                Log.d(TAG, "Initializing program data...")
                val initialized = programRepository.initializeProgramData()
                if (initialized) {
                    Log.d(TAG, "Program data initialized successfully")
                    _programState.value = _programState.value.copy(
                        isLoading = false,
                        isInitialized = true
                    )
                    // Now that program data is loaded, start observing enrollment
                    observeEnrollment()
                } else {
                    Log.e(TAG, "Failed to initialize program data")
                    _programState.value = _programState.value.copy(
                        isLoading = false,
                        error = "Failed to initialize program data"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during program initialization: ${e.message}", e)
                _programState.value = _programState.value.copy(
                    isLoading = false,
                    error = "Error initializing program: ${e.message}"
                )
            }
        }
    }
    
    private fun observeEnrollment() {
        viewModelScope.launch {
            programRepository.getActiveEnrollment().collect { enrollment ->
                Log.d(TAG, "Enrollment changed: ${enrollment?.let { "Day ${it.currentDay}, Active: ${it.isActive}" } ?: "null"}")
                _enrollmentState.value = enrollment
                if (enrollment != null) {
                    updateCurrentDayIfNeeded(enrollment)
                    loadTodaysWorkout()
                } else {
                    Log.d(TAG, "No active enrollment - need to enroll user")
                    // Auto-enroll for testing
                    enrollInProgram()
                }
            }
        }
    }
    
    private suspend fun updateCurrentDayIfNeeded(enrollment: ProgramEnrollment) {
        val daysSinceStart = ChronoUnit.DAYS.between(
            enrollment.startDate.toLocalDate(),
            LocalDateTime.now().toLocalDate()
        ).toInt() + 1 // Day 1 is the start date

        val calculatedDay = minOf(daysSinceStart, 90) // Cap at day 90

        // Only auto-advance if the calendar-derived day is ahead of currentDay.
        // Never decrement currentDay to avoid regressions within the same day.
        if (calculatedDay > enrollment.currentDay && calculatedDay <= 90) {
            programRepository.updateEnrollmentProgress(enrollment.enrollmentId, calculatedDay)
        }
    }
    
    fun enrollInProgram() {
        viewModelScope.launch {
            try {
                val enrollment = programRepository.enrollInProgram()
                _programState.value = _programState.value.copy(
                    isEnrolled = true,
                    enrollmentMessage = "Successfully enrolled in 90-Day Program!"
                )
            } catch (e: Exception) {
                _programState.value = _programState.value.copy(
                    error = "Failed to enroll: ${e.message}"
                )
            }
        }
    }
    
    private fun loadTodaysWorkout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading today's workout...")
                val enrollment = _enrollmentState.value
                if (enrollment == null || !enrollment.isActive) {
                    Log.d(TAG, "No active enrollment found")
                    _todaysWorkout.value = null
                    return@launch
                }
                
                Log.d(TAG, "Getting today's template for day ${enrollment.currentDay}...")
                val template = programRepository.getTodaysTemplate()
                if (template == null) {
                    // Rest day
                    Log.d(TAG, "Today is a rest day")
                    _todaysWorkout.value = TodaysWorkout.RestDay(
                        day = enrollment.currentDay,
                        phase = programRepository.calculateCurrentPhase(enrollment.currentDay),
                        week = programRepository.calculateWeekNumber(enrollment.currentDay)
                    )
                    return@launch
                }
                
                Log.d(TAG, "Today's template: ${template.templateKey} (${template.id})")
                Log.d(TAG, "Getting exercises for template...")
                val exercises = programRepository.getTodaysExercises()
                Log.d(TAG, "Retrieved ${exercises.size} exercises for today's workout")
                
                val exercisesWithProgression = exercises.map { programExercise ->
                    val progression = programRepository.getProgressionForExercise(programExercise.exerciseId)
                    val suggestedWeight = progression?.let { 
                        ProgressionCalculator.calculateNextWeight(it, programExercise)
                    } ?: getEstimatedStartingWeight(programExercise)
                    
                    ExerciseWithProgression(
                        programExercise = programExercise,
                        currentProgression = progression,
                        suggestedWeight = suggestedWeight
                    )
                }
                
                Log.d(TAG, "Created ${exercisesWithProgression.size} exercises with progression data")
                _todaysWorkout.value = TodaysWorkout.WorkoutDay(
                    template = template,
                    exercises = exercisesWithProgression,
                    day = enrollment.currentDay,
                    phase = programRepository.calculateCurrentPhase(enrollment.currentDay),
                    week = programRepository.calculateWeekNumber(enrollment.currentDay)
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load today's workout: ${e.message}", e)
                _programState.value = _programState.value.copy(
                    error = "Failed to load today's workout: ${e.message}"
                )
            }
        }
    }

    // Exercise substitution APIs
    fun swapExerciseForToday(originalExerciseId: String, substituteExerciseId: String) {
        viewModelScope.launch {
            val enrollment = _enrollmentState.value ?: return@launch
            programRepository.setDaySubstitution(enrollment.currentDay, originalExerciseId, substituteExerciseId)
            loadTodaysWorkout()
        }
    }

    fun resetExerciseForToday(originalExerciseId: String) {
        viewModelScope.launch {
            val enrollment = _enrollmentState.value ?: return@launch
            programRepository.resetDaySubstitution(enrollment.currentDay, originalExerciseId)
            loadTodaysWorkout()
        }
    }

    suspend fun hasSubstitutionForToday(originalExerciseId: String): Boolean {
        val enrollment = _enrollmentState.value ?: return false
        return programRepository.getDaySubstitution(enrollment.currentDay, originalExerciseId) != null
    }

    suspend fun getValidSubstitutes(originalExerciseId: String): List<com.athleticai.app.data.database.entities.Exercise> {
        val original = exerciseRepository.getExerciseById(originalExerciseId) ?: return emptyList()
        val muscles = original.primaryMuscles
        val results = mutableSetOf<com.athleticai.app.data.database.entities.Exercise>()
        for (m in muscles) {
            val list = exerciseRepository.getExercisesByMuscleSync(m)
            results.addAll(list)
        }
        return results.filter { it.id != originalExerciseId }
            .sortedBy { it.name }
    }

    suspend fun getOriginalExerciseId(templateId: String, orderIndex: Int): String? {
        val base = programRepository.getExercisesForTemplateSync(templateId)
        return base.getOrNull(orderIndex)?.exerciseId
    }
    
    fun advanceToNextDay() {
        viewModelScope.launch {
            val enrollment = _enrollmentState.value ?: return@launch
            
            if (enrollment.currentDay < 90) {
                val dayToClear = enrollment.currentDay
                val nextDay = enrollment.currentDay + 1
                // Clear substitutions for completed day
                programRepository.clearDaySubstitutions(dayToClear)
                programRepository.updateEnrollmentProgress(enrollment.enrollmentId, nextDay)
                
                _programState.value = _programState.value.copy(
                    enrollmentMessage = "Advanced to Day $nextDay"
                )
            } else {
                _programState.value = _programState.value.copy(
                    enrollmentMessage = "Program completed! Congratulations!"
                )
            }
        }
    }
    
    fun skipToday() {
        viewModelScope.launch {
            advanceToNextDay()
            _programState.value = _programState.value.copy(
                enrollmentMessage = "Day skipped"
            )
        }
    }
    
    fun getSubstitutionsForExercise(exerciseId: String) = 
        programRepository.getSubstitutionsForExercise(exerciseId)
    
    private fun getEstimatedStartingWeight(programExercise: ProgramExercise): Double {
        // Simple heuristic for starting weights based on exercise type and progression type
        return when (programExercise.progressionType.lowercase()) {
            "linear" -> when {
                programExercise.exerciseId.contains("bench") -> 60.0
                programExercise.exerciseId.contains("squat") -> 80.0
                programExercise.exerciseId.contains("deadlift") -> 100.0
                programExercise.exerciseId.contains("press") -> 40.0
                else -> 50.0
            }
            "double" -> 15.0 // Per hand for dumbbells
            "volume" -> 20.0
            "bodyweight" -> 0.0
            else -> 25.0
        }
    }
    
    fun clearError() {
        _programState.value = _programState.value.copy(error = null)
    }
    
    fun clearMessage() {
        _programState.value = _programState.value.copy(enrollmentMessage = null)
    }
    
    /**
     * Refresh program data for pull-to-refresh functionality
     */
    fun refreshProgramData() {
        Log.d(TAG, "Refreshing program data...")
        viewModelScope.launch {
            try {
                // Reload today's workout
                loadTodaysWorkout()
                
                // Clear any existing errors
                _programState.value = _programState.value.copy(error = null)
                
                Log.d(TAG, "Program data refreshed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing program data", e)
                _programState.value = _programState.value.copy(
                    error = "Failed to refresh program data"
                )
            }
        }
    }
}

data class ProgramUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val isEnrolled: Boolean = false,
    val error: String? = null,
    val enrollmentMessage: String? = null
)

sealed class TodaysWorkout {
    data class WorkoutDay(
        val template: ProgramTemplate,
        val exercises: List<ExerciseWithProgression>,
        val day: Int,
        val phase: String,
        val week: Int
    ) : TodaysWorkout()
    
    data class RestDay(
        val day: Int,
        val phase: String,
        val week: Int
    ) : TodaysWorkout()
}

data class ExerciseWithProgression(
    val programExercise: ProgramExercise,
    val currentProgression: UserProgression?,
    val suggestedWeight: Double
)
