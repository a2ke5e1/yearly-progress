package com.a3.yearlyprogess.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.IConsentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    val consentManager: IConsentManager
) : ViewModel() {

    val appSettings: StateFlow<AppSettings?> = appSettingsRepository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _consentGathered = MutableStateFlow(false)
    val consentGathered = _consentGathered.asStateFlow()

    fun onWelcomeCompleted() {
        viewModelScope.launch {
            appSettingsRepository.setFirstLaunch(false)
        }
    }

    fun setConsentGathered(gathered: Boolean) {
        _consentGathered.value = gathered
    }
}
