package com.example.stride.utils

import android.location.Location
import android.util.Log
import kotlin.math.sqrt

/**
 * Kalman filter for GPS location to reduce noise
 * Similar to what Google Maps uses
 */
class LocationKalmanFilter {

    private var isInitialized = false

    // State: [latitude, longitude, velocity_lat, velocity_lon]
    private var latitude = 0.0
    private var longitude = 0.0
    private var velocityLat = 0.0
    private var velocityLon = 0.0

    // Covariance matrix (simplified - using variance for each dimension)
    private var varianceLat = 1.0
    private var varianceLon = 1.0
    private var varianceVelLat = 1.0
    private var varianceVelLon = 1.0

    // Process noise (how much we trust the model)
    private val processNoisePosition = 0.0001
    private val processNoiseVelocity = 0.01

    // Measurement noise (how much we trust GPS)
    private var measurementNoise = 10.0

    private var lastTimestamp = 0L

    fun filter(location: Location): Location {
        val currentTime = location.time

        if (!isInitialized) {
            // Initialize with first location
            latitude = location.latitude
            longitude = location.longitude
            velocityLat = 0.0
            velocityLon = 0.0

            varianceLat = location.accuracy.toDouble()
            varianceLon = location.accuracy.toDouble()

            lastTimestamp = currentTime
            isInitialized = true

            return location
        }

        // Time delta in seconds
        val dt = (currentTime - lastTimestamp) / 1000.0
        lastTimestamp = currentTime

        if (dt <= 0 || dt > 10) {
            // Skip if time delta is invalid
            return createFilteredLocation(location)
        }

        // Prediction step
        val predictedLat = latitude + velocityLat * dt
        val predictedLon = longitude + velocityLon * dt

        val predictedVarLat = varianceLat + processNoisePosition + varianceVelLat * dt * dt
        val predictedVarLon = varianceLon + processNoisePosition + varianceVelLon * dt * dt

        // Update measurement noise based on GPS accuracy
        measurementNoise = (location.accuracy * location.accuracy).toDouble()

        // Kalman gain
        val kalmanGainLat = predictedVarLat / (predictedVarLat + measurementNoise)
        val kalmanGainLon = predictedVarLon / (predictedVarLon + measurementNoise)

        // Update step
        latitude = predictedLat + kalmanGainLat * (location.latitude - predictedLat)
        longitude = predictedLon + kalmanGainLon * (location.longitude - predictedLon)

        // Update velocity
        if (dt > 0) {
            velocityLat = (latitude - predictedLat) / dt
            velocityLon = (longitude - predictedLon) / dt
        }

        // Update covariance
        varianceLat = (1 - kalmanGainLat) * predictedVarLat
        varianceLon = (1 - kalmanGainLon) * predictedVarLon

        varianceVelLat += processNoiseVelocity
        varianceVelLon += processNoiseVelocity

        return createFilteredLocation(location)
    }

    private fun createFilteredLocation(original: Location): Location {
        return Location(original).apply {
            this.latitude = this@LocationKalmanFilter.latitude
            this.longitude = this@LocationKalmanFilter.longitude

            // Improved accuracy estimate
            val estimatedAccuracy = sqrt(varianceLat + varianceLon).toFloat()
            this.accuracy = minOf(estimatedAccuracy, original.accuracy)
        }
    }

    fun reset() {
        isInitialized = false
        latitude = 0.0
        longitude = 0.0
        velocityLat = 0.0
        velocityLon = 0.0
        varianceLat = 1.0
        varianceLon = 1.0
        varianceVelLat = 1.0
        varianceVelLon = 1.0
        lastTimestamp = 0L
    }
}