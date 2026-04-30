package com.hasani.messageapp.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager as AndroidLocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages GPS location acquisition.
 */

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
)

sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: LocationData) : LocationState()
    data class Error(val message: String) : LocationState()
}

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LocationManager"
        private const val MIN_TIME_MS = 1000L // 1 second
        private const val MIN_DISTANCE_M = 1f // 1 meter
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
    
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    /**
     * Check if location permissions are granted.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if GPS is enabled.
     */
    fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get current location (one-shot).
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            _locationState.value = LocationState.Error("دسترسی به موقعیت داده نشده")
            return
        }

        if (!isGpsEnabled()) {
            _locationState.value = LocationState.Error("GPS غیرفعال است")
            return
        }

        _locationState.value = LocationState.Loading

        try {
            // Request fresh location directly to avoid "shaking" (double updates from stale cache then fresh)
            val provider = when {
                locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) -> 
                    AndroidLocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER) -> 
                    AndroidLocationManager.NETWORK_PROVIDER
                else -> {
                    _locationState.value = LocationState.Error("هیچ منبع موقعیتی در دسترس نیست")
                    return
                }
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.d(TAG, "📍 Location received: ${location.latitude}, ${location.longitude}")
                    _locationState.value = LocationState.Success(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy
                        )
                    )
                    locationManager.removeUpdates(this)
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    _locationState.value = LocationState.Error("منبع موقعیت غیرفعال شد")
                }
                
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                listener,
                Looper.getMainLooper()
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting location: ${e.message}")
            _locationState.value = LocationState.Error("خطا در دریافت موقعیت: ${e.message}")
        }
    }

    /**
     * Get last known location from any provider.
     */
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): LocationData? {
        if (!hasLocationPermission()) return null

        val providers = listOf(
            AndroidLocationManager.GPS_PROVIDER,
            AndroidLocationManager.NETWORK_PROVIDER
        )

        for (provider in providers) {
            try {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get last known location from $provider")
            }
        }

        return null
    }

    /**
     * Reset location state.
     */
    fun reset() {
        _locationState.value = LocationState.Idle
    }
}
