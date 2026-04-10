package com.kiwizitos.collection.data.model

/**
 * Informações detalhadas de uma edição do Guia dos Quadrinhos.
 *
 * Campos mapeados diretamente do HTML:
 * - span#nome_titulo_lb, span#data_publi, a#editora_link, span#licenciador,
 *   span#categoria, span#genero, span#status, span#paginas, span#formato,
 *   span#preco, div#cover img
 */
data class ComicDetails(
    val title: String,
    val publishedIn: String? = null,
    val publisher: String? = null,
    val licensor: String? = null,
    val category: String? = null,
    val genre: String? = null,
    val status: String? = null,
    val pages: String? = null,
    val format: String? = null,
    val coverPrice: String? = null,
    val coverUrl: String? = null,
    val coverArtist: String? = null,
    // Extraído do link "Galeria de capas" → href='../../../capas/slug/codigo'
    // Usado para navegar de volta à lista de capas da série na DetailsScreen.
    val seriesUrl: String? = null,
    val seriesTitle: String? = null
)

