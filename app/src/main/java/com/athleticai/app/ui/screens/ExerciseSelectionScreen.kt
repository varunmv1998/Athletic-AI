package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.ExerciseSearchBar
import com.athleticai.app.ui.components.ExerciseFilterChips
import com.athleticai.app.ui.components.ExerciseListItem
import com.athleticai.app.ui.components.ExerciseDetailsModal
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import com.athleticai.app.data.database.entities.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionScreen(
    viewModel: ExerciseSelectionViewModel,
    onNavigateBack: () -> Unit,
    onExercisesSelected: (List<Exercise>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showExerciseDetails by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Exercises",
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
        },
        floatingActionButton = {
            if (uiState.selectedExercises.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onExercisesSelected(uiState.selectedExercises) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${uiState.selectedExercises.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_FORWARD,
                            size = IconSizes.STANDARD,
                            contentDescription = "Continue"
                        )
                    }
                }
            }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chips
            ExerciseFilterChips(
                selectedMuscleGroup = uiState.selectedMuscleGroup,
                selectedEquipment = uiState.selectedEquipment,
                selectedCategory = uiState.selectedCategory,
                onMuscleGroupSelected = viewModel::updateMuscleGroupFilter,
                onEquipmentSelected = viewModel::updateEquipmentFilter,
                onCategorySelected = viewModel::updateCategoryFilter,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recently Used Section (if no search/filter active)
            if (uiState.searchQuery.isBlank() && uiState.hasNoActiveFilters() && uiState.recentlyUsedExercises.isNotEmpty()) {
                Text(
                    text = "Recently Used",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Exercise List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show recently used exercises first (if no search/filter active)
                if (uiState.searchQuery.isBlank() && uiState.hasNoActiveFilters()) {
                    items(uiState.recentlyUsedExercises) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            isSelected = uiState.selectedExercises.contains(exercise),
                            onSelectionChanged = { selected ->
                                if (selected) {
                                    viewModel.addSelectedExercise(exercise)
                                } else {
                                    viewModel.removeSelectedExercise(exercise)
                                }
                            },
                            onDetailsClick = {
                                selectedExercise = exercise
                                showExerciseDetails = true
                            },
                            isRecentlyUsed = true
                        )
                    }
                    
                    if (uiState.recentlyUsedExercises.isNotEmpty() && uiState.searchResults.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "All Exercises",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Show search results or all exercises
                items(uiState.searchResults) { exercise ->
                    // Don't show recently used exercises twice
                    if (uiState.searchQuery.isNotBlank() || uiState.hasActiveFilters() || !uiState.recentlyUsedExercises.contains(exercise)) {
                        ExerciseListItem(
                            exercise = exercise,
                            isSelected = uiState.selectedExercises.contains(exercise),
                            onSelectionChanged = { selected ->
                                if (selected) {
                                    viewModel.addSelectedExercise(exercise)
                                } else {
                                    viewModel.removeSelectedExercise(exercise)
                                }
                            },
                            onDetailsClick = {
                                selectedExercise = exercise
                                showExerciseDetails = true
                            }
                        )
                    }
                }

                // Loading indicator
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Empty state
                if (!uiState.isLoading && uiState.searchResults.isEmpty() && uiState.recentlyUsedExercises.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.SEARCH,
                                    size = IconSizes.EXTRA_LARGE,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    contentDescription = null
                                )
                                Text(
                                    text = "No exercises found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Try adjusting your search or filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Exercise Details Modal
    if (showExerciseDetails && selectedExercise != null) {
        ExerciseDetailsModal(
            exercise = selectedExercise!!,
            onDismiss = { showExerciseDetails = false },
            onAddToWorkout = {
                viewModel.addSelectedExercise(selectedExercise!!)
                showExerciseDetails = false
            }
        )
    }
}