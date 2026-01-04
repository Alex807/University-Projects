package com.example.stride.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.location.LocationManager as AndroidLocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: AndroidLocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager

    private val gnssQualityEvaluator = GnssQualityEvaluator()
    private val speedFilter = GpsSpeedFilter()

    private val _locationFlow = MutableStateFlow(Location("default").apply {
        latitude = 0.0
        longitude = 0.0
        speed = 0f
    })
    val locationFlow: StateFlow<Location> = _locationFlow

    private val _filteredSpeedFlow = MutableStateFlow(0.0)
    val filteredSpeedFlow: StateFlow<Double> = _filteredSpeedFlow

    private val _gnssQualityFlow = MutableStateFlow(GnssQualityResult(0, 0, 0f, 0))
    val gnssQualityFlow: StateFlow<GnssQualityResult> = _gnssQualityFlow

    private var isUpdating = false
    private var gnssCallbackRegistered = false

    // High-accuracy location request
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L  // Update every 1 second
    ).apply {
        setMinUpdateIntervalMillis(300L)  // Allow updates as fast as 500ms
        setMaxUpdateDelayMillis(1000L)    // for faster updates
        setWaitForAccurateLocation(true)  // Wait for accurate fix
        setMinUpdateDistanceMeters(0f)    // No minimum distance (we filter in code)
        setMaxUpdates(Int.MAX_VALUE)      // Unlimited updates
    }.build()

    private val locationListener = LocationListener { location ->
        processLocation(location)
    }

    private val fusedLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                processLocation(location)
            }
        }
    }

    private fun processLocation(location: Location) {
        // Filter the GPS speed using Kalman filter
        val filteredSpeed = speedFilter.filterSpeed(
            rawSpeed = location.speed,
            accuracy = location.accuracy,
            hasSpeed = location.hasSpeed()
        )

        // Update flows
        _locationFlow.value = location
        _filteredSpeedFlow.value = filteredSpeed

        Log.d("LocationManager", "Raw: ${location.speed} m/s, Filtered: $filteredSpeed m/s, Accuracy: ${location.accuracy}m")
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onStarted() {
            Log.d("LocationManager", "GNSS started")
        }

        override fun onStopped() {
            Log.d("LocationManager", "GNSS stopped")
        }

        override fun onFirstFix(ttffMillis: Int) {
            Log.d("LocationManager", "GNSS first fix: $ttffMillis ms")
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val qualityResult = gnssQualityEvaluator.evaluateQuality(status)
            _gnssQualityFlow.value = qualityResult
            Log.d("LocationManager", "Satellites: ${qualityResult.satelliteCount}, Quality: ${qualityResult.score}, SNR: ${qualityResult.avgSnr}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (isUpdating) {
            Log.d("LocationManager", "Already updating, skipping")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationManager", "Location permission not granted")
            return
        }

        try {
            // Reset filters
            speedFilter.reset()

            // Reset state
            _locationFlow.value = Location("gps").apply {
                latitude = 0.0
                longitude = 0.0
                speed = 0f
            }
            _filteredSpeedFlow.value = 0.0
            _gnssQualityFlow.value = GnssQualityResult(0, 0, 0f, 0)

            // Set isUpdating BEFORE starting updates to prevent race conditions
            isUpdating = true

            // Request location updates from GPS provider
            try {
                locationManager.requestLocationUpdates(
                    AndroidLocationManager.GPS_PROVIDER,
                    1000L,  // 1 second
                    0f,     // No minimum distance
                    locationListener,
                    Looper.getMainLooper()
                )
                Log.d("LocationManager", "GPS provider updates started")
            } catch (e: SecurityException) {
                Log.e("LocationManager", "Security exception requesting GPS updates", e)
                isUpdating = false
                return
            } catch (e: Exception) {
                Log.e("LocationManager", "Error requesting GPS updates", e)
                // Continue to try fused location
            }

            // Register GNSS status callback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Android 11+ requires executor
                        locationManager.registerGnssStatusCallback(
                            context.mainExecutor,
                            gnssStatusCallback
                        )
                    } else {
                        // Android 7-10 uses handler
                        @Suppress("DEPRECATION")
                        locationManager.registerGnssStatusCallback(
                            gnssStatusCallback,
                            null  // Use main handler
                        )
                    }
                    gnssCallbackRegistered = true
                    Log.d("LocationManager", "GNSS status callback registered")
                } catch (e: SecurityException) {
                    Log.e("LocationManager", "Security exception registering GNSS callback", e)
                } catch (e: Exception) {
                    Log.e("LocationManager", "Failed to register GNSS callback", e)
                }
            }

            // Also use Fused Location Provider as backup
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    fusedLocationCallback,
                    Looper.getMainLooper()
                ).addOnSuccessListener {
                    Log.d("LocationManager", "Fused location updates started")
                }.addOnFailureListener { e ->
                    Log.e("LocationManager", "Failed to start fused location updates", e)
                }
            } catch (e: Exception) {
                Log.e("LocationManager", "Error requesting fused location updates", e)
            }

            Log.d("LocationManager", "Started location updates")
        } catch (e: Exception) {
            Log.e("LocationManager", "Error starting location updates", e)
            isUpdating = false
        }
    }

    fun stopLocationUpdates() {
        if (!isUpdating) {
            Log.d("LocationManager", "Not updating, nothing to stop")
            return
        }

        try {
            isUpdating = false

            // Stop GPS provider updates
            try {
                locationManager.removeUpdates(locationListener)
                Log.d("LocationManager", "GPS provider updates stopped")
            } catch (e: Exception) {
                Log.e("LocationManager", "Error removing GPS updates", e)
            }

            // Unregister GNSS callback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallbackRegistered) {
                try {
                    locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
                    gnssCallbackRegistered = false
                    Log.d("LocationManager", "GNSS status callback unregistered")
                } catch (e: Exception) {
                    Log.e("LocationManager", "Failed to unregister GNSS callback", e)
                }
            }

            // Stop fused location updates
            try {
                fusedLocationClient.removeLocationUpdates(fusedLocationCallback)
                Log.d("LocationManager", "Fused location updates stopped")
            } catch (e: Exception) {
                Log.e("LocationManager", "Error removing fused location updates", e)
            }

            // Reset filter
            try {
                speedFilter.reset()
            } catch (e: Exception) {
                Log.e("LocationManager", "Error resetting speed filter", e)
            }

            Log.d("LocationManager", "Stopped location updates")
        } catch (e: Exception) {
            Log.e("LocationManager", "Error stopping location updates", e)
        }
    }
}