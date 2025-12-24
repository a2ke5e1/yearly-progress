package com.a3.yearlyprogess.feature.widgets.update

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.feature.widgets.ui.CalendarWidget
import com.a3.yearlyprogess.feature.widgets.ui.DayLightWidget
import com.a3.yearlyprogess.feature.widgets.ui.DayWidget
import com.a3.yearlyprogess.feature.widgets.ui.EventWidget
import com.a3.yearlyprogess.feature.widgets.ui.MonthWidget
import com.a3.yearlyprogess.feature.widgets.ui.NightLightWidget
import com.a3.yearlyprogess.feature.widgets.ui.WeekWidget
import com.a3.yearlyprogess.feature.widgets.ui.YearWidget

class WidgetUpdateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        // wake the device
        // WakeLocker.acquire(context)
        val widgetIntentsAndComponents =
            arrayOf(
                DayWidget::class.java,
                DayLightWidget::class.java,
                NightLightWidget::class.java,
                WeekWidget::class.java,
                MonthWidget::class.java,
                YearWidget::class.java,
                EventWidget::class.java,
                CalendarWidget::class.java,
                )
        // force widget update

        var totalWidgetCount = 0

        widgetIntentsAndComponents.forEach {
            val widgetIntent = Intent(context, it)
            widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids =
                AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, it))
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(widgetIntent)


            totalWidgetCount += ids.size
        }

        // Cancels the alarm if there are no widgets
        // just in case.
        if (totalWidgetCount == 0) {
            WidgetUpdateAlarmHandler(context).cancelAlarmManager()
        }


    }
}
