package com.athleticai.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athleticai.app.data.database.entities.*
import com.athleticai.app.ui.viewmodels.ProgramDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programId: String,
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    viewModel: ProgramDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(programId) {
        viewModel.loadProgram(programId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.program?.name ?: "Program Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isEnrolled) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.enrollInProgram()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Enroll")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Program Overview Card
                item {
                    ProgramOverviewCard(
                        program = uiState.program,
                        enrollment = uiState.enrollment,
                        currentDay = uiState.currentDay
                    )
                }
                
                // Week Headers and Day Cards
                uiState.daysByWeek.forEach { (week, days) ->
                    item {
                        WeekHeader(
                            weekNumber = week,
                            isCurrentWeek = week == uiState.currentWeek
                        )
                    }
                    
                    items(days) { day ->
                        ProgramDayCard(
                            day = day,
                            exercises = uiState.exercisesByDay[day.id] ?: emptyList(),
                            exerciseDetails = uiState.exerciseDetails,
                            isCurrentDay = day.dayNumber == uiState.currentDay,
                            isCompleted = uiState.completedDays.contains(day.id),
                            isSkipped = uiState.skippedDays.contains(day.id),
                            isLocked = !uiState.isEnrolled || (uiState.currentDay != null && day.dayNumber > uiState.currentDay!!),
                            onStartWorkout = {
                                if (day.dayNumber == uiState.currentDay) {
                                    onStartWorkout(day.id)
                                }
                            },
                            onSkipDay = {
                                if (day.dayNumber == uiState.currentDay) {
                                    scope.launch {
                                        viewModel.skipCurrentDay()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramOverviewCard(
    program: Program?,
    enrollment: UserProgramEnrollment?,
    currentDay: Int?
) {
    program?.let {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(
                        icon = Icons.Default.DateRange,
                        label = "${program.durationWeeks} weeks"
                    )
                    InfoChip(
                        icon = Icons.Default.Star,
                        label = "${program.workoutsPerWeek}x/week"
                    )
                    InfoChip(
                        icon = Icons.Default.KeyboardArrowRight,
                        label = program.goal.name.replace("_", " ")
                    )
                }
                
                enrollment?.let { enr ->
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = (currentDay ?: 0).toFloat() / (program.durationWeeks * 7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = "Day ${currentDay ?: 0} of ${program.durationWeeks * 7}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun WeekHeader(
    weekNumber: Int,
    isCurrentWeek: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrentWeek) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "Week $weekNumber",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isCurrentWeek) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrentWeek) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProgramDayCard(
    day: ProgramDay,
    exercises: List<ProgramDayExercise>,
    exerciseDetails: Map<String, Exercise>,
    isCurrentDay: Boolean,
    isCompleted: Boolean,
    isSkipped: Boolean,
    isLocked: Boolean,
    onStartWorkout: () -> Unit,
    onSkipDay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isLocked && isCurrentDay) Modifier.clickable { onStartWorkout() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentDay -> MaterialTheme.colorScheme.primaryContainer
                isCompleted -> MaterialTheme.colorScheme.tertiaryContainer
                isSkipped -> MaterialTheme.colorScheme.surfaceVariant
                isLocked -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Day ${day.dayNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        } else if (isSkipped) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Skipped",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Text(
                        text = day.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium
                    )
                    
                    Text(
                        text = day.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (day.dayType == DayType.WORKOUT) {
                    DayTypeChip(dayType = day.dayType)
                }
            }
            
            // Show exercises for workout days
            if (day.dayType == DayType.WORKOUT && exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercises.take(5).forEach { programExercise ->
                        ExerciseRow(
                            programExercise = programExercise,
                            exercise = exerciseDetails[programExercise.exerciseId]
                        )
                    }
                    
                    if (exercises.size > 5) {
                        Text(
                            text = "+${exercises.size - 5} more exercises",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action buttons for current day
            if (isCurrentDay && !isCompleted && !isLocked) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (day.dayType == DayType.WORKOUT) {
                        Button(
                            onClick = onStartWorkout,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Workout")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onSkipDay,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skip Day")
                    }
                }
            }
        }
    }
}

@Composable
private fun DayTypeChip(dayType: DayType) {
    val iconAndColor = when (dayType) {
        DayType.WORKOUT -> Icons.Default.Star to MaterialTheme.colorScheme.primary
        DayType.REST -> Icons.Default.Home to MaterialTheme.colorScheme.secondary
        DayType.ACTIVE_RECOVERY -> Icons.Default.Refresh to MaterialTheme.colorScheme.tertiary
        DayType.OPTIONAL -> Icons.Default.Close to MaterialTheme.colorScheme.surfaceVariant
        DayType.DELOAD -> Icons.Default.Close to MaterialTheme.colorScheme.tertiary
    }
    val icon = iconAndColor.first
    val color = iconAndColor.second
    
    AssistChip(
        onClick = { },
        label = { Text(dayType.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.height(24.dp)
    )
}

@Composable
private fun ExerciseRow(
    programExercise: ProgramDayExercise,
    exercise: Exercise?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise?.name ?: "Exercise ${programExercise.exerciseId}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (programExercise.isCardio) {
                Text(
                    text = "${programExercise.duration} min • ${programExercise.intensity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "${programExercise.sets} sets × ${programExercise.reps} reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (programExercise.targetRPE != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "RPE ${programExercise.targetRPE}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}