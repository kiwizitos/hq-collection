package com.kiwizitos.collection.presentation.view.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.Ownership
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.siege.components.foundation.SiegeIcon
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeIcons
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing
import kotlin.math.roundToInt

// ── Filter State ──────────────────────────────────────────────────────────────

/**
 * Estado de filtro para a grade de capas.
 *
 * Cada campo controla a visibilidade de uma categoria de status.
 * [matches] determina se um item com determinado [ItemStatus] deve ser exibido.
 */
data class CoversFilterState(
    val showNoStatus: Boolean = true,
    val showTenho: Boolean = true,
    val showQuero: Boolean = true,
    val showLido: Boolean = true,
    val showLendo: Boolean = true
) {
    /** true quando nenhum filtro foi alterado do padrão (tudo visível). */
    val isDefault: Boolean
        get() = showNoStatus && showTenho && showQuero && showLido && showLendo

    /**
     * Retorna true se o item com [status] deve ser exibido com este filtro.
     * Um item é visível apenas se TODOS os seus status ativos passam pelo filtro.
     */
    fun matches(status: ItemStatus?): Boolean {
        if (status == null) return showNoStatus
        val ownershipOk = when (status.ownership) {
            Ownership.TENHO -> showTenho
            Ownership.QUERO -> showQuero
            null -> true
        }
        val readOk = when (status.readStatus) {
            ReadStatus.LIDO -> showLido
            ReadStatus.LENDO -> showLendo
            null -> true
        }
        return ownershipOk && readOk
    }
}

// ── Internal data ─────────────────────────────────────────────────────────────

private data class ProgressSegment(val count: Int, val color: Color)

// ── Main Component ────────────────────────────────────────────────────────────

/**
 * Bottom sheet de filtros para a grade de capas de uma série.
 *
 * Exibe switches para controle de visibilidade por categoria e uma visualização
 * de progresso customizada que se adapta ao estado atual dos filtros.
 *
 * @param onDismiss       Chamado ao fechar a sheet.
 * @param filterState     Estado atual do filtro.
 * @param onFilterChange  Callback de alteração do estado.
 * @param totalCovers     Número total de capas carregadas para a série.
 * @param countNoStatus   Capas sem nenhum status.
 * @param countTenho      Capas com ownership = TENHO.
 * @param countQuero      Capas com ownership = QUERO.
 * @param countLido       Capas com readStatus = LIDO.
 * @param countLendo      Capas com readStatus = LENDO.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    filterState: CoversFilterState,
    onFilterChange: (CoversFilterState) -> Unit,
    totalCovers: Int,
    countNoStatus: Int,
    countTenho: Int,
    countQuero: Int,
    countLido: Int,
    countLendo: Int
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SiegeTheme.colors.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SiegeSpacing.Regular)
                .padding(bottom = SiegeSpacing.XLarge)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
                ) {
                    SiegeIcon(
                        icon = SiegeIcons.ic_filter_solid,
                        tint = SiegeColors.AccentPink,
                        modifier = Modifier.size(20.dp)
                    )
                    SiegeText(text = "Filtros", style = SiegeTextStyle.Headline)
                }
                if (!filterState.isDefault) {
                    TextButton(onClick = { onFilterChange(CoversFilterState()) }) {
                        SiegeText(
                            text = "Redefinir",
                            style = SiegeTextStyle.Label,
                            color = SiegeColors.AccentCyan
                        )
                    }
                }
            }

            Spacer(Modifier.height(SiegeSpacing.Regular))

            // ── EXIBIÇÃO ──────────────────────────────────────────────────────
            SiegeText(
                text = "EXIBIÇÃO",
                style = SiegeTextStyle.Label,
                color = SiegeTheme.colors.textTertiary
            )
            Spacer(Modifier.height(SiegeSpacing.XSmall))

            FilterSwitchRow(
                icon = SiegeIcons.ic_file,
                label = "Sem status",
                count = countNoStatus,
                color = SiegeTheme.colors.textTertiary,
                checked = filterState.showNoStatus,
                onCheckedChange = { onFilterChange(filterState.copy(showNoStatus = it)) }
            )
            FilterSwitchRow(
                icon = SiegeIcons.ic_bookmarks_solid,
                label = "Tenho",
                count = countTenho,
                color = SiegeColors.AccentCyan,
                checked = filterState.showTenho,
                onCheckedChange = { onFilterChange(filterState.copy(showTenho = it)) }
            )
            FilterSwitchRow(
                icon = SiegeIcons.ic_flag_solid,
                label = "Quero",
                count = countQuero,
                color = SiegeColors.AccentAmber,
                checked = filterState.showQuero,
                onCheckedChange = { onFilterChange(filterState.copy(showQuero = it)) }
            )
            FilterSwitchRow(
                icon = SiegeIcons.ic_book_solid,
                label = "Lido",
                count = countLido,
                color = SiegeColors.AccentGreen,
                checked = filterState.showLido,
                onCheckedChange = { onFilterChange(filterState.copy(showLido = it)) }
            )
            FilterSwitchRow(
                icon = SiegeIcons.ic_glasses_solid,
                label = "Lendo",
                count = countLendo,
                color = SiegeColors.AccentPink,
                checked = filterState.showLendo,
                onCheckedChange = { onFilterChange(filterState.copy(showLendo = it)) }
            )

            Spacer(Modifier.height(SiegeSpacing.Medium))
            HorizontalDivider(color = SiegeTheme.colors.outline)
            Spacer(Modifier.height(SiegeSpacing.Medium))

            // ── RESUMO ────────────────────────────────────────────────────────
            val countCatalogados = totalCovers - countNoStatus

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SiegeText(
                    text = "RESUMO DA SÉRIE",
                    style = SiegeTextStyle.Label,
                    color = SiegeTheme.colors.textTertiary
                )
                SiegeText(
                    text = "$countCatalogados / $totalCovers catalogados",
                    style = SiegeTextStyle.Label,
                    color = SiegeTheme.colors.textSecondary
                )
            }

            if (totalCovers > 0) {
                Spacer(Modifier.height(SiegeSpacing.Small))

                // Barra global: catalogados vs sem status
                val globalFraction = countCatalogados.toFloat() / totalCovers
                val animatedGlobal by animateFloatAsState(
                    targetValue = globalFraction,
                    animationSpec = tween(400),
                    label = "global_progress"
                )
                LinearProgressIndicator(
                    progress = { animatedGlobal },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(SiegeShapes.Full),
                    color = SiegeColors.AccentPink,
                    trackColor = SiegeTheme.colors.surfaceVariant
                )

                Spacer(Modifier.height(SiegeSpacing.Medium))

                // ── Barra empilhada (adapta-se ao filtro ativo) ───────────────
                SiegeText(
                    text = "DISTRIBUIÇÃO",
                    style = SiegeTextStyle.Label,
                    color = SiegeTheme.colors.textTertiary
                )
                Spacer(Modifier.height(SiegeSpacing.XSmall))

                StackedProgressBar(
                    segments = buildList {
                        if (filterState.showTenho && countTenho > 0)
                            add(ProgressSegment(countTenho, SiegeColors.AccentCyan))
                        if (filterState.showQuero && countQuero > 0)
                            add(ProgressSegment(countQuero, SiegeColors.AccentAmber))
                        if (filterState.showLido && countLido > 0)
                            add(ProgressSegment(countLido, SiegeColors.AccentGreen))
                        if (filterState.showLendo && countLendo > 0)
                            add(ProgressSegment(countLendo, SiegeColors.AccentPink))
                    },
                    total = totalCovers,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(SiegeSpacing.Medium))

                // ── Barras individuais por categoria ──────────────────────────
                CategoryProgressRow(
                    label = "Tenho",
                    count = countTenho,
                    total = totalCovers,
                    color = SiegeColors.AccentCyan,
                    visible = filterState.showTenho,
                    iconRes = SiegeIcons.ic_bookmarks_solid
                )
                CategoryProgressRow(
                    label = "Quero",
                    count = countQuero,
                    total = totalCovers,
                    color = SiegeColors.AccentAmber,
                    visible = filterState.showQuero,
                    iconRes = SiegeIcons.ic_flag_solid
                )
                CategoryProgressRow(
                    label = "Lido",
                    count = countLido,
                    total = totalCovers,
                    color = SiegeColors.AccentGreen,
                    visible = filterState.showLido,
                    iconRes = SiegeIcons.ic_book_solid
                )
                CategoryProgressRow(
                    label = "Lendo",
                    count = countLendo,
                    total = totalCovers,
                    color = SiegeColors.AccentPink,
                    visible = filterState.showLendo,
                    iconRes = SiegeIcons.ic_glasses_solid
                )
                CategoryProgressRow(
                    label = "Sem status",
                    count = countNoStatus,
                    total = totalCovers,
                    color = SiegeTheme.colors.textDisabled,
                    visible = filterState.showNoStatus,
                    iconRes = SiegeIcons.ic_file
                )
            }
        }
    }
}

// ── FilterSwitchRow ───────────────────────────────────────────────────────────

@Composable
private fun FilterSwitchRow(
    icon: Int,
    label: String,
    count: Int,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SiegeSpacing.XXSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
    ) {
        SiegeIcon(
            icon = icon,
            contentDescription = null,
            tint = if (checked) color else SiegeTheme.colors.textDisabled,
            modifier = Modifier.size(20.dp)
        )
        SiegeText(
            text = label,
            style = SiegeTextStyle.Body,
            color = if (checked) SiegeTheme.colors.textPrimary else SiegeTheme.colors.textDisabled,
            modifier = Modifier.weight(1f)
        )
        SiegeText(
            text = count.toString(),
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textSecondary
        )
        Spacer(Modifier.width(SiegeSpacing.XSmall))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                checkedTrackColor = color.copy(alpha = 0.3f),
                uncheckedThumbColor = SiegeTheme.colors.textDisabled,
                uncheckedTrackColor = SiegeTheme.colors.surfaceVariant
            )
        )
    }
}

// ── StackedProgressBar ────────────────────────────────────────────────────────

/**
 * Barra de progresso empilhada com múltiplos segmentos coloridos.
 *
 * Apenas os segmentos visíveis (filtros ativos) ocupam espaço.
 * O restante é preenchido com a cor de fundo do tema (surfaceVariant).
 */
@Composable
private fun StackedProgressBar(
    segments: List<ProgressSegment>,
    total: Int,
    modifier: Modifier = Modifier
) {
    val visibleTotal = segments.sumOf { it.count }

    Box(
        modifier = modifier
            .height(10.dp)
            .clip(SiegeShapes.Full)
            .background(SiegeTheme.colors.surfaceVariant)
    ) {
        if (total > 0 && visibleTotal > 0) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = visibleTotal.toFloat() / total)
            ) {
                segments.forEach { segment ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(segment.count.toFloat())
                            .background(segment.color)
                    )
                }
            }
        }
    }
}

// ── CategoryProgressRow ───────────────────────────────────────────────────────

/**
 * Linha individual de progresso por categoria.
 *
 * Inclui ícone, label, barra animada e contador/percentual.
 * Quando o filtro está desabilitado ([visible] = false), a linha fica com opacidade reduzida
 * e a barra retrocede para zero com animação.
 */
@Composable
private fun CategoryProgressRow(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    visible: Boolean,
    iconRes: Int
) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = if (visible) fraction else 0f,
        animationSpec = tween(350),
        label = "progress_$label"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SiegeSpacing.XXSmall)
            .alpha(if (visible) 1f else 0.35f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
    ) {
        SiegeIcon(
            icon = iconRes,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        SiegeText(
            text = label,
            style = SiegeTextStyle.Label,
            modifier = Modifier.width(72.dp)
        )
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(SiegeShapes.Full),
            color = color,
            trackColor = SiegeTheme.colors.surfaceVariant
        )
        SiegeText(
            text = "$count  (${(fraction * 100).roundToInt()}%)",
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textSecondary,
            modifier = Modifier.width(68.dp),
            textAlign = TextAlign.End
        )
    }
}