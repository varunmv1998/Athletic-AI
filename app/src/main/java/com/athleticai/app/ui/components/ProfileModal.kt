package com.athleticai.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MeasurementGridItem(
    val title: String,
    val icon: String,
    val currentValue: String?,
    val targetValue: String?,
    val progress: Float,
    val unit: String,
    val isEditable: Boolean = true,
    val onClick: () -> Unit = {}
)

@Composable
fun ProfileModal(
    isVisible: Boolean,
    latestMeasurement: BodyMeasurement?,
    activeGoals: List<Goal>,
    onDismiss: () -> Unit,
    onAddMeasurement: (Double?, Double?, Double?, Double?) -> Unit,
    onSetGoal: (String, Double, LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedMetricType by remember { mutableStateOf("") }
    
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
                    Text(
                        text = "Body Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.CLOSE,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Body Measurements Grid
                    item {
                        Text(
                            text = "Body Measurements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    item {
                        BodyMeasurementsGrid(
                            latestMeasurement = latestMeasurement,
                            activeGoals = activeGoals,
                            onEditClick = { metricType ->
                                selectedMetricType = metricType
                                showGoalDialog = true
                            }
                        )
                    }
                    
                    // Targets Section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Targets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    item {
                        TargetsSection(
                            activeGoals = activeGoals,
                            onSetGoal = { metricType ->
                                selectedMetricType = metricType
                                showGoalDialog = true
                            }
                        )
                    }
                    
                    // Action Buttons
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showMeasurementDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.ADD,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Measurement")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Measurement Dialog
    if (showMeasurementDialog) {
        MeasurementDialog(
            onDismiss = { showMeasurementDialog = false },
            onSave = { weight, height, bodyFat, waist ->
                onAddMeasurement(weight, height, bodyFat, waist)
                showMeasurementDialog = false
            }
        )
    }
    
    // Goal Dialog
    if (showGoalDialog) {
        GoalDialog(
            metricType = selectedMetricType,
            onDismiss = { showGoalDialog = false },
            onSave = { metricType, targetValue, targetDate ->
                onSetGoal(metricType, targetValue, targetDate)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun BodyMeasurementsGrid(
    latestMeasurement: BodyMeasurement?,
    activeGoals: List<Goal>,
    onEditClick: (String) -> Unit
) {
    val items = buildMeasurementGridItems(latestMeasurement, activeGoals, onEditClick)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    MeasurementGridCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number of items
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MeasurementGridCard(
    item: MeasurementGridItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(enabled = item.isEditable) { item.onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MaterialSymbol(
                        symbol = item.icon,
                        contentDescription = null,
                        size = 16.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (item.isEditable) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.EDIT,
                        contentDescription = "Edit",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (item.currentValue != null) {
                Text(
                    text = "${item.currentValue} ${item.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (item.targetValue != null && item.progress > 0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Goal: ${item.targetValue} ${item.unit}",
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

@Composable
private fun TargetsSection(
    activeGoals: List<Goal>,
    onSetGoal: (String) -> Unit
) {
    val targetTypes = listOf(
        Triple("weightKg", "Weight Goal", "kg"),
        Triple("bodyFatPct", "Body Fat Goal", "%"),
        Triple("waistCm", "Waist Goal", "cm")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        targetTypes.forEach { (metricType, title, unit) ->
            val goal = activeGoals.find { it.metricType == metricType }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetGoal(metricType) },
                colors = CardDefaults.cardColors(
                    containerColor = if (goal != null) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface
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
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (goal != null) {
                                "${goal.targetValue} $unit by ${goal.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}"
                            } else {
                                "Tap to set target"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    MaterialSymbol(
                        symbol = if (goal != null) MaterialSymbols.EDIT else MaterialSymbols.ADD,
                        contentDescription = if (goal != null) "Edit goal" else "Set goal",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalDialog(
    metricType: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, LocalDateTime) -> Unit
) {
    var targetValue by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(LocalDateTime.now().plusMonths(3)) }
    
    val (title, unit, validationRange) = when (metricType) {
        "weightKg" -> Triple("Weight Goal", "kg", 30.0..300.0)
        "bodyFatPct" -> Triple("Body Fat Goal", "%", 3.0..60.0)
        "waistCm" -> Triple("Waist Goal", "cm", 50.0..200.0)
        else -> Triple("Goal", "", 0.0..1000.0)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target Value ($unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Valid range: ${validationRange.start.toInt()}-${validationRange.endInclusive.toInt()} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = targetValue.toDoubleOrNull()
                    if (value != null && value in validationRange) {
                        onSave(metricType, value, targetDate)
                    }
                }
            ) {
                Text("Set Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MeasurementDialog(
    onDismiss: () -> Unit,
    onSave: (Double?, Double?, Double?, Double?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Measurement") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    label = { Text("Body Fat (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    label = { Text("Waist (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Valid ranges: Weight 30-300kg, Height 120-230cm, Body Fat 3-60%, Waist 50-200cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weight.toDoubleOrNull()?.takeIf { it in 30.0..300.0 }
                    val h = height.toDoubleOrNull()?.takeIf { it in 120.0..230.0 }
                    val bf = bodyFat.toDoubleOrNull()?.takeIf { it in 3.0..60.0 }
                    val waistVal = waist.toDoubleOrNull()?.takeIf { it in 50.0..200.0 }
                    
                    onSave(w, h, bf, waistVal)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun buildMeasurementGridItems(
    latestMeasurement: BodyMeasurement?,
    activeGoals: List<Goal>,
    onEditClick: (String) -> Unit
): List<MeasurementGridItem> {
    val items = mutableListOf<MeasurementGridItem>()
    
    // Weight Card
    val weightGoal = activeGoals.find { it.metricType == "weightKg" }
    items.add(
        MeasurementGridItem(
            title = "Weight",
            icon = MaterialSymbols.MONITOR_WEIGHT,
            currentValue = latestMeasurement?.weightKg?.let { "%.1f".format(it) },
            targetValue = weightGoal?.targetValue?.let { "%.1f".format(it) },
            progress = if (latestMeasurement?.weightKg != null && weightGoal != null) {
                calculateProgressInternal(latestMeasurement.weightKg, weightGoal.targetValue, "weightKg")
            } else 0f,
            unit = "kg",
            onClick = { onEditClick("weightKg") }
        )
    )
    
    // Height Card
    items.add(
        MeasurementGridItem(
            title = "Height",
            icon = MaterialSymbols.HEIGHT,
            currentValue = latestMeasurement?.heightCm?.let { "%.0f".format(it) },
            targetValue = null,
            progress = 0f,
            unit = "cm",
            isEditable = false
        )
    )
    
    // Body Fat Card
    val bodyFatGoal = activeGoals.find { it.metricType == "bodyFatPct" }
    items.add(
        MeasurementGridItem(
            title = "Body Fat",
            icon = MaterialSymbols.PERCENT,
            currentValue = latestMeasurement?.bodyFatPct?.let { "%.1f".format(it) },
            targetValue = bodyFatGoal?.targetValue?.let { "%.1f".format(it) },
            progress = if (latestMeasurement?.bodyFatPct != null && bodyFatGoal != null) {
                calculateProgressInternal(latestMeasurement.bodyFatPct, bodyFatGoal.targetValue, "bodyFatPct")
            } else 0f,
            unit = "%",
            onClick = { onEditClick("bodyFatPct") }
        )
    )
    
    // Waist Card
    val waistGoal = activeGoals.find { it.metricType == "waistCm" }
    items.add(
        MeasurementGridItem(
            title = "Waist",
            icon = MaterialSymbols.STRAIGHTEN,
            currentValue = latestMeasurement?.waistCm?.let { "%.1f".format(it) },
            targetValue = waistGoal?.targetValue?.let { "%.1f".format(it) },
            progress = if (latestMeasurement?.waistCm != null && waistGoal != null) {
                calculateProgressInternal(latestMeasurement.waistCm, waistGoal.targetValue, "waistCm")
            } else 0f,
            unit = "cm",
            onClick = { onEditClick("waistCm") }
        )
    )
    
    // BMI Card (calculated)
    val bmi = if (latestMeasurement?.weightKg != null && latestMeasurement.heightCm != null) {
        val heightM = latestMeasurement.heightCm / 100.0
        latestMeasurement.weightKg / (heightM * heightM)
    } else null
    
    items.add(
        MeasurementGridItem(
            title = "BMI",
            icon = MaterialSymbols.FITNESS_CENTER,
            currentValue = bmi?.let { "%.1f".format(it) },
            targetValue = null,
            progress = 0f,
            unit = "",
            isEditable = false
        )
    )
    
    return items
}

private fun calculateProgressInternal(current: Double, target: Double, metricType: String): Float {
    return when (metricType) {
        "weightKg", "bodyFatPct", "waistCm" -> {
            // Calculate progress toward goal (0% = far from goal, 100% = goal achieved)
            val difference = kotlin.math.abs(current - target)
            val startingPoint = when (metricType) {
                "weightKg" -> if (target < current) current + (current - target) else current - (target - current)
                "bodyFatPct" -> if (target < current) current + 5.0 else current - 5.0
                "waistCm" -> if (target < current) current + 10.0 else current - 10.0
                else -> current
            }
            val totalRange = kotlin.math.abs(startingPoint - target)
            if (totalRange == 0.0) 1f else {
                val progressMade = kotlin.math.abs(startingPoint - current)
                (progressMade / totalRange).toFloat().coerceIn(0f, 1f)
            }
        }
        else -> 0f
    }
}