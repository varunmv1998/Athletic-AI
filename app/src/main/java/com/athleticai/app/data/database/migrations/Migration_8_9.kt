package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create Folder table
        database.execSQL("""
            CREATE TABLE folders (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                createdDate INTEGER NOT NULL,
                isExpanded INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Create WorkoutRoutine table
        database.execSQL("""
            CREATE TABLE workout_routines (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                folderId TEXT,
                lastPerformed INTEGER,
                createdDate INTEGER NOT NULL,
                notes TEXT NOT NULL,
                FOREIGN KEY (folderId) REFERENCES folders (id) ON DELETE SET NULL
            )
        """.trimIndent())
        
        // Create RoutineExercise table
        database.execSQL("""
            CREATE TABLE routine_exercises (
                id TEXT NOT NULL PRIMARY KEY,
                routineId TEXT NOT NULL,
                exerciseId TEXT NOT NULL,
                orderIndex INTEGER NOT NULL,
                defaultSets TEXT NOT NULL,
                restTimerEnabled INTEGER NOT NULL,
                restSeconds INTEGER NOT NULL,
                notes TEXT NOT NULL,
                FOREIGN KEY (routineId) REFERENCES workout_routines (id) ON DELETE CASCADE,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // Create ActiveWorkoutSession table
        database.execSQL("""
            CREATE TABLE active_workout_sessions (
                id TEXT NOT NULL PRIMARY KEY,
                routineId TEXT,
                routineName TEXT NOT NULL,
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                exercises TEXT NOT NULL,
                isActive INTEGER NOT NULL,
                FOREIGN KEY (routineId) REFERENCES workout_routines (id) ON DELETE SET NULL
            )
        """.trimIndent())
        
        // Create indexes
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_routines_folderId ON workout_routines (folderId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_routineId ON routine_exercises (routineId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_exerciseId ON routine_exercises (exerciseId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_active_workout_sessions_routineId ON active_workout_sessions (routineId)")
    }
}