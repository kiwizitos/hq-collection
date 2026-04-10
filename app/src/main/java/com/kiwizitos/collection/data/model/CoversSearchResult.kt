package com.kiwizitos.collection.data.model

/**
 * Resultado de uma busca de capas de uma série específica.
 *
 * @param seriesTitle    Título da série para exibição no cabeçalho.
 * @param covers         Lista de capas retornadas na página atual.
 * @param paginationInfo Estado de paginação para navegar para próximas páginas.
 */
data class CoversSearchResult(
    val seriesTitle: String,
    val covers: List<CoverItem>,
    val paginationInfo: PaginationInfo
)

