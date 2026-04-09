package com.kiwizitos.siege.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.card.ContentCardStyle
import com.kiwizitos.siege.components.card.SiegeCard
import com.kiwizitos.siege.components.card.SiegeCardStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Style constants ───────────────────────────────────────────────────────────

/**
 * Variantes de layout disponíveis para o [SiegeList].
 *
 * ```
 * SiegeList(style = SiegeListStyle.Vertical,   items = ...) { item -> ... }
 * SiegeList(style = SiegeListStyle.Horizontal, items = ...) { item -> ... }
 * SiegeList(style = SiegeListStyle.Grid(),      items = ...) { item -> ... }
 * ```
 */
object SiegeListStyle {
    /** Lista vertical com scroll. */
    val Vertical = ListStyle.Vertical

    /** Carrossel horizontal com scroll. */
    val Horizontal = ListStyle.Horizontal

    /**
     * Grid de N colunas fixas.
     * @param columns Número de colunas. Padrão: 2.
     */
    fun Grid(columns: Int = 2): ListStyle = ListStyle.Grid(columns)
}

sealed interface ListStyle {
    data object Vertical   : ListStyle
    data object Horizontal : ListStyle
    data class  Grid(val columns: Int = 2) : ListStyle
}

// ── Unified component ─────────────────────────────────────────────────────────

/**
 * Lista unificada do Siege Design System.
 *
 * Renderiza uma coleção de itens em três variantes de layout, controladas
 * por [style]. Suporta header opcional e empty state customizável.
 *
 * @param items        Coleção de itens a renderizar.
 * @param style        Variante de layout — use as constantes de [SiegeListStyle].
 * @param modifier     Modificador opcional.
 * @param header       Slot opcional exibido acima da lista (ex: título de seção + botão "ver todos").
 * @param emptyState   Slot exibido quando [items] está vazio. Por padrão mostra mensagem padrão.
 * @param contentPadding Espaçamento interno ao redor dos itens.
 * @param itemSpacing  Espaçamento entre os itens.
 * @param itemContent  Composable que renderiza cada item.
 */
@Composable
fun <T> SiegeList(
    items: List<T>,
    modifier: Modifier = Modifier,
    style: ListStyle = SiegeListStyle.Vertical,
    header: (@Composable () -> Unit)? = null,
    emptyState: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(SiegeSpacing.XXSmall),
    itemSpacing: Float = SiegeSpacing.Small.value,
    itemContent: @Composable (T) -> Unit
) {
    Column(modifier = modifier) {
        header?.invoke()

        if (items.isEmpty()) {
            val empty = emptyState ?: { DefaultEmptyState() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) { empty() }
            return@Column
        }

        when (style) {
            ListStyle.Vertical -> VerticalList(
                items = items,
                contentPadding = contentPadding,
                itemSpacing = itemSpacing,
                itemContent = itemContent
            )
            ListStyle.Horizontal -> HorizontalList(
                items = items,
                contentPadding = contentPadding,
                itemSpacing = itemSpacing,
                itemContent = itemContent
            )
            is ListStyle.Grid -> GridList(
                items = items,
                columns = style.columns,
                contentPadding = contentPadding,
                itemSpacing = itemSpacing,
                itemContent = itemContent
            )
        }
    }
}

// ── Private layout variants ───────────────────────────────────────────────────

@Composable
private fun <T> VerticalList(
    items: List<T>,
    contentPadding: PaddingValues,
    itemSpacing: Float,
    itemContent: @Composable (T) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Dp(itemSpacing))
    ) {
        items.forEach { item ->
            Box(modifier = Modifier.fillMaxWidth()) {
                itemContent(item)
            }
        }
    }
}

@Composable
private fun <T> HorizontalList(
    items: List<T>,
    contentPadding: PaddingValues,
    itemSpacing: Float,
    itemContent: @Composable (T) -> Unit
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(Dp(itemSpacing))
    ) {
        items(items) { item -> itemContent(item) }
    }
}

@Composable
private fun <T> GridList(
    items: List<T>,
    columns: Int,
    contentPadding: PaddingValues,
    itemSpacing: Float,
    itemContent: @Composable (T) -> Unit
) {
    val rows = items.chunked(columns)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Dp(itemSpacing))
    ) {
        rows.forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dp(itemSpacing)),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        itemContent(item)
                    }
                }
                // Preenche colunas vazias na última linha
                repeat(columns - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DefaultEmptyState() {
    SiegeText(
        text = "Nenhum item encontrado.",
        style = SiegeTextStyle.Body,
        color = SiegeTheme.colors.textSecondary
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

private data class PreviewItem(val title: String, val subtitle: String)

private val previewItems = listOf(
    PreviewItem("Watchmen", "DC Comics"),
    PreviewItem("Berserk", "Young Animal"),
    PreviewItem("One Piece", "Shonen Jump"),
    PreviewItem("Sandman", "Vertigo"),
)

@Preview(showBackground = true, name = "SiegeList — Vertical")
@Composable
private fun SiegeListVerticalPreview() {
    SiegeTheme {
        SiegeList(
            items = previewItems,
            style = SiegeListStyle.Vertical,
            header = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SiegeSpacing.None, vertical = SiegeSpacing.Small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SiegeText(text = "Minha Coleção", style = SiegeTextStyle.Headline)
                    SiegeText(text = "Ver todos", style = SiegeTextStyle.Label, color = SiegeColors.AccentCyan)
                }
            }
        ) { item ->
            SiegeCard(
                style = SiegeCardStyle.Elevated,
                modifier = Modifier.fillMaxWidth(),
                title = item.subtitle,
                titleColor = SiegeColors.AccentPink
            ) {
                SiegeText(text = item.title, style = SiegeTextStyle.Body)
            }
        }
    }
}

@Preview(showBackground = true, name = "SiegeList — Horizontal (Carrossel)")
@Composable
private fun SiegeListHorizontalPreview() {
    SiegeTheme {
        SiegeList(
            items = previewItems,
            style = SiegeListStyle.Horizontal,
            header = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SiegeSpacing.Regular, vertical = SiegeSpacing.Small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SiegeText(text = "Continue assistindo", style = SiegeTextStyle.Headline)
                }
            }
        ) { item ->
            SiegeContentCell(
                coverImage = painterResource(android.R.drawable.ic_menu_gallery),
                title = item.title,
                style = ContentCardStyle.Cover,
                subtitle = item.subtitle,
                badges = listOf(BadgeData("NOVA", SiegeColors.AccentCyan))
            )
        }
    }
}

@Preview(showBackground = true, name = "SiegeList — Grid")
@Composable
private fun SiegeListGridPreview() {
    SiegeTheme {
        SiegeList(
            items = previewItems,
            style = SiegeListStyle.Grid(2),
            modifier = Modifier.fillMaxSize(),
            header = {
                SiegeText(
                    text = "Explorar",
                    style = SiegeTextStyle.Headline,
                    modifier = Modifier.padding(
                        horizontal = SiegeSpacing.Regular,
                        vertical = SiegeSpacing.Small
                    )
                )
            }
        ) { item ->
            SiegeContentCell(
                coverImage = painterResource(android.R.drawable.ic_menu_gallery),
                title = item.title,
                style = ContentCardStyle.Cover,
                subtitle = item.subtitle
            )
        }
    }
}

@Preview(showBackground = true, name = "SiegeList — Empty state")
@Composable
private fun SiegeListEmptyPreview() {
    SiegeTheme {
        SiegeList(
            items = emptyList<PreviewItem>(),
            style = SiegeListStyle.Vertical
        ) {}
    }
}

