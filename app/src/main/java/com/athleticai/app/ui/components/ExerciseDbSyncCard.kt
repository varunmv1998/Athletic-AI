package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseDbSyncCard(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val syncStatus by viewModel.exerciseDbSyncStatus.collectAsState()
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exercise Database Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (state.exerciseDbSyncStatus.exerciseCount > 0) {
                        "Downloaded ${state.exerciseDbSyncStatus.exerciseCount} exercises"
                    } else {
                        "Download 1,500+ exercises from ExerciseDB"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Last sync time
                state.exerciseDbSyncStatus.lastSyncTime?.let { lastSync ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    Text(
                        text = "Last sync: ${dateFormat.format(Date(lastSync))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (syncStatus.isInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
            }
        }
        
        // Progress indicator during sync
        if (syncStatus.isInProgress && syncStatus.progress > 0f) {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                LinearProgressIndicator(
                    progress = { syncStatus.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(syncStatus.progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Error message
        syncStatus.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.exerciseDbSyncStatus.exerciseCount == 0) {
                Button(
                    onClick = { viewModel.triggerExerciseDbSync() },
                    enabled = !syncStatus.isInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.DOWNLOAD,
                            size = IconSizes.SMALL,
                            contentDescription = null
                        )
                        Text("Download Exercises")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.triggerExerciseDbSync() },
                    enabled = !syncStatus.isInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.REPEAT,
                            size = IconSizes.SMALL,
                            contentDescription = null
                        )
                        Text("Update")
                    }
                }
                
                if (state.exerciseDbSyncStatus.exerciseCount > 0) {
                    OutlinedButton(
                        onClick = { viewModel.clearExerciseDbData() },
                        enabled = !syncStatus.isInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.DELETE,
                                size = IconSizes.SMALL,
                                contentDescription = null
                            )
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}