package com.athleticai.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.athleticai.app.data.database.entities.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListItem(
    exercise: Exercise,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRecentlyUsed: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDetailsClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (isRecentlyUsed) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GIF Thumbnail (ExerciseDB only)
            if (exercise.source == "EXERCISE_DB" && !exercise.gifUrl.isNullOrBlank()) {
                ExerciseGifThumbnail(
                    gifUrl = exercise.gifUrl,
                    exerciseName = exercise.name,
                    localGifPath = exercise.gifLocalPath
                )
            }
            
            // Exercise Information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (isRecentlyUsed) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.HISTORY,
                            size = IconSizes.SMALL,
                            contentDescription = "Recently used",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Primary muscles
                if (exercise.primaryMuscles.isNotEmpty()) {
                    Text(
                        text = exercise.primaryMuscles.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Equipment
                if (!exercise.equipment.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.FITNESS_CENTER,
                            size = IconSizes.SMALL,
                            contentDescription = "Equipment",
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                        Text(
                            text = exercise.equipment.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Selection Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ExerciseGifThumbnail(
    gifUrl: String,
    exerciseName: String,
    localGifPath: String? = null
) {
    val context = LocalContext.current
    
    // Create ImageLoader with GIF support
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }
    
    Card(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val imageSource = localGifPath?.takeIf { java.io.File(it).exists() } ?: gifUrl
            
            var isLoading by remember { mutableStateOf(true) }
            
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageSource)
                    .crossfade(200)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "Exercise preview for $exerciseName",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    isLoading = false
                },
                onError = {
                    isLoading = false
                },
                onLoading = {
                    isLoading = true
                }
            )
            
            // Show play icon overlay when loading or as fallback
            if (isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialSymbol(
                            symbol = MaterialSymbols.PLAY_ARROW,
                            size = IconSizes.STANDARD,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}