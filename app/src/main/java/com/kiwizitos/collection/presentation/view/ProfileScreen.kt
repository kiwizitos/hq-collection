package com.kiwizitos.collection.presentation.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiwizitos.collection.data.model.Ownership
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
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
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    galleryViewModel: GalleryViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val galleryMap = galleryViewModel.galleryMap.collectAsState().value
    val seriesMap  = galleryViewModel.seriesMap.collectAsState().value
    val email      = authViewModel.currentUserEmail() ?: "—"

    val totalEditions = galleryMap.size
    val totalSeries   = seriesMap.size
    val countTenho    = galleryMap.values.count { it.ownership == Ownership.TENHO }
    val countQuero    = galleryMap.values.count { it.ownership == Ownership.QUERO }

    // LEITURA — restrito a items possuídos (TENHO): os 3 somam exatamente countTenho (100%)
    //   Lido    = possui + leu        → progresso concluído na coleção própria
    //   Lendo   = possui + lendo      → leitura em andamento
    //   Não lido = possui + sem status → backlog real (itens que tenho mas não li)
    val tenhoItems    = galleryMap.values.filter { it.ownership == Ownership.TENHO }
    val countLido     = tenhoItems.count { it.readStatus == ReadStatus.LIDO  }
    val countLendo    = tenhoItems.count { it.readStatus == ReadStatus.LENDO }
    val countNaoLido  = tenhoItems.count { it.readStatus == null }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(SiegeSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Regular)
    ) {
        // ── Cabeçalho ─────────────────────────────────────────────────────────
        item {
            SiegeText(text = "Perfil", style = SiegeTextStyle.Headline)
        }

        // ── Info do usuário ───────────────────────────────────────────────────
        item {
            SiegeCard(style = SiegeCardStyle.Filled, modifier = Modifier.fillMaxWidth()) {
                SiegeText(
                    text = "E-mail",
                    style = SiegeTextStyle.Label,
                    color = SiegeTheme.colors.textTertiary
                )
                SiegeText(text = email, style = SiegeTextStyle.Body)
            }
        }

        // ── Estatísticas da galeria ───────────────────────────────────────────
        item {
            CollectionStatsCard(
                totalEditions = totalEditions,
                totalSeries   = totalSeries,
                countTenho    = countTenho,
                countQuero    = countQuero,
                countLido     = countLido,
                countLendo    = countLendo,
                countNaoLido  = countNaoLido
            )
        }

        // ── Botão sair ────────────────────────────────────────────────────────
        item {
            SiegeButton(
                text = "Sair",
                style = SiegeButtonStyle.Outlined,
                onClick = { authViewModel.signOut(); onSignOut() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── CollectionStatsCard ───────────────────────────────────────────────────────

@Composable
private fun CollectionStatsCard(
    totalEditions: Int,
    totalSeries:   Int,
    countTenho:    Int,
    countQuero:    Int,
    countLido:     Int,
    countLendo:    Int,
    countNaoLido:  Int
) {
    SiegeCard(
        style      = SiegeCardStyle.Filled,
        title      = "MINHA COLEÇÃO",
        titleColor = SiegeColors.AccentPink,
        modifier   = Modifier.fillMaxWidth()
    ) {

        // ── Hero — totais ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BigStatTile(label = "Volumes", value = totalEditions, color = SiegeColors.AccentCyan)
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(1.dp)
                    .background(SiegeTheme.colors.outline)
            )
            BigStatTile(label = "Séries", value = totalSeries, color = SiegeColors.AccentPink)
        }

        HorizontalDivider(color = SiegeTheme.colors.outline)

        // ── Posse ─────────────────────────────────────────────────────────────
        SiegeText(
            text  = "POSSE",
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textTertiary
        )

        StatusStackedBar(
            segments = buildList {
                if (countTenho > 0) add(countTenho to SiegeColors.AccentCyan)
                if (countQuero > 0) add(countQuero to SiegeColors.AccentAmber)
            },
            total = totalEditions
        )

        StatusProgressRow(
            label   = "Tenho",
            count   = countTenho,
            total   = totalEditions,
            color   = SiegeColors.AccentCyan,
            iconRes = SiegeIcons.ic_bookmarks_solid
        )
        StatusProgressRow(
            label   = "Quero",
            count   = countQuero,
            total   = totalEditions,
            color   = SiegeColors.AccentAmber,
            iconRes = SiegeIcons.ic_flag_solid
        )

        HorizontalDivider(color = SiegeTheme.colors.outline)

        // ── Leitura ─────────────────────────────────────────────────── ──────
        // Percentuais relativos aos volumes possuídos (TENHO = denominador)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SiegeText(
                text  = "LEITURA",
                style = SiegeTextStyle.Label,
                color = SiegeTheme.colors.textTertiary
            )
            SiegeText(
                text  = "de $countTenho volumes",
                style = SiegeTextStyle.Label,
                color = SiegeTheme.colors.textSecondary
            )
        }

        // Barra com os 3 segmentos — preenche 100% dos volumes possuídos
        StatusStackedBar(
            segments = buildList {
                if (countLido    > 0) add(countLido    to SiegeColors.AccentGreen)
                if (countLendo   > 0) add(countLendo   to SiegeColors.AccentPink)
                if (countNaoLido > 0) add(countNaoLido to SiegeColors.Outline)
            },
            total = countTenho
        )

        StatusProgressRow(
            label   = "Lido",
            count   = countLido,
            total   = countTenho,
            color   = SiegeColors.AccentGreen,
            iconRes = SiegeIcons.ic_book_solid
        )
        StatusProgressRow(
            label   = "Lendo",
            count   = countLendo,
            total   = countTenho,
            color   = SiegeColors.AccentPink,
            iconRes = SiegeIcons.ic_glasses_solid
        )
        StatusProgressRow(
            label   = "Não lido",
            count   = countNaoLido,
            total   = countTenho,
            color   = SiegeTheme.colors.textDisabled,
            iconRes = SiegeIcons.ic_book
        )
    }
}

// ── BigStatTile ───────────────────────────────────────────────────────────────

@Composable
private fun BigStatTile(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
    ) {
        SiegeText(
            text  = value.toString(),
            style = SiegeTextStyle.Headline,
            color = color
        )
        SiegeText(
            text  = label,
            style = SiegeTextStyle.Label,
            color = SiegeTheme.colors.textTertiary
        )
    }
}

// ── StatusStackedBar ──────────────────────────────────────────────────────────

/**
 * Barra empilhada que mostra a proporção de cada segmento colorido
 * em relação ao [total]. Segmentos sem itens são ignorados.
 */
@Composable
private fun StatusStackedBar(
    segments: List<Pair<Int, Color>>,
    total: Int,
    modifier: Modifier = Modifier
) {
    val visibleTotal = segments.sumOf { it.first }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(SiegeShapes.Full)
            .background(SiegeTheme.colors.surfaceVariant)
    ) {
        if (total > 0 && visibleTotal > 0) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = visibleTotal.toFloat() / total)
            ) {
                segments.forEach { (count, color) ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(count.toFloat())
                            .background(color)
                    )
                }
            }
        }
    }
}

// ── StatusProgressRow ─────────────────────────────────────────────────────────

/**
 * Linha de progresso por categoria com ícone, label, barra animada e percentual.
 */
@Composable
private fun StatusProgressRow(
    label:   String,
    count:   Int,
    total:   Int,
    color:   Color,
    iconRes: Int
) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    val animatedFraction by animateFloatAsState(
        targetValue    = fraction,
        animationSpec  = tween(500),
        label          = "profile_progress_$label"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SiegeSpacing.XXSmall),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
    ) {
        SiegeIcon(
            icon               = iconRes,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(16.dp)
        )
        SiegeText(
            text     = label,
            style    = SiegeTextStyle.Label,
            modifier = Modifier.width(56.dp)
        )
        LinearProgressIndicator(
            progress  = { animatedFraction },
            modifier  = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(SiegeShapes.Full),
            color      = color,
            trackColor = color.copy(alpha = 0.18f)
        )
        SiegeText(
            text      = "$count  (${(fraction * 100).roundToInt()}%)",
            style     = SiegeTextStyle.Label,
            color     = SiegeTheme.colors.textSecondary,
            modifier  = Modifier.width(68.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ProfileScreenPreview() {
    SiegeTheme(darkTheme = true) {
        // Preview estático — ViewModels reais não disponíveis em preview
    }
}

