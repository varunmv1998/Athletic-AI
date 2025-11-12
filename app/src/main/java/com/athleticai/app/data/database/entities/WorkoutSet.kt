package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutSet(
    @PrimaryKey val setId: String,
    val sessionId: String,
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val rpe: Double, // 6.0 to 10.0 scale
    val setNumber: Int,
    val timestamp: LocalDateTime,
    val restTimeSeconds: Int? = null
)