package com.example.stride.sensors

import kotlin.math.max

class HybridSpeedFusion {

    private val speedHistory = mutableListOf<Double>()
    private val historySize = 3  // Small for responsiveness

    /**
     * Fuses GPS and accelerometer speed
     * Uses accelerometer below 20 km/h for testing
     */
    fun fuseSpeed(
        gpsSpeed: Double,
        accelerometerSpeed: Double,
        gnssQualityScore: Int
    ): Double {

        val gpsSpeedKmh = gpsSpeed * 3.6
        val accelSpeedKmh = accelerometerSpeed * 3.6

        val fusedSpeedKmh = when {
            // Below 10 km/h - use accelerometer primarily for testing
            gpsSpeedKmh < 10.0 -> {
                if (gnssQualityScore >= 60) {
                    // Good GPS - blend both
                    max(gpsSpeedKmh, accelSpeedKmh * 0.8)
                } else {
                    // Poor GPS - use accelerometer
                    accelSpeedKmh
                }
            }
            // Above 20 km/h - use GPS only
            else -> {
                if (gnssQualityScore >= 40) {
                    gpsSpeedKmh
                } else {
                    // Poor GPS at high speed - blend
                    gpsSpeedKmh * 0.7 + accelSpeedKmh * 0.3
                }
            }
        }

        // Convert back to m/s
        val fusedSpeedMs = fusedSpeedKmh / 3.6

        // Light smoothing
        speedHistory.add(fusedSpeedMs)
        if (speedHistory.size > historySize) {
            speedHistory.removeAt(0)
        }

        val smoothedSpeed = speedHistory.average()

        return smoothedSpeed.coerceIn(0.0, 50.0)
    }

    fun reset() {
        speedHistory.clear()
    }
}