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
import java.util.*

class EventConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventConfigActibityBinding

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

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()

            datePicker.show(supportFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener {
            binding.editTextDate.text  = it.toString()
        }


        val isSystem24Hour = is24HourFormat(this)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setTitleText("Select Appointment time")
                .build()

        picker.show(supportFragmentManager, "tag")

        picker.addOnPositiveButtonClickListener {
            binding.editTextTime.text = picker.hour.toString() + ":" + picker.minute.toString()
        }
        updateEventWidget(this, appWidgetManager, appWidgetId)





       /* val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()*/

    }
}