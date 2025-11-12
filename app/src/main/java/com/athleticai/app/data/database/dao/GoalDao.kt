package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY targetDate ASC")
    fun getActiveGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)
    
    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}

