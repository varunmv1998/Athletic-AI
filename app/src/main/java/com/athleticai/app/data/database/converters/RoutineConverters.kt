package com.athleticai.app.data.database.converters

import androidx.room.TypeConverter
import com.athleticai.app.data.database.entities.ExerciseSet
import com.athleticai.app.data.database.entities.SessionExercise
import com.athleticai.app.data.database.entities.PerformedSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExerciseSetListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromExerciseSetList(sets: List<ExerciseSet>): String {
        return gson.toJson(sets)
    }
    
    @TypeConverter
    fun toExerciseSetList(setsString: String): List<ExerciseSet> {
        if (setsString.isEmpty()) return emptyList()
        val type = object : TypeToken<List<ExerciseSet>>() {}.type
        return gson.fromJson(setsString, type)
    }
}

class SessionExerciseListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromSessionExerciseList(exercises: List<SessionExercise>): String {
        return gson.toJson(exercises)
    }
    
    @TypeConverter
    fun toSessionExerciseList(exercisesString: String): List<SessionExercise> {
        if (exercisesString.isEmpty()) return emptyList()
        val type = object : TypeToken<List<SessionExercise>>() {}.type
        return gson.fromJson(exercisesString, type)
    }
}

class PerformedSetListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromPerformedSetList(sets: List<PerformedSet>): String {
        return gson.toJson(sets)
    }
    
    @TypeConverter
    fun toPerformedSetList(setsString: String): List<PerformedSet> {
        if (setsString.isEmpty()) return emptyList()
        val type = object : TypeToken<List<PerformedSet>>() {}.type
        return gson.fromJson(setsString, type)
    }
}