package com.kiwizitos.siege.components.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Style constants ───────────────────────────────────────────────────────────

/**
 * Estilos disponíveis para o [SiegeButton].
 *
 * Use as constantes ao invés de instanciar diretamente:
 * ```
 * SiegeButton(text = "Salvar", style = SiegeButtonStyle.Primary, onClick = {})
 * SiegeButton(text = "Cancelar", style = SiegeButtonStyle.Outlined, onClick = {})
 * SiegeButton(text = "Deletar", style = SiegeButtonStyle.Accent, onClick = {})
 * ```
 */
object SiegeButtonStyle {
    /** Botão preenchido — ações primárias. */
    val Primary = ButtonStyle(
        containerColor = SiegeColors.AccentCyan,
        contentColor = Color.Black,
        borderColor = null
    )

    /** Botão com borda — ações secundárias. */
    val Outlined = ButtonStyle(
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,   // resolvido em runtime via MaterialTheme.colorScheme.onSurface
        borderColor = SiegeColors.AccentPink
    )

    /** Botão de destaque — ações críticas ou de alta importância. */
    val Accent = ButtonStyle(
        containerColor = SiegeColors.AccentPink,
        contentColor = Color.White,
        borderColor = null
    )

    /** Botão fantasma — apenas texto/ícone, sem fundo, borda ou sombra. Ideal para ações terciárias. */
    val Ghost = ButtonStyle(
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,
        borderColor = null
    )
}

/**
 * Dados de estilo de um botão. Utilize as constantes de [SiegeButtonStyle].
 */
data class ButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color?
)

// ── Unified component ─────────────────────────────────────────────────────────

/**
 * Botão unificado do Siege Design System.
 *
 * O visual é controlado pelo parâmetro [style] usando as constantes de [SiegeButtonStyle].
 *
 * @param text          Texto do botão.
 * @param onClick       Ação ao clicar.
 * @param style         Visual do botão — use [SiegeButtonStyle.Primary], [SiegeButtonStyle.Outlined], [SiegeButtonStyle.Accent] ou [SiegeButtonStyle.Ghost].
 * @param modifier      Modificador opcional.
 * @param icon          Ícone opcional exibido **antes** do texto.
 * @param trailingIcon  Ícone opcional exibido **depois** do texto.
 * @param enabled       Se o botão está habilitado.
 */
@Composable
fun SiegeButton(
    text: String,
    onClick: () -> Unit,
    style: ButtonStyle = SiegeButtonStyle.Primary,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    // Resolve Color.Unspecified para a cor de conteúdo padrão do tema
    val resolvedContentColor = if (style.contentColor == Color.Unspecified)
        MaterialTheme.colorScheme.onSurface
    else
        style.contentColor

    val resolvedStyle = style.copy(contentColor = resolvedContentColor)

    val border = resolvedStyle.borderColor?.let { color ->
        BorderStroke(
            width = 1.5.dp,
            color = if (enabled) color else color.copy(alpha = 0.3f)
        )
    }

    val colors = ButtonDefaults.buttonColors(
        containerColor = resolvedStyle.containerColor,
        contentColor = resolvedStyle.contentColor,
        disabledContainerColor = resolvedStyle.containerColor.copy(alpha = 0.3f),
        disabledContentColor = resolvedStyle.contentColor.copy(alpha = 0.3f)
    )

    val padding = PaddingValues(
        horizontal = SiegeSpacing.Regular,
        vertical = SiegeSpacing.Medium
    )

    if (border != null) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = SiegeShapes.Medium,
            border = border,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = resolvedStyle.containerColor,
                contentColor = resolvedStyle.contentColor,
                disabledContentColor = resolvedStyle.contentColor.copy(alpha = 0.3f)
            ),
            contentPadding = padding
        ) {
            ButtonContent(text = text, icon = icon, trailingIcon = trailingIcon, tint = resolvedStyle.borderColor!!)
        }
    } else if (resolvedStyle.containerColor == Color.Transparent) {
        // Ghost — TextButton: sem sombra, sem fundo, sem borda
        TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = SiegeShapes.Medium,
            colors = ButtonDefaults.textButtonColors(
                contentColor = resolvedStyle.contentColor,
                disabledContentColor = resolvedStyle.contentColor.copy(alpha = 0.3f)
            ),
            contentPadding = padding
        ) {
            ButtonContent(text = text, icon = icon, trailingIcon = trailingIcon, tint = resolvedStyle.contentColor)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = SiegeShapes.Medium,
            colors = colors,
            contentPadding = padding,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            ButtonContent(text = text, icon = icon, trailingIcon = trailingIcon, tint = resolvedStyle.contentColor)
        }
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?, trailingIcon: ImageVector?, tint: Color) {
    icon?.let {
        Icon(
            imageVector = it,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(SiegeSpacing.Small))
    }
    Text(text = text, style = MaterialTheme.typography.labelLarge)
    trailingIcon?.let {
        Spacer(modifier = Modifier.width(SiegeSpacing.Small))
        Icon(
            imageVector = it,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SiegeButton — todos os estilos")
@Composable
private fun SiegeButtonStylesPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeButton(
                text = "Primary",
                style = SiegeButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Outlined",
                style = SiegeButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Accent",
                style = SiegeButtonStyle.Accent,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Ghost",
                style = SiegeButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "SiegeButton — com ícone")
@Composable
private fun SiegeButtonIconPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeButton(
                text = "Editar",
                style = SiegeButtonStyle.Primary,
                icon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Editar",
                style = SiegeButtonStyle.Outlined,
                icon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "SiegeButton — desabilitado")
@Composable
private fun SiegeButtonDisabledPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeButton(
                text = "Primary (disabled)",
                style = SiegeButtonStyle.Primary,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Outlined (disabled)",
                style = SiegeButtonStyle.Outlined,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Accent (disabled)",
                style = SiegeButtonStyle.Accent,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "SiegeButton — trailing icon")
@Composable
private fun SiegeButtonTrailingIconPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeButton(
                text = "Ver tudo",
                style = SiegeButtonStyle.Ghost,
                trailingIcon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Próximo",
                style = SiegeButtonStyle.Primary,
                trailingIcon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
            SiegeButton(
                text = "Editar",
                style = SiegeButtonStyle.Outlined,
                icon = Icons.Filled.Edit,
                trailingIcon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
        }
    }
}

