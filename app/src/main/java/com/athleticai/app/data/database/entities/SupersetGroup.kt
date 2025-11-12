package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "superset_groups",
    foreignKeys = [
        ForeignKey(
            entity = CustomWorkout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["groupIndex"])
    ]
)
data class SupersetGroup(
    @PrimaryKey val id: String,
    val workoutId: String,
    val groupIndex: Int,
    val groupType: SupersetType = SupersetType.SUPERSET,
    val restBetweenExercises: Int = 10,  // Seconds of rest between exercises in group
    val restAfterGroup: Int = 90,        // Seconds of rest after completing all rounds
    val rounds: Int = 1,                 // Number of rounds through the group
    val createdAt: Long = System.currentTimeMillis()
)

enum class SupersetType {
    SUPERSET,    // 2 exercises
    TRISET,      // 3 exercises  
    GIANTSET,    // 4+ exercises
    CIRCUIT      // Multiple rounds with minimal rest
}