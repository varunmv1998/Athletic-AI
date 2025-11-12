package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = CustomWorkout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupersetGroup::class,
            parentColumns = ["id"],
            childColumns = ["supersetGroupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["supersetGroupId"])
    ]
)
data class WorkoutExercise(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int,
    val targetReps: String, // "8-10", "12", "AMRAP"
    val rpeTarget: Float,
    val restSeconds: Int,
    val supersetGroupId: String? = null,  // Optional FK to SupersetGroup
    val orderInSuperset: Int = 0          // Order within superset group (0-based)
)