package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create program_enrollment table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_enrollment (
                enrollmentId TEXT PRIMARY KEY NOT NULL,
                programId TEXT NOT NULL,
                startDate TEXT NOT NULL,
                currentDay INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                lastWorkoutDate TEXT,
                completedWorkouts INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // 2. Create program_templates table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_templates (
                templateId TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                weekPhase TEXT NOT NULL,
                dayOrder INTEGER NOT NULL,
                programId TEXT NOT NULL DEFAULT '90day_program_v1'
            )
        """)
        
        // 3. Create program_exercises table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_exercises (
                id TEXT PRIMARY KEY NOT NULL,
                templateId TEXT NOT NULL,
                exerciseId TEXT NOT NULL,
                orderIndex INTEGER NOT NULL,
                sets INTEGER NOT NULL,
                repRangeMin INTEGER NOT NULL,
                repRangeMax INTEGER NOT NULL,
                rpeTarget REAL NOT NULL,
                restSeconds INTEGER NOT NULL,
                progressionType TEXT NOT NULL,
                FOREIGN KEY (templateId) REFERENCES program_templates(templateId) ON DELETE CASCADE,
                FOREIGN KEY (exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """)
        
        // 4. Create user_progression table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS user_progression (
                progressionId TEXT PRIMARY KEY NOT NULL,
                exerciseId TEXT NOT NULL,
                currentWeight REAL NOT NULL,
                lastUpdateDate TEXT NOT NULL,
                lastRpe REAL,
                sessionCount INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """)
        
        // 5. Create exercise_substitutions table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS exercise_substitutions (
                id TEXT PRIMARY KEY NOT NULL,
                primaryExerciseId TEXT NOT NULL,
                substituteExerciseId TEXT NOT NULL,
                muscleGroup TEXT NOT NULL,
                isEquivalent INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (primaryExerciseId) REFERENCES exercises(id) ON DELETE CASCADE,
                FOREIGN KEY (substituteExerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """)
        
        // 6. Add new columns to existing workout_sessions table
        database.execSQL("""
            ALTER TABLE workout_sessions 
            ADD COLUMN programDay INTEGER
        """)
        
        database.execSQL("""
            ALTER TABLE workout_sessions 
            ADD COLUMN templateId TEXT
        """)
        
        database.execSQL("""
            ALTER TABLE workout_sessions 
            ADD COLUMN isCustomWorkout INTEGER NOT NULL DEFAULT 0
        """)
        
        // 7. Create indices for better query performance
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_program_exercises_templateId 
            ON program_exercises(templateId)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_program_exercises_exerciseId 
            ON program_exercises(exerciseId)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_user_progression_exerciseId 
            ON user_progression(exerciseId)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workout_sets_sessionId 
            ON workout_sets(sessionId)
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workout_sets_exerciseId 
            ON workout_sets(exerciseId)
        """)
    }
}