package com.a3.yearlyprogess.widgets.manager.eventManager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.components.dialogbox.PermissionMessageDialog
import com.a3.yearlyprogess.databinding.ActivityImportEventCalendarBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.ImportEventAdapter
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.ImportEventItemDetailsLookup
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.ImportEventItemKeyProvider
import com.a3.yearlyprogess.widgets.manager.eventManager.data.EventDao
import com.a3.yearlyprogess.widgets.manager.eventManager.data.EventDatabase
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImportEventCalendarActivity : AppCompatActivity() {
  private lateinit var binding: ActivityImportEventCalendarBinding
  private var tracker: SelectionTracker<Long>? = null
  private val adapter = ImportEventAdapter(mutableListOf())
  private var selectedDateRange: Pair<Long, Long>? = null
  private lateinit var calendarPermissionDialog: PermissionMessageDialog

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityImportEventCalendarBinding.inflate(layoutInflater)
    setContentView(binding.root)

    calendarPermissionDialog =
        PermissionMessageDialog(
            icon = R.drawable.ic_outline_edit_calendar_24,
            title = getString(R.string.calendar_permission_title),
            message = getString(R.string.calendar_permission_message),
        ) {
          requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }

    binding.toolbar.title = getString(R.string.events_imports)
    setSupportActionBar(binding.toolbar)
    when {
      ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) ==
          PackageManager.PERMISSION_GRANTED -> {
        // You can use the API that requires the permission.
        readEventsFromCalender()
      }

      ActivityCompat.shouldShowRequestPermissionRationale(
          this,
          Manifest.permission.READ_CALENDAR,
      ) -> {
        calendarPermissionDialog.show(supportFragmentManager, "calendar_permission_dialog")
      }

      else -> {
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
        requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
      }
    }
  }

  // Contract for reading events from calender
  private val requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
          // Permission is granted. Continue the action or workflow in your app.
          readEventsFromCalender()
        } else {
          calendarPermissionDialog.show(supportFragmentManager, "calendar_permission_dialog")
        }
      }

  private fun readEventsFromCalender() {
    val eventDao = EventDatabase.getDatabase(this).eventDao()
    val eventList = mutableListOf<Event>()

    lifecycleScope
        .launch(Dispatchers.IO) {
          val uri = CalendarContract.Events.CONTENT_URI
          val projection =
              arrayOf(
                  CalendarContract.Events.TITLE,
                  CalendarContract.Events.DESCRIPTION,
                  CalendarContract.Events.DTSTART,
                  CalendarContract.Events.DTEND,
              )
          val cursor =
              contentResolver.query(
                  uri,
                  projection,
                  null,
                  null,
                  "${CalendarContract.Events.DTSTART} DESC",
              )
          cursor?.use {
            val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
            val descriptionColumn = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val dtStartColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val dtEndColumn = it.getColumnIndex(CalendarContract.Events.DTEND)

            while (it.moveToNext()) {
              try {
                val title = it.getString(titleColumn)
                val description = it.getString(descriptionColumn)
                val dtStart = it.getLong(dtStartColumn)
                val dtEnd = it.getLong(dtEndColumn)

                val event =
                    Event(
                        id = 0,
                        eventTitle = title,
                        eventDescription = description,
                        eventStartTime = Date(dtStart),
                        eventEndTime = Date(dtEnd),
                    )
                eventList.add(event)
              } catch (e: Exception) {
                // Log.d("YearlyProgress.ImportFailed", "${e.printStackTrace()}")
              }
            }
          }
          cursor?.close()
        }
        .invokeOnCompletion { runOnUiThread { setupUI(eventList, eventDao) } }
  }

  private fun setupUI(
      eventList: MutableList<Event>,
      eventDao: EventDao,
  ) {
    binding.progressBar.visibility = View.GONE
    adapter.setEvents(eventList)
    binding.importedEventCalendarRecyclerView.adapter = adapter
    binding.importedEventCalendarRecyclerView.layoutManager = LinearLayoutManager(this)
    tracker =
        SelectionTracker.Builder<Long>(
                "imported_event",
                binding.importedEventCalendarRecyclerView,
                ImportEventItemKeyProvider(binding.importedEventCalendarRecyclerView),
                ImportEventItemDetailsLookup(binding.importedEventCalendarRecyclerView),
                StorageStrategy.createLongStorage(),
            )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
    tracker?.let { adapter.tracker = it }

    tracker?.addObserver(
        object : SelectionTracker.SelectionObserver<Long>() {
          override fun onSelectionChanged() {
            super.onSelectionChanged()
            // Log.d("TAG", "onCreateView: ${adapter.getSelectedEvents()}")
          }
        },
    )

    binding.toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.select_events -> {
          adapter.toggleSelectAll()
          true
        }
        R.id.search_events -> {
          if (binding.searchViewContainer.visibility == View.VISIBLE) {
            binding.searchViewContainer
                .animate()
                .alpha(0f)
                .withEndAction { binding.searchViewContainer.visibility = View.GONE }
                .setDuration(150)
          } else {
            binding.searchViewContainer
                .animate()
                .alpha(1f)
                .withStartAction {
                  binding.searchViewContainer.visibility = View.VISIBLE
                  binding.searchViewEditText.requestFocus()
                }
                .setDuration(150)
          }
          // Log.d("TAG", "onOptionsItemSelected: Search")
          true
        }

        else -> false
      }
    }

    binding.importedEventCalendarRecyclerView.addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
          override fun onScrolled(
              recyclerView: RecyclerView,
              dx: Int,
              dy: Int,
          ) {
            if (dy > 0 && binding.importEventsFab.visibility == View.VISIBLE) {
              binding.importEventsFab.hide()
            } else if (dy < 0 && binding.importEventsFab.visibility != View.VISIBLE) {
              binding.importEventsFab.show()
            }
          }
        },
    )

    binding.importEventsFab.setOnClickListener {
      lifecycleScope
          .launch(Dispatchers.IO) {
            val selectedEvents = adapter.getSelectedEvents()
            eventDao.insertAllEvents(selectedEvents)
          }
          .invokeOnCompletion { finish() }
    }

    binding.searchViewEditText.doAfterTextChanged { text ->
      if (text.toString().isNotEmpty()) {
        adapter.filter(text.toString(), selectedDateRange)
      }
    }

    binding.searchViewEditText.setOnLongClickListener {
      binding.searchViewEditText.text?.clear()
      adapter.resetFilter()
      true
    }

    binding.eventFilterRange.setOnClickListener {
      val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()
      datePicker.show(supportFragmentManager, "date_range_picker")
      datePicker.addOnPositiveButtonClickListener {
        val selectedRange = datePicker.selection
        selectedRange?.let {
          adapter.filterByDateRange(it.first, it.second)
          selectedDateRange = it
        }
        binding.eventFilterRange.text =
            "${DateFormat.format("MMM dd, yyyy ", it.first)} \u2014" +
                " ${DateFormat.format("MMM dd, yyyy", it.second)}"
      }
    }

    binding.eventFilterClear.setOnClickListener {
      adapter.resetFilter()
      selectedDateRange = null
      binding.eventFilterRange.text = getString(R.string.all)
      binding.searchViewEditText.text?.clear()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_import, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        finish()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}
