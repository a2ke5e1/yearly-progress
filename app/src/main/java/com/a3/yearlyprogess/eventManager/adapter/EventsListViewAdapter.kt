package com.a3.yearlyprogess.eventManager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.eventManager.model.Event

class EventsListViewAdapter : RecyclerView.Adapter<EventListViewHolder>() {

    private var eventList = emptyList<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListViewHolder {
        return EventListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.custom_event_item_view, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: EventListViewHolder, position: Int) {
        holder.bind(eventList[position])
    }

    override fun getItemCount(): Int = eventList.size

    fun setData(events: List<Event>) {
        eventList = events
        notifyDataSetChanged()
    }
}