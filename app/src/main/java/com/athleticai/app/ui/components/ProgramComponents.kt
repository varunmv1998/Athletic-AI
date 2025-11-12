package com.athleticai.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.*
import com.athleticai.app.ui.viewmodels.WorkoutViewMode

/**
 * Card component for displaying a program in the discovery/browse view
 */
@Composable
fun ProgramCard(
    program: Program,
    onClick: () -> Unit,
    onEnroll: (() -> Unit)? = null,
    isEnrolled: Boolean = false,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with name and enrollment status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = program.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Goal and Level badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgramBadge(
                            text = program.goal.toDisplayString(),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        ProgramBadge(
                            text = program.experienceLevel.toDisplayString(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                if (isEnrolled) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("ENROLLED", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            // Description
            Text(
                text = program.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // Program details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ProgramDetailItem(
                    icon = MaterialSymbols.CALENDAR_MONTH,
                    label = "${program.durationWeeks} weeks"
                )
                ProgramDetailItem(
                    icon = MaterialSymbols.FITNESS_CENTER,
                    label = "${program.workoutsPerWeek}x/week"
                )
                if (program.isCustom) {
                    ProgramDetailItem(
                        icon = MaterialSymbols.PERSON,
                        label = "Custom"
                    )
                }
            }
            
            // Equipment required (if any)
            if (program.equipmentRequired.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Equipment Required:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        program.equipmentRequired.take(3).forEach { equipment ->
                            EquipmentChip(equipment = equipment)
                        }
                        if (program.equipmentRequired.size > 3) {
                            Text(
                                text = "+${program.equipmentRequired.size - 3} more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Action button
            if (onEnroll != null && !isEnrolled) {
                Button(
                    onClick = onEnroll,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.ADD_CIRCLE,
                        size = IconSizes.SMALL,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enroll in Program")
                }
            }
        }
    }
}

/**
 * Compact card for displaying active program status
 */
@Composable
fun ActiveProgramCard(
    currentState: CurrentProgramState,
    onResume: () -> Unit,
    onSkipDay: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onViewDetails,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentState.program.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Day ${currentState.enrollment.currentDay} of ${currentState.program.durationWeeks * 7}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Streak indicator
                if (currentState.streak > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.TRENDING_UP,
                                size = IconSizes.SMALL,
                                contentDescription = null
                            )
                            Text(
                                text = "${currentState.streak}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            
            // Progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { currentState.completionPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${currentState.completionPercentage.toInt()}% Complete • ${currentState.daysRemaining} days remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Today's workout
            currentState.currentProgramDay?.let { day ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today: ${day.name}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            if (day.targetMuscleGroups.isNotEmpty()) {
                                Text(
                                    text = day.targetMuscleGroups.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        
                        when (day.dayType) {
                            DayType.WORKOUT -> {
                                Button(
                                    onClick = onResume,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Start Workout")
                                }
                            }
                            DayType.REST -> {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Rest Day") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                            DayType.ACTIVE_RECOVERY -> {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Active Recovery") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSkipDay,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Skip Day")
                }
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Program")
                }
            }
        }
    }
}

/**
 * Badge component for displaying program attributes
 */
@Composable
private fun ProgramBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Item for displaying program details with icon
 */
@Composable
private fun ProgramDetailItem(
    icon: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MaterialSymbol(
            symbol = icon,
            size = IconSizes.SMALL,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Chip for displaying equipment
 */
@Composable
private fun EquipmentChip(
    equipment: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = equipment,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Toggle component for switching between Programs and Routines view
 */
@Composable
fun ProgramRoutineToggle(
    selectedMode: WorkoutViewMode,
    onModeChange: (WorkoutViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = when (selectedMode) {
        WorkoutViewMode.ROUTINES -> 0
        WorkoutViewMode.PROGRAMS -> 1
    }
    
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Tab(
            selected = selectedMode == WorkoutViewMode.ROUTINES,
            onClick = { onModeChange(WorkoutViewMode.ROUTINES) },
            text = { Text("Routines") },
            icon = {
                MaterialSymbol(
                    symbol = MaterialSymbols.LIST,
                    size = IconSizes.SMALL,
                    contentDescription = null
                )
            }
        )
        Tab(
            selected = selectedMode == WorkoutViewMode.PROGRAMS,
            onClick = { onModeChange(WorkoutViewMode.PROGRAMS) },
            text = { Text("Programs") },
            icon = {
                MaterialSymbol(
                    symbol = MaterialSymbols.CALENDAR_MONTH,
                    size = IconSizes.SMALL,
                    contentDescription = null
                )
            }
        )
    }
}

// Extension functions for display strings
private fun ProgramGoal.toDisplayString(): String = when (this) {
    ProgramGoal.FAT_LOSS -> "Fat Loss"
    ProgramGoal.MUSCLE_BUILDING -> "Muscle Building"
    ProgramGoal.GENERAL_FITNESS -> "General Fitness"
    ProgramGoal.STRENGTH -> "Strength"
    ProgramGoal.ENDURANCE -> "Endurance"
    ProgramGoal.ATHLETIC_PERFORMANCE -> "Athletic Performance"
    ProgramGoal.OTHER -> "Other"
}

private fun ExperienceLevel.toDisplayString(): String = when (this) {
    ExperienceLevel.BEGINNER -> "Beginner"
    ExperienceLevel.INTERMEDIATE -> "Intermediate"
    ExperienceLevel.ADVANCED -> "Advanced"
}

// Temporary FlowRow implementation until Compose Foundation adds it
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}