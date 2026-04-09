package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kiwizitos.siege.components.foundation.SiegeButton
import com.kiwizitos.siege.components.foundation.SiegeButtonStyle
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeSpacing

// ── Data model ────────────────────────────────────────────────────────────────

/**
 * Representa uma edição individual dentro de uma coleção,
 * usada na lista exibida quando [DetailsScreen] está em modo coleção.
 *
 * @param id      Identificador único da edição.
 * @param number  Número formatado, ex: "#1", "#42".
 * @param name    Nome/título da edição.
 * @param coverUrl URL da capa.
 * @param date    Data de publicação formatada, ex: "abr. de 2012".
 * @param pages   Quantidade de páginas, nullable.
 */
data class EditionItem(
    val id: String,
    val number: String,
    val name: String,
    val coverUrl: String,
    val date: String,
    val pages: String?
)

// ── Main screen ───────────────────────────────────────────────────────────────

/**
 * Tela de detalhe unificada.
 *
 * Funciona em dois modos controlados por [isCollection]:
 * - **Coleção** (`isCollection = true`): exibe capa grande, ficha técnica, botão
 *   externo e lista vertical de edições clicáveis.
 * - **Edição individual** (`isCollection = false`): exibe AppBar com seta de voltar,
 *   capa grande, título, subtítulo e ficha técnica — sem lista de edições.
 *
 * @param title             Título principal (nome da coleção ou título completo da edição).
 * @param subtitle          Subtítulo opcional (ex: "164 pgs • abr. de 2012").
 * @param coverUrl          URL da imagem de capa principal.
 * @param technicalInfo     Lista de pares (rótulo, valor) para a ficha técnica.
 * @param guideButtonText   Texto do botão externo.
 * @param onGuideButtonClick Callback do botão externo.
 * @param onBackClick       Callback da seta de voltar (usada em ambos os modos).
 * @param editionCount      Quantidade de edições — exibido como "X edições" acima da lista.
 * @param editions          Lista de [EditionItem] — usada apenas no modo coleção.
 * @param onEditionClick    Callback ao clicar em uma edição da lista.
 * @param isCollection      `true` → modo coleção; `false` → modo edição individual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    title: String,
    subtitle: String? = null,
    coverUrl: String,
    technicalInfo: List<Pair<String, String>>,
    guideButtonText: String = "Ver no Guia dos Quadrinhos",
    onGuideButtonClick: () -> Unit,
    onBackClick: () -> Unit,
    // Modo coleção
    editionCount: Int? = null,
    editions: List<EditionItem>? = null,
    onEditionClick: ((EditionItem) -> Unit)? = null,
    isCollection: Boolean = true
) {
    val colors = SiegeTheme.colors

    Scaffold(
        // A TopAppBar aparece em ambos os modos — no modo coleção o título fica vazio
        // para não duplicar com o título grande logo abaixo da capa
        topBar = {
            TopAppBar(
                title = {
                    if (!isCollection) {
                        SiegeText(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.surface
                )
            )
        },
        containerColor = colors.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.None)
        ) {

            // ── [Modo coleção] Título grande acima da capa ───────────────────
            if (isCollection) {
                item {
                    SiegeText(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(
                            start = SiegeSpacing.Regular,
                            end = SiegeSpacing.Regular,
                            top = SiegeSpacing.Small,
                            bottom = SiegeSpacing.Regular
                        )
                    )
                }
            }

            // ── Capa principal ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SiegeSpacing.Regular),
                    contentAlignment = Alignment.Center
                ) {
                    CoverImage(
                        url = coverUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .width(if (isCollection) 200.dp else 220.dp)
                            .aspectRatio(2f / 3f)
                            .clip(SiegeShapes.Medium)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(SiegeSpacing.Large)) }

            // ── [Modo edição] Título + subtítulo abaixo da capa ─────────────
            if (!isCollection) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SiegeSpacing.Regular),
                        verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
                    ) {
                        SiegeText(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary
                        )
                        subtitle?.let {
                            SiegeText(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(SiegeSpacing.Large)) }
            }

            // ── Ficha técnica ────────────────────────────────────────────────
            item {
                TechnicalInfoSection(
                    items = technicalInfo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SiegeSpacing.Regular)
                )
            }

            item { Spacer(modifier = Modifier.height(SiegeSpacing.Regular)) }

            // ── Botão externo ────────────────────────────────────────────────
            item {
                SiegeButton(
                    text = guideButtonText,
                    onClick = onGuideButtonClick,
                    style = SiegeButtonStyle.Outlined,
                    icon = Icons.Default.Share,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SiegeSpacing.Regular)
                )
            }

            // ── [Modo coleção] Contador + lista de edições ───────────────────
            if (isCollection && editions != null) {
                item { Spacer(modifier = Modifier.height(SiegeSpacing.Large)) }

                item {
                    SiegeText(
                        text = "${editionCount ?: editions.size} edições",
                        style = MaterialTheme.typography.titleMedium,
                        color = SiegeColors.AccentCyan,
                        modifier = Modifier.padding(
                            start = SiegeSpacing.Regular,
                            bottom = SiegeSpacing.Small
                        )
                    )
                }

                items(items = editions, key = { it.id }) { edition ->
                    EditionListItem(
                        edition = edition,
                        onClick = { onEditionClick?.invoke(edition) }
                    )
                    HorizontalDivider(
                        color = SiegeTheme.colors.outlineVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = SiegeSpacing.Regular)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(SiegeSpacing.XXLarge)) }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

/**
 * Seção de ficha técnica: renderiza uma lista de pares (rótulo → valor)
 * dentro de um Surface com fundo levemente elevado.
 */
@Composable
private fun TechnicalInfoSection(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val colors = SiegeTheme.colors

    Surface(
        modifier = modifier,
        shape = SiegeShapes.Medium,
        color = colors.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SiegeSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
        ) {
            // Cabeçalho da seção
            SiegeText(
                text = "FICHA TÉCNICA",
                style = MaterialTheme.typography.labelMedium,
                color = SiegeColors.AccentPink
            )
            Spacer(modifier = Modifier.height(SiegeSpacing.XXSmall))

            // Linhas de informação
            items.forEach { (label, value) ->
                TechnicalInfoRow(label = label, value = value)
            }
        }
    }
}

/**
 * Linha individual da ficha técnica: rótulo à esquerda, valor à direita.
 */
@Composable
private fun TechnicalInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = SiegeTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SiegeText(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary
        )
        SiegeText(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = SiegeSpacing.Small)
        )
    }
}

/**
 * Item clicável na lista de edições (modo coleção).
 * Capa pequena à esquerda + número (destaque pink) + nome + data + páginas.
 */
@Composable
private fun EditionListItem(
    edition: EditionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SiegeTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SiegeSpacing.Regular, vertical = SiegeSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Regular)
    ) {
        // Capa pequena 60×90 dp
        CoverImage(
            url = edition.coverUrl,
            contentDescription = "${edition.number} - ${edition.name}",
            modifier = Modifier
                .size(width = 60.dp, height = 90.dp)
                .clip(SiegeShapes.Small)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SiegeSpacing.XXSmall)
        ) {
            // Número (ex: "#1") em destaque pink
            SiegeText(
                text = edition.number,
                style = MaterialTheme.typography.labelLarge,
                color = SiegeColors.AccentPink
            )
            // Nome da edição
            SiegeText(
                text = edition.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Data de publicação
            SiegeText(
                text = edition.date,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
            // Páginas (opcional)
            edition.pages?.let {
                SiegeText(
                    text = "$it páginas",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

/**
 * Carrega uma imagem remota via Coil com crossfade e fundo de placeholder.
 * Reutilizado tanto na capa principal quanto nas capas da lista de edições.
 */
@Composable
private fun CoverImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val colors = SiegeTheme.colors
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(colors.surfaceElevated)
    )
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewTechnicalInfo = listOf(
    "Status"       to "Título encerrado",
    "Editora"      to "Panini Comics",
    "Licenciadora" to "Marvel Comics",
    "Gênero"       to "Super-herói"
)

private val previewEditions = listOf(
    EditionItem("#1", "#1", "A Origem do Herói",      "", "jan. de 2012", "120"),
    EditionItem("#2", "#2", "O Retorno das Sombras",  "", "fev. de 2012", "96"),
    EditionItem("#3", "#3", "Batalha Final",           "", "mar. de 2012", null),
    EditionItem("#4", "#4", "Novos Horizontes",        "", "abr. de 2012", "104"),
)

// ── Previews ──────────────────────────────────────────────────────────────────

/** Preview — modo coleção (isCollection = true) */
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DetailsScreen — Coleção")
@Composable
private fun DetailsScreenCollectionPreview() {
    SiegeTheme(darkTheme = true) {
        DetailsScreen(
            title = "Coleção Histórica Marvel",
            coverUrl = "",
            technicalInfo = previewTechnicalInfo,
            onGuideButtonClick = {},
            onBackClick = {},
            editionCount = previewEditions.size,
            editions = previewEditions,
            onEditionClick = {},
            isCollection = true
        )
    }
}

/** Preview — modo edição individual (isCollection = false) */
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "DetailsScreen — Edição")
@Composable
private fun DetailsScreenEditionPreview() {
    SiegeTheme(darkTheme = true) {
        DetailsScreen(
            title = "Coleção Histórica Marvel #1",
            subtitle = "A Origem do Herói • 120 págs. • jan. de 2012",
            coverUrl = "",
            technicalInfo = previewTechnicalInfo,
            onGuideButtonClick = {},
            onBackClick = {},
            isCollection = false
        )
    }
}

