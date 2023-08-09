package com.a3.yearlyprogess.eventManager.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.databinding.CustomEventItemViewBinding
import com.a3.yearlyprogess.eventManager.EventManagerActivity
import com.a3.yearlyprogess.eventManager.EventSelectorActivity
import com.a3.yearlyprogess.eventManager.model.Event

class EventsListViewAdapter :
    RecyclerView.Adapter<EventListViewHolder>() {

    private var eventList = emptyList<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CustomEventItemViewBinding.inflate(inflater, parent, false)
        return EventListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventListViewHolder, position: Int) {
        val currentEvent = eventList[position]
        holder.binding.customEventCardView.setEvent(currentEvent)

        // Opens Event Manager Activity in edit mode
        holder.binding.customEventCardView.setOnEditButtonClickListener {
            val intent = Intent(it.context, EventManagerActivity::class.java)
            intent.putExtra("event", currentEvent)
            intent.putExtra("addMode", false)
            it.context.startActivity(intent)
        }



        /*
                holder.itemView.findViewById<MaterialCardView>(R.id.parent).setOnLongClickListener {
                    mEventViewModel.deleteEvent(currentEvent)
                    notifyItemRemoved(position)
                    false
                }

                holder.itemView.findViewById<MaterialCardView>(R.id.parent).setOnClickListener {
                    mEventViewModel.updateEvent(Event(
                        id = currentEvent.id,
                        eventTitle = "event update",
                        eventDescription = "event desc update",
                        eventStartTime = System.currentTimeMillis() + 36000000,
                        eventEndTime = System.currentTimeMillis() + 72000000
                    ))
                    notifyItemChanged(position)
                }*/

        /* mEventViewModel.updateProgressBar(
             currentEvent,
             holder.itemView.findViewById<TextView>(R.id.progressText),
             holder.itemView.findViewById<LinearProgressIndicator>(R.id.progressBar),
         )*/


    }

    override fun getItemCount(): Int = eventList.size

    fun setData(events: List<Event>) {
        eventList = events
        notifyDataSetChanged()
    }


}

class EventListViewHolder(val binding: CustomEventItemViewBinding) :
    RecyclerView.ViewHolder(binding.root)

