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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(AppRoute.Search.route) {
                SearchScreen()
            }
            composable(AppRoute.Profile.route) {
                ProfileScreen()
            }
            composable(AppRoute.Library.route) {
                LibraryScreen()
            }

            // ── Detail screens (sem bottom bar) ───────────────────────────────
            composable(
                route = AppRoute.CollectionDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                // TODO: buscar dados reais via ViewModel usando [id]
                DetailsScreen(
                    title = id,
                    coverUrl = "",
                    technicalInfo = emptyList(),
                    onGuideButtonClick = {},
                    onBackClick = { navController.popBackStack() },
                    isCollection = true
                )
            }

            composable(
                route = AppRoute.EditionDetail.route,
                arguments = listOf(
                    navArgument("collectionId") { type = NavType.StringType },
                    navArgument("editionId")    { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId") ?: return@composable
                val editionId    = backStackEntry.arguments?.getString("editionId")    ?: return@composable
                // TODO: buscar dados reais via ViewModel usando [collectionId] e [editionId]
                DetailsScreen(
                    title = "$collectionId #$editionId",
                    coverUrl = "",
                    technicalInfo = emptyList(),
                    onGuideButtonClick = {},
                    onBackClick = { navController.popBackStack() },
                    isCollection = false
                )
            }
        }
    }
}
