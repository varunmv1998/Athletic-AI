package com.athleticai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.SupersetGroup
import com.athleticai.app.data.database.entities.WorkoutExercise
import com.athleticai.app.data.repository.SupersetGroupWithExercises
import com.athleticai.app.ui.screens.ExerciseConfiguration

data class DraggableExerciseItem(
    val exercise: Exercise,
    val configuration: ExerciseConfiguration,
    val isSelected: Boolean = false,
    val isInSuperset: Boolean = false,
    val supersetGroup: SupersetGroup? = null
)

@Composable
fun DraggableExerciseList(
    exercises: List<DraggableExerciseItem>,
    supersetGroups: List<SupersetGroupWithExercises>,
    selectedExercisesForSuperset: Set<String>,
    onReorderExercises: (List<Exercise>) -> Unit,
    onReorderSupersetGroups: (List<String>) -> Unit,
    onConfigurationChanged: (String, ExerciseConfiguration) -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    onSelectExerciseForSuperset: (String) -> Unit,
    onDeselectExerciseForSuperset: (String) -> Unit,
    onConfigureGroup: (String) -> Unit,
    onUngroupSuperset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    var draggedGroupIndex by remember { mutableStateOf<Int?>(null) }
    var targetGroupIndex by remember { mutableStateOf<Int?>(null) }
    
    val hapticFeedback = LocalHapticFeedback.current
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Individual exercises (not in supersets)
        val individualExercises = exercises.filter { !it.isInSuperset }
        
        itemsIndexed(
            items = individualExercises,
            key = { _, item -> "exercise_${item.exercise.id}" }
        ) { index, exerciseItem ->
            DraggableExerciseCard(
                exerciseItem = exerciseItem,
                index = index,
                isDragged = draggedItemIndex == index,
                isDropTarget = targetIndex == index,
                isSelectedForSuperset = selectedExercisesForSuperset.contains(exerciseItem.exercise.id),
                onDragStart = {
                    draggedItemIndex = index
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDragEnd = {
                    targetIndex?.let { target ->
                        if (target != index) {
                            val reorderedList = individualExercises.toMutableList()
                            val item = reorderedList.removeAt(index)
                            reorderedList.add(target, item)
                            onReorderExercises(reorderedList.map { it.exercise })
                        }
                    }
                    draggedItemIndex = null
                    targetIndex = null
                },
                onDrag = { offset ->
                    val itemHeight = 160
                    val dragDistance = offset.y.toInt() / itemHeight
                    val newTarget = (index + dragDistance).coerceIn(0, individualExercises.size - 1)
                    if (newTarget != targetIndex) {
                        targetIndex = newTarget
                    }
                },
                onConfigurationChanged = onConfigurationChanged,
                onRemoveExercise = onRemoveExercise,
                onSelectForSuperset = onSelectExerciseForSuperset,
                onDeselectForSuperset = onDeselectExerciseForSuperset
            )
        }
        
        // Superset groups
        itemsIndexed(
            items = supersetGroups,
            key = { _, item -> "superset_${item.group.id}" }
        ) { groupIndex, groupWithExercises ->
            DraggableSupersetCard(
                group = groupWithExercises.group,
                exercises = groupWithExercises.exercises,
                exerciseDetails = exercises.associate { it.exercise.id to it.exercise },
                index = groupIndex,
                isDragged = draggedGroupIndex == groupIndex,
                isDropTarget = targetGroupIndex == groupIndex,
                onDragStart = {
                    draggedGroupIndex = groupIndex
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDragEnd = {
                    targetGroupIndex?.let { target ->
                        if (target != groupIndex) {
                            val reorderedGroups = supersetGroups.toMutableList()
                            val item = reorderedGroups.removeAt(groupIndex)
                            reorderedGroups.add(target, item)
                            onReorderSupersetGroups(reorderedGroups.map { it.group.id })
                        }
                    }
                    draggedGroupIndex = null
                    targetGroupIndex = null
                },
                onDrag = { offset ->
                    val itemHeight = 200
                    val dragDistance = offset.y.toInt() / itemHeight
                    val newTarget = (groupIndex + dragDistance).coerceIn(0, supersetGroups.size - 1)
                    if (newTarget != targetGroupIndex) {
                        targetGroupIndex = newTarget
                    }
                },
                onConfigureGroup = onConfigureGroup,
                onUngroupSuperset = onUngroupSuperset
            )
        }
    }
}

@Composable
fun DraggableExerciseCard(
    exerciseItem: DraggableExerciseItem,
    index: Int,
    isDragged: Boolean,
    isDropTarget: Boolean,
    isSelectedForSuperset: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit,
    onConfigurationChanged: (String, ExerciseConfiguration) -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    onSelectForSuperset: (String) -> Unit,
    onDeselectForSuperset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation = if (isDragged) 8.dp else if (isDropTarget) 4.dp else 1.dp
    val backgroundColor = when {
        isSelectedForSuperset -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isDropTarget -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(12.dp))
            .graphicsLayer {
                if (isDragged) {
                    scaleX = 1.05f
                    scaleY = 1.05f
                    alpha = 0.9f
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelectedForSuperset) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            IconButton(
                onClick = { },
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDrag = { _, offset -> onDrag(offset) }
                        )
                    }
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.DRAG_HANDLE,
                    size = IconSizes.STANDARD,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Exercise configuration
            ExerciseConfigCard(
                exercise = exerciseItem.exercise,
                configuration = exerciseItem.configuration,
                onConfigurationChanged = { newConfig ->
                    onConfigurationChanged(exerciseItem.exercise.id, newConfig)
                },
                onRemoveClick = {
                    onRemoveExercise(exerciseItem.exercise)
                },
                modifier = Modifier.weight(1f)
            )
            
            // Superset selection checkbox
            if (!exerciseItem.isInSuperset) {
                Checkbox(
                    checked = isSelectedForSuperset,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onSelectForSuperset(exerciseItem.exercise.id)
                        } else {
                            onDeselectForSuperset(exerciseItem.exercise.id)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun DraggableSupersetCard(
    group: SupersetGroup,
    exercises: List<WorkoutExercise>,
    exerciseDetails: Map<String, Exercise>,
    index: Int,
    isDragged: Boolean,
    isDropTarget: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit,
    onConfigureGroup: (String) -> Unit,
    onUngroupSuperset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation = if (isDragged) 8.dp else if (isDropTarget) 4.dp else 2.dp
    val backgroundColor = if (isDropTarget) 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    else 
        MaterialTheme.colorScheme.surface
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(12.dp))
            .graphicsLayer {
                if (isDragged) {
                    scaleX = 1.02f
                    scaleY = 1.02f
                    alpha = 0.9f
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Draggable header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle for superset group
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDragEnd = { onDragEnd() },
                                onDrag = { _, offset -> onDrag(offset) }
                            )
                        }
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.DRAG_HANDLE,
                        size = IconSizes.STANDARD,
                        contentDescription = "Drag to reorder superset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Use the existing SupersetGroupCard content
                SupersetGroupCard(
                    group = group,
                    exercises = exercises,
                    exerciseDetails = exerciseDetails,
                    isSelected = false,
                    onGroupClick = { },
                    onConfigureGroup = { onConfigureGroup(group.id) },
                    onUngroupSuperset = { onUngroupSuperset(group.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SupersetDropZone(
    isActive: Boolean,
    exerciseCount: Int,
    onDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isActive) 80.dp else 40.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isActive) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else 
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaterialSymbol(
                    symbol = if (isActive) MaterialSymbols.ADD_CIRCLE else MaterialSymbols.FITNESS_CENTER,
                    size = IconSizes.SMALL,
                    contentDescription = null,
                    tint = if (isActive) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Text(
                    text = if (isActive) 
                        "Drop here to create superset ($exerciseCount exercises)" 
                    else 
                        "Drag exercises here to create superset",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DraggableWorkoutStructure(
    individualExercises: List<DraggableExerciseItem>,
    supersetGroups: List<SupersetGroupWithExercises>,
    selectedExercisesForSuperset: Set<String>,
    onReorderExercises: (List<Exercise>) -> Unit,
    onReorderSupersetGroups: (List<String>) -> Unit,
    onConfigurationChanged: (String, ExerciseConfiguration) -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    onSelectExerciseForSuperset: (String) -> Unit,
    onDeselectExerciseForSuperset: (String) -> Unit,
    onConfigureGroup: (String) -> Unit,
    onUngroupSuperset: (String) -> Unit,
    onCreateSuperset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragActive by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedExercisesForSuperset.size) {
        isDragActive = selectedExercisesForSuperset.size >= 2
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Superset groups
        itemsIndexed(
            items = supersetGroups,
            key = { _, item -> "group_${item.group.id}" }
        ) { groupIndex, groupWithExercises ->
            DraggableSupersetCard(
                group = groupWithExercises.group,
                exercises = groupWithExercises.exercises,
                exerciseDetails = individualExercises.associate { it.exercise.id to it.exercise },
                index = groupIndex,
                isDragged = false,
                isDropTarget = false,
                onDragStart = { },
                onDragEnd = { },
                onDrag = { },
                onConfigureGroup = onConfigureGroup,
                onUngroupSuperset = onUngroupSuperset
            )
        }
        
        // Drop zone for creating supersets
        if (selectedExercisesForSuperset.isNotEmpty()) {
            item {
                SupersetDropZone(
                    isActive = selectedExercisesForSuperset.size >= 2,
                    exerciseCount = selectedExercisesForSuperset.size,
                    onDrop = onCreateSuperset,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        // Individual exercises
        itemsIndexed(
            items = individualExercises,
            key = { _, item -> "exercise_${item.exercise.id}" }
        ) { index, exerciseItem ->
            DraggableExerciseCard(
                exerciseItem = exerciseItem,
                index = index,
                isDragged = false,
                isDropTarget = false,
                isSelectedForSuperset = selectedExercisesForSuperset.contains(exerciseItem.exercise.id),
                onDragStart = { },
                onDragEnd = { },
                onDrag = { },
                onConfigurationChanged = onConfigurationChanged,
                onRemoveExercise = onRemoveExercise,
                onSelectForSuperset = onSelectExerciseForSuperset,
                onDeselectForSuperset = onDeselectExerciseForSuperset
            )
        }
    }
}