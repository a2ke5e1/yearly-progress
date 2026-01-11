package com.a3.yearlyprogess.core.util

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
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.model.NotificationSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YearlyProgressNotification @Inject constructor(
    @ApplicationContext private val context: Context
) {
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

    @SuppressLint("MissingPermission")
    fun showProgressNotification(appSettings: AppSettings) {
        val notificationSettings = appSettings.notificationSettings
        if (!notificationSettings.progressShowNotification || !hasAppNotificationPermission()) {
            hideProgressNotification()
            return
        }

        val yp = YearlyProgressUtil(appSettings.progressSettings)

        val yearProgress = yp.calculateProgress(TimePeriod.YEAR)
        val monthProgress = yp.calculateProgress(TimePeriod.MONTH)
        val weekProgress = yp.calculateProgress(TimePeriod.WEEK)
        val dayProgress = yp.calculateProgress(TimePeriod.DAY)

        val progressInfo = mutableListOf<String>()
        if (notificationSettings.progressShowNotificationDay) {
            progressInfo.add("Day: %.2f%%".format(dayProgress))
        }
        if (notificationSettings.progressShowNotificationWeek) {
            progressInfo.add("Week: %.2f%%".format(weekProgress))
        }
        if (notificationSettings.progressShowNotificationMonth) {
            progressInfo.add("Month: %.2f%%".format(monthProgress))
        }
        if (notificationSettings.progressShowNotificationYear) {
            progressInfo.add("Year: %.2f%%".format(yearProgress))
        }

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent =
            intent?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }

        val notification =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(progressInfo.joinWithAnd())
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

    private fun List<String>.joinWithAnd(): String =
        when (size) {
            0 -> ""
            1 -> this[0]
            2 -> "${this[0]} and ${this[1]}"
            else -> dropLast(1).joinToString(", ") + " and ${last()}"
        }
}