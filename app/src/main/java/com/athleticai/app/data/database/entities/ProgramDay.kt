package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single day within a program.
 * Each day can have a routine assigned, be a rest day, or active recovery.
 */
@Entity(
    tableName = "program_days",
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
        Index(value = ["programId", "dayNumber"], unique = true)
    ]
)
data class ProgramDay(
    @PrimaryKey val id: String,
    val programId: String,
    val dayNumber: Int, // Day 1, 2, 3... up to total program days
    val weekNumber: Int, // Week 1, 2, 3... calculated from dayNumber
    val dayOfWeek: Int, // 1-7 (Monday-Sunday)
    val dayType: DayType,
    val routineId: String?, // Reference to WorkoutRoutine if it's a workout day
    val name: String, // e.g., "Push Day A", "Rest Day", "Active Recovery"
    val description: String? = null,
    val targetMuscleGroups: List<String> = emptyList(),
    val notes: String? = null
)

/**
 * Type of program day
 */
enum class DayType {
    WORKOUT,        // Regular workout day with routine
    REST,           // Complete rest day
    ACTIVE_RECOVERY, // Light activity day (stretching, yoga, walking)
    OPTIONAL,       // Optional workout day
    DELOAD         // Reduced intensity/volume day
}