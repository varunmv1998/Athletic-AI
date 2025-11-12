package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.SupersetGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface SupersetGroupDao {
    @Query("SELECT * FROM superset_groups WHERE workoutId = :workoutId ORDER BY groupIndex ASC")
    fun getSupersetGroupsForWorkout(workoutId: String): Flow<List<SupersetGroup>>
    
    @Query("SELECT * FROM superset_groups WHERE id = :groupId")
    suspend fun getSupersetGroupById(groupId: String): SupersetGroup?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupersetGroup(group: SupersetGroup)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupersetGroups(groups: List<SupersetGroup>)
    
    @Update
    suspend fun updateSupersetGroup(group: SupersetGroup)
    
    @Delete
    suspend fun deleteSupersetGroup(group: SupersetGroup)
    
    @Query("DELETE FROM superset_groups WHERE workoutId = :workoutId")
    suspend fun deleteSupersetGroupsForWorkout(workoutId: String)
    
    @Query("UPDATE superset_groups SET groupIndex = :newIndex WHERE id = :groupId")
    suspend fun updateGroupOrder(groupId: String, newIndex: Int)
    
    @Query("SELECT COUNT(*) FROM superset_groups WHERE workoutId = :workoutId")
    suspend fun getGroupCountForWorkout(workoutId: String): Int
    
    @Query("SELECT MAX(groupIndex) FROM superset_groups WHERE workoutId = :workoutId")
    suspend fun getMaxGroupIndex(workoutId: String): Int?
    
    @Query("DELETE FROM superset_groups")
    suspend fun deleteAll()
}