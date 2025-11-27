package com.a3.yearlyprogess.feature.settings.ui

import android.content.Context
import android.icu.util.ULocale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.ui.components.SelectableItem
import com.a3.yearlyprogess.core.util.CalculationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = appSettingsRepository.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

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
    
}
