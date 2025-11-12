package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val icon: String,
    val requirementType: String,
    val requirementValue: String,
    val rarity: String,
    val points: Int,
    val isRepeatable: Boolean = false,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDate? = null,
    val currentProgress: Int = 0,
    val totalRequired: Int = 0,
    val month: String? = null,
    val year: Int? = null
)

