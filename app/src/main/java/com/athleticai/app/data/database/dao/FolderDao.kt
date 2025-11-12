package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>
    
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): Folder?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)
    
    @Update
    suspend fun updateFolder(folder: Folder)
    
    @Delete
    suspend fun deleteFolder(folder: Folder)
    
    @Query("UPDATE folders SET isExpanded = :isExpanded WHERE id = :folderId")
    suspend fun updateFolderExpansion(folderId: String, isExpanded: Boolean)
}