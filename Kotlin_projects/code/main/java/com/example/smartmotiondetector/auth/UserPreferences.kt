package com.example.smartmotiondetector.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit { putString(KEY_USERNAME, username) }
        sharedPreferences.edit { putBoolean(KEY_IS_LOGGED_IN, true) }
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun saveProfileImage(base64Image: String) {
        sharedPreferences.edit { putString(KEY_PROFILE_IMAGE, base64Image) }
    }

    fun getProfileImage(): String? {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearUserData() {
        sharedPreferences.edit { clear() }
    }
}