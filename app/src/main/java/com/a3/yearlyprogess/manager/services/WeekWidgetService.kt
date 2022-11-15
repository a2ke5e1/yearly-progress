package com.a3.yearlyprogess.manager.services

import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.mwidgets.WeekWidget

class WeekWidgetService : BaseWidgetService() {
    override fun setIntent(context: Context): Intent {
        return Intent(context, WeekWidget::class.java)
    }

}