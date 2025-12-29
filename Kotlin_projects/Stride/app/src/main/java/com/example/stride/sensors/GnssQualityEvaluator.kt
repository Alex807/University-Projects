// File: app/src/main/java/com/example/stride/sensors/GnssQualityEvaluator.kt
package com.example.stride.sensors

import android.location.GnssStatus
import kotlin.math.min

data class GnssQualityResult(
    val score: Int,
    val satelliteCount: Int,
    val avgSnr: Float,
    val usedInFixCount: Int
)

class GnssQualityEvaluator {

    fun evaluateQuality(gnssStatus: GnssStatus?): GnssQualityResult {
        if (gnssStatus == null) {
            return GnssQualityResult(0, 0, 0f, 0)
        }

        val satelliteCount = gnssStatus.satelliteCount
        if (satelliteCount == 0) {
            return GnssQualityResult(0, 0, 0f, 0)
        }

        var totalSnr = 0f
        var usedInFixCount = 0
        var snrCount = 0
        val constellationTypes = mutableSetOf<Int>()

        for (i in 0 until satelliteCount) {
            val snr = gnssStatus.getCn0DbHz(i)
            if (snr > 0) {
                totalSnr += snr
                snrCount++
            }

            if (gnssStatus.usedInFix(i)) {
                usedInFixCount++
            }

            constellationTypes.add(gnssStatus.getConstellationType(i))
        }

        val avgSnr = if (snrCount > 0) totalSnr / snrCount else 0f

        var score = 0

        score += min(usedInFixCount * 10, 40)

        score += when {
            avgSnr >= 40 -> 30
            avgSnr >= 30 -> 25
            avgSnr >= 20 -> 15
            avgSnr >= 10 -> 5
            else -> 0
        }

        score += min(constellationTypes.size * 7, 20)

        score += when {
            satelliteCount >= 12 -> 10
            satelliteCount >= 8 -> 7
            satelliteCount >= 4 -> 4
            else -> 0
        }

        return GnssQualityResult(
            score = min(score, 100),
            satelliteCount = satelliteCount,
            avgSnr = avgSnr,
            usedInFixCount = usedInFixCount
        )
    }
}