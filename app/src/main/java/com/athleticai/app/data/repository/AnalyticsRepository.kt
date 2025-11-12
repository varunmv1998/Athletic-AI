package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.PersonalRecordDao
import com.athleticai.app.data.database.dao.WorkoutSessionDao
import com.athleticai.app.data.database.dao.WorkoutSetDao
import com.athleticai.app.data.database.entities.PersonalRecord
import com.athleticai.app.data.database.entities.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class AnalyticsRepository(
    private val sessionDao: WorkoutSessionDao,
    private val setDao: WorkoutSetDao,
    private val prDao: PersonalRecordDao
) {
    suspend fun computeAndStorePRsForSession(sessionId: String) {
        val sets = setDao.getSetsForSessionSync(sessionId)
        if (sets.isEmpty()) return
        val grouped = sets.groupBy { it.exerciseId }
        for ((exerciseId, exSets) in grouped) {
            // Best 1RM (Epley) across sets
            val best1rm = exSets.maxOfOrNull { epley1RM(it.weight, it.reps) } ?: 0.0
            val prev1rm = prDao.getBest(exerciseId, "1RM")?.value ?: 0.0
            if (best1rm > prev1rm) {
                prDao.insert(
                    PersonalRecord(
                        id = UUID.randomUUID().toString(),
                        exerciseId = exerciseId,
                        type = "1RM",
                        value = best1rm,
                        date = LocalDateTime.now()
                    )
                )
            }
            // Best set by weight*reps
            val bestSet = exSets.maxOfOrNull { it.weight * it.reps } ?: 0.0
            val prevBestSet = prDao.getBest(exerciseId, "BestSet")?.value ?: 0.0
            if (bestSet > prevBestSet) {
                prDao.insert(
                    PersonalRecord(
                        id = UUID.randomUUID().toString(),
                        exerciseId = exerciseId,
                        type = "BestSet",
                        value = bestSet,
                        date = LocalDateTime.now()
                    )
                )
            }
            // Best volume in this session for that exercise
            val sessionVolume = exSets.sumOf { it.weight * it.reps }
            val prevVol = prDao.getBest(exerciseId, "Volume")?.value ?: 0.0
            if (sessionVolume > prevVol) {
                prDao.insert(
                    PersonalRecord(
                        id = UUID.randomUUID().toString(),
                        exerciseId = exerciseId,
                        type = "Volume",
                        value = sessionVolume,
                        date = LocalDateTime.now()
                    )
                )
            }
        }
    }

    fun getRecentPRs(limit: Int): Flow<List<PersonalRecord>> = prDao.getRecent(limit)

    suspend fun get7DayVolume(): Double {
        val now = LocalDate.now()
        val from = now.minusDays(6).atStartOfDay()
        val to = now.plusDays(1).atStartOfDay().minusSeconds(1)
        val sets = setDao.getSetsBetween(from.toString(), to.toString())
        return sets.sumOf { it.weight * it.reps }
    }

    suspend fun getVolumeByExercise(days: Long): Map<String, Double> {
        val now = LocalDate.now()
        val from = now.minusDays(days - 1).atStartOfDay()
        val to = now.plusDays(1).atStartOfDay().minusSeconds(1)
        val sets = setDao.getSetsBetween(from.toString(), to.toString())
        return sets.groupBy { it.exerciseId }
            .mapValues { (_, ss) -> ss.sumOf { it.weight * it.reps } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    fun epley1RM(weight: Double, reps: Int): Double = weight * (1 + reps / 30.0)

    fun computeStreak(completedDates: List<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0
        val today = LocalDate.now()
        var streak = 0
        var d = today
        val set = completedDates.toSet()
        while (set.contains(d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }
    
    // Test data methods
    suspend fun addPersonalRecord(pr: PersonalRecord) {
        prDao.insert(pr)
    }
    
    suspend fun clearAllPersonalRecords() {
        prDao.deleteAll()
    }
}
