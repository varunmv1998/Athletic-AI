package com.athleticai.app.data.database.converters

import androidx.room.TypeConverter
import com.athleticai.app.data.database.entities.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateFormatter) }
    }
    
    // Program Goal converters
    @TypeConverter
    fun fromProgramGoal(value: ProgramGoal): String {
        return value.name
    }
    
    @TypeConverter
    fun toProgramGoal(value: String): ProgramGoal {
        return ProgramGoal.valueOf(value)
    }
    
    // Experience Level converters
    @TypeConverter
    fun fromExperienceLevel(value: ExperienceLevel): String {
        return value.name
    }
    
    @TypeConverter
    fun toExperienceLevel(value: String): ExperienceLevel {
        return ExperienceLevel.valueOf(value)
    }
    
    // Day Type converters
    @TypeConverter
    fun fromDayType(value: DayType): String {
        return value.name
    }
    
    @TypeConverter
    fun toDayType(value: String): DayType {
        return DayType.valueOf(value)
    }
    
    // Enrollment Status converters
    @TypeConverter
    fun fromEnrollmentStatus(value: EnrollmentStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toEnrollmentStatus(value: String): EnrollmentStatus {
        return EnrollmentStatus.valueOf(value)
    }
    
    // Completion Status converters
    @TypeConverter
    fun fromCompletionStatus(value: CompletionStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toCompletionStatus(value: String): CompletionStatus {
        return CompletionStatus.valueOf(value)
    }
}