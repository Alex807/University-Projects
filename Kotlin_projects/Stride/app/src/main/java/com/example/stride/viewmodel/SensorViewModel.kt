package com.example.stride.viewmodel


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.stride.service.TrackingService
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stride.auth.UserPreferences
import com.example.stride.data.AppDatabase
import com.example.stride.data.SensorSample
import com.example.stride.location.GeocodingService
import com.example.stride.sensors.*
import com.example.stride.sound.MovementSoundManager
import com.example.stride.MovementVibrationManager
import com.example.stride.location.LocationDetails
import com.example.stride.tracking.TrackingSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)
    private val accelerometerManager = AccelerometerManager(application)
    private val hybridSpeedFusion = HybridSpeedFusion()
    private val movementClassifier = MovementClassifier()
    private val soundManager = MovementSoundManager(application)
    private val vibrationManager = MovementVibrationManager(application)
    private val geocodingService = GeocodingService(application)
    private val userPreferences = UserPreferences(application)
    private var isAppInForeground = false
    private var startLocationName: String = "Unknown"
    private var hasGottenStartLocation: Boolean = false
    private var trackingService: TrackingService? = null
    private var isServiceBound = false

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

    val isTracking: StateFlow<Boolean> = trackingSession.isTrackingFlow

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.getService()

            // NEW: Pass TrackingSession and stop callback
            trackingService?.setTrackingSession(
                trackingSession,
                onStopCallback = { stopTrackingAndSave() }
            )

            isServiceBound = true
            Log.d("SensorViewModel", "Service connected and bound")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            isServiceBound = false
            Log.d("SensorViewModel", "Service disconnected")
        }
    }

    private var previousMode: MovementMode? = null

    init {
        observeSensors()
        loadSampleCount()
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
        Log.d("SensorViewModel", "App foreground state: $inForeground")
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

                // Update service with current speed
                trackingService?.updateSpeed(fusedSpeed)

                //Get start location name only once when tracking starts
                if (trackingSession.isCurrentlyTracking() && !hasGottenStartLocation) {
                    getStartLocationName(location.latitude, location.longitude)
                    hasGottenStartLocation = true
                }

                val movementMode = movementClassifier.classifyMovement(fusedSpeed)

                if (movementMode != previousMode && previousMode != null) {
                    soundManager.playSound(movementMode)

                    if (isAppInForeground) {
                        vibrationManager.vibrateForMovementMode(movementMode.toString())
                        Log.d("SensorViewModel", "Vibration triggered (foreground)")
                    } else {
                        Log.d("SensorViewModel", "Vibration skipped (background)")
                    }
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

                // Record location and speed if tracking
                if (trackingSession.isCurrentlyTracking()) {
                    trackingSession.addLocationAndSpeed(location, fusedSpeed)
                }
            }
        }
    }

    private fun getStartLocationName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val locationDetails = geocodingService.getLocationDetails(latitude, longitude)
                startLocationName = locationDetails.city.ifEmpty {
                    locationDetails.streetName.ifEmpty { "Unknown" }
                }
                trackingService?.updateLocationName(startLocationName)
                Log.d("SensorViewModel", "Start location name: $startLocationName")
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Error getting start location name", e)
                startLocationName = "Unknown"
                trackingService?.updateLocationName(startLocationName)
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

    fun stopSensors() {
        viewModelScope.launch {
            locationManager.stopLocationUpdates()
            accelerometerManager.stopListening()
            Log.d("SensorViewModel", "Sensors stopped")
        }
    }

    fun startTracking() {
        Log.d("SensorViewModel", "Starting tracking session")
        trackingSession.startTracking()

        // Reset location flag
        hasGottenStartLocation = false
        startLocationName = "Unknown"

        // Start foreground service
        startTrackingService()

        _saveStatus.value = "Recording started..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _saveStatus.value = null
        }
    }

    fun stopTrackingAndSave() {
        viewModelScope.launch {
            try {
                val username = userPreferences.getUsername()
                if (username == null) {
                    _saveStatus.value = "Error: Not logged in"
                    kotlinx.coroutines.delay(2000)
                    _saveStatus.value = null

                    // Stop foreground service even if not logged in
                    stopTrackingService()
                    return@launch
                }

                _saveStatus.value = "Saving session..."

                val sessionResult = trackingSession.stopTracking()

                if (sessionResult == null) {
                    _saveStatus.value = "No data recorded"
                    kotlinx.coroutines.delay(2000)
                    _saveStatus.value = null

                    // Stop foreground service
                    stopTrackingService()
                    return@launch
                }

                val firstCoordinate = sessionResult.gpsCoordinates.firstOrNull()
                val locationDetails = if (firstCoordinate != null) {
                    geocodingService.getLocationDetails(
                        firstCoordinate.latitude,
                        firstCoordinate.longitude
                    )
                } else {
                    LocationDetails(
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
            } finally {
                // ALWAYS stop the foreground service when tracking ends
                stopTrackingService()
            }
        }
    }

    private fun startTrackingService() {
        val context = getApplication<Application>().applicationContext

        val serviceIntent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START_SERVICE
        }

        // Start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Bind to service
        val bindIntent = Intent(context, TrackingService::class.java)
        context.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        Log.d("SensorViewModel", "Tracking service started")
    }

    private fun stopTrackingService() {
        val context = getApplication<Application>().applicationContext

        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Error unbinding service", e)
            }
            isServiceBound = false
        }

        val serviceIntent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP_SERVICE
        }
        context.startService(serviceIntent)

        Log.d("SensorViewModel", "Tracking service stopped")
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

    // MODIFY onCleared() to unbind service
    override fun onCleared() {
        super.onCleared()

        if (isServiceBound) {
            val context = getApplication<Application>().applicationContext
            context.unbindService(serviceConnection)
            isServiceBound = false
        }

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