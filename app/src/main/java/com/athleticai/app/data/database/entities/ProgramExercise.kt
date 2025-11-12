package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "program_exercises",
    foreignKeys = [
        ForeignKey(
            entity = ProgramTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProgramExercise(
    @PrimaryKey val id: String,
    val templateId: String, // Foreign key to ProgramTemplate
    val exerciseId: String, // Foreign key to Exercise
    val orderIndex: Int, // Order within workout (0, 1, 2...)
    val sets: Int,
    val repRangeMin: Int,
    val repRangeMax: Int,
    val rpeTarget: Double,
    val restSeconds: Int,
    val progressionType: String // "linear", "double", "volume", "bodyweight"
)