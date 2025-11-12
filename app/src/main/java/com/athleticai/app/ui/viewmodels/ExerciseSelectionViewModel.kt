package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.repository.ExerciseSearchRepository
import com.athleticai.app.data.repository.ExerciseSearchFilters
import com.athleticai.app.data.database.entities.Exercise
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview

data class ExerciseSelectionUiState(
    val searchQuery: String = "",
    val selectedMuscleGroup: String? = null,
    val selectedEquipment: String? = null,
    val selectedCategory: String? = null,
    val searchResults: List<Exercise> = emptyList(),
    val recentlyUsedExercises: List<Exercise> = emptyList(),
    val selectedExercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationMessage: String? = null,
    val hasDuplicateExercises: Boolean = false
) {
    fun hasNoActiveFilters(): Boolean = 
        selectedMuscleGroup == null && selectedEquipment == null && selectedCategory == null
    
    fun hasActiveFilters(): Boolean = !hasNoActiveFilters()
    
    companion object {
        const val MAX_EXERCISES_PER_WORKOUT = 20
        const val MIN_EXERCISES_PER_WORKOUT = 1
    }
}

@OptIn(FlowPreview::class)
class ExerciseSelectionViewModel(
    private val exerciseSearchRepository: ExerciseSearchRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseSelectionUiState())
    val uiState: StateFlow<ExerciseSelectionUiState> = _uiState.asStateFlow()
    
    // Debounced search query
    private val _searchQuery = MutableStateFlow("")
    private val debouncedSearchQuery = _searchQuery
        .debounce(300) // 300ms debounce as specified
        .distinctUntilChanged()
    
    init {
        // Load initial data
        loadRecentlyUsedExercises()
        loadDefaultExercises()
        
        // Set up debounced search
        viewModelScope.launch {
            debouncedSearchQuery.collect { query ->
                if (query != _uiState.value.searchQuery) {
                    _uiState.value = _uiState.value.copy(searchQuery = query)
                    performSearch()
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            // Immediately show default results for empty query
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                isLoading = false
            )
            loadDefaultExercises()
        } else {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                isLoading = true
            )
        }
    }
    
    fun updateMuscleGroupFilter(muscleGroup: String?) {
        _uiState.value = _uiState.value.copy(
            selectedMuscleGroup = muscleGroup,
            isLoading = true
        )
        performSearch()
    }
    
    fun updateEquipmentFilter(equipment: String?) {
        _uiState.value = _uiState.value.copy(
            selectedEquipment = equipment,
            isLoading = true
        )
        performSearch()
    }
    
    fun updateCategoryFilter(category: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isLoading = true
        )
        performSearch()
    }
    
    fun addSelectedExercise(exercise: Exercise) {
        val currentSelected = _uiState.value.selectedExercises
        
        // Check for maximum exercises limit
        if (currentSelected.size >= ExerciseSelectionUiState.MAX_EXERCISES_PER_WORKOUT) {
            _uiState.value = _uiState.value.copy(
                validationMessage = "Maximum ${ExerciseSelectionUiState.MAX_EXERCISES_PER_WORKOUT} exercises allowed per workout"
            )
            return
        }
        
        // Check for duplicate exercise
        if (currentSelected.contains(exercise)) {
            _uiState.value = _uiState.value.copy(
                validationMessage = "Exercise '${exercise.name}' is already added",
                hasDuplicateExercises = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                selectedExercises = currentSelected + exercise,
                validationMessage = null,
                hasDuplicateExercises = false
            )
        }
    }
    
    fun removeSelectedExercise(exercise: Exercise) {
        _uiState.value = _uiState.value.copy(
            selectedExercises = _uiState.value.selectedExercises - exercise,
            validationMessage = null,
            hasDuplicateExercises = false
        )
    }
    
    fun clearSelectedExercises() {
        _uiState.value = _uiState.value.copy(selectedExercises = emptyList())
    }
    
    private fun loadRecentlyUsedExercises() {
        viewModelScope.launch {
            try {
                exerciseSearchRepository.getRecentlyUsedExercises(10).collect { usageHistory ->
                    val exerciseIds = usageHistory.map { it.exerciseId }
                    val exercises = exerciseSearchRepository.getExercisesWithUsageHistory(exerciseIds)
                        .map { it.exercise }
                    
                    _uiState.value = _uiState.value.copy(recentlyUsedExercises = exercises)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load recently used exercises"
                )
            }
        }
    }
    
    private fun loadDefaultExercises() {
        if (_uiState.value.searchQuery.isNotBlank() || _uiState.value.hasActiveFilters()) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Load a default set of popular exercises when no search/filter is active
                val defaultExercises = exerciseSearchRepository.searchExercisesWithFilters(
                    filters = ExerciseSearchFilters(),
                    limit = 50
                )
                
                _uiState.value = _uiState.value.copy(
                    searchResults = defaultExercises,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load exercises"
                )
            }
        }
    }
    
    private fun performSearch() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val filters = ExerciseSearchFilters(
                    query = currentState.searchQuery.takeIf { it.isNotBlank() },
                    muscleGroup = currentState.selectedMuscleGroup,
                    equipment = currentState.selectedEquipment,
                    category = currentState.selectedCategory
                )
                
                val results = if (currentState.searchQuery.isBlank() && !currentState.hasActiveFilters()) {
                    // Load default exercises
                    exerciseSearchRepository.searchExercisesWithFilters(
                        filters = ExerciseSearchFilters(),
                        limit = 50
                    )
                } else {
                    // Perform filtered search
                    exerciseSearchRepository.searchExercisesWithFilters(
                        filters = filters,
                        limit = 100
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false,
                    errorMessage = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Search failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedMuscleGroup = null,
            selectedEquipment = null,
            selectedCategory = null,
            isLoading = true
        )
        performSearch()
    }
    
    fun retrySearch() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        performSearch()
    }
    
    // Function to record exercise usage when exercises are selected for workout
    fun recordExerciseUsage(exercises: List<Exercise>) {
        viewModelScope.launch {
            exercises.forEach { exercise ->
                try {
                    exerciseSearchRepository.recordExerciseUsage(exercise.id)
                } catch (e: Exception) {
                    // Silently handle usage recording errors
                }
            }
        }
    }
}