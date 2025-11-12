package com.athleticai.app.data.api

import com.athleticai.app.data.api.models.ExerciseDbDto
import com.athleticai.app.data.api.models.ExerciseDbApiResponse
import com.athleticai.app.data.api.models.ExerciseDbListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ExerciseDB API service interface
 * Base URL: https://v1.exercisedb.dev/api/v1/
 */
interface ExerciseDbApiService {
    
    /**
     * Get all exercises (1500+)
     */
    @GET("exercises")
    suspend fun getAllExercises(
        @Query("limit") limit: Int = 1500,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseDbListResponse>
    
    /**
     * Get exercise by ID
     */
    @GET("exercises/{id}")
    suspend fun getExerciseById(@Path("id") id: String): Response<ExerciseDbApiResponse<ExerciseDbDto>>
    
    /**
     * Search exercises by name
     */
    @GET("exercises/search")
    suspend fun searchByName(
        @Query("q") query: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseDbListResponse>
    
    /**
     * Get exercises filtered by body part
     */
    @GET("exercises")
    suspend fun getByBodyPart(
        @Query("bodyParts") bodyPart: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseDbListResponse>
    
    /**
     * Get exercises filtered by target muscle
     */
    @GET("exercises")
    suspend fun getByTarget(
        @Query("targetMuscles") target: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseDbListResponse>
    
    /**
     * Get exercises filtered by equipment
     */
    @GET("exercises")
    suspend fun getByEquipment(
        @Query("equipments") equipment: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseDbListResponse>
}