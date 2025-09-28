package com.a3.yearlyprogess.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.a3.yearlyprogess.app.navigation.AppNavGraph
import com.a3.yearlyprogess.app.navigation.AppNavigationRail
import com.a3.yearlyprogess.app.navigation.BottomNavigationBar
import com.a3.yearlyprogess.app.navigation.Destination
import com.a3.yearlyprogess.app.navigation.appNavItems
import com.a3.yearlyprogess.app.ui.AppTopBar
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
        setContent {
            YearlyProgressTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(activity = this@MainActivity)
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentTitle = appNavItems.firstOrNull { item ->
                    currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                }?.label ?: "Yearly Progress"

                when (windowSizeClass.widthSizeClass) {
                    WindowWidthSizeClass.Compact -> {
                        // Phone
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            topBar = { AppTopBar(title = currentTitle, scrollBehavior = scrollBehavior) },
                            bottomBar = {
                                BottomNavigationBar(navController)
                            }, contentWindowInsets = WindowInsets.safeDrawing
                        ) { innerPadding ->
                            AppNavGraph(navController, Modifier.padding(innerPadding))
                        }
                    }
                    else -> {
                        // Foldable / Tablet
                        Row(
                            modifier = Modifier
                                    .fillMaxSize(),
                            ) {
                                AppNavigationRail(navController)
                            Scaffold(
                                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                                topBar = { AppTopBar(title = currentTitle,scrollBehavior = scrollBehavior) },
                                contentWindowInsets = WindowInsets.safeDrawing
                            ) { innerPadding ->
                                AppNavGraph(
                                    navController, modifier = Modifier.padding(
                                        PaddingValues(
                                            top = innerPadding.calculateTopPadding(),
                                            bottom = innerPadding.calculateBottomPadding()
                                        )
                                    )
                                )
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