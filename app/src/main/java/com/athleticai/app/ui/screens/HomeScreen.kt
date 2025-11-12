package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.RecentAchievementsPreview
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.viewmodels.ProgressViewModel
import com.athleticai.app.ui.viewmodels.HomeViewModel
import com.athleticai.app.ui.viewmodels.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    progressViewModel: ProgressViewModel,
    routineViewModel: RoutineViewModel,
    onNavigateToWorkout: () -> Unit
) {
    val progressState by progressViewModel.state.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val routineState by routineViewModel.uiState.collectAsState()


    PullToRefreshBox(
        isRefreshing = homeState.isRefreshing,
        onRefresh = { homeViewModel.refreshData() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header
            Text(
                text = "Athletic AI",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Track Your Fitness Journey",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            
            // Motivational Quote Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"${homeState.currentQuote?.text ?: "The iron never lies. You are what you lift."}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Quick Start Workout Card
            if (routineState.routines.isNotEmpty() || routineState.recentRoutines.isNotEmpty()) {
                QuickStartCard(
                    recentRoutine = routineState.recentRoutines.firstOrNull(),
                    totalRoutines = routineState.routines.size,
                    onStartWorkout = onNavigateToWorkout
                )
            } else {
                GetStartedCard(
                    onNavigateToWorkout = onNavigateToWorkout
                )
            }
            
            // Recent Achievements Preview
            RecentAchievementsPreview()
            
            // Workout Stats
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quick Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                            Text("Current Streak")
                            Text("${progressState.currentStreak} days", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) 
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                            Text("Weekly Volume")
                            Text("${"%.0f".format(progressState.sevenDayVolume)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) 
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GetStartedCard(
    onNavigateToWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MaterialSymbol(
                symbol = MaterialSymbols.FITNESS_CENTER,
                size = IconSizes.EXTRA_LARGE,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Welcome to Athletic AI!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Start your fitness journey by creating workout routines.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Button(
                onClick = onNavigateToWorkout,
                modifier = Modifier.fillMaxWidth()
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.FITNESS_CENTER,
                    size = IconSizes.STANDARD,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get Started with Workouts",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun QuickStartCard(
    recentRoutine: com.athleticai.app.data.database.entities.WorkoutRoutine?,
    totalRoutines: Int,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ready to Workout?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                
                MaterialSymbol(
                    symbol = MaterialSymbols.FITNESS_CENTER,
                    size = IconSizes.LARGE,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (recentRoutine != null) {
                Text(
                    text = "Latest routine: ${recentRoutine.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Text(
                text = "$totalRoutines routines available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
            
            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.PLAY_ARROW,
                    size = IconSizes.STANDARD,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Workout")
            }
        }
    }
}

