package com.a3.yearlyprogess.app.navigation

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.a3.yearlyprogess.R

data class BottomNavItem(
    val route: Destination,
    val label: String,
    val icon: @Composable () -> Unit,
    val selectedIcon: @Composable () -> Unit
)

@Composable
fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            Destination.Home,
            stringResource(R.string.progress),
            { Icon(Icons.Rounded.BarChart, contentDescription = stringResource(R.string.progress)) },
            { Icon(Icons.Rounded.BarChart, contentDescription = stringResource(R.string.progress)) }
        ),
        BottomNavItem(
            Destination.Widgets,
            stringResource(R.string.widgets),
            { Icon(Icons.Rounded.Widgets, contentDescription = stringResource(R.string.widgets)) },
            { Icon(Icons.Rounded.Widgets, contentDescription = stringResource(R.string.widgets)) }
        ),
        BottomNavItem(
            Destination.Events,
            stringResource(R.string.events),
            { Icon(Icons.Filled.Event, contentDescription = stringResource(R.string.events)) },
            { Icon(Icons.Filled.Event, contentDescription =  stringResource(R.string.events)) }
        ),
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem> = getBottomNavItems()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val view = LocalView.current

    NavigationBar {
        items.forEach { item ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        // Play system click sound and perform haptic feedback
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { if (isSelected) item.selectedIcon() else item.icon() },
                label = { Text(item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}
