package com.example.aistudy.data

import java.util.Date

data class UserData(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val dateOfBirth: Date? = null,
    val createdAt: Date? = null,
    val lastLogin: Date? = null,
    val totalStudyHours: Int = 0,
    val flashcardsCreated: Int = 0,
    val streak: Int = 0
) 