package com.a3.yearlyprogess.domain.repository

import com.a3.yearlyprogess.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
    suspend fun saveManualLocation(latitude: Double, longitude: Double)
    suspend fun saveAutoDetectedLocation(latitude: Double, longitude: Double)
    fun getSavedLocation(): Flow<Location?>
    suspend fun clearSavedLocation()
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    fun wasPermissionAsked(): Flow<Boolean>
    suspend fun setPermissionAsked()
}