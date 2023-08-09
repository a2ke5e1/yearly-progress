package com.a3.yearlyprogess.eventManager.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.components.CustomEventCardView
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

class EventListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val customEventCardView = itemView.findViewById<CustomEventCardView>(R.id.customEventCardView)

    fun bind(event: Event) {
        customEventCardView.setEvent(event)
    }

}
