package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.athleticai.app.data.database.converters.ExerciseSetListConverter

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRoutine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
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
        Index(value = ["routineId"]),
        Index(value = ["exerciseId"])
    ]
)
@TypeConverters(ExerciseSetListConverter::class)
data class RoutineExercise(
    @PrimaryKey val id: String,
    val routineId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val defaultSets: List<ExerciseSet> = emptyList(),
    val restTimerEnabled: Boolean = true,
    val restSeconds: Int = 90, // default from user settings
    val notes: String = ""
)

// Embedded data class for exercise sets
data class ExerciseSet(
    val setNumber: Int,
    val targetReps: String, // e.g., "5", "8-10", "12", "AMRAP"
    val previousWeight: Float? = null, // from last workout
    val previousReps: Int? = null // from last workout
)