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
import androidx.core.graphics.createBitmap

class TrackingService : Service() {

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "stride_tracking_channel"
        private const val CHANNEL_NAME = "Stride Tracking"

        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"

        private var isServiceRunning = false

        fun isRunning(): Boolean = isServiceRunning
    }

    private val binder = LocalBinder()
    private var trackingSession: TrackingSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentSpeed: Double = 0.0
    private var currentDistance: Double = 0.0
    private var startTime: Long = 0

    // Callback to stop tracking from notification
    private var onStopTrackingCallback: (() -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
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
        }

        return START_STICKY
    }

    fun setTrackingSession(session: TrackingSession, onStopCallback: () -> Unit) {
        this.trackingSession = session
        this.onStopTrackingCallback = onStopCallback
        observeTrackingState()
        Log.d(TAG, "TrackingSession set and observing")
    }

    fun updateSpeed(speed: Double) {
        currentSpeed = speed
    }

    private fun startForegroundService() {
        Log.d(TAG, "Starting foreground service")
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

        startUpdatingNotification()
    }

    private fun observeTrackingState() {
        serviceScope.launch {
            trackingSession?.isTrackingFlow?.collectLatest { isTracking ->
                Log.d(TAG, "Tracking state changed: $isTracking")
                if (!isTracking) {
                    // Tracking stopped, stop the service
                    stopForegroundService()
                }
            }
        }
    }

    private fun startUpdatingNotification() {
        serviceScope.launch {
            while (isActive && trackingSession?.isCurrentlyTracking() == true) {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                currentDistance = trackingSession?.getCurrentDistance() ?: 0.0

                updateNotification(currentSpeed, currentDistance, duration)
                delay(1000) // Update every second
            }
        }
    }

    private fun updateNotification(speed: Double, distance: Double, durationSeconds: Long) {
        val notification = createNotification(speed, distance, durationSeconds)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        val locationName = currentLocationName

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active Session")
            .setContentText("📍 $locationName  •  🏃 $speedText  •   🚗 $distanceText")
            .setSmallIcon(R.drawable.notification_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setColor(0xFF07F1F3.toInt())
            .setColorized(false)
            .addAction(
                android.R.drawable.ic_media_pause, // System stop icon
                "Stop Session",
                stopPendingIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private var currentLocationName: String = "Unknown"

    fun updateLocationName(locationName: String) {
        currentLocationName = locationName
        Log.d(TAG, "Start location name updated: $locationName")
    }

    private fun getCurrentLocationName(): String {
        return currentLocationName
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
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isServiceRunning = false
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows real-time tracking session data"
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableLights(true)
            lightColor = 0xFF6200EE.toInt()
            enableVibration(false) // Don't vibrate for updates
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created: $CHANNEL_ID")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        serviceScope.cancel()
        isServiceRunning = false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed - saving session and stopping")

        // Trigger stop tracking callback
        runBlocking {
            onStopTrackingCallback?.invoke()
        }

        stopForegroundService()
    }
}