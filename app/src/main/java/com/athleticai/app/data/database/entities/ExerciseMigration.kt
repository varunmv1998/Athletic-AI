package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Maps old exercise IDs to new ExerciseDB IDs for data migration
 */
@Entity(tableName = "exercise_migration")
data class ExerciseMigration(
    @PrimaryKey val oldId: String,
    val newId: String,
    val confidenceScore: Float, // 0.0 to 1.0 - matching confidence
    val isManualMapping: Boolean = false, // true if user manually confirmed
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tracks sync status and metadata for ExerciseDB integration
 */
@Entity(tableName = "exercise_sync_metadata")
data class ExerciseSyncMetadata(
    @PrimaryKey val id: String = "sync_status",
    val lastSyncTime: Long? = null,
    val exerciseCount: Int = 0,
    val isInitialSyncComplete: Boolean = false,
    val lastSyncVersion: String? = null,
    val errorMessage: String? = null
)

/**
 * Tracks downloaded GIF files for offline viewing
 */
@Entity(tableName = "offline_downloads")
data class OfflineDownload(
    @PrimaryKey val exerciseId: String,
    val downloadedAt: Long,
    val fileSize: Long,
    val lastAccessed: Long,
    val localPath: String
)