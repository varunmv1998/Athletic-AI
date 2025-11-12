package com.athleticai.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal
import com.athleticai.app.data.database.entities.PersonalRecord
import com.athleticai.app.data.database.entities.WorkoutSession
import com.athleticai.app.data.database.entities.WorkoutSet
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.repository.AnalyticsRepository
import com.athleticai.app.data.repository.MeasurementsRepository
import com.athleticai.app.data.repository.WorkoutRepository
import com.athleticai.app.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class ProgressUiState(
    val completedDates: Set<LocalDate> = emptySet(),
    val currentStreak: Int = 0,
    val recentPRs: List<PersonalRecord> = emptyList(),
    val latestMeasurement: BodyMeasurement? = null,
    val sevenDayVolume: Double = 0.0,
    val recentSessions: List<com.athleticai.app.data.database.entities.WorkoutSession> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val allMeasurements: List<BodyMeasurement> = emptyList(),
    val totalWorkouts: Int = 0,
    val daysSinceLastWorkout: Int = 0
)

class ProgressViewModel(
    private val workoutRepository: WorkoutRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            combine(
                workoutRepository.getAllSessions(),
                analyticsRepository.getRecentPRs(10),
                measurementsRepository.getLatestMeasurement(),
                measurementsRepository.getActiveGoals(),
                measurementsRepository.getAllMeasurements()
            ) { sessions, prs, latestMeasurement, goals, allMeasurements ->
                val completedSessions = sessions.filter { it.isCompleted }
                val completedDates = completedSessions
                    .map { it.endTime ?: it.startTime }
                    .map { it.toLocalDate() }
                    .toSet()
                val streak = analyticsRepository.computeStreak(completedDates.toList())
                val sevenDayVol = runCatching { analyticsRepository.get7DayVolume() }.getOrDefault(0.0)
                val recentSessions = completedSessions
                    .sortedByDescending { it.endTime ?: it.startTime }
                    .take(10)
                
                // Calculate days since last workout
                val daysSinceLastWorkout = if (completedSessions.isNotEmpty()) {
                    val lastWorkoutDate = completedSessions
                        .maxByOrNull { it.endTime ?: it.startTime }
                        ?.let { (it.endTime ?: it.startTime).toLocalDate() }
                    if (lastWorkoutDate != null) {
                        java.time.temporal.ChronoUnit.DAYS.between(lastWorkoutDate, LocalDate.now()).toInt()
                    } else {
                        0
                    }
                } else {
                    999 // Very high number if no workouts
                }
                
                ProgressUiState(
                    completedDates = completedDates,
                    currentStreak = streak,
                    recentPRs = prs,
                    latestMeasurement = latestMeasurement,
                    sevenDayVolume = sevenDayVol,
                    recentSessions = recentSessions,
                    activeGoals = goals,
                    allMeasurements = allMeasurements,
                    totalWorkouts = completedSessions.size,
                    daysSinceLastWorkout = daysSinceLastWorkout
                )
            }.collect { _state.value = it }
        }
    }

    fun addMeasurement(weightKg: Double?, heightCm: Double?, bodyFatPct: Double?, waistCm: Double?) {
        viewModelScope.launch {
            measurementsRepository.addMeasurement(weightKg, heightCm, bodyFatPct, waistCm)
        }
    }

    fun addGoal(metricType: String, targetValue: Double, targetDate: LocalDateTime) {
        viewModelScope.launch {
            measurementsRepository.upsertGoal(metricType, targetValue, targetDate)
        }
    }

    suspend fun getVolumeByExercise(days: Long): Map<String, Double> {
        return analyticsRepository.getVolumeByExercise(days)
    }

    fun getBMI(measurement: BodyMeasurement): Double? {
        return if (measurement.weightKg != null && measurement.heightCm != null) {
            val heightM = measurement.heightCm / 100.0
            measurement.weightKg / (heightM * heightM)
        } else null
    }

    fun getGoalProgress(metricType: String): Pair<Double?, Double?> {
        val goal = state.value.activeGoals.find { it.metricType == metricType }
        val latest = state.value.latestMeasurement
        
        return when (metricType) {
            "weightKg" -> Pair(latest?.weightKg, goal?.targetValue)
            "bodyFatPct" -> Pair(latest?.bodyFatPct, goal?.targetValue)
            "waistCm" -> Pair(latest?.waistCm, goal?.targetValue)
            else -> Pair(null, null)
        }
    }
    
    /**
     * Refresh progress data for pull-to-refresh functionality
     */
    fun refreshProgressData() {
        Log.d("ProgressViewModel", "Refreshing progress data...")
        viewModelScope.launch {
            try {
                // The observe() method already handles data refresh through Flow observation
                // Force re-observation by recreating the combine flow
                observe()
                Log.d("ProgressViewModel", "Progress data refreshed successfully")
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error refreshing progress data", e)
            }
        }
    }
    
    /**
     * Get workout details for the workout details screen
     */
    suspend fun getWorkoutDetails(sessionId: String): Triple<WorkoutSession?, List<WorkoutSet>, Map<String, Exercise>> {
        return try {
            val session = workoutRepository.getSessionById(sessionId)
            val sets = workoutRepository.getSetsForSessionSync(sessionId)
            val exerciseIds = sets.map { it.exerciseId }.distinct()
            val exercises = exerciseIds.mapNotNull { exerciseId ->
                exerciseRepository.getExerciseById(exerciseId)?.let { exercise ->
                    exerciseId to exercise
                }
            }.toMap()
            
            Triple(session, sets, exercises)
        } catch (e: Exception) {
            Log.e("ProgressViewModel", "Error loading workout details", e)
            Triple(null, emptyList(), emptyMap())
        }
    }
}
