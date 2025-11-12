package com.athleticai.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.theme.ThemePreset
import com.athleticai.app.ui.theme.CustomThemeColors
import kotlinx.coroutines.launch

@Composable
fun ThemeGridSelector(
    currentPreset: String,
    onPresetSelected: (ThemePreset) -> Unit,
    onCustomThemeRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ThemePreset.values()) { preset ->
            ThemePresetCard(
                preset = preset,
                isSelected = currentPreset == preset.name,
                onClick = {
                    if (preset == ThemePreset.CUSTOM) {
                        onCustomThemeRequested()
                    } else {
                        onPresetSelected(preset)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePresetCard(
    preset: ThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "border"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Color preview circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                preset.previewColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            
            // Preset name and description
            Column {
                Text(
                    text = preset.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            // Selected indicator
            if (isSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    MaterialSymbol(
                        symbol = MaterialSymbols.CHECK_CIRCLE,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemePreviewSection(
    preset: ThemePreset,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Sample UI elements with theme colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary button
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = preset.seedColor
                    )
                ) {
                    Text("Primary", color = Color.White)
                }
                
                // Secondary button
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, preset.seedColor)
                ) {
                    Text("Secondary", color = preset.seedColor)
                }
            }
            
            // Sample card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = preset.previewColors.getOrNull(3)?.copy(alpha = 0.1f) 
                        ?: MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = preset.previewColors.take(2)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sample Workout",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "3 sets • 12 reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Color palette preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                preset.previewColors.forEach { color ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = color
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        // Empty card just for color display
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageColorExtractorDialog(
    onDismiss: () -> Unit,
    onColorsExtracted: (CustomThemeColors) -> Unit
) {
    val context = LocalContext.current
    var extractedColors by remember { mutableStateOf<CustomThemeColors?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isProcessing = true
            // Extract colors from image
            // This would use Android's Palette API
            // For now, we'll simulate with placeholder
            extractedColors = CustomThemeColors(
                seedColor = 0xFF6200EA,
                primary = 0xFF6200EA,
                secondary = 0xFF03DAC6,
                tertiary = 0xFFBB86FC,
                neutral = 0xFF121212
            )
            isProcessing = false
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Extract Theme from Image")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose a photo to extract a custom color theme",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        MaterialSymbol(
                            symbol = MaterialSymbols.FOLDER,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Photo")
                    }
                }
                
                extractedColors?.let { colors ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Extracted Colors",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Color(colors.primary),
                                    Color(colors.secondary),
                                    Color(colors.tertiary),
                                    Color(colors.neutral)
                                ).forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    extractedColors?.let { onColorsExtracted(it) }
                    onDismiss()
                },
                enabled = extractedColors != null
            ) {
                Text("Apply Theme")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Extension to convert theme colors to JSON for storage
fun CustomThemeColors.toJson(): String {
    return "{\"seedColor\":$seedColor,\"primary\":$primary,\"secondary\":$secondary,\"tertiary\":$tertiary,\"neutral\":$neutral}"
}

// Extension to parse theme colors from JSON
fun parseCustomThemeColors(json: String): CustomThemeColors? {
    return try {
        // Simple JSON parsing - in production use Gson
        val values = json.replace(Regex("[{}\"]"), "")
            .split(",")
            .associate {
                val (key, value) = it.split(":")
                key.trim() to value.trim().toLong()
            }
        
        CustomThemeColors(
            seedColor = values["seedColor"] ?: 0,
            primary = values["primary"] ?: 0,
            secondary = values["secondary"] ?: 0,
            tertiary = values["tertiary"] ?: 0,
            neutral = values["neutral"] ?: 0
        )
    } catch (e: Exception) {
        null
    }
}