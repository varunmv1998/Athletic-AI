package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS day_substitutions (
                id TEXT PRIMARY KEY NOT NULL,
                programDay INTEGER NOT NULL,
                originalExerciseId TEXT NOT NULL,
                substituteExerciseId TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
            """
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_day_substitutions_programDay
            ON day_substitutions(programDay)
            """
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_day_substitutions_day_original
            ON day_substitutions(programDay, originalExerciseId)
            """
        )
    }
}

