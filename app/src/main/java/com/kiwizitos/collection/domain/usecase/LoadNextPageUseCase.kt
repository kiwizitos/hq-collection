package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.data.repository.ComicDataSource

/**
 * Parâmetros para [LoadNextPageUseCase].
 *
 * @param query           Termo de busca original.
 * @param viewState       `__VIEWSTATE` extraído da página anterior.
 * @param eventValidation `__EVENTVALIDATION` extraído da página anterior.
 * @param eventTarget     `__EVENTTARGET` do botão "próxima página".
 */
data class LoadNextPageParams(
    val query: String,
    val viewState: String,
    val eventValidation: String,
    val eventTarget: String
)

/**
 * Caso de uso que carrega a próxima página de resultados de uma busca de séries.
 *
 * @param dataSource Fonte de dados de quadrinhos.
 */
class LoadNextPageUseCase(
    private val dataSource: ComicDataSource
) {
    /**
     * Carrega a próxima página de resultados.
     *
     * @param params Parâmetros com query e tokens de paginação ASP.NET.
     * @return [Result] com os novos itens e informações de paginação atualizadas.
     */
    suspend operator fun invoke(params: LoadNextPageParams): Result<SeriesSearchResult> {
        if (params.query.isBlank()) {
            return Result.failure(IllegalArgumentException("Query inválida para paginação"))
        }
        return dataSource.searchSeriesNextPage(
            query           = params.query,
            viewState       = params.viewState,
            eventValidation = params.eventValidation,
            eventTarget     = params.eventTarget
        )
    }
}

