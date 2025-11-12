package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.database.entities.*
import com.athleticai.app.data.repository.ProgramManagementRepository
import com.athleticai.app.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProgramDetailUiState(
    val isLoading: Boolean = true,
    val program: Program? = null,
    val enrollment: UserProgramEnrollment? = null,
    val days: List<ProgramDay> = emptyList(),
    val daysByWeek: Map<Int, List<ProgramDay>> = emptyMap(),
    val exercisesByDay: Map<String, List<ProgramDayExercise>> = emptyMap(),
    val exerciseDetails: Map<String, Exercise> = emptyMap(),
    val completedDays: Set<String> = emptySet(),
    val skippedDays: Set<String> = emptySet(),
    val currentDay: Int? = null,
    val currentWeek: Int? = null,
    val isEnrolled: Boolean = false,
    val error: String? = null
)

class ProgramDetailViewModel(
    private val programRepository: ProgramManagementRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgramDetailUiState())
    val uiState: StateFlow<ProgramDetailUiState> = _uiState.asStateFlow()
    
    fun loadProgram(programId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load program details
                val program = programRepository.getProgramById(programId)
                if (program == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Program not found"
                        ) 
                    }
                    return@launch
                }
                
                // Load program days
                val days = programRepository.getProgramDays(programId)
                val daysByWeek = days.groupBy { it.weekNumber }
                
                // Load exercises for each day
                val exercisesByDay = mutableMapOf<String, List<ProgramDayExercise>>()
                val allExerciseIds = mutableSetOf<String>()
                
                days.forEach { day ->
                    if (day.dayType == DayType.WORKOUT) {
                        val exercises = programRepository.getExercisesForDay(day.id)
                        if (exercises.isNotEmpty()) {
                            exercisesByDay[day.id] = exercises
                            allExerciseIds.addAll(exercises.map { it.exerciseId })
                        }
                    }
                }
                
                // Load exercise details
                val exerciseDetails = mutableMapOf<String, Exercise>()
                allExerciseIds.forEach { exerciseId ->
                    exerciseRepository.getExerciseById(exerciseId)?.let {
                        exerciseDetails[exerciseId] = it
                    }
                }
                
                // Check enrollment status
                val enrollment = programRepository.getActiveEnrollmentSync()
                val isEnrolled = enrollment?.programId == programId && enrollment?.status == EnrollmentStatus.ACTIVE
                
                // Get completion status if enrolled
                val completedDays = mutableSetOf<String>()
                val skippedDays = mutableSetOf<String>()
                var currentDay: Int? = null
                var currentWeek: Int? = null
                
                if (enrollment != null && isEnrolled) {
                    currentDay = enrollment.currentDay
                    currentWeek = ((currentDay - 1) / 7) + 1
                    
                    val completions = programRepository.getCompletionsForEnrollment(enrollment.id)
                    completions.forEach { completion ->
                        when (completion.status) {
                            CompletionStatus.COMPLETED -> completedDays.add(completion.programDayId)
                            CompletionStatus.SKIPPED -> skippedDays.add(completion.programDayId)
                            else -> {}
                        }
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        program = program,
                        enrollment = enrollment,
                        days = days,
                        daysByWeek = daysByWeek,
                        exercisesByDay = exercisesByDay,
                        exerciseDetails = exerciseDetails,
                        completedDays = completedDays,
                        skippedDays = skippedDays,
                        currentDay = currentDay,
                        currentWeek = currentWeek,
                        isEnrolled = isEnrolled
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load program: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    suspend fun enrollInProgram() {
        val program = _uiState.value.program ?: return
        
        viewModelScope.launch {
            try {
                programRepository.enrollInProgram(program.id)
                // Reload to refresh enrollment status
                loadProgram(program.id)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to enroll: ${e.message}")
                }
            }
        }
    }
    
    suspend fun skipCurrentDay() {
        val enrollment = _uiState.value.enrollment ?: return
        
        viewModelScope.launch {
            try {
                programRepository.skipCurrentDay(enrollment.id, "User requested skip")
                // Reload to refresh completion status
                _uiState.value.program?.let { loadProgram(it.id) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to skip day: ${e.message}")
                }
            }
        }
    }
}

class ProgramDetailViewModelFactory(
    private val programRepository: ProgramManagementRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgramDetailViewModel(programRepository, exerciseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}