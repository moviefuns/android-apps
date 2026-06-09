package com.brbrs.bookmarks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brbrs.bookmarks.auth.AuthViewModel
import com.brbrs.bookmarks.ui.screens.applock.AppLockScreen
import com.brbrs.bookmarks.ui.screens.edit.EditBookmarkScreen
import com.brbrs.bookmarks.ui.screens.list.BookmarkListScreen
import com.brbrs.bookmarks.ui.screens.login.LoginScreen
import com.brbrs.bookmarks.ui.screens.settings.SettingsScreen

object Routes {
    const val APP_LOCK = "app_lock"
    const val LOGIN    = "login"
    const val LIST     = "list"
    const val EDIT     = "edit/{bookmarkId}"
    const val ADD      = "edit/-1"
    const val SETTINGS = "settings"

    fun edit(id: Long) = "edit/$id"
}

@Composable
fun BookmarksNavHost(
    sharedUrl:   String? = null,
    sharedTitle: String? = null,
) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.authState.collectAsState()

    val startDest = when {
        !authState.isLoggedIn      -> Routes.LOGIN
        authState.biometricEnabled -> Routes.APP_LOCK
        else                       -> Routes.LIST
    }

    // If the app was opened via a share intent and the user is already logged in,
    // navigate straight to the add screen once the nav graph is ready.
    LaunchedEffect(sharedUrl, authState.isLoggedIn) {
        if (sharedUrl != null && authState.isLoggedIn && !authState.biometricEnabled) {
            navController.navigate(Routes.ADD)
        }
    }

    NavHost(navController = navController, startDestination = startDest) {

        composable(Routes.APP_LOCK) {
            AppLockScreen(
                onUnlocked = {
                    val dest = if (sharedUrl != null) Routes.ADD else Routes.LIST
                    navController.navigate(dest) {
                        popUpTo(Routes.APP_LOCK) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LIST) {
            BookmarkListScreen(
                onAdd      = { navController.navigate(Routes.ADD) },
                onEdit     = { id -> navController.navigate(Routes.edit(id)) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route     = Routes.EDIT,
            arguments = listOf(navArgument("bookmarkId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookmarkId = backStackEntry.arguments?.getLong("bookmarkId") ?: -1L
            EditBookmarkScreen(
                // Pass shared data only when opening the add screen (-1 = new)
                prefillUrl   = if (bookmarkId == -1L) sharedUrl else null,
                prefillTitle = if (bookmarkId == -1L) sharedTitle else null,
                onSaved      = { navController.popBackStack() },
                onDismiss    = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
