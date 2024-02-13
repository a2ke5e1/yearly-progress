package com.a3.yearlyprogess.eventManager.adapter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.Toast
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.components.CustomEventCardView.Companion.displayRelativeDifferenceMessage
import com.a3.yearlyprogess.databinding.CustomEventSelectorItemViewBinding
import com.a3.yearlyprogess.databinding.ImportEventCardViewBinding
import com.a3.yearlyprogess.eventManager.EventManagerActivity
import com.a3.yearlyprogess.eventManager.EventSelectorActivity
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.widgets.EventWidget


class ImportEventAdapter(
    private val eventsList: List<Event>,
) : RecyclerView.Adapter<ImportEventsViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

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

