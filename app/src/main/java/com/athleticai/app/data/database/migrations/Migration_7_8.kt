package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Complete recreation of custom workout tables to ensure schema alignment
        
        // Drop all custom workout related tables
        database.execSQL("DROP TABLE IF EXISTS exercise_usage_history")
        database.execSQL("DROP TABLE IF EXISTS workout_exercises")
        database.execSQL("DROP TABLE IF EXISTS custom_workouts")
        database.execSQL("DROP TABLE IF EXISTS custom_programs")
        
        // Recreate custom_programs table
        database.execSQL("""
            CREATE TABLE custom_programs (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                isActive INTEGER NOT NULL,
                createdDate INTEGER NOT NULL,
                totalWorkouts INTEGER NOT NULL
            )
        """.trimIndent())

        // Recreate custom_workouts table with index
        database.execSQL("""
            CREATE TABLE custom_workouts (
                id TEXT NOT NULL PRIMARY KEY,
                programId TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                dayNumber INTEGER NOT NULL,
                estimatedDurationMinutes INTEGER NOT NULL,
                exerciseCount INTEGER NOT NULL,
                createdDate INTEGER NOT NULL,
                FOREIGN KEY (programId) REFERENCES custom_programs (id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Recreate workout_exercises table
        database.execSQL("""
            CREATE TABLE workout_exercises (
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

        // Recreate exercise_usage_history table
        database.execSQL("""
            CREATE TABLE exercise_usage_history (
                id TEXT NOT NULL PRIMARY KEY,
                exerciseId TEXT NOT NULL,
                usageCount INTEGER NOT NULL,
                lastUsedDate INTEGER NOT NULL,
                averageRpe REAL,
                averageSets REAL,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indexes
        database.execSQL("CREATE INDEX IF NOT EXISTS index_custom_workouts_programId ON custom_workouts (programId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_workoutId ON workout_exercises (workoutId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_exerciseId ON workout_exercises (exerciseId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_usage_history_exerciseId ON exercise_usage_history (exerciseId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_usage_history_usageCount_lastUsedDate ON exercise_usage_history (usageCount ASC, lastUsedDate ASC)")
    }
}