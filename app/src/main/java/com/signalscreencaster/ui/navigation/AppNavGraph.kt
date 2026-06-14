package com.castIRL.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

private val TAB_ORDER = listOf(Routes.HOME, Routes.PROFILES, Routes.SETTINGS, Routes.CHANGELOG)
private val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private const val MS = 380

/** +1 when moving to a later tab, -1 to an earlier one. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.dirSign(): Int {
    val from = TAB_ORDER.indexOf(initialState.destination.route)
    val to   = TAB_ORDER.indexOf(targetState.destination.route)
    return if (to >= from) 1 else -1
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Shared-axis X: incoming slides a short distance from the contextual
        // side while fading in; outgoing fades out toward the opposite side.
        NavHost(
            navController    = navController,
            startDestination = Routes.HOME,
            enterTransition  = {
                slideInHorizontally(tween(MS, easing = Emphasized)) { full -> dirSign() * full / 10 } +
                    fadeIn(tween(MS / 2))
            },
            exitTransition   = {
                slideOutHorizontally(tween(MS, easing = Emphasized)) { full -> -dirSign() * full / 10 } +
                    fadeOut(tween(MS / 2))
            },
            popEnterTransition = {
                slideInHorizontally(tween(MS, easing = Emphasized)) { full -> dirSign() * full / 10 } +
                    fadeIn(tween(MS / 2))
            },
            popExitTransition = {
                slideOutHorizontally(tween(MS, easing = Emphasized)) { full -> -dirSign() * full / 10 } +
                    fadeOut(tween(MS / 2))
            },
        ) {
            composable(Routes.HOME)      { HomeScreen() }
            composable(Routes.PROFILES)  { ProfilesScreen() }
            composable(Routes.SETTINGS)  { SettingsScreen() }
            composable(Routes.CHANGELOG) { ChangelogScreen() }
        }

        // Stationary floating nav bar — overlaid, does NOT move with the slide.
        CastIRLBottomBar(
            currentRoute = currentRoute,
            onNavigate   = onNavigate,
            modifier     = Modifier.align(Alignment.BottomCenter),
        )
    }
}
