package com.a3.yearlyprogess.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager as AndroidLocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.domain.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager

    fun hasLocationPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("LocationManager", "Has location permission: $hasPermission")
        return hasPermission
    }

    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            Log.e("LocationManager", "No location permission")
            return@withContext null
        }

        if (!isLocationEnabled()) {
            Log.e("LocationManager", "Location services disabled")
            return@withContext null
        }

        try {
            // First, try to get last known location
            val lastKnown = getLastKnownLocation()
            if (lastKnown != null) {
                Log.d("LocationManager", "Got last known location: ${lastKnown.latitude}, ${lastKnown.longitude}")
                return@withContext lastKnown
            }

            Log.d("LocationManager", "No last known location, requesting fresh location...")

            // If no last known location, request a fresh one
            val freshLocation = requestFreshLocation()
            if (freshLocation != null) {
                Log.d("LocationManager", "Got fresh location: ${freshLocation.latitude}, ${freshLocation.longitude}")
                return@withContext freshLocation
            }

            Log.e("LocationManager", "Failed to get any location")
            return@withContext null

        } catch (e: Exception) {
            Log.e("LocationManager", "Exception getting location: ${e.message}", e)
            null
        }
    }

    private suspend fun getLastKnownLocation(): Location? = withContext(Dispatchers.IO) {
        try {
            // Try providers in order of preference for coarse location
            val providers = listOf(
                AndroidLocationManager.NETWORK_PROVIDER,
                AndroidLocationManager.GPS_PROVIDER,
                AndroidLocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: android.location.Location? = null

            for (provider in providers) {
                try {
                    if (!locationManager.isProviderEnabled(provider)) {
                        Log.d("LocationManager", "Provider $provider is disabled")
                        continue
                    }

                    val lastKnown = locationManager.getLastKnownLocation(provider)
                    Log.d("LocationManager", "Provider $provider last known: $lastKnown")

                    if (lastKnown != null && (bestLocation == null || lastKnown.time > bestLocation.time)) {
                        bestLocation = lastKnown
                    }
                } catch (e: SecurityException) {
                    Log.e("LocationManager", "SecurityException for provider $provider", e)
                } catch (e: Exception) {
                    Log.e("LocationManager", "Exception for provider $provider: ${e.message}", e)
                }
            }

            bestLocation?.let {
                Location(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    isManual = false
                )
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "Error in getLastKnownLocation: ${e.message}", e)
            null
        }
    }

    private suspend fun requestFreshLocation(): Location? = withTimeoutOrNull(30_000L) {
        suspendCancellableCoroutine { continuation ->
            try {
                val provider = when {
                    locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER) -> {
                        AndroidLocationManager.NETWORK_PROVIDER
                    }
                    locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) -> {
                        AndroidLocationManager.GPS_PROVIDER
                    }
                    else -> {
                        Log.e("LocationManager", "No enabled location providers")
                        continuation.resume(null)
                        return@suspendCancellableCoroutine
                    }
                }

                Log.d("LocationManager", "Requesting location update from provider: $provider")

                val locationListener = LocationListenerCompat { location ->
                    Log.d("LocationManager", "Received location update: ${location.latitude}, ${location.longitude}")
                    continuation.resume(
                        Location(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            isManual = false
                        )
                    )
                }

                continuation.invokeOnCancellation {
                    Log.d("LocationManager", "Location request cancelled")
                    locationManager.removeUpdates(locationListener)
                }

                val locationRequest = LocationRequestCompat.Builder(0L)
                    .setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                    .setMaxUpdates(1)
                    .build()

                LocationManagerCompat.requestLocationUpdates(
                    locationManager,
                    provider,
                    locationRequest,
                    locationListener,
                    Looper.getMainLooper()
                )

            } catch (e: SecurityException) {
                Log.e("LocationManager", "SecurityException requesting location", e)
                continuation.resume(null)
            } catch (e: Exception) {
                Log.e("LocationManager", "Exception requesting location: ${e.message}", e)
                continuation.resume(null)
            }
        }
    }

    fun isLocationEnabled(): Boolean {
        val isEnabled = locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)
        Log.d("LocationManager", "Location enabled: $isEnabled")
        return isEnabled
    }
}