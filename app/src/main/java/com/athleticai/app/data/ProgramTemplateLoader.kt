package com.athleticai.app.data

import android.content.Context
import android.util.Log
import com.athleticai.app.data.database.dao.*
import com.athleticai.app.data.database.entities.*
import com.google.gson.Gson
import com.athleticai.app.data.models.ProgramTemplateData
import com.athleticai.app.data.models.TemplateProgram
import com.athleticai.app.data.models.TemplateProgramDay
import com.athleticai.app.data.ExerciseNameMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ProgramTemplateLoader(
    private val context: Context,
    private val programDao: ProgramDao,
    private val programDayDao: ProgramDayDao,
    private val programDayExerciseDao: ProgramDayExerciseDao,
    private val programTemplateDao: ProgramTemplateDao,
    private val programQuoteDao: ProgramQuoteDao,
    private val restDayActivityDao: RestDayActivityDao,
    private val exerciseDao: ExerciseDao
) {
    private val TAG = "ProgramTemplateLoader"
    private val gson = Gson()

    suspend fun initializeProgramTemplates() = withContext(Dispatchers.IO) {
        try {
            // Check if templates are already loaded
            val existingCount = programTemplateDao.getBuiltInTemplateCount()
            val existingPrograms = programDao.getAllProgramsSync()
            
            Log.d(TAG, "=== Program Template Initialization ===")
            Log.d(TAG, "Existing templates in DB: $existingCount")
            Log.d(TAG, "Existing programs in DB: ${existingPrograms.size}")
            
            // For debugging, always reload if no programs exist
            if (existingPrograms.isEmpty()) {
                Log.d(TAG, "No programs found, forcing template reload...")
            } else if (existingCount > 0) {
                Log.d(TAG, "Templates already loaded, checking programs...")
                if (existingPrograms.isNotEmpty()) {
                    Log.d(TAG, "Programs exist (${existingPrograms.size}), skipping reload")
                    return@withContext
                }
            }

            Log.d(TAG, "Loading program templates from JSON...")

            // Read JSON file
            val jsonString = readJsonFromAssets("program_templates_json.json")
            Log.d(TAG, "JSON file loaded, size: ${jsonString.length} characters")
            
            val templateData = gson.fromJson(jsonString, ProgramTemplateData::class.java)
            Log.d(TAG, "Parsed ${templateData.programTemplates.size} templates from JSON")

            // Load exercises for mapping
            val allExercises = exerciseDao.getAllExercisesSync()
            if (allExercises.isEmpty()) {
                Log.w(TAG, "No exercises available for mapping, skipping template loading")
                return@withContext
            }

            Log.d(TAG, "Found ${allExercises.size} exercises for mapping")

            // Create exercise name mapper
            val exerciseMapper = ExerciseNameMapper(allExercises)

            // Initialize built-in quotes and activities
            initializeBuiltInQuotes()
            initializeBuiltInRestActivities()

            // Process each template
            var successCount = 0
            var failCount = 0
            
            templateData.programTemplates.forEach { template ->
                try {
                    Log.d(TAG, "Processing template: ${template.name} (${template.id})")
                    
                    val programData = createProgramFromTemplate(template, exerciseMapper)
                    val (program, days, dayExercises) = programData

                    // Store program and related data
                    Log.d(TAG, "Inserting program: ${program.name} with ID: ${program.id}")
                    programDao.insertProgram(program)
                    
                    Log.d(TAG, "Inserting ${days.size} days for program ${program.id}")
                    programDayDao.insertProgramDays(days)
                    
                    if (dayExercises.isNotEmpty()) {
                        Log.d(TAG, "Inserting ${dayExercises.size} exercises for program")
                        programDayExerciseDao.insertExercises(dayExercises)
                    }

                    // Store template for reference
                    val programTemplate = ProgramTemplate(
                        id = UUID.randomUUID().toString(),
                        templateKey = template.id,
                        jsonData = gson.toJson(template),
                        isBuiltIn = true
                    )
                    programTemplateDao.insertTemplate(programTemplate)

                    Log.d(TAG, "✓ Successfully created program: ${program.name} with ${days.size} days and ${dayExercises.size} exercises")
                    successCount++

                } catch (e: Exception) {
                    Log.e(TAG, "✗ Error processing template ${template.id}: ${e.message}", e)
                    failCount++
                }
            }

            // Final summary
            val finalPrograms = programDao.getAllProgramsSync()
            Log.d(TAG, "=== Template Loading Complete ===")
            Log.d(TAG, "Successfully loaded: $successCount programs")
            Log.d(TAG, "Failed: $failCount programs")
            Log.d(TAG, "Total programs in DB: ${finalPrograms.size}")
            Log.d(TAG, "Programs: ${finalPrograms.map { it.name }}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize program templates: ${e.message}", e)
        }
    }

    private suspend fun createProgramFromTemplate(
        template: TemplateProgram, 
        exerciseMapper: ExerciseNameMapper
    ): Triple<Program, List<ProgramDay>, List<ProgramDayExercise>> {
        val programId = UUID.randomUUID().toString()
        
        Log.d(TAG, "Creating program from template: ${template.name}")
        Log.d(TAG, "Template details - Goal: ${template.goal}, Level: ${template.experienceLevel}, Weeks: ${template.durationWeeks}")

        // Map goal and experience level
        val goal = mapGoal(template.goal)
        val experienceLevel = mapExperienceLevel(template.experienceLevel)

        val program = Program(
            id = programId,
            name = template.name,
            description = template.description,
            goal = goal,
            experienceLevel = experienceLevel,
            durationWeeks = template.durationWeeks,
            workoutsPerWeek = template.frequencyPerWeek,
            equipmentRequired = template.equipmentRequired,
            isCustom = false,
            createdBy = null
        )

        val days = mutableListOf<ProgramDay>()
        val dayExercises = mutableListOf<ProgramDayExercise>()

        // Generate full program schedule
        generateProgramSchedule(template, programId, exerciseMapper, days, dayExercises)

        return Triple(program, days, dayExercises)
    }

    private suspend fun generateProgramSchedule(
        template: TemplateProgram,
        programId: String,
        exerciseMapper: ExerciseNameMapper,
        days: MutableList<ProgramDay>,
        dayExercises: MutableList<ProgramDayExercise>
    ) {
        // Get the weekly template - for now we use the first week as the pattern for all weeks
        val weekTemplate = template.programDays.firstOrNull() ?: return
        
        // Extract all exercise names for logging
        val allExerciseNames = mutableSetOf<String>()
        weekTemplate.getAllDays().forEach { (_, dayData) ->
            dayData?.exercises?.let { allExerciseNames.addAll(it) }
        }
        exerciseMapper.logMappingResults(allExerciseNames.toList())
        
        // Generate schedule for all weeks using the template pattern
        for (week in 1..template.durationWeeks) {
            weekTemplate.getAllDays().forEach { (dayOfWeek, dayData) ->
                val dayNumber = (week - 1) * 7 + dayOfWeek
                val dayId = UUID.randomUUID().toString()

                val dayType = mapDayType(dayData?.dayType ?: "rest")
                
                val day = ProgramDay(
                    id = dayId,
                    programId = programId,
                    dayNumber = dayNumber,
                    weekNumber = week,
                    dayOfWeek = dayOfWeek,
                    dayType = dayType,
                    routineId = null, // We'll create routines from exercises later
                    name = dayData?.workoutName ?: when(dayType) {
                        DayType.WORKOUT -> "Workout Day"
                        DayType.REST -> "Rest Day"
                        DayType.ACTIVE_RECOVERY -> "Active Recovery"
                        else -> "Day $dayNumber"
                    },
                    description = template.progressionNotes,
                    targetMuscleGroups = extractMuscleGroupsFromExercises(dayData?.exercises ?: emptyList())
                )

                days.add(day)

                // Add exercises for workout days
                if (dayType == DayType.WORKOUT && !dayData?.exercises.isNullOrEmpty()) {
                    dayData.exercises!!.forEachIndexed { index, exerciseName ->
                        val mappedExercise = exerciseMapper.findExercise(exerciseName)
                        if (mappedExercise != null) {
                            dayExercises.add(
                                ProgramDayExercise(
                                    id = UUID.randomUUID().toString(),
                                    programDayId = dayId,
                                    exerciseId = mappedExercise.id,
                                    orderIndex = index,
                                    sets = determineDefaultSets(exerciseName),
                                    reps = determineDefaultReps(exerciseName),
                                    restSeconds = determineDefaultRest(exerciseName),
                                    targetRPE = determineDefaultRPE(template.experienceLevel),
                                    isCardio = isCardioExercise(exerciseName)
                                )
                            )
                        } else {
                            Log.w(TAG, "Could not map exercise: $exerciseName")
                        }
                    }
                }
            }
        }
    }

    // Helper functions for mapping template data
    private fun mapGoal(goal: String): ProgramGoal = when (goal.lowercase()) {
        "fat_loss" -> ProgramGoal.FAT_LOSS
        "muscle_building" -> ProgramGoal.MUSCLE_BUILDING
        "general_fitness" -> ProgramGoal.GENERAL_FITNESS
        "strength" -> ProgramGoal.STRENGTH
        "endurance" -> ProgramGoal.ENDURANCE
        "athletic_performance" -> ProgramGoal.ATHLETIC_PERFORMANCE
        else -> ProgramGoal.OTHER
    }

    private fun mapExperienceLevel(level: String): ExperienceLevel = when (level.lowercase()) {
        "beginner" -> ExperienceLevel.BEGINNER
        "intermediate" -> ExperienceLevel.INTERMEDIATE
        "advanced" -> ExperienceLevel.ADVANCED
        else -> ExperienceLevel.BEGINNER
    }

    private fun mapDayType(dayType: String): DayType = when (dayType.lowercase()) {
        "workout" -> DayType.WORKOUT
        "rest" -> DayType.REST
        "active_recovery" -> DayType.ACTIVE_RECOVERY
        else -> DayType.REST
    }

    // Helper methods for exercise parameters
    private fun determineDefaultSets(exerciseName: String): Int = when {
        exerciseName.contains("plank", ignoreCase = true) -> 3
        exerciseName.contains("cardio", ignoreCase = true) -> 1
        exerciseName.contains("warm", ignoreCase = true) -> 1
        else -> 3
    }

    private fun determineDefaultReps(exerciseName: String): String = when {
        exerciseName.contains("plank", ignoreCase = true) -> "30-60 seconds"
        exerciseName.contains("cardio", ignoreCase = true) -> "20-30 minutes"
        exerciseName.contains("run", ignoreCase = true) -> "20-30 minutes"
        exerciseName.contains("walk", ignoreCase = true) -> "30 minutes"
        else -> "8-12"
    }

    private fun determineDefaultRest(exerciseName: String): Int = when {
        exerciseName.contains("cardio", ignoreCase = true) -> 0
        exerciseName.contains("plank", ignoreCase = true) -> 60
        exerciseName.contains("isolation", ignoreCase = true) -> 60
        else -> 90
    }

    private fun determineDefaultRPE(experienceLevel: String): Int? = when (experienceLevel) {
        "beginner" -> 6
        "intermediate" -> 7
        "advanced" -> 8
        else -> null
    }

    private fun isCardioExercise(exerciseName: String): Boolean = 
        exerciseName.contains("cardio", ignoreCase = true) ||
        exerciseName.contains("run", ignoreCase = true) ||
        exerciseName.contains("walk", ignoreCase = true) ||
        exerciseName.contains("bike", ignoreCase = true) ||
        exerciseName.contains("rowing", ignoreCase = true) ||
        exerciseName.contains("jumping", ignoreCase = true) ||
        exerciseName.contains("burpee", ignoreCase = true) ||
        exerciseName.contains("mountain_climber", ignoreCase = true) ||
        exerciseName.contains("battle_rope", ignoreCase = true)

    private fun extractMuscleGroupsFromExercises(exercises: List<String>): List<String> {
        val muscleGroups = mutableSetOf<String>()
        exercises.forEach { exerciseName ->
            when {
                exerciseName.contains("squat", ignoreCase = true) || 
                exerciseName.contains("lunge", ignoreCase = true) -> muscleGroups.add("Legs")
                exerciseName.contains("press", ignoreCase = true) || 
                exerciseName.contains("push", ignoreCase = true) -> muscleGroups.add("Chest")
                exerciseName.contains("pull", ignoreCase = true) || 
                exerciseName.contains("row", ignoreCase = true) -> muscleGroups.add("Back")
                exerciseName.contains("shoulder", ignoreCase = true) || 
                exerciseName.contains("raise", ignoreCase = true) -> muscleGroups.add("Shoulders")
                exerciseName.contains("curl", ignoreCase = true) -> muscleGroups.add("Arms")
                exerciseName.contains("plank", ignoreCase = true) || 
                exerciseName.contains("crunch", ignoreCase = true) -> muscleGroups.add("Core")
                exerciseName.contains("deadlift", ignoreCase = true) -> {
                    muscleGroups.add("Back")
                    muscleGroups.add("Legs")
                }
            }
        }
        return muscleGroups.toList()
    }

    // Read JSON file from assets
    private fun readJsonFromAssets(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    // Initialize built-in quotes
    private suspend fun initializeBuiltInQuotes() {
        val quotes = listOf(
            ProgramQuote("q1", "Great job! Rest well, tomorrow brings new challenges! 💪", "completion"),
            ProgramQuote("q2", "Another day conquered! Your consistency is your superpower!", "completion"),
            ProgramQuote("q3", "Workout complete! You're one step closer to your goals!", "completion"),
            ProgramQuote("q4", "Rest today, but come back stronger tomorrow! 🌟", "skip"),
            ProgramQuote("q5", "Taking care of yourself includes knowing when to rest.", "skip"),
            ProgramQuote("q6", "Tomorrow is a new opportunity to crush it!", "skip"),
            ProgramQuote("q7", "Recovery is where the magic happens! Enjoy your rest day.", "rest_day"),
            ProgramQuote("q8", "Your muscles grow during rest. Embrace it!", "rest_day"),
            ProgramQuote("q9", "Rest day = Growth day. See you tomorrow!", "rest_day")
        )
        
        programQuoteDao.insertQuotes(quotes)
    }

    // Initialize built-in rest day activities
    private suspend fun initializeBuiltInRestActivities() {
        val activities = listOf(
            RestDayActivity(
                id = "rest1",
                activityType = "stretch",
                title = "Full Body Stretch",
                description = "Gentle stretching routine for all major muscle groups",
                duration = 15,
                instructions = "Hold each stretch for 30 seconds, breathe deeply"
            ),
            RestDayActivity(
                id = "rest2",
                activityType = "mobility",
                title = "Joint Mobility",
                description = "Dynamic movements to improve joint range of motion",
                duration = 10,
                instructions = "Perform each movement slowly and controlled"
            ),
            RestDayActivity(
                id = "rest3",
                activityType = "walk",
                title = "Light Walk",
                description = "Easy-paced walk outdoors or on treadmill",
                duration = 20,
                instructions = "Maintain conversational pace, enjoy the movement"
            )
        )
        
        restDayActivityDao.insertActivities(activities)
    }
}