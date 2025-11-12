package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.athleticai.app.data.database.entities.ProgramTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramTemplateDao {
    
    @Query("SELECT * FROM program_templates")
    fun getAllTemplates(): Flow<List<ProgramTemplate>>
    
    @Query("SELECT * FROM program_templates WHERE isBuiltIn = 1")
    fun getBuiltInTemplates(): Flow<List<ProgramTemplate>>
    
    @Query("SELECT * FROM program_templates WHERE templateKey = :templateKey")
    suspend fun getTemplateByKey(templateKey: String): ProgramTemplate?
    
    @Query("SELECT * FROM program_templates WHERE id = :id")
    suspend fun getTemplateById(id: String): ProgramTemplate?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ProgramTemplate)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<ProgramTemplate>)
    
    @Update
    suspend fun updateTemplate(template: ProgramTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: ProgramTemplate)
    
    @Query("SELECT COUNT(*) FROM program_templates WHERE isBuiltIn = 1")
    suspend fun getBuiltInTemplateCount(): Int
    
    @Query("DELETE FROM program_templates WHERE isBuiltIn = 0")
    suspend fun deleteCustomTemplates()
}