package com.a3.yearlyprogess.manager

import android.content.Intent
import android.app.PendingIntent
import android.app.AlarmManager
import android.content.Context
import com.a3.yearlyprogess.manager.services.DayWidgetService
import java.util.*

class AlarmHandler(private val context: Context) {
    fun setAlarmManager() {
        val intent = Intent(context, DayWidgetService::class.java)
        val sender = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //get current time and add 10 seconds
        val c = Calendar.getInstance()
        val l = c.timeInMillis + 1500

        //set the alarm for 10 seconds in the future
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, l, sender)
    }

    fun cancelAlarmManager() {
        val intent = Intent(context, DayWidgetService::class.java)
        val sender: PendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(sender)
    }
}