package com.a3.yearlyprogess.eventManager.adapter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.databinding.CustomEventSelectorItemViewBinding
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.mWidgets.EventWidget

class EventsSelectorListViewAdapter(private val appWidgetId: Int,private val sendResult : ()-> Unit) :
    RecyclerView.Adapter<EventsSelectorListViewHolder>() {

    private var eventList = emptyList<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsSelectorListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CustomEventSelectorItemViewBinding.inflate(inflater, parent, false)
        return EventsSelectorListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventsSelectorListViewHolder, position: Int) {
        val currentEvent = eventList[position]

        holder.binding.eventName.text = currentEvent.eventTitle

        holder.binding.eventName.setOnClickListener {
            val appWidgetManager = AppWidgetManager.getInstance(it.context)
            val pref = it.context.getSharedPreferences("eventWidget_${appWidgetId}", Context.MODE_PRIVATE)
            val edit = pref.edit()

            edit.putInt("eventId", currentEvent.id)
            edit.putString("eventTitle", currentEvent.eventTitle)
            edit.putString("eventDesc", currentEvent.eventDescription)
            edit.putLong("eventStartTimeInMills", currentEvent.eventStartTime)
            edit.putLong("eventEndDateTimeInMillis", currentEvent.eventEndTime)

            edit.commit()

            EventWidget().updateWidget(it.context, appWidgetManager, appWidgetId)
            sendResult()

        }



    }

    override fun getItemCount(): Int = eventList.size

    fun setData(events: List<Event>) {
        eventList = events
        notifyDataSetChanged()
    }


}

class EventsSelectorListViewHolder(val binding: CustomEventSelectorItemViewBinding) :
    RecyclerView.ViewHolder(binding.root)

