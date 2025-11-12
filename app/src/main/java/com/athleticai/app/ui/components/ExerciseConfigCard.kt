package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.ui.screens.ExerciseConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseConfigCard(
    exercise: Exercise,
    configuration: ExerciseConfiguration,
    onConfigurationChanged: (ExerciseConfiguration) -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sets by remember { mutableIntStateOf(configuration.sets) }
    var minReps by remember { mutableIntStateOf(configuration.minReps) }
    var maxReps by remember { mutableIntStateOf(configuration.maxReps) }
    var restSeconds by remember { mutableIntStateOf(configuration.restSeconds) }
    var rpe by remember { mutableFloatStateOf(configuration.rpe) }
    var notes by remember { mutableStateOf(configuration.notes) }

    // Update configuration when any value changes
    LaunchedEffect(sets, minReps, maxReps, restSeconds, rpe, notes) {
        onConfigurationChanged(
            configuration.copy(
                sets = sets,
                minReps = minReps,
                maxReps = maxReps,
                restSeconds = restSeconds,
                rpe = rpe,
                notes = notes
            )
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with exercise name and remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (exercise.primaryMuscles.isNotEmpty()) {
                        Text(
                            text = exercise.primaryMuscles.joinToString(", ") {
                                it.replaceFirstChar { char -> char.uppercase() }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(onClick = onRemoveClick) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.DELETE,
                        size = IconSizes.STANDARD,
                        contentDescription = "Remove exercise",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Configuration inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sets
                ConfigNumberField(
                    label = "Sets",
                    value = sets,
                    onValueChange = { sets = it.coerceIn(1, 10) },
                    modifier = Modifier.weight(1f)
                )
                
                // Reps Range
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = "Rep Range",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ConfigNumberField(
                            label = "Min",
                            value = minReps,
                            onValueChange = { 
                                minReps = it.coerceIn(1, 50)
                                if (minReps > maxReps) maxReps = minReps
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        ConfigNumberField(
                            label = "Max",
                            value = maxReps,
                            onValueChange = { 
                                maxReps = it.coerceIn(minReps, 50)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rest Time
                RestTimeSelector(
                    restSeconds = restSeconds,
                    onRestSecondsChanged = { restSeconds = it },
                    modifier = Modifier.weight(1f)
                )
                
                // RPE
                RPESelector(
                    rpe = rpe,
                    onRPEChanged = { rpe = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Exercise notes or modifications...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ConfigNumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { newValue ->
            val intValue = newValue.toIntOrNull() ?: 0
            onValueChange(intValue)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestTimeSelector(
    restSeconds: Int,
    onRestSecondsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val restOptions = listOf(30, 45, 60, 90, 120, 180, 240, 300)
    
    Column(modifier = modifier) {
        Text(
            text = "Rest Time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        var expanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = "${restSeconds / 60}:${String.format("%02d", restSeconds % 60)}",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                restOptions.forEach { seconds ->
                    DropdownMenuItem(
                        text = {
                            Text("${seconds / 60}:${String.format("%02d", seconds % 60)}")
                        },
                        onClick = {
                            onRestSecondsChanged(seconds)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RPESelector(
    rpe: Float,
    onRPEChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RPE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = rpe.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = rpe,
            onValueChange = onRPEChanged,
            valueRange = 5f..10f,
            steps = 9, // 5.5, 6.0, 6.5, etc.
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "10",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}