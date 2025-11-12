package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernExerciseSelector(
    exerciseSelectionViewModel: ExerciseSelectionViewModel,
    selectedExercises: List<Exercise>,
    onExerciseToggle: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val exerciseState by exerciseSelectionViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()
    
    var showExerciseDetails by remember { mutableStateOf(false) }
    var selectedExerciseForDetails by remember { mutableStateOf<Exercise?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // Header with search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Exercises",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Selected count badge
                    if (selectedExercises.isNotEmpty()) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text("${selectedExercises.size}")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                ExerciseSearchBar(
                    query = exerciseState.searchQuery,
                    onQueryChanged = exerciseSelectionViewModel::updateSearchQuery,
                    autoFocus = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Muscle group filters
                val muscleGroups = listOf("All", "Chest", "Back", "Shoulders", "Legs", "Arms", "Core")
                items(muscleGroups) { muscle ->
                    FilterChip(
                        onClick = { 
                            exerciseSelectionViewModel.updateMuscleGroupFilter(
                                if (muscle == "All") null else muscle.lowercase()
                            )
                        },
                        label = { Text(muscle) },
                        selected = if (muscle == "All") {
                            exerciseState.selectedMuscleGroup == null
                        } else {
                            exerciseState.selectedMuscleGroup == muscle.lowercase()
                        }
                    )
                }
            }
            
            // Equipment filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val equipment = listOf("All", "Barbell", "Dumbbell", "Cable", "Machine", "Body Only")
                items(equipment) { equip ->
                    FilterChip(
                        onClick = { 
                            exerciseSelectionViewModel.updateEquipmentFilter(
                                if (equip == "All") null else equip.lowercase()
                            )
                        },
                        label = { Text(equip) },
                        selected = if (equip == "All") {
                            exerciseState.selectedEquipment == null
                        } else {
                            exerciseState.selectedEquipment == equip.lowercase()
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }
            
            // Exercise list
            if (exerciseState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Recently used section
                    if (exerciseState.searchQuery.isBlank() && 
                        exerciseState.hasNoActiveFilters() && 
                        exerciseState.recentlyUsedExercises.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Used",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(exerciseState.recentlyUsedExercises) { exercise ->
                            ModernExerciseCard(
                                exercise = exercise,
                                isSelected = selectedExercises.contains(exercise),
                                onToggle = { onExerciseToggle(exercise) },
                                onInfoClick = {
                                    selectedExerciseForDetails = exercise
                                    showExerciseDetails = true
                                }
                            )
                        }
                        
                        if (exerciseState.searchResults.isNotEmpty()) {
                            item {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    text = "All Exercises",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                    
                    // Main exercise list
                    items(exerciseState.searchResults) { exercise ->
                        ModernExerciseCard(
                            exercise = exercise,
                            isSelected = selectedExercises.contains(exercise),
                            onToggle = { onExerciseToggle(exercise) },
                            onInfoClick = {
                                selectedExerciseForDetails = exercise
                                showExerciseDetails = true
                            }
                        )
                    }
                    
                    if (exerciseState.searchResults.isEmpty() && 
                        !exerciseState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No exercises found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom action bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedExercises.isNotEmpty()
                    ) {
                        Text("Done (${selectedExercises.size})")
                    }
                }
            }
        }
    }
    
    // Exercise details bottom sheet
    if (showExerciseDetails && selectedExerciseForDetails != null) {
        ExerciseDetailsBottomSheet(
            exercise = selectedExerciseForDetails!!,
            onDismiss = { 
                showExerciseDetails = false
                selectedExerciseForDetails = null
            },
            onAddToRoutine = { exercise ->
                onExerciseToggle(exercise)
                showExerciseDetails = false
                selectedExerciseForDetails = null
            }
        )
    }
}

@Composable
private fun ModernExerciseCard(
    exercise: Exercise,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Exercise info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary muscles
                    if (exercise.primaryMuscles.isNotEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = exercise.primaryMuscles.first().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    
                    // Equipment
                    exercise.equipment?.let { equipment ->
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = equipment.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
            
            // Info button
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(40.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.INFO,
                    size = IconSizes.SMALL,
                    contentDescription = "Exercise details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}