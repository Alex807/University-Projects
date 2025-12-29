package com.example.smartmotiondetector

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class MovementVibrationManager(private val context: Context) {

    private var vibrator: Vibrator? = null

    init {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e("VibrationManager", "Init error", e)
            vibrator = null
        }
    }

    fun vibrateForMovementMode(mode: String) {
        // Quick return if no vibrator
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            // Always cancel first
            vib.cancel()

            // Simple single vibration based on mode
            val duration = when (mode) {
                "STATIONARY" -> 100L
                "WALKING" -> 150L
                "JOGGING" -> 200L
                "BICYCLE" -> 250L
                "CAR_SLOW" -> 300L
                "CAR_FAST" -> 350L
                "TRAIN" -> 400L
                else -> return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(duration)
            }
        } catch (e: Exception) {
            Log.e("VibrationManager", "Vibrate error", e)
        }
    }

    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("VibrationManager", "Cancel error", e)
        }
    }
}