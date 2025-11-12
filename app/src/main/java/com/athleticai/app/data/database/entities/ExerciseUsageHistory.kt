package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_usage_history",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["usageCount", "lastUsedDate"])
    ]
)
data class ExerciseUsageHistory(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val usageCount: Int = 1,
    val lastUsedDate: Long, // Timestamp
    val averageRpe: Float? = null,
    val averageSets: Float? = null
)