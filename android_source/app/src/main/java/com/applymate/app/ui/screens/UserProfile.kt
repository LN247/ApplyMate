package com.applymate.app.ui.screens

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val headshotUrl: String? = null,
    val createdAt: String
)
