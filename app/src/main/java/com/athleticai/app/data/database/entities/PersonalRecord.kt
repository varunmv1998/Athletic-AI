package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val type: String, // "1RM" | "Volume" | "BestSet"
    val value: Double,
    val date: LocalDateTime
)

