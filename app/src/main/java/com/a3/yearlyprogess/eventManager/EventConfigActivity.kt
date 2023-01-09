package com.a3.yearlyprogess.eventManager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.ActivityEventConfigActivityBinding
import com.a3.yearlyprogess.mWidgets.EventWidget
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class EventConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventConfigActivityBinding

    private var eventStartDateTimeInMillis: Long = -1
    private var eventStartHour: Int = 0
    private var eventStartMinute: Int = 0


    private var eventEndDateTimeInMillis: Long = -1
    private var eventEndHour: Int = 0
    private var eventEndMinute: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventConfigActivityBinding.inflate(layoutInflater)
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


        setResult(Activity.RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val datePicker = MaterialDatePicker.Builder.datePicker()
        val isSystem24Hour = is24HourFormat(this)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(clockFormat)

        val pref = this.getSharedPreferences(appWidgetId.toString(), Context.MODE_PRIVATE)
        val edit = pref.edit()

        loadWidgetDataIfExists(pref)

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

                    val eventTitle = binding.eventTitle.text.toString().ifEmpty { "" }
                    val eventDesc = binding.eventDesc.text.toString().ifEmpty { "" }

                    edit.putString("eventTitle", eventTitle)
                    edit.putString("eventDesc", eventDesc)
                    edit.putLong("eventStartTimeInMills", eventStartDateTimeInMillis)
                    edit.putLong("eventEndDateTimeInMillis", eventEndDateTimeInMillis)

                    edit.commit()

                    EventWidget().updateWidget(this, appWidgetManager, appWidgetId)


                    val resultValue =
                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
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

    private fun loadWidgetDataIfExists(pref: SharedPreferences) {
        val eventTitle = pref.getString("eventTitle", "").toString()
        val eventDesc = pref.getString("eventDesc", "").toString()
        eventStartDateTimeInMillis =
            pref.getLong("eventStartTimeInMills", System.currentTimeMillis())
        eventEndDateTimeInMillis =
            pref.getLong("eventEndDateTimeInMillis", System.currentTimeMillis())


        binding.eventTitle.setText(eventTitle)
        binding.eventDesc.setText(eventDesc)

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