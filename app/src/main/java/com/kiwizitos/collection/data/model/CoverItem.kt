package com.kiwizitos.collection.data.model

/**
 * Representa uma capa individual dentro de uma série.
 *
 * @param title        Título ou número do volume (ex: "#1 — Nascido no Crime").
 * @param relativeLink Caminho relativo para a página de detalhes da edição.
 * @param coverUrl     URL da imagem de capa em miniatura.
 * @param year         Ano de publicação, nullable.
 */
data class CoverItem(
    val title: String,
    val relativeLink: String,
    val coverUrl: String,
    val year: String?
)

