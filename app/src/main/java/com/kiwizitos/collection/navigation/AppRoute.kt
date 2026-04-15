package com.kiwizitos.collection.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Encoding seguro para segmentos de rota e query params do Navigation Compose.
 *
 * Usa [Uri.encode] em vez de [java.net.URLEncoder] porque:
 * - `URLEncoder` converte `/` em `%2F` mas o Navigation faz decode antes de
 *   comparar com o pattern, quebrando rotas que contêm barras.
 * - `Uri.encode` usa percent-encoding RFC 3986 que o Navigation respeita
 *   corretamente em segmentos de path e query params.
 */
fun navEncode(value: String): String = Uri.encode(value)
fun navDecode(value: String): String = Uri.decode(value)

sealed class AppRoute(val route: String) {
    // ── Auth ──────────────────────────────────────────────────────────────────
    data object Auth : AppRoute("auth")

    // ── Bottom Bar ────────────────────────────────────────────────────────────
    data object Home : AppRoute("home")
    data object Search : AppRoute("search")
    data object Library : AppRoute("library")
    data object Profile : AppRoute("profile")

    // ── Detail screens ────────────────────────────────────────────────────────

    /**
     * Detalhe de uma edição individual.
     *
     * Rota base: `edition/{editionUrl}/{editionTitle}`
     *
     * Query parameters opcionais para contexto de série:
     * - `seriesUrl`   → URL da série à qual esta edição pertence.
     * - `seriesTitle` → Título da série, para exibição no card de contexto.
     *
     * Quando `seriesUrl` e `seriesTitle` estão presentes, a tela de detalhe
     * exibe o card "Pertence ao título X" com link para a lista de capas.
     * Quando ausentes, a tela é exibida sem esse card.
     *
     * Todos os parâmetros devem ser codificados com [navEncode] antes de
     * criar a rota, pois URLs de edição/série contêm `/` que quebrariam
     * o parsing do Navigation se não forem corretamente encoded.
     */
    data object EditionDetail : AppRoute(
        "edition/{editionUrl}/{editionTitle}?seriesUrl={seriesUrl}&seriesTitle={seriesTitle}&showSeriesCard={showSeriesCard}"
    ) {
        /** Vindo do CoversScreen — suprime o card de série. */
        fun createRoute(
            editionUrl: String,
            editionTitle: String
        ) = "edition/$editionUrl/$editionTitle?showSeriesCard=false"

        /** Vindo de qualquer outra origem — exibe o card de série. */
        fun createRoute(
            editionUrl: String,
            editionTitle: String,
            seriesUrl: String,
            seriesTitle: String
        ) =
            "edition/$editionUrl/$editionTitle?seriesUrl=$seriesUrl&seriesTitle=$seriesTitle&showSeriesCard=true"
    }

    /**
     * Lista de capas de uma série. Rota: "covers/{seriesUrl}/{seriesTitle}"
     *
     * Os parâmetros devem ser codificados com [navEncode] antes de criar a rota.
     */
    data object SeriesCovers : AppRoute("covers/{seriesUrl}/{seriesTitle}") {
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

