package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.ProfileModal
import com.athleticai.app.ui.components.MeasurementHistoryModal
import com.athleticai.app.ui.components.MeasurementHistoryType
import com.athleticai.app.ui.components.MeasurementCardType
import com.athleticai.app.ui.viewmodels.ProgressViewModel
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal

data class ProfileMetricCard(
    val title: String,
    val icon: String,
    val currentValue: String?,
    val targetValue: String?,
    val unit: String,
    val type: MeasurementCardType,
    val isEditable: Boolean = true,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    progressViewModel: ProgressViewModel,
    onNavigateToSettings: () -> Unit
) {
    val progressState by progressViewModel.state.collectAsState()
    
    var showProfileModal by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var selectedHistoryType by remember { mutableStateOf(MeasurementHistoryType.WEIGHT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.SETTINGS,
                            size = IconSizes.STANDARD,
                            contentDescription = "Settings"
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
            // Personal Information Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showProfileModal = true }) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.EDIT,
                                    size = IconSizes.SMALL,
                                    contentDescription = "Edit Profile"
                                )
                            }
                        }
                        
                        progressState.latestMeasurement?.let { measurement ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (measurement.heightCm != null && measurement.weightKg != null) {
                                    val heightM = measurement.heightCm / 100.0
                                    val bmi = measurement.weightKg / (heightM * heightM)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "BMI",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "%.1f".format(bmi),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                if (measurement.heightCm != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Age",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "—",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } ?: run {
                            Text(
                                text = "Add your measurements to see your profile information",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    }
                }
            }

            // Body Measurements Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Body Measurements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = { 
                                    selectedHistoryType = MeasurementHistoryType.WEIGHT
                                    showHistoryModal = true 
                                }
                            ) {
                                Text("View History")
                            }
                        }

                        val measurementCards = buildProfileMeasurementCards(
                            latestMeasurement = progressState.latestMeasurement,
                            activeGoals = progressState.activeGoals,
                            onCardClick = { showProfileModal = true }
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height(256.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(measurementCards) { card ->
                                ProfileMeasurementCard(card = card)
                            }
                        }
                    }
                }
            }

            // Goals & Progress Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Goals & Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        if (progressState.activeGoals.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.STAR,
                                    size = IconSizes.LARGE,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No active goals yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Set a goal to track your progress",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { showProfileModal = true }) {
                                    Text("Set Your First Goal")
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                progressState.activeGoals.forEach { goal ->
                                    GoalProgressItem(
                                        goal = goal,
                                        latestMeasurement = progressState.latestMeasurement
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Profile Modal for adding measurements
    ProfileModal(
        isVisible = showProfileModal,
        latestMeasurement = progressState.latestMeasurement,
        activeGoals = progressState.activeGoals,
        onDismiss = { showProfileModal = false },
        onAddMeasurement = { weight, height, bodyFat, waist ->
            progressViewModel.addMeasurement(weight, height, bodyFat, waist)
        },
        onSetGoal = { metricType, targetValue, targetDate ->
            progressViewModel.addGoal(metricType, targetValue, targetDate)
        }
    )

    // History Modal
    MeasurementHistoryModal(
        isVisible = showHistoryModal,
        historyType = selectedHistoryType,
        measurements = progressState.allMeasurements,
        goals = progressState.activeGoals,
        onDismiss = { showHistoryModal = false }
    )
}

@Composable
private fun ProfileMeasurementCard(
    card: ProfileMetricCard,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = card.onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Main content
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    // Main icon
                    MaterialSymbol(
                        symbol = card.icon,
                        size = IconSizes.STANDARD,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Values section
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = card.currentValue ?: "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (card.targetValue != null) {
                        Text(
                            text = "Target: ${card.targetValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Edit icon - positioned in top-right corner
            if (card.isEditable) {
                MaterialSymbol(
                    symbol = MaterialSymbols.EDIT,
                    size = IconSizes.SMALL,
                    contentDescription = "Edit ${card.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun GoalProgressItem(
    goal: Goal,
    latestMeasurement: BodyMeasurement?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (goal.metricType) {
                    "weightKg" -> "Weight Goal"
                    "bodyFatPct" -> "Body Fat Goal"
                    "waistCm" -> "Waist Goal"
                    else -> "Goal"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${goal.targetValue} ${
                    when (goal.metricType) {
                        "weightKg" -> "kg"
                        "bodyFatPct" -> "%"
                        "waistCm" -> "cm"
                        else -> ""
                    }
                }",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        val currentValue = when (goal.metricType) {
            "weightKg" -> latestMeasurement?.weightKg
            "bodyFatPct" -> latestMeasurement?.bodyFatPct
            "waistCm" -> latestMeasurement?.waistCm
            else -> null
        }
        
        val progress = if (currentValue != null) {
            (currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
        } else 0f
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun buildProfileMeasurementCards(
    latestMeasurement: BodyMeasurement?,
    activeGoals: List<Goal>,
    onCardClick: () -> Unit
): List<ProfileMetricCard> {
    val weightGoal = activeGoals.find { it.metricType == "weightKg" }
    val bodyFatGoal = activeGoals.find { it.metricType == "bodyFatPct" }
    val waistGoal = activeGoals.find { it.metricType == "waistCm" }

    return listOf(
        ProfileMetricCard(
            title = "Weight",
            icon = MaterialSymbols.MONITOR_WEIGHT,
            currentValue = latestMeasurement?.weightKg?.let { "%.1f kg".format(it) },
            targetValue = weightGoal?.targetValue?.let { "%.1f kg".format(it) },
            unit = "kg",
            type = MeasurementCardType.WEIGHT,
            isEditable = true,
            onClick = onCardClick
        ),
        ProfileMetricCard(
            title = "Body Fat",
            icon = MaterialSymbols.PERCENT,
            currentValue = latestMeasurement?.bodyFatPct?.let { "%.1f%%".format(it) },
            targetValue = bodyFatGoal?.targetValue?.let { "%.1f%%".format(it) },
            unit = "%",
            type = MeasurementCardType.BODY_FAT,
            isEditable = true,
            onClick = onCardClick
        ),
        ProfileMetricCard(
            title = "Height",
            icon = MaterialSymbols.HEIGHT,
            currentValue = latestMeasurement?.heightCm?.let { "%.0f cm".format(it) },
            targetValue = null,
            unit = "cm",
            type = MeasurementCardType.ADD_MEASUREMENT,
            isEditable = true,
            onClick = onCardClick
        ),
        ProfileMetricCard(
            title = "Waist",
            icon = MaterialSymbols.STRAIGHTEN,
            currentValue = latestMeasurement?.waistCm?.let { "%.1f cm".format(it) },
            targetValue = waistGoal?.targetValue?.let { "%.1f cm".format(it) },
            unit = "cm",
            type = MeasurementCardType.ADD_MEASUREMENT,
            isEditable = true,
            onClick = onCardClick
        )
    )
}