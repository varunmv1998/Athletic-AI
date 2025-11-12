package com.athleticai.app.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Body measurements
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS body_measurements (
                id TEXT PRIMARY KEY NOT NULL,
                date TEXT NOT NULL,
                weightKg REAL,
                heightCm REAL,
                bodyFatPct REAL,
                waistCm REAL
            )
            """
        )
        // Goals
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goals (
                id TEXT PRIMARY KEY NOT NULL,
                metricType TEXT NOT NULL,
                targetValue REAL NOT NULL,
                targetDate TEXT NOT NULL,
                createdAt TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
            """
        )
        // Personal Records
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS personal_records (
                id TEXT PRIMARY KEY NOT NULL,
                exerciseId TEXT NOT NULL,
                type TEXT NOT NULL,
                value REAL NOT NULL,
                date TEXT NOT NULL
            )
            """
        )
    }
}

