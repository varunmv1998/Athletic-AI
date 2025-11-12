package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.InteractiveCard
import com.athleticai.app.ui.components.ExerciseSelectionDialog
import com.athleticai.app.ui.components.FolderSelectorDialog
import com.athleticai.app.ui.viewmodels.RoutineViewModel
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import com.athleticai.app.ui.viewmodels.EditableExercise
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ExerciseSet
import com.athleticai.app.data.database.entities.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: String,
    routineViewModel: RoutineViewModel,
    exerciseSelectionViewModel: ExerciseSelectionViewModel,
    onNavigateBack: () -> Unit,
    onRoutineSaved: () -> Unit
) {
    val editState by routineViewModel.editRoutineState.collectAsState()
    val uiState by routineViewModel.uiState.collectAsState()
    var showFolderSelector by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    
    // Load routine for editing when screen loads
    LaunchedEffect(routineId) {
        routineViewModel.startEditingRoutine(routineId)
    }
    
    // Handle successful save
    LaunchedEffect(editState.isSuccessfullyEdited) {
        if (editState.isSuccessfullyEdited) {
            routineViewModel.clearEditRoutineState()
            onRoutineSaved()
        }
    }
    
    // Handle back navigation with unsaved changes
    val handleBackNavigation = {
        if (editState.hasChanges) {
            showDiscardDialog = true
        } else {
            routineViewModel.clearEditRoutineState()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Routine") },
                navigationIcon = {
                    IconButton(onClick = handleBackNavigation) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_BACK,
                            size = IconSizes.STANDARD,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Add Exercise Button
                    IconButton(
                        onClick = { routineViewModel.showExercisePickerForEdit() }
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ADD,
                            size = IconSizes.STANDARD,
                            contentDescription = "Add exercise"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Save/Cancel buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = handleBackNavigation,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { routineViewModel.saveEditedRoutine() },
                        enabled = editState.isValidRoutine && editState.hasChanges,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (editState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        
        if (editState.isLoading && editState.originalRoutine == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Routine Name
                item {
                    OutlinedTextField(
                        value = editState.editedName,
                        onValueChange = { routineViewModel.updateEditedName(it) },
                        label = { Text("Routine Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = editState.editedName.trim().isEmpty()
                    )
                }
                
                // Folder Selection
                item {
                    OutlinedButton(
                        onClick = { showFolderSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.FOLDER,
                                size = IconSizes.SMALL,
                                contentDescription = null
                            )
                            Text(
                                text = editState.selectedFolder?.name ?: "No folder"
                            )
                        }
                    }
                }
                
                // Notes
                item {
                    OutlinedTextField(
                        value = editState.editedNotes,
                        onValueChange = { routineViewModel.updateEditedNotes(it) },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
                
                // Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Exercises (${editState.editedExercises.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = { routineViewModel.showExercisePickerForEdit() }
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.ADD,
                                size = IconSizes.SMALL,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Exercise")
                        }
                    }
                }
                
                // Exercise List
                if (editState.editedExercises.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.FITNESS_CENTER,
                                    size = IconSizes.EXTRA_LARGE,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                
                                Text(
                                    text = "No exercises yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "Add exercises to build your routine",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = editState.editedExercises,
                        key = { _, exercise -> exercise.id }
                    ) { index, editableExercise ->
                        EditableExerciseCard(
                            editableExercise = editableExercise,
                            onRemove = { routineViewModel.removeExerciseFromEdit(index) },
                            onSetsChange = { sets ->
                                routineViewModel.updateExerciseSetsForEdit(editableExercise.id, sets)
                            },
                            onMoveUp = if (index > 0) {
                                { routineViewModel.moveExercise(index, index - 1) }
                            } else null,
                            onMoveDown = if (index < editState.editedExercises.size - 1) {
                                { routineViewModel.moveExercise(index, index + 1) }
                            } else null
                        )
                    }
                }
            }
        }
        
        // Error message
        editState.errorMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                routineViewModel.clearEditError()
            }
            
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
    
    // Exercise selection dialog
    if (editState.showExercisePicker) {
        ExerciseSelectionDialog(
            exerciseSelectionViewModel = exerciseSelectionViewModel,
            onExerciseSelected = { exercise ->
                routineViewModel.addExerciseToEdit(exercise)
            },
            onDismiss = { routineViewModel.hideExercisePickerForEdit() }
        )
    }
    
    // Folder selector dialog
    if (showFolderSelector) {
        FolderSelectorDialog(
            folders = uiState.folders,
            selectedFolder = editState.selectedFolder,
            onFolderSelected = { folder ->
                routineViewModel.setEditRoutineFolder(folder)
                showFolderSelector = false
            },
            onCreateFolder = { name ->
                routineViewModel.createFolder(name)
            },
            onDismiss = { showFolderSelector = false }
        )
    }
    
    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { 
                Text("You have unsaved changes. Are you sure you want to discard them?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        routineViewModel.clearEditRoutineState()
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
}

@Composable
private fun EditableExerciseCard(
    editableExercise: EditableExercise,
    onRemove: () -> Unit,
    onSetsChange: (List<ExerciseSet>) -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    InteractiveCard(
        onClick = { /* Could expand for detailed editing */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = editableExercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    editableExercise.exercise.equipment?.let { equipment ->
                        Text(
                            text = equipment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Move up button
                    onMoveUp?.let { moveUp ->
                        IconButton(onClick = moveUp) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.KEYBOARD_ARROW_UP,
                                size = IconSizes.SMALL,
                                contentDescription = "Move up",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Move down button
                    onMoveDown?.let { moveDown ->
                        IconButton(onClick = moveDown) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.KEYBOARD_ARROW_DOWN,
                                size = IconSizes.SMALL,
                                contentDescription = "Move down",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Remove button
                    IconButton(onClick = onRemove) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.CLOSE,
                            size = IconSizes.SMALL,
                            contentDescription = "Remove exercise",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Sets configuration
            Text(
                text = "Sets: ${editableExercise.sets.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Quick sets summary (could be expanded for detailed editing)
            if (editableExercise.sets.isNotEmpty()) {
                Text(
                    text = editableExercise.sets.joinToString(" • ") { set ->
                        "${set.targetReps ?: "8-10"} reps"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

