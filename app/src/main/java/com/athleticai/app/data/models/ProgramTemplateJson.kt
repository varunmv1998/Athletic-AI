package com.athleticai.app.data.models

import com.google.gson.annotations.SerializedName

/**
 * Data models for parsing program_templates_json.json
 * These match the structure defined in the JSON file
 */

data class ProgramTemplateData(
    @SerializedName("program_templates") val programTemplates: List<TemplateProgram>,
    @SerializedName("exercise_database_info") val exerciseDatabaseInfo: ExerciseDatabaseInfo,
    @SerializedName("implementation_notes") val implementationNotes: ImplementationNotes
)

data class TemplateProgram(
    val id: String,
    val name: String,
    val description: String,
    val goal: String, // "fat_loss", "muscle_building", "general_fitness"
    @SerializedName("experience_level") val experienceLevel: String, // "beginner", "intermediate", "advanced"
    @SerializedName("duration_weeks") val durationWeeks: Int,
    @SerializedName("frequency_per_week") val frequencyPerWeek: Int,
    @SerializedName("equipment_required") val equipmentRequired: List<String>,
    @SerializedName("program_days") val programDays: List<TemplateProgramWeek>,
    @SerializedName("progression_notes") val progressionNotes: String
)

data class TemplateProgramWeek(
    val week: Int,
    @SerializedName("day_1") val day1: TemplateProgramDay?,
    @SerializedName("day_2") val day2: TemplateProgramDay?,
    @SerializedName("day_3") val day3: TemplateProgramDay?,
    @SerializedName("day_4") val day4: TemplateProgramDay?,
    @SerializedName("day_5") val day5: TemplateProgramDay?,
    @SerializedName("day_6") val day6: TemplateProgramDay?,
    @SerializedName("day_7") val day7: TemplateProgramDay?
) {
    // Convert to list for easier processing
    fun getAllDays(): List<Pair<Int, TemplateProgramDay?>> = listOf(
        1 to day1, 2 to day2, 3 to day3, 4 to day4, 5 to day5, 6 to day6, 7 to day7
    )
}

data class TemplateProgramDay(
    @SerializedName("day_type") val dayType: String, // "workout", "rest", "active_recovery"
    val exercises: List<String>? = null, // List of exercise names for workout days
    @SerializedName("workout_name") val workoutName: String? = null
)

data class ExerciseDatabaseInfo(
    val note: String,
    @SerializedName("common_exercise_mappings") val commonExerciseMappings: Map<String, List<String>>,
    @SerializedName("equipment_categories") val equipmentCategories: Map<String, String>
)

data class ImplementationNotes(
    @SerializedName("routine_creation") val routineCreation: String,
    @SerializedName("progressive_overload") val progressiveOverload: String,
    @SerializedName("rest_periods") val restPeriods: String,
    val customization: String
)