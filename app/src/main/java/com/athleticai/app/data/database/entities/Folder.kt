package com.athleticai.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey val id: String,
    val name: String,
    val createdDate: Long = System.currentTimeMillis(),
    val isExpanded: Boolean = true // UI state for collapsible folders
)