package com.athleticai.app.data

import android.content.Context
import com.athleticai.app.data.database.entities.ProgramTemplate
import com.athleticai.app.data.database.entities.ProgramExercise
import com.athleticai.app.data.database.entities.ExerciseSubstitution
import com.athleticai.app.data.models.ProgramJson
import com.athleticai.app.data.models.WeekTemplateJson
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import android.util.Log

class ProgramDataLoader(private val context: Context) {
    
    private val gson = Gson()
    private val TAG = "ProgramDataLoader"
    
    suspend fun loadProgramFromAssets(): ProgramParseResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to load program from assets...")
            val json = context.assets.open("program-json.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "Loaded program JSON, size: ${json.length} characters")
            
            val programJson = gson.fromJson(json, ProgramJson::class.java)
            Log.d(TAG, "Parsed program JSON: ${programJson.program.name}")
            Log.d(TAG, "Week templates count: ${programJson.weekTemplates.size}")
            Log.d(TAG, "Workout templates count: ${programJson.workoutTemplates.size}")
            
            // Log workout template details
            programJson.workoutTemplates.forEach { (templateId, template) ->
                Log.d(TAG, "Template $templateId: ${template.name} with ${template.exercises.size} exercises")
                template.exercises.forEach { exercise ->
                    Log.d(TAG, "  - Exercise: ${exercise.exerciseId} (${exercise.exerciseName})")
                }
            }
            
            val templates = createProgramTemplates(programJson)
            Log.d(TAG, "Created ${templates.size} program templates")

            val exercises = createProgramExercises(programJson)
            Log.d(TAG, "Created ${exercises.size} program exercises")
            
            val substitutions = createBasicSubstitutions()
            Log.d(TAG, "Created ${substitutions.size} substitutions")
            
            ProgramParseResult.Success(
                templates = templates,
                exercises = exercises,
                substitutions = substitutions
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load program file: ${e.message}", e)
            e.printStackTrace()
            ProgramParseResult.Error("Failed to load program file: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse program data: ${e.message}", e)
            e.printStackTrace()
            ProgramParseResult.Error("Failed to parse program data: ${e.message}")
        }
    }
    
    private fun createProgramTemplates(programJson: ProgramJson): List<ProgramTemplate> {
        val templates = mutableListOf<ProgramTemplate>()
        
        programJson.weekTemplates.forEach { weekTemplate ->
            val phase = when {
                weekTemplate.name.contains("Foundation") -> "foundation"
                weekTemplate.name.contains("Strength") -> "strength"  
                weekTemplate.name.contains("Intensity") -> "intensity"
                weekTemplate.name.contains("Deload") -> "deload"
                else -> "foundation"
            }
            
            weekTemplate.schedule.forEachIndexed { dayIndex, templateName ->
                if (templateName != "Rest") {
                    val templateId = templateNameToId(templateName)
                    val workoutTemplate = programJson.workoutTemplates[templateId]
                    
                    if (workoutTemplate != null) {
                        templates.add(
                            ProgramTemplate(
                                id = java.util.UUID.randomUUID().toString(),
                                templateKey = templateId,
                                jsonData = com.google.gson.Gson().toJson(workoutTemplate),
                                isBuiltIn = true
                            )
                        )
                        Log.d(TAG, "Added template: $templateId (${workoutTemplate.name})")
                    } else {
                        Log.w(TAG, "No workout template found for: $templateName (ID: $templateId)")
                    }
                }
            }
        }
        
        val distinctTemplates = templates.distinctBy { it.templateKey }
        Log.d(TAG, "Final distinct templates: ${distinctTemplates.size}")
        return distinctTemplates
    }
    
    private fun createProgramExercises(programJson: ProgramJson): List<ProgramExercise> {
        val exercises = mutableListOf<ProgramExercise>()
        
        programJson.workoutTemplates.forEach { (templateId, workoutTemplate) ->
            Log.d(TAG, "Processing exercises for template: $templateId")
            workoutTemplate.exercises.forEachIndexed { index, exerciseJson ->
                val repRange = parseRepRange(exerciseJson.repRange)
                
                val programExercise = ProgramExercise(
                    id = "${templateId}_${exerciseJson.exerciseId}_$index",
                    templateId = templateId,
                    exerciseId = exerciseJson.exerciseId,
                    orderIndex = index,
                    sets = exerciseJson.sets,
                    repRangeMin = repRange.first,
                    repRangeMax = repRange.second,
                    rpeTarget = exerciseJson.rpeTarget,
                    restSeconds = exerciseJson.restSeconds,
                    progressionType = exerciseJson.progressionType
                )
                
                exercises.add(programExercise)
                Log.d(TAG, "Added program exercise: ${programExercise.id} for template $templateId")
            }
        }
        
        Log.d(TAG, "Total program exercises created: ${exercises.size}")
        return exercises
    }
    
    private fun createBasicSubstitutions(): List<ExerciseSubstitution> {
        // Create basic substitution mappings based on common muscle groups
        // This would be expanded based on the actual exercise database
        return listOf(
            ExerciseSubstitution(
                id = "bench_press_sub_1",
                primaryExerciseId = "barbell_bench_press",
                substituteExerciseId = "dumbbell_bench_press",
                muscleGroup = "chest"
            ),
            ExerciseSubstitution(
                id = "pull_up_sub_1", 
                primaryExerciseId = "pull_ups",
                substituteExerciseId = "lat_pulldown",
                muscleGroup = "lats"
            ),
            ExerciseSubstitution(
                id = "squat_sub_1",
                primaryExerciseId = "barbell_squat",
                substituteExerciseId = "leg_press",
                muscleGroup = "quads"
            )
            // Additional substitutions would be added based on exercise analysis
        )
    }
    
    private fun templateNameToId(templateName: String): String {
        val templateId = templateName
            .lowercase()
            .replace(" ", "_")
            .replace("-", "_")
        
        Log.d(TAG, "Converting template name '$templateName' to ID: '$templateId'")
        return templateId
    }
    
    private fun parseRepRange(repRange: String): Pair<Int, Int> {
        return try {
            val parts = repRange.split("-")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else {
                Pair(8, 12) // Default range
            }
        } catch (e: Exception) {
            Pair(8, 12) // Default range on error
        }
    }
}

sealed class ProgramParseResult {
    data class Success(
        val templates: List<ProgramTemplate>,
        val exercises: List<ProgramExercise>,
        val substitutions: List<ExerciseSubstitution>
    ) : ProgramParseResult()
    
    data class Error(val message: String) : ProgramParseResult()
}