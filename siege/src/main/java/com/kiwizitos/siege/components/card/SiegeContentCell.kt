package com.kiwizitos.siege.components.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

/**
 * Diferencia visualmente uma série (múltiplos volumes) de um volume único.
 *
 * - [Series]  → ícone de biblioteca — indica que o card abre uma lista de edições.
 * - [Volume]  → ícone de livro único — indica que o card abre o detalhe direto.
 */
enum class ContentType {
    /** Coleção com múltiplas edições (ex: Amazing Spider-Man). */
    Series,
    /** Volume único / graphic novel (ex: Watchmen). */
    Volume
}

// ── Style constants ───────────────────────────────────────────────────────────

/**
 * Estilos disponíveis para o [SiegeContentCell].
 *
 * ```
 * ContentCard(style = ContentCardStyle.Cover, coverImage = ..., title = "Watchmen")
 * ContentCard(style = ContentCardStyle.Row,   coverImage = ..., title = "One Piece")
 * ```
 */
object ContentCardStyle {
    /** Card de capa (120dp fixo): ideal para carrosséis horizontais. */
    val Cover = ContentCardVariant.Cover
    /** Card em grade: preenche o espaço disponível. Ideal para grids. */
    val Grid  = ContentCardVariant.Grid
    /** Card em linha: miniatura à esquerda, informações à direita. Ideal para listas verticais. */
    val Row   = ContentCardVariant.Row
}

sealed interface ContentCardVariant {
    data object Cover : ContentCardVariant
    data object Grid  : ContentCardVariant
    data object Row   : ContentCardVariant
}

// ── Unified component ─────────────────────────────────────────────────────────

/**
 * Card de conteúdo do Siege Design System.
 *
 * O visual é controlado pelo parâmetro [style] usando as constantes de [ContentCardStyle]:
 * - **Cover** — card de capa com largura fixa (120dp). Ideal para carrosséis horizontais.
 * - **Grid**  — card de capa que preenche o espaço disponível. Ideal para grids.
 * - **Row**   — item de lista com miniatura à esquerda e informações à direita. Ideal para listas verticais.
 *
 * @param coverImage    Imagem da capa.
 * @param title         Título principal.
 * @param modifier      Modificador opcional.
 * @param style         Variante visual — use as constantes de [ContentCardStyle].
 * @param contentType   Tipo do conteúdo — [ContentType.Series] mostra ícone de biblioteca;
 *                      [ContentType.Volume] mostra ícone de livro único.
 * @param subtitle      Subtítulo opcional (autor, editora, volume).
 * @param badges        Badges de status sobrepostos na capa.
 * @param progress      Progresso de 0.0 a 1.0 para a barra de progresso.
 * @param progressText  Texto do progresso, ex: "11/12".
 * @param onClick       Ação ao clicar no card.
 */
@Composable
fun SiegeContentCell(
    coverImage: Painter,
    title: String,
    modifier: Modifier = Modifier,
    style: ContentCardVariant = ContentCardStyle.Cover,
    contentType: ContentType = ContentType.Series,
    subtitle: String? = null,
    badges: List<BadgeData> = emptyList(),
    progress: Float? = null,
    progressText: String? = null,
    onClick: (() -> Unit)? = null
) {
    when (style) {
        ContentCardVariant.Cover -> CoverContentCard(
            coverImage = coverImage,
            title = title,
            modifier = modifier,
            contentType = contentType,
            subtitle = subtitle,
            badges = badges,
            progress = progress,
            progressText = progressText,
            onClick = onClick
        )
        ContentCardVariant.Grid -> GridContentCard(
            coverImage = coverImage,
            title = title,
            modifier = modifier,
            contentType = contentType,
            subtitle = subtitle,
            badges = badges,
            progress = progress,
            onClick = onClick
        )
        ContentCardVariant.Row -> RowContentCard(
            coverImage = coverImage,
            title = title,
            modifier = modifier,
            contentType = contentType,
            subtitle = subtitle,
            badge = badges.firstOrNull(),
            progress = progress,
            progressText = progressText,
            onClick = onClick
        )
    }
}

// ── Private variants ──────────────────────────────────────────────────────────

@Composable
private fun CoverContentCard(
    coverImage: Painter,
    title: String,
    modifier: Modifier = Modifier,
    contentType: ContentType,
    subtitle: String?,
    badges: List<BadgeData>,
    progress: Float?,
    progressText: String?,
    onClick: (() -> Unit)?
) {
    val colors = SiegeTheme.colors

    Card(
        modifier = modifier.width(120.dp),
        shape = SiegeShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Capa
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
                // Indicador de tipo — canto inferior esquerdo
                ContentTypeIndicator(
                    contentType = contentType,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(SiegeSpacing.XSmall)
                )
                if (badges.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(SiegeSpacing.XSmall),
                        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
                    ) {
                        badges.take(2).forEach { StatusBadge(text = it.text, color = it.color) }
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
                SiegeText(
                    text = title,
                    style = SiegeTextStyle.Body,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    SiegeText(
                        text = it,
                        style = SiegeTextStyle.Label,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (progress != null) {
                    Spacer(modifier = Modifier.height(SiegeSpacing.XXSmall))
                    Column(verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            progressText?.let {
                                SiegeText(text = it, style = SiegeTextStyle.Label, color = colors.textPrimary)
                            }
                            SiegeText(
                                text = "${(progress * 100).toInt()}%",
                                style = SiegeTextStyle.Label,
                                color = colors.textTertiary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(SiegeShapes.Full),
                            color = SiegeColors.Warning,
                            trackColor = colors.outlineVariant,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridContentCard(
    coverImage: Painter,
    title: String,
    modifier: Modifier,
    contentType: ContentType,
    subtitle: String?,
    badges: List<BadgeData>,
    progress: Float?,
    onClick: (() -> Unit)?
) {
    val colors = SiegeTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SiegeShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                // Indicador de tipo — canto inferior esquerdo
                ContentTypeIndicator(
                    contentType = contentType,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(SiegeSpacing.XSmall)
                )
                if (badges.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(SiegeSpacing.XSmall),
                        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
                    ) {
                        badges.take(2).forEach { StatusBadge(text = it.text, color = it.color) }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SiegeSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
            ) {
                SiegeText(
                    text = title,
                    style = SiegeTextStyle.Body,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    SiegeText(
                        text = it,
                        style = SiegeTextStyle.Label,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (progress != null) {
                    Spacer(modifier = Modifier.height(SiegeSpacing.XXSmall))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(SiegeShapes.Full),
                        color = SiegeColors.Warning,
                        trackColor = colors.outlineVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }
        }
    }
}

@Composable
private fun RowContentCard(
    coverImage: Painter,
    title: String,
    modifier: Modifier,
    contentType: ContentType,
    subtitle: String?,
    badge: BadgeData?,
    progress: Float?,
    progressText: String?,
    onClick: (() -> Unit)?
) {
    val colors = SiegeTheme.colors

    Card(
        modifier = modifier,
        shape = SiegeShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SiegeSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Regular),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniatura da capa com indicador de tipo
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .aspectRatio(0.7f)
                    .clip(SiegeShapes.Small)
            ) {
                Image(
                    painter = coverImage,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                ContentTypeIndicator(
                    contentType = contentType,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(SiegeSpacing.XXSmall)
                )
            }

            // Informações
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
            ) {
                SiegeText(
                    text = title,
                    style = SiegeTextStyle.Body,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    SiegeText(
                        text = it,
                        style = SiegeTextStyle.Label,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                badge?.let {
                    StatusBadge(text = it.text, color = it.color)
                }
                if (progress != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            progressText?.let {
                                SiegeText(text = it, style = SiegeTextStyle.Label, color = colors.textPrimary)
                            }
                            SiegeText(
                                text = "${(progress * 100).toInt()}%",
                                style = SiegeTextStyle.Label,
                                color = colors.textTertiary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(SiegeShapes.Full),
                            color = SiegeColors.Warning,
                            trackColor = colors.outlineVariant,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                    }
                }
            }
        }
    }
}

// ── Shared internals ──────────────────────────────────────────────────────────

/**
 * Indicador visual de tipo de conteúdo, sobreposto à capa no canto inferior esquerdo.
 *
 * - [ContentType.Series] → ícone de biblioteca (múltiplos volumes)
 * - [ContentType.Volume] → ícone de livro único
 */
@Composable
private fun ContentTypeIndicator(
    contentType: ContentType,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (contentType) {
        ContentType.Series -> Pair(Icons.AutoMirrored.Filled.LibraryBooks, "Série")
        ContentType.Volume -> Pair(Icons.Filled.Book, "Volume único")
    }
    Surface(
        modifier = modifier,
        shape = SiegeShapes.Small,
        color = SiegeColors.BackgroundDark.copy(alpha = 0.75f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = SiegeColors.TextTertiary,
            modifier = Modifier
                .padding(SiegeSpacing.XXSmall)
                .size(14.dp)
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    // Mapeia textos conhecidos para ícones semânticos
    val icon = when (text.uppercase()) {
        "POSSUÍDA", "POSSUIDO" -> Icons.Filled.Bookmark
        "LIDA", "LIDO"         -> Icons.Filled.CheckCircle
        "RARO", "RARA"         -> Icons.Filled.Star
        "NOVA", "NOVO"         -> Icons.Filled.Verified
        else                   -> Icons.Filled.Bookmark
    }
    Surface(
        modifier = modifier,
        shape = SiegeShapes.Small,
        color = color.copy(alpha = 0.92f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Black,
            modifier = Modifier
                .padding(SiegeSpacing.XXSmall)
                .size(14.dp)
        )
    }
}

/**
 * Dados de um badge de status sobreposto à capa.
 *
 * @param text  Texto do badge (ex: "RARO", "POSSUÍDA").
 * @param color Cor de fundo do badge.
 */
data class BadgeData(val text: String, val color: Color)

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "ContentCard — Cover completo")
@Composable
private fun ContentCardCoverFullPreview() {
    SiegeTheme {
        SiegeContentCell(
            coverImage = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
            title = "Watchmen",
            style = ContentCardStyle.Cover,
            subtitle = "DC Comics / Alan Moore",
            progress = 0.75f,
            progressText = "9/12",
            badges = listOf(
                BadgeData("RARO", SiegeColors.AccentPink),
                BadgeData("POSSUÍDA", SiegeColors.AccentCyan)
            )
        )
    }
}

@Preview(showBackground = true, name = "ContentCard — Cover mínimo")
@Composable
private fun ContentCardCoverMinimalPreview() {
    SiegeTheme {
        SiegeContentCell(
            coverImage = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
            title = "Berserk",
            style = ContentCardStyle.Cover
        )
    }
}

@Preview(showBackground = true, name = "ContentCard — Row com progresso")
@Composable
private fun ContentCardRowProgressPreview() {
    SiegeTheme {
        SiegeContentCell(
            coverImage = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
            title = "Watchmen",
            style = ContentCardStyle.Row,
            subtitle = "DC Comics / Alan Moore",
            progress = 0.75f,
            progressText = "9/12",
            badges = listOf(BadgeData("RARO", SiegeColors.AccentPink))
        )
    }
}

@Preview(showBackground = true, name = "ContentCard — Row mínimo")
@Composable
private fun ContentCardRowMinimalPreview() {
    SiegeTheme {
        SiegeContentCell(
            coverImage = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
            title = "One Piece",
            style = ContentCardStyle.Row,
            subtitle = "Shonen Jump"
        )
    }
}

