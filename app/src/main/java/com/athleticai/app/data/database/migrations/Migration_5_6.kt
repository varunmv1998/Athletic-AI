package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create custom_programs table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS custom_programs (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                isActive INTEGER NOT NULL DEFAULT 0,
                createdDate INTEGER NOT NULL,
                totalWorkouts INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Create custom_workouts table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS custom_workouts (
                id TEXT NOT NULL PRIMARY KEY,
                programId TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                dayNumber INTEGER NOT NULL,
                estimatedDurationMinutes INTEGER NOT NULL DEFAULT 0,
                exerciseCount INTEGER NOT NULL DEFAULT 0,
                createdDate INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (programId) REFERENCES custom_programs (id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create workout_exercises table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS workout_exercises (
                id TEXT NOT NULL PRIMARY KEY,
                workoutId TEXT NOT NULL,
                exerciseId TEXT NOT NULL,
                orderIndex INTEGER NOT NULL,
                targetSets INTEGER NOT NULL,
                targetReps TEXT NOT NULL,
                rpeTarget REAL NOT NULL,
                restSeconds INTEGER NOT NULL,
                FOREIGN KEY (workoutId) REFERENCES custom_workouts (id) ON DELETE CASCADE,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create exercise_usage_history table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS exercise_usage_history (
                id TEXT NOT NULL PRIMARY KEY,
                exerciseId TEXT NOT NULL,
                usageCount INTEGER NOT NULL DEFAULT 1,
                lastUsedDate INTEGER NOT NULL,
                averageRpe REAL,
                averageSets REAL,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indexes for better query performance
        database.execSQL("CREATE INDEX IF NOT EXISTS index_custom_workouts_programId ON custom_workouts (programId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_workoutId ON workout_exercises (workoutId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_exerciseId ON workout_exercises (exerciseId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_usage_history_exerciseId ON exercise_usage_history (exerciseId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_usage_history_usage ON exercise_usage_history (usageCount DESC, lastUsedDate DESC)")
        
        // Add custom program support to WorkoutSession if needed
        // Check if the column already exists to avoid errors
        try {
            database.execSQL("ALTER TABLE workout_sessions ADD COLUMN customProgramId TEXT")
            database.execSQL("ALTER TABLE workout_sessions ADD COLUMN customWorkoutId TEXT")
        } catch (e: Exception) {
            // Columns might already exist, ignore
        }
    }
}