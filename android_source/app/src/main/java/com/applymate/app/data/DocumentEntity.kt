package com.applymate.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fileUrl: String,
    val timestamp: Long,
    val location: String,
    val category: String,
    val localPath: String? = null
)
