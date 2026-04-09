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

// dados de exemplo — serão substituídos por ViewModel futuramente
private data class SeriesItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: ContentType = ContentType.Series  // distingue série de volume único
)

private val mockSeries = listOf(
    SeriesItem("watchmen", "Watchmen", "DC Comics", ContentType.Volume),  // volume único
    SeriesItem("berserk", "Berserk", "Young Animal", ContentType.Series),
    SeriesItem("sandman", "Sandman", "Vertigo", ContentType.Series),
    SeriesItem("one-piece", "One Piece", "Shonen Jump", ContentType.Series),
)

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
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
                            onClick = { /* TODO */ })
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
                    onClick = { /* TODO: navegar para detalhe */ }
                )
            }
        }

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
                            onClick = { /* TODO */ })
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
                    onClick = { /* TODO: navegar para detalhe */ }
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