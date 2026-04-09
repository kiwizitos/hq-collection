package com.kiwizitos.siege.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Style constants ───────────────────────────────────────────────────────────

/**
 * Estilos disponíveis para o [SiegeCard].
 *
 * ```
 * SiegeCard(style = SiegeCardStyle.Filled,   content = { ... })
 * SiegeCard(style = SiegeCardStyle.Elevated, content = { ... })
 * SiegeCard(style = SiegeCardStyle.Outlined, content = { ... })
 * ```
 */
object SiegeCardStyle {
    /** Card preenchido com cor de superfície variante. */
    val Filled   = CardStyle.Filled
    /** Card com elevação (sombra). */
    val Elevated = CardStyle.Elevated
    /** Card com borda. */
    val Outlined = CardStyle.Outlined
}

sealed interface CardStyle {
    data object Filled   : CardStyle
    data object Elevated : CardStyle
    data object Outlined : CardStyle
}

// ── Unified component ─────────────────────────────────────────────────────────

/**
 * Card unificado do Siege Design System.
 *
 * O visual é controlado pelo parâmetro [style] usando as constantes de [SiegeCardStyle].
 *
 * Suporta slots opcionais para compor layouts ricos sem perder a consistência visual:
 * - [title] / [titleColor] — rótulo de cabeçalho colorido
 * - [trailingContent] — slot à direita do conteúdo principal (ex: número grande, ícone)
 * - [bottomContent] — slot abaixo do conteúdo principal (ex: botão de ação)
 *
 * @param style          Visual do card — use [SiegeCardStyle.Filled], [SiegeCardStyle.Elevated] ou [SiegeCardStyle.Outlined].
 * @param modifier        Modificador opcional.
 * @param onClick         Ação ao clicar. Quando nulo, o card não é clicável.
 * @param title           Título opcional exibido no topo do card.
 * @param titleColor      Cor do título. Por padrão usa [SiegeColors.AccentPink].
 * @param trailingContent Slot opcional posicionado à direita do conteúdo (ex: número grande decorativo).
 * @param bottomContent   Slot opcional posicionado abaixo do conteúdo (ex: botão, linha de ações).
 * @param content         Conteúdo principal do card.
 */
@Composable
fun SiegeCard(
    modifier: Modifier = Modifier,
    style: CardStyle = SiegeCardStyle.Filled,
    onClick: (() -> Unit)? = null,
    title: String? = null,
    titleColor: Color = SiegeColors.AccentPink,
    trailingContent: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val inner: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SiegeSpacing.Regular),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
            ) {
                title?.let {
                    SiegeText(
                        text = it,
                        style = SiegeTextStyle.Label,
                        color = titleColor
                    )
                }
                content()
            }
            trailingContent?.let {
                Column(modifier = Modifier.padding(start = SiegeSpacing.Regular)) { it() }
            }
        }
        bottomContent?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SiegeSpacing.Regular,
                        end = SiegeSpacing.Regular,
                        bottom = SiegeSpacing.Regular
                    )
            ) {
                Spacer(modifier = Modifier.height(SiegeSpacing.XSmall))
                it()
            }
        }
    }

    when (style) {
        CardStyle.Filled -> FilledCardContainer(modifier, onClick, inner)
        CardStyle.Elevated -> ElevatedCardContainer(modifier, onClick, inner)
        CardStyle.Outlined -> OutlinedCardContainer(modifier, onClick, inner)
    }
}

// ── Private containers ────────────────────────────────────────────────────────

@Composable
private fun FilledCardContainer(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    val colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
    if (onClick != null) {
        Card(modifier = modifier, shape = MaterialTheme.shapes.medium, colors = colors, onClick = onClick) { content() }
    } else {
        Card(modifier = modifier, shape = MaterialTheme.shapes.medium, colors = colors) { content() }
    }
}

@Composable
private fun ElevatedCardContainer(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    val elevation: CardElevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    if (onClick != null) {
        ElevatedCard(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = elevation, onClick = onClick) { content() }
    } else {
        ElevatedCard(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = elevation) { content() }
    }
}

@Composable
private fun OutlinedCardContainer(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    if (onClick != null) {
        OutlinedCard(modifier = modifier, shape = MaterialTheme.shapes.medium, border = border, onClick = onClick) { content() }
    } else {
        OutlinedCard(modifier = modifier, shape = MaterialTheme.shapes.medium, border = border) { content() }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SiegeCard — estilos")
@Composable
private fun SiegeCardStylesPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeCard(style = SiegeCardStyle.Filled, modifier = Modifier.fillMaxWidth()) {
                SiegeText(text = "Filled Card", style = SiegeTextStyle.Body)
            }
            SiegeCard(style = SiegeCardStyle.Elevated, modifier = Modifier.fillMaxWidth()) {
                SiegeText(text = "Elevated Card", style = SiegeTextStyle.Body)
            }
            SiegeCard(style = SiegeCardStyle.Outlined, modifier = Modifier.fillMaxWidth()) {
                SiegeText(text = "Outlined Card", style = SiegeTextStyle.Body)
            }
        }
    }
}

@Preview(showBackground = true, name = "SiegeCard — com título e trailing")
@Composable
private fun SiegeCardTitleTrailingPreview() {
    SiegeTheme {
        SiegeCard(
            style = SiegeCardStyle.Elevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(SiegeSpacing.Regular),
            title = "Sinopse",
            titleColor = SiegeColors.AccentPink,
            trailingContent = {
                SiegeText(
                    text = "99",
                    style = SiegeTextStyle.Headline,
                    color = SiegeTheme.colors.textTertiary.copy(alpha = 0.2f)
                )
            }
        ) {
            SiegeText(
                text = "Um feiticeiro tentando capturar a Morte para negociar pela vida de seu mestre acidentalmente libera três sonhos cativos.",
                style = SiegeTextStyle.Body
            )
        }
    }
}

@Preview(showBackground = true, name = "SiegeCard — com título e botão")
@Composable
private fun SiegeCardTitleButtonPreview() {
    SiegeTheme {
        SiegeCard(
            style = SiegeCardStyle.Outlined,
            modifier = Modifier
                .fillMaxWidth()
                .padding(SiegeSpacing.Regular),
            title = "Coleção",
            titleColor = SiegeColors.AccentCyan,
            bottomContent = {
                SiegeButton(
                    text = "Ver todos",
                    style = SiegeButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                )
            }
        ) {
            SiegeText(text = "18 séries · 342 volumes", style = SiegeTextStyle.Body)
        }
    }
}

