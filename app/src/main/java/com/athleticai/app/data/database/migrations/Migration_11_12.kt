package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create Programs table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS programs (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                goal TEXT NOT NULL,
                experienceLevel TEXT NOT NULL,
                durationWeeks INTEGER NOT NULL,
                workoutsPerWeek INTEGER NOT NULL,
                equipmentRequired TEXT NOT NULL,
                isCustom INTEGER NOT NULL,
                createdBy TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                imageUrl TEXT,
                benefits TEXT NOT NULL,
                targetMuscleGroups TEXT NOT NULL
            )
        """)
        
        // Create ProgramDays table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_days (
                id TEXT PRIMARY KEY NOT NULL,
                programId TEXT NOT NULL,
                dayNumber INTEGER NOT NULL,
                weekNumber INTEGER NOT NULL,
                dayOfWeek INTEGER NOT NULL,
                dayType TEXT NOT NULL,
                routineId TEXT,
                name TEXT NOT NULL,
                description TEXT,
                targetMuscleGroups TEXT NOT NULL,
                notes TEXT,
                FOREIGN KEY(programId) REFERENCES programs(id) ON DELETE CASCADE
            )
        """)
        
        // Create indices for ProgramDays
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_days_programId ON program_days(programId)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_program_days_programId_dayNumber ON program_days(programId, dayNumber)")
        
        // Create UserProgramEnrollments table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS user_program_enrollments (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                programId TEXT NOT NULL,
                enrolledAt INTEGER NOT NULL,
                startedAt INTEGER,
                currentDay INTEGER NOT NULL,
                status TEXT NOT NULL,
                estimatedCompletionDate INTEGER,
                actualCompletionDate INTEGER,
                totalDaysSkipped INTEGER NOT NULL,
                totalDaysCompleted INTEGER NOT NULL,
                lastActivityDate INTEGER,
                notes TEXT,
                FOREIGN KEY(programId) REFERENCES programs(id) ON DELETE CASCADE
            )
        """)
        
        // Create indices for UserProgramEnrollments
        database.execSQL("CREATE INDEX IF NOT EXISTS index_user_program_enrollments_programId ON user_program_enrollments(programId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_user_program_enrollments_userId ON user_program_enrollments(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_user_program_enrollments_status ON user_program_enrollments(status)")
        
        // Create ProgramDayCompletions table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_day_completions (
                id TEXT PRIMARY KEY NOT NULL,
                enrollmentId TEXT NOT NULL,
                programDayId TEXT NOT NULL,
                programDayNumber INTEGER NOT NULL,
                completionDate INTEGER NOT NULL,
                status TEXT NOT NULL,
                workoutSessionId TEXT,
                skippedReason TEXT,
                notes TEXT,
                FOREIGN KEY(enrollmentId) REFERENCES user_program_enrollments(id) ON DELETE CASCADE,
                FOREIGN KEY(programDayId) REFERENCES program_days(id) ON DELETE CASCADE
            )
        """)
        
        // Create indices for ProgramDayCompletions
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_day_completions_enrollmentId ON program_day_completions(enrollmentId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_day_completions_programDayId ON program_day_completions(programDayId)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_program_day_completions_enrollmentId_programDayNumber ON program_day_completions(enrollmentId, programDayNumber)")
    }
}