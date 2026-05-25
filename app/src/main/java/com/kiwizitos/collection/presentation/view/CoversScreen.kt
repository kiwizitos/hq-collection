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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kiwizitos.collection.data.model.Ownership
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navDecode
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.view.components.CategoryBadge
import com.kiwizitos.collection.presentation.view.components.CoversFilterState
import com.kiwizitos.collection.presentation.view.components.FilterBottomSheet
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.presentation.viewmodel.PaginatedCoversResult
import com.kiwizitos.collection.presentation.viewmodel.SearchViewModel
import com.kiwizitos.collection.presentation.viewmodel.UiState
import com.kiwizitos.siege.components.card.SiegeCard
import com.kiwizitos.siege.components.card.SiegeCardStyle
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeIcon
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeIcons
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

    // Restore scroll position saved before the last navigation away from this screen
    LaunchedEffect(gridState) {
        if (viewModel.savedCoversGridIndex > 0) {
            gridState.scrollToItem(
                index = viewModel.savedCoversGridIndex,
                scrollOffset = viewModel.savedCoversGridOffset
            )
        }
    }

    // Helper: save current position then navigate to an edition detail
    val navigateToEdition: (String, String) -> Unit = { editionUrl, editionTitle ->
        viewModel.saveCoversGridState(
            index = gridState.firstVisibleItemIndex,
            offset = gridState.firstVisibleItemScrollOffset
        )
        navController.navigate(
            AppRoute.EditionDetail.createRoute(
                editionUrl = navEncode(editionUrl),
                editionTitle = navEncode(editionTitle)
            )
        )
    }

// ── Contador ─────────────────────────────────────────────────────────────────

    val firstCoverUrl: String? = when (val s = coversState) {
        is UiState.Success -> s.data.covers.firstOrNull()?.coverUrl
        is UiState.LoadingMore -> s.currentData.covers.firstOrNull()?.coverUrl
        else -> null
    }
    val isSaved = seriesMap.containsKey(seriesUrl)

    // ── Filter state ──────────────────────────────────────────────────────────
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(CoversFilterState()) }

    val allCovers = remember(coversState) {
        when (val s = coversState) {
            is UiState.Success -> s.data.covers
            is UiState.LoadingMore -> s.currentData.covers
            else -> emptyList()
        }
    }

    val countNoStatus = remember(allCovers, galleryMap) {
        allCovers.count { galleryMap[it.relativeLink] == null }
    }
    val countTenho = remember(allCovers, galleryMap) {
        allCovers.count { galleryMap[it.relativeLink]?.ownership == Ownership.TENHO }
    }
    val countQuero = remember(allCovers, galleryMap) {
        allCovers.count { galleryMap[it.relativeLink]?.ownership == Ownership.QUERO }
    }
    val countLido = remember(allCovers, galleryMap) {
        allCovers.count { galleryMap[it.relativeLink]?.readStatus == ReadStatus.LIDO }
    }
    val countLendo = remember(allCovers, galleryMap) {
        allCovers.count { galleryMap[it.relativeLink]?.readStatus == ReadStatus.LENDO }
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
                        viewModel.saveCoversGridState(
                            index = gridState.firstVisibleItemIndex,
                            offset = gridState.firstVisibleItemScrollOffset
                        )
                        viewModel.resetCoversState()
                        navController.popBackStack()
                    }) {
                        SiegeIcon(
                            icon = SiegeIcons.ic_arrow_solid,
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
                        SiegeIcon(
                            icon = if (isSaved) SiegeIcons.ic_like_solid else SiegeIcons.ic_like,
                            contentDescription = if (isSaved) "Remover da biblioteca" else "Salvar na biblioteca",
                            tint = if (isSaved) SiegeColors.AccentPink else SiegeTheme.colors.textTertiary
                        )
                    }
                    
                    // Componente de filtro - bottomsheet

                    IconButton(
                        onClick = { showFilterSheet = true }
                    ) {
                        SiegeIcon(
                            icon = if (filterState.isDefault) SiegeIcons.ic_filter else SiegeIcons.ic_filter_solid,
                            contentDescription = "Filtro",
                            tint = if (filterState.isDefault) SiegeTheme.colors.textTertiary else SiegeColors.AccentPink
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
        // ── Dados estáveis para não alternar entre branches do when ─────────────
        val currentData: PaginatedCoversResult? = when (val s = coversState) {
            is UiState.Success -> s.data
            is UiState.LoadingMore -> s.currentData
            else -> null
        }
        val isLoadingMore = coversState is UiState.LoadingMore

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Contador fixo ─────────────────────────────────────────────────
            if (currentData != null) {
                CoversCounter(
                    loaded = currentData.covers.size,
                    total = currentData.paginationInfo.totalResults
                )
            }

            // ── Conteúdo principal ────────────────────────────────────────────
            when {
                coversState is UiState.Idle || coversState is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = SiegeColors.AccentPink) }

                coversState is UiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SiegeText(
                        text = "Nenhuma capa encontrada para esta série",
                        style = SiegeTextStyle.Body,
                        color = SiegeTheme.colors.textTertiary
                    )
                }

                coversState is UiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SiegeSpacing.Regular),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SiegeText(
                        text = (coversState as UiState.Error).message,
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

                // Success ou LoadingMore — sempre a mesma chamada ao CoversGrid
                // para que o Compose não descarte o estado da LazyVerticalGrid
                currentData != null -> CoversGrid(
                    data = currentData,
                    isLoadingMore = isLoadingMore,
                    galleryMap = galleryMap,
                    filterState = filterState,
                    gridState = gridState,
                    onLoadMore = { viewModel.loadNextCoversPage() },
                    onCoverClick = { cover ->
                        navigateToEdition(cover.relativeLink, cover.title)
                    }
                )
            }
        }
    }

    // ── Filter Bottom Sheet ───────────────────────────────────────────────────
    if (showFilterSheet) {
        FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            filterState = filterState,
            onFilterChange = { filterState = it },
            totalCovers = allCovers.size,
            countNoStatus = countNoStatus,
            countTenho = countTenho,
            countQuero = countQuero,
            countLido = countLido,
            countLendo = countLendo
        )
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
    filterState: CoversFilterState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onLoadMore: () -> Unit,
    onCoverClick: (CoverItem) -> Unit
) {
    val filteredCovers = remember(data.covers, galleryMap, filterState) {
        if (filterState.isDefault) data.covers
        else data.covers.filter { cover -> filterState.matches(galleryMap[cover.relativeLink]) }
    }

    // ── Auto-load when approaching the bottom of the grid ────────────────────
    val shouldLoadMore by remember(data.paginationInfo.hasNextPage, isLoadingMore) {
        derivedStateOf {
            if (!data.paginationInfo.hasNextPage || isLoadingMore) return@derivedStateOf false
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 6      // ~2 rows ahead in a 3-col grid
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(SiegeSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small),
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
    ) {
        items(items = filteredCovers, key = { it.relativeLink }) { cover ->
            CoverCell(
                cover = cover,
                category = galleryMap[cover.relativeLink],
                onClick = { onCoverClick(cover) }
            )
        }
        // Spinner-only footer — button is replaced by auto-scroll
        if (data.paginationInfo.hasNextPage && isLoadingMore) {
            item(key = "load_more_footer", span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SiegeSpacing.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SiegeColors.AccentPink,
                        modifier = Modifier.size(28.dp)
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

