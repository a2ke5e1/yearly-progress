package com.a3.yearlyprogess.feature.widgets.update

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.YearlyProgressNotification
import com.a3.yearlyprogess.feature.widgets.ui.AllInWidget
import com.a3.yearlyprogess.feature.widgets.ui.CalendarWidget
import com.a3.yearlyprogess.feature.widgets.ui.DayLightWidget
import com.a3.yearlyprogess.feature.widgets.ui.DayWidget
import com.a3.yearlyprogess.feature.widgets.ui.EventWidget
import com.a3.yearlyprogess.feature.widgets.ui.MonthWidget
import com.a3.yearlyprogess.feature.widgets.ui.NightLightWidget
import com.a3.yearlyprogess.feature.widgets.ui.WeekWidget
import com.a3.yearlyprogess.feature.widgets.ui.YearWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetUpdateBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var yearlyProgressNotification: YearlyProgressNotification

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.d(TAG, "Widget update broadcast received")

        // Use goAsync() to ensure we have time to complete async work
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        val forceNotificationState = if (intent.hasExtra(EXTRA_FORCE_NOTIFICATION)) {
            intent.getBooleanExtra(EXTRA_FORCE_NOTIFICATION, false)
        } else {
            null
        }

        scope.launch {
            try {
                // Update all widgets
                val totalWidgetCount = updateAllWidgets(context)
                Log.d(TAG, "Updated $totalWidgetCount widgets")

                // Get current settings (use first() instead of collect to avoid hanging)
                val settings = appSettingsRepository.appSettings.first()
                // 3. Apply the override if it exists
                val updatedSettings = if (forceNotificationState != null) {
                    settings.copy(
                        notificationSettings = settings.notificationSettings.copy(
                            progressShowNotification = forceNotificationState
                        )
                    )
                } else {
                    settings
                }

                // Update notification
                yearlyProgressNotification.showProgressNotification(updatedSettings)

                // Cancel alarm if no widgets are active and notification is disabled
                if (totalWidgetCount == 0 && !updatedSettings.notificationSettings.progressShowNotification) {
                    Log.d(TAG, "No widgets active and notification disabled - canceling alarm")
                    WidgetUpdateAlarmHandler(context).cancelAlarmManager()
                } else {
                    // Reschedule next update
                    WidgetUpdateAlarmHandler(context).setAlarmManager()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during widget update", e)
            } finally {
                // Signal that we're done
                pendingResult.finish()
                scope.cancel()
            }
        }
    }

    private fun updateAllWidgets(context: Context): Int {
        val widgetIntentsAndComponents = arrayOf(
            DayWidget::class.java,
            DayLightWidget::class.java,
            NightLightWidget::class.java,
            WeekWidget::class.java,
            MonthWidget::class.java,
            YearWidget::class.java,
            EventWidget::class.java,
            CalendarWidget::class.java,
            AllInWidget::class.java
        )

        var totalWidgetCount = 0

        widgetIntentsAndComponents.forEach { widgetClass ->
            val widgetIntent = Intent(context, widgetClass)
            widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, widgetClass))

            if (ids.isNotEmpty()) {
                widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                context.sendBroadcast(widgetIntent)
                totalWidgetCount += ids.size
            }
        }

        return totalWidgetCount
    }

    companion object {
        private const val TAG = "WidgetUpdateReceiver"
        const val EXTRA_FORCE_NOTIFICATION = "EXTRA_FORCE_NOTIFICATION"
    }
}