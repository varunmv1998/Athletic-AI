package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.SupersetType
import com.athleticai.app.data.repository.SupersetGroupWithExercises
import com.athleticai.app.ui.components.*
import com.athleticai.app.ui.viewmodels.CustomWorkoutViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupersetEnabledExerciseReviewScreen(
    workoutId: String?,
    selectedExercises: List<Exercise>,
    exerciseConfigurations: Map<String, ExerciseConfiguration>,
    customWorkoutViewModel: CustomWorkoutViewModel = viewModel(),
    onConfigurationChanged: (String, ExerciseConfiguration) -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    onReorderExercises: (List<Exercise>) -> Unit,
    onContinue: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by customWorkoutViewModel.uiState.collectAsState()
    val workoutStructure by remember(workoutId) {
        if (workoutId != null) {
            customWorkoutViewModel.getWorkoutStructure(workoutId)
        } else {
            flowOf(null)
        }
    }.collectAsState(initial = null)
    
    var exerciseList by remember(selectedExercises) { 
        mutableStateOf(selectedExercises) 
    }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Error handling
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            customWorkoutViewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.validationMessage) {
        uiState.validationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            customWorkoutViewModel.clearValidationMessages()
        }
    }
    
    val totalEstimatedDuration = remember(exerciseConfigurations) {
        exerciseConfigurations.values.sumOf { config ->
            (config.sets * 30) + (config.restSeconds * config.sets)
        } / 60
    }
    
    // Prepare draggable exercise items
    val draggableExercises = remember(exerciseList, exerciseConfigurations, workoutStructure) {
        val supersetExerciseIds = workoutStructure?.supersetGroups?.flatMap { group ->
            group.exercises.map { it.exerciseId }
        } ?: emptyList()
        
        exerciseList.map { exercise ->
            DraggableExerciseItem(
                exercise = exercise,
                configuration = exerciseConfigurations[exercise.id] ?: ExerciseConfiguration(exercise = exercise),
                isSelected = uiState.selectedExercisesForSuperset.contains(exercise.id),
                isInSuperset = supersetExerciseIds.contains(exercise.id),
                supersetGroup = workoutStructure?.supersetGroups?.find { group ->
                    group.exercises.any { it.exerciseId == exercise.id }
                }?.group
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // Superset selection FAB
            SupersetSelectionFAB(
                selectedCount = uiState.selectedExercisesForSuperset.size,
                onCreateSuperset = {
                    customWorkoutViewModel.showSupersetCreationDialog()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Column {
                        Text("Configure Exercises")
                        Text(
                            text = "${selectedExercises.size} exercises selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_BACK,
                            size = IconSizes.STANDARD,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Duration Estimator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                DurationEstimator(
                    totalMinutes = totalEstimatedDuration,
                    exerciseCount = selectedExercises.size,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Superset instructions
            if (uiState.selectedExercisesForSuperset.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.INFO,
                            size = IconSizes.SMALL,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.selectedExercisesForSuperset.size} exercises selected for superset. Tap the FAB to create.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Exercise and Superset List
            DraggableWorkoutStructure(
                individualExercises = draggableExercises.filter { !it.isInSuperset },
                supersetGroups = workoutStructure?.supersetGroups ?: emptyList(),
                selectedExercisesForSuperset = uiState.selectedExercisesForSuperset,
                onReorderExercises = { reorderedExercises ->
                    exerciseList = reorderedExercises
                    onReorderExercises(reorderedExercises)
                },
                onReorderSupersetGroups = { groupIds ->
                    workoutId?.let { id ->
                        customWorkoutViewModel.reorderSupersetGroups(id, groupIds)
                    }
                },
                onConfigurationChanged = onConfigurationChanged,
                onRemoveExercise = onRemoveExercise,
                onSelectExerciseForSuperset = { exerciseId ->
                    customWorkoutViewModel.selectExerciseForSuperset(exerciseId)
                },
                onDeselectExerciseForSuperset = { exerciseId ->
                    customWorkoutViewModel.deselectExerciseForSuperset(exerciseId)
                },
                onConfigureGroup = { groupId ->
                    // TODO: Implement group configuration dialog
                },
                onUngroupSuperset = { groupId ->
                    customWorkoutViewModel.ungroupSuperset(groupId)
                },
                onCreateSuperset = {
                    customWorkoutViewModel.showSupersetCreationDialog()
                },
                modifier = Modifier.weight(1f)
            )

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedExercises.isNotEmpty()
            ) {
                Text("Continue to Workout Naming")
            }
        }
        
        // Superset Creation Dialog
        if (uiState.showSupersetDialog) {
            val selectedExerciseList = selectedExercises.filter { exercise ->
                uiState.selectedExercisesForSuperset.contains(exercise.id)
            }
            
            SupersetCreationDialog(
                selectedExercises = selectedExerciseList,
                onCreateSuperset = { groupType, restBetween, restAfter, rounds ->
                    workoutId?.let { id ->
                        customWorkoutViewModel.createSuperset(
                            workoutId = id,
                            groupType = groupType,
                            restBetweenExercises = restBetween,
                            restAfterGroup = restAfter,
                            rounds = rounds
                        )
                    }
                },
                onDismiss = {
                    customWorkoutViewModel.hideSupersetCreationDialog()
                }
            )
        }
    }
}