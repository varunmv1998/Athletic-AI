package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_programs")
data class CustomProgram(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val isActive: Boolean = false,
    val createdDate: Long,
    val totalWorkouts: Int = 0 // Calculated field - will be updated via triggers or manually
)