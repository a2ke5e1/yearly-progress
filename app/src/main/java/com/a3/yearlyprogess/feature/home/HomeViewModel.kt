package com.a3.yearlyprogess.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: List<SunriseSunset>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SunriseSunsetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadData(26.16, 72.93, "2025-09-27", "2025-09-29")
    }

    fun loadData(lat: Double, lon: Double, startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.getSunriseSunset(lat, lon, startDate, endDate).collect { result ->
                _uiState.value = when (result) {
                    is Resource.Loading -> HomeUiState.Loading
                    is Resource.Success -> HomeUiState.Success(result.data)
                    is Resource.Error -> HomeUiState.Error(result.message)
                }
            }
        }
    }
}