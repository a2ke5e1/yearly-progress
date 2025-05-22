package com.a3.yearlyprogess

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager

class YearlyProgressNotification(private val context: Context) {
  private val channelId = "progress_channel"
  private val notificationId = 1

  init {
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    val name = "Progress Channel"
    val descriptionText = "Channel for progress notifications"
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel =
        NotificationChannel(channelId, name, importance).apply { description = descriptionText }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }

  fun hasAppNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
    }
    return true
  }

  fun hasNotificationPermission(): Boolean {
    val settingPref = PreferenceManager.getDefaultSharedPreferences(context)
    val userEnableNotification =
        settingPref.getBoolean(context.getString(R.string.progress_show_notification), false)
    return hasAppNotificationPermission() && userEnableNotification
  }

  @SuppressLint("MissingPermission")
  fun showProgressNotification() {
    if (!hasNotificationPermission()) {
      return
    }

    val yp = YearlyProgressUtil(context)

    val yearProgress = yp.calculateProgress(TimePeriod.YEAR)
    val monthProgress = yp.calculateProgress(TimePeriod.MONTH)
    val weekProgress = yp.calculateProgress(TimePeriod.WEEK)
    val dayProgress = yp.calculateProgress(TimePeriod.DAY)

    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pendingIntent =
        intent?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }

    val notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(
                "Day: %.2f%%, Week: %.2f%%, Month: %.2f%% and Year: %.2f%%"
                    .format(dayProgress, weekProgress, monthProgress, yearProgress),
            )
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    with(NotificationManagerCompat.from(context)) { notify(notificationId, notification) }
  }

  fun hideProgressNotification() {
    with(NotificationManagerCompat.from(context)) { cancel(notificationId) }
  }

  fun requestNotificationPermission(requireActivity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requireActivity.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
    }
  }
}
