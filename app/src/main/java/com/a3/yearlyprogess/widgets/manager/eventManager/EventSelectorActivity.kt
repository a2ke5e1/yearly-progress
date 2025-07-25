package com.a3.yearlyprogess.widgets.manager.eventManager

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.EventSelectorScreenListEventsBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.EventsListViewAdapter
import com.a3.yearlyprogess.widgets.manager.eventManager.viewmodel.EventViewModel
import com.a3.yearlyprogess.widgets.ui.EventWidget
import com.google.android.material.color.DynamicColors

class EventSelectorActivity : AppCompatActivity() {
  private lateinit var binding: EventSelectorScreenListEventsBinding
  private val mEventViewModel: EventViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    DynamicColors.applyToActivityIfAvailable(this)
    binding = EventSelectorScreenListEventsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, windowInsets ->
      val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(top = insets.top, left = insets.left, right = insets.right)
      WindowInsetsCompat.CONSUMED
    }

    setSupportActionBar(binding.toolbar)

    val appWidgetManager = AppWidgetManager.getInstance(this)

    val appWidgetId =
        intent
            ?.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    setResult(RESULT_CANCELED, resultValue)

    val eventId = intent?.getIntExtra("eventId", -1)
    if (eventId != -1 && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
      val pref = getSharedPreferences("eventWidget_$appWidgetId", MODE_PRIVATE)
      val edit = pref.edit()

      edit.putInt("eventId", eventId!!)

      edit.commit()
      EventWidget().updateWidget(this, appWidgetManager, appWidgetId)

      val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(RESULT_OK, resultValue)
      finish()
    }

    val eventAdapter =
        EventsListViewAdapter(appWidgetId) {
          val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
          setResult(RESULT_OK, resultValue)
          finish()
        }

    eventAdapter.setSelectedEvent(
        this.getSharedPreferences("eventWidget_$appWidgetId", Context.MODE_PRIVATE)
            .getInt("eventId", -1))
    binding.eventsRecyclerViewer.apply {
      adapter = eventAdapter
      layoutManager = LinearLayoutManager(this@EventSelectorActivity)
    }
    mEventViewModel.readAllData.observe(this) { events ->
      eventAdapter.setData(events)
      if (events.isEmpty()) {
        binding.noEvents.visibility = View.VISIBLE
      } else {
        binding.noEvents.visibility = View.GONE
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.choose_event_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(
      item: MenuItem
  ): Boolean { // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.customize_event_widget -> {

        val appWidgetId =
            intent
                ?.extras
                ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        startActivity(
            Intent(this, EventWidgetConfigManager::class.java).apply {
              putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            })

        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}
