package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String,
    val metricType: String, // weightKg, bodyFatPct, waistCm, etc.
    val targetValue: Double,
    val targetDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val isActive: Boolean = true
)

