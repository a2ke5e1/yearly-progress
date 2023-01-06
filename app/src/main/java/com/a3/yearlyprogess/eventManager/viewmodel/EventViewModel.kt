package com.a3.yearlyprogess.eventManager.viewmodel

import android.app.Application
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.data.EventDatabase
import com.a3.yearlyprogess.eventManager.repo.EventRepository
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<Event>>
    private val repository: EventRepository

    init {
        val eventDao = EventDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        readAllData = repository.getAllEvent
    }

    fun addEvent(event: Event){
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

    fun deleteAllEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllEvent()
        }
    }

   /* fun updateProgressBar(event: Event, progressTextView: TextView, progressBar: LinearProgressIndicator) {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val progress = ProgressPercentage.getProgress(
                    ProgressPercentage.CUSTOM_EVENT,
                    event.eventStartTime,
                    event.eventEndTime
                )
                viewModelScope.launch(Dispatchers.Main) {
                    progressTextView.text =
                        ProgressPercentage.formatProgressStyle(progress)
                    progressBar.progress = progress.toInt()
                }
                delay(1000)
            }
        }
    }
*/
}