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

/**
 * Detects walking steps using accelerometer patterns
 * Eliminates phone swing effect by detecting actual steps
 */
class StepDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private val _stepsPerMinute = MutableStateFlow(0)
    val stepsPerMinute: StateFlow<Int> = _stepsPerMinute

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking

    // Step detection parameters
    private var lastStepTime = 0L
    private var stepTimestamps = mutableListOf<Long>()
    private val stepWindow = 60000L // 1 minute window for cadence

    // Acceleration magnitude threshold
    private val STEP_THRESHOLD = 11.0 // m/s² (gravity + step impact)
    private val MIN_STEP_INTERVAL = 250L // Minimum 250ms between steps (max 240 steps/min)
    private val MAX_STEP_INTERVAL = 2000L // Maximum 2s between steps (min 30 steps/min)

    private var lastMagnitude: Float = 0.0F
    private var isAboveThreshold = false

    fun startListening() {
        try {
            val sensor = accelerometer
            if (sensor == null) {
                Log.w("StepDetector", "Accelerometer not available on this device")
                return
            }

            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )

            if (registered) {
                Log.d("StepDetector", "Step detection started")
            } else {
                Log.e("StepDetector", "Failed to register sensor listener")
            }
        } catch (e: Exception) {
            Log.e("StepDetector", "Error starting step detection", e)
        }
    }

    fun stopListening() {
        try {
            sensorManager.unregisterListener(this)
            Log.d("StepDetector", "Step detection stopped")
        } catch (e: Exception) {
            Log.e("StepDetector", "Error stopping step detection", e)
        }
    }

    fun resetSteps() {
        _stepCount.value = 0
        stepTimestamps.clear()
        _stepsPerMinute.value = 0
        _isWalking.value = false
        Log.d("StepDetector", "Steps reset")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate acceleration magnitude
        val magnitude = sqrt(x * x + y * y + z * z)
        val currentTime = System.currentTimeMillis()

        // Detect step: look for peak in acceleration
        if (magnitude > STEP_THRESHOLD && !isAboveThreshold) {
            // Rising edge detected
            isAboveThreshold = true

            // Check if enough time passed since last step
            if (currentTime - lastStepTime > MIN_STEP_INTERVAL) {
                // Valid step detected
                _stepCount.value += 1
                stepTimestamps.add(currentTime)
                lastStepTime = currentTime

                // Remove old timestamps outside the window
                stepTimestamps.removeAll { it < currentTime - stepWindow }

                // Calculate steps per minute
                val stepsInWindow = stepTimestamps.size
                _stepsPerMinute.value = stepsInWindow

                // Determine if walking (30-140 steps/min is normal walking range)
                _isWalking.value = stepsInWindow in 30..140

                Log.d("StepDetector", "Step detected! Total: ${_stepCount.value}, Cadence: ${_stepsPerMinute.value} steps/min")
            }
        } else if (magnitude < STEP_THRESHOLD) {
            // Falling edge
            isAboveThreshold = false
        }

        // Check if walking stopped (no steps for 2 seconds)
        if (currentTime - lastStepTime > MAX_STEP_INTERVAL) {
            _isWalking.value = false
        }

        lastMagnitude = magnitude
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    /**
     * Estimate walking speed from step cadence
     * Average step length: 0.7m (conservative estimate)
     */
    fun getEstimatedSpeed(): Double {
        val cadence = _stepsPerMinute.value
        if (cadence < 30) return 0.0 // Not walking

        // Speed = (steps/min) * (step_length) / 60
        val stepLength = 0.7 // meters (conservative)
        return (cadence * stepLength) / 60.0 // m/s
    }
}