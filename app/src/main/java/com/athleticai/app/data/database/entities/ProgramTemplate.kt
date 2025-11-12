package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program_templates")
data class ProgramTemplate(
    @PrimaryKey val id: String,
    val templateKey: String, // e.g., "beginner_fat_loss_8w"
    val jsonData: String, // Store full JSON for reference
    val isBuiltIn: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)