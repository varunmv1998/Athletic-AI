package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.ui.screens.ExerciseConfiguration

@Composable
fun WorkoutSummaryCard(
    exercises: List<Exercise>,
    exerciseConfigurations: Map<String, ExerciseConfiguration>,
    totalDuration: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = formatDuration(totalDuration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatistic(
                    icon = MaterialSymbols.FITNESS_CENTER,
                    value = exercises.size.toString(),
                    label = "Exercises"
                )
                
                val totalSets = exerciseConfigurations.values.sumOf { it.sets }
                SummaryStatistic(
                    icon = MaterialSymbols.REPEAT,
                    value = totalSets.toString(),
                    label = "Total Sets"
                )
                
                val avgRPE = if (exerciseConfigurations.isNotEmpty()) {
                    exerciseConfigurations.values.map { it.rpe }.average()
                } else 0.0
                SummaryStatistic(
                    icon = MaterialSymbols.TRENDING_UP,
                    value = String.format("%.1f", avgRPE),
                    label = "Avg RPE"
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            // Exercise List
            Text(
                text = "Exercises (${exercises.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            exercises.forEach { exercise ->
                val config = exerciseConfigurations[exercise.id]
                if (config != null) {
                    ExerciseSummaryItem(
                        exercise = exercise,
                        configuration = config
                    )
                }
            }
            
            // Muscle Groups Summary
            val muscleGroups = exercises.flatMap { it.primaryMuscles }.distinct()
            if (muscleGroups.isNotEmpty()) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                
                Text(
                    text = "Primary Muscle Groups",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = muscleGroups.joinToString(", ") { 
                        it.replaceFirstChar { char -> char.uppercase() } 
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryStatistic(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MaterialSymbol(
            symbol = icon,
            size = IconSizes.STANDARD,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseSummaryItem(
    exercise: Exercise,
    configuration: ExerciseConfiguration,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${configuration.sets} sets • ${configuration.minReps}-${configuration.maxReps} reps • RPE ${configuration.rpe}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${formatRestTime(configuration.restSeconds)} rest",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(totalMinutes: Int): String {
    return when {
        totalMinutes < 60 -> "${totalMinutes}min"
        totalMinutes % 60 == 0 -> "${totalMinutes / 60}hr"
        else -> "${totalMinutes / 60}hr ${totalMinutes % 60}min"
    }
}

private fun formatRestTime(restSeconds: Int): String {
    return when {
        restSeconds < 60 -> "${restSeconds}s"
        restSeconds % 60 == 0 -> "${restSeconds / 60}min"
        else -> "${restSeconds / 60}:${String.format("%02d", restSeconds % 60)}"
    }
}