package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.CustomEventCardViewBinding
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.google.android.material.card.MaterialCardView
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

    private var job: Job
    private var event: Event? = null


    init {
        orientation = VERTICAL

        ProgressPercentage(context).setDefaultWeek()
        ProgressPercentage(context).setDefaultCalculationMode()

        job = Job()

    }

    fun setEvent(event: Event) {
        this.event = event

        binding.eventTitle.text = event.eventTitle
        binding.eventTitle.text = event.eventTitle
        binding.eventDesc.text = event.eventDescription

        binding.eventStart.text = SimpleDateFormat.getDateTimeInstance().format(
            event.eventStartTime
        )
        binding.eventEnd.text = SimpleDateFormat.getDateTimeInstance().format(
            event.eventEndTime
        )

        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
        val decimalPlace: Int =
            settingsPref.getInt(context.getString(R.string.widget_event_widget_decimal_point), 2)


        launch(Dispatchers.IO) {
            while (true) {
                val progress = ProgressPercentage.getProgress(
                    ProgressPercentage.CUSTOM_EVENT,
                    event.eventStartTime,
                    event.eventEndTime
                )

                val progressText = formatProgressStyle(
                    SpannableString(
                        "%,.${decimalPlace}f".format(progress) + "%"
                    )
                )
                launch(Dispatchers.Main) {
                    binding.progressText.text = progressText
                    binding.progressBar.progress = progress.toInt()
                }
                delay(1000)
            }
        }

    }


}