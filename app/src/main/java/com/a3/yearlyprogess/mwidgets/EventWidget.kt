package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.a3.yearlyprogess.R

/**
 * Implementation of App Widget functionality.
 */
class EventWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    // Construct the RemoteViews object
    val smallView = RemoteViews(context.packageName, R.layout.event_widget_small)
    val mediumView = RemoteViews(context.packageName, R.layout.event_widget_medium)
    val viewMapping: Map<SizeF, RemoteViews> = mapOf(
        SizeF(150f, 100f) to smallView,
        SizeF(150f, 200f) to mediumView,
    )
    val remoteViews = RemoteViews(viewMapping)

    val spannable = SpannableString("40.48%")
    spannable.setSpan(
        RelativeSizeSpan(2f),
        0,
        2,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    mediumView.setTextViewText(R.id.eventProgressText, spannable)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}