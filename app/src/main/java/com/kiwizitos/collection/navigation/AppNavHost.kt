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
import com.kiwizitos.collection.presentation.view.AuthScreen
import com.kiwizitos.collection.presentation.view.CoversScreen
import com.kiwizitos.collection.presentation.view.DetailsScreen
import com.kiwizitos.collection.presentation.view.HomeScreen
import com.kiwizitos.collection.presentation.view.LibraryScreen
import com.kiwizitos.collection.presentation.view.ProfileScreen
import com.kiwizitos.collection.presentation.view.SearchScreen
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel

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
    authViewModel: AuthViewModel,
    galleryViewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in topLevelRoutes

    // Determina tela inicial: Auth se não logado, Home se logado
    val startDestination = if (authViewModel.isLoggedIn) AppRoute.Home.route
                           else AppRoute.Auth.route

    Scaffold(
        modifier  = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.fillMaxSize()
        ) {
            // ── Auth — fora da bottom bar ─────────────────────────────────────
            composable(AppRoute.Auth.route) {
                AuthScreen(
                    viewModel    = authViewModel,
                    onAuthSuccess = {
                        // Carrega galeria após login e vai para Home
                        authViewModel.currentUserId()?.let { galleryViewModel.loadGallery(it) }
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Rotas de nível raiz: recebem o padding da bottom bar ──────────
            composable(AppRoute.Home.route) {
                HomeScreen(
                    navController    = navController,
                    galleryViewModel = galleryViewModel,
                    modifier         = Modifier.padding(innerPadding)
                )
            }
            composable(AppRoute.Search.route) {
                SearchScreen(
                    navController    = navController,
                    galleryViewModel = galleryViewModel,
                    modifier         = Modifier.padding(innerPadding)
                )
            }
            composable(AppRoute.Profile.route) {
                ProfileScreen(
                    authViewModel    = authViewModel,
                    galleryViewModel = galleryViewModel,
                    modifier         = Modifier.padding(innerPadding),
                    onSignOut        = {
                        navController.navigate(AppRoute.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Library.route) {
                LibraryScreen(modifier = Modifier.padding(innerPadding))
            }

            // ── Telas de detalhe: têm Scaffold próprio, ignoram innerPadding ──
            composable(
                route = AppRoute.SeriesCovers.route,
                arguments = listOf(
                    navArgument("seriesUrl")   { type = NavType.StringType },
                    navArgument("seriesTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUrl   = backStackEntry.arguments?.getString("seriesUrl")   ?: return@composable
                val encodedTitle = backStackEntry.arguments?.getString("seriesTitle") ?: return@composable
                CoversScreen(
                    navController      = navController,
                    encodedSeriesUrl   = encodedUrl,
                    encodedSeriesTitle = encodedTitle,
                    galleryViewModel   = galleryViewModel
                )
            }

            composable(
                route = AppRoute.EditionDetail.route,
                arguments = listOf(
                    navArgument("editionUrl")     { type = NavType.StringType },
                    navArgument("editionTitle")   { type = NavType.StringType },
                    navArgument("seriesUrl")      { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("seriesTitle")    { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("showSeriesCard") { type = NavType.BoolType;   defaultValue = true }
                )
            ) { backStackEntry ->
                val editionUrl     = backStackEntry.arguments?.getString("editionUrl")    ?: return@composable
                val editionTitle   = backStackEntry.arguments?.getString("editionTitle")  ?: return@composable
                val seriesUrl      = backStackEntry.arguments?.getString("seriesUrl")
                val seriesTitle    = backStackEntry.arguments?.getString("seriesTitle")
                val showSeriesCard = backStackEntry.arguments?.getBoolean("showSeriesCard") ?: true
                DetailsScreen(
                    navController       = navController,
                    encodedEditionUrl   = editionUrl,
                    encodedEditionTitle = editionTitle,
                    encodedSeriesUrl    = seriesUrl,
                    encodedSeriesTitle  = seriesTitle,
                    showSeriesCard      = showSeriesCard,
                    onBackClick         = { navController.popBackStack() },
                    galleryViewModel    = galleryViewModel
                )
            }
        }
    }
}

