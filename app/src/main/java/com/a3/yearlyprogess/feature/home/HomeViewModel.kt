package com.a3.yearlyprogess.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    private var lastLoadedDate: LocalDate? = null

    init {
        loadData(26.16, 72.93)
        observeDateChange(26.16, 72.93)
    }

    fun loadData(lat: Double, lon: Double) {
        lastLoadedDate = LocalDate.now()
        viewModelScope.launch {
            repository.getSunriseSunset(lat, lon).collect { result ->
                _uiState.value = when (result) {
                    is Resource.Loading -> HomeUiState.Loading
                    is Resource.Success -> HomeUiState.Success(result.data)
                    is Resource.Error -> HomeUiState.Error(result.message)
                }
            }
        }
    }

    private fun observeDateChange(lat: Double, lon: Double) {
        viewModelScope.launch {
            while (true) {
                delay(1_000L) // check every 1 secs
                val today = LocalDate.now()
                if (today != lastLoadedDate) {
                    repository.getSunriseSunset(lat, lon).collect { result ->
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
