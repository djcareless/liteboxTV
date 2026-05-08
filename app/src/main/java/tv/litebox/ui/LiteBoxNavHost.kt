package tv.litebox.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tv.litebox.domain.model.ThemeManifest
import tv.litebox.theme.LiteBoxTheme
import tv.litebox.ui.home.HomeScreen
import tv.litebox.ui.library.LibraryScreen
import tv.litebox.ui.player.PlayerScreen
import tv.litebox.ui.plugins.PluginsScreen
import tv.litebox.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val LIBRARY = "library/{sourceType}"
    const val PLAYER = "player/{mediaId}"
    const val PLUGINS = "plugins"
    const val SETTINGS = "settings"

    fun library(type: String) = "library/$type"
    fun player(mediaId: String) = "player/$mediaId"
}

@Composable
fun LiteBoxNavHost(theme: ThemeManifest) {
    LiteBoxTheme(theme = theme) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToLibrary = { type -> navController.navigate(Routes.library(type)) },
                    onNavigateToPlayer = { id -> navController.navigate(Routes.player(id)) },
                    onNavigateToPlugins = { navController.navigate(Routes.PLUGINS) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }

            composable(
                route = Routes.LIBRARY,
                arguments = listOf(navArgument("sourceType") { type = NavType.StringType }),
            ) { backStack ->
                LibraryScreen(
                    sourceType = backStack.arguments?.getString("sourceType") ?: "ALL",
                    onNavigateToPlayer = { id -> navController.navigate(Routes.player(id)) },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.PLAYER,
                arguments = listOf(navArgument("mediaId") { type = NavType.StringType }),
            ) { backStack ->
                PlayerScreen(
                    mediaId = backStack.arguments?.getString("mediaId") ?: "",
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.PLUGINS) {
                PluginsScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
