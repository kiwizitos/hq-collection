package com.kiwizitos.collection.presentation.view.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.siege.components.foundation.SiegeIcon
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Exibe os badges de status de um item da galeria como ícones.
 *
 * Pode mostrar até dois badges lado a lado: um de posse (Tenho/Quero)
 * e um de leitura (Lido/Lendo), refletindo o [ItemStatus] do item.
 *
 * Reutilizado em [com.kiwizitos.collection.presentation.view.SearchScreen], [com.kiwizitos.collection.presentation.view.CoversScreen] e [com.kiwizitos.collection.presentation.view.DetailsScreen].
 * Usa tokens do Design System Siege para cores, formas e espaçamento.
 */
@Composable
fun CategoryBadge(status: ItemStatus, modifier: Modifier = Modifier) {
    val badges = buildList {
        status.ownership?.let { add(it.badgeIcon to it.badgeColor) }
        status.readStatus?.let { add(it.badgeIcon to it.badgeColor) }
    }
    if (badges.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
    ) {
        badges.forEach { (icon, color) ->
            IconChip(icon = icon, color = color)
        }
    }
}

@Composable
private fun IconChip(@DrawableRes icon: Int, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.18f),
                shape = SiegeShapes.Full
            )
            .padding(SiegeSpacing.XXSmall)
    ) {
        SiegeIcon(
            icon = icon,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
    }
}
