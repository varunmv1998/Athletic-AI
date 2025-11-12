package com.athleticai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athleticai.app.data.database.entities.*
import com.athleticai.app.ui.components.*
import com.athleticai.app.ui.viewmodels.ProgramManagementViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProgramCreationScreen(
    viewModel: ProgramManagementViewModel,
    routines: List<WorkoutRoutine>,
    onNavigateBack: () -> Unit,
    onProgramCreated: () -> Unit
) {
    // Simple state management for now
    var programName by remember { mutableStateOf("") }
    var programDescription by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf(ProgramGoal.GENERAL_FITNESS) }
    var selectedExperience by remember { mutableStateOf(ExperienceLevel.BEGINNER) }
    
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with progress
        ProgramCreationHeader(
            currentStep = currentStep,
            totalSteps = totalSteps,
            onNavigateBack = {
                if (currentStep > 0) {
                    currentStep--
                } else {
                    onNavigateBack()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Simplified single step for now
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Create Custom Program",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = programName,
                onValueChange = { programName = it },
                label = { Text("Program Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = programDescription,
                onValueChange = { programDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Program Goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Goal selection chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProgramGoal.values()) { goal ->
                    FilterChip(
                        onClick = { selectedGoal = goal },
                        label = { Text(goal.name.replace("_", " ")) },
                        selected = selectedGoal == goal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Experience Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Experience level chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExperienceLevel.values().forEach { level ->
                    FilterChip(
                        onClick = { selectedExperience = level },
                        label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        selected = selectedExperience == level
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Create button
            Button(
                onClick = {
                    // TODO: Implement program creation
                    onProgramCreated()
                },
                enabled = programName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Program")
            }
        }
    }
}

@Composable
private fun ProgramCreationHeader(
    currentStep: Int,
    totalSteps: Int,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Create Custom Program",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}









