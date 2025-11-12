package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.athleticai.app.ui.components.ModernExerciseSelector
import com.athleticai.app.ui.viewmodels.RoutineViewModel
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ExerciseSet
import com.athleticai.app.data.database.entities.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routineViewModel: RoutineViewModel,
    exerciseSelectionViewModel: ExerciseSelectionViewModel,
    onNavigateBack: () -> Unit,
    onRoutineCreated: () -> Unit
) {
    val createState by routineViewModel.createRoutineState.collectAsState()
    val uiState by routineViewModel.uiState.collectAsState()
    var showFolderSelector by remember { mutableStateOf(false) }
    
    // Initialize the create routine flow when screen loads
    LaunchedEffect(Unit) {
        routineViewModel.startCreatingRoutine()
    }
    
    LaunchedEffect(createState.isSuccessfullyCreated) {
        if (createState.isSuccessfullyCreated) {
            // Routine was successfully created, clean up state and navigate back
            routineViewModel.clearCreateRoutineState()
            onRoutineCreated()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Routine") },
                navigationIcon = {
                    IconButton(onClick = {
                        routineViewModel.cancelCreatingRoutine()
                        onNavigateBack()
                    }) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.CLOSE,
                            size = IconSizes.STANDARD,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { routineViewModel.createRoutine() },
                        enabled = createState.isValidRoutine && !createState.isLoading
                    ) {
                        if (createState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Routine name
            item {
                OutlinedTextField(
                    value = createState.routineName,
                    onValueChange = { routineViewModel.setRoutineName(it) },
                    label = { Text("Routine Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = createState.routineName.isNotEmpty() && createState.routineName.trim().isEmpty()
                )
            }
            
            // Folder selection
            item {
                FolderSelectionCard(
                    selectedFolder = createState.selectedFolder,
                    onSelectFolder = { showFolderSelector = true }
                )
            }
            
            // Exercise selection
            item {
                ExerciseSelectionCard(
                    selectedExercises = createState.selectedExercises,
                    onAddExercises = { routineViewModel.showExercisePickerForCreate() },
                    onRemoveExercise = { exercise -> routineViewModel.removeExerciseFromRoutine(exercise) },
                    onUpdateSets = { exerciseId, sets -> routineViewModel.updateExerciseSetsForCreate(exerciseId, sets) },
                    exerciseSets = createState.exerciseSets
                )
            }
            
            // Notes
            item {
                OutlinedTextField(
                    value = createState.notes,
                    onValueChange = { routineViewModel.setRoutineNotes(it) },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
            
            // Validation feedback
            if (!createState.isValidRoutine) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Complete these steps:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            if (createState.routineName.trim().isEmpty()) {
                                ValidationItem(text = "Enter a routine name")
                            }
                            
                            if (createState.selectedExercises.isEmpty()) {
                                ValidationItem(text = "Add at least one exercise")
                            }
                        }
                    }
                }
            }
        }
        
        // Error message
        createState.errorMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                // Clear error if needed
            }
            
            Card(
                modifier = Modifier
                    .padding(16.dp),
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
    
    // Exercise picker
    if (createState.showExercisePicker) {
        ModernExerciseSelector(
            exerciseSelectionViewModel = exerciseSelectionViewModel,
            selectedExercises = createState.selectedExercises,
            onExerciseToggle = { exercise ->
                if (createState.selectedExercises.contains(exercise)) {
                    routineViewModel.removeExerciseFromRoutine(exercise)
                } else {
                    routineViewModel.addExerciseToRoutine(exercise)
                }
            },
            onDismiss = { routineViewModel.hideExercisePickerForCreate() }
        )
    }
    
    // Folder selector
    if (showFolderSelector) {
        FolderSelectorDialog(
            folders = uiState.folders,
            selectedFolder = createState.selectedFolder,
            onFolderSelected = { folder ->
                routineViewModel.setRoutineFolder(folder)
                showFolderSelector = false
            },
            onCreateFolder = { name ->
                routineViewModel.createFolder(name)
                showFolderSelector = false
            },
            onDismiss = { showFolderSelector = false }
        )
    }
}

@Composable
private fun FolderSelectionCard(
    selectedFolder: Folder?,
    onSelectFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onSelectFolder,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Folder",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = selectedFolder?.name ?: "No folder selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedFolder != null) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            MaterialSymbol(
                symbol = MaterialSymbols.FOLDER,
                size = IconSizes.STANDARD,
                contentDescription = "Select folder",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ExerciseSelectionCard(
    selectedExercises: List<Exercise>,
    onAddExercises: () -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    onUpdateSets: (String, List<ExerciseSet>) -> Unit,
    exerciseSets: Map<String, List<ExerciseSet>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exercises (${selectedExercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                FilledTonalButton(
                    onClick = onAddExercises,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.ADD,
                        size = IconSizes.SMALL,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Exercises")
                }
            }
            
            if (selectedExercises.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.FITNESS_CENTER,
                        size = IconSizes.LARGE,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "No exercises selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Add exercises to create your routine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Exercise list
                selectedExercises.forEachIndexed { index, exercise ->
                    SelectedExerciseItem(
                        exercise = exercise,
                        sets = exerciseSets[exercise.id] ?: emptyList(),
                        onRemove = { onRemoveExercise(exercise) },
                        onUpdateSets = { sets -> onUpdateSets(exercise.id, sets) },
                        showDivider = index < selectedExercises.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedExerciseItem(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    onRemove: () -> Unit,
    onUpdateSets: (List<ExerciseSet>) -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Exercise header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${exercise.primaryMuscles.joinToString(", ")} • ${exercise.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onRemove) {
                MaterialSymbol(
                    symbol = MaterialSymbols.REMOVE,
                    size = IconSizes.SMALL,
                    contentDescription = "Remove exercise",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        // Sets configuration
        SetsConfiguration(
            sets = sets,
            onSetsUpdate = onUpdateSets
        )
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun SetsConfiguration(
    sets: List<ExerciseSet>,
    onSetsUpdate: (List<ExerciseSet>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sets Configuration",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Add/Remove sets buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (sets.isNotEmpty()) {
                        onSetsUpdate(sets.dropLast(1))
                    }
                },
                enabled = sets.isNotEmpty()
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.REMOVE,
                    size = IconSizes.SMALL,
                    contentDescription = "Remove set"
                )
            }
            
            Text(
                text = "${sets.size} sets",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = {
                    val newSet = ExerciseSet(
                        setNumber = sets.size + 1,
                        targetReps = "8-10"
                    )
                    onSetsUpdate(sets + newSet)
                },
                enabled = sets.size < 8 // Max 8 sets
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.ADD,
                    size = IconSizes.SMALL,
                    contentDescription = "Add set"
                )
            }
        }
        
        // Sets list
        sets.forEachIndexed { index, set ->
            SetConfigurationItem(
                setNumber = index + 1,
                targetReps = set.targetReps,
                onRepsChange = { newReps ->
                    val updatedSets = sets.toMutableList()
                    updatedSets[index] = set.copy(targetReps = newReps)
                    onSetsUpdate(updatedSets)
                }
            )
        }
        
        if (sets.isEmpty()) {
            Text(
                text = "Add sets to configure this exercise",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SetConfigurationItem(
    setNumber: Int,
    targetReps: String,
    onRepsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Set $setNumber:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        
        OutlinedTextField(
            value = targetReps,
            onValueChange = onRepsChange,
            label = { Text("Reps") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("e.g., 8-10, 12, AMRAP") }
        )
    }
}

@Composable
private fun ValidationItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaterialSymbol(
            symbol = MaterialSymbols.CIRCLE,
            size = IconSizes.SMALL,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// Reuse the folder selector dialog from RoutineListScreen
@Composable
private fun FolderSelectorDialog(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    
    if (showCreateDialog) {
        CreateFolderDialog(
            onCreateFolder = { name ->
                onCreateFolder(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Folder") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // No folder option
                    item {
                        TextButton(
                            onClick = { onFolderSelected(null) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedFolder == null,
                                    onClick = { onFolderSelected(null) }
                                )
                                Text("No Folder")
                            }
                        }
                    }
                    
                    // Existing folders
                    items(folders) { folder ->
                        TextButton(
                            onClick = { onFolderSelected(folder) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedFolder?.id == folder.id,
                                    onClick = { onFolderSelected(folder) }
                                )
                                Text(folder.name)
                            }
                        }
                    }
                    
                    // Create new folder option
                    item {
                        TextButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.ADD,
                                    size = IconSizes.STANDARD,
                                    contentDescription = null
                                )
                                Text("Create New Folder")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CreateFolderDialog(
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreateFolder(folderName) },
                enabled = folderName.trim().isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}