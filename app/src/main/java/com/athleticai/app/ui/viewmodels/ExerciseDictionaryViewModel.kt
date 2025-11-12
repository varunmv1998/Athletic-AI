package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.repository.ExerciseSearchRepository
import com.athleticai.app.data.repository.ExerciseSearchFilters
import com.athleticai.app.data.database.entities.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

data class ExerciseDictionaryUiState(
    val searchQuery: String = "",
    val selectedBodyPart: String? = null,
    val selectedEquipment: String? = null,
    val selectedMuscleGroup: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalExerciseCount: Int = 0
) {
    fun hasNoActiveFilters(): Boolean = 
        selectedBodyPart == null && selectedEquipment == null && selectedMuscleGroup == null
}

@OptIn(FlowPreview::class)
class ExerciseDictionaryViewModel(
    private val exerciseSearchRepository: ExerciseSearchRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseDictionaryUiState())
    val uiState: StateFlow<ExerciseDictionaryUiState> = _uiState.asStateFlow()
    
    // Debounced search query
    private val _searchQuery = MutableStateFlow("")
    private val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
    
    init {
        // Set up debounced search
        viewModelScope.launch {
            debouncedSearchQuery.collect { query ->
                _uiState.value = _uiState.value.copy(
                    searchQuery = query,
                    isLoading = true
                )
                performSearch()
            }
        }
        
        // Load all exercises initially (trigger with empty search)
        _searchQuery.value = ""
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateBodyPartFilter(bodyPart: String?) {
        _uiState.value = _uiState.value.copy(
            selectedBodyPart = bodyPart,
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
    
    fun updateMuscleGroupFilter(muscleGroup: String?) {
        _uiState.value = _uiState.value.copy(
            selectedMuscleGroup = muscleGroup,
            isLoading = true
        )
        performSearch()
    }
    
    private fun performSearch() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val filters = ExerciseSearchFilters(
                    query = currentState.searchQuery.takeIf { it.isNotBlank() },
                    muscleGroup = currentState.selectedMuscleGroup,
                    equipment = currentState.selectedEquipment,
                    category = currentState.selectedBodyPart
                )
                
                val searchResults = if (filters.query == null && 
                                      filters.muscleGroup == null && 
                                      filters.equipment == null && 
                                      filters.category == null) {
                    // No filters, load all exercises
                    exerciseSearchRepository.getAllExercisesList()
                } else {
                    // Apply filters
                    exerciseSearchRepository.searchExercisesWithFilters(
                        filters = filters,
                        limit = 500
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    exercises = searchResults,
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
            selectedBodyPart = null,
            selectedEquipment = null,
            selectedMuscleGroup = null
        )
        _searchQuery.value = ""
    }
}