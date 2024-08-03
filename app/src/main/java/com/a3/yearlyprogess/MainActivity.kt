package com.a3.yearlyprogess

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.a3.yearlyprogess.databinding.ActivityMainBinding
import com.a3.yearlyprogess.components.dialogbox.AboutDialog
import com.a3.yearlyprogess.components.dialogbox.BackupRestoreDialog
import com.a3.yearlyprogess.widgets.manager.updateManager.services.WidgetUpdateBroadcastReceiver
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private var roomBackup: RoomBackup? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var consentInformation: ConsentInformation
    private var consentForm: ConsentForm? = null

    lateinit var billingManager: YearlyProgressSubscriptionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        DynamicColors.applyToActivityIfAvailable(this)

        val pref = this.getSharedPreferences(YEARLY_PROGRESS_PREF, MODE_PRIVATE)
        val firstLaunch = pref.getBoolean(FIRST_LAUNCH, true)

        /*
        *
        *  Checks if user is agreed to terms and conditions and privacy policy.
        *
        * */
        if (firstLaunch) {
            startActivity(
                Intent(
                    this,
                    WelcomeScreen::class.java
                )
            )
            finish()
        }

        billingManager = YearlyProgressSubscriptionManager(this)


        /*
        *   Loads Consent Form to EU
        * */
        // loads EU Consent Form
        thread {
            loadEUForm()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)



        MobileAds.initialize(this) {}
        roomBackup = RoomBackup(this)


        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label.toString()
        }


        // Start widget update service
        // It ensures that the widget is updated when the app is opened.
        val widgetUpdateServiceIntent = Intent(this, WidgetUpdateBroadcastReceiver::class.java)
        sendBroadcast(widgetUpdateServiceIntent)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)

        billingManager.shouldShowAds.observe(this) {
            when (it) {
                SubscriptionStatus.Subscribed -> {
                    menu.findItem(R.id.removeAds).title = resources.getString(R.string.manage_subscription)
                }
                else -> menu.findItem(R.id.removeAds).isVisible = true
            }
        }


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.infoMenu -> showInfoMenu()
            R.id.shareMenu -> showShareScreen()
            R.id.settings -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(
                    R.id.settingsActivity
                )
                true
            }

            R.id.backupRestoreMenu -> showBackupRestoreDialogBox()
            R.id.removeAds -> {
                billingManager.shouldShowAds.observe(this) {
                    when (it) {
                        SubscriptionStatus.Subscribed -> {
                            billingManager.redirectToSubscriptionPage()
                        }

                        else -> {
                            lifecycleScope.launch(Dispatchers.IO) {
                                billingManager.processPurchases()
                            }
                        }

                    }
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBackupRestoreDialogBox(): Boolean {
        val backupRestoreDialog = roomBackup?.let { BackupRestoreDialog(it) }
        backupRestoreDialog?.show(supportFragmentManager, "backup_restore_dialog_box")
        return true
    }

    private fun showInfoMenu(): Boolean {
        val aboutDialogBox = AboutDialog()
        aboutDialogBox.show(supportFragmentManager, "about_dialog_box")
        return true
    }

    private fun showShareScreen(): Boolean {
        // Launch share if users want to share app with their friends
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        val shareMessage = """
            Check out ${getString(R.string.app_name)} (https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID} )
            You can have awesome widgets to see progress of day, month and year with material you support.
            """.trimIndent()
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        this.startActivity(Intent.createChooser(intent, "Share"))
        return true
    }

    private fun loadEUForm() {

        /*val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("D8E90FB07673BFE0C11C8F378F64B61F")
            .build()*/

        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
            //.setConsentDebugSettings(debugSettings)
            .build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            if (consentInformation.isConsentFormAvailable) {
                loadForm();
            }
        }, {})
    }

    private fun loadForm() {
        UserMessagingPlatform.loadConsentForm(this, { consentForm ->
            this.consentForm = consentForm
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(
                    this
                ) { // Handle dismissal by reloading form.
                    loadForm()
                }
            }
        }) {}
    }


    companion object {

        const val FIRST_LAUNCH = "first_launch"
        const val YEARLY_PROGRESS_PREF = "yearly_progress_pref"
        const val TAG = "MainActivity"

    }

}