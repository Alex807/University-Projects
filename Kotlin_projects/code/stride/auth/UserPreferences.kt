package com.example.stride.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts"
        private const val MAX_SAVED_ACCOUNTS = 3
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
        // Clear login data but keep saved accounts
        sharedPreferences.edit {
            remove(KEY_USERNAME)
            remove(KEY_PROFILE_IMAGE)
            remove(KEY_IS_LOGGED_IN)
        }
    }

    // Remember Me Feature Methods

    fun saveAccount(username: String, password: String, profileImageBase64: String?) {
        try {
            val encryptedPassword = EncryptionUtil.encrypt(password)
            val newAccount = SavedAccount(
                username = username,
                encryptedPassword = encryptedPassword,
                profileImageBase64 = profileImageBase64,
                timestamp = System.currentTimeMillis()
            )

            val savedAccounts = getSavedAccounts().toMutableList()

            // Remove existing account with same username if exists
            savedAccounts.removeAll { it.username == username }

            // Add new account at the beginning (most recent)
            savedAccounts.add(0, newAccount)

            // Keep only last MAX_SAVED_ACCOUNTS accounts (FIFO)
            if (savedAccounts.size > MAX_SAVED_ACCOUNTS) {
                savedAccounts.subList(MAX_SAVED_ACCOUNTS, savedAccounts.size).clear()
            }

            val json = gson.toJson(savedAccounts)
            sharedPreferences.edit { putString(KEY_SAVED_ACCOUNTS, json) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSavedAccounts(): List<SavedAccount> {
        return try {
            val json = sharedPreferences.getString(KEY_SAVED_ACCOUNTS, null)
            if (json.isNullOrEmpty()) {
                return emptyList()
            }
            val type = object : TypeToken<List<SavedAccount>>() {}.type
            val accounts: List<SavedAccount>? = gson.fromJson(json, type)
            accounts ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getDecryptedPassword(encryptedPassword: String): String? {
        return try {
            EncryptionUtil.decrypt(encryptedPassword)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun removeSavedAccounts(usernamesToRemove: List<String>) {
        try {
            val savedAccounts = getSavedAccounts().toMutableList()
            savedAccounts.removeAll { it.username in usernamesToRemove }

            val json = gson.toJson(savedAccounts)
            sharedPreferences.edit { putString(KEY_SAVED_ACCOUNTS, json) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Add to UserPreferences class
    fun setSensorsStarted(started: Boolean) {
        sharedPreferences.edit { putBoolean("sensors_started", started) }
    }

    fun areSensorsStarted(): Boolean {
        return sharedPreferences.getBoolean("sensors_started", false)
    }

}