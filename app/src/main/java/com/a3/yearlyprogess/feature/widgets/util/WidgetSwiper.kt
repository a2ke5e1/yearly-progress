package com.a3.yearlyprogess.feature.widgets.util

import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.ThemeManager
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.graphics.toColor
import com.a3.yearlyprogess.feature.events.domain.model.Event

/**
 * Reusable swiper component for widgets that display multiple events.
 * Manages event cycling and indicator display.
 *
 * @param T The type of items to cycle through
 * @param context Android context
 * @param items List of items to cycle through
 * @param widgetId Unique identifier for the widget instance
 * @param preferenceName Name for SharedPreferences storage
 */
class WidgetSwiper<T>(
    private val context: Context,
    private val items: List<T>,
    private val widgetId: Int,
    private val preferenceName: String,
    private val widgetTheme: WidgetTheme = WidgetTheme.DEFAULT
) {

    private val pref = context.getSharedPreferences(
        "${preferenceName}_${widgetId}",
        Context.MODE_PRIVATE
    )

    private val currentIndexKey = "${preferenceName}_index_${widgetId}"

    /**
     * Gets or sets the current item index
     */
    var currentIndex: Int
        get() {
            val savedIndex = pref.getInt(currentIndexKey, 0)
            return if (savedIndex < items.size) savedIndex else 0
        }
        private set(value) {
            pref.edit().putInt(currentIndexKey, value).apply()
        }

    /**
     * Navigate to the next item
     */
    fun next() {
        if (items.isEmpty()) return
        currentIndex = (currentIndex + 1) % items.size
    }

    /**
     * Navigate to the previous item
     */
    fun previous() {
        if (items.isEmpty()) return
        currentIndex = (currentIndex - 1 + items.size) % items.size
    }

    /**
     * Get the current item
     */
    fun current(): T? {
        return items.getOrNull(currentIndex)
    }

    /**
     * Get the total number of items
     */
    fun size(): Int = items.size

    /**
     * Check if the swiper has items
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Check if the swiper has items
     */
    fun isNotEmpty(): Boolean = items.isNotEmpty()

    /**
     * Create a visual indicator showing current position
     * Returns empty string if there are 0 or 1 items
     */
    fun indicator(): SpannableString {
        if (items.isEmpty() || items.size == 1) {
            return SpannableString("")
        }

        val indicatorText = items.indices.joinToString("") { _ -> "⬤" }
        val spannableString = SpannableString(indicatorText)
        val indicatorColor = ThemeManager.getWidgetColors(context, widgetTheme).secondaryColor.toColor()
        val colorWithOpacity = Color.argb(
            0.5f,
            indicatorColor.red(),
            indicatorColor.green(),
            indicatorColor.blue()
        )

        // Dim all indicators before current
        if (currentIndex > 0) {
            spannableString.setSpan(
                ForegroundColorSpan(colorWithOpacity),
                0,
                currentIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Dim all indicators after current
        if (currentIndex < indicatorText.length - 1) {
            spannableString.setSpan(
                ForegroundColorSpan(colorWithOpacity),
                currentIndex + 1,
                indicatorText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }

    companion object {
        /**
         * Factory method to create an EventSwiper
         */
        fun forEvents(
            context: Context,
            events: List<Event>,
            widgetId: Int
        ): WidgetSwiper<Event> {
            return WidgetSwiper(
                context = context,
                items = events,
                widgetId = widgetId,
                preferenceName = "EventSwiper"
            )
        }

        /**
         * Factory method to create a CalendarSwiper
         */
        fun forCalendar(
            context: Context,
            events: List<Event>,
            limit: Int = 5
        ): WidgetSwiper<Event> {
            val filteredEvents = events
                .filter { it.eventEndTime.time > System.currentTimeMillis() }
                .sortedBy { it.eventStartTime }
                .take(limit)

            return WidgetSwiper(
                context = context,
                items = filteredEvents,
                widgetId = 0, // Calendar widget doesn't use widgetId for preferences
                preferenceName = "CalendarSwiper"
            )
        }
    }
}