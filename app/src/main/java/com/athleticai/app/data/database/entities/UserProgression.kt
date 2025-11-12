package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "user_progression",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserProgression(
    @PrimaryKey val progressionId: String,
    val exerciseId: String, // Foreign key to Exercise
    val currentWeight: Double, // Current working weight
    val lastUpdateDate: LocalDateTime,
    val lastRpe: Double? = null, // Last recorded RPE for this exercise
    val sessionCount: Int = 0 // Number of sessions completed with this exercise
)