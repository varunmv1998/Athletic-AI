package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.athleticai.app.data.database.entities.RestDayActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestDayActivityDao {
    
    @Query("SELECT * FROM rest_day_activities")
    fun getAllActivities(): Flow<List<RestDayActivity>>
    
    @Query("SELECT * FROM rest_day_activities WHERE programDayId = :programDayId")
    suspend fun getActivitiesForDay(programDayId: String): List<RestDayActivity>
    
    @Query("SELECT * FROM rest_day_activities WHERE activityType = :activityType AND isBuiltIn = 1")
    suspend fun getBuiltInActivitiesByType(activityType: String): List<RestDayActivity>
    
    @Query("SELECT * FROM rest_day_activities WHERE isBuiltIn = 1")
    suspend fun getAllBuiltInActivities(): List<RestDayActivity>
    
    @Query("SELECT * FROM rest_day_activities WHERE id = :id")
    suspend fun getActivityById(id: String): RestDayActivity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: RestDayActivity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<RestDayActivity>)
    
    @Update
    suspend fun updateActivity(activity: RestDayActivity)
    
    @Delete
    suspend fun deleteActivity(activity: RestDayActivity)
    
    @Query("SELECT COUNT(*) FROM rest_day_activities WHERE isBuiltIn = 1")
    suspend fun getBuiltInActivityCount(): Int
    
    @Query("DELETE FROM rest_day_activities WHERE isBuiltIn = 0")
    suspend fun deleteCustomActivities()
}