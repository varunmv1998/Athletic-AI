package com.athleticai.app.data.models

import java.time.LocalDate

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val icon: String,
    val requirement: AchievementRequirement,
    val rarity: AchievementRarity,
    val points: Int,
    val isRepeatable: Boolean = false,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDate? = null,
    val currentProgress: Int = 0,
    val totalRequired: Int = 0
)

sealed class AchievementRequirement {
    data class StreakDays(val days: Int) : AchievementRequirement()
    data class TotalCount(val count: Int) : AchievementRequirement()
    data class ConsecutiveCount(val count: Int) : AchievementRequirement()
    data class MonthlyWorkouts(val count: Int) : AchievementRequirement()
    data class MonthlyPerfectStreak(val month: Int, val year: Int) : AchievementRequirement()
    data class CumulativeWorkoutDays(val days: Int) : AchievementRequirement()
    data class ProgramsCompleted(val count: Int) : AchievementRequirement()
    data class TimeRange(val startDate: LocalDate, val endDate: LocalDate) : AchievementRequirement()
    data class Combined(val requirements: List<AchievementRequirement>) : AchievementRequirement()
}

enum class AchievementCategory {
    CONSISTENCY,
    PERFORMANCE,
    DIVERSITY,
    SMART_TRAINING,
    BODY_COMPOSITION,
    MILESTONES,
    MONTHLY,
    CUMULATIVE_DAYS,
    PROGRAM_COMPLETION,
    SEASONAL,
    HOLIDAY_SPECIALS,
    TIME_BASED_CHALLENGES
}

enum class AchievementRarity {
    COMMON,     // 1 point
    RARE,       // 5 points
    EPIC,       // 10 points
    LEGENDARY   // 25 points
}

