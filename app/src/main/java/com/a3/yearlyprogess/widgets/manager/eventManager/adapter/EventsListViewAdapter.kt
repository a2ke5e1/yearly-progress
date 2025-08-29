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
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.CustomEventSelectorItemViewBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.EventEditorActivity
import com.a3.yearlyprogess.widgets.manager.eventManager.EventSelectorActivity
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Converters
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.EventWidget
import com.a3.yearlyprogess.widgets.ui.EventWidgetOption

class EventsListViewAdapter(private val appWidgetId: Int, private val sendResult: () -> Unit) :
    RecyclerView.Adapter<EventsSelectorListViewHolder>() {
  private var eventList = emptyList<Event>()
  val currentEventList: List<Event>
    get() = eventList

  private var tracker: SelectionTracker<Long>? = null

  // This is used to indicate which event is selected by the user.
  private var selectedEventId: Int? = null

  fun setSelectedEvent(eventId: Int?) {
    if (eventId == -1) return
    selectedEventId = eventId
  }

  fun setSelectedEventIds(selectedIds: List<Int>) {
    tracker?.clearSelection()
    selectedIds.forEach { id -> tracker?.select(id.toLong()) }
  }

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = eventList[position].id.toLong()

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

    // This will indicate currently selected event in the event widget's
    // event selection list.
    if (selectedEventId != null && currentEvent.id == selectedEventId) {
      holder.binding.customEventCardView.root.eventCheck.visibility = View.VISIBLE
      holder.binding.customEventCardView.root.eventCheck.isChecked = true
    } else {
      holder.binding.customEventCardView.root.eventCheck.visibility = View.GONE
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      holder.binding.customEventCardView.setOnAddWidgetClickListener {
        requestPinWidget(it.context, currentEvent)
      }
    }

    if (tracker != null) {
      val isSelected = tracker!!.isSelected(currentEvent.id.toLong())
      holder.binding.customEventCardView.root.eventCheck.visibility =
          if (tracker!!.hasSelection()) View.VISIBLE else View.GONE
      holder.binding.customEventCardView.root.eventCheck.isChecked = isSelected

      holder.binding.customEventCardView.root.eventCheck.setOnClickListener {
        if (isSelected) {
          tracker!!.deselect(currentEvent.id.toLong())
        } else {
          tracker!!.select(currentEvent.id.toLong())
        }
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

    val remoteViews: RemoteViews =
        EventWidget.eventWidgetPreview(context, currentEvent, EventWidgetOption.load(context, -1))
    val bundle = Bundle()
    bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)

    val pinnedWidgetCallbackIntent = Intent(context, EventSelectorActivity::class.java)
    pinnedWidgetCallbackIntent.putExtra("eventId", currentEvent.id)

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
    if (tracker == null) return emptyList()
    val selectedIds = tracker!!.selection.map { it.toInt() }
    return eventList.filter { it.id in selectedIds }
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

class EventItemKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Long>(SCOPE_CACHED) {

  override fun getKey(position: Int): Long? {
    val adapter = recyclerView.adapter as? EventsListViewAdapter
    return adapter?.currentEventList?.get(position)?.id?.toLong()
  }

  override fun getPosition(key: Long): Int {
    val adapter = recyclerView.adapter as? EventsListViewAdapter
    return adapter?.currentEventList?.indexOfFirst { it.id.toLong() == key }
        ?: RecyclerView.NO_POSITION
  }
}
