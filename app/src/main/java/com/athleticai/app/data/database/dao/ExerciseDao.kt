package com.athleticai.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.athleticai.app.data.database.entities.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesList(): List<Exercise>
    
    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesSync(): List<Exercise>
    
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): Exercise?
    
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchExercises(query: String): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE category = :category")
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE primaryMuscles LIKE '%' || :muscle || '%' OR secondaryMuscles LIKE '%' || :muscle || '%'")
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE primaryMuscles LIKE '%' || :muscle || '%' OR secondaryMuscles LIKE '%' || :muscle || '%'")
    suspend fun getExercisesByMuscleSync(muscle: String): List<Exercise>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)
    
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
    
    // Advanced search methods for Custom Workout Builder
    
    @Query("""
        SELECT * FROM exercises 
        WHERE (name LIKE '%' || :query || '%' 
            OR category LIKE '%' || :query || '%'
            OR primaryMuscles LIKE '%' || :query || '%'
            OR secondaryMuscles LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            name ASC
        LIMIT :limit
    """)
    suspend fun searchExercisesAdvanced(query: String, limit: Int = 100): List<Exercise>
    
    @Query("""
        SELECT * FROM exercises 
        WHERE equipment = :equipment
        ORDER BY name ASC
        LIMIT :limit
    """)
    suspend fun getExercisesByEquipment(equipment: String, limit: Int = 100): List<Exercise>
    
    @Query("""
        SELECT * FROM exercises 
        WHERE (primaryMuscles LIKE '%' || :muscle || '%' OR secondaryMuscles LIKE '%' || :muscle || '%')
        AND (:equipment IS NULL OR equipment = :equipment)
        ORDER BY 
            CASE WHEN primaryMuscles LIKE '%' || :muscle || '%' THEN 1 ELSE 2 END,
            name ASC
        LIMIT :limit
    """)
    suspend fun getExercisesByMuscleAndEquipment(
        muscle: String, 
        equipment: String? = null, 
        limit: Int = 100
    ): List<Exercise>
    
    @Query("""
        SELECT * FROM exercises 
        WHERE (:muscleGroup IS NULL OR primaryMuscles LIKE '%' || :muscleGroup || '%' OR secondaryMuscles LIKE '%' || :muscleGroup || '%')
        AND (:equipment IS NULL OR equipment = :equipment)
        AND (:category IS NULL OR category = :category)
        AND (:query IS NULL OR name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN :query IS NOT NULL AND name LIKE :query || '%' THEN 1 ELSE 2 END,
            CASE WHEN primaryMuscles LIKE '%' || COALESCE(:muscleGroup, '') || '%' THEN 1 ELSE 2 END,
            name ASC
        LIMIT :limit
    """)
    suspend fun searchExercisesWithFilters(
        query: String? = null,
        muscleGroup: String? = null,
        equipment: String? = null,
        category: String? = null,
        limit: Int = 100
    ): List<Exercise>
    
    @Query("SELECT DISTINCT equipment FROM exercises WHERE equipment IS NOT NULL ORDER BY equipment ASC")
    suspend fun getAllEquipmentTypes(): List<String>
    
    @Query("SELECT DISTINCT category FROM exercises ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("""
        SELECT DISTINCT muscle FROM (
            SELECT TRIM(value) as muscle FROM exercises, json_each(primaryMuscles)
            UNION
            SELECT TRIM(value) as muscle FROM exercises, json_each(secondaryMuscles)
        ) WHERE muscle != '' ORDER BY muscle ASC
    """)
    suspend fun getAllMuscleGroups(): List<String>
    
    // ExerciseDB integration methods
    
    @Query("SELECT * FROM exercises WHERE source = :source")
    suspend fun getExercisesBySource(source: String): List<Exercise>
    
    @Update
    suspend fun updateExercise(exercise: Exercise)
    
    @Query("DELETE FROM exercises WHERE source = :source")
    suspend fun deleteExercisesBySource(source: String)
    
    @Query("SELECT * FROM exercises WHERE bodyPart = :bodyPart ORDER BY name ASC")
    suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise>
    
    @Query("SELECT * FROM exercises WHERE targetMuscle = :target ORDER BY name ASC")
    suspend fun getExercisesByTarget(target: String): List<Exercise>
    
    @Query("SELECT DISTINCT bodyPart FROM exercises WHERE bodyPart IS NOT NULL ORDER BY bodyPart ASC")
    suspend fun getAllBodyParts(): List<String>
    
    @Query("SELECT DISTINCT targetMuscle FROM exercises WHERE targetMuscle IS NOT NULL ORDER BY targetMuscle ASC")
    suspend fun getAllTargets(): List<String>
    
    @Query("""
        SELECT * FROM exercises 
        WHERE (:query IS NULL OR name LIKE '%' || :query || '%' OR searchText LIKE '%' || :query || '%')
        AND (:bodyPart IS NULL OR bodyPart = :bodyPart)
        AND (:target IS NULL OR targetMuscle = :target)
        AND (:equipment IS NULL OR equipment = :equipment)
        ORDER BY 
            CASE WHEN :query IS NOT NULL AND name LIKE :query || '%' THEN 1 ELSE 2 END,
            CASE WHEN source = 'EXERCISE_DB' THEN 1 ELSE 2 END,
            name ASC
        LIMIT :limit
    """)
    suspend fun searchExercisesEnhanced(
        query: String? = null,
        bodyPart: String? = null,
        target: String? = null,
        equipment: String? = null,
        limit: Int = 100
    ): List<Exercise>
}
