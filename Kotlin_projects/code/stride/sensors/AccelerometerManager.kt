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

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _accelerometerSpeedFlow = MutableStateFlow(0.0)
    val accelerometerSpeedFlow: StateFlow<Double> = _accelerometerSpeedFlow

    private var lastUpdateTime = 0L
    private var currentVelocity = 0.0

    // Movement mode tracking
    private var currentMovementMode = "UNKNOWN"

    // Adaptive parameters based on movement mode
    private var alpha = 0.2 // Low-pass filter
    private var deadZone = 0.15 // Noise threshold
    private var decayFactor = 0.92 // Velocity decay
    private var maxVelocity = 3.0 // Speed limit

    private var filteredX = 0.0
    private var filteredY = 0.0
    private var filteredZ = 0.0

    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)

    fun startListening() {
        try {
            val sensor = accelerometer
            if (sensor == null) {
                Log.w("AccelerometerManager", "Accelerometer not available")
                return
            }

            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )

            if (registered) {
                Log.d("AccelerometerManager", "Accelerometer started")
            } else {
                Log.e("AccelerometerManager", "Failed to register accelerometer")
            }
        } catch (e: Exception) {
            Log.e("AccelerometerManager", "Error starting accelerometer", e)
        }
    }

    fun stopListening() {
        try {
            sensorManager.unregisterListener(this)
            currentVelocity = 0.0
            _accelerometerSpeedFlow.value = 0.0
            Log.d("AccelerometerManager", "Accelerometer stopped")
        } catch (e: Exception) {
            Log.e("AccelerometerManager", "Error stopping accelerometer", e)
        }
    }

     // Update accelerometer parameters based on movement mode
    fun setMovementMode(mode: String) {
        if (currentMovementMode == mode) return

        currentMovementMode = mode

        when (mode) {
            "STATIONARY" -> {
                alpha = 0.05
                deadZone = 0.25
                decayFactor = 0.85
                maxVelocity = 0.5
            }
            "WALKING" -> {
                alpha = 0.3
                deadZone = 0.08
                decayFactor = 0.95
                maxVelocity = 2.5
            }
            "JOGGING" -> {
                alpha = 0.25
                deadZone = 0.12
                decayFactor = 0.93
                maxVelocity = 4.5
            }
            "BICYCLE" -> {
                alpha = 0.15
                deadZone = 0.20
                decayFactor = 0.90
                maxVelocity = 10.0
            }
            "CAR_SLOW", "CAR_FAST" -> {
                alpha = 0.1
                deadZone = 0.30
                decayFactor = 0.88
                maxVelocity = 20.0
            }
            "TRAIN" -> {
                alpha = 0.05
                deadZone = 0.35
                decayFactor = 0.85
                maxVelocity = 30.0
            }
            else -> {
                alpha = 0.2
                deadZone = 0.15
                decayFactor = 0.92
                maxVelocity = 3.0
            }
        }

        Log.d("AccelerometerManager",
            "Mode: $mode, alpha=$alpha, deadZone=$deadZone, decay=$decayFactor, maxVel=$maxVelocity")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val currentTime = System.currentTimeMillis()
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return
        }

        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        // Apply adaptive low-pass filter
        filteredX = alpha * event.values[0] + (1 - alpha) * filteredX
        filteredY = alpha * event.values[1] + (1 - alpha) * filteredY
        filteredZ = alpha * event.values[2] + (1 - alpha) * filteredZ

        // Remove gravity
        val alphaGravity = 0.8f
        gravity[0] = alphaGravity * gravity[0] + (1 - alphaGravity) * filteredX.toFloat()
        gravity[1] = alphaGravity * gravity[1] + (1 - alphaGravity) * filteredY.toFloat()
        gravity[2] = alphaGravity * gravity[2] + (1 - alphaGravity) * filteredZ.toFloat()

        linearAcceleration[0] = filteredX.toFloat() - gravity[0]
        linearAcceleration[1] = filteredY.toFloat() - gravity[1]
        linearAcceleration[2] = filteredZ.toFloat() - gravity[2]

        val magnitude = sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        ).toDouble()

        // Apply adaptive dead zone
        val effectiveAcceleration = if (magnitude < deadZone) 0.0 else magnitude

        // Integrate acceleration
        currentVelocity += effectiveAcceleration * deltaTime

        // Apply adaptive decay
        currentVelocity *= decayFactor

        // Apply adaptive limits
        currentVelocity = currentVelocity.coerceIn(0.0, maxVelocity)

        _accelerometerSpeedFlow.value = currentVelocity
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}