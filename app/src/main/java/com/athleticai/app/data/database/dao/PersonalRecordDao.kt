package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.PersonalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND type = :type ORDER BY value DESC, date DESC LIMIT 1")
    suspend fun getBest(exerciseId: String, type: String): PersonalRecord?

    @Query("SELECT * FROM personal_records ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<PersonalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PersonalRecord)
    
    @Query("DELETE FROM personal_records")
    suspend fun deleteAll()
}

