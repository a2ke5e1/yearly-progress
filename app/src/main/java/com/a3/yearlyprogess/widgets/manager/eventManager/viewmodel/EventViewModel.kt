package com.a3.yearlyprogess.widgets.manager.eventManager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.widgets.manager.eventManager.data.EventDatabase
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.manager.eventManager.repo.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {

  val readAllData: LiveData<List<Event>>
  private val repository: EventRepository

  init {
    val eventDao = EventDatabase.getDatabase(application).eventDao()
    repository = EventRepository(eventDao)
    readAllData = repository.getAllEvent
  }

  fun addEvent(event: Event) {
    viewModelScope.launch(Dispatchers.IO) { repository.addEvent(event) }
  }

  fun updateEvent(event: Event) {
    viewModelScope.launch(Dispatchers.IO) { repository.updateEvent(event) }
  }

  fun deleteEvent(event: Event) {
    viewModelScope.launch(Dispatchers.IO) { repository.deleteEvent(event) }
  }

  fun deleteAllEvent() {
    viewModelScope.launch(Dispatchers.IO) { repository.deleteAllEvent() }
  }

  /*fun filterEvent(query: String){
      viewModelScope.launch(Dispatchers.IO) {
          _readAllData.postValue(repository.filterEvent(query))
      }
  }

  fun resetFilter(){
      viewModelScope.launch(Dispatchers.IO) {
          _readAllData.postValue(repository.getAllEvent.value)
      }
  }*/

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
