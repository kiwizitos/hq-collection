package com.kiwizitos.collection.presentation.view

import android.R.drawable.ic_menu_gallery
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.kiwizitos.collection.presentation.view.VolumeFilter.AVULSOS
import com.kiwizitos.collection.presentation.view.VolumeFilter.TODOS
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.util.NaturalOrderComparator
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.components.card.ContentCardStyle
import com.kiwizitos.siege.components.card.ContentType
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.foundation.SiegeIcon
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeIcons
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Tabs ──────────────────────────────────────────────────────────────────────

private enum class LibraryTab(
    val label: String,
    val icon: Int,
    val iconSolid: Int
) {
    SERIES("Séries", SiegeIcons.ic_folder, SiegeIcons.ic_folder_solid),
    VOLUMES("Volumes", SiegeIcons.ic_file, SiegeIcons.ic_file_solid);

    fun iconFor(selected: Boolean) = if (selected) iconSolid else icon
}

// ── Volume filter ─────────────────────────────────────────────────────────────

/**
 * Filtros disponíveis para a aba de volumes.
 *
 * [AVULSOS] é um modo especial: mostra apenas volumes com isStandalone=true ou sem série,
 * numa lista plana (sem agrupamento por série).
 *
 * Os demais filtros operam sobre volumes *agrupados* (não-avulsos) e correspondem
 * aos valores de [Ownership] e [ReadStatus]. [TODOS] mostra todos os agrupados.
 */
private enum class VolumeFilter(
    val label: String,
    val color: Color,
    val icon: Int,
    val iconSolid: Int
) {
    AVULSOS("Avulsos",  SiegeColors.AccentCyan,  SiegeIcons.ic_file,      SiegeIcons.ic_file_solid),
    TODOS("Todos",      SiegeColors.AccentPink,  SiegeIcons.ic_filter,    SiegeIcons.ic_filter_solid),
    QUERO("Quero",      SiegeColors.AccentAmber, SiegeIcons.ic_flag,      SiegeIcons.ic_flag_solid),
    TENHO("Tenho",      SiegeColors.AccentCyan,  SiegeIcons.ic_bookmarks, SiegeIcons.ic_bookmarks_solid),
    LENDO("Lendo",      SiegeColors.AccentPink,  SiegeIcons.ic_glasses,   SiegeIcons.ic_glasses_solid),
    LIDO("Lido",        SiegeColors.AccentGreen, SiegeIcons.ic_book,      SiegeIcons.ic_book_solid);

    fun iconFor(selected: Boolean) = if (selected) iconSolid else icon

    val isAvulsos get() = this == AVULSOS

    /**
     * Verifica se o item deve aparecer neste filtro.
     * [AVULSOS] retorna apenas itens com [UserItem.isStandalone] = true (lista plana).
     * Demais filtros retornam apenas itens *não-avulsos* que satisfaçam o critério de status.
     */
    fun matches(item: UserItem): Boolean = when (this) {
        AVULSOS -> item.isStandalone
        TODOS -> !item.isStandalone
        TENHO -> !item.isStandalone && item.ownership == Ownership.TENHO
        QUERO -> !item.isStandalone && item.ownership == Ownership.QUERO
        LIDO -> !item.isStandalone && item.readStatus == ReadStatus.LIDO
        LENDO -> !item.isStandalone && item.readStatus == ReadStatus.LENDO
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
                        SiegeIcon(
                            icon = tab.iconFor(selected),
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
                when {
                    editions.isEmpty() ->
                        "Nenhum volume salvo ainda.\nAbra uma série e salve edições para vê-las aqui."

                    activeFilter.isAvulsos ->
                        "Nenhum volume avulso salvo.\nMarque um volume como avulso nos detalhes da edição."

                    else ->
                        "Nenhum volume com o filtro \"${activeFilter.label}\"."
                }
            )
            return@Column
        }

        // ── Lista plana: avulsos sem agrupamento ──────────────────────────
        val sorted = filtered.sortedWith(compareBy(NaturalOrderComparator) { it.title })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = SiegeSpacing.Regular)
        ) {
            items(sorted, key = { it.guiaUrl }) { edition ->
                VolumeRow(edition = edition, navController = navController)
                HorizontalDivider(
                    color = SiegeTheme.colors.surface,
                    thickness = androidx.compose.ui.unit.Dp.Hairline
                )
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
            val accentColor = filter.color

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
                leadingIcon = {
                    SiegeIcon(
                        icon = filter.iconFor(selected),
                        contentDescription = null,
                        tint = if (selected) SiegeTheme.colors.background else accentColor
                    )
                },
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


// ── Single volume row ─────────────────────────────────────────────────────────

@Composable
private fun VolumeRow(edition: UserItem, navController: NavController) {
    val painter = if (!edition.coverUrl.isNullOrBlank())
        rememberAsyncImagePainter(edition.coverUrl)
    else
        painterResource(ic_menu_gallery)

    val badges = buildList {
        edition.ownership?.let { add(BadgeData(it.displayLabel.uppercase(), it.badgeColor, it.badgeIcon)) }
        edition.readStatus?.let { add(BadgeData(it.displayLabel.uppercase(), it.badgeColor, it.badgeIcon)) }
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