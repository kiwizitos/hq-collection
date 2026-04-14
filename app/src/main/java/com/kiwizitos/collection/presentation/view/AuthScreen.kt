package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModel
import com.kiwizitos.collection.presentation.viewmodel.UiState
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Tela de autenticação (login / cadastro).
 *
 * Não usa Scaffold com bottom bar — segue o padrão das telas de detalhe.
 * Toda a UI utiliza tokens do Design System Siege.
 *
 * @param onAuthSuccess Callback chamado após login ou cadastro bem-sucedido.
 * @param viewModel     [AuthViewModel] fornecido externamente.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val colors = SiegeTheme.colors

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }

    // Navega ao sucesso
    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            onAuthSuccess()
            viewModel.resetState()
        }
    }

    val isLoading = authState is UiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SiegeSpacing.Regular),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Regular)
        ) {
            // ── Título ────────────────────────────────────────────────────────
            SiegeText(
                text = if (isRegister) "Criar conta" else "Entrar",
                style = SiegeTextStyle.Headline,
                color = colors.textPrimary
            )
            SiegeText(
                text = if (isRegister) "Crie sua conta para salvar sua coleção"
                else "Acesse sua coleção de quadrinhos",
                style = SiegeTextStyle.Label,
                color = colors.textTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(SiegeSpacing.Large))

            // ── Campo e-mail ──────────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    SiegeText(
                        text = "E-mail",
                        style = SiegeTextStyle.Label,
                        color = colors.textTertiary
                    )
                },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = SiegeShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiegeColors.AccentCyan,
                    unfocusedBorderColor = colors.outline,
                    focusedLabelColor = SiegeColors.AccentCyan,
                    cursorColor = SiegeColors.AccentCyan,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── Campo senha ───────────────────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    SiegeText(
                        text = "Senha",
                        style = SiegeTextStyle.Label,
                        color = colors.textTertiary
                    )
                },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = SiegeShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiegeColors.AccentCyan,
                    unfocusedBorderColor = colors.outline,
                    focusedLabelColor = SiegeColors.AccentCyan,
                    cursorColor = SiegeColors.AccentCyan,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── Mensagem de erro ──────────────────────────────────────────────
            if (authState is UiState.Error) {
                SiegeText(
                    text = (authState as UiState.Error).message,
                    style = SiegeTextStyle.Label,
                    color = SiegeColors.Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Botão principal ───────────────────────────────────────────────
            if (isLoading) {
                CircularProgressIndicator(
                    color = SiegeColors.AccentPink,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                SiegeButton(
                    text = if (isRegister) "Cadastrar" else "Entrar",
                    style = SiegeButtonStyle.Primary,
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    onClick = {
                        if (isRegister) viewModel.signUp(email, password)
                        else viewModel.signIn(email, password)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Toggle login / cadastro ───────────────────────────────────────
            SiegeButton(
                text = if (isRegister) "Já tem conta? Entrar"
                else "Não tem conta? Cadastre-se",
                style = SiegeButtonStyle.Ghost,
                enabled = !isLoading,
                onClick = {
                    isRegister = !isRegister
                    viewModel.resetState()
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AuthScreenPreview() {
    SiegeTheme(darkTheme = true) {
        // Preview estático sem ViewModel real
    }
}

