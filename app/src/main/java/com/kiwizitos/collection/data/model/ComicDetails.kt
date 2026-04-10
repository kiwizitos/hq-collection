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
    val publishedIn: String? = null,   // "janeiro de 1996"
    val publisher: String? = null,     // "Abril"
    val licensor: String? = null,      // "Marvel Comics"
    val category: String? = null,      // "Revista Periódica"
    val genre: String? = null,         // "Super-heróis"
    val status: String? = null,        // "Título encerrado"
    val pages: String? = null,         // "52"
    val format: String? = null,        // "Americano (17 x 26 cm)"
    val coverPrice: String? = null,    // "R$ 2,50"
    val coverUrl: String? = null,
    val coverArtist: String? = null    // "Roger Cruz"
)

