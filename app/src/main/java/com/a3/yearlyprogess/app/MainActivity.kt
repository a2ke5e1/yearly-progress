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
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.core.util.Log
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep the splash screen visible until settings are loaded to prevent white flicker
        splashScreen.setKeepOnScreenCondition {
            viewModel.isFirstLaunch.value == null
        }

        enableEdgeToEdge()

        setContent {
            val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()

            YearlyProgressTheme {
                // Ensure we have a background surface immediately to prevent white flashes
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Wait for settings to load
                    if (isFirstLaunch != null) {
                        val navController = rememberNavController()
                        val windowSizeClass = calculateWindowSizeClass(this)
                        
                        // Lock the start destination so it doesn't change when isFirstLaunch updates.
                        // This prevents double navigation/animation issues.
                        val startDestination = remember {
                            if (isFirstLaunch == true) {
                                Destination.Welcome
                            } else {
                                Destination.MainFlow
                            }
                        }

                        AppNavGraph(
                            navController = navController,
                            windowWidthSizeClass = windowSizeClass.widthSizeClass,
                            startDestination = startDestination,
                            onWelcomeCompleted = {
                                // Show consent form after welcome screen
                                viewModel.consentManager.gatherConsent(this) { error ->
                                    if (error != null) {
                                        Log.e("MainActivity", "Consent gathering failed: ${error.message}")
                                    }
                                    if (viewModel.consentManager.canRequestAds()) {
                                        initializeMobileAds()
                                    }
                                    // Mark welcome as completed
                                    viewModel.onWelcomeCompleted()
                                    
                                    // Navigate to main flow explicitly
                                    navController.navigate(Destination.MainFlow) {
                                        popUpTo(Destination.Welcome) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Gather consent on every launch if not first launch
        val isFirstLaunchValue = viewModel.isFirstLaunch.value
        if (isFirstLaunchValue == false) {
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
