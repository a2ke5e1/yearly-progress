package com.a3.yearlyprogess.eventManager.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

class EventListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val eventTitle = itemView.findViewById<TextView>(R.id.eventTitle)
    private val eventDescription = itemView.findViewById<TextView>(R.id.eventDesc)
    private val eventStart = itemView.findViewById<TextView>(R.id.eventStart)
    private val eventEnd = itemView.findViewById<TextView>(R.id.eventEnd)

    fun bind(event: Event) {

        eventTitle.text = event.eventTitle
        eventDescription.text = event.eventDescription


        eventStart.text = SimpleDateFormat.getDateTimeInstance().format(
            event.eventStartTime
        )
        eventEnd.text = SimpleDateFormat.getDateTimeInstance().format(
            event.eventEndTime
        )

    }

}
