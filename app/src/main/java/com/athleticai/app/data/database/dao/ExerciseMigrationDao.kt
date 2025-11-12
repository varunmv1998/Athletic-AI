package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.ExerciseMigration
import com.athleticai.app.data.database.entities.ExerciseSyncMetadata
import com.athleticai.app.data.database.entities.OfflineDownload
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseMigrationDao {
    
    @Query("SELECT * FROM exercise_migration WHERE oldId = :oldId")
    suspend fun getMigrationMapping(oldId: String): ExerciseMigration?
    
    @Query("SELECT * FROM exercise_migration")
    suspend fun getAllMappings(): List<ExerciseMigration>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: ExerciseMigration)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<ExerciseMigration>)
    
    @Query("DELETE FROM exercise_migration WHERE oldId = :oldId")
    suspend fun deleteMappingForOldId(oldId: String)
    
    @Query("DELETE FROM exercise_migration")
    suspend fun clearAllMappings()
}

@Dao
interface ExerciseSyncMetadataDao {
    
    @Query("SELECT * FROM exercise_sync_metadata WHERE id = 'sync_status'")
    suspend fun getSyncMetadata(): ExerciseSyncMetadata?
    
    @Query("SELECT * FROM exercise_sync_metadata WHERE id = 'sync_status'")
    fun getSyncMetadataFlow(): Flow<ExerciseSyncMetadata?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSyncMetadata(metadata: ExerciseSyncMetadata)
    
    @Query("DELETE FROM exercise_sync_metadata")
    suspend fun clearSyncMetadata()
}

@Dao
interface OfflineDownloadDao {
    
    @Query("SELECT * FROM offline_downloads WHERE exerciseId = :exerciseId")
    suspend fun getDownload(exerciseId: String): OfflineDownload?
    
    @Query("SELECT * FROM offline_downloads ORDER BY lastAccessed DESC")
    suspend fun getAllDownloads(): List<OfflineDownload>
    
    @Query("SELECT * FROM offline_downloads WHERE lastAccessed < :cutoffTime")
    suspend fun getOldDownloads(cutoffTime: Long): List<OfflineDownload>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: OfflineDownload)
    
    @Query("UPDATE offline_downloads SET lastAccessed = :accessTime WHERE exerciseId = :exerciseId")
    suspend fun updateLastAccessed(exerciseId: String, accessTime: Long)
    
    @Query("DELETE FROM offline_downloads WHERE exerciseId = :exerciseId")
    suspend fun deleteDownload(exerciseId: String)
    
    @Query("DELETE FROM offline_downloads WHERE lastAccessed < :cutoffTime")
    suspend fun deleteOldDownloads(cutoffTime: Long): Int
    
    @Query("SELECT SUM(fileSize) FROM offline_downloads")
    suspend fun getTotalCacheSize(): Long?
    
    @Query("DELETE FROM offline_downloads")
    suspend fun clearAllDownloads()
}