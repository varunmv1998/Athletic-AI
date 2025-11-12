package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Fix custom_workouts table to match the entity structure
        // First, drop the table and recreate it with correct schema
        database.execSQL("DROP TABLE IF EXISTS custom_workouts")
        
        // Recreate with correct schema (no explicit defaults - let Room handle them)
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
        
        // Recreate the index
        database.execSQL("CREATE INDEX IF NOT EXISTS index_custom_workouts_programId ON custom_workouts (programId)")
    }
}