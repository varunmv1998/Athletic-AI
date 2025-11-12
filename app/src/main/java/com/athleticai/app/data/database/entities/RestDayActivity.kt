package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rest_day_activities",
    foreignKeys = [
        ForeignKey(
            entity = ProgramDay::class,
            parentColumns = ["id"],
            childColumns = ["programDayId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["programDayId"])
    ]
)
data class RestDayActivity(
    @PrimaryKey val id: String,
    val programDayId: String? = null, // Null for built-in activities
    val activityType: String, // "stretch", "mobility", "yoga", "walk", "meditation"
    val title: String,
    val description: String,
    val duration: Int = 15, // minutes
    val videoUrl: String? = null,
    val instructions: String? = null,
    val isBuiltIn: Boolean = true
)