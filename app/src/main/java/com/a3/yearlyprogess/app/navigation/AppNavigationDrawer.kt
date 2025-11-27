package com.a3.yearlyprogess.app.navigation

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppNavigationDrawer(
    navController: NavHostController,
    items: List<BottomNavItem> = getBottomNavItems(),
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                items.forEach { item ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { if (isSelected) item.selectedIcon() else item.icon() }
                    )
                }
            }
        },
        content = content
    )
}
