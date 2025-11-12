package com.athleticai.app.data.managers

import com.athleticai.app.data.database.dao.ProgramDayCompletionDao
import com.athleticai.app.data.database.dao.WorkoutSessionDao
import com.athleticai.app.data.database.entities.*
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.*

class DayStateManager(
    private val completionDao: ProgramDayCompletionDao,
    private val workoutSessionDao: WorkoutSessionDao
) {
    
    suspend fun getDayState(
        day: ProgramDay,
        enrollment: UserProgramEnrollment?,
        completions: List<ProgramDayCompletion>
    ): DayState {
        if (enrollment == null || enrollment.status != EnrollmentStatus.ACTIVE) {
            return DayState.LOCKED
        }
        
        val today = getTodayStart()
        val completion = completions.find { it.programDayId == day.id }
        
        return when {
            // Already completed today
            completion?.completionDate?.let { isToday(it) } == true -> {
                if (completion.status == CompletionStatus.COMPLETED) {
                    DayState.COMPLETED_TODAY
                } else {
                    DayState.SKIPPED
                }
            }
            
            // Current day available for today
            day.dayNumber == enrollment.currentDay -> {
                when (day.dayType) {
                    DayType.REST -> DayState.REST_DAY_ACTIVE
                    DayType.ACTIVE_RECOVERY -> DayState.ACTIVE_RECOVERY_AVAILABLE
                    DayType.WORKOUT -> {
                        // Check if already worked out today (prevent double workout)
                        if (hasWorkoutToday(enrollment.id, day.id)) {
                            DayState.COMPLETED_TODAY
                        } else {
                            DayState.AVAILABLE_TODAY
                        }
                    }
                    else -> DayState.AVAILABLE_TODAY
                }
            }
            
            // Future day
            day.dayNumber > enrollment.currentDay -> DayState.UPCOMING
            
            // Past day (completed or skipped)
            completion?.status == CompletionStatus.COMPLETED -> DayState.COMPLETED_PAST
            completion?.status == CompletionStatus.SKIPPED -> DayState.SKIPPED
            
            // Past day but no completion record (shouldn't happen)
            else -> DayState.LOCKED
        }
    }
    
    suspend fun canStartDay(
        day: ProgramDay,
        enrollment: UserProgramEnrollment?,
        completions: List<ProgramDayCompletion>
    ): Boolean {
        val state = getDayState(day, enrollment, completions)
        return state == DayState.AVAILABLE_TODAY || 
               state == DayState.ACTIVE_RECOVERY_AVAILABLE
    }
    
    suspend fun canSkipDay(
        day: ProgramDay,
        enrollment: UserProgramEnrollment?,
        completions: List<ProgramDayCompletion>
    ): Boolean {
        val state = getDayState(day, enrollment, completions)
        return state == DayState.AVAILABLE_TODAY || 
               state == DayState.ACTIVE_RECOVERY_AVAILABLE ||
               state == DayState.REST_DAY_ACTIVE
    }
    
    suspend fun getNextAvailableDay(
        enrollment: UserProgramEnrollment,
        allDays: List<ProgramDay>,
        completions: List<ProgramDayCompletion>
    ): ProgramDay? {
        return allDays
            .filter { it.dayNumber >= enrollment.currentDay }
            .find { day ->
                val state = getDayState(day, enrollment, completions)
                state == DayState.AVAILABLE_TODAY || 
                state == DayState.ACTIVE_RECOVERY_AVAILABLE ||
                state == DayState.REST_DAY_ACTIVE
            }
    }
    
    suspend fun advanceToNextDay(
        enrollment: UserProgramEnrollment,
        allDays: List<ProgramDay>
    ): Int {
        val maxDay = allDays.maxOfOrNull { it.dayNumber } ?: enrollment.currentDay
        return if (enrollment.currentDay < maxDay) {
            enrollment.currentDay + 1
        } else {
            enrollment.currentDay // Program complete
        }
    }
    
    private suspend fun hasWorkoutToday(enrollmentId: String, dayId: String): Boolean {
        val todayStart = LocalDate.now().atStartOfDay()
        val tomorrowStart = todayStart.plusDays(1)
        
        val sessions = workoutSessionDao.getSessionsInDateRange(todayStart, tomorrowStart)
        return sessions.any { 
            it.programEnrollmentId == enrollmentId && 
            it.programDayId == dayId &&
            it.isCompleted
        }
    }
    
    private fun getTodayStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    private fun isToday(timestamp: Long): Boolean {
        val today = getTodayStart()
        val tomorrow = today + 24 * 60 * 60 * 1000
        return timestamp >= today && timestamp < tomorrow
    }
    
    fun getDayStateDisplayText(state: DayState): String {
        return when (state) {
            DayState.AVAILABLE_TODAY -> "Start Today"
            DayState.COMPLETED_TODAY -> "✓ Completed Today"
            DayState.COMPLETED_PAST -> "✓ Completed"
            DayState.SKIPPED -> "Skipped"
            DayState.REST_DAY_ACTIVE -> "Rest Day"
            DayState.ACTIVE_RECOVERY_AVAILABLE -> "Active Recovery"
            DayState.UPCOMING -> "Upcoming"
            DayState.LOCKED -> "Locked"
        }
    }
    
    fun getDayStateColor(state: DayState): String {
        return when (state) {
            DayState.AVAILABLE_TODAY -> "primary"
            DayState.COMPLETED_TODAY, DayState.COMPLETED_PAST -> "success"
            DayState.SKIPPED -> "warning"
            DayState.REST_DAY_ACTIVE, DayState.ACTIVE_RECOVERY_AVAILABLE -> "secondary"
            DayState.UPCOMING -> "surface"
            DayState.LOCKED -> "disabled"
        }
    }
}

enum class DayState {
    AVAILABLE_TODAY,        // Current day, can start workout
    COMPLETED_TODAY,        // Completed today already
    COMPLETED_PAST,         // Completed in the past
    SKIPPED,               // Was skipped
    REST_DAY_ACTIVE,       // Current day is rest day
    ACTIVE_RECOVERY_AVAILABLE, // Current day is active recovery
    UPCOMING,              // Future day
    LOCKED                 // Not accessible yet
}