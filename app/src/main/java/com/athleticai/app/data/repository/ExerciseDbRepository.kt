package com.athleticai.app.data.repository

import android.content.Context
import android.util.Log
import com.athleticai.app.data.api.ExerciseDbApiService
import com.athleticai.app.data.api.ExerciseDbClient
import com.athleticai.app.data.api.models.ExerciseDbDto
import com.athleticai.app.data.api.models.ExerciseSource
import com.athleticai.app.data.api.models.SyncStatus
import com.athleticai.app.data.database.dao.ExerciseDao
import com.athleticai.app.data.database.dao.ExerciseMigrationDao
import com.athleticai.app.data.database.dao.ExerciseSyncMetadataDao
import com.athleticai.app.data.database.dao.OfflineDownloadDao
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.ExerciseMigration
import com.athleticai.app.data.database.entities.ExerciseSyncMetadata
import com.athleticai.app.data.database.entities.OfflineDownload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ExerciseDbRepository(
    private val context: Context,
    private val exerciseDao: ExerciseDao,
    private val migrationDao: ExerciseMigrationDao,
    private val syncMetadataDao: ExerciseSyncMetadataDao,
    private val offlineDownloadDao: OfflineDownloadDao,
    private val settingsRepository: SettingsRepository
) {
    
    private val TAG = "ExerciseDbRepository"
    
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val apiService: ExerciseDbApiService = ExerciseDbClient.apiService
    
    /**
     * Perform initial sync of all ExerciseDB exercises
     */
    suspend fun performInitialSync(): Result<Int> {
        return try {
            _syncStatus.value = _syncStatus.value.copy(isInProgress = true, error = null)
            Log.d(TAG, "Starting initial ExerciseDB sync...")
            
            // Check if sync already completed
            val metadata = syncMetadataDao.getSyncMetadata()
            if (metadata?.isInitialSyncComplete == true) {
                Log.d(TAG, "Initial sync already completed")
                _syncStatus.value = _syncStatus.value.copy(isInProgress = false)
                return Result.success(metadata.exerciseCount)
            }
            
            // Fetch all exercises from ExerciseDB API with pagination
            val allExercises = mutableListOf<ExerciseDbDto>()
            var offset = 0
            val limit = 100 // API maximum limit
            var hasMore = true
            
            while (hasMore) {
                Log.d(TAG, "Fetching exercises: offset=$offset, limit=$limit")
                val response = apiService.getAllExercises(limit = limit, offset = offset)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        val exerciseBatch = responseBody.data
                        allExercises.addAll(exerciseBatch)
                        
                        // Update progress
                        _syncStatus.value = _syncStatus.value.copy(
                            progress = allExercises.size / 1500f // Estimated total
                        )
                        
                        Log.d(TAG, "Fetched batch: ${exerciseBatch.size} exercises (total: ${allExercises.size})")
                        
                        // Check if we got fewer exercises than requested (end of data)
                        hasMore = exerciseBatch.size == limit
                        offset += limit
                    } else {
                        val error = "API returned success=false: ${responseBody?.error}"
                        Log.e(TAG, error)
                        _syncStatus.value = _syncStatus.value.copy(
                            isInProgress = false,
                            error = error
                        )
                        return Result.failure(Exception(error))
                    }
                } else {
                    val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                    Log.e(TAG, errorMsg)
                    _syncStatus.value = _syncStatus.value.copy(
                        isInProgress = false,
                        error = errorMsg
                    )
                    return Result.failure(Exception(errorMsg))
                }
            }
            
            Log.d(TAG, "Fetched total ${allExercises.size} exercises from ExerciseDB")
            
            // Convert to Exercise entities
            val exercises = allExercises.mapIndexed { index, dto ->
                _syncStatus.value = _syncStatus.value.copy(
                    progress = 0.5f + (index.toFloat() / allExercises.size) * 0.5f // 50-100% for conversion
                )
                convertToExercise(dto)
            }
                
            // Insert exercises into database
            exerciseDao.insertExercises(exercises)
            Log.d(TAG, "Inserted ${exercises.size} exercises into database")
            
            // Update sync metadata
            val syncMetadata = ExerciseSyncMetadata(
                lastSyncTime = System.currentTimeMillis(),
                exerciseCount = exercises.size,
                isInitialSyncComplete = true,
                lastSyncVersion = "v1",
                errorMessage = null
            )
            syncMetadataDao.updateSyncMetadata(syncMetadata)
            
            _syncStatus.value = _syncStatus.value.copy(
                isInProgress = false,
                progress = 1f
            )
            
            Log.d(TAG, "ExerciseDB sync completed successfully")
            Result.success(exercises.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error during ExerciseDB sync", e)
            _syncStatus.value = _syncStatus.value.copy(
                isInProgress = false,
                error = e.message
            )
            Result.failure(e)
        }
    }
    
    /**
     * Convert ExerciseDB DTO to Exercise entity
     */
    private fun convertToExercise(dto: ExerciseDbDto): Exercise {
        return Exercise(
            id = "edb_${dto.exerciseId}", // Prefix to avoid conflicts with local exercises
            name = dto.name.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            },
            force = null, // Not provided by ExerciseDB
            level = null, // Not provided by ExerciseDB
            mechanic = null, // Not provided by ExerciseDB
            equipment = dto.equipments.firstOrNull(), // Take first equipment
            primaryMuscles = dto.targetMuscles, // Use all target muscles
            secondaryMuscles = dto.secondaryMuscles,
            instructions = dto.instructions,
            category = dto.bodyParts.firstOrNull() ?: "unknown", // Use first body part as category
            images = emptyList(), // ExerciseDB uses GIFs, not static images
            source = ExerciseSource.EXERCISE_DB.value,
            targetMuscle = dto.targetMuscles.firstOrNull(),
            bodyPart = dto.bodyParts.firstOrNull(),
            gifUrl = dto.gifUrl,
            gifLocalPath = null,
            legacyId = null,
            searchText = "${dto.name} ${dto.targetMuscles.joinToString(" ")} ${dto.bodyParts.joinToString(" ")} ${dto.equipments.joinToString(" ")} ${dto.secondaryMuscles.joinToString(" ")}"
        )
    }
    
    /**
     * Download and cache GIF for offline viewing
     */
    suspend fun downloadGifForExercise(exerciseId: String, gifUrl: String): Result<String> {
        return try {
            Log.d(TAG, "Downloading GIF for exercise: $exerciseId")
            
            // Create local file path
            val gifsDir = File(context.filesDir, "exercise_gifs")
            if (!gifsDir.exists()) {
                gifsDir.mkdirs()
            }
            
            val fileName = "${exerciseId}.gif"
            val localFile = File(gifsDir, fileName)
            
            // Download GIF
            val url = URL(gifUrl)
            url.openStream().use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Record download in database
            val download = OfflineDownload(
                exerciseId = exerciseId,
                downloadedAt = System.currentTimeMillis(),
                fileSize = localFile.length(),
                lastAccessed = System.currentTimeMillis(),
                localPath = localFile.absolutePath
            )
            offlineDownloadDao.insertDownload(download)
            
            // Update exercise with local path
            val exercise = exerciseDao.getExerciseById(exerciseId)
            exercise?.let {
                val updatedExercise = it.copy(gifLocalPath = localFile.absolutePath)
                exerciseDao.updateExercise(updatedExercise)
            }
            
            Log.d(TAG, "Successfully downloaded GIF for exercise: $exerciseId")
            Result.success(localFile.absolutePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading GIF for exercise: $exerciseId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Clean up old cached GIFs (older than 30 days)
     */
    suspend fun cleanupOldGifs(): Int {
        val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
        val oldDownloads = offlineDownloadDao.getOldDownloads(cutoffTime)
        
        var deletedCount = 0
        oldDownloads.forEach { download ->
            try {
                val file = File(download.localPath)
                if (file.exists() && file.delete()) {
                    offlineDownloadDao.deleteDownload(download.exerciseId)
                    deletedCount++
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting cached GIF: ${download.localPath}", e)
            }
        }
        
        Log.d(TAG, "Cleaned up $deletedCount old GIF files")
        return deletedCount
    }
    
    /**
     * Get total cache size
     */
    suspend fun getCacheSize(): Long {
        return offlineDownloadDao.getTotalCacheSize() ?: 0L
    }
    
    /**
     * Clear all cached GIFs
     */
    suspend fun clearAllCache() {
        val downloads = offlineDownloadDao.getAllDownloads()
        downloads.forEach { download ->
            try {
                val file = File(download.localPath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting cached GIF: ${download.localPath}", e)
            }
        }
        offlineDownloadDao.clearAllDownloads()
        Log.d(TAG, "Cleared all cached GIFs")
    }
    
    /**
     * Update last accessed time for a GIF
     */
    suspend fun markGifAccessed(exerciseId: String) {
        offlineDownloadDao.updateLastAccessed(exerciseId, System.currentTimeMillis())
    }
    
    /**
     * Check if initial sync is needed
     */
    suspend fun isInitialSyncNeeded(): Boolean {
        val metadata = syncMetadataDao.getSyncMetadata()
        return metadata?.isInitialSyncComplete != true
    }
    
    /**
     * Get sync metadata as Flow
     */
    fun getSyncMetadataFlow(): Flow<ExerciseSyncMetadata?> {
        return syncMetadataDao.getSyncMetadataFlow()
    }
    
    /**
     * Force re-sync (for settings option)
     */
    suspend fun forceResync(): Result<Int> {
        // Clear existing ExerciseDB exercises
        exerciseDao.deleteExercisesBySource(ExerciseSource.EXERCISE_DB.value)
        
        // Reset sync metadata
        syncMetadataDao.clearSyncMetadata()
        
        // Perform fresh sync
        return performInitialSync()
    }
    
    /**
     * Clear sync metadata (for settings clear option)
     */
    suspend fun clearSyncMetadata() {
        syncMetadataDao.clearSyncMetadata()
    }
}