package com.athleticai.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["source"]),
        Index(value = ["bodyPart"]),
        Index(value = ["targetMuscle"])
    ]
)
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val category: String,
    val images: List<String> = emptyList(),
    // New fields for ExerciseDB integration
    @ColumnInfo(defaultValue = "LOCAL")
    val source: String = "LOCAL", // "LOCAL" or "EXERCISE_DB"
    val targetMuscle: String? = null, // ExerciseDB target muscle
    val bodyPart: String? = null, // ExerciseDB body part
    val gifUrl: String? = null, // ExerciseDB GIF URL
    val gifLocalPath: String? = null, // Local cached GIF path
    val legacyId: String? = null, // Original exercise ID for migration mapping
    val searchText: String? = null // Pre-computed search text for FTS optimization
)