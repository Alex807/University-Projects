// File: app/src/main/java/com/example/stride/data/SensorRepository.kt
package com.example.stride.data

class SensorRepository(private val sensorSampleDao: SensorSampleDao) {

    suspend fun insertSample(sample: SensorSample): Long {
        return sensorSampleDao.insert(sample)
    }

    suspend fun getAllSamples(): List<SensorSample> {
        return sensorSampleDao.getAllSamples()
    }

    suspend fun getSampleById(id: Long): SensorSample? {
        return sensorSampleDao.getSampleById(id)
    }

    suspend fun updateSample(sample: SensorSample) {
        sensorSampleDao.update(sample)
    }

    suspend fun deleteSample(sample: SensorSample) {
        sensorSampleDao.delete(sample)
    }

    suspend fun deleteSampleById(id: Long) {
        sensorSampleDao.deleteById(id)
    }

    suspend fun getSampleCount(): Int {
        return sensorSampleDao.getCount()
    }
}