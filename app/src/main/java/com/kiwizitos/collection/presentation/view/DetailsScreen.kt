package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navDecode
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.viewmodel.EditionViewModel
import com.kiwizitos.collection.presentation.viewmodel.EditionViewModelFactory
import com.kiwizitos.collection.presentation.viewmodel.UiState
import com.kiwizitos.siege.components.card.SiegeCard
import com.kiwizitos.siege.components.card.SiegeCardStyle
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

private const val GUIA_BASE = "http://www.guiadosquadrinhos.com"

/**
 * Tela de detalhe de uma edição.
 *
 * O card "Pertence ao título X" é exibido automaticamente quando a edição
 * carregada contém o link "Galeria de capas" no HTML — sem necessidade de
 * parâmetros externos. Os parâmetros [encodedSeriesUrl]/[encodedSeriesTitle]
 * servem apenas como fallback para casos em que o modelo não retorna esses dados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavController,
    encodedEditionUrl: String,
    encodedEditionTitle: String,
    onBackClick: () -> Unit,
    showSeriesCard: Boolean = true,
    encodedSeriesUrl: String? = null,
    encodedSeriesTitle: String? = null,
    viewModel: EditionViewModel = viewModel(factory = EditionViewModelFactory())
) {
    val editionUrl = navDecode(encodedEditionUrl)
    val editionTitle = navDecode(encodedEditionTitle)
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(editionUrl) {
        viewModel.load(editionUrl)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SiegeText(
                        text = editionTitle,
                        style = SiegeTextStyle.Body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        when (val s = state) {

            is UiState.Idle, is UiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = SiegeColors.AccentPink) }

            is UiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(SiegeSpacing.Regular),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SiegeText(
                    text = s.message,
                    style = SiegeTextStyle.Body,
                    color = SiegeColors.Error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(SiegeSpacing.Medium))
                SiegeButton(
                    text = "Tentar novamente",
                    style = SiegeButtonStyle.Primary,
                    onClick = { viewModel.load(editionUrl) }
                )
            }

            is UiState.Empty -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                SiegeText(
                    text = "Dados não encontrados",
                    style = SiegeTextStyle.Body,
                    color = SiegeTheme.colors.textTertiary
                )
            }

            is UiState.Success, is UiState.LoadingMore -> {
                val details = when (s) {
                    is UiState.Success -> s.data
                    is UiState.LoadingMore -> s.currentData
                    else -> return@Scaffold
                }
                // O card só aparece se showSeriesCard=true (não veio do CoversScreen)
                val resolvedSeriesUrl = if (showSeriesCard) details.seriesUrl
                    ?: encodedSeriesUrl?.let { navDecode(it) } else null
                val resolvedSeriesTitle =
                    if (showSeriesCard) details.seriesTitle ?: encodedSeriesTitle?.let {
                        navDecode(it)
                    } else null
                EditionContent(
                    details = details,
                    seriesTitle = resolvedSeriesTitle,
                    onSeriesClick = if (resolvedSeriesUrl != null && resolvedSeriesTitle != null) {
                        {
                            navController.navigate(
                                AppRoute.SeriesCovers.createRoute(
                                    navEncode(resolvedSeriesUrl),
                                    navEncode(resolvedSeriesTitle)
                                )
                            )
                        }
                    } else null,
                    onOpenWeb = {
                        val fullUrl = if (editionUrl.startsWith("http")) editionUrl
                        else "$GUIA_BASE/$editionUrl"
                        uriHandler.openUri(fullUrl)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

// ── Conteúdo ──────────────────────────────────────────────────────────────────

@Composable
private fun EditionContent(
    details: ComicDetails,
    seriesTitle: String?,
    onSeriesClick: (() -> Unit)?,
    onOpenWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.None)
    ) {
        // ── Card "Pertence ao título" — só aparece quando vem da Home ─────────
        if (seriesTitle != null && onSeriesClick != null) {
            item {
                SeriesBelongsToCard(
                    seriesTitle = seriesTitle,
                    onClick = onSeriesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SiegeSpacing.Regular,
                            end = SiegeSpacing.Regular,
                            top = SiegeSpacing.Regular
                        )
                )
            }
        }

        // ── Capa ──────────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SiegeSpacing.Large),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(details.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = details.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(2f / 3f)
                        .clip(SiegeShapes.Medium)
                        .background(SiegeTheme.colors.surfaceElevated)
                )
            }
        }

        // ── Título + data ─────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SiegeSpacing.Regular),
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XSmall)
            ) {
                SiegeText(
                    text = details.title,
                    style = SiegeTextStyle.Headline,
                    color = SiegeTheme.colors.textPrimary
                )
                details.publishedIn?.let {
                    SiegeText(
                        text = it,
                        style = SiegeTextStyle.Label,
                        color = SiegeTheme.colors.textTertiary
                    )
                }
            }
        }

        item { Spacer(Modifier.height(SiegeSpacing.Large)) }

        // ── Ficha técnica ─────────────────────────────────────────────────────
        item {
            InfoCard(
                title = "FICHA TÉCNICA",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SiegeSpacing.Regular)
            ) {
                listOfNotNull(
                    details.publisher?.let { "Editora" to it },
                    details.licensor?.let { "Licenciador" to it },
                    details.category?.let { "Categoria" to it },
                    details.genre?.let { "Gênero" to it },
                    details.status?.let { "Status" to it },
                    details.pages?.let { "Páginas" to it },
                    details.format?.let { "Formato" to it.replace("\n", " · ") },
                    details.coverPrice?.let { "Preço de capa" to it },
                    details.coverArtist?.let { "Arte da capa" to it }
                ).forEach { (label, value) ->
                    InfoRow(label = label, value = value)
                }
            }
        }

        // ── Botão ─────────────────────────────────────────────────────────────
        item { Spacer(Modifier.height(SiegeSpacing.Regular)) }
        item {
            SiegeButton(
                text = "Ver no Guia dos Quadrinhos",
                style = SiegeButtonStyle.Outlined,
                onClick = onOpenWeb,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SiegeSpacing.Regular)
            )
        }
        item { Spacer(Modifier.height(SiegeSpacing.XXLarge)) }
    }
}

// ── Card "Pertence ao título" ─────────────────────────────────────────────────

@Composable
private fun SeriesBelongsToCard(
    seriesTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SiegeCard(
        style = SiegeCardStyle.Outlined,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SiegeText(
                    text = "Pertence ao título",
                    style = SiegeTextStyle.Label,
                    color = SiegeTheme.colors.textTertiary
                )
                SiegeText(
                    text = seriesTitle,
                    style = SiegeTextStyle.Body,
                    color = SiegeColors.AccentCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver título",
                tint = SiegeTheme.colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Componentes de ficha ──────────────────────────────────────────────────────

@Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = SiegeShapes.Medium,
        color = SiegeTheme.colors.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeText(
                text = title,
                style = SiegeTextStyle.Label,
                color = SiegeColors.AccentPink
            )
            Spacer(Modifier.height(SiegeSpacing.XXSmall))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        SiegeText(
            text = label,
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textTertiary,
            modifier = Modifier.weight(0.4f)
        )
        SiegeText(
            text = value,
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textPrimary,
            modifier = Modifier
                .weight(0.6f)
                .padding(start = SiegeSpacing.Small)
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun DetailsScreenPreview() {
    SiegeTheme(darkTheme = true) {
        DetailsScreen(
            navController = rememberNavController(),
            encodedEditionUrl = "edicao%2Fx-men-2099-n-1%2Fx-011158%2F179145",
            encodedEditionTitle = "X-Men+2099+n%C2%B0+1",
            onBackClick = {}
        )
    }
}

