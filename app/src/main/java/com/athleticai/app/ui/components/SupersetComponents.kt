package com.athleticai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.SupersetGroup
import com.athleticai.app.data.database.entities.SupersetType
import com.athleticai.app.data.database.entities.WorkoutExercise
import com.athleticai.app.data.database.entities.Exercise

@Composable
fun SupersetGroupCard(
    group: SupersetGroup,
    exercises: List<WorkoutExercise>,
    exerciseDetails: Map<String, Exercise>,
    isSelected: Boolean = false,
    onGroupClick: () -> Unit = {},
    onConfigureGroup: () -> Unit = {},
    onUngroupSuperset: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val groupColor = when (group.groupType) {
        SupersetType.SUPERSET -> MaterialTheme.colorScheme.primaryContainer
        SupersetType.TRISET -> MaterialTheme.colorScheme.secondaryContainer
        SupersetType.GIANTSET -> MaterialTheme.colorScheme.tertiaryContainer
        SupersetType.CIRCUIT -> MaterialTheme.colorScheme.errorContainer
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                groupColor.copy(alpha = 0.3f) 
            else 
                groupColor.copy(alpha = 0.1f)
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Superset header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        symbol = when (group.groupType) {
                            SupersetType.SUPERSET -> MaterialSymbols.FITNESS_CENTER
                            SupersetType.TRISET -> MaterialSymbols.SPORTS
                            SupersetType.GIANTSET -> MaterialSymbols.SELF_IMPROVEMENT
                            SupersetType.CIRCUIT -> MaterialSymbols.REPEAT
                        },
                        size = IconSizes.SMALL,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "${group.groupType.name.lowercase().capitalize()} (${exercises.size} exercises)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (group.rounds > 1) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = "${group.rounds}x",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onConfigureGroup) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.SETTINGS,
                            size = IconSizes.SMALL,
                            contentDescription = "Configure superset",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = onUngroupSuperset) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.CLOSE,
                            size = IconSizes.SMALL,
                            contentDescription = "Ungroup superset",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Exercise list with bracket indicator
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Bracket indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Exercises column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    exercises.sortedBy { it.orderInSuperset }.forEach { exercise ->
                        val exerciseInfo = exerciseDetails[exercise.exerciseId]
                        if (exerciseInfo != null) {
                            SupersetExerciseItem(
                                exercise = exercise,
                                exerciseInfo = exerciseInfo,
                                showOrderNumber = true
                            )
                        }
                    }
                }
            }
            
            // Rest configuration summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Between: ${group.restBetweenExercises}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "After: ${group.restAfterGroup}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SupersetExerciseItem(
    exercise: WorkoutExercise,
    exerciseInfo: Exercise,
    showOrderNumber: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showOrderNumber) {
            Card(
                modifier = Modifier.size(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${exercise.orderInSuperset + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = exerciseInfo.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${exercise.targetSets} sets × ${exercise.targetReps} reps @ RPE ${exercise.rpeTarget}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SupersetCreationDialog(
    selectedExercises: List<Exercise>,
    onCreateSuperset: (SupersetType, Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var groupType by remember { mutableStateOf(SupersetType.SUPERSET) }
    var restBetweenExercises by remember { mutableStateOf(10) }
    var restAfterGroup by remember { mutableStateOf(90) }
    var rounds by remember { mutableStateOf(1) }
    
    val suggestedType = when (selectedExercises.size) {
        2 -> SupersetType.SUPERSET
        3 -> SupersetType.TRISET
        in 4..6 -> SupersetType.GIANTSET
        else -> SupersetType.SUPERSET
    }
    
    LaunchedEffect(selectedExercises.size) {
        groupType = suggestedType
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Create ${groupType.name.lowercase().capitalize()}") 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selected exercises preview
                Text(
                    text = "Selected Exercises (${selectedExercises.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    selectedExercises.take(4).forEach { exercise ->
                        Text(
                            text = "• ${exercise.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (selectedExercises.size > 4) {
                        Text(
                            text = "... and ${selectedExercises.size - 4} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Divider()
                
                // Group type selection
                Text(
                    text = "Group Type:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                SupersetType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = groupType == type,
                            onClick = { groupType = type }
                        )
                        Text(
                            text = type.name.lowercase().capitalize(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Rest configuration
                Text(
                    text = "Rest Configuration:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = restBetweenExercises.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { 
                                if (it in 0..30) restBetweenExercises = it 
                            }
                        },
                        label = { Text("Between (s)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = restAfterGroup.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { 
                                if (it in 30..300) restAfterGroup = it 
                            }
                        },
                        label = { Text("After (s)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                // Rounds configuration (for circuits)
                if (groupType == SupersetType.CIRCUIT) {
                    OutlinedTextField(
                        value = rounds.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { 
                                if (it in 1..5) rounds = it 
                            }
                        },
                        label = { Text("Rounds") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateSuperset(groupType, restBetweenExercises, restAfterGroup, rounds)
                }
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

@Composable
fun SupersetProgressIndicator(
    currentExercise: Int,
    totalExercises: Int,
    currentRound: Int,
    totalRounds: Int,
    groupType: SupersetType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialSymbol(
                symbol = when (groupType) {
                    SupersetType.SUPERSET -> MaterialSymbols.FITNESS_CENTER
                    SupersetType.TRISET -> MaterialSymbols.SPORTS
                    SupersetType.GIANTSET -> MaterialSymbols.SELF_IMPROVEMENT
                    SupersetType.CIRCUIT -> MaterialSymbols.REPEAT
                },
                size = IconSizes.STANDARD,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column {
                Text(
                    text = "${groupType.name.lowercase().capitalize()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Exercise $currentExercise of $totalExercises" + 
                           if (totalRounds > 1) " • Round $currentRound of $totalRounds" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress circle
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { currentExercise.toFloat() / totalExercises.toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "$currentExercise",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SupersetSelectionFAB(
    selectedCount: Int,
    onCreateSuperset: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedCount >= 2) {
        ExtendedFloatingActionButton(
            onClick = onCreateSuperset,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            MaterialSymbol(
                symbol = MaterialSymbols.FITNESS_CENTER,
                size = IconSizes.STANDARD,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Superset ($selectedCount)")
        }
    }
}

@Composable
fun SupersetBracket(
    exerciseCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(4.dp)
            .height((exerciseCount * 60).dp) // Approximate height per exercise
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(2.dp)
            )
    )
}