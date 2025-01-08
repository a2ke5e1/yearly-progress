package com.a3.yearlyprogess.widgets.manager.eventManager.adapter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.Toast
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.CustomEventSelectorItemViewBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.EventEditorActivity
import com.a3.yearlyprogess.widgets.manager.eventManager.EventSelectorActivity
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Converters
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.EventWidget

class EventsListViewAdapter(private val appWidgetId: Int, private val sendResult: () -> Unit) :
    RecyclerView.Adapter<EventsSelectorListViewHolder>() {
  private var eventList = emptyList<Event>()
  val currentEventList: List<Event>
    get() = eventList

  private var tracker: SelectionTracker<Long>? = null

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int,
  ): EventsSelectorListViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = CustomEventSelectorItemViewBinding.inflate(inflater, parent, false)
    return EventsSelectorListViewHolder(binding)
  }

  override fun onBindViewHolder(
      holder: EventsSelectorListViewHolder,
      position: Int,
  ) {
    val currentEvent = eventList[position]
    holder.binding.customEventCardView.root.eventCheck.isChecked = false
    holder.binding.customEventCardView.root.eventCheck.visibility = View.GONE

    holder.binding.customEventCardView.setEvent(currentEvent)
    holder.binding.customEventCardView.setOnEditButtonClickListener {
      val intent = Intent(it.context, EventEditorActivity::class.java)
      intent.putExtra("event", currentEvent)
      intent.putExtra("addMode", false)
      it.context.startActivity(intent)
    }
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      holder.binding.customEventCardView.setOnAddWidgetClickListener {
        requestPinWidget(it.context, currentEvent)
      }

      if (tracker != null) {
        holder.binding.customEventCardView.root.eventCheck.visibility =
            if (tracker!!.hasSelection() || tracker!!.selection.size() > 0) {
              View.VISIBLE
            } else {
              View.GONE
            }
        holder.binding.customEventCardView.root.eventCheck.isChecked =
            tracker!!.isSelected(position.toLong())

        holder.binding.customEventCardView.root.eventCheck.setOnClickListener {
          if (tracker!!.isSelected(position.toLong())) {
            tracker!!.deselect(position.toLong())
          } else {
            tracker!!.select(position.toLong())
          }
        }
      }
    } else {
      holder.binding.customEventCardView.setOnClickListener {
        val appWidgetManager = AppWidgetManager.getInstance(it.context)
        val pref = it.context.getSharedPreferences("eventWidget_$appWidgetId", Context.MODE_PRIVATE)
        val edit = pref.edit()
        val conv = Converters()
        val eventDays = conv.fromRepeatDaysList(currentEvent.repeatEventDays)

        edit.putInt("eventId", currentEvent.id)
        edit.putString("eventTitle", currentEvent.eventTitle)
        edit.putString("eventDesc", currentEvent.eventDescription)
        edit.putBoolean("allDayEvent", currentEvent.allDayEvent)
        edit.putLong("eventStartTimeInMills", currentEvent.eventStartTime.time)
        edit.putLong("eventEndDateTimeInMillis", currentEvent.eventEndTime.time)
        edit.putString("eventRepeatDays", eventDays)

        edit.commit()

        EventWidget().updateWidget(it.context, appWidgetManager, appWidgetId)
        sendResult()
      }
    }
  }

  fun setTracker(tracker: SelectionTracker<Long>) {
    this.tracker = tracker
  }

  private fun requestPinWidget(
      context: Context,
      currentEvent: Event,
  ) {
    val mAppWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    if (!mAppWidgetManager.isRequestPinAppWidgetSupported) {
      Toast.makeText(context, context.getString(R.string.unsupported_launcher), Toast.LENGTH_LONG)
          .show()
      return
    }
    val myProvider = ComponentName(context, EventWidget::class.java)
    val conv = Converters()
    val eventDays = conv.fromRepeatDaysList(currentEvent.repeatEventDays)

    val remoteViews: RemoteViews = EventWidget.eventWidgetPreview(context, currentEvent)
    val bundle = Bundle()
    bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)

    val pinnedWidgetCallbackIntent = Intent(context, EventSelectorActivity::class.java)
    pinnedWidgetCallbackIntent.putExtra("eventId", currentEvent.id)
    pinnedWidgetCallbackIntent.putExtra("eventTitle", currentEvent.eventTitle)
    pinnedWidgetCallbackIntent.putExtra("eventDesc", currentEvent.eventDescription)
    pinnedWidgetCallbackIntent.putExtra("allDayEvent", currentEvent.allDayEvent)
    pinnedWidgetCallbackIntent.putExtra("eventStartTimeInMills", currentEvent.eventStartTime)
    pinnedWidgetCallbackIntent.putExtra("eventEndDateTimeInMillis", currentEvent.eventEndTime)
    pinnedWidgetCallbackIntent.putExtra("eventRepeatDays", eventDays)

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            pinnedWidgetCallbackIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    mAppWidgetManager.requestPinAppWidget(myProvider, bundle, pendingIntent)
  }

  override fun getItemCount(): Int = eventList.size

  fun setData(events: List<Event>) {
    eventList = events
    notifyDataSetChanged()
  }

  fun selectAll() {
    if (tracker != null) {
      if (tracker!!.hasSelection() && tracker!!.selection.size() == itemCount) {
        tracker!!.clearSelection()
      } else {
        for (i in 0 until itemCount) {
          tracker!!.select(i.toLong())
        }
      }
    }
  }

  fun getSelectedEvents(): List<Event> {
    val selectedEvents = mutableListOf<Event>()
    if (tracker != null) {
      for (i in 0 until itemCount) {
        if (tracker!!.isSelected(i.toLong())) {
          selectedEvents.add(eventList[i])
        }
      }
    }
    return selectedEvents
  }
}

class EventsSelectorListViewHolder(val binding: CustomEventSelectorItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
  fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
      object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition

        override fun getSelectionKey(): Long = itemId
      }
}

class MyItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
  override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
    val view = recyclerView.findChildViewUnder(event.x, event.y)
    if (view != null) {
      return (recyclerView.getChildViewHolder(view) as EventsSelectorListViewHolder)
          .getItemDetails()
    }
    return null
  }
}
