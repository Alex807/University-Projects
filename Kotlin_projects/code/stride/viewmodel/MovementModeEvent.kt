// File: app/src/main/java/com/example/stride/viewmodel/MovementModeEvent.kt
package com.example.stride.viewmodel

import com.example.stride.sensors.MovementMode

data class MovementModeEvent(
    val mode: MovementMode,
    val timestamp: Long = System.currentTimeMillis()
)