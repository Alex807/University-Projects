package com.example.stride.utils

/**
 * Simple 1D Kalman filter for speed
 * Adapted from LocationKalmanFilter
 */
class SpeedFilter {

    private var isInitialized = false
    private var speed = 0.0
    private var variance = 1.0

    private val processNoise = 0.01
    private val measurementNoise = 0.5

    private var lastTimestamp = 0L

    fun filter(speedMeasurement: Double, timestamp: Long = System.currentTimeMillis()): Double {
        if (!isInitialized) {
            speed = speedMeasurement
            variance = 1.0
            lastTimestamp = timestamp
            isInitialized = true
            return speed
        }

        val dt = (timestamp - lastTimestamp) / 1000.0
        lastTimestamp = timestamp

        if (dt <= 0 || dt > 10) {
            return speed
        }

        // Prediction step (assume constant speed)
        val predictedSpeed = speed
        val predictedVariance = variance + processNoise

        // Kalman gain
        val kalmanGain = predictedVariance / (predictedVariance + measurementNoise)

        // Update step
        speed = predictedSpeed + kalmanGain * (speedMeasurement - predictedSpeed)
        variance = (1 - kalmanGain) * predictedVariance

        return speed
    }

    fun reset() {
        isInitialized = false
        speed = 0.0
        variance = 1.0
        lastTimestamp = 0L
    }
}