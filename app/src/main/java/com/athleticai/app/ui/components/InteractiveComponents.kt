package com.athleticai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.athleticai.app.ui.theme.AthleticAIMotion

/**
 * Enhanced Card with Material3 motion physics and interactive states
 */
@Composable
fun InteractiveCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = when {
            isPressedState -> AthleticAIMotion.CARD_ELEVATION_PRESS
            isPressed -> AthleticAIMotion.CARD_ELEVATION_HOVER
            else -> AthleticAIMotion.CARD_ELEVATION_DEFAULT
        },
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "card_elevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressedState) AthleticAIMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
        ) {
            content()
        }
    }
}

/**
 * Enhanced Button with spring animations and interactive feedback
 */
@Composable
fun InteractiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) AthleticAIMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        content()
    }
}

/**
 * Enhanced IconButton with consistent sizing and interactive states
 */
@Composable
fun InteractiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) AthleticAIMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "icon_button_scale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp) // Consistent 48dp touch target
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        content()
    }
}

/**
 * Animated Progress Indicator with smooth transitions
 */
@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "progress_animation"
    )
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Shimmer Loading Effect for content sections
 */
@Composable
fun ShimmerLoadingCard(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AthleticAIMotion.SHORT_DURATION * 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp)
        ) {
            // Shimmer placeholder content
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .offset(y = (index * 24).dp)
                )
            }
        }
    }
}

/**
 * Animated Counter with smooth number transitions
 */
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "counter_animation"
    )
    
    Text(
        text = animatedCount.toString(),
        style = style,
        modifier = modifier
    )
}

/**
 * Enhanced List Item with interactive feedback
 */
@Composable
fun InteractiveListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AthleticAIMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = AthleticAIMotion.SPRING_DAMPING_RATIO,
            stiffness = AthleticAIMotion.SPRING_STIFFNESS
        ),
        label = "list_item_scale"
    )
    
    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(IconSizes.STANDARD)
                        .padding(end = 16.dp)
                ) {
                    it()
                }
            }
            content()
        }
    }
}

