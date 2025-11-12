package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.athleticai.app.data.database.converters.Converters

/**
 * Represents a structured workout program with defined duration and goals.
 * Programs are time-bound workout plans (4-26 weeks) that users can enroll in.
 */
@Entity(tableName = "programs")
@TypeConverters(Converters::class)
data class Program(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val goal: ProgramGoal,
    val experienceLevel: ExperienceLevel,
    val durationWeeks: Int, // 4-26 weeks
    val workoutsPerWeek: Int, // Frequency
    val equipmentRequired: List<String>, // List of equipment needed
    val isCustom: Boolean, // True if user-created, false if pre-built
    val createdBy: String?, // User ID if custom program
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val imageUrl: String? = null, // Optional image for program
    val benefits: List<String> = emptyList(), // Key benefits of the program
    val targetMuscleGroups: List<String> = emptyList() // Primary muscle groups targeted
)

/**
 * Program goals enum
 */
enum class ProgramGoal {
    FAT_LOSS,
    MUSCLE_BUILDING,
    GENERAL_FITNESS,
    STRENGTH,
    ENDURANCE,
    ATHLETIC_PERFORMANCE,
    OTHER
}

/**
 * Experience level enum
 */
enum class ExperienceLevel {
    BEGINNER,       // 0-6 months experience
    INTERMEDIATE,   // 6 months - 2 years experience
    ADVANCED       // 2+ years experience
}

/**
 * Standard equipment categories
 */
object EquipmentCategories {
    const val BODYWEIGHT_ONLY = "Bodyweight Only"
    const val DUMBBELLS = "Dumbbells"
    const val BARBELL = "Barbell + Weights"
    const val PULL_UP_BAR = "Pull-up Bar"
    const val RESISTANCE_BANDS = "Resistance Bands"
    const val BENCH = "Bench"
    const val SQUAT_RACK = "Squat Rack"
    const val CABLE_MACHINE = "Cable Machine"
    const val CARDIO_EQUIPMENT = "Cardio Equipment"
    const val KETTLEBELLS = "Kettlebells"
    const val OTHER = "Other/Specialized"
}

/**
 * Program status for UI display
 */
enum class ProgramStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    PAUSED
}