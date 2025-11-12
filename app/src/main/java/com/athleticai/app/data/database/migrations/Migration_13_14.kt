package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add program support fields to workout_sessions table
        database.execSQL("""
            ALTER TABLE workout_sessions 
            ADD COLUMN programDayId TEXT
        """)
        database.execSQL("""
            ALTER TABLE workout_sessions 
            ADD COLUMN programEnrollmentId TEXT
        """)
        database.execSQL("""
            ALTER TABLE workout_sessions 
            ADD COLUMN sessionType TEXT DEFAULT 'ROUTINE'
        """)
        
        // Drop and recreate program_templates table with new schema
        // First drop foreign key constraints by dropping dependent table
        database.execSQL("DROP TABLE IF EXISTS program_exercises")
        database.execSQL("DROP TABLE IF EXISTS program_templates")
        
        // Recreate program_templates table with new schema
        database.execSQL("""
            CREATE TABLE program_templates (
                id TEXT NOT NULL PRIMARY KEY,
                templateKey TEXT NOT NULL,
                jsonData TEXT NOT NULL,
                isBuiltIn INTEGER NOT NULL DEFAULT 1,
                lastUpdated INTEGER NOT NULL,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Create rest_day_activities table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS rest_day_activities (
                id TEXT NOT NULL PRIMARY KEY,
                programDayId TEXT,
                activityType TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                duration INTEGER NOT NULL DEFAULT 15,
                videoUrl TEXT,
                instructions TEXT,
                isBuiltIn INTEGER NOT NULL DEFAULT 1
            )
        """)
        
        // Create program_quotes table for motivational messages
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_quotes (
                id TEXT NOT NULL PRIMARY KEY,
                text TEXT NOT NULL,
                category TEXT NOT NULL,
                context TEXT,
                isActive INTEGER NOT NULL DEFAULT 1
            )
        """)
        
        // Create indices for better performance
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sessions_programDayId ON workout_sessions(programDayId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sessions_programEnrollmentId ON workout_sessions(programEnrollmentId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_templates_templateKey ON program_templates(templateKey)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_rest_day_activities_programDayId ON rest_day_activities(programDayId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_quotes_category ON program_quotes(category)")
    }
}