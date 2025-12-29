// File: app/src/main/java/com/example/smartmotiondetector/sound/MovementSoundManager.kt
package com.example.smartmotiondetector.sound

import android.content.Context
import android.media.SoundPool
import com.example.smartmotiondetector.R
import com.example.smartmotiondetector.sensors.MovementMode

class MovementSoundManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()

    private val soundMap = mutableMapOf<MovementMode, Int>()

    init {
        soundMap[MovementMode.WALKING] = soundPool.load(context, R.raw.walk_sound, 1)
        soundMap[MovementMode.JOGGING] = soundPool.load(context, R.raw.jog_sound, 1)
        soundMap[MovementMode.BICYCLE] = soundPool.load(context, R.raw.bicycle_sound, 1)
        soundMap[MovementMode.CAR_SLOW] = soundPool.load(context, R.raw.car_slow_sound, 1)
        soundMap[MovementMode.CAR_FAST] = soundPool.load(context, R.raw.car_fast_sound, 1)
        soundMap[MovementMode.TRAIN] = soundPool.load(context, R.raw.train_sound, 1)
    }

    fun playSound(mode: MovementMode) {
        soundMap[mode]?.let { soundId ->
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}