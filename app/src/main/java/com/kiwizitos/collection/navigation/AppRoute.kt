
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

    /** Detalhe de edição individual. Rota: "edition/{editionUrl}/{editionTitle}" */
    data object EditionDetail : AppRoute("edition/{editionUrl}/{editionTitle}") {
        /**
         * @param editionUrl   URL da edição já codificada com [URLEncoder].
         * @param editionTitle Título da edição já codificado com [URLEncoder].
         */
        fun createRoute(editionUrl: String, editionTitle: String) =
            "edition/$editionUrl/$editionTitle"
    }

    /**
     * Lista de capas de uma série. Rota: "covers/{seriesUrl}/{seriesTitle}"
     *
     * Os parâmetros devem ser codificados com [URLEncoder] antes de criar a rota,
     * pois URLs de série contêm `/` e `?` que quebrariam o parsing do Navigation.
     */
    data object SeriesCovers : AppRoute("covers/{seriesUrl}/{seriesTitle}") {
        /**
         * @param encodedSeriesUrl   URL da série codificada com [URLEncoder].
         * @param encodedSeriesTitle Título da série codificado com [URLEncoder].
         */
        fun createRoute(encodedSeriesUrl: String, encodedSeriesTitle: String) =
            "covers/$encodedSeriesUrl/$encodedSeriesTitle"
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

