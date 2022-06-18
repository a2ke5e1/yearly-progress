package com.a3.yearlyprogess.manager

import android.content.Intent
import android.app.PendingIntent
import android.app.AlarmManager
import android.content.Context
import com.a3.yearlyprogess.manager.services.DayWidgetService
import com.a3.yearlyprogess.manager.services.MonthWidgetService
import com.a3.yearlyprogess.manager.services.WeekWidgetService
import com.a3.yearlyprogess.manager.services.YearWidgetService
import java.util.*

class AlarmHandler(private val context: Context, private val service : Int) {

    private val intent = when (service) {
        DAY_WIDGET_SERVICE ->  Intent(context, DayWidgetService::class.java)
        MONTH_WIDGET_SERVICE ->  Intent(context, MonthWidgetService::class.java)
        WEEK_WIDGET_SERVICE ->  Intent(context, WeekWidgetService::class.java)
        YEAR_WIDGET_SERVICE ->  Intent(context, YearWidgetService::class.java)
        else -> Intent(context, DayWidgetService::class.java)
    }

    fun setAlarmManager() {
        val sender = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //get current time and add 1.5 seconds
        val c = Calendar.getInstance()
        val l = c.timeInMillis + 1500

        //set the alarm for 10 seconds in the future
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, l, sender)
    }

    fun cancelAlarmManager() {
        val sender: PendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(sender)
    }

    companion object {
        const val DAY_WIDGET_SERVICE = 500
        const val MONTH_WIDGET_SERVICE = 501
        const val WEEK_WIDGET_SERVICE = 502
        const val YEAR_WIDGET_SERVICE = 503
    }
}