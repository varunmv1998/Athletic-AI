package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.models.AchievementCategory
import com.athleticai.app.ui.components.AchievementCard
import com.athleticai.app.ui.viewmodels.AchievementViewModel
import com.athleticai.app.ui.viewmodels.AchievementUiState
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_BACK,
                            size = IconSizes.STANDARD,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Stats
            item {
                AchievementStatsHeader(uiState)
            }
            
            // Achievement Categories
            AchievementCategory.values().forEach { category ->
                item {
                    CategoryHeader(category, viewModel.getCategoryProgress(category))
                }
                
                val categoryAchievements = viewModel.getAchievementsByCategory(category)
                items(categoryAchievements) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (categoryAchievements.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun AchievementStatsHeader(uiState: AchievementUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.STAR,
                    size = IconSizes.STANDARD,
                    contentDescription = "Total Points",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${uiState.totalPoints}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total Points",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    label = "Unlocked",
                    value = "${uiState.achievementCount}",
                    total = "${uiState.totalAchievementCount}"
                )
                
                StatItem(
                    label = "Completion",
                    value = "${if (uiState.totalAchievementCount > 0) (uiState.achievementCount * 100 / uiState.totalAchievementCount) else 0}%"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    total: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        
        if (total != null) {
            Text(
                text = "of $total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CategoryHeader(
    category: AchievementCategory,
    progress: Pair<Int, Int>
) {
    val (unlocked, total) = progress
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getCategoryDisplayName(category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "$unlocked of $total achievements unlocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // Progress indicator
            LinearProgressIndicator(
                progress = if (total > 0) unlocked.toFloat() / total else 0f,
                modifier = Modifier.width(60.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getCategoryDisplayName(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.CONSISTENCY -> "Consistency"
        AchievementCategory.PERFORMANCE -> "Performance"
        AchievementCategory.DIVERSITY -> "Exercise Diversity"
        AchievementCategory.SMART_TRAINING -> "Smart Training"
        AchievementCategory.BODY_COMPOSITION -> "Body Composition"
        AchievementCategory.MILESTONES -> "Milestones"
        AchievementCategory.MONTHLY -> "Monthly Challenges"
        AchievementCategory.CUMULATIVE_DAYS -> "Cumulative Days"
        AchievementCategory.PROGRAM_COMPLETION -> "Program Completion"
        AchievementCategory.SEASONAL -> "Seasonal"
        AchievementCategory.HOLIDAY_SPECIALS -> "Holiday Specials"
        AchievementCategory.TIME_BASED_CHALLENGES -> "Time-Based Challenges"
    }
}
