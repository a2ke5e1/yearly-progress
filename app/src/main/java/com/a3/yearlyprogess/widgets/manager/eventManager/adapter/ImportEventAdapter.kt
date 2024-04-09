package com.a3.yearlyprogess.widgets.manager.eventManager.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.components.CustomEventCardView.Companion.displayRelativeDifferenceMessage
import com.a3.yearlyprogess.databinding.ImportEventCardViewBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event


class ImportEventAdapter(
    private var _eventsList: List<Event>,
) : RecyclerView.Adapter<ImportEventsViewHolder>() {

    var tracker: SelectionTracker<Long>? = null
    private val eventsList = _eventsList.toMutableList()
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ImportEventsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ImportEventCardViewBinding.inflate(inflater, parent, false)
        return ImportEventsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImportEventsViewHolder, position: Int) {
        val currentEvent = eventsList[position]
        val context = holder.binding.root.context
        val timeDesc = displayRelativeDifferenceMessage(
            context,
            currentEvent.eventStartTime,
            currentEvent.eventEndTime,
            false
        )

        // Set the event details
        holder.binding.eventTitle.text = currentEvent.eventTitle
        holder.binding.eventDesc.text = currentEvent.eventDescription
        holder.binding.eventStart.text = timeDesc
        holder.binding.eventCheck.isChecked = tracker?.isSelected(position.toLong()) ?: false

        // hide description if it is empty
        if (currentEvent.eventDescription.isEmpty()) {
            holder.binding.eventDesc.visibility = View.GONE
        } else {
            holder.binding.eventDesc.visibility = View.VISIBLE
        }

        holder.binding.eventCheck.setOnClickListener {
            handleSelection(holder, position)
        }
        holder.binding.parent.setOnClickListener {
            handleSelection(holder, position)
        }

    }

    private fun handleSelection(holder: ImportEventsViewHolder, position: Int) {
        if (tracker?.isSelected(position.toLong()) == true) {
            tracker?.deselect(position.toLong())
        } else {
            tracker?.select(position.toLong())
        }
        holder.binding.eventCheck.isChecked = tracker?.isSelected(position.toLong()) ?: false
    }

    override fun getItemCount(): Int = eventsList.size

    fun toggleSelectAll() {
        if (tracker?.hasSelection() == true) {
            tracker?.clearSelection()
        } else {
            tracker?.let {
                for (i in 0 until itemCount) {
                    it.select(i.toLong())
                }
            }
        }
    }


    fun getSelectedEvents(): List<Event> {
        val selectedEvents = mutableListOf<Event>()
        tracker?.selection?.let {
            for (i in it) {
                selectedEvents.add(eventsList[i.toInt()])
            }
        }
        return selectedEvents
    }

    fun setEvents(events: List<Event>) {
        _eventsList = events
        eventsList.clear()
        eventsList.addAll(events)
        notifyDataSetChanged()
    }

    fun updateEvents(events: List<Event>) {
        eventsList.clear()
        eventsList.addAll(events)
        notifyDataSetChanged()
    }

    fun filter(query: String, range: Pair<Long,Long>? = null) {
        val filteredEvents = _eventsList.filter {(
            it.eventTitle.contains(query, ignoreCase = true) ||
                    it.eventDescription.contains(query, ignoreCase = true)) &&
                    (range == null || it.eventStartTime in range.first..range.second)
        }
        Log.d("EventAdapter", "eventsList: ${eventsList.size}, org: ${_eventsList.size}, filtered: ${filteredEvents.size}")
        updateEvents(filteredEvents)
    }

    fun resetFilter() {
        Log.d("EventAdapter", "eventsList: ${eventsList.size}, org: ${_eventsList.size}")
        updateEvents(_eventsList)
    }

    fun filterByDateRange(startDate: Long, endDate: Long) {
        val filteredEvents = _eventsList.filter {
            it.eventStartTime in startDate..endDate
        }
        updateEvents(filteredEvents)
    }

}

class ImportEventsViewHolder(val binding: ImportEventCardViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long = itemId
        }
}

class ImportEventItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as ImportEventsViewHolder)
                .getItemDetails()
        }
        return null
    }
}

class ImportEventItemKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        return recyclerView.adapter?.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}

