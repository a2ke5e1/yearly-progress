package com.a3.yearlyprogess.widgets.manager.updateManager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.widgets.manager.updateManager.services.WidgetUpdateBroadcastReceiver
import java.util.*

class WidgetUpdateAlarmHandler(private val context: Context) {

    private val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
    private val intent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)
    private val service = 100

    fun setAlarmManager() {
        val sender = PendingIntent.getBroadcast(context, service, intent, PendingIntent.FLAG_IMMUTABLE)

        // minimum widget update time is 5 seconds
        val c = Calendar.getInstance()
        val l = c.timeInMillis + 5000

        //set the alarm for 2 seconds in the future
        am?.setAndAllowWhileIdle(AlarmManager.RTC, l, sender)
        // am.setInexactRepeating(AlarmManager.RTC_WAKEUP, l, 2000, sender)
    }

    fun cancelAlarmManager() {
        val sender: PendingIntent = PendingIntent.getBroadcast(context, service, intent, PendingIntent.FLAG_IMMUTABLE)
        am?.cancel(sender)
    }

    companion object {
        const val DAY_WIDGET_SERVICE = 500
        const val MONTH_WIDGET_SERVICE = 501
        const val WEEK_WIDGET_SERVICE = 502
        const val YEAR_WIDGET_SERVICE = 503
        const val EVENT_WIDGET_SERVICE = 504
        const val ALL_IN_WIDGET_SERVICE = 505
        private const val TAG = "AlarmHandler"
    }
}