package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.CustomEventCardViewBinding
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.a3.yearlyprogess.helper.ProgressPercentage
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@SuppressLint("ViewConstructor", "SetTextI18n")
class CustomEventCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
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
            binding.eventDesc.visibility = View.VISIBLE
            binding.eventDesc.text = event.eventDescription
        } else {
            binding.eventDesc.visibility = View.GONE
        }

        binding.eventStart.text = displayRelativeDifferenceMessage(
            event.eventStartTime,
            event.eventEndTime
        )
        binding.eventEnd.visibility = View.GONE

        launch(Dispatchers.IO) {

            var progress = ProgressPercentage.getProgress(
                ProgressPercentage.CUSTOM_EVENT,
                event.eventStartTime,
                event.eventEndTime
            )

            if (progress > 100) {
                progress = 100.0
            }

            if (progress < 0) {
                progress = 0.0
            }

            launch(Dispatchers.Main) {
                updateView(progress)
            }

            while (true) {

                val decimalPlace: Int =
                    settingsPref.getInt(
                        context.getString(R.string.widget_event_widget_decimal_point),
                        2
                    )

                ProgressPercentage(context).setDefaultWeek()
                ProgressPercentage(context).setDefaultCalculationMode()

                progress = ProgressPercentage.getProgress(
                    ProgressPercentage.CUSTOM_EVENT,
                    event.eventStartTime,
                    event.eventEndTime
                )

                if (progress > 100) {
                    progress = 100.0
                }

                if (progress < 0) {
                    progress = 0.0
                }


                val progressText = formatProgressStyle(
                    SpannableString(
                        "%,.${decimalPlace}f".format(progress) + "%"
                    )
                )

                launch(Dispatchers.Main) {

                    binding.eventTitle.text = event.eventTitle
                    if (event.eventDescription.isNotEmpty()) {
                        binding.eventDesc.visibility = View.VISIBLE
                        binding.eventDesc.text = event.eventDescription
                    } else {
                        binding.eventDesc.visibility = View.GONE
                    }

                    binding.eventStart.text = displayRelativeDifferenceMessage(
                        event.eventStartTime,
                        event.eventEndTime
                    )
                    binding.eventEnd.visibility = View.GONE

                    binding.progressText.text = progressText
                    binding.progressBar.progress = progress.toInt()
                }
                delay(1000)
            }
        }

    }

    private fun updateView(progress: Double) {

        val decimalPlace: Int =
            settingsPref.getInt(context.getString(R.string.widget_event_widget_decimal_point), 2)




        // val params = widgetProgressCard.layoutParams
        // val target = (progress * 0.01 * widgetParentCard.width).toInt()
        val progressBarValueAnimator = ValueAnimator.ofInt(0, progress.toInt())
        val progressTextValueAnimator = ValueAnimator.ofFloat(0F, progress.toFloat())

        progressBarValueAnimator.duration = 500
        progressTextValueAnimator.duration = 500

        progressBarValueAnimator.addUpdateListener {
            binding.progressBar.progress = it.animatedValue as Int
            binding.progressBar.requestLayout()
        }

        progressTextValueAnimator.addUpdateListener {
            binding.progressText.text = formatProgressStyle(
                SpannableString(
                    "%,.${decimalPlace}f".format(it.animatedValue) + "%"
                )
            )
        }

        progressBarValueAnimator.start()
        progressTextValueAnimator.start()
    }

    /**
     * It will return a string that will display relative difference between two dates
     * such as if there is difference is time but not in day then it will display
     * Aug 12, 2023
     * 12:00 AM - 11:59 PM
     *
     * if there is difference in day then it will display
     * Aug 12, 2023 12:00 AM - Aug 13, 2023 11:59 PM
     *
     * @param startTime in milliseconds
     * @param endTime in milliseconds

     */
    fun displayRelativeDifferenceMessage(startTime: Long, endTime: Long): String {

            val startDay = SimpleDateFormat.getDateInstance().format(startTime)
            val endDay = SimpleDateFormat.getDateInstance().format(endTime)

            val startTimeString = SimpleDateFormat.getTimeInstance().format(startTime)
            val endTimeString = SimpleDateFormat.getTimeInstance().format(endTime)

            return if (startDay == endDay) {
                "$startDay \n$startTimeString - $endTimeString"
            } else {
                "$startDay $startTimeString - $endDay $endTimeString"
            }
    }

    fun setOnEditButtonClickListener(listener: OnClickListener) {
        binding.editButton.setOnClickListener(listener)
    }

    fun setOnAddWidgetClickListener(listener: OnClickListener) {
        binding.addButton.visibility = VISIBLE
        binding.addButton.setOnClickListener(listener)
    }


}