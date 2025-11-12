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
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementGuideScreen(
    viewModel: AchievementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievement Guide") },
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
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.STAR,
                            size = IconSizes.EXTRA_LARGE,
                            contentDescription = "Achievement Guide",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Achievement Guide",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Text(
                            text = "Discover all the achievements you can unlock on your fitness journey",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
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



