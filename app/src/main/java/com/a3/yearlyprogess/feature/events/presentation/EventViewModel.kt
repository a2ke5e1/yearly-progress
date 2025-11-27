package com.a3.yearlyprogess.feature.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.events.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    // Similar pattern to your settings
    val events: StateFlow<List<Event>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<AppSettings> = appSettingsRepository.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _isAllSelected = MutableStateFlow(false)

    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val isAllSelected: StateFlow<Boolean> = _isAllSelected.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun toggleSelection(id: Int) {
        _selectedIds.value =
            if (_selectedIds.value.contains(id))
                _selectedIds.value - id
            else
                _selectedIds.value + id
    }

    fun toggleAllSelections() {
        val allIds = events.value.map { it.id }.toSet()

        if (_selectedIds.value.size == allIds.size && allIds.isNotEmpty()) {
            // All are selected → Deselect all
            _selectedIds.value = emptySet()
            _isAllSelected.value = false
        } else {
            // Not all selected → Select all
            _selectedIds.value = allIds
            _isAllSelected.value = true
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
        _isAllSelected.value = false
    }

    fun deleteSelectedEvents() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            ids.forEach { id ->
                val event = repository.getEvent(id)
                if (event != null) {
                    repository.deleteEvent(event)
                }
            }
        }

        _selectedIds.value = emptySet()
    }

    fun addEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEvent(event)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
        }
    }

    fun deleteAllEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllEvents()
        }
    }

    suspend fun getEvent(id: Int): Event? {
        return repository.getEvent(id)
    }

}