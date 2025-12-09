package com.example.smartmotiondetector.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmotiondetector.auth.UserPreferences
import com.example.smartmotiondetector.data.AppDatabase
import com.example.smartmotiondetector.data.SensorSample
import com.example.smartmotiondetector.location.GeocodingService
import com.example.smartmotiondetector.sensors.*
import com.example.smartmotiondetector.sound.MovementSoundManager
import com.example.smartmotiondetector.MovementVibrationManager
import com.example.smartmotiondetector.tracking.TrackingSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = com.example.smartmotiondetector.sensors.LocationManager(application)
    private val accelerometerManager = AccelerometerManager(application)
    private val hybridSpeedFusion = HybridSpeedFusion()
    private val movementClassifier = MovementClassifier()
    private val soundManager = MovementSoundManager(application)
    private val vibrationManager = MovementVibrationManager(application)
    private val geocodingService = GeocodingService(application)
    private val userPreferences = UserPreferences(application)

    // NEW: Tracking session
    private val trackingSession = TrackingSession()

    private val database = AppDatabase.getDatabase(application)
    private val sampleDao = database.sensorSampleDao()

    private val _uiState = MutableStateFlow(Stage2UiState())
    val uiState: StateFlow<Stage2UiState> = _uiState.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    private val _countStatus = MutableStateFlow<String?>(null)
    val countStatus: StateFlow<String?> = _countStatus

    private val _sessions = MutableStateFlow<List<SensorSample>>(emptyList())
    val sessions: StateFlow<List<SensorSample>> = _sessions

    // NEW: Tracking state
    val isTracking: StateFlow<Boolean> = trackingSession.isTrackingFlow

    private var previousMode: MovementMode? = null

    init {
        observeSensors()
        loadSampleCount()
    }

    private fun observeSensors() {
        viewModelScope.launch {
            combine(
                locationManager.locationFlow,
                locationManager.filteredSpeedFlow,
                accelerometerManager.accelerometerSpeedFlow,
                locationManager.gnssQualityFlow
            ) { location, gpsSpeed, accelSpeed, gnssQuality ->
                FourTuple(location, gpsSpeed, accelSpeed, gnssQuality)
            }.collect { (location, gpsSpeed, accelSpeed, gnssQuality) ->

                val fusedSpeed = hybridSpeedFusion.fuseSpeed(
                    gpsSpeed = gpsSpeed,
                    accelerometerSpeed = accelSpeed,
                    gnssQualityScore = gnssQuality.score
                )

                val movementMode = movementClassifier.classifyMovement(fusedSpeed)

                if (movementMode != previousMode && previousMode != null) {
                    soundManager.playSound(movementMode)
                    vibrationManager.vibrateForMovementMode(movementMode.toString())
                }
                previousMode = movementMode

                _uiState.update {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        rawGnssSpeed = gpsSpeed,
                        accelerometerSpeed = accelSpeed,
                        fusedSpeed = fusedSpeed,
                        gnssQualityScore = gnssQuality.score,
                        satelliteCount = gnssQuality.satelliteCount,
                        avgSnr = gnssQuality.avgSnr,
                        movementMode = movementMode
                    )
                }

                // NEW: Add to tracking session if active
                if (trackingSession.isCurrentlyTracking()) {
                    trackingSession.addLocationAndSpeed(location, fusedSpeed)
                }
            }
        }
    }

    fun startSensors() {
        viewModelScope.launch {
            locationManager.startLocationUpdates()
            accelerometerManager.startListening()
            Log.d("SensorViewModel", "Sensors started (GPS + Accelerometer)")
        }
    }

    // NEW: Start tracking session
    fun startTracking() {
        trackingSession.startTracking()
        _saveStatus.value = "Recording started..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _saveStatus.value = null
        }
    }

    // NEW: Stop tracking and save session
    fun stopTrackingAndSave() {
        viewModelScope.launch {
            try {
                val username = userPreferences.getUsername()
                if (username == null) {
                    _saveStatus.value = "Error: Not logged in"
                    kotlinx.coroutines.delay(2000)
                    _saveStatus.value = null
                    return@launch
                }

                _saveStatus.value = "Saving session..."

                val sessionResult = trackingSession.stopTracking()

                if (sessionResult == null) {
                    _saveStatus.value = "No data recorded"
                    kotlinx.coroutines.delay(2000)
                    _saveStatus.value = null
                    return@launch
                }

                // Get location details from first coordinate
                val firstCoordinate = sessionResult.gpsCoordinates.firstOrNull()
                val locationDetails = if (firstCoordinate != null) {
                    geocodingService.getLocationDetails(
                        firstCoordinate.latitude,
                        firstCoordinate.longitude
                    )
                } else {
                    com.example.smartmotiondetector.location.LocationDetails(
                        city = "Unknown",
                        streetName = "Unknown",
                        streetNumber = ""
                    )
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDate = dateFormat.format(Date(sessionResult.startTimestamp))

                val sample = SensorSample(
                    username = username,
                    city = locationDetails.city,
                    streetName = locationDetails.streetName,
                    streetNumber = locationDetails.streetNumber,
                    startTimestamp = sessionResult.startTimestamp,
                    endTimestamp = sessionResult.endTimestamp,
                    durationSeconds = sessionResult.durationSeconds,
                    startTime = sessionResult.startTime,
                    endTime = sessionResult.endTime,
                    averageSpeed = sessionResult.averageSpeed,
                    movementMode = sessionResult.movementMode.name,
                    gpsCoordinates = sessionResult.gpsCoordinates,
                    latitude = firstCoordinate?.latitude ?: 0.0,
                    longitude = firstCoordinate?.longitude ?: 0.0,
                    speed = sessionResult.averageSpeed,
                    timestamp = sessionResult.startTimestamp,
                    date = currentDate
                )

                sampleDao.insert(sample)
                loadSampleCount()

                _saveStatus.value = "Session saved! (${sessionResult.gpsCoordinates.size} points)"
                Log.d("SensorViewModel", "Session saved: ${sessionResult.gpsCoordinates.size} coordinates, Avg Speed=${sessionResult.averageSpeed}")

                kotlinx.coroutines.delay(3000)
                _saveStatus.value = null
            } catch (e: Exception) {
                _saveStatus.value = "Error: ${e.message}"
                Log.e("SensorViewModel", "Error saving session", e)

                kotlinx.coroutines.delay(2000)
                _saveStatus.value = null
            }
        }
    }

    fun loadSampleCount() {
        viewModelScope.launch {
            try {
                val username = userPreferences.getUsername()
                if (username != null) {
                    val count = sampleDao.getCountByUsername(username)
                    _uiState.update { it.copy(sampleCount = count) }
                    Log.d("SensorViewModel", "Sample count for $username: $count")
                }
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Error loading sample count", e)
            }
        }
    }

    fun showCount() {
        viewModelScope.launch {
            val count = _uiState.value.sampleCount
            _countStatus.value = "Total sessions: $count"

            kotlinx.coroutines.delay(2000)
            _countStatus.value = null
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            try {
                val username = userPreferences.getUsername()
                if (username != null) {
                    val sessionsList = sampleDao.getAllByUsername(username)
                    _sessions.value = sessionsList
                    Log.d("SensorViewModel", "Loaded ${sessionsList.size} sessions")
                }
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Error loading sessions", e)
            }
        }
    }

    fun deleteSession(session: SensorSample) {
        viewModelScope.launch {
            try {
                sampleDao.delete(session)
                loadSessions()
                loadSampleCount()
                Log.d("SensorViewModel", "Session deleted: ${session.id}")
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Error deleting session", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationManager.stopLocationUpdates()
        accelerometerManager.stopListening()
        soundManager.release()
    }
}

private data class FourTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)