package com.example.stride.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class AccelerometerManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _accelerometerSpeedFlow = MutableStateFlow(0.0)
    val accelerometerSpeedFlow: StateFlow<Double> = _accelerometerSpeedFlow

    private var lastTimestamp: Long = 0
    private var currentSpeed = 0.0

    // Low-pass filter variables for smoothing
    private var lastFilteredX = 0f
    private var lastFilteredY = 0f
    private var lastFilteredZ = 0f
    private val alpha = 0.8f // Smoothing factor (higher = smoother)

    // Speed history for averaging
    private val speedHistory = mutableListOf<Double>()
    private val historySize = 10 // Increased for better smoothing

    // Enhanced thresholds
    private val noiseThreshold = 0.4  // Ignore very small movements
    private val decayFactor = 0.90    // Speed decay when no movement

    // LIMIT: Maximum speed from accelerometer (5 km/h = 1.39 m/s)
    private val maxAccelerometerSpeed = 1.39  // 5 km/h in m/s

    // Minimum movement threshold
    private val minMovementThreshold = 0.12  // Below this = stationary

    private var isUpdating = false

    // Gravity compensation
    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Apply low-pass filter to reduce noise
            lastFilteredX = alpha * lastFilteredX + (1 - alpha) * x
            lastFilteredY = alpha * lastFilteredY + (1 - alpha) * y
            lastFilteredZ = alpha * lastFilteredZ + (1 - alpha) * z

            // Isolate gravity using low-pass filter
            gravity[0] = alpha * gravity[0] + (1 - alpha) * lastFilteredX
            gravity[1] = alpha * gravity[1] + (1 - alpha) * lastFilteredY
            gravity[2] = alpha * gravity[2] + (1 - alpha) * lastFilteredZ

            // Remove gravity to get linear acceleration
            linearAcceleration[0] = lastFilteredX - gravity[0]
            linearAcceleration[1] = lastFilteredY - gravity[1]
            linearAcceleration[2] = lastFilteredZ - gravity[2]

            // Calculate magnitude of linear acceleration
            val accelerationMagnitude = sqrt(
                linearAcceleration[0] * linearAcceleration[0] +
                        linearAcceleration[1] * linearAcceleration[1] +
                        linearAcceleration[2] * linearAcceleration[2]
            ).toDouble()

            // Apply noise threshold
            val filteredAcceleration = if (accelerationMagnitude < noiseThreshold) {
                0.0
            } else {
                accelerationMagnitude
            }

            if (lastTimestamp != 0L) {
                val deltaTime = (event.timestamp - lastTimestamp) / 1_000_000_000.0

                if (deltaTime > 0 && deltaTime < 1.0) {
                    // Calculate speed change
                    val deltaSpeed = filteredAcceleration * deltaTime

                    // Apply decay when no significant movement
                    if (filteredAcceleration < noiseThreshold) {
                        currentSpeed *= decayFactor
                    } else {
                        currentSpeed += deltaSpeed
                    }

                    // ENFORCE 5 km/h LIMIT
                    currentSpeed = currentSpeed.coerceIn(0.0, maxAccelerometerSpeed)

                    // Set to zero if below minimum threshold
                    if (currentSpeed < minMovementThreshold) {
                        currentSpeed = 0.0
                    }

                    // Add to history for smoothing
                    speedHistory.add(currentSpeed)
                    if (speedHistory.size > historySize) {
                        speedHistory.removeAt(0)
                    }

                    // Calculate smoothed speed using weighted average
                    val smoothedSpeed = if (speedHistory.isNotEmpty()) {
                        // Give more weight to recent values
                        val weights = speedHistory.indices.map { it + 1.0 }
                        val weightedSum = speedHistory.zip(weights).sumOf { it.first * it.second }
                        val totalWeight = weights.sum()
                        weightedSum / totalWeight
                    } else {
                        currentSpeed
                    }

                    // Update flow with smoothed, limited speed
                    _accelerometerSpeedFlow.value = smoothedSpeed

                    // Debug logging
                    if (smoothedSpeed > 0.1) {
                        Log.d("AccelerometerManager",
                            "Speed: %.2f m/s (%.1f km/h) | Accel: %.2f".format(
                                smoothedSpeed,
                                smoothedSpeed * 3.6,
                                accelerationMagnitude
                            )
                        )
                    }
                }
            }

            lastTimestamp = event.timestamp
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("AccelerometerManager", "Sensor accuracy changed: $accuracy")
    }

    fun startListening() {
        if (isUpdating) {
            Log.d("AccelerometerManager", "Already listening, skipping")
            return
        }

        accelerometer?.let {
            // Reset all values
            lastTimestamp = 0
            currentSpeed = 0.0
            speedHistory.clear()
            _accelerometerSpeedFlow.value = 0.0

            // Reset filters
            lastFilteredX = 0f
            lastFilteredY = 0f
            lastFilteredZ = 0f
            gravity[0] = 0f
            gravity[1] = 0f
            gravity[2] = 0f

            // Use SENSOR_DELAY_GAME for good balance of speed and battery
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            isUpdating = true
            Log.d("AccelerometerManager", "Started listening with 5 km/h limit")
        } ?: run {
            Log.e("AccelerometerManager", "Accelerometer sensor not available!")
        }
    }

    fun stopListening() {
        if (!isUpdating) return

        sensorManager.unregisterListener(this)

        // Reset all values
        lastTimestamp = 0
        currentSpeed = 0.0
        speedHistory.clear()
        _accelerometerSpeedFlow.value = 0.0

        // Reset filters
        lastFilteredX = 0f
        lastFilteredY = 0f
        lastFilteredZ = 0f
        gravity[0] = 0f
        gravity[1] = 0f
        gravity[2] = 0f

        isUpdating = false
        Log.d("AccelerometerManager", "Stopped listening")
    }

    /**
     * Get current speed in km/h
     */
    fun getCurrentSpeedKmh(): Double {
        return _accelerometerSpeedFlow.value * 3.6
    }

    /**
     * Check if currently moving (above minimum threshold)
     */
    fun isMoving(): Boolean {
        return _accelerometerSpeedFlow.value > minMovementThreshold
    }

    /**
     * Reset speed to zero (useful when GPS detects stationary)
     */
    fun resetSpeed() {
        currentSpeed = 0.0
        speedHistory.clear()
        _accelerometerSpeedFlow.value = 0.0
        Log.d("AccelerometerManager", "Speed reset to zero")
    }
}