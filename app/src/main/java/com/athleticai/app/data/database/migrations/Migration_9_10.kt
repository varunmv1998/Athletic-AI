package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create SupersetGroup table
        database.execSQL("""
            CREATE TABLE superset_groups (
                id TEXT NOT NULL PRIMARY KEY,
                workoutId TEXT NOT NULL,
                groupIndex INTEGER NOT NULL,
                groupType TEXT NOT NULL DEFAULT 'SUPERSET',
                restBetweenExercises INTEGER NOT NULL DEFAULT 10,
                restAfterGroup INTEGER NOT NULL DEFAULT 90,
                rounds INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (workoutId) REFERENCES custom_workouts (id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // Create indexes for superset_groups
        database.execSQL("CREATE INDEX IF NOT EXISTS index_superset_groups_workoutId ON superset_groups (workoutId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_superset_groups_groupIndex ON superset_groups (groupIndex)")
        
        // Recreate workout_exercises table with new columns and foreign key constraints
        // 1. Rename the existing table
        database.execSQL("ALTER TABLE workout_exercises RENAME TO workout_exercises_old")
        
        // 2. Create new table with superset columns and proper foreign keys
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
                supersetGroupId TEXT DEFAULT NULL,
                orderInSuperset INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (workoutId) REFERENCES custom_workouts (id) ON DELETE CASCADE,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE,
                FOREIGN KEY (supersetGroupId) REFERENCES superset_groups (id) ON DELETE SET NULL
            )
        """.trimIndent())
        
        // 3. Copy data from old table to new table
        database.execSQL("""
            INSERT INTO workout_exercises (
                id, workoutId, exerciseId, orderIndex, 
                targetSets, targetReps, rpeTarget, restSeconds,
                supersetGroupId, orderInSuperset
            )
            SELECT 
                id, workoutId, exerciseId, orderIndex,
                targetSets, targetReps, rpeTarget, restSeconds,
                NULL, 0
            FROM workout_exercises_old
        """.trimIndent())
        
        // 4. Drop the old table
        database.execSQL("DROP TABLE workout_exercises_old")
        
        // 5. Create indexes for workout_exercises
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_workoutId ON workout_exercises (workoutId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_exerciseId ON workout_exercises (exerciseId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_supersetGroupId ON workout_exercises (supersetGroupId)")
    }
}