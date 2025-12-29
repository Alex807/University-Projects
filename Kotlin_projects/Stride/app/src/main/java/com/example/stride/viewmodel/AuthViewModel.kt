package com.example.stride.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stride.auth.SavedAccount
import com.example.stride.auth.UserPreferences
import com.example.stride.data.AppDatabase
import com.example.stride.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val userPreferences = UserPreferences(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _savedAccounts = MutableStateFlow<List<SavedAccount>>(emptyList())
    val savedAccounts: StateFlow<List<SavedAccount>> = _savedAccounts

    init {
        loadSavedAccounts()
    }

    fun isLoggedIn(): Boolean {
        return userPreferences.isLoggedIn()
    }

    fun getUserProfileImage(): String? {
        return userPreferences.getProfileImage()
    }

    fun login(username: String, password: String, rememberMe: Boolean = false) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Username and password are required")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val user = userDao.getUserByUsername(username)

                if (user == null) {
                    _authState.value = AuthState.Error("User not found")
                    return@launch
                }

                if (user.password != password) {
                    _authState.value = AuthState.Error("Incorrect password")
                    return@launch
                }

                // Save login state
                userPreferences.saveUsername(username)
                user.profileImageBase64?.let { userPreferences.saveProfileImage(it) }

                // Save account if Remember Me is checked
                if (rememberMe) {
                    userPreferences.saveAccount(username, password, user.profileImageBase64)
                    loadSavedAccounts()
                }

                _authState.value = AuthState.Success
                Log.d("AuthViewModel", "Login successful for user: $username")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
                Log.e("AuthViewModel", "Login error", e)
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        age: String,
        profession: String,
        username: String,
        password: String,
        profileImageBase64: String?,
        rememberMe: Boolean = false
    ) {
        // Validation
        if (firstName.isBlank() || lastName.isBlank() || username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("First name, last name, username, and password are required")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                // Check if username already exists
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    _authState.value = AuthState.Error("Username already exists")
                    return@launch
                }

                // Create new user
                val newUser = User(
                    firstName = firstName,
                    lastName = lastName,
                    age = age.toIntOrNull(),
                    profession = profession.ifBlank { null },
                    username = username,
                    password = password,
                    profileImageBase64 = profileImageBase64
                )

                userDao.insert(newUser)

                // Auto-login after registration
                userPreferences.saveUsername(username)
                profileImageBase64?.let { userPreferences.saveProfileImage(it) }

                // Save account if Remember Me is checked
                if (rememberMe) {
                    userPreferences.saveAccount(username, password, profileImageBase64)
                    loadSavedAccounts()
                }

                _authState.value = AuthState.Success
                Log.d("AuthViewModel", "Registration successful for user: $username")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
                Log.e("AuthViewModel", "Registration error", e)
            }
        }
    }

    fun logout() {
        userPreferences.clearUserData()
        _authState.value = AuthState.Idle
        Log.d("AuthViewModel", "User logged out")
    }

    fun checkLoginStatus(): Boolean {
        return userPreferences.isLoggedIn()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Remember Me Feature Methods

    fun loadSavedAccounts() {
        viewModelScope.launch {
            _savedAccounts.value = userPreferences.getSavedAccounts()
        }
    }

    fun getDecryptedPassword(encryptedPassword: String): String? {
        return userPreferences.getDecryptedPassword(encryptedPassword)
    }

    fun removeSavedAccounts(usernamesToRemove: List<String>) {
        viewModelScope.launch {
            userPreferences.removeSavedAccounts(usernamesToRemove)
            loadSavedAccounts()
        }
    }
}