// File: app/src/main/java/com/example/smartmotiondetector/sensors/MovementClassifier.kt
package com.example.smartmotiondetector.sensors

enum class MovementMode {
    STATIONARY,
    WALKING,
    JOGGING,
    BICYCLE,
    CAR_SLOW,
    CAR_FAST,
    TRAIN
}

class MovementClassifier {

    fun classifyMovement(speed: Double): MovementMode {
        // Speed is in m/s
        return when {
            speed < 0.5 -> MovementMode.STATIONARY      // < 1.8 km/h
            speed < 2.2 -> MovementMode.WALKING         // < 8 km/h
            speed < 3.5 -> MovementMode.JOGGING         // < 12.6 km/h
            speed < 7.0 -> MovementMode.BICYCLE         // < 25 km/h
            speed < 16.7 -> MovementMode.CAR_SLOW       // < 60 km/h
            speed < 30.0 -> MovementMode.CAR_FAST       // < 108 km/h
            else -> MovementMode.TRAIN                  // >= 108 km/h
        }
    }

    fun getMovementModeString(mode: MovementMode): String {
        return when (mode) {
            MovementMode.STATIONARY -> "Stationary"
            MovementMode.WALKING -> "Walking"
            MovementMode.JOGGING -> "Jogging"
            MovementMode.BICYCLE -> "Cycling"
            MovementMode.CAR_SLOW -> "Driving"
            MovementMode.CAR_FAST -> "Fast Driving"
            MovementMode.TRAIN -> "High Speed"
        }
    }
}