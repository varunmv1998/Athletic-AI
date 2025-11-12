package com.athleticai.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.athleticai.app.data.database.converters.Converters
import com.athleticai.app.data.database.dao.ExerciseDao
import com.athleticai.app.data.database.dao.WorkoutSessionDao
import com.athleticai.app.data.database.dao.WorkoutSetDao
import com.athleticai.app.data.database.dao.ProgramEnrollmentDao
import com.athleticai.app.data.database.dao.ProgramExerciseDao
import com.athleticai.app.data.database.dao.UserProgressionDao
import com.athleticai.app.data.database.dao.ExerciseSubstitutionDao
import com.athleticai.app.data.database.dao.AchievementDao
import com.athleticai.app.data.database.dao.FolderDao
import com.athleticai.app.data.database.dao.WorkoutRoutineDao
import com.athleticai.app.data.database.dao.RoutineExerciseDao
import com.athleticai.app.data.database.dao.ActiveWorkoutSessionDao
import com.athleticai.app.data.database.dao.ExerciseMigrationDao
import com.athleticai.app.data.database.dao.ExerciseSyncMetadataDao
import com.athleticai.app.data.database.dao.OfflineDownloadDao
import com.athleticai.app.data.database.converters.AchievementConverters
import com.athleticai.app.data.database.converters.ExerciseSetListConverter
import com.athleticai.app.data.database.converters.SessionExerciseListConverter
import com.athleticai.app.data.database.converters.PerformedSetListConverter
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.database.entities.Folder
import com.athleticai.app.data.database.entities.WorkoutRoutine
import com.athleticai.app.data.database.entities.RoutineExercise
import com.athleticai.app.data.database.entities.ActiveWorkoutSession
import com.athleticai.app.data.database.entities.ExerciseMigration
import com.athleticai.app.data.database.entities.ExerciseSyncMetadata
import com.athleticai.app.data.database.entities.OfflineDownload
import com.athleticai.app.data.database.entities.WorkoutSession
import com.athleticai.app.data.database.entities.WorkoutSet
import com.athleticai.app.data.database.entities.ProgramEnrollment
import com.athleticai.app.data.database.entities.ProgramExercise
import com.athleticai.app.data.database.entities.UserProgression
import com.athleticai.app.data.database.entities.ExerciseSubstitution
import com.athleticai.app.data.database.entities.AchievementEntity
import com.athleticai.app.data.database.entities.UserAchievementEntity
import com.athleticai.app.data.database.entities.MonthlyStatsEntity
import com.athleticai.app.data.database.entities.CumulativeStatsEntity
import com.athleticai.app.data.database.entities.Program
import com.athleticai.app.data.database.entities.ProgramDay
import com.athleticai.app.data.database.entities.UserProgramEnrollment
import com.athleticai.app.data.database.entities.ProgramDayCompletion
import com.athleticai.app.data.database.entities.ProgramDayExercise
import com.athleticai.app.data.database.entities.ProgramTemplate
import com.athleticai.app.data.database.entities.RestDayActivity
import com.athleticai.app.data.database.entities.ProgramQuote
import com.athleticai.app.data.database.migrations.MIGRATION_1_2
import com.athleticai.app.data.database.migrations.MIGRATION_2_3
import com.athleticai.app.data.database.migrations.MIGRATION_3_4
import com.athleticai.app.data.database.migrations.MIGRATION_4_5
import com.athleticai.app.data.database.migrations.MIGRATION_5_6
import com.athleticai.app.data.database.migrations.MIGRATION_6_7
import com.athleticai.app.data.database.migrations.MIGRATION_7_8
import com.athleticai.app.data.database.migrations.MIGRATION_8_9
import com.athleticai.app.data.database.migrations.MIGRATION_9_10
import com.athleticai.app.data.database.migrations.MIGRATION_10_11
import com.athleticai.app.data.database.migrations.MIGRATION_11_12
import com.athleticai.app.data.database.migrations.MIGRATION_12_13
import com.athleticai.app.data.database.migrations.MIGRATION_13_14
import com.athleticai.app.data.database.entities.DaySubstitution
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal
import com.athleticai.app.data.database.entities.PersonalRecord
import com.athleticai.app.data.database.dao.BodyMeasurementDao
import com.athleticai.app.data.database.dao.GoalDao
import com.athleticai.app.data.database.dao.PersonalRecordDao
import com.athleticai.app.data.database.dao.CustomProgramDao
import com.athleticai.app.data.database.dao.ProgramDao
import com.athleticai.app.data.database.dao.ProgramDayDao
import com.athleticai.app.data.database.dao.UserProgramEnrollmentDao
import com.athleticai.app.data.database.dao.ProgramDayCompletionDao
import com.athleticai.app.data.database.dao.ProgramDayExerciseDao
import com.athleticai.app.data.database.dao.ProgramTemplateDao
import com.athleticai.app.data.database.dao.RestDayActivityDao
import com.athleticai.app.data.database.dao.ProgramQuoteDao
import com.athleticai.app.data.database.dao.CustomWorkoutDao
import com.athleticai.app.data.database.dao.WorkoutExerciseDao
import com.athleticai.app.data.database.dao.ExerciseUsageHistoryDao
import com.athleticai.app.data.database.dao.SupersetGroupDao
import com.athleticai.app.data.database.entities.CustomProgram
import com.athleticai.app.data.database.entities.CustomWorkout
import com.athleticai.app.data.database.entities.WorkoutExercise
import com.athleticai.app.data.database.entities.ExerciseUsageHistory
import com.athleticai.app.data.database.entities.SupersetGroup
import android.util.Log

@Database(
    entities = [
        Exercise::class, 
        WorkoutSession::class, 
        WorkoutSet::class,
        ProgramEnrollment::class,
        ProgramExercise::class,
        UserProgression::class,
        ExerciseSubstitution::class,
        DaySubstitution::class,
        BodyMeasurement::class,
        Goal::class,
        PersonalRecord::class,
        AchievementEntity::class,
        UserAchievementEntity::class,
        MonthlyStatsEntity::class,
        CumulativeStatsEntity::class,
        CustomProgram::class,
        CustomWorkout::class,
        WorkoutExercise::class,
        ExerciseUsageHistory::class,
        Folder::class,
        WorkoutRoutine::class,
        RoutineExercise::class,
        ActiveWorkoutSession::class,
        SupersetGroup::class,
        ExerciseMigration::class,
        ExerciseSyncMetadata::class,
        OfflineDownload::class,
        Program::class,
        ProgramDay::class,
        UserProgramEnrollment::class,
        ProgramDayCompletion::class,
        ProgramDayExercise::class,
        ProgramTemplate::class,
        RestDayActivity::class,
        ProgramQuote::class
    ],
    version = 14,
    exportSchema = false
)
@TypeConverters(
    Converters::class, 
    AchievementConverters::class,
    ExerciseSetListConverter::class,
    SessionExerciseListConverter::class,
    PerformedSetListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    
    // Phase 1 DAOs
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutSetDao(): WorkoutSetDao
    
    // Phase 2 DAOs
    abstract fun programEnrollmentDao(): ProgramEnrollmentDao
    abstract fun programExerciseDao(): ProgramExerciseDao
    abstract fun userProgressionDao(): UserProgressionDao
    abstract fun exerciseSubstitutionDao(): ExerciseSubstitutionDao
    abstract fun daySubstitutionDao(): com.athleticai.app.data.database.dao.DaySubstitutionDao
    
    // Phase 3 DAOs
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun goalDao(): GoalDao
    abstract fun personalRecordDao(): PersonalRecordDao
    
    // Achievement DAOs
    abstract fun achievementDao(): AchievementDao
    
    // Custom Workout Builder DAOs
    abstract fun customProgramDao(): CustomProgramDao
    abstract fun customWorkoutDao(): CustomWorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun exerciseUsageHistoryDao(): ExerciseUsageHistoryDao
    
    // New Hevy/Strong-style DAOs
    abstract fun folderDao(): FolderDao
    abstract fun workoutRoutineDao(): WorkoutRoutineDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun activeWorkoutSessionDao(): ActiveWorkoutSessionDao
    
    // Superset DAOs
    abstract fun supersetGroupDao(): SupersetGroupDao
    
    // ExerciseDB Migration DAOs
    abstract fun exerciseMigrationDao(): ExerciseMigrationDao
    abstract fun exerciseSyncMetadataDao(): ExerciseSyncMetadataDao
    abstract fun offlineDownloadDao(): OfflineDownloadDao
    
    // Program Management DAOs
    abstract fun programDao(): ProgramDao
    abstract fun programDayDao(): ProgramDayDao
    abstract fun userProgramEnrollmentDao(): UserProgramEnrollmentDao
    abstract fun programDayCompletionDao(): ProgramDayCompletionDao
    abstract fun programDayExerciseDao(): ProgramDayExerciseDao
    abstract fun programTemplateDao(): ProgramTemplateDao
    abstract fun restDayActivityDao(): RestDayActivityDao
    abstract fun programQuoteDao(): ProgramQuoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        @Volatile
        private var TEST_INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d("AppDatabase", "Creating new database instance...")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "athletic_ai_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                .fallbackToDestructiveMigration() // Only as fallback for development
                .build()
                Log.d("AppDatabase", "Database instance created successfully")
                INSTANCE = instance
                instance
            }
        }
        
        fun getTestDatabase(context: Context): AppDatabase {
            return TEST_INSTANCE ?: synchronized(this) {
                Log.d("AppDatabase", "Creating new TEST database instance...")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "athletic_ai_test_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                .fallbackToDestructiveMigration() // Only as fallback for development
                .build()
                Log.d("AppDatabase", "Test database instance created successfully")
                TEST_INSTANCE = instance
                instance
            }
        }
        
        // Method to clear test database instance when switching modes
        fun clearTestDatabase() {
            synchronized(this) {
                TEST_INSTANCE?.close()
                TEST_INSTANCE = null
                Log.d("AppDatabase", "Test database instance cleared")
            }
        }
    }
}
