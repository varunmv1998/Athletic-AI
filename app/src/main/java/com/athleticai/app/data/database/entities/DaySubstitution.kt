package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "day_substitutions",
    indices = [
        Index(value = ["programDay"]),
        Index(value = ["programDay", "originalExerciseId"])
    ]
)
data class DaySubstitution(
    @PrimaryKey val id: String,
    val programDay: Int,
    val originalExerciseId: String,
    val substituteExerciseId: String,
    val timestamp: LocalDateTime
)
