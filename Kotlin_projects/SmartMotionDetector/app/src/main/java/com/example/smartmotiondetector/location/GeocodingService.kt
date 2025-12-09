package com.example.smartmotiondetector.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationDetails(
    val city: String,
    val streetName: String,
    val streetNumber: String
)

class GeocodingService(private val context: Context) {

    suspend fun getLocationDetails(latitude: Double, longitude: Double): LocationDetails {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // ✔️ New non-deprecated API
                    suspendCancellableCoroutine<List<Address>?> { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                cont.resume(addresses)
                            }

                            override fun onError(errorMessage: String?) {
                                Log.e("GeocodingService", "Geocode error: $errorMessage")
                                cont.resume(null)
                            }
                        })
                    }
                } else {
                    // For devices before Android 33
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    LocationDetails(
                        city = address.locality ?: address.subAdminArea ?: "Unknown",
                        streetName = address.thoroughfare ?: "Unknown",
                        streetNumber = address.subThoroughfare ?: "N/A"
                    )
                } else {
                    LocationDetails("Unknown", "Unknown", "N/A")
                }
            } catch (e: Exception) {
                Log.e("GeocodingService", "Error getting location details", e)
                LocationDetails("Unknown", "Unknown", "N/A")
            }
        }
    }
}
