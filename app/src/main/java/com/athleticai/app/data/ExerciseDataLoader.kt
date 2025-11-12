package com.athleticai.app.data

import android.content.Context
import android.util.Log
import com.athleticai.app.data.database.entities.Exercise
import com.athleticai.app.data.models.ExerciseJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ExerciseDataLoader(private val context: Context) {
    
    private val gson = Gson()
    private val TAG = "ExerciseDataLoader"
    
    suspend fun loadExercisesFromAssets(): List<Exercise> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to load exercises from assets...")
            val json = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "Loaded exercises JSON, size: ${json.length} characters")
            
            val exerciseListType = object : TypeToken<List<ExerciseJson>>() {}.type
            val exerciseJsonList: List<ExerciseJson> = gson.fromJson(json, exerciseListType)
            Log.d(TAG, "Parsed ${exerciseJsonList.size} exercises from JSON")
            
            val exercises = exerciseJsonList.map { exerciseJson ->
                Exercise(
                    id = exerciseJson.id,
                    name = exerciseJson.name,
                    force = exerciseJson.force,
                    level = exerciseJson.level,
                    mechanic = exerciseJson.mechanic,
                    equipment = exerciseJson.equipment,
                    primaryMuscles = exerciseJson.primaryMuscles,
                    secondaryMuscles = exerciseJson.secondaryMuscles,
                    instructions = exerciseJson.instructions,
                    category = exerciseJson.category,
                    images = exerciseJson.images
                )
            }
            
            Log.d(TAG, "Successfully created ${exercises.size} Exercise entities")
            
            // Log some sample exercises for debugging
            exercises.take(5).forEach { exercise ->
                Log.d(TAG, "Sample exercise: ${exercise.id} - ${exercise.name}")
            }
            
            exercises
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load exercises file: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse exercises data: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
}