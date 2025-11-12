package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "program_day_exercises",
    foreignKeys = [
        ForeignKey(
            entity = ProgramDay::class,
            parentColumns = ["id"],
            childColumns = ["programDayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["programDayId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["programDayId", "orderIndex"], unique = true)
    ]
)
data class ProgramDayExercise(
    @PrimaryKey val id: String,
    val programDayId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val sets: Int = 3,
    val reps: String = "8-12", // Can be "8-12", "AMRAP", "30 seconds", etc.
    val restSeconds: Int = 90,
    val notes: String? = null,
    val targetRPE: Int? = null, // 1-10 scale
    val isCardio: Boolean = false,
    val duration: Int? = null, // For cardio, in minutes
    val distance: Float? = null, // For cardio, in km/miles
    val intensity: String? = null // "Easy", "Moderate", "Hard" for cardio
)