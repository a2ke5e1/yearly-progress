package com.a3.yearlyprogess.feature.widgets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.LocationRepository
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetPreviewViewModel @Inject constructor(
    private val sunriseSunsetRepository: SunriseSunsetRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _sunsetData = MutableStateFlow<List<SunriseSunset>?>(null)
    val sunsetData = _sunsetData.asStateFlow()

    init {
        fetchSunsetData()
    }

    private fun fetchSunsetData() {
        viewModelScope.launch {
            try {
                val location = locationRepository.getSavedLocation().first()
                if (location != null) {
                    val result = sunriseSunsetRepository.getSunriseSunset(location.latitude, location.longitude).first { 
                        it is Resource.Success || it is Resource.Error 
                    }
                    if (result is Resource.Success) {
                        _sunsetData.value = result.data
                    }
                }
            } catch (e: Exception) {
                // Silently fail for preview
            }
        }
    }
}
