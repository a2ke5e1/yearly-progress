package com.a3.yearlyprogess.feature.settings.ui.domain.model

import com.a3.yearlyprogess.domain.model.Location

/**
 * Represents the current screen/dialog state for location settings.
 * Replaces multiple boolean flags with a single source of truth.
 */
sealed interface LocationScreen {
    data object Idle : LocationScreen
    data object RequestingPermission : LocationScreen
    data object DetectingLocation : LocationScreen
    data object ManualCityPicker : LocationScreen
    data object ManualCoordinates : LocationScreen
    data class Error(val message: String) : LocationScreen
}

/**
 * Represents permission-related state.
 * Groups all permission concerns in one place.
 */
data class PermissionState(
    val isGranted: Boolean = false,
    val isLocationServiceEnabled: Boolean = false,
    val requestedByUser: Boolean = false,
    val shouldShowRationale: Boolean = true
)

/**
 * Represents location detection mode.
 */
enum class LocationMode {
    Automatic,
    Manual
}

/**
 * Holds temporary input data for manual location entry.
 * Separates input concerns from main state.
 */
data class ManualLocationInput(
    val searchQuery: String = "",
    val latitude: String = "",
    val longitude: String = ""
) {
    /**
     * Validates coordinate inputs.
     */
    fun isValidCoordinates(): Boolean {
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        return lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0
    }

    /**
     * Clears all input fields.
     */
    fun clear() = ManualLocationInput()
}

/**
 * Main UI state for location settings.
 * Reduced from 14 properties to 6 grouped properties.
 */
data class LocationUiState(
    val permission: PermissionState = PermissionState(),
    val mode: LocationMode = LocationMode.Automatic,
    val screen: LocationScreen = LocationScreen.Idle,
    val input: ManualLocationInput = ManualLocationInput(),
    val detectedLocation: Location? = null,
    val isLoadingCities: Boolean = false
)