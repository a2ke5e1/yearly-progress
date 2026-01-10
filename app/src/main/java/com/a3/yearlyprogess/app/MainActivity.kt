package com.a3.yearlyprogess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.app.navigation.AppNavGraph
import com.a3.yearlyprogess.app.navigation.Destination
import com.a3.yearlyprogess.core.backup.BackupManager
import com.a3.yearlyprogess.core.backup.RoomBackupHelper
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.events.data.local.EventDatabase
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
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

            YearlyProgressTheme {
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

                        AppNavGraph(
                            navController = navController,
                            windowWidthSizeClass = windowSizeClass.widthSizeClass,
                            backupManager = backupManager,
                            startDestination = startDestination,
                            mainViewModel = viewModel,
                            onWelcomeCompleted = {
                                navController.navigate(Destination.MainFlow) {
                                    popUpTo(Destination.Welcome) { inclusive = true }
                                }
                                viewModel.onWelcomeCompleted()
                                viewModel.consentManager.gatherConsent(this) { error ->
                                    if (error != null) {
                                        Log.e("MainActivity", "Consent gathering failed: ${error.message}")
                                    }
                                    if (viewModel.consentManager.canRequestAds()) {
                                        initializeMobileAds()
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