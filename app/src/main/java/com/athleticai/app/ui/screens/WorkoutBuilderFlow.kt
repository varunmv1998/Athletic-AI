package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.ui.viewmodels.CustomWorkoutViewModel
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import kotlinx.coroutines.launch

enum class WorkoutBuilderStep {
    EXERCISE_SELECTION,
    EXERCISE_CONFIGURATION,
    WORKOUT_NAMING,
    PROGRAM_OVERVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderFlow(
    customWorkoutViewModel: CustomWorkoutViewModel,
    exerciseSelectionViewModel: ExerciseSelectionViewModel,
    onNavigateBack: () -> Unit,
    onWorkoutCreated: () -> Unit
) {
    var currentStep by remember { mutableStateOf(WorkoutBuilderStep.PROGRAM_OVERVIEW) }
    var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var exerciseConfigurations by remember { mutableStateOf<Map<String, ExerciseConfiguration>>(emptyMap()) }
    
    val uiState by customWorkoutViewModel.uiState.collectAsState()
    val selectionUiState by exerciseSelectionViewModel.uiState.collectAsState()
    val activeProgram = uiState.activeProgram
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Dialog states
    var showCreateProgramDialog by remember { mutableStateOf(false) }
    var programName by remember { mutableStateOf("") }
    var programDescription by remember { mutableStateOf("") }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentStep) {
                WorkoutBuilderStep.PROGRAM_OVERVIEW -> {
                    // Simplified program overview replacement
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Legacy Workout Builder",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "This feature has been replaced with the new Routine system.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                    /*ProgramOverviewScreen(
                        programs = uiState.programs,
                        currentWorkouts = uiState.currentWorkouts,
                        onCreateProgram = {
                            showCreateProgramDialog = true
                        },
                        onEditProgram = { program ->
                            // TODO: Handle program editing
                        },
                        onDeleteProgram = { program ->
                            customWorkoutViewModel.deleteProgram(program)
                        },
                        onActivateProgram = { program ->
                            customWorkoutViewModel.activateProgram(program)
                        },
                        onCreateWorkout = {
                            if (activeProgram != null) {
                                currentStep = WorkoutBuilderStep.EXERCISE_SELECTION
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please create or activate a program first")
                                }
                            }
                        },
                        onEditWorkout = { workout ->
                            // TODO: Handle workout editing
                        },
                        onDeleteWorkout = { workout ->
                            customWorkoutViewModel.deleteWorkout(workout)
                        },
                        onNavigateBack = onNavigateBack
                    )*/
                }
                
                WorkoutBuilderStep.EXERCISE_SELECTION -> {
                    ExerciseSelectionScreen(
                        viewModel = exerciseSelectionViewModel,
                        onNavigateBack = {
                            currentStep = WorkoutBuilderStep.PROGRAM_OVERVIEW
                        },
                        onExercisesSelected = { exercises ->
                            selectedExercises = exercises
                            // Initialize configurations for selected exercises
                            exerciseConfigurations = exercises.associate { exercise: Exercise ->
                                exercise.id to ExerciseConfiguration(exercise = exercise)
                            }
                            currentStep = WorkoutBuilderStep.EXERCISE_CONFIGURATION
                        }
                    )
                }
                
                WorkoutBuilderStep.EXERCISE_CONFIGURATION -> {
                    SupersetEnabledExerciseReviewScreen(
                        workoutId = null, // Creating new workout
                        selectedExercises = selectedExercises,
                        exerciseConfigurations = exerciseConfigurations,
                        customWorkoutViewModel = customWorkoutViewModel,
                        onConfigurationChanged = { exerciseId: String, config: ExerciseConfiguration ->
                            exerciseConfigurations = exerciseConfigurations.toMutableMap().apply {
                                put(exerciseId, config)
                            }
                        },
                        onRemoveExercise = { exercise ->
                            selectedExercises = selectedExercises - exercise
                            exerciseConfigurations = exerciseConfigurations.toMutableMap().apply {
                                remove(exercise.id)
                            }
                            customWorkoutViewModel.deselectExerciseForSuperset(exercise.id)
                        },
                        onReorderExercises = { newOrder ->
                            selectedExercises = newOrder
                        },
                        onContinue = {
                            currentStep = WorkoutBuilderStep.WORKOUT_NAMING
                        },
                        onBackClick = {
                            currentStep = WorkoutBuilderStep.EXERCISE_SELECTION
                        }
                    )
                }
                
                        WorkoutBuilderStep.WORKOUT_NAMING -> {
                    WorkoutNamingScreen(
                exercises = selectedExercises,
                exerciseConfigurations = exerciseConfigurations,
                onSaveWorkout = { name, description ->
                    if (activeProgram != null) {
                        customWorkoutViewModel.createWorkout(
                            programId = activeProgram.id,
                            name = name,
                            description = description,
                            exercises = selectedExercises,
                            configurations = exerciseConfigurations
                        )
                        
                        // Reset flow state
                        selectedExercises = emptyList()
                        exerciseConfigurations = emptyMap()
                        currentStep = WorkoutBuilderStep.PROGRAM_OVERVIEW
                        
                        onWorkoutCreated()
                    }
                },
                onBackClick = {
                    currentStep = WorkoutBuilderStep.EXERCISE_CONFIGURATION
                }
                    )
                }
            }
            
            // Show loading overlay
            if (uiState.isCreatingWorkout || uiState.isCreatingProgram || uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when {
                                    uiState.isCreatingWorkout -> "Creating workout..."
                                    uiState.isCreatingProgram -> "Creating program..."
                                    else -> "Loading..."
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            customWorkoutViewModel.clearError()
        }
    }
    
    // Handle validation messages
    LaunchedEffect(uiState.validationMessage) {
        uiState.validationMessage?.let { validation ->
            snackbarHostState.showSnackbar(
                message = validation,
                duration = SnackbarDuration.Short
            )
            customWorkoutViewModel.clearValidationMessages()
        }
    }
    
    // Handle duration warnings
    LaunchedEffect(uiState.durationWarning) {
        uiState.durationWarning?.let { warning ->
            snackbarHostState.showSnackbar(
                message = warning,
                actionLabel = "OK",
                duration = SnackbarDuration.Long
            )
        }
    }
    
    // Handle exercise selection validation
    LaunchedEffect(selectionUiState.validationMessage) {
        selectionUiState.validationMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Create Program Dialog
    if (showCreateProgramDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCreateProgramDialog = false
                programName = ""
                programDescription = ""
            },
            title = { Text("Create New Program") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = programName,
                        onValueChange = { programName = it },
                        label = { Text("Program Name") },
                        placeholder = { Text("e.g., Push/Pull/Legs") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = programDescription,
                        onValueChange = { programDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Brief description of your program") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (programName.isNotBlank()) {
                            customWorkoutViewModel.createProgram(
                                name = programName.trim(),
                                description = programDescription.trim()
                            )
                            showCreateProgramDialog = false
                            programName = ""
                            programDescription = ""
                            
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Program created successfully",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please enter a program name",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    enabled = programName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCreateProgramDialog = false
                        programName = ""
                        programDescription = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}