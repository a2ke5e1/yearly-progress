package com.a3.yearlyprogess.widgets.manager.eventManager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.format
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.ActivityEventManagerActivityBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Converters
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.manager.eventManager.model.RepeatDays
import com.a3.yearlyprogess.widgets.manager.eventManager.viewmodel.EventViewModel
import com.a3.yearlyprogess.widgets.ui.EventWidget
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

        // Set Start Date and Time and End Date and Time to current date and time
        if (isAddMode) {
            val localCalendar = Calendar.getInstance()
            val ONE_HOUR = 60 * 60 * 1000L

            eventStartDateTimeInMillis = System.currentTimeMillis()
            eventEndDateTimeInMillis = System.currentTimeMillis() + ONE_HOUR

            localCalendar.timeInMillis = eventStartDateTimeInMillis
            eventStartHour = localCalendar.get(Calendar.HOUR_OF_DAY)
            eventStartMinute = localCalendar.get(Calendar.MINUTE)


            binding.editTextStartDate.setText(
                format("MMMM dd, yyyy", eventStartDateTimeInMillis).toString()
            )
            binding.editTextStartTime.setText(getHourMinuteLocal(eventStartDateTimeInMillis))

            localCalendar.timeInMillis = eventEndDateTimeInMillis
            eventEndHour = localCalendar.get(Calendar.HOUR_OF_DAY)
            eventEndMinute = localCalendar.get(Calendar.MINUTE)

            binding.editTextEndDate.setText(
                format("MMMM dd, yyyy", eventEndDateTimeInMillis).toString()
            )
            binding.editTextEndTime.setText(getHourMinuteLocal(eventEndDateTimeInMillis))

        }

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        handleRepeatEventSwitch()

        if (event != null && !isAddMode) {
            loadWidgetDataFromEvent(event)
        }


        setUpToolbarActions(isAddMode, event)
        setUpDateTimePickers()


        Log.d(
            "TAG",
            "EventEnd: ${SimpleDateFormat.getDateTimeInstance().format(eventEndDateTimeInMillis)}"
        )

    }

    private fun handleRepeatEventSwitch() {
        binding.everyYearSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.everyMonthSwitch.isChecked = false
                binding.repeatWeekdaysSwitch.isChecked = false
            }
        }

        binding.everyMonthSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.everyYearSwitch.isChecked = false
                binding.repeatWeekdaysSwitch.isChecked = false
            }
        }

        binding.repeatWeekdaysSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.everyMonthSwitch.isChecked = false
                binding.everyYearSwitch.isChecked = false
            }


            if (isChecked) {
                binding.repeatDays.animate().alpha(1f).setDuration(200).start()
            } else {
                binding.repeatDays.animate().alpha(0f).setDuration(200).start()
            }
            binding.repeatDays.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun handleAllDayUIChanges(isChecked: Boolean) {
        if (isChecked) {
            binding.editTextStartTime.animate().alpha(0f).setDuration(200).start()
            binding.editTextEndTime.animate().alpha(0f).setDuration(200).start()

            binding.editTextStartTime.visibility = View.GONE
            binding.editTextEndTime.visibility = View.GONE
        } else {
            binding.editTextStartTime.animate().alpha(1f).setDuration(200).start()
            binding.editTextEndTime.animate().alpha(1f).setDuration(200).start()

            binding.editTextStartTime.visibility = View.VISIBLE
            binding.editTextEndTime.visibility = View.VISIBLE
        }
    }

    private fun handleAllDayTimeOffset(isChecked: Boolean) {
        if (isChecked) {


            val localCalendar = Calendar.getInstance()
            localCalendar.timeInMillis = eventStartDateTimeInMillis
            localCalendar.set(Calendar.HOUR_OF_DAY, 0)
            localCalendar.set(Calendar.MINUTE, 0)
            localCalendar.set(Calendar.SECOND, 0)
            localCalendar.set(Calendar.MILLISECOND, 0)

            eventStartDateTimeInMillis = localCalendar.timeInMillis

            localCalendar.timeInMillis = eventEndDateTimeInMillis
            localCalendar.set(Calendar.HOUR_OF_DAY, 23)
            localCalendar.set(Calendar.MINUTE, 59)
            localCalendar.set(Calendar.SECOND, 59)
            localCalendar.set(Calendar.MILLISECOND, 999)

            eventEndDateTimeInMillis = localCalendar.timeInMillis


        } else {


            val localCalendar = Calendar.getInstance()
            localCalendar.timeInMillis = eventStartDateTimeInMillis

            localCalendar.set(Calendar.HOUR_OF_DAY, eventStartHour)
            localCalendar.set(Calendar.MINUTE, eventStartMinute)

            eventStartDateTimeInMillis = localCalendar.timeInMillis

            localCalendar.timeInMillis = eventEndDateTimeInMillis

            localCalendar.set(Calendar.HOUR_OF_DAY, eventEndHour)
            localCalendar.set(Calendar.MINUTE, eventEndMinute)

            eventEndDateTimeInMillis = localCalendar.timeInMillis

        }
    }

    private fun setUpDateTimePickers() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
        val isSystem24Hour = is24HourFormat(this)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(clockFormat)

        binding.allDaySwitch.setOnCheckedChangeListener { compoundButton, b ->
            // animate visibility of time pickers
            handleAllDayUIChanges(b)
            handleAllDayTimeOffset(b)

        }



        binding.editTextStartDate.setOnClickListener {


            datePicker.setTitleText("Choose event first day")
                .setSelection(removeTimeFromDate(eventStartDateTimeInMillis))
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventStartDateTimeInMillis = it.toLong()
                        handleAllDayTimeOffset(binding.allDaySwitch.isChecked)
                        binding.editTextStartDate.setText(
                            format("MMMM dd, yyyy", eventStartDateTimeInMillis).toString()
                        )
                        setUpDateTimePickers()
                    }
                }


        }
        binding.editTextStartTime.setOnClickListener {
            timePicker.setTitleText("Choose event starting time")
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
                            format("MMMM dd, yyyy", eventStartDateTimeInMillis).toString()
                        )

                        setUpDateTimePickers()

                    }
                }

        }
        binding.editTextEndDate.setOnClickListener {
            datePicker.setTitleText("Choose event last day")
                .setSelection(removeTimeFromDate(eventEndDateTimeInMillis))
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventEndDateTimeInMillis = it.toLong()
                        handleAllDayTimeOffset(binding.allDaySwitch.isChecked)
                        binding.editTextEndDate.setText(
                            format("MMMM dd, yyyy", eventEndDateTimeInMillis).toString()
                        )
                        setUpDateTimePickers()
                    }
                }


        }
        binding.editTextEndTime.setOnClickListener {
            timePicker.setTitleText("Choose event ending time")
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
                            format("MMMM dd, yyyy", eventEndDateTimeInMillis).toString()
                        )
                        setUpDateTimePickers()
                    }
                }

        }
    }


    private fun setUpToolbarActions(
        isAddMode: Boolean,
        event: Event?
    ) {

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
        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.materialToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save_config -> {

                    if (binding.eventTitle.text.toString().isEmpty()) {
                        binding.eventTitleContainer.error = "Event title is required"
                        return@setOnMenuItemClickListener true
                    }




                    if (!isAddMode) {
                        val repeatDays = getRepeatDays()
                        val updatedEvent = Event(
                            event!!.id,
                            binding.eventTitle.text.toString().ifEmpty { "" },
                            binding.eventDesc.text.toString().ifEmpty { "" },
                            binding.allDaySwitch.isChecked,
                            eventStartDateTimeInMillis,
                            eventEndDateTimeInMillis,
                            repeatDays
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
                            val pref =
                                getSharedPreferences("eventWidget_${appWidgetId}", MODE_PRIVATE)
                            val prefEventId = pref.getInt("eventId", -1)
                            if (prefEventId == updatedEvent.id) {
                                val edit = pref.edit()
                                val conv = Converters()

                                edit.putInt("eventId", updatedEvent.id)
                                edit.putString("eventTitle", updatedEvent.eventTitle)
                                edit.putString("eventDesc", updatedEvent.eventDescription)
                                edit.putBoolean("allDayEvent", updatedEvent.allDayEvent)
                                edit.putLong("eventStartTimeInMills", updatedEvent.eventStartTime)
                                edit.putLong("eventEndDateTimeInMillis", updatedEvent.eventEndTime)
                                edit.putString(
                                    "eventRepeatDays",
                                    conv.fromRepeatDaysList(updatedEvent.repeatEventDays)
                                )

                                edit.commit()
                                EventWidget().updateWidget(this, appWidgetManager, appWidgetId)
                            }
                        }


                    } else {
                        val repeatDays = getRepeatDays()
                        mEventViewModel.addEvent(
                            Event(
                                0,
                                binding.eventTitle.text.toString().ifEmpty { "" },
                                binding.eventDesc.text.toString().ifEmpty { "" },
                                binding.allDaySwitch.isChecked,
                                eventStartDateTimeInMillis,
                                eventEndDateTimeInMillis,
                                repeatDays
                            )
                        )
                    }



                    finish()

                    true
                }

                else -> false
            }
        }
        binding.eventTitle.doAfterTextChanged {
            if (it.toString().isNotEmpty()) {
                binding.eventTitleContainer.error = null
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
        binding.allDaySwitch.isChecked = event.allDayEvent
        handleAllDayUIChanges(event.allDayEvent)

        eventStartDateTimeInMillis =
            event.eventStartTime
        eventEndDateTimeInMillis = event.eventEndTime


        val localCalendar = Calendar.getInstance()


        localCalendar.timeInMillis = eventStartDateTimeInMillis
        eventStartHour = localCalendar.get(Calendar.HOUR_OF_DAY)
        eventStartMinute = localCalendar.get(Calendar.MINUTE)


        binding.editTextStartDate.setText(
            format("MMMM dd, yyyy", eventStartDateTimeInMillis).toString()
        )
        binding.editTextStartTime.setText(getHourMinuteLocal(eventStartDateTimeInMillis))

        localCalendar.timeInMillis = eventEndDateTimeInMillis
        eventEndHour = localCalendar.get(Calendar.HOUR_OF_DAY)
        eventEndMinute = localCalendar.get(Calendar.MINUTE)

        binding.editTextEndDate.setText(
            format("MMMM dd, yyyy", eventEndDateTimeInMillis).toString()
        )
        binding.editTextEndTime.setText(getHourMinuteLocal(eventEndDateTimeInMillis))

        setRepeatDays(event.repeatEventDays)

    }

    private fun modifiedEventDateTime(date: Long, hour: Int, min: Int): Long {
        val localCalendar = Calendar.getInstance()
        localCalendar.timeInMillis = date
        localCalendar.set(Calendar.MINUTE, min)
        localCalendar.set(Calendar.HOUR_OF_DAY, hour)

        return localCalendar.timeInMillis
    }

    private fun removeTimeFromDate(date: Long): Long {
        val localCalendar = Calendar.getInstance()
        localCalendar.timeInMillis = date

        // add localCalendar.timeZone.rawOffset to get the correct date
        localCalendar.timeInMillis = localCalendar.timeInMillis + localCalendar.timeZone.rawOffset
        return localCalendar.timeInMillis
    }

    private fun getRepeatDays(): List<RepeatDays> {
        val repeatDays = mutableListOf<RepeatDays>()
        if (binding.btnSunday.isChecked) {
            repeatDays.add(RepeatDays.SUNDAY)
        }
        if (binding.btnMonday.isChecked) {
            repeatDays.add(RepeatDays.MONDAY)
        }
        if (binding.btnTuesday.isChecked) {
            repeatDays.add(RepeatDays.TUESDAY)
        }
        if (binding.btnWednesday.isChecked) {
            repeatDays.add(RepeatDays.WEDNESDAY)
        }
        if (binding.btnThursday.isChecked) {
            repeatDays.add(RepeatDays.THURSDAY)
        }
        if (binding.btnFriday.isChecked) {
            repeatDays.add(RepeatDays.FRIDAY)
        }
        if (binding.btnSaturday.isChecked) {
            repeatDays.add(RepeatDays.SATURDAY)
        }
        if (binding.everyMonthSwitch.isChecked) {
            repeatDays.add(RepeatDays.EVERY_MONTH)
        }
        if (binding.everyYearSwitch.isChecked) {
            repeatDays.add(RepeatDays.EVERY_YEAR)
        }
        return repeatDays
    }

    private fun setRepeatDays(repeatDays: List<RepeatDays>) {
        repeatDays.forEach {
            when (it) {
                RepeatDays.SUNDAY -> binding.btnSunday.isChecked = true
                RepeatDays.MONDAY -> binding.btnMonday.isChecked = true
                RepeatDays.TUESDAY -> binding.btnTuesday.isChecked = true
                RepeatDays.WEDNESDAY -> binding.btnWednesday.isChecked = true
                RepeatDays.THURSDAY -> binding.btnThursday.isChecked = true
                RepeatDays.FRIDAY -> binding.btnFriday.isChecked = true
                RepeatDays.SATURDAY -> binding.btnSaturday.isChecked = true
                RepeatDays.EVERY_MONTH -> binding.everyMonthSwitch.isChecked = true
                RepeatDays.EVERY_YEAR -> binding.everyYearSwitch.isChecked = true
            }
        }

        val checkRepeatWeekdays = repeatDays.any {
            it in listOf(
                RepeatDays.SUNDAY, RepeatDays.MONDAY,
                RepeatDays.TUESDAY, RepeatDays.WEDNESDAY,
                RepeatDays.THURSDAY, RepeatDays.FRIDAY,
                RepeatDays.SATURDAY
            )
        }

        binding.repeatWeekdaysSwitch.isChecked = checkRepeatWeekdays
    }
}