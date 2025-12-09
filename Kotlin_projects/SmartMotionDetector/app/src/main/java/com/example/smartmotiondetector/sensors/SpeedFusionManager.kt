package com.example.smartmotiondetector.sensors

import kotlin.math.abs

class SpeedFusionManager {

    private val speedHistory = mutableListOf<Double>()
    private val historySize = 5

    fun fuseSpeed(
        gnssSpeed: Double,
        accelerometerSpeed: Double,
        gnssQualityScore: Int
    ): Double {
        // Prioritize GNSS speed when quality is good
        val fusedSpeed = when {
            gnssQualityScore >= 70 -> {
                // High quality GNSS - use it primarily
                gnssSpeed * 0.9 + accelerometerSpeed * 0.1
            }
            gnssQualityScore >= 40 -> {
                // Medium quality - balanced fusion
                gnssSpeed * 0.6 + accelerometerSpeed * 0.4
            }
            else -> {
                // Low quality GNSS - rely more on accelerometer
                gnssSpeed * 0.3 + accelerometerSpeed * 0.7
            }
        }

        // Add to history for additional smoothing
        speedHistory.add(fusedSpeed)
        if (speedHistory.size > historySize) {
            speedHistory.removeAt(0)
        }

        // Return smoothed average
        val smoothedSpeed = speedHistory.average()

        // Clamp to realistic values (0 to 150 km/h = 0 to 41.67 m/s)
        return smoothedSpeed.coerceIn(0.0, 42.0)
    }

    fun reset() {
        speedHistory.clear()
    }
}