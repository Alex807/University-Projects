package com.example.smartmotiondetector.sensors

import kotlin.math.abs
import kotlin.math.round

class GpsSpeedFilter {

    // Kalman filter parameters - more responsive
    private var estimatedSpeed = 0.0
    private var estimationError = 1.0

    // Reduced noise for faster response
    private val processNoise = 0.05
    private val measurementNoise = 0.3

    // Smaller buffer for faster updates
    private val speedBuffer = mutableListOf<Double>()
    private val bufferSize = 3  // Reduced from 5

    // More permissive speed change for testing
    private var lastSpeed = 0.0
    private val maxSpeedChange = 10.0  // Increased from 5.0

    fun filterSpeed(
        rawSpeed: Float,
        accuracy: Float,
        hasSpeed: Boolean
    ): Double {

        if (!hasSpeed || accuracy > 30f) {
            return decaySpeed()
        }

        var speed = rawSpeed.toDouble().coerceAtLeast(0.0)

        // Lower threshold for testing
        if (speed < 0.3) {
            speed = 0.0
        }

        // Kalman filter - more responsive
        val predictedSpeed = estimatedSpeed
        val predictedError = estimationError + processNoise

        val kalmanGain = predictedError / (predictedError + measurementNoise)

        estimatedSpeed = predictedSpeed + kalmanGain * (speed - predictedSpeed)
        estimationError = (1 - kalmanGain) * predictedError

        // More permissive rate limiter
        val speedDiff = estimatedSpeed - lastSpeed
        if (abs(speedDiff) > maxSpeedChange) {
            estimatedSpeed = lastSpeed + (if (speedDiff > 0) maxSpeedChange else -maxSpeedChange)
        }

        speedBuffer.add(estimatedSpeed)
        if (speedBuffer.size > bufferSize) {
            speedBuffer.removeAt(0)
        }

        val smoothedSpeed = speedBuffer.average()
        lastSpeed = smoothedSpeed

        return smoothedSpeed.coerceAtLeast(0.0)
    }

    private fun decaySpeed(): Double {
        estimatedSpeed *= 0.85  // Faster decay
        if (estimatedSpeed < 0.3) {
            estimatedSpeed = 0.0
        }
        return estimatedSpeed
    }

    fun getSpeedKmh(speedMs: Double): Double {
        return speedMs * 3.6
    }

    fun reset() {
        estimatedSpeed = 0.0
        estimationError = 1.0
        speedBuffer.clear()
        lastSpeed = 0.0
    }
}