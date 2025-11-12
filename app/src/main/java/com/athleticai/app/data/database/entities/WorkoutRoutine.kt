package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(
    tableName = "workout_routines",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class WorkoutRoutine(
    @PrimaryKey val id: String,
    val name: String,
    val folderId: String? = null, // nullable for organization
    val lastPerformed: Long? = null, // timestamp, nullable
    val createdDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

// Data class for UI representation with exercises
data class WorkoutRoutineWithExercises(
    @Embedded val routine: WorkoutRoutine,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val exercises: List<RoutineExercise>
)