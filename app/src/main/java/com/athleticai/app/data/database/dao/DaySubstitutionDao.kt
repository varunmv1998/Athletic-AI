package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athleticai.app.data.database.entities.DaySubstitution

@Dao
interface DaySubstitutionDao {

    @Query("SELECT substituteExerciseId FROM day_substitutions WHERE programDay = :programDay AND originalExerciseId = :originalId LIMIT 1")
    suspend fun getSubstitute(programDay: Int, originalId: String): String?

    @Query("SELECT * FROM day_substitutions WHERE programDay = :programDay")
    suspend fun getAllForDay(programDay: Int): List<DaySubstitution>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(substitution: DaySubstitution)

    @Query("DELETE FROM day_substitutions WHERE programDay = :programDay AND originalExerciseId = :originalId")
    suspend fun delete(programDay: Int, originalId: String)

    @Query("DELETE FROM day_substitutions WHERE programDay = :programDay")
    suspend fun clearDay(programDay: Int)
}

