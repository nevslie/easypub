package io.github.nevslie.easypub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.nevslie.easypub.ui.libraries.LibrariesScreen
import io.github.nevslie.easypub.ui.library.LibraryScreen
import io.github.nevslie.easypub.ui.library.LibraryViewModel
import io.github.nevslie.easypub.ui.settings.SettingsScreen

private object Routes {
    const val LIBRARIES = "libraries"
    const val LIBRARY = "library/{${LibraryViewModel.ARG_LIBRARY_ID}}"
    const val SETTINGS = "settings"

    fun library(id: Long) = "library/$id"
}

/**
 * Navigation zwischen den Screens. Der Reader ist eine eigene Activity
 * (siehe [io.github.nevslie.easypub.ui.reader.ReaderActivity]).
 */
@Composable
fun EasyPubNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIBRARIES) {
        composable(Routes.LIBRARIES) {
            LibrariesScreen(
                onOpenLibrary = { navController.navigate(Routes.library(it)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.LIBRARY,
            arguments = listOf(navArgument(LibraryViewModel.ARG_LIBRARY_ID) { type = NavType.LongType }),
        ) {
            LibraryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
