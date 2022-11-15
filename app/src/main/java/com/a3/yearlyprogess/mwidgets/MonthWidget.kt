package com.a3.yearlyprogess.mwidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.manager.AlarmHandler
import com.a3.yearlyprogess.mwidgets.util.StandaloneWidget
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class MonthWidget : StandaloneWidget(AlarmHandler.MONTH_WIDGET_SERVICE)

