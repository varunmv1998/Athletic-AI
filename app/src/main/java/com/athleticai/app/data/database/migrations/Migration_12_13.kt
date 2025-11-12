package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create program_day_exercises table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS program_day_exercises (
                id TEXT NOT NULL PRIMARY KEY,
                programDayId TEXT NOT NULL,
                exerciseId TEXT NOT NULL,
                orderIndex INTEGER NOT NULL,
                sets INTEGER NOT NULL DEFAULT 3,
                reps TEXT NOT NULL DEFAULT '8-12',
                restSeconds INTEGER NOT NULL DEFAULT 90,
                notes TEXT,
                targetRPE INTEGER,
                isCardio INTEGER NOT NULL DEFAULT 0,
                duration INTEGER,
                distance REAL,
                intensity TEXT,
                FOREIGN KEY(programDayId) REFERENCES program_days(id) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """)
        
        // Create indices for efficient querying
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_day_exercises_programDayId ON program_day_exercises(programDayId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_program_day_exercises_exerciseId ON program_day_exercises(exerciseId)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_program_day_exercises_programDayId_orderIndex ON program_day_exercises(programDayId, orderIndex)")
    }
}