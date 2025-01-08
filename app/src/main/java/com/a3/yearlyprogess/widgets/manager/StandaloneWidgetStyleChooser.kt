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
import androidx.core.view.updatePadding
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.ActivityStandaloneWidgetStyleBinding
import com.a3.yearlyprogess.widgets.ui.StandaloneWidget
import com.a3.yearlyprogess.widgets.ui.StandaloneWidgetOptions
import com.a3.yearlyprogess.widgets.ui.StandaloneWidgetOptions.Companion.WidgetShape
import com.google.android.material.card.MaterialCardView

class StandaloneWidgetStyleChooser : AppCompatActivity() {

  private var _binding: ActivityStandaloneWidgetStyleBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityStandaloneWidgetStyleBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, windowInsets ->
      val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(top = insets.top, left = insets.left, right = insets.right)
      WindowInsetsCompat.CONSUMED
    }

    setSupportActionBar(binding.toolbar)
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

    binding.toolbar.title =
        getString(
            R.string.standalone_widget_style_chooser_title_lable,
            options.widgetType?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
    binding.leftCounter.isChecked = options.timeLeftCounter
    binding.dynamicLeftCounter.isChecked = options.dynamicLeftCounter
    binding.backgroundSlider.value = options.backgroundTransparency.toFloat()
    binding.decimalSlider.value = options.decimalPlaces.toFloat()
    binding.replaceCounter.isChecked = options.replaceProgressWithDaysLeft

    inflateRemoteViewPreview(
        binding.rectBtn, binding.rectContainer, options.copy(shape = WidgetShape.RECTANGLE))
    inflateRemoteViewPreview(
        binding.cloverBtn, binding.cloverContainer, options.copy(shape = WidgetShape.CLOVER))
    inflateRemoteViewPreview(
        binding.pillBtn, binding.pillContainer, options.copy(shape = WidgetShape.PILL))

    binding.rectBtn.strokeWidth =
        if (options.shape == WidgetShape.RECTANGLE) WIDGET_SELECTOR_STROKE_WIDTH else 0
    binding.cloverBtn.strokeWidth =
        if (options.shape == WidgetShape.CLOVER) WIDGET_SELECTOR_STROKE_WIDTH else 0
    binding.pillBtn.strokeWidth =
        if (options.shape == WidgetShape.PILL) WIDGET_SELECTOR_STROKE_WIDTH else 0

    binding.replaceCounter.setOnCheckedChangeListener { _, isChecked ->
      options = options.copy(replaceProgressWithDaysLeft = isChecked)
      options.save(this)

      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, replaceProgressWithDaysLeft = isChecked))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, replaceProgressWithDaysLeft = isChecked))
      inflateRemoteViewPreview(
          binding.pillBtn,
          binding.pillContainer,
          options.copy(shape = WidgetShape.PILL, replaceProgressWithDaysLeft = isChecked))
    }

    binding.decimalSlider.addOnChangeListener { _, value, _ ->
      options = options.copy(decimalPlaces = value.toInt())
      options.save(this)
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, decimalPlaces = value.toInt()))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, decimalPlaces = value.toInt()))
      inflateRemoteViewPreview(
          binding.pillBtn,
          binding.pillContainer,
          options.copy(shape = WidgetShape.PILL, decimalPlaces = value.toInt()))
    }

    binding.leftCounter.setOnCheckedChangeListener { _, isChecked ->
      options = options.copy(timeLeftCounter = isChecked)
      options.save(this)

      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, timeLeftCounter = isChecked))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, timeLeftCounter = isChecked))
      inflateRemoteViewPreview(
          binding.pillBtn,
          binding.pillContainer,
          options.copy(shape = WidgetShape.PILL, timeLeftCounter = isChecked))
    }

    binding.dynamicLeftCounter.setOnCheckedChangeListener { _, isChecked ->
      options = options.copy(dynamicLeftCounter = isChecked)
      if (isChecked) {
        options = options.copy(timeLeftCounter = true)
        binding.leftCounter.isChecked = true
      }
      options.save(this)
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, dynamicLeftCounter = isChecked))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, dynamicLeftCounter = isChecked))
      inflateRemoteViewPreview(
          binding.pillBtn,
          binding.pillContainer,
          options.copy(shape = WidgetShape.PILL, dynamicLeftCounter = isChecked))
    }

    binding.backgroundSlider.addOnChangeListener { _, value, _ ->
      options = options.copy(backgroundTransparency = (value).toInt())
      options.save(this)
      inflateRemoteViewPreview(
          binding.rectBtn,
          binding.rectContainer,
          options.copy(shape = WidgetShape.RECTANGLE, backgroundTransparency = (value).toInt()))
      inflateRemoteViewPreview(
          binding.cloverBtn,
          binding.cloverContainer,
          options.copy(shape = WidgetShape.CLOVER, backgroundTransparency = (value).toInt()))
      inflateRemoteViewPreview(
          binding.pillBtn,
          binding.pillContainer,
          options.copy(shape = WidgetShape.PILL, backgroundTransparency = (value).toInt()))
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

  companion object {
    private const val WIDGET_SELECTOR_STROKE_WIDTH = 4
  }
}
