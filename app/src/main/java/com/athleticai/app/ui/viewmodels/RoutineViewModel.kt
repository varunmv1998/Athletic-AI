package com.athleticai.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.repository.WorkoutRoutineRepository
import com.athleticai.app.data.repository.ExerciseRepository
import com.athleticai.app.data.database.entities.Folder
import com.athleticai.app.data.database.entities.WorkoutRoutine
import com.athleticai.app.data.database.entities.WorkoutRoutineWithExercises
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ExerciseSet
import com.athleticai.app.data.database.entities.RoutineExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

data class RoutineUiState(
    val folders: List<Folder> = emptyList(),
    val routines: List<WorkoutRoutine> = emptyList(),
    val uncategorizedRoutines: List<WorkoutRoutine> = emptyList(),
    val recentRoutines: List<WorkoutRoutine> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFolder: Folder? = null,
    val isCreatingRoutine: Boolean = false,
    val searchQuery: String = "",
    val isRoutineListExpanded: Boolean = false,
    val routineListLimit: Int = 5,
    val showOptionsMenu: Boolean = false,
    val selectedRoutineForOptions: WorkoutRoutine? = null
)

data class CreateRoutineUiState(
    val routineName: String = "",
    val selectedFolder: Folder? = null,
    val selectedExercises: List<Exercise> = emptyList(),
    val exerciseSets: Map<String, List<ExerciseSet>> = emptyMap(),
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showExercisePicker: Boolean = false,
    val isValidRoutine: Boolean = false,
    val isSuccessfullyCreated: Boolean = false
)

data class EditRoutineUiState(
    val routineId: String = "",
    val originalRoutine: WorkoutRoutineWithExercises? = null,
    val editedName: String = "",
    val editedExercises: List<EditableExercise> = emptyList(),
    val editedNotes: String = "",
    val selectedFolder: Folder? = null,
    val hasChanges: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showExercisePicker: Boolean = false,
    val isValidRoutine: Boolean = false,
    val isSuccessfullyEdited: Boolean = false
)

data class EditableExercise(
    val id: String,
    val exercise: Exercise,
    val sets: List<ExerciseSet>,
    val restSeconds: Int = 90,
    val orderIndex: Int = 0
)

class RoutineViewModel(
    private val routineRepository: WorkoutRoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()
    
    private val _createRoutineState = MutableStateFlow(CreateRoutineUiState())
    val createRoutineState: StateFlow<CreateRoutineUiState> = _createRoutineState.asStateFlow()
    
    private val _editRoutineState = MutableStateFlow(EditRoutineUiState())
    val editRoutineState: StateFlow<EditRoutineUiState> = _editRoutineState.asStateFlow()
    
    init {
        loadRoutinesData()
    }
    
    private fun loadRoutinesData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    routineRepository.getAllFolders(),
                    routineRepository.getAllRoutines(),
                    routineRepository.getUncategorizedRoutines(),
                    routineRepository.getRecentRoutines(5)
                ) { folders, routines, uncategorized, recent ->
                    RoutineUiState(
                        folders = folders,
                        routines = routines,
                        uncategorizedRoutines = uncategorized,
                        recentRoutines = recent,
                        isLoading = false,
                        selectedFolder = _uiState.value.selectedFolder,
                        searchQuery = _uiState.value.searchQuery
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load routines: ${e.message}"
                )
            }
        }
    }
    
    // Folder management
    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                routineRepository.createFolder(name.trim())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to create folder: ${e.message}"
                )
            }
        }
    }
    
    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            try {
                routineRepository.deleteFolder(folderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete folder: ${e.message}"
                )
            }
        }
    }
    
    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            try {
                routineRepository.renameFolder(folderId, newName.trim())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to rename folder: ${e.message}"
                )
            }
        }
    }
    
    // Routine management
    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            try {
                routineRepository.deleteRoutine(routineId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete routine: ${e.message}"
                )
            }
        }
    }
    
    fun duplicateRoutine(routineId: String, newName: String) {
        viewModelScope.launch {
            try {
                routineRepository.duplicateRoutine(routineId, newName.trim())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to duplicate routine: ${e.message}"
                )
            }
        }
    }
    
    fun selectFolder(folder: Folder?) {
        _uiState.value = _uiState.value.copy(selectedFolder = folder)
    }
    
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Create routine flow
    fun startCreatingRoutine() {
        _createRoutineState.value = CreateRoutineUiState()
        _uiState.value = _uiState.value.copy(isCreatingRoutine = true)
    }
    
    fun cancelCreatingRoutine() {
        _createRoutineState.value = CreateRoutineUiState()
        _uiState.value = _uiState.value.copy(isCreatingRoutine = false)
    }
    
    fun clearCreateRoutineState() {
        _createRoutineState.value = CreateRoutineUiState()
        _uiState.value = _uiState.value.copy(isCreatingRoutine = false)
    }
    
    fun setRoutineName(name: String) {
        val currentState = _createRoutineState.value
        _createRoutineState.value = currentState.copy(
            routineName = name,
            isValidRoutine = validateRoutine(name, currentState.selectedExercises)
        )
    }
    
    fun setRoutineFolder(folder: Folder?) {
        _createRoutineState.value = _createRoutineState.value.copy(selectedFolder = folder)
    }
    
    fun setRoutineNotes(notes: String) {
        _createRoutineState.value = _createRoutineState.value.copy(notes = notes)
    }
    
    fun showExercisePickerForCreate() {
        _createRoutineState.value = _createRoutineState.value.copy(showExercisePicker = true)
    }
    
    fun hideExercisePickerForCreate() {
        _createRoutineState.value = _createRoutineState.value.copy(showExercisePicker = false)
    }
    
    fun addExerciseToRoutine(exercise: Exercise) {
        val currentState = _createRoutineState.value
        val updatedExercises = currentState.selectedExercises + exercise
        val defaultSets = listOf(
            ExerciseSet(setNumber = 1, targetReps = "8-10"),
            ExerciseSet(setNumber = 2, targetReps = "8-10"),
            ExerciseSet(setNumber = 3, targetReps = "8-10")
        )
        val updatedSets = currentState.exerciseSets + (exercise.id to defaultSets)
        
        _createRoutineState.value = currentState.copy(
            selectedExercises = updatedExercises,
            exerciseSets = updatedSets,
            isValidRoutine = validateRoutine(currentState.routineName, updatedExercises)
        )
    }
    
    fun removeExerciseFromRoutine(exercise: Exercise) {
        val currentState = _createRoutineState.value
        val updatedExercises = currentState.selectedExercises - exercise
        val updatedSets = currentState.exerciseSets - exercise.id
        
        _createRoutineState.value = currentState.copy(
            selectedExercises = updatedExercises,
            exerciseSets = updatedSets,
            isValidRoutine = validateRoutine(currentState.routineName, updatedExercises)
        )
    }
    
    fun updateExerciseSetsForCreate(exerciseId: String, sets: List<ExerciseSet>) {
        val currentState = _createRoutineState.value
        val updatedSets = currentState.exerciseSets.toMutableMap()
        updatedSets[exerciseId] = sets
        
        _createRoutineState.value = currentState.copy(exerciseSets = updatedSets)
    }
    
    fun reorderExercises(exercises: List<Exercise>) {
        _createRoutineState.value = _createRoutineState.value.copy(selectedExercises = exercises)
    }
    
    fun createRoutine() {
        val currentState = _createRoutineState.value
        if (!currentState.isValidRoutine) return
        
        viewModelScope.launch {
            _createRoutineState.value = currentState.copy(isLoading = true)
            
            try {
                val exercisesWithSets = currentState.selectedExercises.map { exercise ->
                    exercise.id to (currentState.exerciseSets[exercise.id] ?: emptyList())
                }
                
                routineRepository.createRoutine(
                    name = currentState.routineName.trim(),
                    folderId = currentState.selectedFolder?.id,
                    exercises = exercisesWithSets,
                    notes = currentState.notes.trim()
                )
                
                // Set success flag instead of immediately resetting
                _createRoutineState.value = currentState.copy(
                    isLoading = false,
                    isSuccessfullyCreated = true
                )
                _uiState.value = _uiState.value.copy(isCreatingRoutine = false)
                
            } catch (e: Exception) {
                _createRoutineState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Failed to create routine: ${e.message}"
                )
            }
        }
    }
    
    suspend fun getRoutineWithExercises(routineId: String): WorkoutRoutineWithExercises? {
        return try {
            routineRepository.getRoutineWithExercises(routineId)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to load routine: ${e.message}"
            )
            null
        }
    }
    
    suspend fun getExerciseById(exerciseId: String): Exercise? {
        return try {
            exerciseRepository.getExerciseById(exerciseId)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun validateRoutine(name: String, exercises: List<Exercise>): Boolean {
        return name.trim().isNotEmpty() && exercises.isNotEmpty()
    }
    
    // Edit routine flow
    fun startEditingRoutine(routineId: String) {
        viewModelScope.launch {
            _editRoutineState.value = EditRoutineUiState(isLoading = true, routineId = routineId)
            
            try {
                val routineWithExercises = routineRepository.getRoutineWithExercises(routineId)
                if (routineWithExercises != null) {
                    val editableExercises = routineWithExercises.exercises.map { routineExercise ->
                        val exercise = exerciseRepository.getExerciseById(routineExercise.exerciseId)
                        if (exercise != null) {
                            EditableExercise(
                                id = routineExercise.id,
                                exercise = exercise,
                                sets = routineExercise.defaultSets,
                                restSeconds = routineExercise.restSeconds,
                                orderIndex = routineExercise.orderIndex
                            )
                        } else null
                    }.filterNotNull().sortedBy { it.orderIndex }
                    
                    // Find the folder if routine has one
                    val folder = routineWithExercises.routine.folderId?.let { folderId ->
                        _uiState.value.folders.find { it.id == folderId }
                    }
                    
                    _editRoutineState.value = _editRoutineState.value.copy(
                        isLoading = false,
                        originalRoutine = routineWithExercises,
                        editedName = routineWithExercises.routine.name,
                        editedNotes = routineWithExercises.routine.notes,
                        editedExercises = editableExercises,
                        selectedFolder = folder,
                        isValidRoutine = validateEditedRoutine(routineWithExercises.routine.name, editableExercises)
                    )
                } else {
                    _editRoutineState.value = _editRoutineState.value.copy(
                        isLoading = false,
                        errorMessage = "Routine not found"
                    )
                }
            } catch (e: Exception) {
                _editRoutineState.value = _editRoutineState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load routine: ${e.message}"
                )
            }
        }
    }
    
    fun clearEditRoutineState() {
        _editRoutineState.value = EditRoutineUiState()
    }
    
    fun updateEditedName(name: String) {
        val currentState = _editRoutineState.value
        val hasChanges = hasEditChanges(currentState.copy(editedName = name))
        _editRoutineState.value = currentState.copy(
            editedName = name,
            hasChanges = hasChanges,
            isValidRoutine = validateEditedRoutine(name, currentState.editedExercises)
        )
    }
    
    fun updateEditedNotes(notes: String) {
        val currentState = _editRoutineState.value
        val hasChanges = hasEditChanges(currentState.copy(editedNotes = notes))
        _editRoutineState.value = currentState.copy(
            editedNotes = notes,
            hasChanges = hasChanges
        )
    }
    
    fun setEditRoutineFolder(folder: Folder?) {
        val currentState = _editRoutineState.value
        val hasChanges = hasEditChanges(currentState.copy(selectedFolder = folder))
        _editRoutineState.value = currentState.copy(
            selectedFolder = folder,
            hasChanges = hasChanges
        )
    }
    
    fun addExerciseToEdit(exercise: Exercise) {
        val currentState = _editRoutineState.value
        val defaultSets = listOf(
            ExerciseSet(setNumber = 1, targetReps = "8-10"),
            ExerciseSet(setNumber = 2, targetReps = "8-10"),
            ExerciseSet(setNumber = 3, targetReps = "8-10")
        )
        
        val newEditableExercise = EditableExercise(
            id = java.util.UUID.randomUUID().toString(),
            exercise = exercise,
            sets = defaultSets,
            restSeconds = 90,
            orderIndex = currentState.editedExercises.size
        )
        
        val updatedExercises = currentState.editedExercises + newEditableExercise
        val hasChanges = hasEditChanges(currentState.copy(editedExercises = updatedExercises))
        
        _editRoutineState.value = currentState.copy(
            editedExercises = updatedExercises,
            hasChanges = hasChanges,
            isValidRoutine = validateEditedRoutine(currentState.editedName, updatedExercises),
            showExercisePicker = false
        )
    }
    
    fun removeExerciseFromEdit(index: Int) {
        val currentState = _editRoutineState.value
        val updatedExercises = currentState.editedExercises.toMutableList().apply {
            removeAt(index)
        }
        val hasChanges = hasEditChanges(currentState.copy(editedExercises = updatedExercises))
        
        _editRoutineState.value = currentState.copy(
            editedExercises = updatedExercises,
            hasChanges = hasChanges,
            isValidRoutine = validateEditedRoutine(currentState.editedName, updatedExercises)
        )
    }
    
    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val currentState = _editRoutineState.value
        val exercises = currentState.editedExercises.toMutableList()
        val exercise = exercises.removeAt(fromIndex)
        exercises.add(toIndex, exercise)
        
        // Update order indices
        val updatedExercises = exercises.mapIndexed { index, ex ->
            ex.copy(orderIndex = index)
        }
        
        val hasChanges = hasEditChanges(currentState.copy(editedExercises = updatedExercises))
        
        _editRoutineState.value = currentState.copy(
            editedExercises = updatedExercises,
            hasChanges = hasChanges
        )
    }
    
    fun updateExerciseSetsForEdit(exerciseId: String, sets: List<ExerciseSet>) {
        val currentState = _editRoutineState.value
        val updatedExercises = currentState.editedExercises.map { ex ->
            if (ex.id == exerciseId) {
                ex.copy(sets = sets)
            } else {
                ex
            }
        }
        val hasChanges = hasEditChanges(currentState.copy(editedExercises = updatedExercises))
        
        _editRoutineState.value = currentState.copy(
            editedExercises = updatedExercises,
            hasChanges = hasChanges
        )
    }
    
    fun showExercisePickerForEdit() {
        _editRoutineState.value = _editRoutineState.value.copy(showExercisePicker = true)
    }
    
    fun hideExercisePickerForEdit() {
        _editRoutineState.value = _editRoutineState.value.copy(showExercisePicker = false)
    }
    
    fun clearEditError() {
        _editRoutineState.value = _editRoutineState.value.copy(errorMessage = null)
    }
    
    fun saveEditedRoutine() {
        val currentState = _editRoutineState.value
        if (!currentState.isValidRoutine || !currentState.hasChanges) return
        
        viewModelScope.launch {
            _editRoutineState.value = currentState.copy(isLoading = true)
            
            try {
                val originalRoutine = currentState.originalRoutine?.routine
                if (originalRoutine != null) {
                    // Update routine basic info
                    val updatedRoutine = originalRoutine.copy(
                        name = currentState.editedName.trim(),
                        notes = currentState.editedNotes.trim(),
                        folderId = currentState.selectedFolder?.id
                    )
                    
                    routineRepository.updateRoutine(updatedRoutine)
                    
                    // Update exercises - simplified approach: delete all and recreate
                    // First, delete all existing routine exercises
                    currentState.originalRoutine.exercises.forEach { routineExercise ->
                        routineRepository.removeExerciseFromRoutine(routineExercise.id)
                    }
                    
                    // Then add all edited exercises
                    currentState.editedExercises.forEachIndexed { index, editableExercise ->
                        routineRepository.addExerciseToRoutine(
                            routineId = originalRoutine.id,
                            exerciseId = editableExercise.exercise.id,
                            sets = editableExercise.sets,
                            restSeconds = editableExercise.restSeconds
                        )
                    }
                    
                    _editRoutineState.value = currentState.copy(
                        isLoading = false,
                        isSuccessfullyEdited = true
                    )
                } else {
                    _editRoutineState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Original routine not found"
                    )
                }
            } catch (e: Exception) {
                _editRoutineState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Failed to save routine: ${e.message}"
                )
            }
        }
    }
    
    private fun hasEditChanges(state: EditRoutineUiState): Boolean {
        val original = state.originalRoutine ?: return false
        
        // Check basic info changes
        if (state.editedName != original.routine.name) return true
        if (state.editedNotes != original.routine.notes) return true
        if (state.selectedFolder?.id != original.routine.folderId) return true
        
        // Check exercise changes
        if (state.editedExercises.size != original.exercises.size) return true
        
        // Check exercise order and content
        state.editedExercises.forEachIndexed { index, editableExercise ->
            val originalExercise = original.exercises.getOrNull(index)
            if (originalExercise == null) return true
            if (editableExercise.exercise.id != originalExercise.exerciseId) return true
            if (editableExercise.sets != originalExercise.defaultSets) return true
            if (editableExercise.restSeconds != originalExercise.restSeconds) return true
        }
        
        return false
    }
    
    private fun validateEditedRoutine(name: String, exercises: List<EditableExercise>): Boolean {
        return name.trim().isNotEmpty() && exercises.isNotEmpty()
    }
    
    // Routine list expansion methods
    fun expandRoutineList() {
        _uiState.value = _uiState.value.copy(isRoutineListExpanded = true)
    }
    
    fun collapseRoutineList() {
        _uiState.value = _uiState.value.copy(isRoutineListExpanded = false)
    }
    
    fun toggleRoutineListExpansion() {
        _uiState.value = _uiState.value.copy(
            isRoutineListExpanded = !_uiState.value.isRoutineListExpanded
        )
    }
    
    // Get routines with limit applied based on expansion state
    fun getDisplayedRoutines(): List<WorkoutRoutine> {
        val filteredRoutines = getFilteredRoutines()
        val currentState = _uiState.value
        
        return if (currentState.isRoutineListExpanded) {
            filteredRoutines
        } else {
            filteredRoutines.take(currentState.routineListLimit)
        }
    }
    
    // Check if there are more routines to show
    fun hasMoreRoutines(): Boolean {
        val filteredRoutines = getFilteredRoutines()
        val currentState = _uiState.value
        return filteredRoutines.size > currentState.routineListLimit
    }
    
    // Filtered routines based on search and folder selection
    fun getFilteredRoutines(): List<WorkoutRoutine> {
        val currentState = _uiState.value
        val allRoutines = if (currentState.selectedFolder != null) {
            currentState.routines.filter { it.folderId == currentState.selectedFolder!!.id }
        } else {
            currentState.routines
        }
        
        return if (currentState.searchQuery.isNotEmpty()) {
            allRoutines.filter { routine ->
                routine.name.contains(currentState.searchQuery, ignoreCase = true)
            }
        } else {
            allRoutines
        }
    }
    
    // Options menu functions
    fun showRoutineOptions(routineId: String) {
        val routine = _uiState.value.routines.find { it.id == routineId }
        _uiState.value = _uiState.value.copy(
            showOptionsMenu = true,
            selectedRoutineForOptions = routine
        )
    }
    
    fun hideRoutineOptions() {
        _uiState.value = _uiState.value.copy(
            showOptionsMenu = false,
            selectedRoutineForOptions = null
        )
    }
    
    fun deleteRoutineFromOptions(routineId: String) {
        deleteRoutine(routineId)
        hideRoutineOptions()
        loadRoutinesData()
    }
    
    fun duplicateRoutine(routineId: String) {
        viewModelScope.launch {
            try {
                val routineWithExercises = routineRepository.getRoutineWithExercises(routineId)
                if (routineWithExercises != null) {
                    // Prepare exercises for createRoutine
                    val exercisePairs = routineWithExercises.exercises.map { routineExercise ->
                        Pair(routineExercise.exerciseId, routineExercise.defaultSets)
                    }
                    
                    val newRoutineId = routineRepository.createRoutine(
                        name = "${routineWithExercises.routine.name} (Copy)",
                        folderId = routineWithExercises.routine.folderId,
                        exercises = exercisePairs,
                        notes = routineWithExercises.routine.notes
                    )
                    
                    hideRoutineOptions()
                    loadRoutinesData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to duplicate routine: ${e.message}"
                )
                hideRoutineOptions()
            }
        }
    }
    
    // Add exercise to existing routine (for Exercise Dictionary integration)
    fun addExerciseToExistingRoutine(routineId: String, exercise: Exercise) {
        viewModelScope.launch {
            try {
                // Create default sets for the exercise
                val defaultSets = listOf(
                    ExerciseSet(setNumber = 1, targetReps = "8-12"),
                    ExerciseSet(setNumber = 2, targetReps = "8-12"),
                    ExerciseSet(setNumber = 3, targetReps = "8-12")
                )
                
                // Add the exercise to the routine
                routineRepository.addExerciseToRoutine(
                    routineId = routineId,
                    exerciseId = exercise.id,
                    sets = defaultSets,
                    restSeconds = 90
                )
                
                // Reload routines to reflect changes
                loadRoutinesData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add exercise: ${e.message}"
                )
            }
        }
    }
}