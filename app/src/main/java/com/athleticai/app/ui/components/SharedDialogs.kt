package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.Folder

@Composable
fun ExerciseSelectionDialog(
    exerciseSelectionViewModel: ExerciseSelectionViewModel,
    onExerciseSelected: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val exerciseState by exerciseSelectionViewModel.uiState.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.CLOSE,
                            size = IconSizes.STANDARD,
                            contentDescription = "Close"
                        )
                    }
                }
                
                // Search bar
                OutlinedTextField(
                    value = exerciseState.searchQuery,
                    onValueChange = { exerciseSelectionViewModel.updateSearchQuery(it) },
                    label = { Text("Search exercises") },
                    leadingIcon = {
                        MaterialSymbol(
                            symbol = MaterialSymbols.SEARCH,
                            size = IconSizes.STANDARD,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Exercise list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = exerciseState.searchResults,
                        key = { exercise -> exercise.id }
                    ) { exercise ->
                        ExerciseSelectionItem(
                            exercise = exercise,
                            onClick = {
                                onExerciseSelected(exercise)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseSelectionItem(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            exercise.equipment?.let { equipment ->
                Text(
                    text = equipment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (exercise.primaryMuscles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.primaryMuscles.take(3).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FolderSelectorDialog(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    
    if (showCreateDialog) {
        CreateFolderDialog(
            onCreateFolder = { name ->
                onCreateFolder(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Select Folder",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = onDismiss) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.CLOSE,
                                size = IconSizes.STANDARD,
                                contentDescription = "Close"
                            )
                        }
                    }
                    
                    // Folder list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // No folder option
                        item {
                            FolderItem(
                                name = "No Folder",
                                icon = MaterialSymbols.FOLDER,
                                isSelected = selectedFolder == null,
                                onClick = { 
                                    onFolderSelected(null)
                                    onDismiss()
                                }
                            )
                        }
                        
                        // Folder options
                        items(folders) { folder ->
                            FolderItem(
                                name = folder.name,
                                icon = MaterialSymbols.FOLDER,
                                isSelected = selectedFolder?.id == folder.id,
                                onClick = { 
                                    onFolderSelected(folder)
                                    onDismiss()
                                }
                            )
                        }
                    }
                    
                    // Create folder button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        TextButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MaterialSymbol(
                                symbol = MaterialSymbols.ADD,
                                size = IconSizes.STANDARD,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Create New Folder",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderItem(
    name: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InteractiveCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialSymbol(
                symbol = icon,
                size = IconSizes.STANDARD,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                MaterialSymbol(
                    symbol = MaterialSymbols.CHECK,
                    size = IconSizes.STANDARD,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderName.trim().isNotEmpty()) {
                        onCreateFolder(folderName.trim())
                    }
                },
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