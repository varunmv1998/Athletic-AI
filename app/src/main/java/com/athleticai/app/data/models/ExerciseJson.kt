package com.athleticai.app.data.models

import com.google.gson.annotations.SerializedName

data class ExerciseJson(
    val id: String,
    val name: String,
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    @SerializedName("primaryMuscles") val primaryMuscles: List<String>,
    @SerializedName("secondaryMuscles") val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val category: String,
    val images: List<String> = emptyList()
)