package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.SerieResult
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.presentation.viewmodel.PaginatedSearchResult
import com.kiwizitos.collection.presentation.viewmodel.SearchViewModel
import com.kiwizitos.collection.presentation.viewmodel.UiState
import com.kiwizitos.siege.components.card.SiegeCard
import com.kiwizitos.siege.components.card.SiegeCardStyle
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeSearchBar
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

@Composable
fun SearchScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    viewModel: SearchViewModel = hiltViewModel()
) {
    var query by rememberSaveable { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()
    val galleryMap by galleryViewModel.galleryMap.collectAsState()
    val listState = rememberLazyListState()

    val onSearch = {
        val trimmed = query.trim()
        if (trimmed.length >= 2) viewModel.searchSeries(trimmed)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SiegeSpacing.Regular)
    ) {
        SiegeSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = "Pesquisar séries, volumes...",
            onSearch = { onSearch() },
            modifier = Modifier.padding(vertical = SiegeSpacing.Regular)
        )

        // Contador — visível apenas quando há resultados
        when (val s = searchState) {
            is UiState.Success -> SearchCounter(
                s.data.series.size,
                s.data.paginationInfo.totalResults
            )

            is UiState.LoadingMore -> SearchCounter(
                s.currentData.series.size,
                s.currentData.paginationInfo.totalResults
            )

            else -> Unit
        }

        when (val s = searchState) {
            is UiState.Idle -> SearchIdleState()
            is UiState.Loading -> SearchLoadingState()
            is UiState.Empty -> SearchEmptyState(query = query)
            is UiState.Error -> SearchErrorState(message = s.message, onRetry = { onSearch() })

            is UiState.Success -> SearchResultsList(
                data = s.data,
                isLoadingMore = false,
                galleryMap = galleryMap,
                listState = listState,
                onLoadMore = { viewModel.loadNextSearchPage() },
                onSeriesClick = { serie ->
                    navController.navigate(
                        AppRoute.SeriesCovers.createRoute(
                            navEncode(serie.relativeLink),
                            navEncode(serie.title)
                        )
                    )
                }
            )

            is UiState.LoadingMore -> SearchResultsList(
                data = s.currentData,
                isLoadingMore = true,
                galleryMap = galleryMap,
                listState = listState,
                onLoadMore = {},
                onSeriesClick = { serie ->
                    navController.navigate(
                        AppRoute.SeriesCovers.createRoute(
                            navEncode(serie.relativeLink),
                            navEncode(serie.title)
                        )
                    )
                }
            )
        }
    }
}

// ── Contador ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchCounter(loaded: Int, total: Int) {
    val text = if (total > 0) "Exibindo $loaded de $total resultados" else "$loaded resultado(s)"
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

// ── Lista de resultados ───────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(
    data: PaginatedSearchResult,
    isLoadingMore: Boolean,
    galleryMap: Map<String, ItemStatus>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onLoadMore: () -> Unit,
    onSeriesClick: (SerieResult) -> Unit
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small),
        modifier = Modifier.padding(top = SiegeSpacing.Small)
    ) {
        itemsIndexed(items = data.series, key = { _, s -> s.relativeLink }) { index, serie ->
            SerieResultCard(
                serie = serie,
                position = index + 1,
                category = galleryMap[serie.relativeLink],
                onClick = { onSeriesClick(serie) }
            )
        }
        if (data.paginationInfo.hasNextPage) {
            item(key = "load_more_footer") {
                LoadMoreFooter(isLoading = isLoadingMore, onLoadMore = onLoadMore)
            }
        }
    }
}

// ── Rodapé ────────────────────────────────────────────────────────────────────

@Composable
private fun LoadMoreFooter(isLoading: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SiegeSpacing.Medium),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) CircularProgressIndicator(
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

// ── Card de série ─────────────────────────────────────────────────────────────

@Composable
private fun SerieResultCard(
    serie: SerieResult,
    position: Int,
    category: ItemStatus?,
    onClick: () -> Unit
) {
    SiegeCard(style = SiegeCardStyle.Outlined, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium)
        ) {
            SiegeText(
                text = "$position",
                style = SiegeTextStyle.Label,
                color = SiegeColors.AccentPink
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.XSmall)
                ) {
                    SiegeText(
                        text = serie.title,
                        style = SiegeTextStyle.Body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (category != null) CategoryBadge(status = category)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SiegeSpacing.XSmall),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SiegeText(
                        text = serie.publisher.ifBlank { serie.originalPublisher },
                        style = SiegeTextStyle.Label,
                        color = SiegeTheme.colors.textTertiary
                    )
                    SiegeText(
                        text = buildString {
                            if (serie.year.isNotBlank()) append(serie.year)
                            if (serie.issueCount.isNotBlank()) append(" · ${serie.issueCount} eds.")
                        },
                        style = SiegeTextStyle.Label,
                        color = SiegeTheme.colors.textTertiary
                    )
                }
            }
        }
    }
}

// ── Estados ───────────────────────────────────────────────────────────────────

@Composable
private fun SearchIdleState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SiegeSpacing.XLarge),
        contentAlignment = Alignment.Center
    ) {
        SiegeText(
            text = "Digite o nome da série e pressione buscar",
            style = SiegeTextStyle.Body,
            color = SiegeTheme.colors.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SiegeColors.AccentPink)
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SiegeSpacing.XLarge),
        contentAlignment = Alignment.Center
    ) {
        SiegeText(
            text = "Nenhum resultado para \"$query\"",
            style = SiegeTextStyle.Body,
            color = SiegeTheme.colors.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = SiegeSpacing.XLarge,
                start = SiegeSpacing.Regular,
                end = SiegeSpacing.Regular
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium)
    ) {
        SiegeText(
            text = message,
            style = SiegeTextStyle.Body,
            color = SiegeColors.Error,
            textAlign = TextAlign.Center
        )
        SiegeButton(text = "Tentar novamente", style = SiegeButtonStyle.Primary, onClick = onRetry)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    SiegeTheme { SearchScreen(navController = rememberNavController()) }
}

