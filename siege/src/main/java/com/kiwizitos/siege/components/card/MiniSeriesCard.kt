package com.kiwizitos.siege.components.card

import android.R.drawable.ic_menu_gallery
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Versão compacta do ContentCard para exibição em carrosséis ou listas menores.
 * 
 * Características:
 * - Menor que o ContentCard
 * - Foco na imagem
 * - Informações mínimas
 * - Badges opcionais
 * 
 * Exemplo de uso:
 * ```
 * MiniSeriesCard(
 *     coverImage = painterResource(R.drawable.one_piece),
 *     title = "One Piece",
 *     subtitle = "Vol. 101",
 *     badge = BadgeData("NOVA", SiegeColors.AccentCyan)
 * )
 * ```
 * 
 * @param coverImage Imagem da capa
 * @param title Título
 * @param modifier Modificador opcional
 * @param subtitle Subtítulo opcional
 * @param badge Badge único opcional
 * @param onClick Ação ao clicar
 */
@Composable
fun MiniSeriesCard(
    coverImage: Painter,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: BadgeData? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.width(160.dp),
        shape = SiegeShapes.Medium,
        colors = CardDefaults.cardColors(
            containerColor = SiegeColors.SurfaceVariantDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagem de capa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .clip(SiegeShapes.Medium)
            ) {
                Image(
                    painter = coverImage,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Badge único
                badge?.let {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(SiegeSpacing.XSmall),
                        shape = SiegeShapes.Small,
                        color = it.color.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = it.text.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = SiegeColors.TextPrimary,
                            modifier = Modifier.padding(
                                horizontal = SiegeSpacing.XSmall,
                                vertical = SiegeSpacing.XXSmall
                            )
                        )
                    }
                }
            }
            
            // Informações
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SiegeSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
            ) {
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SiegeColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Subtítulo
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = SiegeColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MiniSeriesCardPreview() {
    MiniSeriesCard(
        coverImage = painterResource(id = ic_menu_gallery),
        title = "One Piece",
        subtitle = "Vol. 101",
        badge = BadgeData("NOVA", SiegeColors.AccentCyan)
    )
}