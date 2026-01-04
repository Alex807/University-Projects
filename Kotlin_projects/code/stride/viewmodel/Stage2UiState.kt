// File: app/src/main/java/com/example/stride/viewmodel/Stage2UiState.kt
package com.example.stride.viewmodel

import com.example.stride.sensors.MovementMode

data class Stage2UiState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rawGnssSpeed: Double = 0.0,
    val accelerometerSpeed: Double = 0.0,
    val fusedSpeed: Double = 0.0,
    val gnssQualityScore: Int = 0,
    val satelliteCount: Int = 0,
    val avgSnr: Float = 0f,
    val movementMode: MovementMode = MovementMode.WALKING,
    val sampleCount: Int = 0
)