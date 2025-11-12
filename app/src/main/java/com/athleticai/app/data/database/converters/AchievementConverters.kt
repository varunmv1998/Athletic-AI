package com.athleticai.app.data.database.converters

import androidx.room.TypeConverter
import com.athleticai.app.data.models.AchievementRequirement
import com.athleticai.app.data.models.AchievementCategory
import com.athleticai.app.data.models.AchievementRarity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AchievementConverters {
    
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }
    
    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toAchievementCategory(value: String): AchievementCategory {
        return AchievementCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromAchievementRarity(rarity: AchievementRarity): String {
        return rarity.name
    }
    
    @TypeConverter
    fun toAchievementRarity(value: String): AchievementRarity {
        return AchievementRarity.valueOf(value)
    }
    
    @TypeConverter
    fun fromAchievementRequirement(requirement: AchievementRequirement): String {
        return Gson().toJson(requirement)
    }
    
    @TypeConverter
    fun toAchievementRequirement(value: String): AchievementRequirement {
        val type = object : TypeToken<AchievementRequirement>() {}.type
        return Gson().fromJson(value, type)
    }
}

