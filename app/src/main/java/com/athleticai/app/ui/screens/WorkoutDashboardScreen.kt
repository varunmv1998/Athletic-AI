package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.components.*
import com.athleticai.app.ui.viewmodels.RoutineViewModel
import com.athleticai.app.ui.viewmodels.WorkoutViewModel
import com.athleticai.app.ui.viewmodels.ProgramManagementViewModel
import com.athleticai.app.ui.viewmodels.WorkoutViewMode
import com.athleticai.app.data.database.entities.WorkoutRoutine
import com.athleticai.app.data.database.entities.Program
import com.athleticai.app.data.database.entities.CurrentProgramState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDashboardScreen(
    routineViewModel: RoutineViewModel,
    workoutViewModel: WorkoutViewModel,
    programViewModel: ProgramManagementViewModel,
    onStartRoutine: (String) -> Unit,
    onViewAllRoutines: () -> Unit,
    onCreateRoutine: () -> Unit,
    onEditRoutine: (String) -> Unit,
    onEnrollInProgram: (String) -> Unit,
    onViewProgramDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val routineUiState by routineViewModel.uiState.collectAsState()
    val programUiState by programViewModel.uiState.collectAsState()
    val viewMode by programViewModel.viewMode.collectAsState()
    val activeEnrollment by programViewModel.activeEnrollment.collectAsState()
    val currentProgramState by programViewModel.currentProgramState.collectAsState()
    val allPrograms by programViewModel.allPrograms.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (viewMode) {
                            WorkoutViewMode.ROUTINES -> "My Routines"
                            WorkoutViewMode.PROGRAMS -> "Programs"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ARROW_BACK,
                            size = IconSizes.STANDARD,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    when (viewMode) {
                        WorkoutViewMode.ROUTINES -> {
                            IconButton(onClick = onCreateRoutine) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.ADD,
                                    size = IconSizes.STANDARD,
                                    contentDescription = "Create routine"
                                )
                            }
                            IconButton(onClick = onViewAllRoutines) {
                                MaterialSymbol(
                                    symbol = MaterialSymbols.LIST,
                                    size = IconSizes.STANDARD,
                                    contentDescription = "View all routines"
                                )
                            }
                        }
                        WorkoutViewMode.PROGRAMS -> {
                            // Could add program-specific actions here
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        // Check if we're in program creation mode
        if (programUiState.isCreatingProgram) {
            CustomProgramCreationScreen(
                viewModel = programViewModel,
                routines = routineUiState.routines,
                onNavigateBack = {
                    programViewModel.cancelProgramCreation()
                },
                onProgramCreated = {
                    programViewModel.cancelProgramCreation()
                    // Optionally refresh programs list
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Programs/Routines Toggle
                ProgramRoutineToggle(
                    selectedMode = viewMode,
                    onModeChange = { programViewModel.setViewMode(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Content based on selected mode
                when (viewMode) {
                    WorkoutViewMode.ROUTINES -> {
                        RoutinesContent(
                            routineUiState = routineUiState,
                            routineViewModel = routineViewModel,
                            onStartRoutine = onStartRoutine,
                            onEditRoutine = onEditRoutine,
                            onCreateRoutine = onCreateRoutine
                        )
                    }
                    WorkoutViewMode.PROGRAMS -> {
                        ProgramsContent(
                            programs = allPrograms,
                            currentProgramState = currentProgramState,
                            activeEnrollment = activeEnrollment,
                            isLoading = programUiState.isLoading,
                            onEnrollInProgram = { programId ->
                                programViewModel.enrollInProgram(programId)
                            },
                            onViewProgramDetails = onViewProgramDetails,
                            onStartWorkout = { 
                                currentProgramState?.currentProgramDay?.routineId?.let { routineId ->
                                    onStartRoutine(routineId)
                                }
                            },
                            onSkipDay = {
                                programViewModel.skipCurrentDay()
                            },
                            onCreateProgram = {
                                programViewModel.startProgramCreation()
                            }
                        )
                    }
                }
            }
        }
        
        // Show any error messages
        programUiState.error?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                programViewModel.clearError()
            }
            
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun RoutinesContent(
    routineUiState: com.athleticai.app.ui.viewmodels.RoutineUiState,
    routineViewModel: RoutineViewModel,
    onStartRoutine: (String) -> Unit,
    onEditRoutine: (String) -> Unit,
    onCreateRoutine: () -> Unit
) {
    if (routineUiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recent Routines Section (Quick Start)
            if (routineUiState.recentRoutines.isNotEmpty()) {
                item {
                    SectionHeader(title = "Quick Start", icon = MaterialSymbols.HISTORY)
                }
                
                items(routineUiState.recentRoutines.take(2)) { routine ->
                    CompactRoutineCard(
                        routine = routine,
                        onStartRoutine = { onStartRoutine(routine.id) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
            
            // All Routines Section
            if (routineUiState.routines.isEmpty()) {
                item {
                    EmptyRoutinesCard(onCreateRoutine = onCreateRoutine)
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "All Routines (${routineUiState.routines.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Display routine cards
                val displayedRoutines = routineViewModel.getDisplayedRoutines()
                items(displayedRoutines) { routine ->
                    RoutineCard(
                        routine = routine,
                        exerciseCount = 0, // TODO: Get actual exercise count
                        estimatedDuration = "45-60 min", // TODO: Calculate from exercises
                        onStartRoutine = { onStartRoutine(routine.id) },
                        onShowOptions = { routineViewModel.showRoutineOptions(routine.id) }
                    )
                }
                
                // Show More/Less button
                if (routineViewModel.hasMoreRoutines()) {
                    item {
                        ShowMoreButton(
                            isExpanded = routineUiState.isRoutineListExpanded,
                            totalRoutines = routineUiState.routines.size,
                            displayedCount = displayedRoutines.size,
                            onToggle = { routineViewModel.toggleRoutineListExpansion() }
                        )
                    }
                }
            }
        }
        
        // Options menu dialog
        routineUiState.selectedRoutineForOptions?.let { routine ->
            if (routineUiState.showOptionsMenu) {
                RoutineOptionsDialog(
                    routine = routine,
                    onEditRoutine = {
                        onEditRoutine(routine.id)
                        routineViewModel.hideRoutineOptions()
                    },
                    onDuplicateRoutine = {
                        routineViewModel.duplicateRoutine(routine.id)
                    },
                    onDeleteRoutine = {
                        routineViewModel.deleteRoutineFromOptions(routine.id)
                    },
                    onDismiss = { routineViewModel.hideRoutineOptions() }
                )
            }
        }
    }
}

@Composable
private fun ProgramsContent(
    programs: List<Program>,
    currentProgramState: CurrentProgramState?,
    activeEnrollment: com.athleticai.app.data.database.entities.UserProgramEnrollmentWithProgram?,
    isLoading: Boolean,
    onEnrollInProgram: (String) -> Unit,
    onViewProgramDetails: (String) -> Unit,
    onStartWorkout: () -> Unit,
    onSkipDay: () -> Unit,
    onCreateProgram: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Program Card (if enrolled)
            currentProgramState?.let { state ->
                item {
                    SectionHeader(
                        title = "Active Program",
                        icon = MaterialSymbols.PLAY_ARROW
                    )
                }
                item {
                    ActiveProgramCard(
                        currentState = state,
                        onResume = onStartWorkout,
                        onSkipDay = onSkipDay,
                        onViewDetails = { 
                            onViewProgramDetails(state.program.id)
                        }
                    )
                }
                item { 
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            
            // Available Programs Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = if (currentProgramState != null) "Other Programs" else "Available Programs",
                        icon = MaterialSymbols.FITNESS_CENTER
                    )
                    
                    TextButton(onClick = onCreateProgram) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ADD,
                            size = IconSizes.SMALL,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Custom")
                    }
                }
            }
            
            if (programs.isEmpty()) {
                item {
                    EmptyProgramsCard(onCreateProgram = onCreateProgram)
                }
            } else {
                items(programs) { program ->
                    ProgramCard(
                        program = program,
                        onClick = { onViewProgramDetails(program.id) },
                        onEnroll = if (activeEnrollment?.program?.id != program.id) {
                            { onEnrollInProgram(program.id) }
                        } else null,
                        isEnrolled = activeEnrollment?.program?.id == program.id
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowMoreButton(
    isExpanded: Boolean,
    totalRoutines: Int,
    displayedCount: Int,
    onToggle: () -> Unit
) {
    OutlinedButton(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        MaterialSymbol(
            symbol = if (isExpanded) MaterialSymbols.EXPAND_LESS else MaterialSymbols.EXPAND_MORE,
            size = IconSizes.STANDARD,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isExpanded) "Show Less" else "Show ${totalRoutines - displayedCount} More"
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaterialSymbol(
            symbol = icon,
            size = IconSizes.STANDARD,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyRoutinesCard(
    onCreateRoutine: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MaterialSymbol(
                symbol = MaterialSymbols.FITNESS_CENTER,
                size = IconSizes.LARGE,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                contentDescription = null
            )
            Text(
                text = "No Routines Yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Create your first workout routine to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Button(
                onClick = onCreateRoutine,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.ADD,
                    size = IconSizes.SMALL,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Routine")
            }
        }
    }
}

@Composable
private fun EmptyProgramsCard(
    onCreateProgram: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MaterialSymbol(
                symbol = MaterialSymbols.CALENDAR_MONTH,
                size = IconSizes.LARGE,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                contentDescription = null
            )
            Text(
                text = "No Programs Available",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "Create a custom program or wait for pre-built programs to be added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Button(
                onClick = onCreateProgram,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                MaterialSymbol(
                    symbol = MaterialSymbols.ADD,
                    size = IconSizes.SMALL,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Custom Program")
            }
        }
    }
}

@Composable
private fun RoutineOptionsDialog(
    routine: WorkoutRoutine,
    onEditRoutine: () -> Unit,
    onDuplicateRoutine: () -> Unit,
    onDeleteRoutine: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(routine.name) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onEditRoutine,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.EDIT,
                            size = IconSizes.STANDARD,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Edit Routine")
                    }
                }
                TextButton(
                    onClick = onDuplicateRoutine,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.CONTENT_COPY,
                            size = IconSizes.STANDARD,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Duplicate")
                    }
                }
                TextButton(
                    onClick = onDeleteRoutine,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.DELETE,
                            size = IconSizes.STANDARD,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Delete",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}