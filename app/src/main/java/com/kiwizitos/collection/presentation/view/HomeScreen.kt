package com.kiwizitos.collection.presentation.view

import android.R.drawable.ic_menu_gallery
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModelFactory
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.components.card.ContentCardStyle
import com.kiwizitos.siege.components.card.ContentType
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.card.StatCard
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.components.layout.SiegeList
import com.kiwizitos.siege.components.layout.SiegeListStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    galleryViewModel: GalleryViewModel = viewModel(factory = GalleryViewModelFactory())
) {
    val galleryMap by galleryViewModel.galleryMap.collectAsState()
    val seriesMap  by galleryViewModel.seriesMap.collectAsState()

    // ── Estatísticas ──────────────────────────────────────────────────────────
    val totalSeries   = seriesMap.size
    val totalEditions = galleryMap.size

    val editionsFull  by galleryViewModel.editionsFull.collectAsState()
    val lendoEditions = editionsFull.values.filter { it.readStatus == ReadStatus.LENDO }
    val savedSeries   = ArrayList<UserSeries>(seriesMap.values)

    LazyColumn(
        modifier            = modifier
            .fillMaxSize()
            .padding(SiegeSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium)
    ) {
        // ── Saudação ──────────────────────────────────────────────────────────
        item {
            SiegeText(text = "Minha biblioteca", style = SiegeTextStyle.Headline)
        }

        // ── Stats ─────────────────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
            ) {
                StatCard(
                    label       = "Séries salvas",
                    value       = "$totalSeries",
                    accentColor = SiegeColors.AccentPink,
                    modifier    = Modifier.weight(1f)
                )
                StatCard(
                    label       = "Volumes salvos",
                    value       = "$totalEditions",
                    accentColor = SiegeColors.AccentCyan,
                    modifier    = Modifier.weight(1f)
                )
            }
        }

        // ── Continue lendo ────────────────────────────────────────────────────
        if (lendoEditions.isNotEmpty()) {
            item {
                SiegeList(
                    items  = lendoEditions,
                    style  = SiegeListStyle.Horizontal,
                    header = {
                        SiegeText(text = "Continue lendo", style = SiegeTextStyle.Body)
                    }
                ) { edition ->
                    val coverPainter = if (!edition.coverUrl.isNullOrBlank())
                        rememberAsyncImagePainter(edition.coverUrl)
                    else
                        painterResource(ic_menu_gallery)

                    val badges = buildList {
                        edition.ownership?.let  { add(BadgeData(it.displayLabel.uppercase(), it.badgeColor)) }
                        edition.readStatus?.let { add(BadgeData(it.displayLabel.uppercase(), it.badgeColor)) }
                    }

                    SiegeContentCell(
                        coverImage  = coverPainter,
                        title       = edition.title,
                        style       = ContentCardStyle.Cover,
                        contentType = ContentType.Volume,
                        subtitle    = edition.seriesTitle,
                        badges      = badges,
                        onClick     = {
                            navController.navigate(
                                AppRoute.EditionDetail.createRoute(
                                    editionUrl   = navEncode(edition.guiaUrl),
                                    editionTitle = navEncode(edition.title),
                                    seriesUrl    = edition.seriesUrl?.let { navEncode(it) } ?: "",
                                    seriesTitle  = edition.seriesTitle?.let { navEncode(it) } ?: ""
                                )
                            )
                        }
                    )
                }
            }
        }

        // ── Minhas séries ─────────────────────────────────────────────────────
        if (savedSeries.isNotEmpty()) {
            item {
                SiegeList(
                    items  = savedSeries,
                    style  = SiegeListStyle.Grid(2),
                    header = {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            SiegeText(text = "Minhas séries", style = SiegeTextStyle.Body)
                        }
                    }
                ) { series: UserSeries ->
                    val coverPainter = if (!series.coverUrl.isNullOrBlank())
                        rememberAsyncImagePainter(series.coverUrl)
                    else
                        painterResource(ic_menu_gallery)

                    SiegeContentCell(
                        coverImage  = coverPainter,
                        title       = series.seriesTitle,
                        style       = ContentCardStyle.Grid,
                        contentType = ContentType.Series,
                        subtitle    = series.publisher,
                        onClick     = {
                            navController.navigate(
                                AppRoute.SeriesCovers.createRoute(
                                    navEncode(series.seriesUrl),
                                    navEncode(series.seriesTitle)
                                )
                            )
                        }
                    )
                }
            }
        }

        // ── Estado vazio ──────────────────────────────────────────────────────
        if (savedSeries.isEmpty() && editionsFull.isEmpty()) {
            item {
                SiegeText(
                    text     = "Sua biblioteca está vazia.\nBusque séries ou edições para começar!",
                    style    = SiegeTextStyle.Body,
                    color    = SiegeTheme.colors.textTertiary,
                    modifier = Modifier.fillMaxWidth().padding(vertical = SiegeSpacing.XLarge)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SiegeTheme { HomeScreen(navController = rememberNavController()) }
}