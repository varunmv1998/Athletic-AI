package com.athleticai.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFilterChips(
    selectedMuscleGroup: String?,
    selectedEquipment: String?,
    selectedCategory: String?,
    onMuscleGroupSelected: (String?) -> Unit,
    onEquipmentSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Predefined filter options based on free-exercise-db structure
    val muscleGroups = listOf(
        "chest", "back", "shoulders", "biceps", "triceps",
        "quadriceps", "hamstrings", "glutes", "calves", "abdominals"
    )
    
    val equipmentTypes = listOf(
        "barbell", "dumbbell", "cable", "machine", "body only", "kettle bells"
    )
    
    val categories = listOf(
        "strength", "stretching", "plyometrics", "strongman", "powerlifting", "cardio"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Muscle Groups
        FilterChipRow(
            title = "Muscle Groups",
            options = muscleGroups,
            selectedOption = selectedMuscleGroup,
            onOptionSelected = onMuscleGroupSelected
        )

        // Equipment
        FilterChipRow(
            title = "Equipment",
            options = equipmentTypes,
            selectedOption = selectedEquipment,
            onOptionSelected = onEquipmentSelected
        )

        // Categories
        FilterChipRow(
            title = "Categories",
            options = categories,
            selectedOption = selectedCategory,
            onOptionSelected = onCategorySelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(
    title: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit
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
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            if (selectedOption != null) {
                TextButton(
                    onClick = { onOptionSelected(null) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOption == option,
                    onClick = {
                        onOptionSelected(if (selectedOption == option) null else option)
                    },
                    label = {
                        Text(
                            text = option.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}