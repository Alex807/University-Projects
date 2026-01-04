package com.example.stride.sound

import android.content.Context
import android.media.SoundPool
import android.util.Log
import com.example.stride.R
import com.example.stride.sensors.MovementMode

class MovementSoundManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()

    private val soundMap = mutableMapOf<MovementMode, Int>()
    private val loadedSounds = mutableSetOf<Int>()  // Track loaded sounds

    init {
        // Set load complete listener
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
                Log.d("MovementSoundManager", "Sound loaded: $sampleId")
            } else {
                Log.e("MovementSoundManager", "Failed to load sound: $sampleId")
            }
        }

        // Load sounds
        try {
            soundMap[MovementMode.WALKING] = soundPool.load(context, R.raw.walk_sound, 1)
            soundMap[MovementMode.JOGGING] = soundPool.load(context, R.raw.jog_sound, 1)
            soundMap[MovementMode.BICYCLE] = soundPool.load(context, R.raw.bicycle_sound, 1)
            soundMap[MovementMode.CAR_SLOW] = soundPool.load(context, R.raw.car_slow_sound, 1)
            soundMap[MovementMode.CAR_FAST] = soundPool.load(context, R.raw.car_fast_sound, 1)
            soundMap[MovementMode.TRAIN] = soundPool.load(context, R.raw.train_sound, 1)
        } catch (e: Exception) {
            Log.e("MovementSoundManager", "Error loading sounds", e)
        }
    }

    fun playSound(mode: MovementMode) {
        try {
            soundMap[mode]?.let { soundId ->
                // Only play if loaded
                if (loadedSounds.contains(soundId)) {
                    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                    Log.d("MovementSoundManager", "Playing sound for $mode")
                } else {
                    Log.w("MovementSoundManager", "Sound not loaded yet for $mode")
                }
            }
        } catch (e: Exception) {
            Log.e("MovementSoundManager", "Error playing sound", e)
        }
    }

    fun release() {
        try {
            soundPool.release()
            loadedSounds.clear()
            soundMap.clear()
        } catch (e: Exception) {
            Log.e("MovementSoundManager", "Error releasing sound pool", e)
        }
    }
}