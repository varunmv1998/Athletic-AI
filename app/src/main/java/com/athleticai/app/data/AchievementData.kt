package com.athleticai.app.data

import com.athleticai.app.data.models.*
import java.time.LocalDate

object AchievementData {
    
    fun getAllAchievements(): List<Achievement> {
        return listOf(
            // CONSISTENCY ACHIEVEMENTS
            Achievement(
                id = "consistency_7_day_streak",
                name = "7-Day Warrior",
                description = "Complete 7 consecutive days of workouts",
                category = AchievementCategory.CONSISTENCY,
                icon = "fitness_center",
                requirement = AchievementRequirement.StreakDays(7),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "consistency_30_day_streak",
                name = "30-Day Inferno",
                description = "Complete 30 consecutive days of workouts",
                category = AchievementCategory.CONSISTENCY,
                icon = "local_fire_department",
                requirement = AchievementRequirement.StreakDays(30),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "consistency_90_day_streak",
                name = "90-Day Legend",
                description = "Complete the full 90-day program without missing a day",
                category = AchievementCategory.CONSISTENCY,
                icon = "emoji_events",
                requirement = AchievementRequirement.StreakDays(90),
                rarity = AchievementRarity.EPIC,
                points = 10
            ),
            
            // PERFORMANCE ACHIEVEMENTS
            Achievement(
                id = "performance_first_pr",
                name = "First PR",
                description = "Set your first personal record",
                category = AchievementCategory.PERFORMANCE,
                icon = "trending_up",
                requirement = AchievementRequirement.TotalCount(1),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "performance_pr_hunter",
                name = "PR Hunter",
                description = "Set 10 personal records across different exercises",
                category = AchievementCategory.PERFORMANCE,
                icon = "hunt",
                requirement = AchievementRequirement.TotalCount(10),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "performance_strength_beast",
                name = "Strength Beast",
                description = "Increase weight on 5+ exercises in a single workout",
                category = AchievementCategory.PERFORMANCE,
                icon = "fitness_center",
                requirement = AchievementRequirement.TotalCount(5),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            
            // MONTHLY ACHIEVEMENTS
            Achievement(
                id = "monthly_january_warrior",
                name = "January Warrior",
                description = "Complete 12+ workouts in January",
                category = AchievementCategory.MONTHLY,
                icon = "ac_unit",
                requirement = AchievementRequirement.MonthlyWorkouts(12),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_february_fighter",
                name = "February Fighter",
                description = "Complete 20+ workouts in February",
                category = AchievementCategory.MONTHLY,
                icon = "favorite",
                requirement = AchievementRequirement.MonthlyWorkouts(20),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_march_madness",
                name = "March Madness",
                description = "Complete 25+ workouts in March",
                category = AchievementCategory.MONTHLY,
                icon = "grass",
                requirement = AchievementRequirement.MonthlyWorkouts(25),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_april_power",
                name = "April Power",
                description = "Complete 22+ workouts in April",
                category = AchievementCategory.MONTHLY,
                icon = "eco",
                requirement = AchievementRequirement.MonthlyWorkouts(22),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_may_master",
                name = "May Master",
                description = "Complete 28+ workouts in May",
                category = AchievementCategory.MONTHLY,
                icon = "local_florist",
                requirement = AchievementRequirement.MonthlyWorkouts(28),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_june_juggernaut",
                name = "June Juggernaut",
                description = "Complete 30+ workouts in June",
                category = AchievementCategory.MONTHLY,
                icon = "wb_sunny",
                requirement = AchievementRequirement.MonthlyWorkouts(30),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_july_beast",
                name = "July Beast",
                description = "Complete 25+ workouts in July",
                category = AchievementCategory.MONTHLY,
                icon = "beach_access",
                requirement = AchievementRequirement.MonthlyWorkouts(25),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_august_assassin",
                name = "August Assassin",
                description = "Complete 27+ workouts in August",
                category = AchievementCategory.MONTHLY,
                icon = "local_fire_department",
                requirement = AchievementRequirement.MonthlyWorkouts(27),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_september_savage",
                name = "September Savage",
                description = "Complete 24+ workouts in September",
                category = AchievementCategory.MONTHLY,
                icon = "school",
                requirement = AchievementRequirement.MonthlyWorkouts(24),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_october_overlord",
                name = "October Overlord",
                description = "Complete 26+ workouts in October",
                category = AchievementCategory.MONTHLY,
                icon = "forest",
                requirement = AchievementRequirement.MonthlyWorkouts(26),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_november_ninja",
                name = "November Ninja",
                description = "Complete 23+ workouts in November",
                category = AchievementCategory.MONTHLY,
                icon = "ac_unit",
                requirement = AchievementRequirement.MonthlyWorkouts(23),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_december_destroyer",
                name = "December Destroyer",
                description = "Complete 29+ workouts in December",
                category = AchievementCategory.MONTHLY,
                icon = "ac_unit",
                requirement = AchievementRequirement.MonthlyWorkouts(29),
                rarity = AchievementRarity.COMMON,
                points = 1,
                isRepeatable = true
            ),
            
            // MONTHLY PERFECT STREAKS
            Achievement(
                id = "monthly_perfect_january",
                name = "No Breaks January",
                description = "Work out every single day in January",
                category = AchievementCategory.MONTHLY,
                icon = "star",
                requirement = AchievementRequirement.MonthlyPerfectStreak(1, 2024),
                rarity = AchievementRarity.RARE,
                points = 5,
                isRepeatable = true
            ),
            Achievement(
                id = "monthly_perfect_february",
                name = "February Fighter",
                description = "No missed workout days in February",
                category = AchievementCategory.MONTHLY,
                icon = "star",
                requirement = AchievementRequirement.MonthlyPerfectStreak(2, 2024),
                rarity = AchievementRarity.RARE,
                points = 5,
                isRepeatable = true
            ),
            
            // CUMULATIVE DAYS ACHIEVEMENTS
            Achievement(
                id = "cumulative_10_days",
                name = "10-Day Starter",
                description = "Complete 10 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_one",
                requirement = AchievementRequirement.CumulativeWorkoutDays(10),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "cumulative_25_days",
                name = "25-Day Rising Star",
                description = "Complete 25 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_two",
                requirement = AchievementRequirement.CumulativeWorkoutDays(25),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "cumulative_50_days",
                name = "50-Day Half Century",
                description = "Complete 50 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_3",
                requirement = AchievementRequirement.CumulativeWorkoutDays(50),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "cumulative_100_days",
                name = "100-Day Centurion",
                description = "Complete 100 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_4",
                requirement = AchievementRequirement.CumulativeWorkoutDays(100),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "cumulative_200_days",
                name = "200-Day Double Century",
                description = "Complete 200 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_5",
                requirement = AchievementRequirement.CumulativeWorkoutDays(200),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "cumulative_250_days",
                name = "250-Day Quarter Millennium",
                description = "Complete 250 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_6",
                requirement = AchievementRequirement.CumulativeWorkoutDays(250),
                rarity = AchievementRarity.EPIC,
                points = 10
            ),
            Achievement(
                id = "cumulative_300_days",
                name = "300-Day Triple Century",
                description = "Complete 300 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_7",
                requirement = AchievementRequirement.CumulativeWorkoutDays(300),
                rarity = AchievementRarity.EPIC,
                points = 10
            ),
            Achievement(
                id = "cumulative_500_days",
                name = "500-Day Legend",
                description = "Complete 500 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_8",
                requirement = AchievementRequirement.CumulativeWorkoutDays(500),
                rarity = AchievementRarity.LEGENDARY,
                points = 25
            ),
            Achievement(
                id = "cumulative_1000_days",
                name = "1000-Day Millennium Master",
                description = "Complete 1000 total workout days",
                category = AchievementCategory.CUMULATIVE_DAYS,
                icon = "looks_9",
                requirement = AchievementRequirement.CumulativeWorkoutDays(1000),
                rarity = AchievementRarity.LEGENDARY,
                points = 25
            ),
            
            // PROGRAM COMPLETION ACHIEVEMENTS
            Achievement(
                id = "program_first_complete",
                name = "First Program Complete",
                description = "Finish your first 90-day program",
                category = AchievementCategory.PROGRAM_COMPLETION,
                icon = "flag",
                requirement = AchievementRequirement.ProgramsCompleted(1),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "program_second_complete",
                name = "Second Program Complete",
                description = "Finish your second 90-day program",
                category = AchievementCategory.PROGRAM_COMPLETION,
                icon = "flag",
                requirement = AchievementRequirement.ProgramsCompleted(2),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "program_third_complete",
                name = "Third Program Complete",
                description = "Finish your third 90-day program",
                category = AchievementCategory.PROGRAM_COMPLETION,
                icon = "flag",
                requirement = AchievementRequirement.ProgramsCompleted(3),
                rarity = AchievementRarity.EPIC,
                points = 10
            ),
            Achievement(
                id = "program_veteran",
                name = "Program Veteran",
                description = "Complete 5 full programs",
                category = AchievementCategory.PROGRAM_COMPLETION,
                icon = "military_tech",
                requirement = AchievementRequirement.ProgramsCompleted(5),
                rarity = AchievementRarity.EPIC,
                points = 10
            ),
            Achievement(
                id = "program_master",
                name = "Program Master",
                description = "Complete 10 full programs",
                category = AchievementCategory.PROGRAM_COMPLETION,
                icon = "emoji_events",
                requirement = AchievementRequirement.ProgramsCompleted(10),
                rarity = AchievementRarity.LEGENDARY,
                points = 25
            ),
            
            // SEASONAL ACHIEVEMENTS
            Achievement(
                id = "seasonal_summer_strong",
                name = "Summer Strong",
                description = "Complete 80+ workouts during summer months (Jun-Aug)",
                category = AchievementCategory.SEASONAL,
                icon = "wb_sunny",
                requirement = AchievementRequirement.TimeRange(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 8, 31)
                ),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            Achievement(
                id = "seasonal_winter_warrior",
                name = "Winter Warrior",
                description = "Complete 70+ workouts during winter months (Dec-Feb)",
                category = AchievementCategory.SEASONAL,
                icon = "ac_unit",
                requirement = AchievementRequirement.TimeRange(
                    LocalDate.of(2024, 12, 1),
                    LocalDate.of(2025, 2, 28)
                ),
                rarity = AchievementRarity.RARE,
                points = 5
            ),
            
            // HOLIDAY SPECIALS
            Achievement(
                id = "holiday_new_year",
                name = "New Year Resolution Keeper",
                description = "Start a program on January 1st",
                category = AchievementCategory.HOLIDAY_SPECIALS,
                icon = "celebration",
                requirement = AchievementRequirement.TimeRange(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 1)
                ),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "holiday_birthday",
                name = "Birthday Beast",
                description = "Complete a workout on your birthday",
                category = AchievementCategory.HOLIDAY_SPECIALS,
                icon = "cake",
                requirement = AchievementRequirement.TotalCount(1),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            
            // TIME-BASED CHALLENGES
            Achievement(
                id = "time_early_bird",
                name = "Early Bird",
                description = "Complete 5 workouts before 8 AM",
                category = AchievementCategory.TIME_BASED_CHALLENGES,
                icon = "wb_sunny",
                requirement = AchievementRequirement.TotalCount(5),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "time_night_owl",
                name = "Night Owl",
                description = "Complete 5 workouts after 10 PM",
                category = AchievementCategory.TIME_BASED_CHALLENGES,
                icon = "bedtime",
                requirement = AchievementRequirement.TotalCount(5),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "time_weekend_warrior",
                name = "Weekend Warrior",
                description = "Complete 10 workouts on weekends",
                category = AchievementCategory.TIME_BASED_CHALLENGES,
                icon = "weekend",
                requirement = AchievementRequirement.TotalCount(10),
                rarity = AchievementRarity.COMMON,
                points = 1
            ),
            Achievement(
                id = "time_lunch_break",
                name = "Lunch Break Lifter",
                description = "Complete 5 workouts during lunch hours (11 AM - 2 PM)",
                category = AchievementCategory.TIME_BASED_CHALLENGES,
                icon = "lunch_dining",
                requirement = AchievementRequirement.TotalCount(5),
                rarity = AchievementRarity.COMMON,
                points = 1
            )
        )
    }
    
    fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return getAllAchievements().filter { it.category == category }
    }
    
    fun getAchievementById(id: String): Achievement? {
        return getAllAchievements().find { it.id == id }
    }
}

