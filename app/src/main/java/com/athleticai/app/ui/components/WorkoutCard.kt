package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.CustomWorkout
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutCard(
    workout: CustomWorkout,
    exerciseCount: Int,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.MORE_VERT,
                            size = IconSizes.STANDARD,
                            contentDescription = "Workout options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Start Workout") },
                            onClick = {
                                onStartWorkout()
                                showMenu = false
                            },
                            leadingIcon = {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.PLAY_ARROW,
                                    size = IconSizes.SMALL,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEditClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.EDIT,
                                    size = IconSizes.SMALL,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        Divider()
                        
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.DELETE,
                                    size = IconSizes.SMALL,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Description
            if (workout.description.isNotBlank()) {
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkoutStat(
                        icon = MaterialSymbols.FITNESS_CENTER,
                        value = exerciseCount.toString(),
                        label = "Exercises"
                    )
                    
                    WorkoutStat(
                        icon = MaterialSymbols.SCHEDULE,
                        value = "${workout.estimatedDurationMinutes}min",
                        label = "Duration"
                    )
                    
                    WorkoutStat(
                        icon = MaterialSymbols.CALENDAR_MONTH,
                        value = formatDate(workout.createdDate),
                        label = "Created"
                    )
                }
                
                Button(
                    onClick = onStartWorkout,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.PLAY_ARROW,
                        size = IconSizes.SMALL,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
                }
            }
        }
    }
}

@Composable
private fun WorkoutStat(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MaterialSymbol(
            symbol = icon,
            size = IconSizes.SMALL,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$value $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}