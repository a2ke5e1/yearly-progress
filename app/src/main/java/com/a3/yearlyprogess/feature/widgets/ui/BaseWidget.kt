package com.a3.yearlyprogess.feature.widgets.ui


import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateAlarmHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseWidget : AppWidgetProvider() {


    /**
     * Synchronous widget update - called immediately
     * Override this for simple, quick updates
     */
    abstract fun updateWidget(
        context: Context,
        appWidgetId: Int,
    ): RemoteViews

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled - setting AlarmManager")

        val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
        widgetUpdateAlarmHandler.setAlarmManager()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.d(TAG, "onUpdate called with ids: ${appWidgetIds.joinToString()}")

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        Log.d(TAG, "Options changed for widget $appWidgetId")

        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    // Checks if any other widgets are active.
    // If not, cancels the alarm.
    override fun onDisabled(context: Context) {
        Log.d(TAG, "All widgets disabled - checking if AlarmManager should be cancelled")
        updateJob?.cancel()

        val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)

        val widgetIntentsAndComponents = arrayOf(
            DayWidget::class.java,
            WeekWidget::class.java,
            MonthWidget::class.java,
            YearWidget::class.java,
        )
        var totalWidgets = 0
        widgetIntentsAndComponents.forEach {
            val ids =
                AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, it))
            totalWidgets += ids.size
        }

        if (totalWidgets > 0) {
            Log.d(TAG, "canceling alarm shutdown, $totalWidgets still active")
            return
        }

        widgetUpdateAlarmHandler.cancelAlarmManager()
    }

    /**
     * Internal update function that handles both sync and async updates
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        Log.d(TAG, "Starting update loop for widget $appWidgetId")
        updateJob?.cancel()

        // Launch coroutine for async updates
        updateJob = CoroutineScope(Dispatchers.IO).launch {

            var counter = 0
            while (true) {
                counter++
                Log.d(TAG, "Updating widget $appWidgetId, iteration $counter")

                val views = updateWidget(context, appWidgetId)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                delay(1000L)
            }
        }


        Log.d(TAG, "Resetting AlarmManager after update loop start for widget $appWidgetId")


        // Reset alarm for next update
        val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
        widgetUpdateAlarmHandler.cancelAlarmManager()
        widgetUpdateAlarmHandler.setAlarmManager()
    }

    companion object {
        private const val TAG = "BaseWidget"
        private var updateJob: Job? = null

    }
}
