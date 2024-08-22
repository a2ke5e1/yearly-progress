package com.a3.yearlyprogess.widgets.manager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.widgets.ui.StandaloneWidget
import com.a3.yearlyprogess.widgets.ui.StandaloneWidgetOptions
import com.a3.yearlyprogess.widgets.ui.WidgetShape
import com.google.android.material.button.MaterialButton

class StandaloneWidgetStyle : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_standalone_widget_style)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    val appWidgetId =
        intent
            ?.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }
    findViewById<MaterialButton>(R.id.rectBtn).setOnClickListener {
      val options =
          StandaloneWidgetOptions.load(this, appWidgetId).copy(shape = WidgetShape.RECTANGLE)
      options.save(this)

      val views = StandaloneWidget.standaloneWidgetRemoteView(this, options)
      val appWidgetManager = AppWidgetManager.getInstance(this)
      appWidgetManager.updateAppWidget(appWidgetId, views)

      val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(Activity.RESULT_OK, resultValue)
      finish()
    }
    findViewById<MaterialButton>(R.id.cloverBtn).setOnClickListener {
      val options = StandaloneWidgetOptions.load(this, appWidgetId).copy(shape = WidgetShape.CLOVER)
      options.save(this)

      val views = StandaloneWidget.standaloneWidgetRemoteView(this, options)
      val appWidgetManager = AppWidgetManager.getInstance(this)
      appWidgetManager.updateAppWidget(appWidgetId, views)

      val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(Activity.RESULT_OK, resultValue)
      finish()
    }
  }
}
