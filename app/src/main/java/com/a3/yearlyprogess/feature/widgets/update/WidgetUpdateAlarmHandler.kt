package com.a3.yearlyprogess.feature.widgets.update

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class WidgetUpdateAlarmHandler(private val context: Context) {
  private val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
  private val intent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)
  private val service = 100

  fun setAlarmManager() {
    val sender = PendingIntent.getBroadcast(context, service, intent, PendingIntent.FLAG_IMMUTABLE)
    val c = Calendar.getInstance()
    val l = c.timeInMillis + (5 * 1000)
    am?.setAndAllowWhileIdle(AlarmManager.RTC, l, sender)
  }

  fun cancelAlarmManager() {
    val sender: PendingIntent =
        PendingIntent.getBroadcast(context, service, intent, PendingIntent.FLAG_IMMUTABLE)
    am?.cancel(sender)
  }

  companion object {
    private const val TAG = "AlarmHandler"
  }
}
