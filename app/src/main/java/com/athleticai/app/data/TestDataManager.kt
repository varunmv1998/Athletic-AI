package com.athleticai.app.data

import android.content.Context
import android.util.Log
import com.athleticai.app.data.database.AppDatabase
import com.athleticai.app.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages seamless switching between test and production data
 * Handles backup, restore, and data isolation
 */
class TestDataManager(
    private val context: Context,
    private val database: AppDatabase,
    private val testDataGenerator: TestDataGenerator,
    private val settingsRepository: SettingsRepository
) {
    
    private val TAG = "TestDataManager"
    private val BACKUP_DIR = "data_backups"
    private val PROD_BACKUP_NAME = "production_backup.db"
    private val TEST_BACKUP_NAME = "test_backup.db"
    
    /**
     * Switch to test mode
     * 1. Backup current production data
     * 2. Clear database
     * 3. Generate test data
     * 4. Mark as test mode
     */
    suspend fun switchToTestMode(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Switching to test mode...")
            
            // Check if already in test mode
            if (settingsRepository.getTestModeEnabled()) {
                Log.w(TAG, "Already in test mode")
                return@withContext Result.success(Unit)
            }
            
            // Step 1: Backup production data
            Log.d(TAG, "Backing up production data...")
            backupDatabase(PROD_BACKUP_NAME)
            
            // Step 2: Clear current data
            Log.d(TAG, "Clearing current data...")
            clearAllData()
            
            // Step 3: Check if we have a test backup
            val testBackup = getBackupFile(TEST_BACKUP_NAME)
            if (testBackup.exists()) {
                Log.d(TAG, "Restoring previous test data...")
                restoreDatabase(TEST_BACKUP_NAME)
            } else {
                // Step 4: Generate fresh test data
                Log.d(TAG, "Generating fresh test data...")
                testDataGenerator.generateTestData()
            }
            
            // Step 5: Mark as test mode
            settingsRepository.setTestModeEnabled(true)
            
            Log.d(TAG, "Successfully switched to test mode")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to test mode", e)
            
            // Try to restore production data on failure
            try {
                restoreDatabase(PROD_BACKUP_NAME)
                settingsRepository.setTestModeEnabled(false)
            } catch (restoreError: Exception) {
                Log.e(TAG, "Failed to restore production data after error", restoreError)
            }
            
            Result.failure(e)
        }
    }
    
    /**
     * Switch to production mode
     * 1. Backup current test data
     * 2. Clear database
     * 3. Restore production data
     * 4. Mark as production mode
     */
    suspend fun switchToProductionMode(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Switching to production mode...")
            
            // Check if already in production mode
            if (!settingsRepository.getTestModeEnabled()) {
                Log.w(TAG, "Already in production mode")
                return@withContext Result.success(Unit)
            }
            
            // Step 1: Backup test data for future use
            Log.d(TAG, "Backing up test data...")
            backupDatabase(TEST_BACKUP_NAME)
            
            // Step 2: Clear current data
            Log.d(TAG, "Clearing current data...")
            clearAllData()
            
            // Step 3: Restore production data
            val prodBackup = getBackupFile(PROD_BACKUP_NAME)
            if (prodBackup.exists()) {
                Log.d(TAG, "Restoring production data...")
                restoreDatabase(PROD_BACKUP_NAME)
            } else {
                Log.d(TAG, "No production backup found, starting fresh")
            }
            
            // Step 4: Mark as production mode
            settingsRepository.setTestModeEnabled(false)
            
            Log.d(TAG, "Successfully switched to production mode")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to production mode", e)
            
            // Try to restore test data on failure
            try {
                restoreDatabase(TEST_BACKUP_NAME)
                settingsRepository.setTestModeEnabled(true)
            } catch (restoreError: Exception) {
                Log.e(TAG, "Failed to restore test data after error", restoreError)
            }
            
            Result.failure(e)
        }
    }
    
    /**
     * Regenerate test data (only works in test mode)
     */
    suspend fun regenerateTestData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!settingsRepository.getTestModeEnabled()) {
                return@withContext Result.failure(
                    IllegalStateException("Cannot regenerate test data in production mode")
                )
            }
            
            Log.d(TAG, "Regenerating test data...")
            
            // Clear current test data
            clearAllData()
            
            // Generate fresh test data
            testDataGenerator.generateTestData()
            
            Log.d(TAG, "Test data regenerated successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to regenerate test data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if we have a production backup
     */
    fun hasProductionBackup(): Boolean {
        return getBackupFile(PROD_BACKUP_NAME).exists()
    }
    
    /**
     * Check if we have a test backup
     */
    fun hasTestBackup(): Boolean {
        return getBackupFile(TEST_BACKUP_NAME).exists()
    }
    
    /**
     * Get backup statistics
     */
    fun getBackupInfo(): BackupInfo {
        val prodBackup = getBackupFile(PROD_BACKUP_NAME)
        val testBackup = getBackupFile(TEST_BACKUP_NAME)
        
        return BackupInfo(
            hasProductionBackup = prodBackup.exists(),
            productionBackupSize = if (prodBackup.exists()) prodBackup.length() else 0,
            productionBackupDate = if (prodBackup.exists()) prodBackup.lastModified() else 0,
            hasTestBackup = testBackup.exists(),
            testBackupSize = if (testBackup.exists()) testBackup.length() else 0,
            testBackupDate = if (testBackup.exists()) testBackup.lastModified() else 0,
            currentMode = if (settingsRepository.getTestModeEnabled()) "TEST" else "PRODUCTION"
        )
    }
    
    /**
     * Clear all backups (use with caution!)
     */
    suspend fun clearAllBackups(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (backupDir.exists()) {
                backupDir.deleteRecursively()
            }
            Log.d(TAG, "All backups cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear backups", e)
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private suspend fun backupDatabase(backupName: String) {
        database.close()
        
        val dbFile = context.getDatabasePath(database.openHelper.databaseName)
        val backupDir = File(context.filesDir, BACKUP_DIR)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        
        val backupFile = File(backupDir, backupName)
        
        // Also backup the -wal and -shm files if they exist
        val walFile = File(dbFile.parent, "${dbFile.name}-wal")
        val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
        
        // Force a checkpoint to ensure all data is in the main database file
        database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
        
        dbFile.copyTo(backupFile, overwrite = true)
        
        // Copy WAL and SHM files if they exist
        if (walFile.exists()) {
            val walBackup = File(backupDir, "$backupName-wal")
            walFile.copyTo(walBackup, overwrite = true)
        }
        if (shmFile.exists()) {
            val shmBackup = File(backupDir, "$backupName-shm")
            shmFile.copyTo(shmBackup, overwrite = true)
        }
        
        Log.d(TAG, "Database backed up to $backupName (${backupFile.length()} bytes)")
    }
    
    private suspend fun restoreDatabase(backupName: String) {
        database.close()
        
        val dbFile = context.getDatabasePath(database.openHelper.databaseName)
        val backupDir = File(context.filesDir, BACKUP_DIR)
        val backupFile = File(backupDir, backupName)
        
        if (!backupFile.exists()) {
            throw IllegalStateException("Backup file $backupName does not exist")
        }
        
        // Delete existing database files
        dbFile.delete()
        File(dbFile.parent, "${dbFile.name}-wal").delete()
        File(dbFile.parent, "${dbFile.name}-shm").delete()
        
        // Restore the backup
        backupFile.copyTo(dbFile, overwrite = true)
        
        // Restore WAL and SHM files if they exist
        val walBackup = File(backupDir, "$backupName-wal")
        val shmBackup = File(backupDir, "$backupName-shm")
        
        if (walBackup.exists()) {
            val walFile = File(dbFile.parent, "${dbFile.name}-wal")
            walBackup.copyTo(walFile, overwrite = true)
        }
        if (shmBackup.exists()) {
            val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
            shmBackup.copyTo(shmFile, overwrite = true)
        }
        
        Log.d(TAG, "Database restored from $backupName")
    }
    
    private suspend fun clearAllData() {
        database.clearAllTables()
        Log.d(TAG, "All database tables cleared")
    }
    
    private fun getBackupFile(name: String): File {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        return File(backupDir, name)
    }
}

/**
 * Information about current backups
 */
data class BackupInfo(
    val hasProductionBackup: Boolean,
    val productionBackupSize: Long,
    val productionBackupDate: Long,
    val hasTestBackup: Boolean,
    val testBackupSize: Long,
    val testBackupDate: Long,
    val currentMode: String
)