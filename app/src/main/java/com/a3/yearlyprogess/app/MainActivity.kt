package com.a3.yearlyprogess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.app.navigation.AppNavGraph
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            YearlyProgressTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)
                AppNavGraph(
                    navController = navController,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }

    }
}
