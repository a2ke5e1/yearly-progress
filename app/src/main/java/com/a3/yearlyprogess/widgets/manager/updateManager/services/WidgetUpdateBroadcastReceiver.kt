package com.a3.yearlyprogess.widgets.manager.updateManager.services

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import com.a3.yearlyprogess.YearlyProgressNotification
import com.a3.yearlyprogess.cacheSunriseSunset
import com.a3.yearlyprogess.data.SunriseSunsetApi
import com.a3.yearlyprogess.loadCachedLocation
import com.a3.yearlyprogess.loadCachedSunriseSunset
import com.a3.yearlyprogess.provideSunriseSunsetApi
import com.a3.yearlyprogess.screens.UserLocationPref
import com.a3.yearlyprogess.widgets.manager.updateManager.WidgetUpdateAlarmHandler
import com.a3.yearlyprogess.widgets.ui.AllInWidget
import com.a3.yearlyprogess.widgets.ui.CalendarWidget
import com.a3.yearlyprogess.widgets.ui.DayLightWidget
import com.a3.yearlyprogess.widgets.ui.DayWidget
import com.a3.yearlyprogess.widgets.ui.EventWidget
import com.a3.yearlyprogess.widgets.ui.MonthWidget
import com.a3.yearlyprogess.widgets.ui.NightLightWidget
import com.a3.yearlyprogess.widgets.ui.WeekWidget
import com.a3.yearlyprogess.widgets.ui.YearWidget
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
            WeekWidget::class.java,
            MonthWidget::class.java,
            YearWidget::class.java,
            AllInWidget::class.java,
            EventWidget::class.java,
            DayLightWidget::class.java,
            NightLightWidget::class.java,
            CalendarWidget::class.java)

    // force widget update

    var totalWidgetCount = 0

    widgetIntentsAndComponents.forEach {
      val widgetIntent = Intent(context, it)
      widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
      val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, it))
      widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
      context.sendBroadcast(widgetIntent)

      if (it == DayLightWidget::class.java || it == NightLightWidget::class.java) {
        CoroutineScope(Dispatchers.IO).launch {
          val userLocationPref = UserLocationPref.load(context)
          val location =
              if (userLocationPref.automaticallyDetectLocation) {
                loadCachedLocation(context)
              } else {
                userLocationPref.userLocationPref?.let { place ->
                  Location("").apply {
                    latitude = place.lat.toDouble()
                    longitude = place.lon.toDouble()
                  }
                }
              }

          // Log.d("WUBR", location.toString())
          if (location == null) {
            cancel()
            return@launch
          }

          val cal = Calendar.getInstance()
          cal.timeInMillis = System.currentTimeMillis()

          val currentDate =
              StringBuilder("")
                  .append(cal.get(Calendar.YEAR))
                  .append("-")
                  .append(if (cal.get(Calendar.MONTH) + 1 < 10) "0" else "")
                  .append(cal.get(Calendar.MONTH) + 1)
                  .append("-")
                  .append(cal.get(Calendar.DATE))
                  .toString()

          val sunriseSunset = loadCachedSunriseSunset(context)
          if (sunriseSunset == null || sunriseSunset.results[1].date != currentDate) {
            val sunriseSunsetApi: SunriseSunsetApi = provideSunriseSunsetApi()

            cal.add(Calendar.DATE, -1)
            val startDateRange =
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${
                                cal.get(
                                    Calendar.DATE,
                                )
                            }"
            cal.add(Calendar.DATE, 2)
            val endDateRange =
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${
                                cal.get(
                                    Calendar.DATE,
                                )
                            }"

            try {
              val response =
                  sunriseSunsetApi.getSunriseSunset(
                      location.latitude,
                      location.longitude,
                      startDateRange,
                      endDateRange,
                  )
              val result = response.body()
              if (response.isSuccessful && result != null && result.status == "OK") {
                cacheSunriseSunset(context, result)
              }
            } catch (ex: Exception) {
              // Log.d("WUBR", ex.message.toString())
            } finally {
              cancel()
            }
          }
        }
      }

      totalWidgetCount += ids.size
    }

    // Cancels the alarm if there are no widgets
    // just in case.
    if (totalWidgetCount == 0) {
      WidgetUpdateAlarmHandler(context).cancelAlarmManager()
    }

    val yearlyProgressNotification = YearlyProgressNotification(context)

    if (yearlyProgressNotification.hasNotificationPermission()) {
      val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
      widgetUpdateAlarmHandler.cancelAlarmManager()
      widgetUpdateAlarmHandler.setAlarmManager()
      yearlyProgressNotification.showProgressNotification()
    } else {
      yearlyProgressNotification.hideProgressNotification()
    }

    // go back to sleep
    // WakeLocker.release()
  }
}
