package com.a3.yearlyprogess

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.a3.yearlyprogess.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val FIRST_LAUNCH = "first_launch"
    private val YEARLY_PROGRESS_PREF = "yearly_progress_pref"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        installSplashScreen()


        val pref = this.getSharedPreferences(YEARLY_PROGRESS_PREF, MODE_PRIVATE)
        val firstLaunch  = pref.getBoolean(FIRST_LAUNCH, true)

        /*
        *
        *  Checks if user is agreed to terms and conditions and privacy policy.
        *
        * */
        if (firstLaunch) {
            startActivity(
                Intent(
                    this,
                    FirstScreen::class.java
                )
            )
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)



        MobileAds.initialize(this) {}

        window.navigationBarDividerColor =
            ContextCompat.getColor(this, android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            WindowInsetsCompat.CONSUMED
        }


        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label.toString()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.infoMenu -> showInfoMenu()
            R.id.shareMenu -> showShareScreen()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInfoMenu(): Boolean {
        // Creates a dialog box to show info about the app
        MaterialAlertDialogBuilder(this, R.style.CentralCard)
            .setTitle(getString(R.string.app_name))
            .setMessage(
                HtmlCompat.fromHtml(
                    """
                          Version: ${BuildConfig.VERSION_NAME}<br>                            
                          Build: ${BuildConfig.BUILD_TYPE.uppercase()}<br>
                          <br><br>
                          An <b>A3 Group</b> Product
                        """.trimIndent(), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setNeutralButton(
                "Dismiss", null
            )
            .setIcon(R.mipmap.ic_launcher_round)
            .show()
        return true
    }

    private fun showShareScreen(): Boolean {
        // Launch share if users want to share app with their friends
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        val shareMessage =
            """
            Check out ${getString(R.string.app_name)} (https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} )
            You can have awesome widgets to see progress of day, month and year with material you support.
            """.trimIndent()
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        this.startActivity(Intent.createChooser(intent, "Share"))
        return true
    }

}