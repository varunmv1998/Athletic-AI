package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks completion of individual program days by users.
 * Records whether a day was completed, skipped, or partially completed.
 */
@Entity(
    tableName = "program_day_completions",
    foreignKeys = [
        ForeignKey(
            entity = UserProgramEnrollment::class,
            parentColumns = ["id"],
            childColumns = ["enrollmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProgramDay::class,
            parentColumns = ["id"],
            childColumns = ["programDayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["enrollmentId"]),
        Index(value = ["programDayId"]),
        Index(value = ["enrollmentId", "programDayNumber"], unique = true)
    ]
)
data class ProgramDayCompletion(
    @PrimaryKey val id: String,
    val enrollmentId: String,
    val programDayId: String,
    val programDayNumber: Int, // The day number in the program (1, 2, 3...)
    val completionDate: Long,
    val status: CompletionStatus,
    val workoutSessionId: String? = null, // Link to WorkoutSession if completed
    val skippedReason: String? = null, // Reason for skipping (optional)
    val notes: String? = null
)

/**
 * Completion status for a program day
 */
enum class CompletionStatus {
    COMPLETED,      // Fully completed the day's workout
    SKIPPED,        // Skipped the day
    PARTIAL,        // Partially completed (some exercises done)
    SUBSTITUTED     // Completed with a different routine
}

/**
 * Summary statistics for program progress
 */
data class ProgramProgressSummary(
    val totalDays: Int,
    val completedDays: Int,
    val skippedDays: Int,
    val partialDays: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val averageWorkoutsPerWeek: Float,
    val estimatedCompletionDate: Long?,
    val progressPercentage: Float
)