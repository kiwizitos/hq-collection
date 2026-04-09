package com.kiwizitos.siege.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Botão preenchido padrão do Design System Siege.
 * 
 * Usado para ações primárias.
 * 
 * Exemplo de uso:
 * ```
 * FilledAppButton(
 *     text = "Importar de 'Lido'",
 *     onClick = { /* ação */ },
 *     containerColor = SiegeColors.AccentCyan
 * )
 * ```
 * 
 * @param text Texto do botão
 * @param onClick Ação ao clicar
 * @param modifier Modificador opcional
 * @param icon Ícone opcional antes do texto
 * @param enabled Se o botão está habilitado
 * @param containerColor Cor de fundo do botão
 * @param contentColor Cor do texto e ícone
 */
@Composable
fun FilledAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = SiegeColors.AccentCyan,
    contentColor: Color = Color.Black
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = SiegeShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(
            horizontal = SiegeSpacing.Regular,
            vertical = SiegeSpacing.Medium
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(SiegeSpacing.Small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Botão com borda (outlined) do Design System Siege.
 * 
 * Usado para ações secundárias.
 * 
 * Exemplo de uso:
 * ```
 * OutlinedAppButton(
 *     text = "Quero completar",
 *     onClick = { /* ação */ },
 *     icon = Icons.Default.Check,
 *     borderColor = SiegeColors.AccentPink
 * )
 * ```
 * 
 * @param text Texto do botão
 * @param onClick Ação ao clicar
 * @param modifier Modificador opcional
 * @param icon Ícone opcional antes do texto
 * @param enabled Se o botão está habilitado
 * @param borderColor Cor da borda
 * @param contentColor Cor do texto e ícone
 */
@Composable
fun OutlinedAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    borderColor: Color = SiegeColors.AccentPink,
    contentColor: Color = SiegeColors.TextPrimary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = SiegeShapes.Medium,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) borderColor else borderColor.copy(alpha = 0.3f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(
            horizontal = SiegeSpacing.Regular,
            vertical = SiegeSpacing.Medium
        )
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = borderColor
            )
            Spacer(modifier = Modifier.width(SiegeSpacing.Small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Botão de destaque com cor de accent (Pink).
 * 
 * Usado para ações críticas ou de alta importância.
 * 
 * Exemplo de uso:
 * ```
 * AccentButton(
 *     text = "Atualizar Status da Edição",
 *     onClick = { /* ação */ },
 *     icon = Icons.Default.Edit
 * )
 * ```
 * 
 * @param text Texto do botão
 * @param onClick Ação ao clicar
 * @param modifier Modificador opcional
 * @param icon Ícone opcional antes do texto
 * @param enabled Se o botão está habilitado
 */
@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    FilledAppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        containerColor = SiegeColors.AccentPink,
        contentColor = Color.White
    )
}
