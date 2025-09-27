package com.a3.yearlyprogess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.app.navigation.AppNavGraph
import com.a3.yearlyprogess.app.navigation.AppNavigationRail
import com.a3.yearlyprogess.app.navigation.BottomNavigationBar
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
        setContent {
            YearlyProgressTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(activity = this@MainActivity)

                when (windowSizeClass.widthSizeClass) {
                    WindowWidthSizeClass.Compact -> {
                        // Phone
                        Scaffold(
                            bottomBar = { BottomNavigationBar(navController) }
                        ) { innerPadding ->
                            AppNavGraph(navController, Modifier.padding(innerPadding))
                        }
                    }
                    else -> {
                        // Foldable / Tablet
                        Scaffold { innerPadding ->
                            Row(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                            ) {
                                AppNavigationRail(navController)
                                AppNavGraph(navController)
                            }
                        }
                    }
//                    WindowWidthSizeClass.Expanded -> {
//                        // Desktop
//                        AppNavigationDrawer(navController) {
//                            AppNavGraph(navController, Modifier.fillMaxSize())
//                        }
//                    }
                }
            }
        }
    }
}