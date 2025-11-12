package com.athleticai.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal

data class MeasurementCardData(
    val title: String,
    val icon: String,
    val currentValue: String?,
    val targetValue: String?,
    val progress: Float,
    val unit: String,
    val type: MeasurementCardType
)

enum class MeasurementCardType {
    WEIGHT,
    BODY_FAT,
    ADD_MEASUREMENT,
    HISTORY
}

@Composable
fun BodyMeasurementsCarousel(
    latestMeasurement: BodyMeasurement?,
    activeGoals: List<Goal>,
    onCardClick: (MeasurementCardType) -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = buildMeasurementCards(latestMeasurement, activeGoals)
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(cards) { card ->
            MeasurementCard(
                data = card,
                onClick = { onCardClick(card.type) }
            )
        }
    }
}

@Composable
private fun MeasurementCard(
    data: MeasurementCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (data.type) {
                MeasurementCardType.ADD_MEASUREMENT -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaterialSymbol(
                    symbol = data.icon,
                    contentDescription = null,
                    tint = when (data.type) {
                        MeasurementCardType.ADD_MEASUREMENT -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = when (data.type) {
                        MeasurementCardType.ADD_MEASUREMENT -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            when (data.type) {
                MeasurementCardType.ADD_MEASUREMENT -> {
                    Text(
                        text = "Tap to add\nmeasurement",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                MeasurementCardType.HISTORY -> {
                    Text(
                        text = "View all\nmeasurements",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                else -> {
                    if (data.currentValue != null) {
                        Text(
                            text = "${data.currentValue} ${data.unit}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (data.targetValue != null) {
                            ProgressIndicator(
                                progress = data.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )
                            Text(
                                text = "Goal: ${data.targetValue} ${data.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "No data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

private fun buildMeasurementCards(
    latestMeasurement: BodyMeasurement?,
    activeGoals: List<Goal>
): List<MeasurementCardData> {
    val cards = mutableListOf<MeasurementCardData>()
    
    // Weight Progress Card
    val weightGoal = activeGoals.find { it.metricType == "weightKg" }
    val weightProgress = if (latestMeasurement?.weightKg != null && weightGoal != null) {
        calculateProgress(latestMeasurement.weightKg, weightGoal.targetValue, weightGoal.metricType)
    } else 0f
    
    cards.add(
        MeasurementCardData(
            title = "Weight",
            icon = MaterialSymbols.MONITOR_WEIGHT,
            currentValue = latestMeasurement?.weightKg?.let { "%.1f".format(it) },
            targetValue = weightGoal?.targetValue?.let { "%.1f".format(it) },
            progress = weightProgress,
            unit = "kg",
            type = MeasurementCardType.WEIGHT
        )
    )
    
    // Body Fat Progress Card
    val bodyFatGoal = activeGoals.find { it.metricType == "bodyFatPct" }
    val bodyFatProgress = if (latestMeasurement?.bodyFatPct != null && bodyFatGoal != null) {
        calculateProgress(latestMeasurement.bodyFatPct, bodyFatGoal.targetValue, bodyFatGoal.metricType)
    } else 0f
    
    cards.add(
        MeasurementCardData(
            title = "Body Fat",
            icon = MaterialSymbols.PERCENT,
            currentValue = latestMeasurement?.bodyFatPct?.let { "%.1f".format(it) },
            targetValue = bodyFatGoal?.targetValue?.let { "%.1f".format(it) },
            progress = bodyFatProgress,
            unit = "%",
            type = MeasurementCardType.BODY_FAT
        )
    )
    
    // Add Measurement Card
    cards.add(
        MeasurementCardData(
            title = "Add Data",
            icon = MaterialSymbols.ADD,
            currentValue = null,
            targetValue = null,
            progress = 0f,
            unit = "",
            type = MeasurementCardType.ADD_MEASUREMENT
        )
    )
    
    // History Card
    cards.add(
        MeasurementCardData(
            title = "History",
            icon = MaterialSymbols.HISTORY,
            currentValue = null,
            targetValue = null,
            progress = 0f,
            unit = "",
            type = MeasurementCardType.HISTORY
        )
    )
    
    return cards
}

private fun calculateProgress(current: Double, target: Double, metricType: String): Float {
    return when (metricType) {
        "weightKg", "bodyFatPct", "waistCm" -> {
            // For metrics where lower is typically better
            val range = kotlin.math.abs(target - current)
            if (range == 0.0) 1f else (1f - (kotlin.math.abs(target - current) / target).toFloat()).coerceIn(0f, 1f)
        }
        else -> 0f
    }
}