package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.YearlyProgressUtil
import com.a3.yearlyprogess.databinding.CustomEventCardViewBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale.getDefault
import kotlin.coroutines.CoroutineContext

@SuppressLint("ViewConstructor", "SetTextI18n")
class EventDetailView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private var binding: CustomEventCardViewBinding =
      CustomEventCardViewBinding.inflate(LayoutInflater.from(context), this, true)
  private val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)

  private var job: Job

  val root: CustomEventCardViewBinding
    get() = binding

  init {
    orientation = VERTICAL
    job = Job()
  }

  fun setEvent(event: Event) {
    // cancel the previous job
    job.cancel()
    // make a job
    job = Job()

    binding.eventTitle.text = event.eventTitle
    if (event.eventDescription.isNotEmpty()) {
      binding.eventDesc.visibility = VISIBLE
      binding.eventDesc.text = event.eventDescription
    } else {
      binding.eventDesc.visibility = GONE
    }

    binding.eventStart.text =
        displayRelativeDifferenceMessage(
            context,
            event.eventStartTime.time,
            event.eventEndTime.time,
            event.allDayEvent,
        )
    // binding.eventEnd.visibility = View.GONE

    launch(Dispatchers.IO) {
      val yp = YearlyProgressUtil(context)
      val (start, end) = event.nextStartAndEndTime()
      val newProgress = yp.calculateProgress(start, end)

      // eventStartTimeInMills = newEventStart
      // eventEndDateTimeInMillis = newEventEnd
      var progress = newProgress

      progress = if (progress > 100) 100.0 else progress
      progress = if (progress < 0) 0.0 else progress

      launch(Dispatchers.Main) { updateView(progress) }

      while (true) {
        val decimalPlace: Int =
            settingsPref.getInt(context.getString(R.string.widget_event_widget_decimal_point), 2)

        val (_start, _end) = event.nextStartAndEndTime()
        val _newProgress = yp.calculateProgress(_start, _end)
        // Log.d("EventDetailView", "EventDetailView: $newProgress")
        // Log.d("EventDetailView", "EventDetailView: $_start $_end")

        // eventStartTimeInMills = newEventStart
        // eventEndDateTimeInMillis = newEventEnd
        progress = _newProgress.coerceIn(0.0, 100.0)

        val progressText = progress.styleFormatted(0)
        val eventTimeLeft = if (System.currentTimeMillis() < event.eventStartTime.time) {
          context.getString(
            R.string.time_in,
            (event.eventStartTime.time - System.currentTimeMillis()).toTimePeriodText()
          )
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
        } else {
          context.getString(
            R.string.time_left,
            yp.calculateTimeLeft(event.eventEndTime.time)
              .toTimePeriodText()
          )
        }

        launch(Dispatchers.Main) {
          binding.eventTitle.text = event.eventTitle
          if (event.eventDescription.isNotEmpty()) {
            binding.eventDesc.visibility = VISIBLE
            binding.eventDesc.text = event.eventDescription
          } else {
            binding.eventDesc.visibility = GONE
          }

          binding.eventStart.text =
              displayRelativeDifferenceMessage(context, _start, _end, event.allDayEvent)
          binding.daysLeft.text = eventTimeLeft

          binding.progressText.text = progressText
          binding.progressBar.progress = progress.toInt()
          updateView(progress, animate = false)
        }
        delay(1000)
      }
    }
  }

  private fun updateView(
      progress: Double,
      animate: Boolean = true,
  ) {
    val decimalPlace: Int =
        settingsPref.getInt(context.getString(R.string.app_widget_decimal_point), 2)

    // val params = binding.customProgressBar.layoutParams
    // val target = (progress * 0.01 * binding.parent.width).toInt()
    // val progressBarValueAnimator = ValueAnimator.ofInt(params.width, target)
    val currentProgress = binding.progressBar.progress.toFloat()
    val progressTextValueAnimator = ValueAnimator.ofFloat(currentProgress, progress.toFloat())

    // progressBarValueAnimator.duration = if (animate) ANIMATION_DURATION else 0
    progressTextValueAnimator.duration = if (animate) ANIMATION_DURATION else 0

    /*progressBarValueAnimator.addUpdateListener {
      binding.customProgressBar.layoutParams.width = it.animatedValue as Int
      binding.customProgressBar.requestLayout()
    }*/

    progressTextValueAnimator.addUpdateListener {
      binding.progressText.text = progress.styleFormatted(0)
    }

    // progressBarValueAnimator.start()
    progressTextValueAnimator.start()
  }

  fun setOnEditButtonClickListener(listener: OnClickListener) {
    binding.editButton.setOnClickListener(listener)
  }

  fun setOnAddWidgetClickListener(listener: OnClickListener) {
    binding.addButton.visibility = VISIBLE
    binding.addButton.setOnClickListener(listener)
  }

  companion object {
    /**
     * It will return a string that will display relative difference between two dates such as if
     * there is difference is time but not in day then it will display Aug 12, 2023 12:00 AM - 11:59
     * PM
     *
     * if there is difference in day then it will display Aug 12, 2023 12:00 AM - Aug 13, 2023 11:59
     * PM
     *
     * @param startTime in milliseconds
     * @param endTime in milliseconds
     */
    fun displayRelativeDifferenceMessage(
        context: Context,
        startTime: Long,
        endTime: Long,
        allDayEvent: Boolean,
    ): String {
      val startDay = SimpleDateFormat.getDateInstance().format(startTime)
      val endDay = SimpleDateFormat.getDateInstance().format(endTime)

      val startTimeString =
          DateFormat.format(
                  if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a",
                  startTime,
              )
              .toString()
              .uppercase()
      val endTimeString =
          DateFormat.format(if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a", endTime)
              .toString()
              .uppercase()

      // long dash unicode is \u2014

      return if (allDayEvent) {
        "${DateFormat.format(
                        "MMM dd, yyyy",
                        startTime,
                    )} \u2014 ${DateFormat.format("MMM dd, yyyy", endTime)} \u00B7 ${ContextCompat.getString(context, R.string.all_day)}"
      } else {
        if (startDay == endDay) {
          "${DateFormat.format("MMM dd, yyyy", startTime)} \u00B7 $startTimeString \u2014 $endTimeString"
        } else {
          "${DateFormat.format(
                            "MMM dd, yyyy ",
                            startTime,
                        )} $startTimeString \u2014 ${DateFormat.format("MMM dd, yyyy", endTime)} $endTimeString"
        }
      }
    }

    private const val ANIMATION_DURATION = 500L
  }
}
