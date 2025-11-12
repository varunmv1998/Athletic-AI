package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.Embedded
import androidx.room.Relation
import com.athleticai.app.data.database.converters.SessionExerciseListConverter

@Entity(
    tableName = "active_workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRoutine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["routineId"])]
)
@TypeConverters(SessionExerciseListConverter::class)
data class ActiveWorkoutSession(
    @PrimaryKey val id: String,
    val routineId: String? = null, // nullable if from empty workout
    val routineName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val exercises: List<SessionExercise> = emptyList(),
    val isActive: Boolean = true
)

// Data class for exercises within a session
data class SessionExercise(
    val exerciseId: String,
    val exerciseName: String, // Cached for display
    val sets: List<PerformedSet> = emptyList(),
    val restTimerActive: Boolean = false,
    val restSeconds: Int = 90,
    val notes: String = ""
)

// Data class for performed sets
data class PerformedSet(
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Float? = null,
    val completed: Boolean = false,
    val restTimerTriggered: Boolean = false,
    val completedTime: Long? = null
)

// For UI display with full exercise info
data class ActiveWorkoutSessionWithDetails(
    @Embedded val session: ActiveWorkoutSession,
    val exerciseDetails: Map<String, Exercise> = emptyMap()
)