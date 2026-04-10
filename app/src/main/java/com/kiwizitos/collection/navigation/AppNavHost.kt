package com.kiwizitos.collection.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.kiwizitos.collection.presentation.view.CoversScreen
import com.kiwizitos.collection.presentation.view.DetailsScreen
import com.kiwizitos.collection.presentation.view.HomeScreen
import com.kiwizitos.collection.presentation.view.LibraryScreen
import com.kiwizitos.collection.presentation.view.ProfileScreen
import com.kiwizitos.collection.presentation.view.SearchScreen

/** Rotas de nível raiz que exibem a bottom bar. */
private val topLevelRoutes = setOf(
    AppRoute.Home.route,
    AppRoute.Search.route,
    AppRoute.Library.route,
    AppRoute.Profile.route
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            // Sem padding aqui — cada tela gerencia seus próprios insets
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Rotas de nível raiz: recebem o padding da bottom bar ──────────
            composable(AppRoute.Home.route) {
                HomeScreen(navController = navController, modifier = Modifier.padding(innerPadding))
            }
            composable(AppRoute.Search.route) {
                SearchScreen(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(AppRoute.Profile.route) {
                ProfileScreen(modifier = Modifier.padding(innerPadding))
            }
            composable(AppRoute.Library.route) {
                LibraryScreen(modifier = Modifier.padding(innerPadding))
            }

            // ── Telas de detalhe: têm Scaffold próprio, ignoram innerPadding ──
            composable(
                route = AppRoute.SeriesCovers.route,
                arguments = listOf(
                    navArgument("seriesUrl") { type = NavType.StringType },
                    navArgument("seriesTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUrl =
                    backStackEntry.arguments?.getString("seriesUrl") ?: return@composable
                val encodedTitle =
                    backStackEntry.arguments?.getString("seriesTitle") ?: return@composable
                CoversScreen(
                    navController = navController,
                    encodedSeriesUrl = encodedUrl,
                    encodedSeriesTitle = encodedTitle
                )
            }

            composable(
                route = AppRoute.EditionDetail.route,
                arguments = listOf(
                    navArgument("editionUrl") { type = NavType.StringType },
                    navArgument("editionTitle") { type = NavType.StringType },
                    navArgument("seriesUrl") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("seriesTitle") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("showSeriesCard") { type = NavType.BoolType; defaultValue = true }
                )
            ) { backStackEntry ->
                val editionUrl =
                    backStackEntry.arguments?.getString("editionUrl") ?: return@composable
                val editionTitle =
                    backStackEntry.arguments?.getString("editionTitle") ?: return@composable
                val seriesUrl = backStackEntry.arguments?.getString("seriesUrl")
                val seriesTitle = backStackEntry.arguments?.getString("seriesTitle")
                val showSeriesCard = backStackEntry.arguments?.getBoolean("showSeriesCard") ?: true
                DetailsScreen(
                    navController = navController,
                    encodedEditionUrl = editionUrl,
                    encodedEditionTitle = editionTitle,
                    encodedSeriesUrl = seriesUrl,
                    encodedSeriesTitle = seriesTitle,
                    showSeriesCard = showSeriesCard,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
