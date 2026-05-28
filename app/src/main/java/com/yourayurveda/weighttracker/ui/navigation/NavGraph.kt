package com.yourayurveda.weighttracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.navDeepLink
import com.yourayurveda.weighttracker.AppContainer
import com.yourayurveda.weighttracker.ui.goals.GoalsScreen
import com.yourayurveda.weighttracker.ui.goals.GoalsViewModel
import com.yourayurveda.weighttracker.ui.history.HistoryScreen
import com.yourayurveda.weighttracker.ui.history.HistoryViewModel
import com.yourayurveda.weighttracker.ui.progress.ProgressScreen
import com.yourayurveda.weighttracker.ui.progress.ProgressViewModel
import com.yourayurveda.weighttracker.ui.today.TodayScreen
import com.yourayurveda.weighttracker.ui.today.TodayViewModel

private sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    data object Today : BottomTab("today", "Today", Icons.Default.Today)
    data object History : BottomTab("history", "History", Icons.Default.History)
    data object Progress : BottomTab("progress", "Progress", Icons.Default.BarChart)
    data object Goals : BottomTab("goals", "Goals", Icons.Default.Settings)
}

private val tabs = listOf(
    BottomTab.Today, BottomTab.History, BottomTab.Progress, BottomTab.Goals
)

@Composable
fun AppNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = BottomTab.Today.route,
                deepLinks = listOf(navDeepLink { uriPattern = "weighttracker://today" })
            ) {
                val vm: TodayViewModel = viewModel(factory = TodayViewModel.factory(container))
                TodayScreen(vm, snackbarHostState)
            }
            composable(BottomTab.History.route) {
                val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
                HistoryScreen(vm)
            }
            composable(BottomTab.Progress.route) {
                val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(container))
                ProgressScreen(vm)
            }
            composable(BottomTab.Goals.route) {
                val vm: GoalsViewModel = viewModel(factory = GoalsViewModel.factory(container))
                GoalsScreen(vm)
            }
        }
    }
}
