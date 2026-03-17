package com.example.applymate.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Application(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val organization: String = "",
    val status: String = "Pending", // Pending, Accepted, Rejected
    val deadline: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
