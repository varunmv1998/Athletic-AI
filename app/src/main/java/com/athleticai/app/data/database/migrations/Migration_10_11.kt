package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns to exercises table for ExerciseDB integration
        database.execSQL("ALTER TABLE exercises ADD COLUMN source TEXT NOT NULL DEFAULT 'LOCAL'")
        database.execSQL("ALTER TABLE exercises ADD COLUMN targetMuscle TEXT")
        database.execSQL("ALTER TABLE exercises ADD COLUMN bodyPart TEXT")
        database.execSQL("ALTER TABLE exercises ADD COLUMN gifUrl TEXT")
        database.execSQL("ALTER TABLE exercises ADD COLUMN gifLocalPath TEXT")
        database.execSQL("ALTER TABLE exercises ADD COLUMN legacyId TEXT")
        database.execSQL("ALTER TABLE exercises ADD COLUMN searchText TEXT")
        
        // Create exercise migration table
        database.execSQL("""
            CREATE TABLE exercise_migration (
                oldId TEXT NOT NULL PRIMARY KEY,
                newId TEXT NOT NULL,
                confidenceScore REAL NOT NULL,
                isManualMapping INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Create exercise sync metadata table
        database.execSQL("""
            CREATE TABLE exercise_sync_metadata (
                id TEXT NOT NULL PRIMARY KEY,
                lastSyncTime INTEGER DEFAULT NULL,
                exerciseCount INTEGER NOT NULL DEFAULT 0,
                isInitialSyncComplete INTEGER NOT NULL DEFAULT 0,
                lastSyncVersion TEXT DEFAULT NULL,
                errorMessage TEXT DEFAULT NULL
            )
        """.trimIndent())
        
        // Create offline downloads table
        database.execSQL("""
            CREATE TABLE offline_downloads (
                exerciseId TEXT NOT NULL PRIMARY KEY,
                downloadedAt INTEGER NOT NULL,
                fileSize INTEGER NOT NULL,
                lastAccessed INTEGER NOT NULL,
                localPath TEXT NOT NULL
            )
        """.trimIndent())
        
        // Create indexes for new tables
        // Note: No index on exercise_migration as entity doesn't define any
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_source ON exercises (source)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_bodyPart ON exercises (bodyPart)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_targetMuscle ON exercises (targetMuscle)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_offline_downloads_lastAccessed ON offline_downloads (lastAccessed)")
        
        // Insert initial sync metadata
        database.execSQL("""
            INSERT INTO exercise_sync_metadata (id, lastSyncTime, exerciseCount, isInitialSyncComplete) 
            VALUES ('sync_status', NULL, 0, 0)
        """.trimIndent())
    }
}