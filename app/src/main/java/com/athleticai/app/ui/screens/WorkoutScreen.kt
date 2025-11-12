package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.viewmodels.WorkoutViewModel
import com.athleticai.app.ui.viewmodels.ExerciseWithDetails
import com.athleticai.app.ui.viewmodels.SetLog
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.SupersetProgressIndicator
import com.athleticai.app.ui.components.ExerciseGuidanceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    units: String = "kg",
    onNavigateBack: () -> Unit
) {
    val workoutState by workoutViewModel.workoutState.collectAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Workout Session",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_BACK,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        
        if (workoutState.isActive) {
            // Active routine workout session
            RoutineActiveWorkoutScreen(
                workoutState = workoutState,
                workoutViewModel = workoutViewModel,
                snackbarHostState = snackbarHostState,
                units = units,
                onFinishWorkout = {
                    onNavigateBack()
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // No active session
            LoadingScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading workout...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun RoutineActiveWorkoutScreen(
    workoutState: com.athleticai.app.ui.viewmodels.WorkoutUiState,
    workoutViewModel: WorkoutViewModel,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    units: String,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Show error if there's one
    workoutState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            workoutViewModel.clearError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workout progress header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MaterialSymbol(
                            symbol = if (workoutState.isPaused) MaterialSymbols.PLAY_ARROW else MaterialSymbols.TIMER,
                            size = IconSizes.MEDIUM,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentDescription = if (workoutState.isPaused) "Paused" else "Workout Timer"
                        )
                        Text(
                            text = if (workoutState.isPaused) "Workout Paused" else "Workout in Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Show superset progress if in superset, otherwise regular exercise progress
                    if (workoutState.isInSuperset && workoutState.currentSupersetGroup != null) {
                        val supersetProgress = workoutViewModel.getSupersetProgress()
                        supersetProgress?.let { progress ->
                            Text(
                                text = "Superset: Exercise ${progress.currentExercise} of ${progress.totalExercises} • Round ${progress.currentRound} of ${progress.totalRounds}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Text(
                            text = "Exercise ${workoutState.currentExerciseIndex + 1} of ${workoutState.exercises.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Progress indicator  
                LinearProgressIndicator(
                    progress = if (workoutState.exercises.isNotEmpty()) {
                        val completedExercises = workoutState.completedSets.keys.size
                        (completedExercises.toFloat() / workoutState.exercises.size.toFloat())
                    } else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "${workoutState.completedSets.keys.size}/${workoutState.exercises.size} exercises completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Superset progress indicator
        if (workoutState.isInSuperset && workoutState.currentSupersetGroup != null) {
            val supersetProgress = workoutViewModel.getSupersetProgress()
            supersetProgress?.let { progress ->
                SupersetProgressIndicator(
                    currentExercise = progress.currentExercise,
                    totalExercises = progress.totalExercises,
                    currentRound = progress.currentRound,
                    totalRounds = progress.totalRounds,
                    groupType = workoutState.currentSupersetGroup!!.groupType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
        
        // Rest timer display
        if (workoutState.isResting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (workoutState.isInSuperset) {
                            if (workoutState.restTimeRemaining <= 30) {
                                "Between Exercises"
                            } else {
                                "Between Rounds"
                            }
                        } else {
                            "Rest Time"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${workoutState.restTimeRemaining / 60}:${String.format("%02d", workoutState.restTimeRemaining % 60)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Button(
                        onClick = { workoutViewModel.skipRest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Skip Rest")
                    }
                }
            }
        }
        
        // Current exercise with active set logging
        workoutState.exercises.getOrNull(workoutState.currentExerciseIndex)?.let { exercise ->
            RoutineActiveExerciseCard(
                exercise = exercise,
                currentSetNumber = workoutState.currentSetNumber,
                completedSets = workoutState.completedSets[exercise.programExercise.exerciseId] ?: emptyList(),
                isResting = workoutState.isResting,
                isPaused = workoutState.isPaused,
                onLogSet = { weight, reps, rpe ->
                    workoutViewModel.logSet(exercise.programExercise.exerciseId, weight, reps, rpe)
                },
                onSkipRest = { workoutViewModel.skipRest() },
                units = units
            )
        }
        
        // Action buttons
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { 
                    if (workoutState.isPaused) {
                        workoutViewModel.resumeWorkout()
                    } else {
                        workoutViewModel.pauseWorkout()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        symbol = if (workoutState.isPaused) MaterialSymbols.PLAY_ARROW else MaterialSymbols.PAUSE,
                        size = IconSizes.MEDIUM,
                        contentDescription = if (workoutState.isPaused) "Resume" else "Pause"
                    )
                    Text(if (workoutState.isPaused) "Resume" else "Pause")
                }
            }
            
            Button(
                onClick = { workoutViewModel.finishWorkout { onFinishWorkout() } },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.CHECK_CIRCLE,
                        size = IconSizes.MEDIUM,
                        contentDescription = "Finish Workout"
                    )
                    Text("Finish")
                }
            }
        }
    }
}

@Composable
private fun RoutineActiveExerciseCard(
    exercise: ExerciseWithDetails,
    currentSetNumber: Int,
    completedSets: List<SetLog>,
    isResting: Boolean,
    isPaused: Boolean,
    onLogSet: (weight: Double, reps: Int, rpe: Double) -> Unit,
    onSkipRest: () -> Unit,
    units: String
) {
    var currentWeight by remember { mutableStateOf(exercise.suggestedWeight.toString()) }
    var currentReps by remember { mutableStateOf("") }
    var currentRpe by remember { mutableStateOf("") }
    var isInstructionsExpanded by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Exercise guidance card with GIF and instructions
        exercise.exerciseInfo?.let { exerciseInfo ->
            ExerciseGuidanceCard(
                exercise = exerciseInfo,
                currentSetNumber = if (completedSets.size >= exercise.programExercise.sets) {
                    exercise.programExercise.sets
                } else {
                    completedSets.size + 1
                },
                totalSets = exercise.programExercise.sets,
                isExpanded = isInstructionsExpanded,
                onToggleExpand = { isInstructionsExpanded = !isInstructionsExpanded }
            )
        } ?: run {
            // Fallback to basic card if no exercise info
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.programExercise.exerciseId
                            .replace("_", " ")
                            .split(" ")
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${exercise.programExercise.sets} sets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${exercise.programExercise.repRangeMin}-${exercise.programExercise.repRangeMax} reps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Set logging card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Completed Sets Display
                if (completedSets.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Completed Sets",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            completedSets.forEach { set ->
                                Text(
                                    text = "Set ${set.setNumber}: ${set.weight}${units} × ${set.reps} reps @ RPE ${set.rpe}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                // Current Set Input
                if (!isResting && !isPaused && currentSetNumber <= exercise.programExercise.sets) {
                    Text(
                        text = "Log Set ${completedSets.size + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentWeight,
                            onValueChange = { currentWeight = it },
                            label = { Text("Weight (${units})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = currentReps,
                            onValueChange = { currentReps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = currentRpe,
                            onValueChange = { currentRpe = it },
                            label = { Text("RPE") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                    
                    Button(
                        onClick = {
                            val weight = currentWeight.toDoubleOrNull() ?: 0.0
                            val reps = currentReps.toIntOrNull() ?: 0
                            val rpe = currentRpe.toDoubleOrNull() ?: 0.0
                            
                            if (weight > 0 && reps > 0 && rpe > 0) {
                                onLogSet(weight, reps, rpe)
                                // Reset for next set
                                currentReps = ""
                                currentRpe = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentWeight.toDoubleOrNull() != null && 
                                 currentReps.toIntOrNull() != null && 
                                 currentRpe.toDoubleOrNull() != null
                    ) {
                        Text("Complete Set")
                    }
                } else if (isPaused) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Workout Paused",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Resume when ready to continue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                } else if (completedSets.size >= exercise.programExercise.sets) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.CHECK_CIRCLE,
                                size = IconSizes.LARGE,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentDescription = "Exercise Complete"
                            )
                            Text(
                                text = "Exercise Complete!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}