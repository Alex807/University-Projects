package com.example.smartmotiondetector.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Base64

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val age: Int? = null,  // Optional
    val profession: String? = null,  // Optional
    val username: String,
    val password: String,
    val profileImageBase64: String? = null  // Base64 encoded compressed image
)