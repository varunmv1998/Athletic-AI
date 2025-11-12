package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.WorkoutRoutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoutineCard(
    routine: WorkoutRoutine,
    exerciseCount: Int = 0,
    estimatedDuration: String? = null,
    onStartRoutine: () -> Unit,
    onShowOptions: (() -> Unit)? = null,
    showRecentLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onStartRoutine,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Routine icon
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (showRecentLabel) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.FITNESS_CENTER,
                        size = IconSizes.LARGE,
                        contentDescription = null,
                        tint = if (showRecentLabel) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Routine info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name and recent label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (showRecentLabel) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "Recent",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Exercise count and duration
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (exerciseCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.LIST,
                                size = IconSizes.SMALL,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$exerciseCount exercises",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    estimatedDuration?.let { duration ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.SCHEDULE,
                                size = IconSizes.SMALL,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = duration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Last performed info
                routine.lastPerformed?.let { timestamp ->
                    val date = Date(timestamp)
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    Text(
                        text = "Last: ${formatter.format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: run {
                    if (!showRecentLabel) {
                        Text(
                            text = "Never performed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Notes preview
                if (routine.notes.isNotEmpty()) {
                    Text(
                        text = routine.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Options button (optional)
                onShowOptions?.let { optionsAction ->
                    IconButton(onClick = optionsAction) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.MORE_VERT,
                            size = IconSizes.STANDARD,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Start button
                FilledTonalButton(
                    onClick = onStartRoutine,
                    modifier = Modifier.size(width = 80.dp, height = 40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.PLAY_ARROW,
                        size = IconSizes.SMALL,
                        contentDescription = "Start routine"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CompactRoutineCard(
    routine: WorkoutRoutine,
    onStartRoutine: () -> Unit,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onStartRoutine,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Routine icon (smaller)
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.FITNESS_CENTER,
                        size = IconSizes.STANDARD,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Routine info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                routine.lastPerformed?.let { timestamp ->
                    val date = Date(timestamp)
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    Text(
                        text = "Last: ${formatter.format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: run {
                    Text(
                        text = "New routine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Start button
            FilledTonalButton(
                onClick = onStartRoutine,
                modifier = Modifier.size(width = 80.dp, height = 36.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.PLAY_ARROW,
                    size = IconSizes.SMALL,
                    contentDescription = "Start routine"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}