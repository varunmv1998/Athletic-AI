package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "program_enrollment")
data class ProgramEnrollment(
    @PrimaryKey val enrollmentId: String,
    val programId: String, // "90day_program_v1"
    val startDate: LocalDateTime,
    val currentDay: Int, // 1-90
    val isActive: Boolean = true,
    val lastWorkoutDate: LocalDateTime? = null,
    val completedWorkouts: Int = 0
)