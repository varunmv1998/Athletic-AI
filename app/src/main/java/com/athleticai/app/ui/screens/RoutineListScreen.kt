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
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import com.athleticai.app.ui.components.InteractiveCard
import com.athleticai.app.ui.viewmodels.RoutineViewModel
import com.athleticai.app.data.database.entities.WorkoutRoutine
import com.athleticai.app.data.database.entities.Folder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    routineViewModel: RoutineViewModel,
    onStartRoutine: (String) -> Unit,
    onEditRoutine: (String) -> Unit,
    onCreateRoutine: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by routineViewModel.uiState.collectAsState()
    var showFolderSelector by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRoutineOptions by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        // Refresh data when screen loads
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.selectedFolder != null) 
                            uiState.selectedFolder!!.name 
                        else 
                            "My Routines"
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
                    // Create routine button
                    IconButton(onClick = onCreateRoutine) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.ADD,
                            size = IconSizes.STANDARD,
                            contentDescription = "Create routine"
                        )
                    }
                    
                    // Folder selector
                    IconButton(onClick = { showFolderSelector = true }) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.FOLDER,
                            size = IconSizes.STANDARD,
                            contentDescription = "Select folder"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                else -> {
                    RoutineContent(
                        routines = routineViewModel.getFilteredRoutines(),
                        recentRoutines = uiState.recentRoutines,
                        onStartRoutine = onStartRoutine,
                        onEditRoutine = onEditRoutine,
                        onShowOptions = { routineId -> showRoutineOptions = routineId },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Error message
            uiState.errorMessage?.let { message ->
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    routineViewModel.clearErrorMessage()
                }
                
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
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
    
    // Folder selector dialog
    if (showFolderSelector) {
        FolderSelectorDialog(
            folders = uiState.folders,
            selectedFolder = uiState.selectedFolder,
            onFolderSelected = { folder ->
                routineViewModel.selectFolder(folder)
                showFolderSelector = false
            },
            onCreateFolder = { 
                showCreateFolderDialog = true
                showFolderSelector = false
            },
            onDismiss = { showFolderSelector = false }
        )
    }
    
    // Create folder dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onCreateFolder = { name ->
                routineViewModel.createFolder(name)
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }
    
    // Routine options dialog
    showRoutineOptions?.let { routineId ->
        RoutineOptionsDialog(
            onEditRoutine = { 
                onEditRoutine(routineId)
                showRoutineOptions = null
            },
            onDuplicateRoutine = { name ->
                routineViewModel.duplicateRoutine(routineId, name)
                showRoutineOptions = null
            },
            onDeleteRoutine = {
                routineViewModel.deleteRoutine(routineId)
                showRoutineOptions = null
            },
            onDismiss = { showRoutineOptions = null }
        )
    }
}

@Composable
private fun RoutineContent(
    routines: List<WorkoutRoutine>,
    recentRoutines: List<WorkoutRoutine>,
    onStartRoutine: (String) -> Unit,
    onEditRoutine: (String) -> Unit,
    onShowOptions: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recent routines section
        if (recentRoutines.isNotEmpty()) {
            item {
                SectionHeader(title = "Recent", icon = MaterialSymbols.HISTORY)
            }
            
            items(recentRoutines) { routine ->
                RoutineCard(
                    routine = routine,
                    onStartRoutine = { onStartRoutine(routine.id) },
                    onEditRoutine = { onEditRoutine(routine.id) },
                    onShowOptions = { onShowOptions(routine.id) },
                    showRecentLabel = true
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // All routines section
        if (routines.isNotEmpty()) {
            if (recentRoutines.isNotEmpty()) {
                item {
                    SectionHeader(title = "All Routines", icon = MaterialSymbols.FITNESS_CENTER)
                }
            }
            
            items(routines) { routine ->
                RoutineCard(
                    routine = routine,
                    onStartRoutine = { onStartRoutine(routine.id) },
                    onEditRoutine = { onEditRoutine(routine.id) },
                    onShowOptions = { onShowOptions(routine.id) }
                )
            }
        } else {
            // Empty state
            item {
                EmptyRoutinesState()
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: WorkoutRoutine,
    onStartRoutine: () -> Unit,
    onEditRoutine: () -> Unit,
    onShowOptions: () -> Unit,
    showRecentLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onStartRoutine,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Routine icon
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.FITNESS_CENTER,
                        size = IconSizes.STANDARD,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Routine info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (showRecentLabel) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "Recent",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                // Last performed info
                routine.lastPerformed?.let { timestamp ->
                    val date = Date(timestamp)
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = "Last performed: ${formatter.format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: run {
                    Text(
                        text = "Never performed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Notes preview
                if (routine.notes.isNotEmpty()) {
                    Text(
                        text = routine.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button
                IconButton(onClick = onEditRoutine) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.EDIT,
                        size = IconSizes.SMALL,
                        contentDescription = "Edit routine",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Options button
                IconButton(onClick = onShowOptions) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.MORE_VERT,
                        size = IconSizes.SMALL,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Start button
                FilledTonalButton(
                    onClick = onStartRoutine,
                    modifier = Modifier.size(width = 80.dp, height = 36.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.PLAY_ARROW,
                        size = IconSizes.SMALL,
                        contentDescription = "Start routine"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaterialSymbol(
            symbol = icon,
            size = IconSizes.STANDARD,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyRoutinesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MaterialSymbol(
            symbol = MaterialSymbols.FITNESS_CENTER,
            size = IconSizes.EXTRA_LARGE,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Text(
            text = "No routines yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Create your first workout routine to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FolderSelectorDialog(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit,
    onCreateFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Folder") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All routines option
                item {
                    TextButton(
                        onClick = { onFolderSelected(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedFolder == null,
                                onClick = { onFolderSelected(null) }
                            )
                            Text("All Routines")
                        }
                    }
                }
                
                // Folder options
                items(folders) { folder ->
                    TextButton(
                        onClick = { onFolderSelected(folder) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedFolder?.id == folder.id,
                                onClick = { onFolderSelected(folder) }
                            )
                            Text(folder.name)
                        }
                    }
                }
                
                // Create folder option
                item {
                    TextButton(
                        onClick = onCreateFolder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.ADD,
                                size = IconSizes.STANDARD,
                                contentDescription = null
                            )
                            Text("Create New Folder")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun CreateFolderDialog(
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreateFolder(folderName) },
                enabled = folderName.trim().isNotEmpty()
            ) {
                Text("Create")
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
private fun RoutineOptionsDialog(
    onEditRoutine: () -> Unit,
    onDuplicateRoutine: (String) -> Unit,
    onDeleteRoutine: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    if (showDuplicateDialog) {
        DuplicateRoutineDialog(
            onDuplicate = { name ->
                onDuplicateRoutine(name)
                showDuplicateDialog = false
            },
            onDismiss = { showDuplicateDialog = false }
        )
    } else if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Routine") },
            text = { Text("Are you sure you want to delete this routine? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRoutine()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Routine Options") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onEditRoutine,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.EDIT,
                                size = IconSizes.STANDARD,
                                contentDescription = null
                            )
                            Text("Edit Routine")
                        }
                    }
                    
                    TextButton(
                        onClick = { showDuplicateDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.CONTENT_COPY,
                                size = IconSizes.STANDARD,
                                contentDescription = null
                            )
                            Text("Duplicate")
                        }
                    }
                    
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.DELETE,
                                size = IconSizes.STANDARD,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun DuplicateRoutineDialog(
    onDuplicate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duplicate Routine") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Routine Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onDuplicate(newName) },
                enabled = newName.trim().isNotEmpty()
            ) {
                Text("Duplicate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}