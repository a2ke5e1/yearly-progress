package com.a3.yearlyprogess.feature.settings.ui

import android.content.Context
import android.icu.util.ULocale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.domain.model.City
import com.a3.yearlyprogess.domain.model.Location
import com.a3.yearlyprogess.domain.repository.LocationRepository
import com.a3.yearlyprogess.feature.settings.ui.domain.model.LocationMode
import com.a3.yearlyprogess.feature.settings.ui.domain.model.LocationScreen
import com.a3.yearlyprogess.feature.settings.ui.domain.model.LocationUiState
import com.a3.yearlyprogess.feature.settings.util.CityDataParser
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val locationRepository: LocationRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<AppSettings> = appSettingsRepository.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val savedLocation: StateFlow<Location?> = locationRepository.getSavedLocation()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    private val _locationUiState = MutableStateFlow(LocationUiState())
    val locationUiState: StateFlow<LocationUiState> = _locationUiState.asStateFlow()

    init {
        loadCities()
        refreshPermissionState()
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setFirstLaunch(isFirstLaunch)
        }
    }

    fun setLocale(locale: ULocale) {
        viewModelScope.launch {
            appSettingsRepository.setLocale(locale)
        }
    }

    fun setCalculationType(calculationType: CalculationType) {
        viewModelScope.launch {
            appSettingsRepository.setCalculationType(calculationType)
        }
    }

    fun setWeekStartDay(weekStartDay: Int) {
        viewModelScope.launch {
            appSettingsRepository.setWeekStartDay(weekStartDay)
        }
    }

    fun setDecimalDigits(decimalDigits: Int) {
        viewModelScope.launch {
            appSettingsRepository.setDecimalDigits(decimalDigits)
        }
    }

    fun setAppTheme(theme: WidgetTheme) {
        viewModelScope.launch {
            appSettingsRepository.setAppTheme(theme)
        }
    }

    fun setEventProgressDecimalDigits(decimalDigits: Int) {
        viewModelScope.launch {
            appSettingsRepository.setEventDecimalDigits(decimalDigits)
        }
    }

    // ============================================================
    // Notification Settings
    // ============================================================

    fun setProgressShowNotification(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setProgressShowNotification(enabled)
        }
    }

    fun setProgressShowNotificationYear(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setProgressShowNotificationYear(enabled)
        }
    }

    fun setProgressShowNotificationMonth(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setProgressShowNotificationMonth(enabled)
        }
    }

    fun setProgressShowNotificationWeek(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setProgressShowNotificationWeek(enabled)
        }
    }

    fun setProgressShowNotificationDay(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setProgressShowNotificationDay(enabled)
        }
    }

    // ============================================================
    // Location Mode Management
    // ============================================================

    /**
     * Enables/disables automatic location detection.
     * Handles permission checks internally.
     */
    fun setAutomaticDetection(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !locationRepository.hasLocationPermission()) {
                // Mark that user requested permission
                updateState {
                    it.copy(
                        permission = it.permission.copy(requestedByUser = true),
                        screen = LocationScreen.RequestingPermission
                    )
                }
                return@launch
            }

            appSettingsRepository.setAutomaticallyDetectLocation(enabled)
            updateState {
                it.copy(mode = if (enabled) LocationMode.Automatic else LocationMode.Manual)
            }
        }
    }

    // ============================================================
    // Screen Navigation
    // ============================================================

    /**
     * Opens the manual city picker dialog.
     */
    fun openManualCityPicker() {
        updateScreen(LocationScreen.ManualCityPicker)
    }

    /**
     * Opens the manual coordinates input dialog.
     */
    fun openManualCoordinates() {
        updateScreen(LocationScreen.ManualCoordinates)
    }

    /**
     * Closes any open dialog and returns to idle state.
     */
    fun dismissDialog() {
        updateState {
            it.copy(
                screen = LocationScreen.Idle,
                input = it.input.clear()
            )
        }
    }

    /**
     * Clears any error state.
     */
    fun clearError() {
        if (_locationUiState.value.screen is LocationScreen.Error) {
            updateScreen(LocationScreen.Idle)
        }
    }

    // ============================================================
    // Manual Input Management
    // ============================================================

    /**
     * Updates manual location input fields.
     * Uses named parameters to update only specific fields.
     */
    fun updateManualInput(
        latitude: String? = null,
        longitude: String? = null,
        searchQuery: String? = null
    ) {
        updateState {
            it.copy(
                input = it.input.copy(
                    latitude = latitude ?: it.input.latitude,
                    longitude = longitude ?: it.input.longitude,
                    searchQuery = searchQuery ?: it.input.searchQuery
                )
            )
        }
    }

    // ============================================================
    // Location Detection
    // ============================================================

    /**
     * Detects current location using GPS.
     * Handles permission checks and error states.
     */
    fun detectCurrentLocation() {
        if (!locationRepository.hasLocationPermission()) {
            updateScreen(LocationScreen.Error("Location permission not granted"))
            return
        }

        viewModelScope.launch {
            updateScreen(LocationScreen.DetectingLocation)

            runCatching {
                locationRepository.getCurrentLocation()
            }.onSuccess { location ->
                if (location != null) {
                    locationRepository.saveAutoDetectedLocation(
                        location.latitude,
                        location.longitude
                    )
                    updateState {
                        it.copy(
                            detectedLocation = location,
                            screen = LocationScreen.Idle
                        )
                    }
                } else {
                    updateScreen(
                        LocationScreen.Error(
                            "Unable to detect location. Please check if location services are enabled."
                        )
                    )
                }
            }.onFailure { error ->
                updateScreen(
                    LocationScreen.Error("Error detecting location: ${error.message}")
                )
            }
        }
    }

    /**
     * Saves manually entered coordinates.
     * Validates input before saving.
     */
    fun saveManualCoordinates() {
        val input = _locationUiState.value.input

        if (!input.isValidCoordinates()) {
            updateScreen(
                LocationScreen.Error(
                    "Invalid coordinates. Latitude must be between -90 and 90, " +
                            "longitude between -180 and 180."
                )
            )
            return
        }

        saveLocation(
            input.latitude.toDouble(),
            input.longitude.toDouble()
        )
    }

    /**
     * Saves a city location.
     */
    fun saveCityLocation(city: City) {
        saveLocation(city.latitude, city.longitude)
    }

    /**
     * Internal method to save location and update state.
     */
    private fun saveLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            runCatching {
                locationRepository.saveManualLocation(latitude, longitude)
            }.onSuccess {
                updateState {
                    it.copy(
                        screen = LocationScreen.Idle,
                        input = it.input.clear()
                    )
                }
            }.onFailure { error ->
                updateScreen(
                    LocationScreen.Error("Error saving location: ${error.message}")
                )
            }
        }
    }

    /**
     * Clears saved location data.
     */
    fun clearLocation() {
        viewModelScope.launch {
            locationRepository.clearSavedLocation()
            updateState {
                it.copy(detectedLocation = null)
            }
        }
    }


    /**
     * Refreshes the current permission state.
     * Should be called when permissions might have changed.
     */
    fun refreshPermissionState() {
        updateState {
            it.copy(
                permission = it.permission.copy(
                    isGranted = locationRepository.hasLocationPermission(),
                    isLocationServiceEnabled = locationRepository.isLocationEnabled()
                )
            )
        }
    }

    /**
     * Marks permission as requested by user.
     * Used to track user-initiated permission requests.
     */
    fun setPermissionRequestedByUser(requested: Boolean) {
        updateState {
            it.copy(
                permission = it.permission.copy(requestedByUser = requested)
            )
        }
    }

    /**
     * Updates whether rationale should be shown.
     */
    fun setShowPermissionRationale(show: Boolean) {
        updateState {
            it.copy(
                permission = it.permission.copy(shouldShowRationale = show)
            )
        }
    }

    /**
     * Marks permission as asked in repository.
     */
    fun setPermissionAsked() {
        viewModelScope.launch {
            locationRepository.setPermissionAsked()
        }
    }


    /**
     * Searches cities by query string.
     * Returns up to 100 matching cities.
     */
    fun searchCities(query: String): List<City> {
        if (query.isBlank()) return emptyList()

        val searchTerm = query.lowercase()
        return _cities.value
            .filter { city -> city.searchableText.contains(searchTerm) }
            .take(100)
    }

    /**
     * Loads city data from assets.
     */
    private fun loadCities() {
        viewModelScope.launch {
            updateState { it.copy(isLoadingCities = true) }

            runCatching {
                CityDataParser.loadCitiesFromAssets(context)
            }.onSuccess { loadedCities ->
                _cities.value = loadedCities
            }.onFailure { error ->
                updateScreen(
                    LocationScreen.Error("Failed to load city data: ${error.message}")
                )
            }

            updateState { it.copy(isLoadingCities = false) }
        }
    }

    // ============================================================
    // Internal State Management Helpers
    // ============================================================

    /**
     * Centralized state update function.
     * Reduces boilerplate in update logic.
     */
    private inline fun updateState(block: (LocationUiState) -> LocationUiState) {
        _locationUiState.update(block)
    }

    /**
     * Updates only the screen state.
     */
    private fun updateScreen(screen: LocationScreen) {
        updateState { it.copy(screen = screen) }
    }
}
