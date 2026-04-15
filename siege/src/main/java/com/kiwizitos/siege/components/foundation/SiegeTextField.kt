package com.kiwizitos.siege.components.foundation

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes

/**
 * Campo de texto unificado do Siege Design System.
 *
 * Wrapper sobre [OutlinedTextField] com cores, forma e tipografia alinhados
 * ao tema Siege. Aceita transformações visuais para campos de senha.
 *
 * Exemplo:
 * ```
 * SiegeTextField(
 *     value = email,
 *     onValueChange = { email = it },
 *     label = "E-mail",
 *     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
 * )
 * ```
 *
 * @param value               Texto atual do campo.
 * @param onValueChange       Callback chamado a cada alteração.
 * @param label               Rótulo exibido dentro/acima do campo.
 * @param modifier            Modificador opcional.
 * @param enabled             Controla se o campo está habilitado.
 * @param singleLine          Se true, limita o campo a uma linha.
 * @param visualTransformation Transformação visual — use `PasswordVisualTransformation` para senhas.
 * @param keyboardOptions     Opções do teclado virtual (tipo, ação IME etc.).
 */
@Composable
fun SiegeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = SiegeTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            SiegeText(
                text = label,
                style = SiegeTextStyle.Label,
                color = colors.textTertiary
            )
        },
        singleLine = singleLine,
        enabled = enabled,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = SiegeShapes.Medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SiegeColors.AccentCyan,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = SiegeColors.AccentCyan,
            cursorColor = SiegeColors.AccentCyan,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            disabledBorderColor = colors.outline.copy(alpha = 0.4f),
            disabledTextColor = colors.textTertiary,
            disabledLabelColor = colors.textTertiary
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SiegeTextFieldPreview() {
    SiegeTheme(darkTheme = true) {
        SiegeTextField(
            value = "exemplo@email.com",
            onValueChange = {},
            label = "E-mail"
        )
    }
}

