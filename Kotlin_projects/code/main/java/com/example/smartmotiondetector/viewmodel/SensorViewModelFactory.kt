package com.example.smartmotiondetector.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SensorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SensorViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SensorViewModel(application) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}