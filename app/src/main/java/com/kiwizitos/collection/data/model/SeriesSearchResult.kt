package com.kiwizitos.collection.data.model

/**
 * Resultado de uma busca de séries, incluindo a lista de resultados e informações de paginação.
 *
 * @param series         Lista de séries retornadas na página atual.
 * @param paginationInfo Estado de paginação para navegar para próximas páginas.
 */
data class SeriesSearchResult(
    val series: List<SerieResult>,
    val paginationInfo: PaginationInfo
)

