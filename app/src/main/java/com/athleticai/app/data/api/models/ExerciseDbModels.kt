package com.athleticai.app.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * ExerciseDB API response model
 * Maps to the ExerciseDB API schema:
 * {
 *   "id": "0001",
 *   "name": "3/4 sit-up",
 *   "target": "abs",
 *   "bodyPart": "waist",
 *   "equipment": "body weight",
 *   "gifUrl": "https://...",
 *   "secondaryMuscles": ["obliques"],
 *   "instructions": [...]
 * }
 */
data class ExerciseDbDto(
    @SerializedName("exerciseId")
    val exerciseId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("targetMuscles")
    val targetMuscles: List<String>,
    
    @SerializedName("bodyParts")
    val bodyParts: List<String>,
    
    @SerializedName("equipments")
    val equipments: List<String>,
    
    @SerializedName("gifUrl")
    val gifUrl: String,
    
    @SerializedName("secondaryMuscles")
    val secondaryMuscles: List<String>,
    
    @SerializedName("instructions")
    val instructions: List<String>
)

/**
 * Generic API response wrapper for v1.exercisedb.dev
 */
data class ExerciseDbApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T,
    
    @SerializedName("error")
    val error: Any? = null
)

/**
 * List response with pagination metadata
 */
data class ExerciseDbListResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("metadata")
    val metadata: ExerciseDbMetadata,
    
    @SerializedName("data")
    val data: List<ExerciseDbDto>,
    
    @SerializedName("error")
    val error: Any? = null
)

/**
 * Pagination metadata
 */
data class ExerciseDbMetadata(
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("totalExercises")
    val totalExercises: Int,
    
    @SerializedName("currentPage")
    val currentPage: Int,
    
    @SerializedName("previousPage")
    val previousPage: String?,
    
    @SerializedName("nextPage")
    val nextPage: String?
)

/**
 * Body parts list response
 */
data class BodyPartResponse(
    val bodyParts: List<String>
)

/**
 * Target muscles list response
 */
data class TargetResponse(
    val targets: List<String>
)

/**
 * Equipment list response
 */
data class EquipmentResponse(
    val equipment: List<String>
)

/**
 * Sync status for tracking ExerciseDB integration
 */
data class SyncStatus(
    val isInProgress: Boolean = false,
    val lastSyncTime: Long? = null,
    val exerciseCount: Int = 0,
    val error: String? = null,
    val progress: Float = 0f
)

/**
 * Exercise source enum
 */
enum class ExerciseSource(val value: String) {
    LOCAL("LOCAL"),
    EXERCISE_DB("EXERCISE_DB");
    
    companion object {
        fun fromValue(value: String): ExerciseSource {
            return values().find { it.value == value } ?: LOCAL
        }
    }
}