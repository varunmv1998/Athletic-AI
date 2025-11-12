package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "body_measurements")
data class BodyMeasurement(
    @PrimaryKey val id: String,
    val date: LocalDateTime,
    val weightKg: Double?,
    val heightCm: Double?,
    val bodyFatPct: Double?,
    val waistCm: Double?
)

