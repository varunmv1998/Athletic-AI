package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "monthly_stats")
data class MonthlyStatsEntity(
    @PrimaryKey val id: String,
    val month: String, // Format: "2024-01"
    val year: Int,
    val totalWorkouts: Int,
    val totalWorkoutDays: Int,
    val missedDays: Int,
    val completionRate: Float,
    val lastUpdated: LocalDate
)

