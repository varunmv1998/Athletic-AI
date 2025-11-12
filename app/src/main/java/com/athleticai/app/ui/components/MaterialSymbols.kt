package com.athleticai.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athleticai.app.R
import androidx.compose.ui.Alignment

// Material Symbols font family
val MaterialSymbolsOutlined = FontFamily(
    Font(R.font.material_symbols_outlined_regular)
)

// Standardized icon sizes for consistent UI
object IconSizes {
    val SMALL = 16.dp      // For inline text icons
    val MEDIUM = 20.dp     // For secondary actions
    val STANDARD = 24.dp   // Default interface icons
    val LARGE = 32.dp      // For primary actions
    val EXTRA_LARGE = 48.dp // For touch targets
}

/**
 * Enhanced Material Symbols Icon composable with interactive states
 * Uses the Material Symbols Outlined font from Google Fonts
 * Prevents icon clipping with proper padding and sizing
 */
@Composable
fun MaterialSymbol(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = IconSizes.STANDARD,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    // Calculate the actual icon size with padding to prevent clipping
    val iconSize = size - 2.dp // Reduce by 2dp to add padding
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontFamily = MaterialSymbolsOutlined,
            fontSize = iconSize.value.sp,
            color = tint,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Material Symbols constants for commonly used icons in Athletic AI
 * Organized by functional categories with consistent naming
 */
object MaterialSymbols {
    // Navigation - All use 24dp standard size
    const val HOME = "home"
    const val FITNESS_CENTER = "fitness_center"
    const val ANALYTICS = "analytics"
    const val PSYCHOLOGY = "psychology"
    const val SETTINGS = "settings"
    
    // Primary Actions - 24dp with 48dp touch targets
    const val ADD = "add"
    const val ADD_CIRCLE = "add_circle"
    const val DELETE = "delete"
    const val EDIT = "edit"
    const val CLOSE = "close"
    const val CHECK = "check"
    const val CHECK_CIRCLE = "check_circle"
    const val SEARCH = "search"
    const val MORE_VERT = "more_vert"
    const val REMOVE = "remove"
    const val CONTENT_COPY = "content_copy"
    const val LIST = "list"
    const val FOLDER = "folder"
    
    // Navigation arrows - 24dp standard
    const val ARROW_BACK = "arrow_back"
    const val ARROW_FORWARD = "arrow_forward"
    const val KEYBOARD_ARROW_RIGHT = "keyboard_arrow_right"
    const val KEYBOARD_ARROW_DOWN = "keyboard_arrow_down"
    const val KEYBOARD_ARROW_UP = "keyboard_arrow_up"
    const val DRAG_HANDLE = "drag_handle"
    const val EXPAND_LESS = "expand_less"
    const val EXPAND_MORE = "expand_more"
    
    // Progress and Analytics - 24dp for data visualization
    const val TRENDING_UP = "trending_up"
    const val BAR_CHART = "bar_chart"
    const val SHOW_CHART = "show_chart"
    const val EMOJI_EVENTS = "emoji_events"
    const val CALENDAR_MONTH = "calendar_month"
    const val DATE_RANGE = "date_range"
    
    // Body measurements - 24dp for profile sections
    const val MONITOR_WEIGHT = "monitor_weight"
    const val HEIGHT = "height"
    const val STRAIGHTEN = "straighten"
    const val PERCENT = "percent"
    const val SCALE = "scale"
    
    // Workout and Exercise - 24dp for fitness interface
    const val TIMER = "timer"
    const val SELF_IMPROVEMENT = "self_improvement"
    const val SPORTS = "sports"
    const val EXERCISE = "exercise"
    const val PLAY_ARROW = "play_arrow"
    const val PAUSE = "pause"
    
    // Settings and Configuration - 24dp for system interface
    const val DARK_MODE = "dark_mode"
    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATIONS_ACTIVE = "notifications_active"
    const val SCHEDULE = "schedule"
    const val KEY = "key"
    const val DOWNLOAD = "download"
    const val HELP = "help"
    const val INFO = "info"
    const val SECURITY = "security"
    
    // AI and Chat - 24dp for coaching interface
    const val SMART_TOY = "smart_toy"
    const val CHAT = "chat"
    const val SEND = "send"
    const val ASSIGNMENT = "assignment"
    const val LIGHTBULB = "lightbulb"
    
    // Status and Indicators - 16dp for inline use
    const val CIRCLE = "circle"
    const val RADIO_BUTTON_UNCHECKED = "radio_button_unchecked"
    
    // User and Profile - 24dp for user interface
    const val PERSON = "person"
    const val STAR = "star"
    const val FAVORITE = "favorite"
    const val VISIBILITY = "visibility"
    const val VISIBILITY_OFF = "visibility_off"
    
    // History and Time
    const val HISTORY = "history"
    
    // Media and Actions
    const val REPEAT = "repeat"
    const val SAVE = "save"
}

