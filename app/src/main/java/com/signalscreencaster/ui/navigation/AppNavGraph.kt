package com.signalscreencaster.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.signalscreencaster.ui.home.HomeScreen
import com.signalscreencaster.ui.profiles.ProfilesScreen
import com.signalscreencaster.ui.settings.SettingsScreen

object Routes {
    const val HOME     = "home"
    const val SETTINGS = "settings"
    const val PROFILES = "profiles"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateProfiles = { navController.navigate(Routes.PROFILES) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILES) {
            ProfilesScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
