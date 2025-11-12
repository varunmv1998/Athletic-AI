package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a user's enrollment in a specific program.
 * Users can only be enrolled in one program at a time.
 */
@Entity(
    tableName = "user_program_enrollments",
    foreignKeys = [
        ForeignKey(
            entity = Program::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["programId"]),
        Index(value = ["userId"]),
        Index(value = ["status"])
    ]
)
data class UserProgramEnrollment(
    @PrimaryKey val id: String,
    val userId: String = "default_user", // For single-user app
    val programId: String,
    val enrolledAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null, // Set when first workout is completed
    val currentDay: Int = 0, // Current program day (0 = not started, 1+ = active)
    val status: EnrollmentStatus = EnrollmentStatus.ENROLLED,
    val estimatedCompletionDate: Long? = null,
    val actualCompletionDate: Long? = null,
    val totalDaysSkipped: Int = 0,
    val totalDaysCompleted: Int = 0,
    val lastActivityDate: Long? = null,
    val notes: String? = null
)

/**
 * Enrollment status
 */
enum class EnrollmentStatus {
    ENROLLED,    // Enrolled but not started
    ACTIVE,      // Started and in progress
    PAUSED,      // Temporarily paused
    COMPLETED,   // Successfully completed
    CANCELLED    // Cancelled/unenrolled before completion
}

/**
 * Data class for UI representation with full program details
 */
data class UserProgramEnrollmentWithProgram(
    @Embedded val enrollment: UserProgramEnrollment,
    @Relation(
        parentColumn = "programId",
        entityColumn = "id"
    )
    val program: Program
)

/**
 * Data class for current program state
 */
data class CurrentProgramState(
    val enrollment: UserProgramEnrollment,
    val program: Program,
    val currentProgramDay: ProgramDay?,
    val completionPercentage: Float,
    val daysRemaining: Int,
    val nextWorkoutDay: ProgramDay?,
    val streak: Int = 0
)