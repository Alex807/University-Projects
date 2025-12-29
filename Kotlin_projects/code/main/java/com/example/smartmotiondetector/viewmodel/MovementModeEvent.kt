// File: app/src/main/java/com/example/smartmotiondetector/viewmodel/MovementModeEvent.kt
package com.example.smartmotiondetector.viewmodel

import com.example.smartmotiondetector.sensors.MovementMode

data class MovementModeEvent(
    val mode: MovementMode,
    val timestamp: Long = System.currentTimeMillis()
)