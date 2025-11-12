package com.athleticai.app.data.database.dao

import androidx.room.*
import com.athleticai.app.data.database.entities.BodyMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAll(): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<BodyMeasurement?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measurement: BodyMeasurement)

    @Delete
    suspend fun delete(measurement: BodyMeasurement)
    
    @Query("DELETE FROM body_measurements")
    suspend fun deleteAll()
}

