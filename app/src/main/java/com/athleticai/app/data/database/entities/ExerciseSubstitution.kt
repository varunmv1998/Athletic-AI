package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_substitutions",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["primaryExerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["substituteExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseSubstitution(
    @PrimaryKey val id: String,
    val primaryExerciseId: String, // Original exercise from program
    val substituteExerciseId: String, // Alternative exercise
    val muscleGroup: String, // Primary muscle group for categorization
    val isEquivalent: Boolean = true // Whether substitution maintains similar difficulty
)