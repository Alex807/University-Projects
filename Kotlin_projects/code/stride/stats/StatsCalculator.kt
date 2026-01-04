package com.example.stride.stats

import com.example.stride.data.SensorSample
import com.example.stride.sensors.MovementMode
import java.util.*

enum class TimeFilter {
    THIS_HOUR,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

data class SessionStats(
    val totalSessions: Int,
    val totalDuration: Long, // seconds
    val totalDistance: Float, // kilometers
    val averageSpeed: Float, // km/h
    val maxSpeed: Float, // km/h
    val favoriteLocations: List<LocationFrequency>,
    val movementModeDistribution: Map<MovementMode, Int>,
    val speedDistribution: Map<String, Int>,
    val sessionsPerDay: Map<String, Int>,
    val longestSession: SensorSample?,
    val fastestSession: SensorSample?,
    val mostActiveDay: String?,
    val totalGpsPoints: Int
)

data class LocationFrequency(
    val city: String,
    val count: Int,
    val percentage: Float
)

class StatsCalculator {

    fun calculateStats(sessions: List<SensorSample>, filter: TimeFilter): SessionStats {
        val filteredSessions = filterSessionsByTime(sessions, filter)

        if (filteredSessions.isEmpty()) {
            return SessionStats(
                totalSessions = 0,
                totalDuration = 0,
                totalDistance = 0f,
                averageSpeed = 0f,
                maxSpeed = 0f,
                favoriteLocations = emptyList(),
                movementModeDistribution = emptyMap(),
                speedDistribution = emptyMap(),
                sessionsPerDay = emptyMap(),
                longestSession = null,
                fastestSession = null,
                mostActiveDay = null,
                totalGpsPoints = 0
            )
        }

        val totalDuration = filteredSessions.sumOf { it.durationSeconds }
        val totalDistance = filteredSessions.sumOf {
            (it.averageSpeed * it.durationSeconds / 1000.0).toDouble()
        }.toFloat()

        val averageSpeed = if (filteredSessions.isNotEmpty()) {
            filteredSessions.map { it.averageSpeed }.average().toFloat() * 3.6f
        } else 0f

        val maxSpeed = filteredSessions.maxOfOrNull { it.averageSpeed * 3.6f } ?: 0f

        val favoriteLocations = calculateFavoriteLocations(filteredSessions)
        val movementModeDistribution = calculateMovementModeDistribution(filteredSessions)
        val speedDistribution = calculateSpeedDistribution(filteredSessions)
        val sessionsPerDay = calculateSessionsPerDay(filteredSessions)

        val longestSession = filteredSessions.maxByOrNull { it.durationSeconds }
        val fastestSession = filteredSessions.maxByOrNull { it.averageSpeed }

        val mostActiveDay = sessionsPerDay.maxByOrNull { it.value }?.key

        val totalGpsPoints = filteredSessions.sumOf { it.gpsCoordinates.size }

        return SessionStats(
            totalSessions = filteredSessions.size,
            totalDuration = totalDuration,
            totalDistance = totalDistance,
            averageSpeed = averageSpeed,
            maxSpeed = maxSpeed,
            favoriteLocations = favoriteLocations,
            movementModeDistribution = movementModeDistribution,
            speedDistribution = speedDistribution,
            sessionsPerDay = sessionsPerDay,
            longestSession = longestSession,
            fastestSession = fastestSession,
            mostActiveDay = mostActiveDay,
            totalGpsPoints = totalGpsPoints
        )
    }

    private fun filterSessionsByTime(sessions: List<SensorSample>, filter: TimeFilter): List<SensorSample> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter) {
            TimeFilter.THIS_HOUR -> {
                val oneHourAgo = now - (60 * 60 * 1000)
                sessions.filter { it.startTimestamp >= oneHourAgo }
            }
            TimeFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                sessions.filter { it.startTimestamp >= startOfDay }
            }
            TimeFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                sessions.filter { it.startTimestamp >= startOfWeek }
            }
            TimeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                sessions.filter { it.startTimestamp >= startOfMonth }
            }
            TimeFilter.ALL_TIME -> sessions
        }
    }

    private fun calculateFavoriteLocations(sessions: List<SensorSample>): List<LocationFrequency> {
        val locationCounts = sessions.groupBy { it.city }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val total = sessions.size.toFloat()

        return locationCounts.map { (city, count) ->
            LocationFrequency(
                city = city,
                count = count,
                percentage = (count / total) * 100
            )
        }
    }

    private fun calculateMovementModeDistribution(sessions: List<SensorSample>): Map<MovementMode, Int> {
        return sessions.groupBy { MovementMode.valueOf(it.movementMode) }
            .mapValues { it.value.size }
    }

    private fun calculateSpeedDistribution(sessions: List<SensorSample>): Map<String, Int> {
        val distribution = mutableMapOf(
            "0-5 km/h" to 0,
            "5-20 km/h" to 0,
            "20-50 km/h" to 0,
            "50-100 km/h" to 0,
            "100+ km/h" to 0
        )

        sessions.forEach { session ->
            val speedKmh = session.averageSpeed * 3.6f
            when {
                speedKmh < 5 -> distribution["0-5 km/h"] = distribution["0-5 km/h"]!! + 1
                speedKmh < 20 -> distribution["5-20 km/h"] = distribution["5-20 km/h"]!! + 1
                speedKmh < 50 -> distribution["20-50 km/h"] = distribution["20-50 km/h"]!! + 1
                speedKmh < 100 -> distribution["50-100 km/h"] = distribution["50-100 km/h"]!! + 1
                else -> distribution["100+ km/h"] = distribution["100+ km/h"]!! + 1
            }
        }

        return distribution
    }

    private fun calculateSessionsPerDay(sessions: List<SensorSample>): Map<String, Int> {
        return sessions.groupBy { session ->
            session.date.split(" ").firstOrNull() ?: "Unknown"
        }.mapValues { it.value.size }
    }
}