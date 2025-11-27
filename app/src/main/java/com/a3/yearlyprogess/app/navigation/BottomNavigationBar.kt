package com.a3.yearlyprogess.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
            { Icon(Icons.Rounded.BarChart, contentDescription = "Progress") },
            { Icon(Icons.Rounded.BarChart, contentDescription = "Progress") }
        ),
        BottomNavItem(
            Destination.Widgets,
            stringResource(R.string.widgets),
            { Icon(Icons.Rounded.Widgets, contentDescription = "Widgets") },
            { Icon(Icons.Rounded.Widgets, contentDescription = "Widgets") }
        ),
        BottomNavItem(
            Destination.Events,
            stringResource(R.string.events),
            { Icon(Icons.Filled.Event, contentDescription = "Events") },
            { Icon(Icons.Filled.Event, contentDescription = "Events") }
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

    NavigationBar {
        items.forEach { item ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { if (isSelected) item.selectedIcon() else item.icon() },
                label = { Text(item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}

