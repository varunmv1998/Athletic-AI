package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.database.entities.*
import com.athleticai.app.data.repository.ProgramManagementRepository
import com.athleticai.app.data.repository.ProgramStatistics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing the Program feature UI state and operations
 */
class ProgramManagementViewModel(
    private val repository: ProgramManagementRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(ProgramManagementUiState())
    val uiState: StateFlow<ProgramManagementUiState> = _uiState.asStateFlow()
    
    // View mode state (Programs vs Routines)
    private val _viewMode = MutableStateFlow(WorkoutViewMode.ROUTINES)
    val viewMode: StateFlow<WorkoutViewMode> = _viewMode.asStateFlow()
    
    // Program lists
    val allPrograms = repository.getAllPrograms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val preBuiltPrograms = repository.getPreBuiltPrograms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val customPrograms = repository.getCustomPrograms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Active enrollment
    val activeEnrollment = repository.getActiveEnrollmentWithProgram()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Current program state
    val currentProgramState = activeEnrollment.map { enrollment ->
        enrollment?.let {
            val currentDay = repository.getCurrentProgramDay(it.enrollment.id)
            val nextDay = repository.getNextProgramDay(it.enrollment.id)
            val progress = repository.getProgramProgress(it.enrollment.id).first()
            
            CurrentProgramState(
                enrollment = it.enrollment,
                program = it.program,
                currentProgramDay = currentDay,
                completionPercentage = progress?.progressPercentage ?: 0f,
                daysRemaining = calculateDaysRemaining(it.enrollment, it.program),
                nextWorkoutDay = nextDay,
                streak = progress?.currentStreak ?: 0
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    init {
        // Load initial data
        loadPrograms()
    }
    
    // ====== View Mode Management ======
    
    fun toggleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            WorkoutViewMode.ROUTINES -> WorkoutViewMode.PROGRAMS
            WorkoutViewMode.PROGRAMS -> WorkoutViewMode.ROUTINES
        }
    }
    
    fun setViewMode(mode: WorkoutViewMode) {
        _viewMode.value = mode
    }
    
    // ====== Program Discovery ======
    
    private fun loadPrograms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Programs will be loaded via the StateFlow declarations above
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load programs: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun filterPrograms(goal: ProgramGoal? = null, level: ExperienceLevel? = null) {
        _uiState.update { 
            it.copy(
                selectedGoalFilter = goal,
                selectedLevelFilter = level
            )
        }
    }
    
    fun searchPrograms(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            loadPrograms()
        } else {
            viewModelScope.launch {
                repository.searchPrograms(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
            }
        }
    }
    
    fun selectProgram(programId: String) {
        viewModelScope.launch {
            val program = repository.getProgramById(programId)
            _uiState.update { it.copy(selectedProgram = program) }
        }
    }
    
    // ====== Enrollment Management ======
    
    fun enrollInProgram(programId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnrolling = true, error = null) }
            
            repository.enrollInProgram(programId)
                .onSuccess { enrollment ->
                    _uiState.update { 
                        it.copy(
                            isEnrolling = false,
                            enrollmentSuccess = true
                        )
                    }
                    // Switch view to Programs after enrollment
                    setViewMode(WorkoutViewMode.PROGRAMS)
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isEnrolling = false,
                            error = error.message
                        )
                    }
                }
        }
    }
    
    fun unenrollFromCurrentProgram() {
        viewModelScope.launch {
            activeEnrollment.value?.let { enrollment ->
                repository.unenrollFromProgram(enrollment.enrollment.id)
                    .onSuccess {
                        _uiState.update { 
                            it.copy(enrollmentSuccess = false)
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(error = error.message)
                        }
                    }
            }
        }
    }
    
    fun pauseProgram() {
        viewModelScope.launch {
            activeEnrollment.value?.let { enrollment ->
                repository.pauseProgram(enrollment.enrollment.id)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }
    
    fun resumeProgram() {
        viewModelScope.launch {
            activeEnrollment.value?.let { enrollment ->
                repository.resumeProgram(enrollment.enrollment.id)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }
    
    // ====== Day Management ======
    
    fun startCurrentDay() {
        viewModelScope.launch {
            activeEnrollment.value?.let { enrollment ->
                repository.startProgramDay(enrollment.enrollment.id)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }
    
    fun completeCurrentDay(workoutSessionId: String? = null, notes: String? = null) {
        viewModelScope.launch {
            activeEnrollment.value?.let { enrollment ->
                repository.completeCurrentDay(enrollment.enrollment.id, workoutSessionId, notes)
                    .onSuccess {
                        // Move to next day automatically
                        repository.startProgramDay(enrollment.enrollment.id)
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }
    
    fun skipCurrentDay(reason: String? = null) {
        viewModelScope.launch {
            activeEnrollment.value?.let { enrollment ->
                repository.skipCurrentDay(enrollment.enrollment.id, reason)
                    .onSuccess {
                        // Move to next day
                        repository.startProgramDay(enrollment.enrollment.id)
                        _uiState.update { 
                            it.copy(showSkipConfirmation = false)
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }
    
    fun showSkipDayDialog() {
        _uiState.update { it.copy(showSkipConfirmation = true) }
    }
    
    fun hideSkipDayDialog() {
        _uiState.update { it.copy(showSkipConfirmation = false) }
    }
    
    // ====== Custom Program Creation ======
    
    fun startProgramCreation() {
        _uiState.update { 
            it.copy(
                isCreatingProgram = true,
                programCreationState = ProgramCreationState()
            )
        }
    }
    
    fun updateProgramCreationBasics(
        name: String? = null,
        description: String? = null,
        goal: ProgramGoal? = null,
        level: ExperienceLevel? = null,
        durationWeeks: Int? = null,
        workoutsPerWeek: Int? = null,
        equipment: List<String>? = null
    ) {
        _uiState.update { state ->
            val creation = state.programCreationState ?: ProgramCreationState()
            state.copy(
                programCreationState = creation.copy(
                    name = name ?: creation.name,
                    description = description ?: creation.description,
                    goal = goal ?: creation.goal,
                    experienceLevel = level ?: creation.experienceLevel,
                    durationWeeks = durationWeeks ?: creation.durationWeeks,
                    workoutsPerWeek = workoutsPerWeek ?: creation.workoutsPerWeek,
                    equipmentRequired = equipment ?: creation.equipmentRequired
                )
            )
        }
    }
    
    fun addProgramDay(
        dayNumber: Int,
        weekNumber: Int,
        dayType: DayType,
        routineId: String? = null,
        name: String,
        description: String? = null
    ) {
        _uiState.update { state ->
            val creation = state.programCreationState ?: return@update state
            val newDay = ProgramDay(
                id = UUID.randomUUID().toString(),
                programId = "", // Will be set when program is created
                dayNumber = dayNumber,
                weekNumber = weekNumber,
                dayOfWeek = (dayNumber - 1) % 7 + 1,
                dayType = dayType,
                routineId = routineId,
                name = name,
                description = description
            )
            state.copy(
                programCreationState = creation.copy(
                    programDays = creation.programDays + newDay
                )
            )
        }
    }
    
    fun removeProgramDay(dayNumber: Int) {
        _uiState.update { state ->
            val creation = state.programCreationState ?: return@update state
            state.copy(
                programCreationState = creation.copy(
                    programDays = creation.programDays.filter { it.dayNumber != dayNumber }
                )
            )
        }
    }
    
    fun saveCustomProgram() {
        viewModelScope.launch {
            val creation = _uiState.value.programCreationState ?: return@launch
            
            if (!creation.isValid()) {
                _uiState.update { 
                    it.copy(error = "Please fill in all required fields")
                }
                return@launch
            }
            
            _uiState.update { it.copy(isSavingProgram = true, error = null) }
            
            repository.createCustomProgram(
                name = creation.name,
                description = creation.description,
                goal = creation.goal,
                experienceLevel = creation.experienceLevel,
                durationWeeks = creation.durationWeeks,
                workoutsPerWeek = creation.workoutsPerWeek,
                equipmentRequired = creation.equipmentRequired,
                programDays = creation.programDays
            )
                .onSuccess { program ->
                    _uiState.update { 
                        it.copy(
                            isSavingProgram = false,
                            isCreatingProgram = false,
                            programCreationState = null,
                            creationSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isSavingProgram = false,
                            error = error.message
                        )
                    }
                }
        }
    }
    
    fun cancelProgramCreation() {
        _uiState.update { 
            it.copy(
                isCreatingProgram = false,
                programCreationState = null
            )
        }
    }
    
    // ====== Statistics ======
    
    fun loadProgramStatistics() {
        viewModelScope.launch {
            val stats = repository.getProgramStatistics()
            _uiState.update { it.copy(statistics = stats) }
        }
    }
    
    // ====== Utility Functions ======
    
    private fun calculateDaysRemaining(enrollment: UserProgramEnrollment, program: Program): Int {
        val totalDays = program.durationWeeks * 7
        return (totalDays - enrollment.currentDay).coerceAtLeast(0)
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun getFilteredPrograms(): List<Program> {
        val allProgramsList = when (_uiState.value.activeTab) {
            ProgramTab.ALL -> allPrograms.value
            ProgramTab.PREBUILT -> preBuiltPrograms.value
            ProgramTab.CUSTOM -> customPrograms.value
        }
        
        return allProgramsList.filter { program ->
            val matchesGoal = _uiState.value.selectedGoalFilter?.let { 
                program.goal == it 
            } ?: true
            
            val matchesLevel = _uiState.value.selectedLevelFilter?.let { 
                program.experienceLevel == it 
            } ?: true
            
            val matchesSearch = _uiState.value.searchQuery.let { query ->
                query.isBlank() || 
                program.name.contains(query, ignoreCase = true) ||
                program.description.contains(query, ignoreCase = true)
            }
            
            matchesGoal && matchesLevel && matchesSearch
        }
    }
    
    fun setActiveTab(tab: ProgramTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }
}

/**
 * View mode for workout screen
 */
enum class WorkoutViewMode {
    ROUTINES,
    PROGRAMS
}

/**
 * Program tabs
 */
enum class ProgramTab {
    ALL,
    PREBUILT,
    CUSTOM
}

/**
 * UI state for Program Management
 */
data class ProgramManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedProgram: Program? = null,
    val searchQuery: String = "",
    val searchResults: List<Program> = emptyList(),
    val selectedGoalFilter: ProgramGoal? = null,
    val selectedLevelFilter: ExperienceLevel? = null,
    val activeTab: ProgramTab = ProgramTab.ALL,
    val isEnrolling: Boolean = false,
    val enrollmentSuccess: Boolean = false,
    val showSkipConfirmation: Boolean = false,
    val isCreatingProgram: Boolean = false,
    val isSavingProgram: Boolean = false,
    val creationSuccess: Boolean = false,
    val programCreationState: ProgramCreationState? = null,
    val statistics: ProgramStatistics? = null
)

/**
 * State for custom program creation
 */
data class ProgramCreationState(
    val name: String = "",
    val description: String = "",
    val goal: ProgramGoal = ProgramGoal.GENERAL_FITNESS,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val durationWeeks: Int = 4,
    val workoutsPerWeek: Int = 3,
    val equipmentRequired: List<String> = emptyList(),
    val programDays: List<ProgramDay> = emptyList(),
    val currentStep: Int = 0
) {
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               description.isNotBlank() && 
               durationWeeks in 4..26 &&
               workoutsPerWeek in 1..7 &&
               programDays.isNotEmpty()
    }
}