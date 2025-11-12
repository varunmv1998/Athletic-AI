package com.athleticai.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

data class TrendData(
    val value: Double,
    val date: LocalDateTime
)

enum class MeasurementHistoryType(
    val title: String,
    val icon: String,
    val unit: String,
    val color: Color
) {
    WEIGHT("Weight History", MaterialSymbols.MONITOR_WEIGHT, "kg", Color(0xFF4CAF50)),
    BODY_FAT("Body Fat History", MaterialSymbols.PERCENT, "%", Color(0xFF2196F3)),
    WAIST("Waist History", MaterialSymbols.STRAIGHTEN, "cm", Color(0xFF9C27B0)),
    BMI("BMI History", MaterialSymbols.FITNESS_CENTER, "", Color(0xFF607D8B))
}

@Composable
fun MeasurementHistoryModal(
    isVisible: Boolean,
    historyType: MeasurementHistoryType,
    measurements: List<BodyMeasurement>,
    goals: List<Goal>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    val trendData = extractTrendData(measurements, historyType)
    val goal = goals.find { 
        when (historyType) {
            MeasurementHistoryType.WEIGHT -> it.metricType == "weightKg"
            MeasurementHistoryType.BODY_FAT -> it.metricType == "bodyFatPct"
            MeasurementHistoryType.WAIST -> it.metricType == "waistCm"
            MeasurementHistoryType.BMI -> false // BMI doesn't have goals
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.ARROW_BACK,
                                contentDescription = "Back"
                            )
                        }
                        MaterialSymbol(
                            symbol = historyType.icon,
                            contentDescription = null,
                            tint = historyType.color
                        )
                        Text(
                            text = historyType.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Trend Chart
                    item {
                        TrendChart(
                            trendData = trendData,
                            historyType = historyType,
                            goal = goal
                        )
                    }
                    
                    // Statistics
                    item {
                        StatisticsSection(
                            trendData = trendData,
                            historyType = historyType,
                            goal = goal
                        )
                    }
                    
                    // History List
                    item {
                        Text(
                            text = "Measurement History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    items(trendData.reversed()) { data ->
                        HistoryListItem(
                            data = data,
                            historyType = historyType
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChart(
    trendData: List<TrendData>,
    historyType: MeasurementHistoryType,
    goal: Goal?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Trend Chart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (trendData.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawTrendChart(
                            trendData = trendData,
                            goalValue = goal?.targetValue,
                            color = historyType.color,
                            canvasSize = size
                        )
                    }
                }
                
                // Chart legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(
                        color = historyType.color,
                        label = historyType.title.replace(" History", "")
                    )
                    if (goal != null) {
                        LegendItem(
                            color = Color.Red,
                            label = "Goal"
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .wrapContentSize()
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatisticsSection(
    trendData: List<TrendData>,
    historyType: MeasurementHistoryType,
    goal: Goal?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (trendData.isNotEmpty()) {
                val current = trendData.lastOrNull()?.value
                val previous = trendData.getOrNull(trendData.size - 2)?.value
                val min = trendData.minOfOrNull { it.value }
                val max = trendData.maxOfOrNull { it.value }
                val avg = trendData.map { it.value }.average()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem(
                        title = "Current",
                        value = current?.let { "%.1f".format(it) } ?: "—",
                        unit = historyType.unit
                    )
                    StatisticItem(
                        title = "Average",
                        value = "%.1f".format(avg),
                        unit = historyType.unit
                    )
                    StatisticItem(
                        title = "Range",
                        value = "${min?.let { "%.1f".format(it) } ?: "—"} - ${max?.let { "%.1f".format(it) } ?: "—"}",
                        unit = historyType.unit
                    )
                }
                
                // Progress toward goal
                if (goal != null && current != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = calculateGoalProgress(current, goal.targetValue, historyType)
                    val (icon, color, text) = when {
                        progress > 0 -> Triple(MaterialSymbols.KEYBOARD_ARROW_UP, Color(0xFF4CAF50), "On track")
                        progress == 0.0 -> Triple(MaterialSymbols.CLOSE, Color(0xFF9E9E9E), "No change")
                        else -> Triple(MaterialSymbols.KEYBOARD_ARROW_DOWN, Color(0xFFF44336), "Needs improvement")
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialSymbol(
                            symbol = icon,
                            contentDescription = null,
                            tint = color,
                            size = 16.dp
                        )
                        Text(
                            text = "Goal Progress: $text",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
                
                // Trend indicator
                if (previous != null && current != null) {
                    val change = current - previous
                    val changePercent = (change / previous) * 100
                    val (icon, color, direction) = when {
                        change > 0 -> Triple(MaterialSymbols.KEYBOARD_ARROW_UP, Color(0xFFF44336), "increasing")
                        change < 0 -> Triple(MaterialSymbols.KEYBOARD_ARROW_DOWN, Color(0xFF4CAF50), "decreasing")
                        else -> Triple(MaterialSymbols.CLOSE, Color(0xFF9E9E9E), "stable")
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialSymbol(
                            symbol = icon,
                            contentDescription = null,
                            tint = color,
                            size = 16.dp
                        )
                        Text(
                            text = "Trend: ${direction.replaceFirstChar { it.uppercase() }} (${if (change > 0) "+" else ""}${"%.1f".format(change)} ${historyType.unit})",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
            } else {
                Text(
                    text = "No data available for statistics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HistoryListItem(
    data: TrendData,
    historyType: MeasurementHistoryType
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "${"%.1f".format(data.value)} ${historyType.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = data.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            MaterialSymbol(
                symbol = historyType.icon,
                contentDescription = null,
                tint = historyType.color,
                size = 20.dp
            )
        }
    }
}

private fun DrawScope.drawTrendChart(
    trendData: List<TrendData>,
    goalValue: Double?,
    color: Color,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    if (trendData.isEmpty()) return
    
    val padding = 40f
    val chartWidth = canvasSize.width - (padding * 2)
    val chartHeight = canvasSize.height - (padding * 2)
    
    val minValue = (trendData.minOfOrNull { it.value } ?: 0.0) * 0.95
    val maxValue = (trendData.maxOfOrNull { it.value } ?: 100.0) * 1.05
    val valueRange = maxValue - minValue
    
    val points = trendData.mapIndexed { index, data ->
        val x = padding + (index.toFloat() / (trendData.size - 1).coerceAtLeast(1)) * chartWidth
        val y = padding + (1f - ((data.value - minValue) / valueRange).toFloat()) * chartHeight
        Offset(x, y)
    }
    
    // Draw goal line if available
    goalValue?.let { goal ->
        val goalY = padding + (1f - ((goal - minValue) / valueRange).toFloat()) * chartHeight
        drawLine(
            color = Color.Red,
            start = Offset(padding, goalY),
            end = Offset(canvasSize.width - padding, goalY),
            strokeWidth = 2.dp.toPx()
        )
    }
    
    // Draw trend line
    if (points.size > 1) {
        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
    
    // Draw data points
    points.forEach { point ->
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = point
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = point
        )
    }
}

private fun extractTrendData(
    measurements: List<BodyMeasurement>,
    historyType: MeasurementHistoryType
): List<TrendData> {
    return measurements.mapNotNull { measurement ->
        val value = when (historyType) {
            MeasurementHistoryType.WEIGHT -> measurement.weightKg
            MeasurementHistoryType.BODY_FAT -> measurement.bodyFatPct
            MeasurementHistoryType.WAIST -> measurement.waistCm
            MeasurementHistoryType.BMI -> {
                if (measurement.weightKg != null && measurement.heightCm != null) {
                    val heightM = measurement.heightCm / 100.0
                    measurement.weightKg / (heightM * heightM)
                } else null
            }
        }
        
        value?.let { TrendData(it, measurement.date) }
    }.sortedBy { it.date }
}

private fun calculateGoalProgress(
    current: Double,
    target: Double,
    historyType: MeasurementHistoryType
): Double {
    return when (historyType) {
        MeasurementHistoryType.WEIGHT, MeasurementHistoryType.BODY_FAT, MeasurementHistoryType.WAIST -> {
            // For these metrics, typically lower is better, but depends on goal direction
            if (target < current) {
                // Goal is to reduce
                if (current <= target) 1.0 else (target / current) - 1.0
            } else {
                // Goal is to increase
                if (current >= target) 1.0 else (current / target) - 1.0
            }
        }
        MeasurementHistoryType.BMI -> 0.0 // BMI doesn't have direct goals
    }
}