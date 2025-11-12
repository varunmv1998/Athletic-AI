package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.ui.components.ExerciseConfigCard
import com.athleticai.app.ui.components.DurationEstimator
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes

data class ExerciseConfiguration(
    val exercise: Exercise,
    val sets: Int = 3,
    val minReps: Int = 8,
    val maxReps: Int = 12,
    val restSeconds: Int = 60,
    val rpe: Float = 7.0f,
    val notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseReviewScreen(
    selectedExercises: List<Exercise>,
    onConfigurationChanged: (String, ExerciseConfiguration) -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    onReorderExercises: (List<Exercise>) -> Unit,
    onContinue: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exerciseList by remember(selectedExercises) { 
        mutableStateOf(selectedExercises) 
    }
    
    var exerciseConfigurations by remember {
        mutableStateOf(
            selectedExercises.associateWith { exercise ->
                ExerciseConfiguration(exercise = exercise)
            }.toMutableMap()
        )
    }
    
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    val totalEstimatedDuration = remember(exerciseConfigurations) {
        exerciseConfigurations.values.sumOf { config ->
            // Exercise time + rest time * sets
            (config.sets * 30) + (config.restSeconds * config.sets)
        } / 60 // Convert to minutes
    }

    Column(
        modifier = modifier.fillMaxSize()
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

        // Exercise Configuration List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = exerciseList,
                key = { _, item -> item.id }
            ) { index, exercise ->
                val configuration = exerciseConfigurations[exercise] ?: ExerciseConfiguration(exercise = exercise)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Scale up the dragged item
                            if (draggedItemIndex == index) {
                                scaleX = 1.05f
                                scaleY = 1.05f
                                alpha = 0.8f
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Drag handle
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { 
                                        draggedItemIndex = index
                                    },
                                    onDragEnd = {
                                        targetIndex?.let { target ->
                                            if (target != index) {
                                                val mutableList = exerciseList.toMutableList()
                                                val item = mutableList.removeAt(index)
                                                mutableList.add(target, item)
                                                exerciseList = mutableList
                                                onReorderExercises(mutableList)
                                            }
                                        }
                                        draggedItemIndex = null
                                        targetIndex = null
                                    },
                                    onDrag = { _, offset ->
                                        // Calculate target index based on drag offset
                                        val itemHeight = 140 // Approximate height of each item
                                        val dragDistance = offset.y.toInt() / itemHeight
                                        val newTarget = (index + dragDistance).coerceIn(0, exerciseList.size - 1)
                                        if (newTarget != targetIndex) {
                                            targetIndex = newTarget
                                        }
                                    }
                                )
                            }
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.DRAG_HANDLE,
                            size = IconSizes.STANDARD,
                            contentDescription = "Reorder"
                        )
                    }
                    
                    // Exercise config card
                    ExerciseConfigCard(
                        exercise = exercise,
                        configuration = configuration,
                        onConfigurationChanged = { newConfig ->
                            exerciseConfigurations[exercise] = newConfig
                            onConfigurationChanged(exercise.id, newConfig)
                        },
                        onRemoveClick = {
                            exerciseConfigurations.remove(exercise)
                            val mutableList = exerciseList.toMutableList()
                            mutableList.remove(exercise)
                            exerciseList = mutableList
                            onRemoveExercise(exercise)
                            onReorderExercises(mutableList)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Continue Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedExercises.isNotEmpty()
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.ARROW_FORWARD,
                    size = IconSizes.STANDARD,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue to Workout Details",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}