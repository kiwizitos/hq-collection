package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.repository.ComicDataSource
import javax.inject.Inject

/**
 * Parâmetros para [LoadNextCoversPageUseCase].
 *
 * @param seriesUrl       Caminho relativo da série no Guia dos Quadrinhos.
 * @param seriesTitle     Título da série para exibição.
 * @param viewState       `__VIEWSTATE` extraído da página anterior.
 * @param eventValidation `__EVENTVALIDATION` extraído da página anterior.
 * @param eventTarget     `__EVENTTARGET` do botão "próxima página".
 * @param viewStateGenerator `__VIEWSTATEGENERATOR` extraído da página anterior.
 */
data class LoadNextCoversPageParams(
    val seriesUrl: String,
    val seriesTitle: String,
    val viewState: String,
    val eventValidation: String,
    val eventTarget: String,
    val viewStateGenerator: String
)

/**
 * Caso de uso que carrega a próxima página de capas de uma série.
 *
 * @param dataSource Fonte de dados de quadrinhos.
 */
class LoadNextCoversPageUseCase @Inject constructor(
    private val dataSource: ComicDataSource
) {
    /**
     * Carrega a próxima página de capas.
     *
     * @param params Parâmetros com URL, título e tokens de paginação ASP.NET.
     * @return [Result] com as novas capas e informações de paginação atualizadas.
     */
    suspend operator fun invoke(params: LoadNextCoversPageParams): Result<CoversSearchResult> {
        if (params.seriesUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("URL da série inválida para paginação de capas"))
        }
        if (listOf(params.viewState, params.eventValidation, params.eventTarget, params.viewStateGenerator)
                .any { it.isBlank() }
        ) {
            return Result.failure(IllegalArgumentException("Tokens de paginação ausentes. Reabra a série."))
        }
        return dataSource.getSeriesCoversNextPage(
            seriesUrl = params.seriesUrl,
            seriesTitle = params.seriesTitle,
            viewState = params.viewState,
            eventValidation = params.eventValidation,
            eventTarget = params.eventTarget,
            viewStateGenerator = params.viewStateGenerator
        )
    }
}
