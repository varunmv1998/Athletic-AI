package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.WorkoutSummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutNamingScreen(
    exercises: List<Exercise>,
    exerciseConfigurations: Map<String, ExerciseConfiguration>,
    onSaveWorkout: (name: String, description: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var workoutName by remember { mutableStateOf("") }
    var workoutDescription by remember { mutableStateOf("") }
    
    val totalDuration = remember(exerciseConfigurations) {
        exerciseConfigurations.values.sumOf { config ->
            (config.sets * 30) + (config.restSeconds * config.sets)
        } / 60
    }
    
    val isValidName = workoutName.trim().isNotEmpty()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Save Workout") },
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workout Name Input
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Workout Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = workoutName,
                        onValueChange = { workoutName = it },
                        label = { Text("Workout Name*") },
                        placeholder = { Text("e.g., Push Day, Leg Blast, Upper Body") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = workoutName.isNotEmpty() && !isValidName,
                        supportingText = {
                            if (workoutName.isNotEmpty() && !isValidName) {
                                Text(
                                    text = "Workout name cannot be empty",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    OutlinedTextField(
                        value = workoutDescription,
                        onValueChange = { workoutDescription = it },
                        label = { Text("Description (optional)") },
                        placeholder = { Text("Notes about this workout...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Workout Summary
            WorkoutSummaryCard(
                exercises = exercises,
                exerciseConfigurations = exerciseConfigurations,
                totalDuration = totalDuration
            )
        }

        // Save Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isValidName) {
                            onSaveWorkout(workoutName.trim(), workoutDescription.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isValidName
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.SAVE,
                        size = IconSizes.STANDARD,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Workout",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                if (!isValidName && workoutName.isNotEmpty()) {
                    Text(
                        text = "Please enter a valid workout name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

// Quick suggestions for workout names based on exercises
@Composable
private fun WorkoutNameSuggestions(
    exercises: List<Exercise>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = remember(exercises) {
        generateWorkoutNameSuggestions(exercises)
    }
    
    if (suggestions.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.take(3).forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onSuggestionClick(suggestion) },
                        label = { Text(suggestion) }
                    )
                }
            }
        }
    }
}

private fun generateWorkoutNameSuggestions(exercises: List<Exercise>): List<String> {
    val suggestions = mutableListOf<String>()
    
    // Analyze primary muscle groups
    val primaryMuscles = exercises.flatMap { it.primaryMuscles }.distinct()
    
    when {
        primaryMuscles.any { it.contains("chest", ignoreCase = true) } && 
        primaryMuscles.any { it.contains("shoulder", ignoreCase = true) } -> {
            suggestions.add("Push Day")
            suggestions.add("Chest & Shoulders")
        }
        primaryMuscles.any { it.contains("back", ignoreCase = true) } && 
        primaryMuscles.any { it.contains("bicep", ignoreCase = true) } -> {
            suggestions.add("Pull Day")
            suggestions.add("Back & Biceps")
        }
        primaryMuscles.any { it.contains("quadriceps", ignoreCase = true) } ||
        primaryMuscles.any { it.contains("hamstrings", ignoreCase = true) } -> {
            suggestions.add("Leg Day")
            suggestions.add("Lower Body")
        }
        primaryMuscles.size >= 4 -> {
            suggestions.add("Full Body")
            suggestions.add("Total Body")
        }
    }
    
    // Add generic suggestions
    suggestions.addAll(listOf("Morning Workout", "Quick Session", "Strength Training"))
    
    return suggestions.distinct()
}