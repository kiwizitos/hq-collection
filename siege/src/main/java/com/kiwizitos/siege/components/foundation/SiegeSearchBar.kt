package com.kiwizitos.siege.components.foundation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Barra de pesquisa unificada do Siege Design System.
 *
 * Exibe um campo de texto estilizado com ícone de busca à esquerda e botão
 * de limpar à direita (visível apenas quando há conteúdo).
 *
 * @param value         Texto atual do campo.
 * @param onValueChange Callback chamado a cada alteração do texto.
 * @param modifier      Modificador opcional.
 * @param placeholder   Texto exibido quando o campo está vazio.
 * @param onSearch      Callback chamado ao confirmar a busca pelo teclado. Opcional.
 * @param onClear       Callback chamado ao pressionar o botão de limpar. Por padrão limpa o campo via [onValueChange].
 */
@Composable
fun SiegeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Pesquisar...",
    onSearch: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = SiegeTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            SiegeText(
                text = placeholder,
                style = SiegeTextStyle.Body,
                color = colors.textTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Pesquisar",
                tint = colors.textTertiary
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (onClear != null) onClear() else onValueChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Limpar",
                        tint = colors.textTertiary
                    )
                }
            }
        },
        singleLine = true,
        shape = SiegeShapes.Full,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SiegeColors.AccentPink,
            unfocusedBorderColor = colors.outline,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = SiegeColors.AccentPink,
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch?.invoke(value)
                keyboard?.hide()
            }
        )
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SiegeSearchBar — vazio")
@Composable
private fun SiegeSearchBarEmptyPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeSearchBar(value = "", onValueChange = {})
        }
    }
}

@Preview(showBackground = true, name = "SiegeSearchBar — com texto")
@Composable
private fun SiegeSearchBarFilledPreview() {
    SiegeTheme {
        Column(
            modifier = Modifier.padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            SiegeSearchBar(value = "Watchmen", onValueChange = {})
        }
    }
}

