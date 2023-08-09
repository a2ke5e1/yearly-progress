package com.a3.yearlyprogess.eventManager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.ActivityEventManagerActivityBinding
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel
import com.a3.yearlyprogess.mWidgets.EventWidget
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class EventManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventManagerActivityBinding
    private val mEventViewModel: EventViewModel by viewModels()

    private var eventStartDateTimeInMillis: Long = -1
    private var eventStartHour: Int = 0
    private var eventStartMinute: Int = 0


    private var eventEndDateTimeInMillis: Long = -1
    private var eventEndHour: Int = 0
    private var eventEndMinute: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventManagerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarDividerColor =
            ContextCompat.getColor(this, android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            WindowInsetsCompat.CONSUMED
        }

        val event: Event? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("event", Event::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("event")
            }
        val isAddMode = intent.getBooleanExtra("addMode", true)


        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val datePicker = MaterialDatePicker.Builder.datePicker()
        val isSystem24Hour = is24HourFormat(this)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(clockFormat)


        if (event != null && !isAddMode) {
            loadWidgetDataFromEvent(event)
        }

        binding.eventTitle.requestFocus()
        val inputMethodManager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.eventTitle, InputMethodManager.SHOW_IMPLICIT)

        binding.eventTitle.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.eventDesc.apply {
                    requestFocus()
                    setSelection(this.text.toString().length)
                }
                true
            } else {
                false
            }
        }

        binding.editTextStartDate.setOnClickListener {
            datePicker.setTitleText("Select Event Start Date")
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {


                        eventStartDateTimeInMillis = it.toLong()
                        eventStartDateTimeInMillis = modifiedEventDateTime(
                            eventStartDateTimeInMillis,
                            eventEndHour,
                            eventEndMinute
                        )

                        binding.editTextStartDate.setText(
                            SimpleDateFormat.getDateInstance().format(eventStartDateTimeInMillis)
                                .toString()
                        )
                    }
                }


        }
        binding.editTextStartTime.setOnClickListener {
            timePicker.setTitleText("Select Event Start Time")
                .setHour(eventStartHour)
                .setMinute(eventStartMinute)
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventStartHour = hour
                        eventStartMinute = minute

                        eventStartDateTimeInMillis = modifiedEventDateTime(
                            eventStartDateTimeInMillis,
                            eventStartHour,
                            eventStartMinute
                        )

                        binding.editTextStartTime.setText(
                            getHourMinuteLocal(
                                eventStartDateTimeInMillis
                            )
                        )

                        binding.editTextStartDate.setText(

                            SimpleDateFormat.getDateInstance().format(eventStartDateTimeInMillis)
                                .toString()
                        )

                    }
                }

        }
        binding.editTextEndDate.setOnClickListener {
            datePicker.setTitleText("Select Event End Date")
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {


                        eventEndDateTimeInMillis = it.toLong()
                        eventEndDateTimeInMillis = modifiedEventDateTime(
                            eventEndDateTimeInMillis,
                            eventEndHour,
                            eventEndMinute
                        )

                        binding.editTextEndDate.setText(
                            SimpleDateFormat.getDateInstance().format(eventEndDateTimeInMillis)
                                .toString()
                        )
                    }
                }


        }
        binding.editTextEndTime.setOnClickListener {
            timePicker.setTitleText("Select Event End Time")
                .setHour(eventEndHour)
                .setMinute(eventEndMinute)
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventEndHour = hour
                        eventEndMinute = minute

                        eventEndDateTimeInMillis = modifiedEventDateTime(
                            eventEndDateTimeInMillis,
                            eventEndHour,
                            eventEndMinute
                        )

                        binding.editTextEndTime.setText(getHourMinuteLocal(eventEndDateTimeInMillis))
                        binding.editTextEndDate.setText(
                            SimpleDateFormat.getDateInstance().format(eventEndDateTimeInMillis)
                                .toString()
                        )

                    }
                }

        }


        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.materialToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save_config -> {

                    if (!isAddMode) {

                        val updatedEvent = Event(
                            event!!.id,
                            binding.eventTitle.text.toString().ifEmpty { "" },
                            binding.eventDesc.text.toString().ifEmpty { "" },
                            eventStartDateTimeInMillis,
                            eventEndDateTimeInMillis
                        )
                        mEventViewModel.updateEvent(updatedEvent)

                        val appWidgetManager = AppWidgetManager.getInstance(this)
                        val appWidgetIds = appWidgetManager.getAppWidgetIds(
                            ComponentName(
                                this,
                                EventWidget::class.java
                            )
                        )

                        appWidgetIds.forEach { appWidgetId ->
                            val pref = getSharedPreferences("eventWidget_${appWidgetId}", Context.MODE_PRIVATE)
                            val prefEventId = pref.getInt("eventId", -1)
                            if (prefEventId == updatedEvent.id) {
                                val edit = pref.edit()

                                edit.putInt("eventId", updatedEvent.id)
                                edit.putString("eventTitle", updatedEvent.eventTitle)
                                edit.putString("eventDesc", updatedEvent.eventDescription)
                                edit.putLong("eventStartTimeInMills", updatedEvent.eventStartTime)
                                edit.putLong("eventEndDateTimeInMillis", updatedEvent.eventEndTime)

                                edit.commit()
                                EventWidget().updateWidget(this, appWidgetManager, appWidgetId)
                            }
                        }



                    } else {
                        mEventViewModel.addEvent(
                            Event(
                                0,
                                binding.eventTitle.text.toString().ifEmpty { "" },
                                binding.eventDesc.text.toString().ifEmpty { "" },
                                eventStartDateTimeInMillis,
                                eventEndDateTimeInMillis
                            )
                        )
                    }



                    finish()

                    true
                }

                else -> false
            }
        }
    }

    private fun getHourMinuteLocal(time: Long): String {
        return if (is24HourFormat(this)) SimpleDateFormat("HH:mm", Locale.getDefault()).format(
            time
        ) else SimpleDateFormat("hh:mm a", Locale.getDefault()).format(time)
    }

    private fun loadWidgetDataFromEvent(event: Event) {

        binding.eventTitle.setText(event.eventTitle)
        binding.eventDesc.setText(event.eventDescription)

        eventStartDateTimeInMillis =
            event.eventStartTime
        eventEndDateTimeInMillis = event.eventEndTime


        val localCalendar = Calendar.getInstance()


        localCalendar.timeInMillis = eventStartDateTimeInMillis
        eventStartHour = localCalendar.get(Calendar.HOUR_OF_DAY)
        eventStartMinute = localCalendar.get(Calendar.MINUTE)


        binding.editTextStartDate.setText(
            SimpleDateFormat.getDateInstance().format(eventStartDateTimeInMillis).toString()
        )
        binding.editTextStartTime.setText(getHourMinuteLocal(eventStartDateTimeInMillis))

        localCalendar.timeInMillis = eventEndDateTimeInMillis
        eventEndHour = localCalendar.get(Calendar.HOUR_OF_DAY)
        eventEndMinute = localCalendar.get(Calendar.MINUTE)

        binding.editTextEndDate.setText(
            SimpleDateFormat.getDateInstance().format(eventEndDateTimeInMillis).toString()
        )
        binding.editTextEndTime.setText(getHourMinuteLocal(eventEndDateTimeInMillis))

    }

    private fun modifiedEventDateTime(date: Long, hour: Int, min: Int): Long {
        val localCalendar = Calendar.getInstance()
        localCalendar.timeInMillis = date
        localCalendar.set(Calendar.MINUTE, min)
        localCalendar.set(Calendar.HOUR_OF_DAY, hour)

        return localCalendar.timeInMillis
    }
}