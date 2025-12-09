package com.example.smartmotiondetector.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class GpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

class Converters {
    @TypeConverter
    fun fromGpsCoordinateList(value: List<GpsCoordinate>?): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGpsCoordinateList(value: String): List<GpsCoordinate>? {
        val gson = Gson()
        val type = object : TypeToken<List<GpsCoordinate>>() {}.type
        return gson.fromJson(value, type)
    }
}

@Entity(tableName = "sensor_samples")
@TypeConverters(Converters::class)
data class SensorSample(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val city: String,
    val streetName: String,
    val streetNumber: String,

    // Session timing
    val startTimestamp: Long,
    val endTimestamp: Long,
    val durationSeconds: Long,
    val startTime: String,  // HH:mm:ss format
    val endTime: String,    // HH:mm:ss format

    // Speed and movement
    val averageSpeed: Float,
    val movementMode: String,

    // GPS coordinates list
    val gpsCoordinates: List<GpsCoordinate>,

    // Legacy fields for compatibility
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Float = 0f,
    val timestamp: Long = 0L,
    val date: String = ""
)