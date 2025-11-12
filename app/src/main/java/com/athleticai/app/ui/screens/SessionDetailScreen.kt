package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.WorkoutSet
import com.athleticai.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.map

@Composable
fun SessionDetailScreen(
    sessionId: String,
    workoutRepository: WorkoutRepository
) {
    // Observe sets for this session
    val sets by workoutRepository.getSetsForSession(sessionId).collectAsState(initial = emptyList())
    val groups = sets.groupBy { it.exerciseId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groups.entries.toList()) { (exerciseId, exSets) ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(exerciseId.replace('_',' '), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    exSets.sortedBy { it.setNumber }.forEach { s ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Set ${s.setNumber}")
                            Text("${s.weight} x ${s.reps} @ RPE ${s.rpe}")
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.padding(8.dp)) }
    }
}

