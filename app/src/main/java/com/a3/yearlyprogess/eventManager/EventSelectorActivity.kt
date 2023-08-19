package com.a3.yearlyprogess.eventManager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.yearlyprogess.databinding.EventSelectorScreenListEventsBinding
import com.a3.yearlyprogess.eventManager.adapter.EventsListViewAdapter
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel
import com.a3.yearlyprogess.mWidgets.EventWidget

class EventSelectorActivity : AppCompatActivity() {

    private lateinit var binding: EventSelectorScreenListEventsBinding
    private val mEventViewModel: EventViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = EventSelectorScreenListEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarDividerColor =
            ContextCompat.getColor(this, android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top, left = insets.left, right = insets.right)
            WindowInsetsCompat.CONSUMED
        }

        setSupportActionBar(binding.toolbar)

        val appWidgetManager = AppWidgetManager.getInstance(this)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        val event: Event? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable(
                "event",
                Event::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.extras?.getParcelable(
                "event"
            )
        }

        if (event != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val pref = getSharedPreferences("eventWidget_${appWidgetId}", MODE_PRIVATE)
            val edit = pref.edit()

            edit.putInt("eventId", event.id)
            edit.putString("eventTitle", event.eventTitle)
            edit.putBoolean("allDayEvent", event.allDayEvent)
            edit.putString("eventDesc", event.eventDescription)
            edit.putLong("eventStartTimeInMills", event.eventStartTime)
            edit.putLong("eventEndDateTimeInMillis", event.eventEndTime)

            edit.commit()
            EventWidget().updateWidget(this, appWidgetManager, appWidgetId)

            val resultValue =
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }

        val eventAdapter = EventsListViewAdapter(appWidgetId) {
            val resultValue =
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }

        binding.eventsRecyclerViewer.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(this@EventSelectorActivity)
        }
        mEventViewModel.readAllData.observe(this) { events ->
            eventAdapter.setData(events)
        }


    }


}