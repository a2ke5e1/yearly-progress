package com.a3.yearlyprogess.eventManager

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.yearlyprogess.databinding.ActivityImportEventCalendarBinding
import com.a3.yearlyprogess.eventManager.adapter.ImportEventAdapter
import com.a3.yearlyprogess.eventManager.adapter.ImportEventItemDetailsLookup
import com.a3.yearlyprogess.eventManager.adapter.ImportEventItemKeyProvider
import com.a3.yearlyprogess.eventManager.adapter.MyItemDetailsLookup
import com.a3.yearlyprogess.eventManager.data.EventDatabase
import com.a3.yearlyprogess.eventManager.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImportEventCalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportEventCalendarBinding
    private var tracker: SelectionTracker<Long>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportEventCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)



        when {
            ContextCompat.checkSelfPermission(
               this,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                readEventsFromCalender()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_CALENDAR) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                // showInContextUI(...)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CALENDAR)
            }
        }
    }


    // Contract for reading events from calender
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            readEventsFromCalender()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their decision.
        }
    }

    private fun readEventsFromCalender() {
        val eventDao = EventDatabase.getDatabase(this).eventDao()
        val eventList = mutableListOf<Event>()






        // TODO: Let user select which events to import
        lifecycleScope.launch(Dispatchers.IO) {

            val uri = CalendarContract.Events.CONTENT_URI
            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )

            val cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descriptionColumn = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val dtStartColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val dtEndColumn = it.getColumnIndex(CalendarContract.Events.DTEND)

                while (it.moveToNext()) {
                    val title = it.getString(titleColumn)
                    val description = it.getString(descriptionColumn)
                    val dtStart = it.getLong(dtStartColumn)
                    val dtEnd = it.getLong(dtEndColumn)

                    val event = Event(
                        id = 0,
                        eventTitle = title,
                        eventDescription = description,
                        eventStartTime = dtStart,
                        eventEndTime = dtEnd
                    )
                    eventList.add(event)
                }
            }

            cursor?.close()
            // eventDao.insertAllEvents(eventList)
        }


        val adapter = ImportEventAdapter(eventList)
        binding.importedEventCalendarRecyclerView.adapter = adapter
        binding.importedEventCalendarRecyclerView.layoutManager = LinearLayoutManager(this)
        tracker = SelectionTracker.Builder<Long>(
            "imported_event",
            binding.importedEventCalendarRecyclerView,
            ImportEventItemKeyProvider(binding.importedEventCalendarRecyclerView),
            ImportEventItemDetailsLookup(binding.importedEventCalendarRecyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        tracker?.let {
            adapter.tracker = it
        }

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    Log.d("TAG", "onCreateView: ${adapter.getSelectedEvents()}")
                }
            }
        )

        binding.testBtn.setOnClickListener {
            adapter.selectAll()
        }




    }
}