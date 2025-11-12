package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey val sessionId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val sessionName: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    // Phase 2 additions
    val programDay: Int? = null, // Day in program (1-90) if this was a program workout
    val templateId: String? = null, // Template used (push_a, pull_b, etc.)
    val isCustomWorkout: Boolean = false, // true if user-created, false if program-based
    // Custom Workout Builder additions
    val customProgramId: String? = null, // Reference to CustomProgram if applicable
    val customWorkoutId: String? = null, // Reference to CustomWorkout if applicable
    // Program Templates additions
    val programDayId: String? = null, // Link to ProgramDay for template-based programs
    val programEnrollmentId: String? = null, // Link to UserProgramEnrollment
    val sessionType: String = "ROUTINE" // ROUTINE, PROGRAM, CUSTOM
)

enum class SessionType {
    ROUTINE,
    PROGRAM, 
    CUSTOM
}