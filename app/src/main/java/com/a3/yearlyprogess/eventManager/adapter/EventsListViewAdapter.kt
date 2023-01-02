package com.a3.yearlyprogess.eventManager.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel

class EventsListViewAdapter(private val mEventViewModel: EventViewModel) :
    RecyclerView.Adapter<EventListViewHolder>() {

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

        holder.itemView.findViewById<LinearLayout>(R.id.parent).setOnLongClickListener {
            mEventViewModel.deleteEvent(eventList[position])
            notifyItemRemoved(position)
            false
        }

        holder.itemView.findViewById<LinearLayout>(R.id.parent).setOnClickListener {
            val currentEvent = eventList[position]
            mEventViewModel.updateEvent(Event(
                id = currentEvent.id,
                eventTitle = "event update",
                eventDescription = "event desc update",
                eventStartTime = System.currentTimeMillis() + 36000000,
                eventEndTime = System.currentTimeMillis() + 72000000
            ))
            notifyItemChanged(position)
        }

    }

    override fun getItemCount(): Int = eventList.size

    fun setData(events: List<Event>) {
        eventList = events
        notifyDataSetChanged()
    }

}