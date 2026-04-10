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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.components.card.ContentCardStyle
import com.kiwizitos.siege.components.card.ContentType
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.card.StatCard
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.components.layout.SiegeList
import com.kiwizitos.siege.components.layout.SiegeListStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Dado de teste real ────────────────────────────────────────────────────────
// Edição: http://www.guiadosquadrinhos.com/edicao/x-men-2099-n-1/x-011158/179145
// Série:  http://www.guiadosquadrinhos.com/capas/x-men-2099/x-011158
private data class SeriesItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: ContentType = ContentType.Series,
    val seriesUrl: String,
    val latestEditionUrl: String
)

private val mockSeries = listOf(
    SeriesItem(
        id = "x-men-2099",
        title = "X-Men 2099",
        subtitle = "Marvel Comics",
        type = ContentType.Series,
        seriesUrl = "capas/x-men-2099/x-011158",
        latestEditionUrl = "edicao/x-men-2099-n-1/x-011158/179145"
    )
)

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(SiegeSpacing.Regular)
    ) {
        item {
            SiegeText(text = "Bem vindo, usuário", style = SiegeTextStyle.Headline)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SiegeSpacing.Medium),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = "Total de séries",
                    value = "42",
                    accentColor = SiegeColors.AccentPink,
                    modifier = Modifier
                        .weight(1f)
                        .padding(SiegeSpacing.XXSmall)
                )
                StatCard(
                    label = "Completas",
                    value = "18",
                    accentColor = SiegeColors.AccentCyan,
                    modifier = Modifier
                        .weight(1f)
                        .padding(SiegeSpacing.XXSmall)
                )
            }
        }

        // ── "Continue de onde parou" — navega para a última edição COM contexto de série ──
        item {
            SiegeList(
                items = mockSeries,
                style = SiegeListStyle.Horizontal,
                header = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SiegeText(text = "Continue de onde parou", style = SiegeTextStyle.Body)
                        SiegeButton(
                            text = "Ver tudo",
                            style = SiegeButtonStyle.Ghost,
                            onClick = { /* TODO */ }
                        )
                    }
                }
            ) { item ->
                SiegeContentCell(
                    coverImage = painterResource(ic_menu_gallery),
                    title = item.title,
                    style = ContentCardStyle.Cover,
                    contentType = item.type,
                    subtitle = item.subtitle,
                    progress = .1F,
                    badges = listOf(BadgeData("POSSUÍDA", SiegeColors.AccentCyan)),
                    onClick = {
                        val encEditionUrl = navEncode(item.latestEditionUrl)
                        val encEditionTitle = navEncode(item.title)
                        val encSeriesUrl = navEncode(item.seriesUrl)
                        val encSeriesTitle = navEncode(item.title)
                        navController.navigate(
                            AppRoute.EditionDetail.createRoute(
                                editionUrl = encEditionUrl,
                                editionTitle = encEditionTitle,
                                seriesUrl = encSeriesUrl,
                                seriesTitle = encSeriesTitle
                            )
                        )
                    }
                )
            }
        }

        // ── "Coleção completa" — navega para a lista de capas do título ───────
        item {
            SiegeList(
                items = mockSeries,
                style = SiegeListStyle.Grid(2),
                header = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SiegeText(text = "Coleção completa", style = SiegeTextStyle.Body)
                        SiegeButton(
                            text = "Ver tudo",
                            style = SiegeButtonStyle.Ghost,
                            onClick = { /* TODO */ }
                        )
                    }
                }
            ) { item ->
                SiegeContentCell(
                    coverImage = painterResource(ic_menu_gallery),
                    title = item.title,
                    style = ContentCardStyle.Grid,
                    contentType = item.type,
                    subtitle = item.subtitle,
                    badges = listOf(BadgeData("POSSUÍDA", SiegeColors.AccentCyan)),
                    onClick = {
                        val encSeriesUrl = navEncode(item.seriesUrl)
                        val encSeriesTitle = navEncode(item.title)
                        navController.navigate(
                            AppRoute.SeriesCovers.createRoute(encSeriesUrl, encSeriesTitle)
                        )
                    }
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