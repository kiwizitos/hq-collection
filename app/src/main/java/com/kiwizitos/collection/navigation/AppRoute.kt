
package com.kiwizitos.collection.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppRoute(val route: String) {
    // ── Bottom Bar ────────────────────────────────────────────────────────────
    data object Home    : AppRoute("home")
    data object Search  : AppRoute("search")
    data object Library : AppRoute("library")
    data object Profile : AppRoute("profile")

    // ── Detail screens ────────────────────────────────────────────────────────
    /** Detalhe de coleção. Rota: "collection/{id}" */
    data object CollectionDetail : AppRoute("collection/{id}") {
        fun createRoute(id: String) = "collection/$id"
    }

    /** Detalhe de edição individual. Rota: "collection/{collectionId}/edition/{editionId}" */
    data object EditionDetail : AppRoute("collection/{collectionId}/edition/{editionId}") {
        fun createRoute(collectionId: String, editionId: String) =
            "collection/$collectionId/edition/$editionId"
    }
}

// ── Content type ──────────────────────────────────────────────────────────────
// ContentType vive em com.kiwizitos.siege.components.card.ContentType.
// Use-o diretamente nos composables — não redefina aqui.

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = AppRoute.Home.route,
        label = "Home",
        icon = Icons.Filled.Home
    ),
    BottomNavItem(
        route = AppRoute.Search.route,
        label = "Adicionar",
        icon = Icons.Filled.AddCircle
    ),
    BottomNavItem(
        route = AppRoute.Library.route,
        label = "Biblioteca",
        icon = Icons.AutoMirrored.Filled.List
    ),
    BottomNavItem(
        route = AppRoute.Profile.route,
        label = "Perfil",
        icon = Icons.Filled.AccountCircle
    )
)

