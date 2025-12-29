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
        setMinUpdateIntervalMillis(500L)
        setMaxUpdateDelayMillis(2000L)
        setWaitForAccurateLocation(true)
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
            // Stop any existing updates first
            stopLocationUpdates()

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

            // Request location updates from GPS provider
            locationManager.requestLocationUpdates(
                AndroidLocationManager.GPS_PROVIDER,
                1000L,  // 1 second
                0f,     // No minimum distance
                locationListener,
                Looper.getMainLooper()
            )

            // Register GNSS status callback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    locationManager.registerGnssStatusCallback(
                        gnssStatusCallback,
                        null  // Use main handler
                    )
                    gnssCallbackRegistered = true
                    Log.d("LocationManager", "GNSS status callback registered")
                } catch (e: Exception) {
                    Log.e("LocationManager", "Failed to register GNSS callback", e)
                }
            }

            // Also use Fused Location Provider as backup
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                fusedLocationCallback,
                Looper.getMainLooper()
            )

            isUpdating = true
            Log.d("LocationManager", "Started location updates")
        } catch (e: Exception) {
            Log.e("LocationManager", "Error starting location updates", e)
        }
    }

    fun stopLocationUpdates() {
        if (!isUpdating) return

        try {
            locationManager.removeUpdates(locationListener)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallbackRegistered) {
                try {
                    locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
                    gnssCallbackRegistered = false
                    Log.d("LocationManager", "GNSS status callback unregistered")
                } catch (e: Exception) {
                    Log.e("LocationManager", "Failed to unregister GNSS callback", e)
                }
            }

            fusedLocationClient.removeLocationUpdates(fusedLocationCallback)
            speedFilter.reset()
            isUpdating = false
            Log.d("LocationManager", "Stopped location updates")
        } catch (e: Exception) {
            Log.e("LocationManager", "Error stopping location updates", e)
        }
    }

    fun getFilteredSpeedKmh(): Double {
        return speedFilter.getSpeedKmh(_filteredSpeedFlow.value)
    }
}