package com.a3.yearlyprogess.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.domain.model.Location
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.LocationRepository
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: List<SunriseSunset>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class LocationState {
    object Unknown : LocationState()
    object PermissionRequired : LocationState()
    object LocationDisabled : LocationState()
    data class Available(val location: Location) : LocationState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sunriseSunsetRepository: SunriseSunsetRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Unknown)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private var lastLoadedDate: LocalDate? = null

    init {
        initializeLocation()
    }

    private fun initializeLocation() {
        viewModelScope.launch {
            // First, check if we have a saved location
            locationRepository.getSavedLocation().collect { savedLocation ->
                if (savedLocation != null) {
                    _currentLocation.value = savedLocation
                    _locationState.value = LocationState.Available(savedLocation)
                    loadData(savedLocation.latitude, savedLocation.longitude)
                    observeDateChange()
                } else {
                    // No saved location, check permissions
                    checkLocationPermissionAndFetch()
                }
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        viewModelScope.launch {
            when {
                !locationRepository.hasLocationPermission() -> {
                    _locationState.value = LocationState.PermissionRequired
                }
                !locationRepository.isLocationEnabled() -> {
                    _locationState.value = LocationState.LocationDisabled
                }
                else -> {
                    fetchCurrentLocation()
                }
            }
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            if (!locationRepository.isLocationEnabled()) {
                _locationState.value = LocationState.LocationDisabled
            } else {
                fetchCurrentLocation()
            }
        }
    }

    fun onPermissionDenied() {
        // User can still manually add location later
        _locationState.value = LocationState.PermissionRequired
        _uiState.value = HomeUiState.Error("Location permission denied. Please add location manually in settings.")
    }

    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()
            if (location != null) {
                _currentLocation.value = location
                _locationState.value = LocationState.Available(location)
                // Save the fetched location
                locationRepository.saveManualLocation(location.latitude, location.longitude)
                loadData(location.latitude, location.longitude)
                observeDateChange()
            } else {
                _locationState.value = LocationState.PermissionRequired
                _uiState.value = HomeUiState.Error("Unable to fetch location. Please add manually in settings.")
            }
        }
    }

    fun saveManualLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            locationRepository.saveManualLocation(latitude, longitude)
            val location = Location(latitude, longitude, isManual = true)
            _currentLocation.value = location
            _locationState.value = LocationState.Available(location)
            loadData(latitude, longitude)
        }
    }

    private fun loadData(lat: Double, lon: Double) {
        lastLoadedDate = LocalDate.now()
        viewModelScope.launch {
            sunriseSunsetRepository.getSunriseSunset(lat, lon).collect { result ->
                _uiState.value = when (result) {
                    is Resource.Loading -> HomeUiState.Loading
                    is Resource.Success -> HomeUiState.Success(result.data)
                    is Resource.Error -> HomeUiState.Error(result.message)
                }
            }
        }
    }

    private fun observeDateChange() {
        val location = _currentLocation.value ?: return

        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val today = LocalDate.now()
                if (today != lastLoadedDate) {
                    lastLoadedDate = today
                    sunriseSunsetRepository.getSunriseSunset(location.latitude, location.longitude).collect { result ->
                        when (result) {
                            is Resource.Success -> _uiState.value = HomeUiState.Success(result.data)
                            else -> Log.w("getSunriseSunset", "Failed to load sunrise/sunset data")
                        }
                    }
                }
            }
        }
    }
}