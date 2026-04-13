package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kiwizitos.collection.data.model.CoverItem
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navDecode
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.presentation.viewmodel.PaginatedCoversResult
import com.kiwizitos.collection.presentation.viewmodel.SearchViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoversScreen(
    navController: NavController,
    encodedSeriesUrl: String,
    encodedSeriesTitle: String,
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    viewModel: SearchViewModel = hiltViewModel()
) {
    val seriesUrl = navDecode(encodedSeriesUrl)
    val seriesTitle = navDecode(encodedSeriesTitle)
    val coversState by viewModel.coversState.collectAsState()
    val galleryMap by galleryViewModel.galleryMap.collectAsState()
    val seriesMap by galleryViewModel.seriesMap.collectAsState()
    val gridState = rememberLazyGridState()

    // URL da capa do 1º volume — disponível assim que a primeira página carrega
    val firstCoverUrl: String? = when (val s = coversState) {
        is UiState.Success -> s.data.covers.firstOrNull()?.coverUrl
        is UiState.LoadingMore -> s.currentData.covers.firstOrNull()?.coverUrl
        else -> null
    }
    val isSaved = seriesMap.containsKey(seriesUrl)

    LaunchedEffect(seriesUrl) {
        viewModel.getSeriesCovers(seriesUrl = seriesUrl, seriesTitle = seriesTitle)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SiegeText(
                        text = seriesTitle,
                        style = SiegeTextStyle.Body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetCoversState()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SiegeTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSaved) {
                                galleryViewModel.removeSeries(seriesUrl)
                            } else {
                                galleryViewModel.saveSeries(
                                    UserSeries(
                                        seriesUrl = seriesUrl,
                                        seriesTitle = seriesTitle,
                                        coverUrl = firstCoverUrl
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Remover da biblioteca" else "Salvar na biblioteca",
                            tint = if (isSaved) SiegeColors.AccentPink else SiegeTheme.colors.textTertiary
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Contador fixo ─────────────────────────────────────────────────
            when (val s = coversState) {
                is UiState.Success -> CoversCounter(
                    s.data.covers.size,
                    s.data.paginationInfo.totalResults
                )

                is UiState.LoadingMore -> CoversCounter(
                    s.currentData.covers.size,
                    s.currentData.paginationInfo.totalResults
                )

                else -> Unit
            }

            // ── Conteúdo principal ────────────────────────────────────────────
            when (val s = coversState) {

                is UiState.Idle, is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = SiegeColors.AccentPink) }

                is UiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SiegeText(
                        text = "Nenhuma capa encontrada para esta série",
                        style = SiegeTextStyle.Body,
                        color = SiegeTheme.colors.textTertiary
                    )
                }

                is UiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                    Box(modifier = Modifier.padding(top = SiegeSpacing.Medium)) {
                        SiegeButton(
                            text = "Tentar novamente",
                            style = SiegeButtonStyle.Primary,
                            onClick = { viewModel.getSeriesCovers(seriesUrl, seriesTitle) }
                        )
                    }
                }

                is UiState.Success -> CoversGrid(
                    data = s.data,
                    isLoadingMore = false,
                    galleryMap = galleryMap,
                    gridState = gridState,
                    onLoadMore = { viewModel.loadNextCoversPage() },
                    onCoverClick = { cover ->
                        navController.navigate(
                            AppRoute.EditionDetail.createRoute(
                                editionUrl = navEncode(cover.relativeLink),
                                editionTitle = navEncode(cover.title)
                            )
                        )
                    }
                )

                is UiState.LoadingMore -> CoversGrid(
                    data = s.currentData,
                    isLoadingMore = true,
                    galleryMap = galleryMap,
                    gridState = gridState,
                    onLoadMore = {},
                    onCoverClick = { cover ->
                        navController.navigate(
                            AppRoute.EditionDetail.createRoute(
                                editionUrl = navEncode(cover.relativeLink),
                                editionTitle = navEncode(cover.title)
                            )
                        )
                    }
                )
            }
        }
    }
}

// ── Contador ─────────────────────────────────────────────────────────────────

@Composable
private fun CoversCounter(loaded: Int, total: Int) {
    val text = if (total > 0) "Exibindo $loaded de $total capas" else "$loaded capa(s)"
    Column {
        SiegeText(
            text = text,
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textTertiary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SiegeSpacing.XSmall),
            textAlign = TextAlign.Center
        )
        HorizontalDivider(color = SiegeTheme.colors.outline)
    }
}

// ── Grade ─────────────────────────────────────────────────────────────────────

@Composable
private fun CoversGrid(
    data: PaginatedCoversResult,
    isLoadingMore: Boolean,
    galleryMap: Map<String, ItemStatus>,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onLoadMore: () -> Unit,
    onCoverClick: (CoverItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(SiegeSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small),
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
    ) {
        items(items = data.covers, key = { it.relativeLink }) { cover ->
            CoverCell(
                cover = cover,
                category = galleryMap[cover.relativeLink],
                onClick = { onCoverClick(cover) }
            )
        }
        if (data.paginationInfo.hasNextPage) {
            item(key = "load_more_footer", span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SiegeSpacing.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) CircularProgressIndicator(
                        color = SiegeColors.AccentPink,
                        modifier = Modifier.size(28.dp)
                    )
                    else SiegeButton(
                        text = "Carregar mais",
                        style = SiegeButtonStyle.Ghost,
                        onClick = onLoadMore
                    )
                }
            }
        }
    }
}

// ── Célula ────────────────────────────────────────────────────────────────────

@Composable
private fun CoverCell(cover: CoverItem, category: ItemStatus?, onClick: () -> Unit) {
    Box {
        SiegeCard(style = SiegeCardStyle.Elevated, onClick = onClick) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(cover.coverUrl)
                    .crossfade(true).build(),
                contentDescription = cover.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(SiegeShapes.Small)
            )
            SiegeText(
                text = cover.title,
                style = SiegeTextStyle.Label,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = SiegeSpacing.XSmall)
            )
        }
        if (category != null) {
            CategoryBadge(
                status = category,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(SiegeSpacing.XSmall)
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CoversScreenPreview() {
    SiegeTheme {
        CoversScreen(
            navController = rememberNavController(),
            encodedSeriesUrl = "titulo%2Fbatman",
            encodedSeriesTitle = "Batman"
        )
    }
}

