package com.athleticai.app

import com.athleticai.app.ui.viewmodels.WorkoutViewModel
import com.athleticai.app.ui.viewmodels.ProgressViewModel
import com.athleticai.app.ui.viewmodels.SettingsViewModel
import com.athleticai.app.ui.viewmodels.AICoachViewModel
import com.athleticai.app.ui.viewmodels.AchievementViewModel
import com.athleticai.app.ui.viewmodels.HomeViewModel
import com.athleticai.app.ui.viewmodels.CustomWorkoutViewModel
import com.athleticai.app.ui.viewmodels.ExerciseSelectionViewModel
import com.athleticai.app.ui.viewmodels.RoutineViewModel
import com.athleticai.app.ui.viewmodels.ExerciseDictionaryViewModel
import com.athleticai.app.ui.viewmodels.ProgramManagementViewModel
import com.athleticai.app.ui.viewmodels.ProgramDetailViewModel
import com.athleticai.app.data.QuoteLoader
import com.athleticai.app.data.TestDataGenerator

class AppContainer(private val application: AthleticAIApplication) {
    
    val workoutViewModel: WorkoutViewModel by lazy {
        WorkoutViewModel(
            exerciseRepository = application.exerciseRepository,
            workoutRepository = application.workoutRepository,
            analyticsRepository = application.analyticsRepository,
            programRepository = application.programRepository,
            routineRepository = application.workoutRoutineRepository,
            programManagementRepository = application.programManagementRepository,
            context = application.applicationContext
        )
    }
    
    val progressViewModel: ProgressViewModel by lazy {
        ProgressViewModel(
            workoutRepository = application.workoutRepository,
            analyticsRepository = application.analyticsRepository,
            measurementsRepository = application.measurementsRepository,
            exerciseRepository = application.exerciseRepository
        )
    }

    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(
            repo = application.settingsRepository,
            aiService = application.aiService,
            testDataGenerator = testDataGenerator,
            exerciseRepository = application.exerciseRepository
        )
    }
    
    private val testDataGenerator: TestDataGenerator by lazy {
        TestDataGenerator(
            context = application.applicationContext,
            workoutRepository = application.workoutRepository,
            measurementsRepository = application.measurementsRepository,
            programRepository = application.programRepository,
            analyticsRepository = application.analyticsRepository,
            achievementRepository = application.achievementRepository,
            exerciseRepository = application.exerciseRepository,
            routineRepository = application.workoutRoutineRepository,
            customProgramRepository = application.customProgramRepository,
            supersetRepository = application.supersetRepository
        )
    }
    
    val aiCoachViewModel: AICoachViewModel by lazy {
        AICoachViewModel(
            aiService = application.aiService,
            workoutRepository = application.workoutRepository,
            measurementsRepository = application.measurementsRepository,
            analyticsRepository = application.analyticsRepository,
            settingsRepository = application.settingsRepository
        )
    }
    
    val achievementViewModel: AchievementViewModel by lazy {
        AchievementViewModel(
            achievementRepository = application.achievementRepository
        )
    }
    
    val homeViewModel: HomeViewModel by lazy {
        HomeViewModel(
            quoteLoader = QuoteLoader(application.applicationContext),
            progressViewModel = progressViewModel
        )
    }
    
    val customWorkoutViewModel: CustomWorkoutViewModel by lazy {
        CustomWorkoutViewModel(
            customProgramRepository = application.customProgramRepository,
            exerciseSearchRepository = application.exerciseSearchRepository,
            supersetRepository = application.supersetRepository
        )
    }
    
    val exerciseSelectionViewModel: ExerciseSelectionViewModel by lazy {
        ExerciseSelectionViewModel(
            exerciseSearchRepository = application.exerciseSearchRepository
        )
    }
    
    val routineViewModel: RoutineViewModel by lazy {
        RoutineViewModel(
            routineRepository = application.workoutRoutineRepository,
            exerciseRepository = application.exerciseRepository
        )
    }
    
    val exerciseDictionaryViewModel: ExerciseDictionaryViewModel by lazy {
        ExerciseDictionaryViewModel(
            exerciseSearchRepository = application.exerciseSearchRepository
        )
    }
    
    val programManagementViewModel: ProgramManagementViewModel by lazy {
        ProgramManagementViewModel(
            repository = application.programManagementRepository
        )
    }
    
    val programDetailViewModel: ProgramDetailViewModel by lazy {
        ProgramDetailViewModel(
            programRepository = application.programManagementRepository,
            exerciseRepository = application.exerciseRepository
        )
    }

    // Expose repositories needed by UI navigations
    val workoutRepositoryRef get() = application.workoutRepository
}
