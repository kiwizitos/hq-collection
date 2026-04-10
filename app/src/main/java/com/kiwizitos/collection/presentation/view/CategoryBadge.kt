package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Exibe os badges de status de um item da galeria.
 *
 * Pode mostrar até dois badges lado a lado: um de posse (Tenho/Quero)
 * e um de leitura (Lido/Lendo), refletindo o [ItemStatus] do item.
 *
 * Reutilizado em [SearchScreen], [CoversScreen] e [DetailsScreen].
 * Usa tokens do Design System Siege para cores, formas e espaçamento.
 */
@Composable
fun CategoryBadge(status: ItemStatus, modifier: Modifier = Modifier) {
    val badges = status.badges()
    if (badges.isEmpty()) return

    Row(
        modifier            = modifier,
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
    ) {
        badges.forEach { (label, color) ->
            StatusChip(label = label, color = color)
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.18f),
                shape = SiegeShapes.Full
            )
            .padding(horizontal = SiegeSpacing.Small, vertical = SiegeSpacing.XXSmall)
    ) {
        SiegeText(
            text  = label.uppercase(),
            style = SiegeTextStyle.Label,
            color = color
        )
    }
}
