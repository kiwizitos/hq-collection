package com.kiwizitos.collection.presentation.view

import android.R.drawable.ic_menu_gallery
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.kiwizitos.collection.data.model.Ownership
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.navigation.AppRoute
import com.kiwizitos.collection.navigation.navEncode
import com.kiwizitos.collection.presentation.view.VolumeFilter.TODOS
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.util.NaturalOrderComparator
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.components.card.ContentCardStyle
import com.kiwizitos.siege.components.card.ContentType
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Tabs ──────────────────────────────────────────────────────────────────────

private enum class LibraryTab(val label: String) {
    SERIES("Séries"),
    VOLUMES("Volumes")
}

// ── Volume filter ─────────────────────────────────────────────────────────────

/**
 * Filtros disponíveis para a aba de volumes.
 * [TODOS] mostra tudo; os demais correspondem aos valores de [Ownership] e [ReadStatus].
 */
private enum class VolumeFilter(val label: String, val color: Color) {
    TODOS("Todos", Color.Unspecified),
    TENHO("Tenho", Ownership.TENHO.badgeColor),
    QUERO("Quero", Ownership.QUERO.badgeColor),
    LIDO("Lido", ReadStatus.LIDO.badgeColor),
    LENDO("Lendo", ReadStatus.LENDO.badgeColor);

    fun matches(item: UserItem): Boolean = when (this) {
        TODOS -> true
        TENHO -> item.ownership == Ownership.TENHO
        QUERO -> item.ownership == Ownership.QUERO
        LIDO -> item.readStatus == ReadStatus.LIDO
        LENDO -> item.readStatus == ReadStatus.LENDO
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {
    val seriesMap by galleryViewModel.seriesMap.collectAsState()
    val editionsFull by galleryViewModel.editionsFull.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(LibraryTab.SERIES) }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────────────────
        SiegeText(
            text = "Galeria",
            style = SiegeTextStyle.Headline,
            modifier = Modifier.padding(
                start = SiegeSpacing.Regular,
                end = SiegeSpacing.Regular,
                top = SiegeSpacing.Regular,
                bottom = SiegeSpacing.Small
            )
        )

        // ── Tab toggle ────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = SiegeColors.AccentPink,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = SiegeColors.AccentPink
                )
            },
            divider = {}
        ) {
            LibraryTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Tab(
                    selected = selected,
                    onClick = { selectedTab = tab },
                    icon = {
                        Icon(
                            imageVector = if (tab == LibraryTab.SERIES)
                                Icons.Filled.Apps
                            else
                                Icons.AutoMirrored.Filled.List,
                            contentDescription = tab.label,
                            tint = if (selected) SiegeColors.AccentPink
                            else SiegeTheme.colors.textTertiary
                        )
                    },
                    text = {
                        SiegeText(
                            text = tab.label,
                            style = SiegeTextStyle.Label,
                            color = if (selected) SiegeColors.AccentPink
                            else SiegeTheme.colors.textTertiary
                        )
                    }
                )
            }
        }

        HorizontalDivider(color = SiegeTheme.colors.surface)

        // ── Content ───────────────────────────────────────────────────────────
        when (selectedTab) {
            LibraryTab.SERIES -> SeriesTab(
                series = seriesMap.values.toList(),
                navController = navController
            )

            LibraryTab.VOLUMES -> VolumesTab(
                editions = editionsFull.values.toList(),
                navController = navController
            )
        }
    }
}

// ── Series tab — 2-column grid ────────────────────────────────────────────────

@Composable
private fun SeriesTab(
    series: List<UserSeries>,
    navController: NavController
) {
    if (series.isEmpty()) {
        LibraryEmptyState("Nenhuma série salva ainda.\nBusque e salve séries para vê-las aqui.")
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SiegeSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small),
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small),
        contentPadding = PaddingValues(vertical = SiegeSpacing.Regular)
    ) {
        val sorted = series.sortedWith(compareBy(NaturalOrderComparator) { it.seriesTitle })
        items(sorted, key = { it.seriesUrl }) { s ->
            val painter = if (!s.coverUrl.isNullOrBlank())
                rememberAsyncImagePainter(s.coverUrl)
            else
                painterResource(ic_menu_gallery)

            SiegeContentCell(
                coverImage = painter,
                title = s.seriesTitle,
                style = ContentCardStyle.Grid,
                contentType = ContentType.Series,
                subtitle = s.publisher,
                onClick = {
                    navController.navigate(
                        AppRoute.SeriesCovers.createRoute(
                            navEncode(s.seriesUrl),
                            navEncode(s.seriesTitle)
                        )
                    )
                }
            )
        }
    }
}

// ── Volumes tab — filter chips + grouped collapsible list ─────────────────────

@Composable
private fun VolumesTab(
    editions: List<UserItem>,
    navController: NavController
) {
    var activeFilter by rememberSaveable { mutableStateOf(VolumeFilter.TODOS) }

    // Aplica filtro antes de agrupar — grupos sem itens desaparecem automaticamente
    val filtered = editions.filter { activeFilter.matches(it) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Filter chips ──────────────────────────────────────────────────────
        FilterChipRow(
            active = activeFilter,
            onSelect = { activeFilter = it }
        )

        HorizontalDivider(color = SiegeTheme.colors.surface)

        // ── Content ───────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            LibraryEmptyState(
                if (editions.isEmpty())
                    "Nenhum volume salvo ainda.\nAbra uma série e salve edições para vê-las aqui."
                else
                    "Nenhum volume com o filtro \"${activeFilter.label}\"."
            )
            return@Column
        }

        val grouped: Map<String, List<UserItem>> = filtered
            .groupBy { it.seriesTitle ?: "Avulsos" }
            .entries
            .sortedWith(compareBy(NaturalOrderComparator) { it.key })
            .associate { (key, items) ->
                key to items.sortedWith(compareBy(NaturalOrderComparator) { it.title })
            }

        val singleGroup = grouped.size == 1

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = SiegeSpacing.Regular)
        ) {
            grouped.forEach { (groupTitle, items) ->
                if (singleGroup) {
                    // Único grupo visível — sem header colapsável
                    items.forEach { edition ->
                        item(key = edition.guiaUrl) {
                            VolumeRow(edition = edition, navController = navController)
                            HorizontalDivider(
                                color = SiegeTheme.colors.surface,
                                thickness = androidx.compose.ui.unit.Dp.Hairline
                            )
                        }
                    }
                } else {
                    item(key = "header_${activeFilter.name}_$groupTitle") {
                        ExpandableGroupHeader(
                            title = groupTitle,
                            count = items.size,
                            groupKey = "${activeFilter.name}_$groupTitle"
                        ) { expanded ->
                            AnimatedVisibility(visible = expanded) {
                                Column {
                                    items.forEach { edition ->
                                        VolumeRow(edition = edition, navController = navController)
                                        HorizontalDivider(
                                            color = SiegeTheme.colors.surface,
                                            thickness = androidx.compose.ui.unit.Dp.Hairline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Filter chip row ───────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(
    active: VolumeFilter,
    onSelect: (VolumeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SiegeSpacing.Regular, vertical = SiegeSpacing.Small),
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
    ) {
        VolumeFilter.entries.forEach { filter ->
            val selected = filter == active
            val accentColor = if (filter == VolumeFilter.TODOS) SiegeColors.AccentPink
            else filter.color

            FilterChip(
                selected = selected,
                onClick = { onSelect(filter) },
                label = {
                    SiegeText(
                        text = filter.label,
                        style = SiegeTextStyle.Label,
                        color = if (selected) SiegeTheme.colors.background
                        else SiegeTheme.colors.textSecondary
                    )
                },
                leadingIcon = if (selected) ({
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = SiegeTheme.colors.background
                    )
                }) else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = SiegeTheme.colors.background,
                    selectedLeadingIconColor = SiegeTheme.colors.background,
                    containerColor = SiegeTheme.colors.surface,
                    labelColor = SiegeTheme.colors.textSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = if (selected) Color.Transparent else accentColor.copy(alpha = 0.4f),
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

// ── Expandable group header ───────────────────────────────────────────────────

@Composable
private fun ExpandableGroupHeader(
    title: String,
    count: Int,
    groupKey: String,
    content: @Composable (Boolean) -> Unit
) {
    var expanded by rememberSaveable(groupKey) { mutableStateOf(true) }
    val arrowAngle by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "arrow_$groupKey"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(SiegeTheme.colors.surface)
                .padding(horizontal = SiegeSpacing.Regular, vertical = SiegeSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SiegeText(
                text = title,
                style = SiegeTextStyle.Body,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.XSmall)
            ) {
                SiegeText(
                    text = "$count",
                    style = SiegeTextStyle.Label,
                    color = SiegeColors.TextTertiary
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = SiegeColors.TextTertiary,
                    modifier = Modifier.rotate(arrowAngle)
                )
            }
        }

        content(expanded)
    }
}

// ── Single volume row ─────────────────────────────────────────────────────────

@Composable
private fun VolumeRow(edition: UserItem, navController: NavController) {
    val painter = if (!edition.coverUrl.isNullOrBlank())
        rememberAsyncImagePainter(edition.coverUrl)
    else
        painterResource(ic_menu_gallery)

    val badges = buildList {
        edition.ownership?.let { add(BadgeData(it.displayLabel.uppercase(), it.badgeColor)) }
        edition.readStatus?.let { add(BadgeData(it.displayLabel.uppercase(), it.badgeColor)) }
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
            navController.navigate(
                AppRoute.EditionDetail.createRoute(
                    editionUrl = navEncode(edition.guiaUrl),
                    editionTitle = navEncode(edition.title),
                    seriesUrl = edition.seriesUrl?.let { navEncode(it) } ?: "",
                    seriesTitle = edition.seriesTitle?.let { navEncode(it) } ?: ""
                )
            )
        }
    )
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun LibraryEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SiegeSpacing.XLarge),
        contentAlignment = Alignment.Center
    ) {
        SiegeText(
            text = message,
            style = SiegeTextStyle.Body,
            color = SiegeTheme.colors.textTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    SiegeTheme(darkTheme = true) {
        LibraryScreen()
    }
}