package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.data.repository.ComicDataSource
import javax.inject.Inject

/**
 * Parâmetros para [LoadNextPageUseCase].
 *
 * @param query           Termo de busca original.
 * @param viewState       `__VIEWSTATE` extraído da página anterior.
 * @param eventValidation `__EVENTVALIDATION` extraído da página anterior.
 * @param eventTarget     `__EVENTTARGET` do botão "próxima página".
 * @param viewStateGenerator `__VIEWSTATEGENERATOR` extraído da página anterior.
 */
data class LoadNextPageParams(
    val query: String,
    val viewState: String,
    val eventValidation: String,
    val eventTarget: String,
    val viewStateGenerator: String
)

/**
 * Caso de uso que carrega a próxima página de resultados de uma busca de séries.
 *
 * @param dataSource Fonte de dados de quadrinhos.
 */
class LoadNextPageUseCase @Inject constructor(
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
        if (listOf(params.viewState, params.eventValidation, params.eventTarget, params.viewStateGenerator)
                .any { it.isBlank() }
        ) {
            return Result.failure(IllegalArgumentException("Tokens de paginação ausentes. Faça uma nova busca."))
        }
        return dataSource.searchSeriesNextPage(
            query = params.query,
            viewState = params.viewState,
            eventValidation = params.eventValidation,
            eventTarget = params.eventTarget,
            viewStateGenerator = params.viewStateGenerator
        )
    }
}
