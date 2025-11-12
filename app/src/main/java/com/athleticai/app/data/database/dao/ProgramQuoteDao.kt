package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.athleticai.app.data.database.entities.ProgramQuote
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramQuoteDao {
    
    @Query("SELECT * FROM program_quotes WHERE isActive = 1")
    fun getAllActiveQuotes(): Flow<List<ProgramQuote>>
    
    @Query("SELECT * FROM program_quotes WHERE category = :category AND isActive = 1")
    suspend fun getQuotesByCategory(category: String): List<ProgramQuote>
    
    @Query("SELECT * FROM program_quotes WHERE category = :category AND context = :context AND isActive = 1")
    suspend fun getQuotesByCategoryAndContext(category: String, context: String): List<ProgramQuote>
    
    @Query("SELECT * FROM program_quotes WHERE id = :id")
    suspend fun getQuoteById(id: String): ProgramQuote?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: ProgramQuote)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<ProgramQuote>)
    
    @Update
    suspend fun updateQuote(quote: ProgramQuote)
    
    @Delete
    suspend fun deleteQuote(quote: ProgramQuote)
    
    @Query("SELECT COUNT(*) FROM program_quotes WHERE isActive = 1")
    suspend fun getActiveQuoteCount(): Int
    
    @Query("UPDATE program_quotes SET isActive = 0")
    suspend fun deactivateAllQuotes()
    
    @Query("UPDATE program_quotes SET isActive = :isActive WHERE id = :id")
    suspend fun setQuoteActive(id: String, isActive: Boolean)
}