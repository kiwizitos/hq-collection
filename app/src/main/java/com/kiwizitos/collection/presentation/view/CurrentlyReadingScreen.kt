package com.kiwizitos.collection.presentation.view

import android.R.drawable.ic_menu_gallery
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.components.card.ContentCardStyle
import com.kiwizitos.siege.components.card.ContentType
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.foundation.SiegeIcon
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeIcons
import com.kiwizitos.siege.tokens.SiegeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentlyReadingScreen(
    navController: NavController,
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {
    val editionsFull by galleryViewModel.editionsFull.collectAsState()
    val lendoEditions = editionsFull.values
        .filter { it.readStatus == ReadStatus.LENDO }
        .sortedBy { it.seriesTitle ?: it.title }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { SiegeText(text = "Lendo agora", style = SiegeTextStyle.Body) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        SiegeIcon(
                            icon = SiegeIcons.ic_arrow_solid,
                            contentDescription = "Voltar",
                            tint = SiegeTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SiegeTheme.colors.surface,
                    titleContentColor = SiegeTheme.colors.textPrimary
                )
            )
        }
    ) { innerPadding ->
        if (lendoEditions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(SiegeSpacing.Large),
                contentAlignment = Alignment.Center
            ) {
                SiegeText(
                    text = "Nenhuma leitura em andamento.\nAbra uma edição e marque como \"Lendo\" para vê-la aqui.",
                    style = SiegeTextStyle.Body,
                    color = SiegeTheme.colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = SiegeSpacing.Regular)
            ) {
                items(lendoEditions, key = { it.guiaUrl }) { edition ->
                    ReadingEditionRow(edition = edition) { url, title, sUrl, sTitle ->
                        navController.navigate(
                            AppRoute.EditionDetail.createRoute(
                                editionUrl = navEncode(url),
                                editionTitle = navEncode(title),
                                seriesUrl = sUrl?.let { navEncode(it) } ?: "",
                                seriesTitle = sTitle?.let { navEncode(it) } ?: ""
                            )
                        )
                    }
                    HorizontalDivider(color = SiegeTheme.colors.outline)
                }
            }
        }
    }
}

// ── Row ───────────────────────────────────────────────────────────────────────

@Composable
private fun ReadingEditionRow(
    edition: UserItem,
    onNavigate: (url: String, title: String, seriesUrl: String?, seriesTitle: String?) -> Unit
) {
    val painter = if (!edition.coverUrl.isNullOrBlank())
        rememberAsyncImagePainter(edition.coverUrl)
    else
        painterResource(ic_menu_gallery)

    val badges = buildList {
        edition.ownership?.let {
            add(BadgeData(it.displayLabel.uppercase(), it.badgeColor, it.badgeIcon))
        }
        edition.readStatus?.let {
            add(BadgeData(it.displayLabel.uppercase(), it.badgeColor, it.badgeIcon))
        }
    }

    SiegeContentCell(
        coverImage = painter,
        title = edition.title,
        style = ContentCardStyle.Row,
        contentType = ContentType.Volume,
        subtitle = edition.seriesTitle,
        badges = badges,
        modifier = Modifier.padding(
            horizontal = SiegeSpacing.Regular,
            vertical = SiegeSpacing.XSmall
        ),
        onClick = {
            onNavigate(edition.guiaUrl, edition.title, edition.seriesUrl, edition.seriesTitle)
        }
    )
}
