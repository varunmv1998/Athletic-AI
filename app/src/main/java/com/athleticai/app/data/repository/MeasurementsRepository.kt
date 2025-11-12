package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.BodyMeasurementDao
import com.athleticai.app.data.database.dao.GoalDao
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.Goal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

class MeasurementsRepository(
    private val measurementDao: BodyMeasurementDao,
    private val goalDao: GoalDao
) {
    fun getAllMeasurements(): Flow<List<BodyMeasurement>> = measurementDao.getAll()
    fun getLatestMeasurement(): Flow<BodyMeasurement?> = measurementDao.getLatest()
    fun getActiveGoals(): Flow<List<Goal>> = goalDao.getActiveGoals()

    suspend fun addMeasurement(
        weightKg: Double?, heightCm: Double?, bodyFatPct: Double?, waistCm: Double?
    ) {
        val bm = BodyMeasurement(
            id = UUID.randomUUID().toString(),
            date = LocalDateTime.now(),
            weightKg = weightKg,
            heightCm = heightCm,
            bodyFatPct = bodyFatPct,
            waistCm = waistCm
        )
        measurementDao.upsert(bm)
    }

    suspend fun upsertGoal(metricType: String, targetValue: Double, targetDate: LocalDateTime) {
        val goal = Goal(
            id = UUID.randomUUID().toString(),
            metricType = metricType,
            targetValue = targetValue,
            targetDate = targetDate,
            createdAt = LocalDateTime.now(),
            isActive = true
        )
        goalDao.upsert(goal)
    }

    suspend fun calculateBMI(measurement: BodyMeasurement): Double? {
        return if (measurement.weightKg != null && measurement.heightCm != null) {
            val heightM = measurement.heightCm / 100.0
            measurement.weightKg / (heightM * heightM)
        } else null
    }
    
    // Test data methods
    suspend fun addMeasurementDirect(measurement: BodyMeasurement) {
        measurementDao.upsert(measurement)
    }
    
    suspend fun addGoalDirect(goal: Goal) {
        goalDao.upsert(goal)
    }
    
    suspend fun clearAllMeasurements() {
        measurementDao.deleteAll()
    }
    
    suspend fun clearAllGoals() {
        goalDao.deleteAll()
    }
}

