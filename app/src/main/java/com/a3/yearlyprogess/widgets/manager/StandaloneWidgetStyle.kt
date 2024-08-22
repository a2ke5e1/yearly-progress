package com.a3.yearlyprogess.widgets.manager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.ActivityStandaloneWidgetStyleBinding
import com.a3.yearlyprogess.widgets.ui.StandaloneWidget
import com.a3.yearlyprogess.widgets.ui.StandaloneWidgetOptions
import com.a3.yearlyprogess.widgets.ui.StandaloneWidgetOptions.Companion.WidgetShape
import com.google.android.material.card.MaterialCardView

class StandaloneWidgetStyle : AppCompatActivity() {

  private var _binding: ActivityStandaloneWidgetStyleBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityStandaloneWidgetStyleBinding.inflate(layoutInflater)
    setContentView(binding.root)
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

    var options = StandaloneWidgetOptions.load(this, appWidgetId)

    binding.leftCounter.isChecked = options.timeLeftCounter
    binding.dynamicLeftCounter.isChecked = options.dynamicLeftCounter
    binding.backgroundSlider.value = options.backgroundTransparency.toFloat()
    binding.decimalSlider.value = options.decimalPlaces.toFloat()
    binding.replaceCounter.isChecked = options.replaceProgressWithDaysLeft

    inflateRemoteViewPreview(
        binding.rectBtn, binding.rectContainer, options.copy(shape = WidgetShape.RECTANGLE))
    inflateRemoteViewPreview(
        binding.cloverBtn, binding.cloverContainer, options.copy(shape = WidgetShape.CLOVER))

    binding.replaceCounter.setOnCheckedChangeListener { _, isChecked ->
      options = options.copy(replaceProgressWithDaysLeft = isChecked)
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, replaceProgressWithDaysLeft = isChecked))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, replaceProgressWithDaysLeft = isChecked))
    }

    binding.decimalSlider.addOnChangeListener { _, value, _ ->
      options = options.copy(decimalPlaces = value.toInt())
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, decimalPlaces = value.toInt()))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, decimalPlaces = value.toInt()))
    }

    binding.leftCounter.setOnCheckedChangeListener { _, isChecked ->
      options = options.copy(timeLeftCounter = isChecked)
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, timeLeftCounter = isChecked))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, timeLeftCounter = isChecked))
    }

    binding.dynamicLeftCounter.setOnCheckedChangeListener { _, isChecked ->
      options = options.copy(dynamicLeftCounter = isChecked)
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, dynamicLeftCounter = isChecked))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, dynamicLeftCounter = isChecked))
    }

    binding.backgroundSlider.addOnChangeListener { _, value, _ ->
      options = options.copy(backgroundTransparency = (value).toInt())
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(
              shape = WidgetShape.RECTANGLE, backgroundTransparency = (value * 100).toInt()))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, backgroundTransparency = (value * 100).toInt()))
    }
  }

  private fun inflateRemoteViewPreview(
      parent: MaterialCardView,
      container: FrameLayout,
      options: StandaloneWidgetOptions
  ) {
    val views =
        StandaloneWidget.standaloneWidgetRemoteView(applicationContext, options)
            .apply(applicationContext, container)
    parent.setOnClickListener {
      options.save(this)
      Log.d("StandaloneWidgetStyle", "onCreate: $options")
      applyOptions(options)
    }
    views.findViewById<FrameLayout>(R.id.background).setOnClickListener {
      options.save(this)
      Log.d("StandaloneWidgetStyle", "onCreate: $options")
      applyOptions(options)
    }
    container.removeAllViews()
    container.addView(views)
  }

  private fun applyOptions(options: StandaloneWidgetOptions) {
    val views = StandaloneWidget.standaloneWidgetRemoteView(this, options)
    val appWidgetManager = AppWidgetManager.getInstance(this)
    appWidgetManager.updateAppWidget(options.widgetId, views)
    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, options.widgetId)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }
}
