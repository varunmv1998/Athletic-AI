package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.ExerciseDetailsBottomSheet
import com.athleticai.app.ui.components.ExerciseSearchBar
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.RoutineSelectionDialog
import com.athleticai.app.ui.viewmodels.ExerciseDictionaryViewModel
import com.athleticai.app.ui.viewmodels.RoutineViewModel
import com.athleticai.app.data.database.entities.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDictionaryScreen(
    viewModel: ExerciseDictionaryViewModel,
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreateRoutine: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val routineUiState by routineViewModel.uiState.collectAsState()
    var showExerciseDetails by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var showRoutineSelection by remember { mutableStateOf(false) }
    var exerciseToAdd by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Exercise Dictionary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_BACK,
                            size = IconSizes.STANDARD,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            ExerciseSearchBar(
                query = uiState.searchQuery,
                onQueryChanged = viewModel::updateSearchQuery,
                placeholder = "Search exercises...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Body Part Filters
                val bodyParts = listOf("All", "chest", "back", "shoulders", "legs", "arms", "core")
                items(bodyParts) { bodyPart ->
                    FilterChip(
                        onClick = { 
                            viewModel.updateBodyPartFilter(
                                if (bodyPart == "All") null else bodyPart
                            )
                        },
                        label = { 
                            Text(
                                text = bodyPart.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        selected = if (bodyPart == "All") {
                            uiState.selectedBodyPart == null
                        } else {
                            uiState.selectedBodyPart == bodyPart
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Equipment Filter Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val equipment = listOf("All", "barbell", "dumbbell", "cable", "machine", "body weight")
                items(equipment) { equip ->
                    FilterChip(
                        onClick = { 
                            viewModel.updateEquipmentFilter(
                                if (equip == "All") null else equip
                            )
                        },
                        label = { 
                            Text(
                                text = equip.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = if (equip == "All") {
                            uiState.selectedEquipment == null
                        } else {
                            uiState.selectedEquipment == equip
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }

            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading exercises...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Error State
            else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.INFO,
                            size = IconSizes.LARGE,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = null
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Exercise List
            else {
                if (uiState.exercises.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.SEARCH,
                                size = IconSizes.LARGE,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = null
                            )
                            Text(
                                text = if (uiState.searchQuery.isBlank() && uiState.hasNoActiveFilters()) {
                                    "No exercises available.\nTry downloading from Settings > Exercise Database."
                                } else {
                                    "No exercises found.\nTry different search terms or filters."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.exercises) { exercise ->
                            ExerciseDictionaryItem(
                                exercise = exercise,
                                onInfoClick = {
                                    selectedExercise = exercise
                                    showExerciseDetails = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Exercise Details Bottom Sheet
        if (showExerciseDetails && selectedExercise != null) {
            ExerciseDetailsBottomSheet(
                exercise = selectedExercise!!,
                onDismiss = { 
                    showExerciseDetails = false 
                    selectedExercise = null
                },
                onAddToRoutine = { exercise ->
                    exerciseToAdd = exercise
                    showRoutineSelection = true
                    showExerciseDetails = false
                    selectedExercise = null
                }
            )
        }
        
        // Routine Selection Dialog
        if (showRoutineSelection && exerciseToAdd != null) {
            RoutineSelectionDialog(
                exercise = exerciseToAdd!!,
                routines = routineUiState.routines,
                onRoutineSelected = { routine, exercise ->
                    routineViewModel.addExerciseToExistingRoutine(routine.id, exercise)
                    showRoutineSelection = false
                    exerciseToAdd = null
                },
                onCreateNewRoutine = {
                    showRoutineSelection = false
                    exerciseToAdd = null
                    onNavigateToCreateRoutine()
                },
                onDismiss = {
                    showRoutineSelection = false
                    exerciseToAdd = null
                }
            )
        }
    }
}

@Composable
private fun ExerciseDictionaryItem(
    exercise: Exercise,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Primary muscles
                if (exercise.primaryMuscles.isNotEmpty()) {
                    Text(
                        text = exercise.primaryMuscles.joinToString(", ") { 
                            it.replaceFirstChar { char -> char.uppercase() } 
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Equipment and category
                Row {
                    exercise.equipment?.let { equipment ->
                        Text(
                            text = equipment.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (exercise.equipment != null && exercise.category.isNotEmpty()) {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = exercise.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            // Info button
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(48.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.INFO,
                    size = IconSizes.STANDARD,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Exercise Details"
                )
            }
        }
    }
}