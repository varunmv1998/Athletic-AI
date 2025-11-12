package com.athleticai.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.viewmodels.ProgressViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel, 
    onOpenSession: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
            // Workout Progress Section
            item { 
                ProgressSectionHeader(
                    title = "Workout Progress",
                    icon = MaterialSymbols.TRENDING_UP
                )
            }
            item { CalendarSection(state.completedDates) }
            item { VolumeChartSection(state.sevenDayVolume) }
            item { ChartsSection(viewModel) }
            item { PRsSection(state) }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Workout History Section
            item { 
                ProgressSectionHeader(
                    title = "Workout History",
                    icon = MaterialSymbols.HISTORY
                )
            }
            item { HistorySection(state, onOpenSession) }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ProgressSectionHeader(
    title: String,
    icon: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaterialSymbol(
            symbol = icon,
            contentDescription = "$title section icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun CalendarSection(completedDates: Set<LocalDate>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) { 
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.DATE_RANGE,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Workout Calendar", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            val ym = YearMonth.now()
            val first = ym.atDay(1)
            val daysInMonth = ym.lengthOfMonth()
            val weeks = ((first.dayOfWeek.value - 1 + daysInMonth + 6) / 7)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var day = 1
                for (w in 0 until weeks) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (d in 1..7) {
                            val date = runCatching { ym.atDay(day) }.getOrNull()
                            val active = date != null && date.month == ym.month
                            val status = if (active && completedDates.contains(date)) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(color = if (active) status.copy(alpha = 0.2f) else Color.Transparent, shape = RoundedCornerShape(6.dp))
                                    .border(1.dp, status, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (active) "$day" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) MaterialTheme.colorScheme.onSurface else Color.Transparent
                                )
                            }
                            if (active) day++
                            if (day > daysInMonth) break
                        }
                    }
                    if (day > daysInMonth) break
                }
            }
        }
    }
}

@Composable
private fun VolumeChartSection(sevenDayVolume: Double) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.BAR_CHART,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "7-Day Volume", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            
            if (sevenDayVolume > 0) {
                LinearProgressIndicator(
                    progress = { (sevenDayVolume / (sevenDayVolume + 1000)).toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Total: ${"%.0f".format(sevenDayVolume)} kg·reps", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                EmptyState(
                    icon = MaterialSymbols.BAR_CHART,
                    title = "No Volume Data",
                    description = "Complete workouts to see your training volume"
                )
            }
        }
    }
}

@Composable
private fun ChartsSection(viewModel: ProgressViewModel) {
    var range by remember { mutableStateOf(7L) }
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    LaunchedEffect(range) {
        // access repository through VM by adding a small helper
        data = viewModel.getVolumeByExercise(range)
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) { 
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.SHOW_CHART,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Exercise Volume", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7L, 30L).forEach { d ->
                        FilterChip(
                            selected = range == d, 
                            onClick = { range = d }, 
                            label = { Text("${d}d") }
                        )
                    }
                }
            }
            if (data.isEmpty()) {
                EmptyState(
                    icon = MaterialSymbols.SHOW_CHART,
                    title = "No Exercise Data",
                    description = "Complete workouts to see exercise breakdown"
                )
            } else {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val max = (data.values.maxOrNull() ?: 1.0)
                    data.entries.take(8).forEach { (exerciseId, vol) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = exerciseId.replace('_',' '),
                                modifier = Modifier.width(160.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            LinearProgressIndicator(progress = { (vol / max).toFloat() }, modifier = Modifier.weight(1f))
                            Text("${"%.0f".format(vol)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PRsSection(state: com.athleticai.app.ui.viewmodels.ProgressUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.EMOJI_EVENTS,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Personal Records", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            if (state.recentPRs.isEmpty()) {
                EmptyState(
                    icon = MaterialSymbols.EMOJI_EVENTS,
                    title = "No Personal Records",
                    description = "Keep training to set your first PR!"
                )
            } else {
                state.recentPRs.forEach { pr ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${pr.type} • ${pr.exerciseId}")
                        Text("${"%.1f".format(pr.value)}")
                    }
                }
            }
        }
    }
}



@Composable
private fun EmptyState(
    icon: String,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(
                symbol = icon,
                contentDescription = null,
                size = 40.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun HistorySection(
    state: com.athleticai.app.ui.viewmodels.ProgressUiState,
    onOpen: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state.recentSessions.isEmpty()) {
                EmptyState(
                    icon = MaterialSymbols.HISTORY,
                    title = "No Workout History",
                    description = "Complete your first workout to see history"
                )
            } else {
                state.recentSessions.forEach { s ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(s.sessionId) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                    text = s.sessionName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = s.endTime?.toLocalDate()?.toString() ?: s.startTime.toLocalDate().toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            MaterialSymbol(
                                symbol = MaterialSymbols.KEYBOARD_ARROW_RIGHT,
                                contentDescription = "Open session",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

