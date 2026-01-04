package com.example.stride.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.stride.MainActivity
import com.example.stride.R
import com.example.stride.tracking.TrackingSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class TrackingService : Service() {

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "stride_tracking_channel"
        private const val CHANNEL_NAME = "Stride Tracking"

        private const val SAVED_NOTIFICATION_ID = 2002
        private const val SAVED_CHANNEL_ID = "stride_session_saved_channel"
        private const val SAVED_CHANNEL_NAME = "Session Saved"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val ACTION_OPEN_SESSION_DETAILS = "ACTION_OPEN_SESSION_DETAILS"

        const val ACTION_OPEN_HOME = "ACTION_OPEN_HOME"
        const val ACTION_START_NEW_SESSION = "ACTION_START_NEW_SESSION"

        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"

        private var isServiceRunning = false

        fun isRunning(): Boolean = isServiceRunning
    }

    private val binder = LocalBinder()
    private var trackingSession: TrackingSession? = null
    private var serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    private var currentSpeed: Double = 0.0
    private var currentDistance: Double = 0.0
    private var startTime: Long = 0

    // Callback to stop tracking from notification
    private var onStopTrackingCallback: (() -> Unit)? = null
    private var onStartNewSessionCallback: (() -> Unit)? = null

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var currentLocationName: String = "Unknown"
    private var currentStreetName: String = ""

    private var startLocationCityName: String = "Unknown"
    private var startLocationStreetName: String = ""
    private var lastLocationUpdateTime: Long = 0
    private val LOCATION_UPDATE_INTERVAL = 30_000L // 30 seconds

    private var isWaitingForAccurateHomeLocation = true
    private var isHomeLocationOffline = false

    private var homeLocationLatitude: Double = 0.0
    private var homeLocationLongitude: Double = 0.0

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
        createSessionSavedNotificationChannel()
        isServiceRunning = true
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundService()
            }
            ACTION_STOP_TRACKING -> {
                stopTrackingFromNotification()
            }
            ACTION_STOP_SERVICE -> {
                stopForegroundService()
            }
            ACTION_START_NEW_SESSION -> {
                startNewSessionFromNotification()
            }
            ACTION_OPEN_HOME -> {
                openHomeScreen()
            }
        }

        return START_STICKY
    }

    private fun recreateScope() { //to can re-active the update-notif after each end of session
        if (!serviceScope.isActive) {
            serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            Log.d(TAG, "CoroutineScope recreated")
        }
    }

    fun setTrackingSession(
        session: TrackingSession,
        onStopCallback: () -> Unit,
        onStartNewCallback: () -> Unit
    ) {
        this.trackingSession = session
        this.onStopTrackingCallback = onStopCallback
        this.onStartNewSessionCallback = onStartNewCallback
        this.startTime = System.currentTimeMillis()
        this.isWaitingForAccurateHomeLocation = true
        this.homeLocationLatitude = 0.0
        this.homeLocationLongitude = 0.0
        this.isHomeLocationOffline = false
        observeTrackingState()
        startUpdatingNotification()
        Log.d(TAG, "TrackingSession set and observing")
    }

    fun updateSpeed(speed: Double) {
        currentSpeed = speed
    }

    private fun startForegroundService() {
        Log.d(TAG, "Starting foreground service")
        recreateScope()
        startTime = System.currentTimeMillis()

        val notification = createNotification(
            speed = 0.0,
            distance = 0.0,
            durationSeconds = 0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        // DON'T start updating yet - wait for trackingSession to be set
        Log.d(TAG, "Foreground service started, waiting for tracking session")
    }

    private fun observeTrackingState() {
        serviceScope.launch {
            try {
                trackingSession?.isTrackingFlow?.collectLatest { isTracking ->
                    Log.d(TAG, "Tracking state changed: $isTracking")
                    if (!isTracking) {
                        stopForegroundService()
                    }
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "observeTrackingState cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error in observeTrackingState", e)
            }
        }
    }

    private fun startUpdatingNotification() {
        serviceScope.launch {
            try {
                while (isActive) {
                    val session = trackingSession
                    if (session == null || !session.isCurrentlyTracking()) {
                        Log.d(TAG, "Session ended, stopping notification updates")
                        break
                    }

                    val currentTime = System.currentTimeMillis()
                    val duration = (currentTime - startTime) / 1000

                    currentDistance = session.getCurrentDistance()

                    // Update location every 30 seconds
                    if (currentTime - lastLocationUpdateTime >= LOCATION_UPDATE_INTERVAL) {
                        updateCurrentLocationName()

                        // Retry home location geocoding if it's offline
                        if (isHomeLocationOffline && homeLocationLatitude != 0.0 && homeLocationLongitude != 0.0) {
                            Log.d(TAG, "Retrying home location geocoding (was offline)")
                            retryHomeLocationGeocoding()
                        }

                        lastLocationUpdateTime = currentTime
                    }

                    Log.d(TAG, "Notification update: Speed=$currentSpeed m/s, Distance=$currentDistance m, Duration=${duration}s")

                    updateNotification(currentSpeed, currentDistance, duration)
                    delay(1000)
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "Notification updates cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification", e)
            }
        }
    }

    private fun updateCurrentLocationName() {
        if (currentLatitude == 0.0 && currentLongitude == 0.0) {
            Log.d(TAG, "No current location available yet")
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(applicationContext, Locale.getDefault())

                if (!android.location.Geocoder.isPresent()) {
                    Log.w(TAG, "Geocoder not available")
                    return@launch
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(currentLatitude, currentLongitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            processCurrentAddress(addresses[0])
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        processCurrentAddress(addresses[0])
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Geocoding failed for current location: ${e.message}", e)
                currentLocationName = "Offline"
                currentStreetName = ""
            }
        }
    }

    private fun processCurrentAddress(address: android.location.Address) {
        val locationName = when {
            !address.locality.isNullOrEmpty() -> address.locality
            !address.subAdminArea.isNullOrEmpty() -> address.subAdminArea
            !address.adminArea.isNullOrEmpty() -> address.adminArea
            else -> "Unknown"
        }
        currentLocationName = locationName

        val streetName = when {
            !address.thoroughfare.isNullOrEmpty() -> address.thoroughfare
            !address.subThoroughfare.isNullOrEmpty() -> address.subThoroughfare
            else -> ""
        }
        currentStreetName = streetName

        Log.d(TAG, "Current location: $currentLocationName, Street: $currentStreetName")
    }

    private fun updateNotification(speed: Double, distance: Double, durationSeconds: Long) {
        val notification = createNotification(speed, distance, durationSeconds)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @SuppressLint("DefaultLocale")
    private fun createNotification(speed: Double, distance: Double, durationSeconds: Long): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            pendingIntentFlags
        )

        // Stop tracking action
        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            pendingIntentFlags
        )

        //Format duration as HH:MM:SS or MM:SS
        val durationText = formatDuration(durationSeconds)

        // Format distance
        val distanceText = if (distance >= 1000) {
            String.format("%.2f km", distance / 1000)
        } else {
            String.format("%.0f m", distance)
        }

        // Format speed
        val speedKmh = speed * 3.6
        val speedText = String.format("%.1f km/h", speedKmh)

        // Get start location name
        val startLocationName = when {
            isWaitingForAccurateHomeLocation -> "Acquiring GPS..."
            isHomeLocationOffline -> "Offline (retrying...)"
            startLocationStreetName.isNotEmpty() -> "$startLocationCityName, $startLocationStreetName"
            else -> startLocationCityName
        }

        // format current location with street name
        val currentLocationDisplay = if (currentStreetName.isNotEmpty()) {
            "$currentLocationName, $currentStreetName"
        } else {
            currentLocationName
        }

        //Expanded content (single line with bullets)
        val expandedText = "📍$currentLocationDisplay  •  🏃 $speedText \n🏠 $startLocationName  •  ⏱️ $durationText"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active Session    🚗 $distanceText")
            .setContentText("📍$currentLocationDisplay  •  🏃 $speedText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedText)
            )
            .setSmallIcon(R.drawable.notification_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setColor(0xFF07F1F3.toInt())
            .setColorized(false)
            .addAction(
                android.R.drawable.ic_media_pause, // System stop icon
                "Stop Session",
                stopPendingIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) //Only alert on first notification
            .setSound(null) //Explicitly no sound
            .setVibrate(null) // Explicitly no vibration
            .build()
    }

    fun showSessionSavedNotification(
        sessionId: Int,
        startLocation: String,
        endLocation: String,
        totalDistanceMeters: Double
    ) {
        Log.d(TAG, "Showing session saved notification for session ID: $sessionId")

        // Format distance
        val distanceText = if (totalDistanceMeters >= 1000) {
            String.format("%.2f km", totalDistanceMeters / 1000)
        } else {
            String.format("%.0f m", totalDistanceMeters)
        }

        // Create intent to open HOME SCREEN (not session details)
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_HOME
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val homePendingIntent = PendingIntent.getActivity(
            this,
            0, // Different request code
            homeIntent,
            pendingIntentFlags
        )

        val detailsIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_SESSION_DETAILS
            putExtra(EXTRA_SESSION_ID, sessionId)
            putExtra("AUTO_DISMISS_NOTIFICATION", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val detailsPendingIntent = PendingIntent.getActivity(
            this,
            sessionId, // Use sessionId as request code for uniqueness
            detailsIntent,
            pendingIntentFlags
        )

        val startAgainIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_START_NEW_SESSION
        }

        val startAgainPendingIntent = PendingIntent.getService(
            this,
            sessionId + 10000, // Different request code to avoid conflicts
            startAgainIntent,
            pendingIntentFlags
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, SAVED_CHANNEL_ID)
            .setContentTitle("Session Saved Successfully!     🚗 $distanceText")
            .setContentText("🏠 $startLocation  - 📍$endLocation")
            .setSmallIcon(R.drawable.notification_logo)
            .setContentIntent(homePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH priority for heads-up
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(0xFF07F1F3.toInt()) // Brand color
            .setColorized(false)
            .setAutoCancel(true) // Auto-dismiss when tapped
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 200, 100, 200)) // Pleasant vibration
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .addAction(
                android.R.drawable.ic_media_pause, // You can use a different icon if you have one
                "Details",
                detailsPendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_pause, // You can use a different icon if you have one
                "Start Again",
                startAgainPendingIntent
            )
            .build()

        // Show notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SAVED_NOTIFICATION_ID, notification)

        Log.d(TAG, "Session saved notification displayed")
    }

    fun updateStartLocation(latitude: Double, longitude: Double) {
        // Skip if home location already set successfully
        if (!isWaitingForAccurateHomeLocation && !isHomeLocationOffline) {
            Log.d(TAG, "Home location already set, skipping")
            return
        }

        // Validate coordinates
        if (latitude == 0.0 && longitude == 0.0) {
            Log.d(TAG, "Skipping home location: Invalid coordinates (0,0)")
            return
        }

        // Check if coordinates are reasonable (not in the ocean)
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            Log.d(TAG, "Skipping home location: Invalid coordinate range")
            return
        }

        // Save coordinates for potential retry
        homeLocationLatitude = latitude
        homeLocationLongitude = longitude

        serviceScope.launch(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(applicationContext, Locale.getDefault())

                // Check if Geocoder is available
                if (!android.location.Geocoder.isPresent()) {
                    Log.w(TAG, "Geocoder not available on this device")
                    startLocationCityName = "Unknown"
                    startLocationStreetName = ""
                    isWaitingForAccurateHomeLocation = false
                    isHomeLocationOffline = false
                    return@launch
                }

                // Use modern API for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            processStartAddress(addresses[0])
                        } else {
                            startLocationCityName = "Offline"
                            startLocationStreetName = ""
                            isHomeLocationOffline = true
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        processStartAddress(addresses[0])
                    } else {
                        startLocationCityName = "Offline"
                        startLocationStreetName = ""
                        isHomeLocationOffline = true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Geocoding failed for start location: ${e.message}", e)
                startLocationCityName = "Offline"
                startLocationStreetName = ""
                isHomeLocationOffline = true
            }
        }
    }

    private fun processStartAddress(address: android.location.Address) {
        val locationName = when {
            !address.locality.isNullOrEmpty() -> address.locality
            !address.subAdminArea.isNullOrEmpty() -> address.subAdminArea
            !address.adminArea.isNullOrEmpty() -> address.adminArea
            !address.countryName.isNullOrEmpty() -> address.countryName
            else -> null
        }

        //  Check if we got a valid location
        if (locationName.isNullOrEmpty()) {
            Log.w(TAG, " Geocoding returned empty address - likely offline or no data")
            startLocationCityName = "Offline"
            startLocationStreetName = ""
            isHomeLocationOffline = true
            return
        }
        startLocationCityName = locationName

        if (currentLocationName == "Unknown") {
            currentLocationName = locationName
        }

        val streetName = when {
            !address.thoroughfare.isNullOrEmpty() -> address.thoroughfare
            !address.subThoroughfare.isNullOrEmpty() -> address.subThoroughfare
            else -> ""
        }
        startLocationStreetName = streetName

        if (currentStreetName == "") {
            currentStreetName = streetName
        }

        //Mark home location as successfully set
        isWaitingForAccurateHomeLocation = false
        isHomeLocationOffline = false
        Log.d(TAG, "Home location SET: $startLocationCityName, Street: $startLocationStreetName")
    }

    private fun retryHomeLocationGeocoding() {
        if (homeLocationLatitude == 0.0 && homeLocationLongitude == 0.0) {
            Log.d(TAG, "No saved coordinates to retry geocoding")
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(applicationContext, Locale.getDefault())

                if (!android.location.Geocoder.isPresent()) {
                    Log.w(TAG, "Geocoder not available")
                    return@launch
                }

                Log.d(TAG, "Retrying home location geocoding: ($homeLocationLatitude, $homeLocationLongitude)")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(homeLocationLatitude, homeLocationLongitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            processStartAddress(addresses[0])
                            Log.d(TAG, "Home location geocoding retry SUCCESS")
                        } else {
                            Log.w(TAG, "Retry: No address found")
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(homeLocationLatitude, homeLocationLongitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        processStartAddress(addresses[0])
                        Log.d(TAG, "Home location geocoding retry SUCCESS")
                    } else {
                        Log.w(TAG, "Retry: No address found")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Retry geocoding failed: ${e.message}", e)
            }
        }
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        currentLatitude = latitude
        currentLongitude = longitude
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else if (minutes > 0) {
            String.format("%02d:%02d", minutes, secs)
        } else {
            String.format("%02ds", secs)
        }
    }

    private fun stopTrackingFromNotification() {
        Log.d(TAG, "Stop tracking requested from notification")
        serviceScope.launch(Dispatchers.Main) {
            onStopTrackingCallback?.invoke()
            // Service will stop automatically when isTracking becomes false
        }
    }

    private fun stopForegroundService() {
        Log.d(TAG, "Stopping foreground service")

        // Cancel scope gracefully
        serviceScope.cancel()

        // Create new scope for next session
        serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        // Reset location variables
        startLocationCityName = "Unknown"
        startLocationStreetName = ""
        currentLocationName = "Unknown"
        currentStreetName = ""
        lastLocationUpdateTime = 0

        isWaitingForAccurateHomeLocation = true
        homeLocationLatitude = 0.0
        homeLocationLongitude = 0.0
        isHomeLocationOffline = false

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isServiceRunning = false
    }

    private fun startNewSessionFromNotification() {
        //Dismiss the saved session notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(SAVED_NOTIFICATION_ID)

        // Check if there's already an active session
        val isCurrentlyTracking = trackingSession?.isCurrentlyTracking() ?: false

        if (isCurrentlyTracking) {
            // Show a toast or notification that session is already running
            showSessionAlreadyRunningNotification()
            return
        }

        // Let ViewModel handle everything: create new session, start service, bind
        onStartNewSessionCallback?.invoke()
    }

    private fun openHomeScreen() {
        Log.d(TAG, "Opening home screen from notification")

        // Create intent to open MainActivity (home screen)
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_HOME
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun showSessionAlreadyRunningNotification() {
        val notification = NotificationCompat.Builder(this, SAVED_CHANNEL_ID)
            .setContentTitle("Session Already Running!! 🛑")
            .setContentText("Please stop the current session before starting a new one.")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFFE82500.toInt())
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 100, 100, 100))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SAVED_NOTIFICATION_ID + 1, notification)
    }

    private fun showAutoSaveErrorNotification() {
        val notification = NotificationCompat.Builder(this, SAVED_CHANNEL_ID)
            .setContentTitle("Auto-Save Failed!! ❌")
            .setContentText("Failed to save your session after the app was closed.")
            .setSmallIcon(R.drawable.notification_logo)
            .setColor(0xFFE82500.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 100, 100, 100))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SAVED_NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW //to have no-sound notifications
        ).apply {
            description = "Shows real-time tracking session data"
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableLights(false)
            enableVibration(false) // Don't vibrate for updates
            setSound(null, null) // Explicitly disable sound
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created: $CHANNEL_ID")
    }

    private fun createSessionSavedNotificationChannel() {
        val channel = NotificationChannel(
            SAVED_CHANNEL_ID,
            SAVED_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH // HIGH for heads-up notification
        ).apply {
            description = "Notifies when a tracking session is successfully saved"
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableLights(true)
            lightColor = 0xFF07F1F3.toInt() // Brand color
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200) // Pleasant vibration pattern
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Session Saved notification channel created: $SAVED_CHANNEL_ID")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        serviceScope.cancel()
        isServiceRunning = false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed - App closed from recent apps")

        // Check if there's an active tracking session
        val isCurrentlyTracking = trackingSession?.isCurrentlyTracking() ?: false

        if (isCurrentlyTracking) {
            Log.d(TAG, "Active session detected - auto-saving before shutdown")

            // Use separate coroutine scope that won't be cancelled
            val saveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

            // Trigger stop tracking callback to save the session
            saveScope.launch {
                try {
                    // Stop sensors first (via callback)
                    withContext(Dispatchers.Main) {
                        onStopTrackingCallback?.invoke()
                    }
                    // Give it time to save
                    delay(3000)

                } catch (e: Exception) {
                    Log.e(TAG, "Error auto-saving session", e)
                    withContext(Dispatchers.Main) {
                        showAutoSaveErrorNotification()
                    }
                } finally {
                    // Stop service after save completes
                    withContext(Dispatchers.Main) {
                        stopForegroundService()
                    }
                }
            }
            // Don't call stopForegroundService() here - let the coroutine handle it
            return
        }
        // No active session, just stop service
        stopForegroundService()
    }
}