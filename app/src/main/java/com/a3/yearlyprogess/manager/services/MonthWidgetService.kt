package com.a3.yearlyprogess.manager.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.mWidgets.MonthWidget

class MonthWidgetService : BaseWidgetService() {

    private val widgetClass = MonthWidget::class.java

    override fun setIntent(context: Context): Intent {
        return Intent(context, widgetClass)
    }

    override fun setComponent(context: Context): ComponentName {
        return ComponentName(context, widgetClass)
    }
}