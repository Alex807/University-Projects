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
import com.example.stride.sensors.MovementClassifier
import com.example.stride.MovementVibrationManager
import com.example.stride.location.LocationDetails
import com.example.stride.tracking.TrackingSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)
    private val accelerometerManager = AccelerometerManager(application)
    private val stepDetector = StepDetector(application)
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

    private var trackingSession = TrackingSession()

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

            //Pass TrackingSession with both callbacks
            trackingService?.setTrackingSession(
                session = trackingSession,
                onStopCallback = { stopTrackingAndSave() },
                onStartNewCallback = { startTracking() }
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
        locationManager.startLocationUpdates()
        accelerometerManager.startListening()
        stepDetector.startListening()
        observeSensors()
        loadSampleCount()
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
        Log.d("SensorViewModel", "App foreground state: $inForeground")
    }

    private fun observeSensors() {
        viewModelScope.launch {
            try {
                locationManager.locationFlow
                    .combine(locationManager.filteredSpeedFlow) { location, gpsSpeed ->
                        Pair(location, gpsSpeed)
                    }
                    .combine(accelerometerManager.accelerometerSpeedFlow) { pair, accelSpeed ->
                        Triple(pair.first, pair.second, accelSpeed)
                    }
                    .combine(locationManager.gnssQualityFlow) { triple, gnssQuality ->
                        FourTuple(triple.first, triple.second, triple.third, gnssQuality)
                    }
                    .combine(stepDetector.isWalking) { fourTuple, isWalking ->
                        FiveTuple(fourTuple.first, fourTuple.second, fourTuple.third, fourTuple.fourth, isWalking)
                    }
                    .combine(stepDetector.stepsPerMinute) { fiveTuple, stepsPerMin ->
                        SixTuple(fiveTuple.first, fiveTuple.second, fiveTuple.third, fiveTuple.fourth, fiveTuple.fifth, stepsPerMin)
                    }
                    .collect { sixTuple ->
                        try {
                            val location = sixTuple.first
                            val gpsSpeed = sixTuple.second
                            val accelSpeed = sixTuple.third
                            val gnssQuality = sixTuple.fourth
                            val isWalking = sixTuple.fifth
                            val stepsPerMin = sixTuple.sixth

                            val stepSpeed = stepDetector.getEstimatedSpeed()

                            val effectiveSpeed = when {
                                isWalking && stepSpeed > 0.3 -> stepSpeed
                                gpsSpeed > 2.0 -> gpsSpeed
                                else -> maxOf(gpsSpeed, accelSpeed)
                            }

                            val movementMode = movementClassifier.classifyMovement(effectiveSpeed)
                            val movementModeString = movementMode.name

                            accelerometerManager.setMovementMode(movementModeString)

                            val gnssScore = (gnssQuality.score / 100.0).coerceIn(0.0, 1.0)

                            val fusedSpeed = hybridSpeedFusion.fuseSpeed(
                                gpsSpeed = gpsSpeed,
                                accelerometerSpeed = accelSpeed,
                                gnssQualityScore = gnssScore,
                                stepDetectorSpeed = stepSpeed,
                                isWalking = isWalking,
                                movementMode = movementModeString
                            )

                            if (trackingSession.isCurrentlyTracking()) {
                                trackingSession.updateMovementMode(movementMode)
                            }

                            trackingService?.updateSpeed(fusedSpeed)
                            trackingService?.updateCurrentLocation(location.latitude, location.longitude)

                            if (trackingSession.isCurrentlyTracking() && !hasGottenStartLocation) {
                                if (location.hasAccuracy() && location.accuracy <= 30f) {
                                    getStartLocationName(location.latitude, location.longitude)
                                    hasGottenStartLocation = true
                                }
                            }

                            if (movementMode != previousMode && previousMode != null) {
                                soundManager.playSound(movementMode)

                                if (isAppInForeground) {
                                    vibrationManager.vibrateForMovementMode(movementMode.toString())
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

                            if (trackingSession.isCurrentlyTracking()) {
                                trackingSession.addLocationAndSpeed(location, fusedSpeed)
                            }
                        } catch (e: Exception) {
                            Log.e("SensorViewModel", "Error processing sensor data", e)
                        }
                    }
            } catch (e: CancellationException) {
                Log.d("SensorViewModel", "observeSensors cancelled")
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Fatal error in observeSensors", e)
            }
        }
    }

    private fun getStartLocationName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                // Wait 1 seconds before geocoding
                delay(1000)

                // Call the new function that gets both city and street
                trackingService?.updateStartLocation(latitude, longitude)
                Log.d("SensorViewModel", "Updating start location with coordinates: $latitude, $longitude")
            } catch (e: Exception) {
                Log.w("SensorViewModel", "Failed to get location name: ${e.message}")
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
        trackingSession = TrackingSession()  //important for start another session from notif(create a new session each time)
        trackingSession.startTracking()

        // Reset location flag
        hasGottenStartLocation = false
        startLocationName = "Unknown"

        // Start foreground service
        startTrackingService()

        // Set tracking session immediately if service is already bound
        if (isServiceBound && trackingService != null) {
            trackingService?.setTrackingSession(
                session = trackingSession,
                onStopCallback = { stopTrackingAndSave() },
                onStartNewCallback = { startTracking() }
            )
        }

        _saveStatus.value = "Recording started..."
        viewModelScope.launch {

            delay(2000)
            _saveStatus.value = null
        }
    }

    fun stopTrackingAndSave() {
        viewModelScope.launch {
            try {
                if (!trackingSession.isCurrentlyTracking()) {
                    Log.d("SensorViewModel", "No active session to save")
                    stopTrackingService()
                    return@launch
                }

                val username = userPreferences.getUsername()
                if (username == null) {
                    _saveStatus.value = "Error: Not logged in"
                    delay(2000)
                    _saveStatus.value = null

                    // Stop foreground service even if not logged in
                    stopTrackingService()
                    return@launch
                }

                _saveStatus.value = "Saving session..."

                val sessionResult = trackingSession.stopTracking()

                if (sessionResult == null) {
                    _saveStatus.value = "No data recorded"
                    delay(2000)
                    _saveStatus.value = null

                    // Stop foreground service
                    stopTrackingService()
                    return@launch
                }

                val firstCoordinate = sessionResult.gpsCoordinates.firstOrNull()
                val lastCoordinate = sessionResult.gpsCoordinates.lastOrNull()

                // Try geocoding, but use "Unknown" if it fails (offline)
                val locationDetails = if (firstCoordinate != null) {
                    try {
                        withTimeout(5000) { // 5 second timeout
                            geocodingService.getLocationDetails(
                                firstCoordinate.latitude,
                                firstCoordinate.longitude
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("SensorViewModel", "Geocoding failed (offline?): ${e.message}")
                        LocationDetails(
                            city = "Offline",
                            streetName = "Offline",
                            streetNumber = ""
                        )
                    }
                } else {
                    LocationDetails(
                        city = "Unknown",
                        streetName = "Unknown",
                        streetNumber = ""
                    )
                }

                // Get end location details
                val endLocationDetails = if (lastCoordinate != null && lastCoordinate != firstCoordinate) {
                    try {
                        withTimeout(5000) {
                            geocodingService.getLocationDetails(
                                lastCoordinate.latitude,
                                lastCoordinate.longitude
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("SensorViewModel", "End location geocoding failed: ${e.message}")
                        locationDetails // Use start location as fallback
                    }
                } else {
                    locationDetails
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

                val insertedId = sampleDao.insert(sample).toInt() // Get the inserted session ID
                loadSampleCount()

                // Show distance in save confirmation
                val distanceKm = sessionResult.totalDistance / 1000.0

                // Detect if this was an auto-save (app closed)
                val isAutoSave = !isAppInForeground
                val saveMessage = if (isAutoSave) {
                    "Session auto-saved! Distance: %.2f km (%d points)".format(
                        distanceKm,
                        sessionResult.gpsCoordinates.size
                    )
                } else {
                    "Session saved! Distance: %.2f km (%d points)".format(
                        distanceKm,
                        sessionResult.gpsCoordinates.size
                    )
                }

                _saveStatus.value = saveMessage
                Log.d("SensorViewModel", "Session saved with ID: $insertedId, ${sessionResult.gpsCoordinates.size} coordinates, Distance=${sessionResult.totalDistance}m, AutoSave=$isAutoSave")
                Log.d("SensorViewModel", "Session saved with ID: $insertedId, ${sessionResult.gpsCoordinates.size} coordinates, Distance=${sessionResult.totalDistance}m")

                // NEW: Show session saved notification
                val startLocationName = if (locationDetails.streetName.isNotEmpty() && locationDetails.streetName != "Offline") {
                    "${locationDetails.city}, ${locationDetails.streetName}"
                } else {
                    locationDetails.city
                }

                val endLocationName = if (endLocationDetails.streetName.isNotEmpty() && endLocationDetails.streetName != "Offline") {
                    "${endLocationDetails.city}, ${endLocationDetails.streetName}"
                } else {
                    endLocationDetails.city
                }

                // Call the notification function from the service
                trackingService?.showSessionSavedNotification(
                    sessionId = insertedId,
                    startLocation = startLocationName,
                    endLocation = endLocationName,
                    totalDistanceMeters = sessionResult.totalDistance
                )

                kotlinx.coroutines.delay(3000)
                _saveStatus.value = null

            } catch (e: Exception) {
                _saveStatus.value = "Error: ${e.message}"
                Log.e("SensorViewModel", "Error saving session", e)
                kotlinx.coroutines.delay(2000)
                _saveStatus.value = null
            } finally {
                stopTrackingService()
            }
        }
    }

    private fun startTrackingService() {
        val context = getApplication<Application>().applicationContext

        // check if service is already bound
        if (isServiceBound && trackingService != null) {
            Log.d("SensorViewModel", "Service already bound, just starting foreground")

            // Service is already bound, just start foreground mode
            val serviceIntent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START_SERVICE
            }
            context.startService(serviceIntent)

            return
        }

        val serviceIntent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START_SERVICE
        }

        // Start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // wait for service to be ready before binding
        viewModelScope.launch {
            delay(500) // Give service time to start

            // Now bind to service
            val bindIntent = Intent(context, TrackingService::class.java)
            context.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)

            Log.d("SensorViewModel", "Tracking service started and binding initiated")
        }
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
        locationManager.stopLocationUpdates()
        accelerometerManager.stopListening()
        stepDetector.stopListening()
        soundManager.release()

        // Unbind service
        if (isServiceBound) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
                isServiceBound = false
            } catch (e: Exception) {
                Log.e("SensorViewModel", "Error unbinding service: ${e.message}")
            }
        }
    }
}

private data class FourTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class FiveTuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class SixTuple<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)