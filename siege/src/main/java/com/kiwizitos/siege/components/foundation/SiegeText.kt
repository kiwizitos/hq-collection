package com.kiwizitos.siege.components.foundation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Style constants ───────────────────────────────────────────────────────────

/**
 * Estilos tipográficos disponíveis para o [SiegeText].
 *
 * Use as constantes ao invés de instanciar diretamente:
 * ```
 * SiegeText(text = "Título",  style = SiegeTextStyle.Headline)
 * SiegeText(text = "Corpo",   style = SiegeTextStyle.Body)
 * SiegeText(text = "Rótulo",  style = SiegeTextStyle.Label)
 * ```
 */
object SiegeTextStyle {
    /** Título — headlineMedium */
    val Headline @Composable get() = MaterialTheme.typography.headlineMedium

    /** Corpo — bodyMedium */
    val Body @Composable get() = MaterialTheme.typography.bodyMedium

    /** Rótulo — labelMedium */
    val Label @Composable get() = MaterialTheme.typography.labelMedium
}

// ── Unified component ─────────────────────────────────────────────────────────

/**
 * Texto unificado do Siege Design System.
 *
 * O estilo tipográfico é controlado pelo parâmetro [style] usando as constantes de [SiegeTextStyle].
 *
 * @param text      Texto a exibir.
 * @param style     Estilo tipográfico — use [SiegeTextStyle.Headline], [SiegeTextStyle.Body] ou [SiegeTextStyle.Label].
 * @param modifier  Modificador opcional.
 * @param color     Cor do texto. Por padrão herda a cor do tema.
 * @param textAlign Alinhamento do texto.
 * @param maxLines  Número máximo de linhas.
 * @param overflow  Comportamento ao ultrapassar [maxLines].
 */
@Composable
fun SiegeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SiegeText — estilos")
@Composable
private fun SiegeTextStylesPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeText(text = "Headline — Título da tela", style = SiegeTextStyle.Headline)
            SiegeText(text = "Body — Texto de conteúdo e descrições",  style = SiegeTextStyle.Body)
            SiegeText(text = "Label — Rótulo de botão ou chip",        style = SiegeTextStyle.Label)
        }
    }
}

@Preview(showBackground = true, name = "SiegeText — overflow")
@Composable
private fun SiegeTextOverflowPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeText(
                text = "Título muito longo que deve ser cortado com reticências no final da linha",
                style = SiegeTextStyle.Headline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SiegeText(
                text = "Corpo com duas linhas máximas e overflow por ellipsis para textos longos que não cabem no espaço disponível",
                style = SiegeTextStyle.Body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
