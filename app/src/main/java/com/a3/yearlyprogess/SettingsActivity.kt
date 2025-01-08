package com.a3.yearlyprogess

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.a3.yearlyprogess.widgets.manager.updateManager.services.WidgetUpdateBroadcastReceiver
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SettingsActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_activity)
    enableEdgeToEdge()
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
    val appBarLayout: AppBarLayout = findViewById(R.id.appBarLayout)

    ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
      val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(top = insets.top)
      WindowInsetsCompat.CONSUMED
    }

    toolbar.setNavigationOnClickListener { finish() }
  }

  class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey)

      val updateFrequencyPreference =
          findPreference<Preference>(getString(R.string.widget_widget_update_frequency))
      val defaultUpdateFrequencyPreferenceSummary =
          getString(R.string.adjust_widget_frequency_summary)

      updatePreferenceSummary(updateFrequencyPreference, defaultUpdateFrequencyPreferenceSummary) {
          value ->
        (value as? Int ?: 5).toDuration(DurationUnit.SECONDS).toString()
      }

      val notificationPref =
          findPreference<Preference>(getString(R.string.progress_show_notification))
      notificationPref?.setOnPreferenceChangeListener { _, newValue ->
        if (newValue == true) {
          val notificationHelper = YearlyProgressNotification(requireContext())
          if (!notificationHelper.hasAppNotificationPermission()) {
            notificationHelper.requestNotificationPermission(requireActivity())
            return@setOnPreferenceChangeListener false
          }
        }
        val widgetUpdateServiceIntent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)
        context?.sendBroadcast(widgetUpdateServiceIntent)
        true
      }
    }

    private fun updatePreferenceSummary(
        preference: Preference?,
        defaultSummary: String,
        formatValue: (Any?) -> String
    ) {
      preference?.let {
        val currentValue = it.sharedPreferences?.all?.get(it.key) ?: return
        it.summary =
            "$defaultSummary\n" +
                getString(R.string.current_value_settings, formatValue(currentValue))
        it.setOnPreferenceChangeListener { pref, newValue ->
          pref.summary =
              "$defaultSummary\n" +
                  getString(R.string.current_value_settings, formatValue(newValue))
          true
        }
      }
    }
  }
}
