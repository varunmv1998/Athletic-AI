package com.athleticai.app.data

import com.athleticai.app.data.database.dao.ProgramDao
import com.athleticai.app.data.database.dao.ProgramDayDao
import com.athleticai.app.data.database.dao.ProgramDayExerciseDao
import com.athleticai.app.data.database.dao.ExerciseDao
import com.athleticai.app.data.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Creates sample program data with exercises for demonstration purposes
 */
class SampleProgramDataWithExercises(
    private val programDao: ProgramDao,
    private val programDayDao: ProgramDayDao,
    private val programDayExerciseDao: ProgramDayExerciseDao,
    private val exerciseDao: ExerciseDao
) {
    
    suspend fun initializeSamplePrograms() = withContext(Dispatchers.IO) {
        // Check if programs already exist
        val existingCount = programDao.getProgramCount()
        if (existingCount > 0) {
            return@withContext // Already have programs
        }
        
        // Get exercises from database for program creation
        val allExercises = exerciseDao.getAllExercisesSync()
        if (allExercises.isEmpty()) {
            return@withContext // No exercises available
        }
        
        // Create sample programs with exercises
        val programs = listOf(
            createBeginnerFatLossProgram(allExercises),
            createIntermediateMuscleProgram(allExercises),
            createBeginnerGeneralFitnessProgram(allExercises)
        )
        
        programs.forEach { (program, days, dayExercises) ->
            programDao.insertProgram(program)
            programDayDao.insertProgramDays(days)
            programDayExerciseDao.insertExercises(dayExercises)
        }
    }
    
    private fun createBeginnerFatLossProgram(exercises: List<Exercise>): Triple<Program, List<ProgramDay>, List<ProgramDayExercise>> {
        val programId = UUID.randomUUID().toString()
        
        val program = Program(
            id = programId,
            name = "Beginner Fat Loss Program",
            description = "A 8-week program designed for beginners to lose fat through a combination of cardio and strength training. Perfect for those new to fitness.",
            goal = ProgramGoal.FAT_LOSS,
            experienceLevel = ExperienceLevel.BEGINNER,
            durationWeeks = 8,
            workoutsPerWeek = 4,
            equipmentRequired = listOf(EquipmentCategories.DUMBBELLS, EquipmentCategories.BODYWEIGHT_ONLY),
            isCustom = false,
            createdBy = null,
            benefits = listOf(
                "Progressive fat loss",
                "Improved cardiovascular health",
                "Build foundational strength",
                "Establish workout habits"
            ),
            targetMuscleGroups = listOf("Full Body", "Core", "Cardio")
        )
        
        val days = mutableListOf<ProgramDay>()
        val dayExercises = mutableListOf<ProgramDayExercise>()
        
        // Find exercises by category
        val chestExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("chest", ignoreCase = true) } }
        val backExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("back", ignoreCase = true) || m.contains("lats", ignoreCase = true) } }
        val legExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("quadriceps", ignoreCase = true) || m.contains("hamstrings", ignoreCase = true) || m.contains("glutes", ignoreCase = true) } }
        val shoulderExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("shoulders", ignoreCase = true) } }
        val armExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("biceps", ignoreCase = true) || m.contains("triceps", ignoreCase = true) } }
        val coreExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("abdominals", ignoreCase = true) || m.contains("abs", ignoreCase = true) } }
        val cardioExercises = exercises.filter { it.category.contains("cardio", ignoreCase = true) || it.name.contains("run", ignoreCase = true) || it.name.contains("jog", ignoreCase = true) }
        
        // Create a basic cardio exercise if none found
        val treadmillRun = Exercise(
            id = "cardio-treadmill-run",
            name = "Treadmill Run",
            force = null,
            level = "beginner",
            mechanic = null,
            equipment = "treadmill",
            primaryMuscles = listOf("cardiovascular system"),
            secondaryMuscles = listOf("legs"),
            instructions = listOf("Start with a 5-minute warm-up walk", "Gradually increase speed to a comfortable running pace", "Maintain steady breathing", "Cool down with 5-minute walk"),
            category = "cardio"
        )
        
        // Generate 8 weeks of workouts (4 days per week + 3 rest days)
        for (week in 1..8) {
            for (dayOfWeek in 1..7) {
                val dayNumber = (week - 1) * 7 + dayOfWeek
                val dayId = UUID.randomUUID().toString()
                
                val (dayType, name, description) = when (dayOfWeek) {
                    1 -> Triple(DayType.WORKOUT, "Full Body Strength", "Focus on compound movements")
                    2 -> Triple(DayType.WORKOUT, "Cardio & Core", "HIIT cardio with core work")
                    3 -> Triple(DayType.REST, "Rest Day", "Recovery and stretching")
                    4 -> Triple(DayType.WORKOUT, "Upper Body", "Arms, chest, back, and shoulders")
                    5 -> Triple(DayType.WORKOUT, "Lower Body & Cardio", "Legs and glutes with cardio finisher")
                    6 -> Triple(DayType.ACTIVE_RECOVERY, "Active Recovery", "Light walking or yoga")
                    7 -> Triple(DayType.REST, "Rest Day", "Complete rest")
                    else -> Triple(DayType.REST, "Rest Day", "Recovery")
                }
                
                days.add(
                    ProgramDay(
                        id = dayId,
                        programId = programId,
                        dayNumber = dayNumber,
                        weekNumber = week,
                        dayOfWeek = dayOfWeek,
                        dayType = dayType,
                        routineId = null,
                        name = name,
                        description = description,
                        targetMuscleGroups = when (dayType) {
                            DayType.WORKOUT -> when (dayOfWeek) {
                                1 -> listOf("Full Body")
                                2 -> listOf("Core", "Cardio")
                                4 -> listOf("Chest", "Back", "Arms", "Shoulders")
                                5 -> listOf("Legs", "Glutes")
                                else -> emptyList()
                            }
                            else -> emptyList()
                        }
                    )
                )
                
                // Add exercises for workout days
                if (dayType == DayType.WORKOUT) {
                    when (dayOfWeek) {
                        1 -> { // Full Body Strength
                            addExerciseIfAvailable(dayExercises, dayId, 0, legExercises, "Squats", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 1, chestExercises, "Push-ups", "3", "8-10", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 2, backExercises, "Bent-over Row", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 3, shoulderExercises, "Shoulder Press", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 4, legExercises, "Lunges", "3", "10 each leg", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 5, coreExercises, "Plank", "3", "30-45 seconds", 60)
                        }
                        2 -> { // Cardio & Core
                            // Cardio
                            dayExercises.add(
                                ProgramDayExercise(
                                    id = UUID.randomUUID().toString(),
                                    programDayId = dayId,
                                    exerciseId = treadmillRun.id,
                                    orderIndex = 0,
                                    sets = 1,
                                    reps = "20-30 minutes",
                                    restSeconds = 0,
                                    notes = "Moderate intensity - you should be able to hold a conversation",
                                    isCardio = true,
                                    duration = 25,
                                    intensity = "Moderate"
                                )
                            )
                            // Core exercises
                            addExerciseIfAvailable(dayExercises, dayId, 1, coreExercises, "Crunches", "3", "15-20", 45)
                            addExerciseIfAvailable(dayExercises, dayId, 2, coreExercises, "Russian Twists", "3", "20", 45)
                            addExerciseIfAvailable(dayExercises, dayId, 3, coreExercises, "Leg Raises", "3", "12-15", 45)
                            addExerciseIfAvailable(dayExercises, dayId, 4, coreExercises, "Mountain Climbers", "3", "20", 60)
                        }
                        4 -> { // Upper Body
                            addExerciseIfAvailable(dayExercises, dayId, 0, chestExercises, "Dumbbell Press", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 1, backExercises, "Lat Pulldown", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 2, shoulderExercises, "Lateral Raises", "3", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 3, armExercises, "Bicep Curls", "3", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 4, armExercises, "Tricep Extensions", "3", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 5, chestExercises, "Chest Fly", "3", "12-15", 60)
                        }
                        5 -> { // Lower Body & Cardio
                            addExerciseIfAvailable(dayExercises, dayId, 0, legExercises, "Leg Press", "3", "12-15", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 1, legExercises, "Romanian Deadlifts", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 2, legExercises, "Leg Curls", "3", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 3, legExercises, "Calf Raises", "3", "15-20", 45)
                            // Cardio finisher
                            dayExercises.add(
                                ProgramDayExercise(
                                    id = UUID.randomUUID().toString(),
                                    programDayId = dayId,
                                    exerciseId = treadmillRun.id,
                                    orderIndex = 4,
                                    sets = 1,
                                    reps = "10-15 minutes",
                                    restSeconds = 0,
                                    notes = "Cool-down cardio at easy pace",
                                    isCardio = true,
                                    duration = 12,
                                    intensity = "Easy"
                                )
                            )
                        }
                    }
                }
            }
        }
        
        return Triple(program, days, dayExercises)
    }
    
    private fun createIntermediateMuscleProgram(exercises: List<Exercise>): Triple<Program, List<ProgramDay>, List<ProgramDayExercise>> {
        val programId = UUID.randomUUID().toString()
        
        val program = Program(
            id = programId,
            name = "Intermediate Muscle Building",
            description = "A 12-week hypertrophy-focused program using push/pull/legs split. Designed for intermediate lifters looking to build muscle mass.",
            goal = ProgramGoal.MUSCLE_BUILDING,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            durationWeeks = 12,
            workoutsPerWeek = 5,
            equipmentRequired = listOf(
                EquipmentCategories.BARBELL,
                EquipmentCategories.DUMBBELLS,
                EquipmentCategories.BENCH,
                EquipmentCategories.PULL_UP_BAR
            ),
            isCustom = false,
            createdBy = null,
            benefits = listOf(
                "Maximize muscle growth",
                "Progressive overload",
                "Balanced development",
                "Strength gains"
            ),
            targetMuscleGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms")
        )
        
        val days = mutableListOf<ProgramDay>()
        val dayExercises = mutableListOf<ProgramDayExercise>()
        
        // Find exercises by category
        val chestExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("chest", ignoreCase = true) } }
        val backExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("back", ignoreCase = true) || m.contains("lats", ignoreCase = true) } }
        val legExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("quadriceps", ignoreCase = true) || m.contains("hamstrings", ignoreCase = true) || m.contains("glutes", ignoreCase = true) } }
        val shoulderExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("shoulders", ignoreCase = true) } }
        val bicepExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("biceps", ignoreCase = true) } }
        val tricepExercises = exercises.filter { it.primaryMuscles.any { m -> m.contains("triceps", ignoreCase = true) } }
        
        // Generate 12 weeks with PPL split
        for (week in 1..12) {
            for (dayOfWeek in 1..7) {
                val dayNumber = (week - 1) * 7 + dayOfWeek
                val dayId = UUID.randomUUID().toString()
                
                val (dayType, name, description, muscles) = when (dayOfWeek) {
                    1 -> Quadruple(DayType.WORKOUT, "Push Day", "Chest, shoulders, triceps", listOf("Chest", "Shoulders", "Triceps"))
                    2 -> Quadruple(DayType.WORKOUT, "Pull Day", "Back and biceps", listOf("Back", "Biceps"))
                    3 -> Quadruple(DayType.WORKOUT, "Legs Day", "Quads, hamstrings, glutes", listOf("Legs", "Glutes"))
                    4 -> Quadruple(DayType.REST, "Rest Day", "Recovery", emptyList<String>())
                    5 -> Quadruple(DayType.WORKOUT, "Upper Power", "Heavy compound movements", listOf("Chest", "Back", "Shoulders"))
                    6 -> Quadruple(DayType.WORKOUT, "Lower Power", "Heavy leg work", listOf("Legs", "Glutes"))
                    7 -> Quadruple(DayType.REST, "Rest Day", "Complete rest", emptyList<String>())
                    else -> Quadruple(DayType.REST, "Rest Day", "Recovery", emptyList<String>())
                }
                
                days.add(
                    ProgramDay(
                        id = dayId,
                        programId = programId,
                        dayNumber = dayNumber,
                        weekNumber = week,
                        dayOfWeek = dayOfWeek,
                        dayType = dayType,
                        routineId = null,
                        name = name,
                        description = description,
                        targetMuscleGroups = muscles
                    )
                )
                
                // Add exercises for workout days
                if (dayType == DayType.WORKOUT) {
                    when (dayOfWeek) {
                        1 -> { // Push Day
                            addExerciseIfAvailable(dayExercises, dayId, 0, chestExercises, "Barbell Bench Press", "4", "6-8", 120, targetRPE = 8)
                            addExerciseIfAvailable(dayExercises, dayId, 1, chestExercises, "Incline Dumbbell Press", "3", "8-10", 90, targetRPE = 7)
                            addExerciseIfAvailable(dayExercises, dayId, 2, shoulderExercises, "Overhead Press", "4", "6-8", 120, targetRPE = 8)
                            addExerciseIfAvailable(dayExercises, dayId, 3, shoulderExercises, "Lateral Raises", "4", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 4, tricepExercises, "Close-Grip Bench Press", "3", "8-10", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 5, tricepExercises, "Cable Tricep Extension", "3", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 6, chestExercises, "Cable Fly", "3", "12-15", 60)
                        }
                        2 -> { // Pull Day
                            addExerciseIfAvailable(dayExercises, dayId, 0, backExercises, "Deadlifts", "4", "5-6", 150, targetRPE = 9)
                            addExerciseIfAvailable(dayExercises, dayId, 1, backExercises, "Pull-ups", "4", "6-10", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 2, backExercises, "Barbell Row", "4", "8-10", 90, targetRPE = 7)
                            addExerciseIfAvailable(dayExercises, dayId, 3, backExercises, "Cable Row", "3", "10-12", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 4, bicepExercises, "Barbell Curls", "3", "8-10", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 5, bicepExercises, "Hammer Curls", "3", "10-12", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 6, backExercises, "Face Pulls", "3", "15-20", 45)
                        }
                        3 -> { // Legs Day
                            addExerciseIfAvailable(dayExercises, dayId, 0, legExercises, "Back Squats", "4", "6-8", 150, targetRPE = 8)
                            addExerciseIfAvailable(dayExercises, dayId, 1, legExercises, "Romanian Deadlifts", "4", "8-10", 90, targetRPE = 7)
                            addExerciseIfAvailable(dayExercises, dayId, 2, legExercises, "Leg Press", "3", "10-12", 90)
                            addExerciseIfAvailable(dayExercises, dayId, 3, legExercises, "Leg Curls", "3", "12-15", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 4, legExercises, "Walking Lunges", "3", "12 each leg", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 5, legExercises, "Calf Raises", "4", "15-20", 45)
                        }
                        5 -> { // Upper Power
                            addExerciseIfAvailable(dayExercises, dayId, 0, chestExercises, "Barbell Bench Press", "5", "3-5", 180, targetRPE = 9)
                            addExerciseIfAvailable(dayExercises, dayId, 1, backExercises, "Bent-over Row", "5", "3-5", 150, targetRPE = 9)
                            addExerciseIfAvailable(dayExercises, dayId, 2, shoulderExercises, "Overhead Press", "4", "5-6", 120, targetRPE = 8)
                            addExerciseIfAvailable(dayExercises, dayId, 3, backExercises, "Weighted Pull-ups", "4", "5-6", 120)
                            addExerciseIfAvailable(dayExercises, dayId, 4, chestExercises, "Dips", "3", "6-8", 90)
                        }
                        6 -> { // Lower Power
                            addExerciseIfAvailable(dayExercises, dayId, 0, legExercises, "Back Squats", "5", "3-5", 180, targetRPE = 9)
                            addExerciseIfAvailable(dayExercises, dayId, 1, legExercises, "Sumo Deadlifts", "4", "3-5", 180, targetRPE = 9)
                            addExerciseIfAvailable(dayExercises, dayId, 2, legExercises, "Front Squats", "3", "6-8", 120)
                            addExerciseIfAvailable(dayExercises, dayId, 3, legExercises, "Box Jumps", "4", "5", 60)
                            addExerciseIfAvailable(dayExercises, dayId, 4, legExercises, "Leg Press", "3", "8-10", 90)
                        }
                    }
                }
            }
        }
        
        return Triple(program, days, dayExercises)
    }
    
    private fun createBeginnerGeneralFitnessProgram(exercises: List<Exercise>): Triple<Program, List<ProgramDay>, List<ProgramDayExercise>> {
        val programId = UUID.randomUUID().toString()
        
        val program = Program(
            id = programId,
            name = "Beginner General Fitness",
            description = "A 6-week introductory program to build overall fitness, strength, and establish healthy exercise habits.",
            goal = ProgramGoal.GENERAL_FITNESS,
            experienceLevel = ExperienceLevel.BEGINNER,
            durationWeeks = 6,
            workoutsPerWeek = 3,
            equipmentRequired = listOf(EquipmentCategories.BODYWEIGHT_ONLY),
            isCustom = false,
            createdBy = null,
            benefits = listOf(
                "Build fitness foundation",
                "Improve overall health",
                "Learn proper form",
                "Develop consistency"
            ),
            targetMuscleGroups = listOf("Full Body")
        )
        
        val days = mutableListOf<ProgramDay>()
        val dayExercises = mutableListOf<ProgramDayExercise>()
        
        // Find bodyweight exercises
        val bodyweightExercises = exercises.filter { 
            it.equipment == null || it.equipment.contains("body", ignoreCase = true) || it.equipment == "bodyweight"
        }
        
        // Generate 6 weeks with 3 workouts per week
        for (week in 1..6) {
            for (dayOfWeek in 1..7) {
                val dayNumber = (week - 1) * 7 + dayOfWeek
                val dayId = UUID.randomUUID().toString()
                
                val (dayType, name, description) = when (dayOfWeek) {
                    1 -> Triple(DayType.WORKOUT, "Full Body A", "Basic compound movements")
                    2 -> Triple(DayType.REST, "Rest Day", "Recovery")
                    3 -> Triple(DayType.WORKOUT, "Cardio & Core", "Cardiovascular endurance and core strength")
                    4 -> Triple(DayType.REST, "Rest Day", "Recovery")
                    5 -> Triple(DayType.WORKOUT, "Full Body B", "Different movement patterns")
                    6 -> Triple(DayType.ACTIVE_RECOVERY, "Active Recovery", "Light activity")
                    7 -> Triple(DayType.REST, "Rest Day", "Complete rest")
                    else -> Triple(DayType.REST, "Rest Day", "Recovery")
                }
                
                days.add(
                    ProgramDay(
                        id = dayId,
                        programId = programId,
                        dayNumber = dayNumber,
                        weekNumber = week,
                        dayOfWeek = dayOfWeek,
                        dayType = dayType,
                        routineId = null,
                        name = name,
                        description = description,
                        targetMuscleGroups = when (dayType) {
                            DayType.WORKOUT -> listOf("Full Body")
                            else -> emptyList()
                        }
                    )
                )
                
                // Add exercises for workout days
                if (dayType == DayType.WORKOUT) {
                    when (dayOfWeek) {
                        1 -> { // Full Body A
                            addBodyweightExercise(dayExercises, dayId, 0, "Bodyweight Squats", "3", "10-15", 60)
                            addBodyweightExercise(dayExercises, dayId, 1, "Push-ups", "3", "8-12", 60, "Modify on knees if needed")
                            addBodyweightExercise(dayExercises, dayId, 2, "Walking Lunges", "3", "10 each leg", 60)
                            addBodyweightExercise(dayExercises, dayId, 3, "Plank", "3", "20-30 seconds", 45)
                            addBodyweightExercise(dayExercises, dayId, 4, "Jumping Jacks", "3", "20", 45)
                            addBodyweightExercise(dayExercises, dayId, 5, "Mountain Climbers", "3", "15", 45)
                        }
                        3 -> { // Cardio & Core
                            addCardioExercise(dayExercises, dayId, 0, "Jogging in Place", "1", "15-20 minutes", 0, 17, "Easy to Moderate")
                            addBodyweightExercise(dayExercises, dayId, 1, "Crunches", "3", "15-20", 45)
                            addBodyweightExercise(dayExercises, dayId, 2, "Bicycle Crunches", "3", "15 each side", 45)
                            addBodyweightExercise(dayExercises, dayId, 3, "Leg Raises", "3", "10-12", 45)
                            addBodyweightExercise(dayExercises, dayId, 4, "Side Plank", "2", "15-20 seconds each side", 45)
                        }
                        5 -> { // Full Body B
                            addBodyweightExercise(dayExercises, dayId, 0, "Burpees", "3", "5-8", 90, "Take your time, focus on form")
                            addBodyweightExercise(dayExercises, dayId, 1, "Reverse Lunges", "3", "10 each leg", 60)
                            addBodyweightExercise(dayExercises, dayId, 2, "Pike Push-ups", "3", "8-10", 60)
                            addBodyweightExercise(dayExercises, dayId, 3, "Glute Bridges", "3", "15-20", 45)
                            addBodyweightExercise(dayExercises, dayId, 4, "Superman", "3", "12-15", 45)
                            addBodyweightExercise(dayExercises, dayId, 5, "Wall Sit", "3", "20-30 seconds", 60)
                        }
                    }
                }
            }
        }
        
        return Triple(program, days, dayExercises)
    }
    
    // Helper function to add exercises from available list
    private fun addExerciseIfAvailable(
        dayExercises: MutableList<ProgramDayExercise>,
        dayId: String,
        orderIndex: Int,
        exerciseList: List<Exercise>,
        preferredName: String,
        sets: String,
        reps: String,
        restSeconds: Int,
        notes: String? = null,
        targetRPE: Int? = null
    ) {
        val exercise = exerciseList.firstOrNull { it.name.contains(preferredName, ignoreCase = true) }
            ?: exerciseList.firstOrNull()
        
        if (exercise != null) {
            dayExercises.add(
                ProgramDayExercise(
                    id = UUID.randomUUID().toString(),
                    programDayId = dayId,
                    exerciseId = exercise.id,
                    orderIndex = orderIndex,
                    sets = sets.toIntOrNull() ?: 3,
                    reps = reps,
                    restSeconds = restSeconds,
                    notes = notes,
                    targetRPE = targetRPE,
                    isCardio = false
                )
            )
        }
    }
    
    // Helper function for bodyweight exercises
    private fun addBodyweightExercise(
        dayExercises: MutableList<ProgramDayExercise>,
        dayId: String,
        orderIndex: Int,
        name: String,
        sets: String,
        reps: String,
        restSeconds: Int,
        notes: String? = null
    ) {
        // Create a placeholder exercise ID for bodyweight exercises
        val exerciseId = "bodyweight-${name.lowercase().replace(" ", "-")}"
        
        dayExercises.add(
            ProgramDayExercise(
                id = UUID.randomUUID().toString(),
                programDayId = dayId,
                exerciseId = exerciseId,
                orderIndex = orderIndex,
                sets = sets.toIntOrNull() ?: 3,
                reps = reps,
                restSeconds = restSeconds,
                notes = notes,
                isCardio = false
            )
        )
    }
    
    // Helper function for cardio exercises
    private fun addCardioExercise(
        dayExercises: MutableList<ProgramDayExercise>,
        dayId: String,
        orderIndex: Int,
        name: String,
        sets: String,
        duration: String,
        restSeconds: Int,
        durationMinutes: Int,
        intensity: String
    ) {
        val exerciseId = "cardio-${name.lowercase().replace(" ", "-")}"
        
        dayExercises.add(
            ProgramDayExercise(
                id = UUID.randomUUID().toString(),
                programDayId = dayId,
                exerciseId = exerciseId,
                orderIndex = orderIndex,
                sets = sets.toIntOrNull() ?: 1,
                reps = duration,
                restSeconds = restSeconds,
                isCardio = true,
                duration = durationMinutes,
                intensity = intensity
            )
        )
    }
}

// Helper data class
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)