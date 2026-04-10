package com.kiwizitos.collection.data.model

/**
 * Resultado de busca de uma série/coleção de quadrinhos.
 *
 * @param title             Título da série.
 * @param relativeLink      Caminho relativo da série no Guia dos Quadrinhos (ex: "titulo/batman").
 * @param publisher         Editora nacional.
 * @param originalPublisher Editora original (ex: DC Comics).
 * @param year              Ano de publicação (ex: "1997").
 * @param issueCount        Número de edições (ex: "12").
 */
data class SerieResult(
    val title: String,
    val relativeLink: String,
    val publisher: String,
    val originalPublisher: String,
    val year: String,
    val issueCount: String
)

