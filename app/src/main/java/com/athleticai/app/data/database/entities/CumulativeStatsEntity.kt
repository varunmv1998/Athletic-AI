package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "cumulative_stats")
data class CumulativeStatsEntity(
    @PrimaryKey val id: String = "user_stats",
    val totalWorkoutDays: Int,
    val totalProgramsCompleted: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastUpdated: LocalDate
)

