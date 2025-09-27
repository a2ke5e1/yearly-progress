package com.a3.yearlyprogess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.app.navigation.AppNavGraph
import com.a3.yearlyprogess.app.navigation.AppNavigationDrawer
import com.a3.yearlyprogess.app.navigation.AppNavigationRail
import com.a3.yearlyprogess.app.navigation.BottomNavigationBar
import com.a3.yearlyprogess.feature.home.ui.components.ProgressCard
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.home.ui.HomeScreen
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
                            Row(Modifier.fillMaxSize()) {
                                AppNavigationRail(navController)
                                AppNavGraph(navController, Modifier.padding(innerPadding))
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