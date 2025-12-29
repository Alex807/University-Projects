package com.example.stride.tracking

import android.location.Location
import android.util.Log
import com.example.stride.data.GpsCoordinate
import com.example.stride.sensors.MovementMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

data class SpeedSample(
    val speed: Double,
    val timestamp: Long
)

class TrackingSession {

    private var isTracking = false
    private var startTimestamp: Long = 0
    private var endTimestamp: Long = 0

    private val gpsCoordinates = mutableListOf<GpsCoordinate>()
    private val speedSamples = mutableListOf<SpeedSample>()

    // Speed consistency tracking
    private var lastStableSpeed: Double = 0.0
    private var lastStableSpeedTime: Long = 0
    private val STABILITY_THRESHOLD = 1000L // 1 second in milliseconds
    private val SPEED_CHANGE_THRESHOLD = 0.5f // 0.5 m/s change threshold

    private val _isTrackingFlow = MutableStateFlow(false)
    val isTrackingFlow: StateFlow<Boolean> = _isTrackingFlow

    fun startTracking() {
        if (isTracking) return

        isTracking = true
        startTimestamp = System.currentTimeMillis()
        gpsCoordinates.clear()
        speedSamples.clear()
        lastStableSpeed = 0.0
        lastStableSpeedTime = 0

        _isTrackingFlow.value = true
        Log.d("TrackingSession", "Session started at $startTimestamp")
    }

    fun addLocationAndSpeed(location: Location, speed: Double) {
        if (!isTracking) return

        val currentTime = System.currentTimeMillis()

        // Check if speed is stable for at least 1 second
        val speedDifference = Math.abs(speed - lastStableSpeed)

        if (speedDifference <= SPEED_CHANGE_THRESHOLD) {
            // Speed is similar to last stable speed
            if (lastStableSpeedTime == 0L) {
                lastStableSpeedTime = currentTime
            } else if (currentTime - lastStableSpeedTime >= STABILITY_THRESHOLD) {
                // Speed has been stable for 1+ second, record it
                val coordinate = GpsCoordinate(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = currentTime
                )
                gpsCoordinates.add(coordinate)

                val speedSample = SpeedSample(
                    speed = speed,
                    timestamp = currentTime
                )
                speedSamples.add(speedSample)

                Log.d("TrackingSession", "Recorded: Lat=${location.latitude}, Lon=${location.longitude}, Speed=$speed")
            }
        } else {
            // Speed changed significantly, reset stability timer
            lastStableSpeed = speed
            lastStableSpeedTime = currentTime
        }
    }

    fun stopTracking(): SessionResult? {
        if (!isTracking) return null

        isTracking = false
        endTimestamp = System.currentTimeMillis()
        _isTrackingFlow.value = false

        if (gpsCoordinates.isEmpty() || speedSamples.isEmpty()) {
            Log.w("TrackingSession", "No data recorded during session")
            return null
        }

        val durationSeconds = (endTimestamp - startTimestamp) / 1000
        val averageSpeed = speedSamples.map { it.speed }.average().toFloat()

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val startTime = timeFormat.format(Date(startTimestamp))
        val endTime = timeFormat.format(Date(endTimestamp))

        // Determine movement mode based on average speed
        val movementMode = classifyMovementMode(averageSpeed)

        Log.d("TrackingSession", "Session ended: Duration=${durationSeconds}s, AvgSpeed=$averageSpeed, Mode=$movementMode")

        return SessionResult(
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            durationSeconds = durationSeconds,
            startTime = startTime,
            endTime = endTime,
            averageSpeed = averageSpeed,
            movementMode = movementMode,
            gpsCoordinates = gpsCoordinates.toList()
        )
    }



    private fun classifyMovementMode(avgSpeedMps: Float): MovementMode {
        return when {
            avgSpeedMps < 0.5f -> MovementMode.STATIONARY
            avgSpeedMps < 2.2f -> MovementMode.WALKING
            avgSpeedMps < 3.5f -> MovementMode.JOGGING
            avgSpeedMps < 7.0f -> MovementMode.BICYCLE
            avgSpeedMps < 16.7f -> MovementMode.CAR_SLOW
            avgSpeedMps < 30.0f -> MovementMode.CAR_FAST
            else -> MovementMode.TRAIN
        }
    }

    fun isCurrentlyTracking(): Boolean = isTracking

    fun getRecordedPointsCount(): Int = gpsCoordinates.size
}

data class SessionResult(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val durationSeconds: Long,
    val startTime: String,
    val endTime: String,
    val averageSpeed: Float,
    val movementMode: MovementMode,
    val gpsCoordinates: List<GpsCoordinate>
)