// File: app/src/main/java/com/example/stride/data/SensorSampleDao.kt
package com.example.stride.data

import androidx.room.*

@Dao
interface SensorSampleDao {

    @Insert
    suspend fun insert(sample: SensorSample): Long

    @Query("SELECT * FROM sensor_samples ORDER BY timestamp DESC")
    suspend fun getAllSamples(): List<SensorSample>

    @Query("SELECT * FROM sensor_samples WHERE id = :id")
    suspend fun getSampleById(id: Long): SensorSample?

    @Update
    suspend fun update(sample: SensorSample)

    @Delete
    suspend fun delete(sample: SensorSample)

    @Query("DELETE FROM sensor_samples WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM sensor_samples")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM sensor_samples WHERE username = :username")
    suspend fun getCountByUsername(username: String): Int

    @Query("SELECT * FROM sensor_samples WHERE username = :username ORDER BY timestamp DESC")
    suspend fun getAllByUsername(username: String): List<SensorSample>

    @Query("SELECT * FROM sensor_samples WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Int): SensorSample?
}