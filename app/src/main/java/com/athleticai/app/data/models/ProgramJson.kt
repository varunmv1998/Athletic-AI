package com.athleticai.app.data.models

import com.google.gson.annotations.SerializedName

data class ProgramJson(
    val program: ProgramInfoJson,
    @SerializedName("weekTemplates") val weekTemplates: List<WeekTemplateJson>,
    @SerializedName("workoutTemplates") val workoutTemplates: Map<String, WorkoutTemplateJson>
)

data class ProgramInfoJson(
    val id: String,
    val name: String,
    val description: String,
    val totalWeeks: Int,
    val totalDays: Int,
    val daysPerWeek: Int,
    val splitType: String
)

data class WeekTemplateJson(
    val weekRange: String, // "1-4", "5-8", etc.
    val name: String,
    val focus: String,
    val schedule: List<String> // ["Push A", "Pull A", "Legs A", ...]
)

data class WorkoutTemplateJson(
    val name: String,
    val exercises: List<ProgramExerciseJson>
)

data class ProgramExerciseJson(
    val exerciseId: String,
    val exerciseName: String, // Added missing field
    val sets: Int,
    val repRange: String, // "8-10", "12-15"
    val rpeTarget: Double,
    val restSeconds: Int,
    val progressionType: String // "linear", "double", "volume", "bodyweight"
)