package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.repository.ComicDataSource

/**
 * Parâmetros para [GetSeriesCoversUseCase].
 *
 * @param seriesUrl   Caminho relativo da série no Guia dos Quadrinhos.
 * @param seriesTitle Título da série para exibição no cabeçalho.
 */
data class GetSeriesCoversParams(
    val seriesUrl: String,
    val seriesTitle: String
)

/**
 * Caso de uso que carrega a primeira página de capas de uma série.
 *
 * @param dataSource Fonte de dados de quadrinhos.
 */
class GetSeriesCoversUseCase(
    private val dataSource: ComicDataSource
) {
    /**
     * Carrega a lista de capas (primeira página).
     *
     * @param params Parâmetros com URL e título da série.
     * @return [Result] com as capas e informações de paginação.
     */
    suspend operator fun invoke(params: GetSeriesCoversParams): Result<CoversSearchResult> {
        if (params.seriesUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("URL da série inválida"))
        }
        return dataSource.getSeriesCovers(
            seriesUrl   = params.seriesUrl,
            seriesTitle = params.seriesTitle
        )
    }
}

