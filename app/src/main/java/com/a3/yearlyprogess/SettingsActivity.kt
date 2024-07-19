package com.a3.yearlyprogess

import android.icu.text.NumberFormat
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val appBarLayout: AppBarLayout = findViewById(R.id.appBarLayout)


        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            WindowInsetsCompat.CONSUMED
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val updateFrequencyPreference =
                findPreference<Preference>(getString(R.string.widget_widget_update_frequency))
            val defaultUpdateFrequencyPreferenceSummary = getString(R.string.adjust_widget_frequency_summary)
            val updateFPCurrentValue = updateFrequencyPreference?.sharedPreferences?.getInt(
                updateFrequencyPreference.key, 5) ?: 5
            val formattedValue = updateFPCurrentValue.toDuration(DurationUnit.SECONDS).toString()
            updateFrequencyPreference?.summary = "$defaultUpdateFrequencyPreferenceSummary\nCurrent value: $formattedValue"
            updateFrequencyPreference?.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue.toString().toInt()
                val formattedValue = value.toDuration(DurationUnit.SECONDS).toString()
                preference.summary = "$defaultUpdateFrequencyPreferenceSummary\nCurrent value: $formattedValue"
                true
            }
        }
    }
}