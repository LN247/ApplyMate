package com.applymate.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class PreferenceProfile(
    @PrimaryKey val id: Int = 1, // Single profile for the user
    val fieldOfStudy: String = "",
    val preferredLocations: String = "", // Comma separated
    val keywords: String = "", // Comma separated
    val internshipType: String = "Remote", // Remote, On-site, Hybrid
    val degreeLevel: String = "Undergraduate"
)

@Entity(tableName = "discovered_opportunities")
data class DiscoveredOpportunity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val organization: String,
    val location: String,
    val link: String,
    val matchScore: Int,
    val description: String,
    val type: String, // Internship, Scholarship
    val discoveredAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false
)
