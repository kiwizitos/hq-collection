package com.kiwizitos.siege.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Card compacto para exibir estatísticas numéricas.
 *
 * Características:
 * - Borda vertical colorida do lado esquerdo (accent)
 * - Número grande em destaque (Headline)
 * - Label descritivo acima do número
 *
 * Exemplo de uso:
 * ```
 * StatCard(
 *     label = "SÉRIES SALVAS",
 *     value = "18",
 *     accentColor = SiegeColors.AccentPink
 * )
 * ```
 *
 * @param label Texto descritivo (ex: "SÉRIES SALVAS")
 * @param value Valor numérico em destaque
 * @param accentColor Cor da borda vertical de destaque
 * @param modifier Modificador opcional
 * @param borderWidth Largura da borda vertical
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = SiegeColors.AccentPink,
    borderWidth: Dp = 8.dp
) {
    val colors = SiegeTheme.colors

    Card(
        modifier = modifier,
        shape = SiegeShapes.Small,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row() {
            Box(
                modifier = Modifier
                    .width(borderWidth)
                    .height(96.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .padding(SiegeSpacing.Regular)
                    .width(128.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XSmall)
            ) {
                SiegeText(
                    text = label.uppercase(),
                    style = SiegeTextStyle.Label,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Start
                )
                SiegeText(
                    text = value,
                    style = SiegeTextStyle.Headline,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "StatCard — Pink")
@Composable
private fun StatCardPinkPreview() {
    SiegeTheme {
        StatCard(
            label = "Séries Salvas",
            value = "18",
            accentColor = SiegeColors.AccentPink
        )
    }
}

@Preview(showBackground = true, name = "StatCard — Cyan")
@Composable
private fun StatCardCyanPreview() {
    SiegeTheme {
        StatCard(
            label = "Volumes Lidos",
            value = "342",
            accentColor = SiegeColors.AccentCyan
        )
    }
}
