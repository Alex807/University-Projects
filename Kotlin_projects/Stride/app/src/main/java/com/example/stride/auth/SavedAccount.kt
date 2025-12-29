package com.example.stride.auth

data class SavedAccount(
    val username: String,
    val encryptedPassword: String,
    val profileImageBase64: String?,
    val timestamp: Long = System.currentTimeMillis()
)