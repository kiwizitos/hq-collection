package com.kiwizitos.collection.data.repository

import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.SeriesSearchResult

/**
 * Contrato da fonte de dados de quadrinhos.
 *
 * Segue o princípio de inversão de dependência — a camada de domínio depende
 * desta interface, não da implementação concreta.
 */
interface ComicDataSource {

    /** Busca séries pelo termo informado (primeira página). */
    suspend fun searchSeries(query: String): Result<SeriesSearchResult>

    /** Carrega a próxima página de resultados de uma busca. */
    suspend fun searchSeriesNextPage(
        query: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String
    ): Result<SeriesSearchResult>

    /** Carrega a lista de capas de uma série (primeira página). */
    suspend fun getSeriesCovers(
        seriesUrl: String,
        seriesTitle: String
    ): Result<CoversSearchResult>

    /** Carrega a próxima página da lista de capas de uma série. */
    suspend fun getSeriesCoversNextPage(
        seriesUrl: String,
        seriesTitle: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String
    ): Result<CoversSearchResult>

    /** Carrega os detalhes de uma edição específica. */
    suspend fun getComicDetails(url: String): Result<ComicDetails>
}

