package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create achievements table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `requirementType` TEXT NOT NULL,
                `requirementValue` TEXT NOT NULL,
                `rarity` TEXT NOT NULL,
                `points` INTEGER NOT NULL,
                `isRepeatable` INTEGER NOT NULL DEFAULT 0,
                `isUnlocked` INTEGER NOT NULL DEFAULT 0,
                `unlockedAt` TEXT,
                `currentProgress` INTEGER NOT NULL DEFAULT 0,
                `totalRequired` INTEGER NOT NULL DEFAULT 0,
                `month` TEXT,
                `year` INTEGER,
                PRIMARY KEY(`id`)
            )
        """)
        
        // Create user_achievements table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `user_achievements` (
                `id` TEXT NOT NULL,
                `achievementId` TEXT NOT NULL,
                `unlockedAt` TEXT NOT NULL,
                `progress` INTEGER NOT NULL,
                `month` TEXT,
                `year` INTEGER,
                PRIMARY KEY(`id`)
            )
        """)
        
        // Create monthly_stats table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `monthly_stats` (
                `id` TEXT NOT NULL,
                `month` TEXT NOT NULL,
                `year` INTEGER NOT NULL,
                `totalWorkouts` INTEGER NOT NULL,
                `totalWorkoutDays` INTEGER NOT NULL,
                `missedDays` INTEGER NOT NULL,
                `completionRate` REAL NOT NULL,
                `lastUpdated` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        
        // Create cumulative_stats table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `cumulative_stats` (
                `id` TEXT NOT NULL DEFAULT 'user_stats',
                `totalWorkoutDays` INTEGER NOT NULL DEFAULT 0,
                `totalProgramsCompleted` INTEGER NOT NULL DEFAULT 0,
                `currentStreak` INTEGER NOT NULL DEFAULT 0,
                `longestStreak` INTEGER NOT NULL DEFAULT 0,
                `lastUpdated` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        
        // Insert initial cumulative stats
        database.execSQL("""
            INSERT INTO `cumulative_stats` (`id`, `totalWorkoutDays`, `totalProgramsCompleted`, `currentStreak`, `longestStreak`, `lastUpdated`)
            VALUES ('user_stats', 0, 0, 0, 0, '2024-01-01')
        """)
    }
}

