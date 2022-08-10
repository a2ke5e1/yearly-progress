package com.a3.yearlyprogess.manager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import androidx.appcompat.app.AppCompatActivity
import com.a3.yearlyprogess.databinding.ActivityEventConfigActibityBinding
import com.a3.yearlyprogess.mwidgets.updateEventWidget
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class EventConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventConfigActibityBinding
    private var eventEndDateTimeInMillis: Long = -1
    private var eventEndHour: Int = 0
    private var eventEndMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventConfigActibityBinding.inflate(layoutInflater)
        setContentView(binding.root)
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




        binding.editTextDate.setOnClickListener {
            datePicker.setTitleText("Select Event End Date")
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {



                        eventEndDateTimeInMillis = it.toLong()
                        eventEndDateTimeInMillis = modifiedEventDateTime(eventEndHour, eventEndMinute)

                        binding.editTextDate.text =
                            SimpleDateFormat("YYYY, MMMM dd HH:mm:ss").format(eventEndDateTimeInMillis).toString()
                    }
                }


        }

        binding.editTextTime.setOnClickListener {
            timePicker.setTitleText("Select Event End Time")
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventEndHour = hour
                        eventEndMinute = minute

                        eventEndDateTimeInMillis = modifiedEventDateTime(eventEndHour, eventEndMinute)

                        binding.editTextTime.text = "${hour}:${minute}"
                    }
                }


        }


        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }

        updateEventWidget(this, appWidgetManager, appWidgetId)

    }

    private fun modifiedEventDateTime(hour: Int, min: Int): Long {
        val localCalendar = Calendar.getInstance()
        localCalendar.timeInMillis = eventEndDateTimeInMillis
        localCalendar.set(Calendar.MINUTE, min )
        localCalendar.set(Calendar.HOUR_OF_DAY, hour)

        return  localCalendar.timeInMillis
    }
}