package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program_quotes")
data class ProgramQuote(
    @PrimaryKey val id: String,
    val text: String,
    val category: String, // "completion", "skip", "rest_day", "motivation", "encouragement"
    val context: String? = null, // Additional context like "morning", "evening"
    val isActive: Boolean = true
)

enum class QuoteCategory {
    COMPLETION,    // Success after completing workout
    SKIP,         // Encouragement after skipping
    REST_DAY,     // Rest day motivation
    MOTIVATION,   // General motivation
    ENCOURAGEMENT // General encouragement
}