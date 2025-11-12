package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "user_achievements")
data class UserAchievementEntity(
    @PrimaryKey val id: String,
    val achievementId: String,
    val unlockedAt: LocalDate,
    val progress: Int,
    val month: String? = null,
    val year: Int? = null
)

