package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_workouts",
    foreignKeys = [
        ForeignKey(
            entity = CustomProgram::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["programId"])
    ]
)
data class CustomWorkout(
    @PrimaryKey val id: String,
    val programId: String,
    val name: String,
    val description: String = "",
    val dayNumber: Int,
    val estimatedDurationMinutes: Int = 0,
    val exerciseCount: Int = 0,
    val createdDate: Long = System.currentTimeMillis()
)