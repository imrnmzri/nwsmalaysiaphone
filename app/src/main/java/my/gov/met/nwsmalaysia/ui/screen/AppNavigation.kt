package my.gov.met.nwsmalaysia.ui.screen

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import my.gov.met.nwsmalaysia.domain.model.Warning

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object WarningDetail : Screen("warning_detail")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var selectedWarning by remember { mutableStateOf<Warning?>(null) }

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onWarningDetailClick = { warning ->
                    selectedWarning = warning
                    navController.navigate(Screen.WarningDetail.route)
                }
            )
        }

        composable(Screen.WarningDetail.route) {
            val warning = selectedWarning
            if (warning != null) {
                WarningDetailScreen(
                    warning = warning,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
