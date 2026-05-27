package com.castIRL.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.castIRL.ui.changelog.ChangelogScreen
import com.castIRL.ui.home.HomeScreen
import com.castIRL.ui.profiles.ProfilesScreen
import com.castIRL.ui.settings.SettingsScreen

object Routes {
    const val HOME      = "home"
    const val SETTINGS  = "settings"
    const val PROFILES  = "profiles"
    const val CHANGELOG = "changelog"
}

private val spatialSpring = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness    = Spring.StiffnessMedium
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Routes.HOME,
        // Forward: new screen slides in from right, current slides left
        enterTransition  = {
            slideInHorizontally(spatialSpring) { it } +
            fadeIn(tween(180))
        },
        exitTransition   = {
            slideOutHorizontally(spatialSpring) { -it / 4 } +
            fadeOut(tween(120))
        },
        // Back: previous screen slides in from left, current exits right
        popEnterTransition = {
            slideInHorizontally(spatialSpring) { -it / 4 } +
            fadeIn(tween(180))
        },
        popExitTransition = {
            slideOutHorizontally(spatialSpring) { it } +
            fadeOut(tween(120))
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateSettings  = { navController.navigate(Routes.SETTINGS) },
                onNavigateProfiles  = { navController.navigate(Routes.PROFILES) },
                onNavigateChangelog = { navController.navigate(Routes.CHANGELOG) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILES) {
            ProfilesScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.CHANGELOG) {
            ChangelogScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
