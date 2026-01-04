package com.example.stride.sensors

import android.util.Log
import com.example.stride.utils.SpeedFilter

class HybridSpeedFusion {

    private var lastFusedSpeed = 0.0
    private val speedFilter = SpeedFilter()

    private var lastDriftCorrectionTime = 0L
    private val DRIFT_CORRECTION_INTERVAL = 10000L // 10 seconds

    fun fuseSpeed(
        gpsSpeed: Double,
        accelerometerSpeed: Double,
        gnssQualityScore: Double,
        stepDetectorSpeed: Double = 0.0,
        isWalking: Boolean = false,
        movementMode: String = "UNKNOWN"
    ): Double {

        val currentTime = System.currentTimeMillis()

        // STATIONARY
        if (movementMode == "STATIONARY") {
            val filtered = speedFilter.filter(0.0)
            lastFusedSpeed = filtered
            return filtered
        }

        // WALKING: Steps + Accelerometer PRIMARY
        if (movementMode == "WALKING" && stepDetectorSpeed > 0.0) {
            val stepConfidence = 0.9
            val accelConfidence = 0.7

            val primarySpeed = (stepDetectorSpeed * stepConfidence +
                    accelerometerSpeed * accelConfidence) /
                    (stepConfidence + accelConfidence)

            val finalSpeed = if (shouldApplyDriftCorrection(currentTime) && gpsSpeed > 0.3) {
                val driftFactor = gpsSpeed / primarySpeed.coerceAtLeast(0.1)
                if (driftFactor in 0.8..1.2) {
                    primarySpeed * 0.9 + gpsSpeed * 0.1
                } else {
                    primarySpeed
                }
            } else {
                primarySpeed
            }

            val filtered = speedFilter.filter(finalSpeed)
            lastFusedSpeed = filtered

            Log.d("HybridSpeedFusion",
                "🚶 WALKING: Steps=${String.format("%.2f", stepDetectorSpeed)}, " +
                        "Accel=${String.format("%.2f", accelerometerSpeed)}, " +
                        "Final=${String.format("%.2f", filtered)}")

            return filtered
        }

        // JOGGING: Balanced
        if (movementMode == "JOGGING") {
            val gpsWeight = gnssQualityScore.coerceIn(0.5, 0.7)
            val accelWeight = 0.5

            val rawFused = (gpsSpeed * gpsWeight + accelerometerSpeed * accelWeight) /
                    (gpsWeight + accelWeight)

            val filtered = speedFilter.filter(rawFused)
            lastFusedSpeed = filtered

            Log.d("HybridSpeedFusion",
                "🏃 JOGGING: GPS=${String.format("%.2f", gpsSpeed)}, " +
                        "Accel=${String.format("%.2f", accelerometerSpeed)}, " +
                        "Final=${String.format("%.2f", filtered)}")

            return filtered
        }

        // BICYCLE/CAR/TRAIN: GPS DOMINANT
        if (movementMode in listOf("BICYCLE", "CAR_SLOW", "CAR_FAST", "TRAIN")) {
            val gpsWeight = when (movementMode) {
                "BICYCLE" -> gnssQualityScore.coerceIn(0.75, 0.90)
                "CAR_SLOW" -> gnssQualityScore.coerceIn(0.85, 0.95)
                "CAR_FAST" -> gnssQualityScore.coerceIn(0.90, 0.98)
                "TRAIN" -> gnssQualityScore.coerceIn(0.95, 0.99)
                else -> 0.8
            }
            val accelWeight = 1.0 - gpsWeight

            val rawFused = (gpsSpeed * gpsWeight) + (accelerometerSpeed * accelWeight)
            val filtered = speedFilter.filter(rawFused)

            val smoothingFactor = 0.8
            val smoothed = (filtered * smoothingFactor) + (lastFusedSpeed * (1 - smoothingFactor))
            lastFusedSpeed = smoothed

            Log.d("HybridSpeedFusion",
                "🚴 $movementMode: GPS=${String.format("%.2f", gpsSpeed)}, " +
                        "Final=${String.format("%.2f", smoothed)}")

            return smoothed
        }

        // DEFAULT
        val gpsWeight = gnssQualityScore.coerceIn(0.5, 0.7)
        val accelWeight = 1.0 - gpsWeight
        val rawFused = (gpsSpeed * gpsWeight) + (accelerometerSpeed * accelWeight)
        val filtered = speedFilter.filter(rawFused)
        lastFusedSpeed = filtered

        return filtered
    }

    private fun shouldApplyDriftCorrection(currentTime: Long): Boolean {
        if (currentTime - lastDriftCorrectionTime > DRIFT_CORRECTION_INTERVAL) {
            lastDriftCorrectionTime = currentTime
            return true
        }
        return false
    }

    fun reset() {
        lastFusedSpeed = 0.0
        speedFilter.reset()
        lastDriftCorrectionTime = 0L
        Log.d("HybridSpeedFusion", "Filter reset")
    }
}