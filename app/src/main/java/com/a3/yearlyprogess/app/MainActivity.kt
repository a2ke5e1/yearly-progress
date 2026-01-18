package com.a3.yearlyprogess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.navigation.AppNavGraph
import com.a3.yearlyprogess.app.navigation.Destination
import com.a3.yearlyprogess.core.backup.BackupManager
import com.a3.yearlyprogess.core.backup.RoomBackupHelper
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.events.data.local.EventDatabase
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var backupManager: BackupManager

    @Inject
    lateinit var database: EventDatabase

    private val viewModel: MainViewModel by viewModels()

    // Initialize RoomBackup early in the Activity lifecycle
    private lateinit var roomBackup: RoomBackup
    private lateinit var roomBackupHelper: RoomBackupHelper

    companion object {
        // Minimum delay to ensure smooth transition with scale+fade animations
        private const val TRANSITION_DELAY_MS = 300L
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize RoomBackup BEFORE the Activity is STARTED
        roomBackup = RoomBackup(this).apply {
            database(database)
            enableLogDebug(true)
            backupIsEncrypted(true)
            customEncryptPassword("haha idc if you forgot")
        }

        roomBackupHelper = RoomBackupHelper(this, roomBackup)

        // Set the helper in BackupManager
        backupManager.setRoomBackupHelper(roomBackupHelper)

        // Keep the splash screen visible until settings are loaded to prevent white flicker
        splashScreen.setKeepOnScreenCondition {
            viewModel.appSettings.value?.isFirstLaunch == null
        }

        enableEdgeToEdge()

        setContent {
            val appSettings by viewModel.appSettings.collectAsState()

            YearlyProgressTheme(
                appTheme = appSettings?.appTheme ?: WidgetTheme.DEFAULT
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (appSettings != null) {
                        val navController = rememberNavController()
                        val windowSizeClass = calculateWindowSizeClass(this)

                        val startDestination = remember {
                            if (appSettings?.isFirstLaunch == true) {
                                Destination.Welcome
                            } else {
                                Destination.MainFlow
                            }
                        }

                        if (viewModel.showMigrationDialog) {
                            AlertDialog(
                                onDismissRequest = { viewModel.onDismissMigration() },
                                title = { Text(stringResource(R.string.settings_migration_title)) },
                                text = { Text(stringResource(R.string.settings_migration_message)) },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.onMigrateSettings() }) {
                                        Text(stringResource(R.string.transfer_settings))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { viewModel.onDismissMigration() }) {
                                        Text(stringResource(R.string.not_now))
                                    }
                                }
                            )
                        }

                        AppNavGraph(
                            navController = navController,
                            windowWidthSizeClass = windowSizeClass.widthSizeClass,
                            backupManager = backupManager,
                            startDestination = startDestination,
                            mainViewModel = viewModel,
                            onWelcomeCompleted = {
                                lifecycleScope.launch {
                                    delay(TRANSITION_DELAY_MS)
                                    navController.navigate(Destination.MainFlow) {
                                        popUpTo(Destination.Welcome) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    viewModel.onWelcomeCompleted()
                                    viewModel.consentManager.gatherConsent(this@MainActivity) { error ->
                                        if (error != null) {
                                            Log.e("MainActivity", "Consent gathering failed: ${error.message}")
                                        }
                                        if (viewModel.consentManager.canRequestAds()) {
                                            initializeMobileAds()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (viewModel.appSettings.value?.isFirstLaunch == false) {
            viewModel.consentManager.gatherConsent(this) { error ->
                if (error != null) {
                    Log.e("MainActivity", "Consent gathering failed: ${error.message}")
                }
                if (viewModel.consentManager.canRequestAds()) {
                    initializeMobileAds()
                }
            }
        }
    }

    private fun initializeMobileAds() {
        Log.d("MainActivity", "Initializing Mobile Ads")
        MobileAds.initialize(this) {}
    }
}
