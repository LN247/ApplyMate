package com.example.opportunitytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val organization: String,
    val status: String, // Pending, Accepted, Rejected
    val deadline: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)
