package com.a3.yearlyprogess.widgets.manager

import android.content.ContentResolver
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.databinding.ActivityCalendarWidgetConfigManagerBinding
import com.a3.yearlyprogess.databinding.CalendarInfoItemViewBinding
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCalendarsDetails
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getSelectedCalendarIds
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.saveSelectedCalendarIds
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import java.util.Calendar
import java.util.Date

object CalendarEventInfo {
  fun getTodayOrNearestEvents(
      contentResolver: ContentResolver,
      selectedCalendarId: Long
  ): List<Event> {
    val uri: Uri = CalendarContract.Events.CONTENT_URI
    val projection =
        arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_ID)

    val now = System.currentTimeMillis()
    val calendar =
        Calendar.getInstance().apply {
          timeInMillis = now
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val startOfDay = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endOfDay = calendar.timeInMillis

    val events = mutableListOf<Event>()

    // Check for events happening today in the selected calendar
    val selectionToday =
        "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
    val selectionArgsToday =
        arrayOf(selectedCalendarId.toString(), startOfDay.toString(), endOfDay.toString())
    val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

    contentResolver.query(uri, projection, selectionToday, selectionArgsToday, sortOrder)?.use {
        cursor ->
      while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
        val description =
            cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
        val startTimeUtc =
            cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
        val location =
            cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
        val endTimeUtc = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

        events.add(
            Event(
                id = 0,
                eventTitle = title,
                eventDescription = description,
                eventStartTime = Date(startTimeUtc),
                eventEndTime = Date(endTimeUtc)))
      }
    }

    if (events.isNotEmpty()) {
      return events // Return events for today
    }

    // If no events today, get the nearest upcoming event in the selected calendar
    val selectionUpcoming =
        "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ?"
    val selectionArgsUpcoming = arrayOf(selectedCalendarId.toString(), now.toString())

    contentResolver
        .query(uri, projection, selectionUpcoming, selectionArgsUpcoming, sortOrder)
        ?.use { cursor ->
          if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
            val title =
                cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
            val description =
                cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
            val startTimeUtc =
                cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
            val location =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
            val endTimeUtc =
                cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

            events.add(
                Event(
                    id = 0,
                    eventTitle = title,
                    eventDescription = description,
                    eventStartTime = Date(startTimeUtc),
                    eventEndTime = Date(endTimeUtc)))
          }
        }

    return events // Return the nearest upcoming event (if any) as a single-item list
  }

  data class CalendarInfo(val id: Long, val displayName: String, val accountName: String)

  fun getCalendarsDetails(contentResolver: ContentResolver): List<CalendarInfo> {
    val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    val projection =
        arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME)

    val calendars = mutableListOf<CalendarInfo>()

    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
      while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
        val displayName =
            cursor.getString(
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
        val accountName =
            cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME))

        calendars.add(CalendarInfo(id = id, displayName = displayName, accountName = accountName))
      }
    }

    return calendars
  }

  fun getCurrentEventOrUpcomingEvent(contentResolver: ContentResolver, calendarId: Long): Event? {
    val events = getTodayOrNearestEvents(contentResolver, calendarId)
    return events
        .filter { event -> event.eventEndTime.time > System.currentTimeMillis() }
        .minByOrNull { event -> event.eventStartTime }
  }

  fun saveSelectedCalendarIds(context: Context, selectedCalendarIds: List<Long>) {
    // Save selected calendar IDs to SharedPreferences
    val sharedPreferences =
        context.getSharedPreferences(SELECTED_CALENDAR_PREF, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putStringSet(
        SELECTED_CALENDAR_IDS_KEY, selectedCalendarIds.map { it.toString() }.toSet())
    editor.apply()
  }

  fun getSelectedCalendarIds(context: Context): List<Long>? {
    // Get selected calendar IDs from SharedPreferences
    val sharedPreferences =
        context.getSharedPreferences(SELECTED_CALENDAR_PREF, Context.MODE_PRIVATE)
    val selectedCalendarIds = sharedPreferences.getStringSet(SELECTED_CALENDAR_IDS_KEY, null)
    return selectedCalendarIds?.map { it.toLong() }
  }

  private const val SELECTED_CALENDAR_IDS_KEY = "selected_calendar_ids"
  private const val SELECTED_CALENDAR_PREF = "selected_calendar_prefs"
}

class CalendarSyncListAdapter(
    private val calendarInfoList: List<CalendarEventInfo.CalendarInfo>,
) : RecyclerView.Adapter<CalendarInfoViewHolder>() {
  var tracker: SelectionTracker<Long>? = null

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int,
  ): CalendarInfoViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = CalendarInfoItemViewBinding.inflate(inflater, parent, false)
    return CalendarInfoViewHolder(binding)
  }

  override fun onBindViewHolder(
      holder: CalendarInfoViewHolder,
      position: Int,
  ) {
    val currentEvent = calendarInfoList[position]
    val context = holder.binding.root.context
    holder.binding.calendarName.text = currentEvent.displayName
    holder.binding.calendarAccount.text = currentEvent.accountName
    holder.binding.calendarCheck.isChecked = tracker?.isSelected(position.toLong()) == true

    holder.binding.calendarCheck.setOnClickListener { handleSelection(holder, position) }
    holder.binding.parent.setOnClickListener { handleSelection(holder, position) }
  }

  private fun handleSelection(
      holder: CalendarInfoViewHolder,
      position: Int,
  ) {
    if (tracker?.isSelected(position.toLong()) == true) {
      tracker?.deselect(position.toLong())
    } else {
      tracker?.select(position.toLong())
    }
    holder.binding.calendarCheck.isChecked = tracker?.isSelected(position.toLong()) == true
  }

  override fun getItemCount(): Int = calendarInfoList.size

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

  fun selectCalendarWithIds(selectedCalendarIds: List<Long>) {
    tracker?.let {
      for (i in 0 until itemCount) {
        if (calendarInfoList[i].id in selectedCalendarIds) {
          it.select(i.toLong())
        }
      }
    }
  }

  fun getSelectedCalendarInfos(): List<CalendarEventInfo.CalendarInfo> {
    val selectedEvents = mutableListOf<CalendarEventInfo.CalendarInfo>()
    tracker?.selection?.let {
      for (i in it) {
        selectedEvents.add(calendarInfoList[i.toInt()])
      }
    }
    return selectedEvents
  }
}

class CalendarInfoViewHolder(val binding: CalendarInfoItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
  fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
      object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition

        override fun getSelectionKey(): Long = itemId
      }
}

class CalendarInfoItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
  override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
    val view = recyclerView.findChildViewUnder(event.x, event.y)
    if (view != null) {
      return (recyclerView.getChildViewHolder(view) as CalendarInfoViewHolder).getItemDetails()
    }
    return null
  }
}

class CalendarInfoItemKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {
  override fun getKey(position: Int): Long? {
    return recyclerView.adapter?.getItemId(position)
  }

  override fun getPosition(key: Long): Int {
    val viewHolder = recyclerView.findViewHolderForItemId(key)
    return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
  }
}

class ItemSpaceDecoration : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(
      outRect: Rect,
      view: View,
      parent: RecyclerView,
      state: RecyclerView.State,
  ) {
    val position = parent.getChildAdapterPosition(view)

    // Add space between items with different view types
    when (position) {
      0 -> outRect.bottom = 0
      else -> outRect.top = 16
    }
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
      leftMargin = 20
      rightMargin = 20
    }
    if (position == state.itemCount - 1) {
      outRect.bottom = 64
    }
  }
}

class CalendarWidgetConfigManager : AppCompatActivity() {
  private var _binding: ActivityCalendarWidgetConfigManagerBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityCalendarWidgetConfigManagerBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    val calendars = getCalendarsDetails(this.contentResolver)
    val selectedCalendarIds = getSelectedCalendarIds(this)

    val adapter = CalendarSyncListAdapter(calendars)
    binding.calendarList.apply {
      this.adapter = adapter
      this.layoutManager =
          androidx.recyclerview.widget.LinearLayoutManager(this@CalendarWidgetConfigManager)
      this.addItemDecoration(ItemSpaceDecoration())

      val tracker =
          SelectionTracker.Builder<Long>(
                  "calendar-selection",
                  this,
                  CalendarInfoItemKeyProvider(this),
                  CalendarInfoItemDetailsLookup(this),
                  androidx.recyclerview.selection.StorageStrategy.createLongStorage())
              .build()
      adapter.tracker = tracker
      if (selectedCalendarIds != null) {
        adapter.selectCalendarWithIds(selectedCalendarIds)
      } else {
        adapter.toggleSelectAll()
      }
      tracker.addObserver(
          object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
              super.onSelectionChanged()
              val selectedCalendars = adapter.getSelectedCalendarInfos()
              saveSelectedCalendarIds(
                  this@CalendarWidgetConfigManager, selectedCalendars.map { it.id })
            }
          })
    }
  }
}
