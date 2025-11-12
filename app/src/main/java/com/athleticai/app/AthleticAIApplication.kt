package com.athleticai.app

import android.app.Application
import com.athleticai.app.data.database.AppDatabase
import com.athleticai.app.data.ProgramDataLoader
import com.athleticai.app.data.ExerciseDataLoader
import com.athleticai.app.data.SampleProgramDataWithExercises
import com.athleticai.app.data.ProgramTemplateLoader
import com.athleticai.app.data.repository.ProgramRepository
import com.athleticai.app.data.repository.ProgramManagementRepository
import com.athleticai.app.data.repository.WorkoutRepository
import com.athleticai.app.data.repository.ExerciseRepository
import com.athleticai.app.data.repository.AnalyticsRepository
import com.athleticai.app.data.repository.MeasurementsRepository
import com.athleticai.app.data.repository.SettingsRepository
import com.athleticai.app.data.repository.AchievementRepository
import com.athleticai.app.data.repository.CustomProgramRepository
import com.athleticai.app.data.repository.ExerciseSearchRepository
import com.athleticai.app.data.repository.WorkoutRoutineRepository
import com.athleticai.app.data.repository.SupersetRepository
import com.athleticai.app.data.repository.ExerciseDbRepository
import com.athleticai.app.data.AchievementService
import com.athleticai.app.data.api.AIService
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AthleticAIApplication : Application(), ImageLoaderFactory {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    val programDataLoader by lazy { ProgramDataLoader(this) }
    val exerciseDataLoader by lazy { ExerciseDataLoader(this) }
    
    val programTemplateLoader by lazy {
        ProgramTemplateLoader(
            context = this,
            programDao = database.programDao(),
            programDayDao = database.programDayDao(),
            programDayExerciseDao = database.programDayExerciseDao(),
            programTemplateDao = database.programTemplateDao(),
            programQuoteDao = database.programQuoteDao(),
            restDayActivityDao = database.restDayActivityDao(),
            exerciseDao = database.exerciseDao()
        )
    }
    
    val programRepository by lazy {
        ProgramRepository(
            enrollmentDao = database.programEnrollmentDao(),
            templateDao = database.programTemplateDao(),
            exerciseDao = database.programExerciseDao(),
            progressionDao = database.userProgressionDao(),
            substitutionDao = database.exerciseSubstitutionDao(),
            daySubstitutionDao = database.daySubstitutionDao(),
            programDataLoader = programDataLoader
        )
    }
    
    val workoutRepository by lazy {
        WorkoutRepository(
            sessionDao = database.workoutSessionDao(),
            setDao = database.workoutSetDao()
        )
    }
    
    val exerciseRepository by lazy {
        ExerciseRepository(
            exerciseDao = database.exerciseDao(),
            exerciseDataLoader = exerciseDataLoader,
            exerciseDbRepository = exerciseDbRepository,
            migrationDao = database.exerciseMigrationDao()
        )
    }
    
    val analyticsRepository by lazy {
        AnalyticsRepository(
            sessionDao = database.workoutSessionDao(),
            setDao = database.workoutSetDao(),
            prDao = database.personalRecordDao()
        )
    }
    
    val measurementsRepository by lazy {
        MeasurementsRepository(
            measurementDao = database.bodyMeasurementDao(),
            goalDao = database.goalDao()
        )
    }

    val settingsRepository by lazy { SettingsRepository(this) }
    
    val exerciseDbRepository by lazy {
        ExerciseDbRepository(
            context = this,
            exerciseDao = database.exerciseDao(),
            migrationDao = database.exerciseMigrationDao(),
            syncMetadataDao = database.exerciseSyncMetadataDao(),
            offlineDownloadDao = database.offlineDownloadDao(),
            settingsRepository = settingsRepository
        )
    }
    
    val achievementRepository by lazy {
        AchievementRepository(
            achievementService = AchievementService(
                achievementDao = database.achievementDao()
            )
        )
    }
    
    val aiService by lazy {
        AIService(settingsRepository = settingsRepository)
    }
    
    val customProgramRepository by lazy {
        CustomProgramRepository(
            customProgramDao = database.customProgramDao(),
            customWorkoutDao = database.customWorkoutDao(),
            workoutExerciseDao = database.workoutExerciseDao()
        )
    }
    
    val exerciseSearchRepository by lazy {
        ExerciseSearchRepository(
            exerciseDao = database.exerciseDao(),
            usageHistoryDao = database.exerciseUsageHistoryDao()
        )
    }
    
    val workoutRoutineRepository by lazy {
        WorkoutRoutineRepository(
            folderDao = database.folderDao(),
            routineDao = database.workoutRoutineDao(),
            routineExerciseDao = database.routineExerciseDao()
        )
    }
    
    val supersetRepository by lazy {
        SupersetRepository(
            supersetGroupDao = database.supersetGroupDao(),
            workoutExerciseDao = database.workoutExerciseDao()
        )
    }
    
    val programManagementRepository by lazy {
        ProgramManagementRepository(
            programDao = database.programDao(),
            programDayDao = database.programDayDao(),
            enrollmentDao = database.userProgramEnrollmentDao(),
            completionDao = database.programDayCompletionDao(),
            routineDao = database.workoutRoutineDao(),
            programDayExerciseDao = database.programDayExerciseDao()
        )
    }
    
    val sampleProgramData by lazy {
        SampleProgramDataWithExercises(
            programDao = database.programDao(),
            programDayDao = database.programDayDao(),
            programDayExerciseDao = database.programDayExerciseDao(),
            exerciseDao = database.exerciseDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize data in proper order in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("AthleticAI", "Starting app initialization...")
                
                // Step 1: Initialize exercises first (required for program templates)
                android.util.Log.d("AthleticAI", "Loading exercises from assets...")
                val exercisesLoaded = exerciseRepository.initializeExerciseData()
                
                if (!exercisesLoaded) {
                    android.util.Log.e("AthleticAI", "Failed to load exercises, skipping program templates")
                    return@launch
                }
                
                // Wait a bit longer to ensure database writes are complete
                kotlinx.coroutines.delay(1000)
                
                // Verify exercises are actually in the database
                val exerciseCount = database.exerciseDao().getExerciseCount()
                android.util.Log.d("AthleticAI", "Exercise count after initialization: $exerciseCount")
                
                if (exerciseCount == 0) {
                    android.util.Log.e("AthleticAI", "No exercises in database after initialization, skipping program templates")
                    return@launch
                }
                
                // Step 2: Initialize program templates from JSON
                android.util.Log.d("AthleticAI", "Loading program templates from JSON...")
                programTemplateLoader.initializeProgramTemplates()
                
                // Step 3: Initialize sample programs (if needed)
                // Commenting out to avoid conflicts with program templates
                // sampleProgramData.initializeSamplePrograms()
                
                android.util.Log.d("AthleticAI", "App initialization completed successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("AthleticAI", "Error during app initialization", e)
                e.printStackTrace()
            }
        }
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}
