package com.kiwizitos.collection.data.model

/**
 * Informações de estado de paginação ASP.NET de uma listagem.
 *
 * @param currentPage     Número da página atual (começa em 1).
 * @param totalResults    Total de resultados disponíveis.
 * @param hasNextPage     Indica se há uma próxima página.
 * @param viewState       Valor do campo oculto `__VIEWSTATE` usado no POST de navegação.
 * @param eventValidation Valor do campo oculto `__EVENTVALIDATION`.
 * @param eventTarget     Alvo do evento `__doPostBack` extraído do botão "próxima página".
 */
data class PaginationInfo(
    val currentPage: Int = 1,
    val totalResults: Int = 0,
    val hasNextPage: Boolean = false,
    val viewState: String? = null,
    val eventValidation: String? = null,
    val eventTarget: String? = null
)

