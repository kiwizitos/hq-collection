package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kiwizitos.collection.data.model.Ownership
import com.kiwizitos.collection.data.model.ReadStatus
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.siege.components.card.SiegeCard
import com.kiwizitos.siege.components.card.SiegeCardStyle
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    galleryViewModel: GalleryViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val galleryMap = galleryViewModel.galleryMap.collectAsState().value
    val seriesMap = galleryViewModel.seriesMap.collectAsState().value
    val email = authViewModel.currentUserEmail() ?: "—"

    val totalEditions = galleryMap.size
    val totalSeries = seriesMap.size

    // Contagem por status de posse — edições
    val countTenho = galleryMap.values.count { it.ownership == Ownership.TENHO }
    val countQuero = galleryMap.values.count { it.ownership == Ownership.QUERO }

    // Contagem por status de leitura — edições
    val countLido = galleryMap.values.count { it.readStatus == ReadStatus.LIDO }
    val countLendo = galleryMap.values.count { it.readStatus == ReadStatus.LENDO }

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
            SiegeCard(
                style = SiegeCardStyle.Filled,
                modifier = Modifier.fillMaxWidth()
            ) {
                SiegeText(
                    text = "E-mail",
                    style = SiegeTextStyle.Label,
                    color = SiegeTheme.colors.textTertiary
                )
                SiegeText(
                    text = email,
                    style = SiegeTextStyle.Body,
                    color = SiegeTheme.colors.textPrimary
                )
            }
        }

        // ── Stats da galeria ──────────────────────────────────────────────────
        item {
            SiegeCard(
                style = SiegeCardStyle.Filled,
                title = "GALERIA",
                modifier = Modifier.fillMaxWidth()
            ) {
                StatRow("Volumes salvos", "$totalEditions", SiegeColors.AccentCyan)
                StatRow("Séries salvas", "$totalSeries", SiegeColors.AccentCyan)
                HorizontalDivider(
                    color = SiegeTheme.colors.outline,
                    modifier = Modifier.padding(vertical = SiegeSpacing.XSmall)
                )
                StatRow("Tenho", "$countTenho", Ownership.TENHO.badgeColor)
                StatRow("Quero", "$countQuero", Ownership.QUERO.badgeColor)
                HorizontalDivider(
                    color = SiegeTheme.colors.outline,
                    modifier = Modifier.padding(vertical = SiegeSpacing.XSmall)
                )
                StatRow("Lido", "$countLido", ReadStatus.LIDO.badgeColor)
                StatRow("Lendo", "$countLendo", ReadStatus.LENDO.badgeColor)
            }
        }

        // ── Botão sair ────────────────────────────────────────────────────────
        item {
            SiegeButton(
                text = "Sair",
                style = SiegeButtonStyle.Outlined,
                onClick = {
                    authViewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SiegeText(text = label, style = SiegeTextStyle.Label, color = SiegeTheme.colors.textSecondary)
        SiegeText(text = value, style = SiegeTextStyle.Label, color = valueColor)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ProfileScreenPreview() {
    SiegeTheme(darkTheme = true) {
        // Preview estático — ViewModels reais não disponíveis em preview
    }
}

