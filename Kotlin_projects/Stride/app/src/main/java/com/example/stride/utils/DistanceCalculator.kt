package com.example.stride.utils

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

object DistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val TAG = "DistanceCalculator"

    /**
     * Calculate distance between two locations using Haversine formula
     * More accurate than Android's Location.distanceTo() for longer distances
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculate distance between two LatLng points
     */
    fun calculateDistance(start: LatLng, end: LatLng): Double {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
    }

    /**
     * Calculate distance between two Location objects
     */
    fun calculateDistance(start: Location, end: Location): Double {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
    }

    /**
     * Calculate bearing (direction) between two points in degrees (0-360)
     */
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }

    /**
     * Calculate the difference between two bearings (0-180 degrees)
     */
    fun bearingDifference(bearing1: Double, bearing2: Double): Double {
        var diff = abs(bearing1 - bearing2)
        if (diff > 180) {
            diff = 360 - diff
        }
        return diff
    }

    /**
     * Advanced filtering: Check if a location update should be counted for distance
     * Uses multiple criteria to filter out GPS noise and ensure accuracy
     */
    fun shouldCountDistance(
        previousLocation: Location,
        currentLocation: Location,
        currentSpeed: Double,
        previousBearing: Double? = null,
        movementMode: String = "UNKNOWN"
    ): FilterResult {

        val distance = calculateDistance(previousLocation, currentLocation)
        val accuracy = currentLocation.accuracy
        val timeDelta = (currentLocation.time - previousLocation.time) / 1000.0 // seconds

        // Calculate current bearing
        val currentBearing = calculateBearing(
            previousLocation.latitude,
            previousLocation.longitude,
            currentLocation.latitude,
            currentLocation.longitude
        )

        // Adaptive thresholds based on movement mode
        val thresholds = getAdaptiveThresholds(movementMode, currentSpeed)

        // Filter 1: Minimum distance threshold (prevents GPS drift)
        if (distance < thresholds.minDistance) {
            Log.d(TAG, " Filtered: Distance too small (${distance}m < ${thresholds.minDistance}m)")
            return FilterResult(false, "Distance too small", distance)
        }

        // Filter 2: Maximum distance threshold (prevents GPS jumps)
        if (distance > thresholds.maxDistance) {
            Log.d(TAG, " Filtered: GPS jump detected (${distance}m > ${thresholds.maxDistance}m)")
            return FilterResult(false, "GPS jump", distance)
        }

        // Filter 3: Accuracy threshold (only use high-quality GPS)
        if (accuracy > thresholds.maxAccuracy) {
            Log.d(TAG, " Filtered: Poor GPS accuracy (${accuracy}m > ${thresholds.maxAccuracy}m)")
            return FilterResult(false, "Poor accuracy", distance)
        }

        // Filter 4: Speed threshold (ignore stationary drift)
        if (currentSpeed < thresholds.minSpeed) {
            Log.d(TAG, " Filtered: Speed too low (${currentSpeed}m/s < ${thresholds.minSpeed}m/s)")
            return FilterResult(false, "Speed too low", distance)
        }

        // Filter 5: Speed consistency check (distance vs speed vs time)
        if (timeDelta > 0) {
            val impliedSpeed = distance / timeDelta
            val speedDifference = abs(impliedSpeed - currentSpeed)

            // Allow some variance, but not too much
            if (speedDifference > max(currentSpeed * 0.5, 2.0)) {
                Log.d(TAG, " Filtered: Speed inconsistency (implied: ${impliedSpeed}m/s, actual: ${currentSpeed}m/s)")
                return FilterResult(false, "Speed inconsistency", distance)
            }
        }

        // Filter 6: Bearing consistency (ignore perpendicular movements)
        if (previousBearing != null) {
            val bearingChange = bearingDifference(previousBearing, currentBearing)

            // If moving fast and bearing changes drastically, it's likely GPS noise
            if (currentSpeed > 2.0 && bearingChange > thresholds.maxBearingChange) {
                Log.d(TAG, " Filtered: Erratic bearing change (${bearingChange}° > ${thresholds.maxBearingChange}°)")
                return FilterResult(false, "Erratic bearing", distance)
            }
        }

        Log.d(TAG, "Accepted: Distance=${distance}m, Speed=${currentSpeed}m/s, Accuracy=${accuracy}m, Bearing=${currentBearing}°")
        return FilterResult(true, "Accepted", distance, currentBearing)
    }

    /**
     * Get adaptive thresholds based on movement mode and speed
     */
    private fun getAdaptiveThresholds(movementMode: String, speed: Double): FilterThresholds {
        return when (movementMode) {
            "STATIONARY" -> FilterThresholds(
                minDistance = 5.0,
                maxDistance = 20.0,
                minSpeed = 0.1,
                maxAccuracy = 9.0f,
                maxBearingChange = 180.0,
            )
            "WALKING" -> FilterThresholds(
                minDistance = 1.5,
                maxDistance = 20.0,
                minSpeed = 0.3,
                maxAccuracy = 12.0f,
                maxBearingChange = 80.0,
            )
            "JOGGING" -> FilterThresholds(
                minDistance = 3.0,
                maxDistance = 50.0,
                minSpeed = 0.8,
                maxAccuracy = 20.0f,
                maxBearingChange = 90.0,
            )
            "BICYCLE" -> FilterThresholds(
                minDistance = 5.0,
                maxDistance = 150.0,
                minSpeed = 1.5,
                maxAccuracy = 20.0f,
                maxBearingChange = 60.0,
            )
            "CAR_SLOW", "CAR_FAST" -> FilterThresholds(
                minDistance = 8.0,
                maxDistance = 300.0,
                minSpeed = 2.0,
                maxAccuracy = 25.0f,
                maxBearingChange = 45.0,
            )
            "TRAIN" -> FilterThresholds(
                minDistance = 10.0,
                maxDistance = 500.0,
                minSpeed = 5.0,
                maxAccuracy = 30.0f,
                maxBearingChange = 30.0,
            )
            else -> FilterThresholds(
                minDistance = 5.0,
                maxDistance = 100.0,
                minSpeed = 0.3,
                maxAccuracy = 20.0f,
                maxBearingChange = 90.0,
            )
        }
    }

    data class FilterThresholds(
        val minDistance: Double,
        val maxDistance: Double,
        val minSpeed: Double,
        val maxAccuracy: Float,
        val maxBearingChange: Double,
    )

    data class FilterResult(
        val shouldCount: Boolean,
        val reason: String,
        val distance: Double,
        val bearing: Double? = null
    )
}