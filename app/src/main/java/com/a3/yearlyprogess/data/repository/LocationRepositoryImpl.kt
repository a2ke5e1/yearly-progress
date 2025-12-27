package com.a3.yearlyprogess.data.repository

import com.a3.yearlyprogess.core.location.LocationManager
import com.a3.yearlyprogess.data.local.LocationPreferences
import com.a3.yearlyprogess.domain.model.Location
import com.a3.yearlyprogess.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationManager: LocationManager,
    private val locationPreferences: LocationPreferences
) : LocationRepository {

    override suspend fun getCurrentLocation(): Location? {
        val location = locationManager.getCurrentLocation()
        return location?.copy(isManual = false) // Always mark auto-detected location as non-manual
    }

    override suspend fun saveManualLocation(latitude: Double, longitude: Double) {
        locationPreferences.saveLocation(
            Location(
                latitude = latitude,
                longitude = longitude,
                isManual = true
            )
        )
    }

    override suspend fun saveAutoDetectedLocation(latitude: Double, longitude: Double) {
        locationPreferences.saveLocation(
            Location(
                latitude = latitude,
                longitude = longitude,
                isManual = false
            )
        )
    }

    override fun getSavedLocation(): Flow<Location?> {
        return locationPreferences.savedLocation
    }

    override suspend fun clearSavedLocation() {
        locationPreferences.clearLocation()
    }

    override fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    override fun isLocationEnabled(): Boolean {
        return locationManager.isLocationEnabled()
    }

    override fun wasPermissionAsked(): Flow<Boolean> {
        return locationPreferences.wasPermissionAsked
    }

    override suspend fun setPermissionAsked() {
        locationPreferences.setPermissionAsked()
    }
}