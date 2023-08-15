package com.a3.yearlyprogess.eventManager.adapter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.CustomEventSelectorItemViewBinding
import com.a3.yearlyprogess.eventManager.EventManagerActivity
import com.a3.yearlyprogess.eventManager.EventSelectorActivity
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.mWidgets.EventWidget


class EventsListViewAdapter(
    private val appWidgetId: Int,
    private val sendResult: () -> Unit
) :
    RecyclerView.Adapter<EventsSelectorListViewHolder>() {

    private var eventList = emptyList<Event>()
    private var _selectedEventList = MutableLiveData(emptyList<Event>())

    val selectedEventList: MutableLiveData<List<Event>>
        get() = _selectedEventList


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventsSelectorListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CustomEventSelectorItemViewBinding.inflate(inflater, parent, false)
        return EventsSelectorListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventsSelectorListViewHolder, position: Int) {
        val currentEvent = eventList[position]
        holder.binding.customEventCardView.background = null

        holder.binding.customEventCardView.setEvent(currentEvent)
        holder.binding.customEventCardView.setOnEditButtonClickListener {
            val intent = Intent(it.context, EventManagerActivity::class.java)
            intent.putExtra("event", currentEvent)
            intent.putExtra("addMode", false)
            it.context.startActivity(intent)

        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            holder.binding.customEventCardView.setOnAddWidgetClickListener {
                requestPinWidget(it.context, currentEvent)
            }
        }


        holder.binding.customEventCardView.setOnClickListener {
            val appWidgetManager = AppWidgetManager.getInstance(it.context)
            val pref =
                it.context.getSharedPreferences("eventWidget_${appWidgetId}", Context.MODE_PRIVATE)
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


        holder.binding.customEventCardView.setOnLongClickListener {

            val copied = _selectedEventList.value!!.toMutableList()

            if (copied.contains(currentEvent)) {
                copied.remove(currentEvent)

                _selectedEventList.value = copied
                holder.binding.customEventCardView.background = null
            } else {
                copied.add(currentEvent)
                holder.binding.customEventCardView.setBackgroundColor(
                    Color.parseColor(
                        "#FF6200EE"
                    )
                )

                _selectedEventList.value = copied
            }
            true
        }


    }

    private fun requestPinWidget(context: Context, currentEvent: Event) {
        val mAppWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(context, EventWidget::class.java)
        if (!mAppWidgetManager.isRequestPinAppWidgetSupported) {
            Toast.makeText(
                context, context.getString(R.string.unsupported_launcher), Toast.LENGTH_LONG
            ).show()
            return
        }

        val remoteViews: RemoteViews =  EventWidget.eventWidgetPreview(context, currentEvent)
        val bundle = Bundle()
        bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)

        val pinnedWidgetCallbackIntent =
            Intent(context, EventSelectorActivity::class.java)

        val extras = Bundle()
        extras.putParcelable("event", currentEvent)

        pinnedWidgetCallbackIntent.putExtra("event", currentEvent)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            pinnedWidgetCallbackIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mAppWidgetManager.requestPinAppWidget(myProvider, bundle, pendingIntent)

    }

    override fun getItemCount(): Int = eventList.size

    fun setData(events: List<Event>) {
        eventList = events
        _selectedEventList.value = emptyList()
        notifyDataSetChanged()
    }


}

class EventsSelectorListViewHolder(val binding: CustomEventSelectorItemViewBinding) :
    RecyclerView.ViewHolder(binding.root)

