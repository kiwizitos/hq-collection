package com.kiwizitos.collection.data.repository

import android.util.Log
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.data.remote.GuiaQuadrinhosParser
import com.kiwizitos.collection.data.remote.GuiaQuadrinhosService
import kotlinx.coroutines.delay

private const val TAG = "GuiaRepository"
private const val BASE_URL = "http://www.guiadosquadrinhos.com"
private const val DELAY_MS = 2000L

/**
 * Implementação de [ComicDataSource] que busca dados do Guia dos Quadrinhos.
 *
 * Usa o padrão padrão do site (30 itens/página para séries, 30 para capas).
 * A paginação é feita via POST com ViewState ASP.NET.
 */
class GuiaQuadrinhosRepository(
    private val service: GuiaQuadrinhosService
) : ComicDataSource {

    companion object {
        private var lastRequestTime = 0L
    }

    private suspend fun applyRateLimit() {
        val elapsed = System.currentTimeMillis() - lastRequestTime
        if (elapsed < DELAY_MS) delay(DELAY_MS - elapsed)
        lastRequestTime = System.currentTimeMillis()
    }

    // ── Busca de Séries ───────────────────────────────────────────────────────

    override suspend fun searchSeries(query: String): Result<SeriesSearchResult> {
        return try {
            val url = "$BASE_URL/titulos/${query.trim().replace(" ", "%20")}"
            applyRateLimit()
            Log.d(TAG, "GET $url")
            val html = service.getPageHtml(url)
            Result.success(GuiaQuadrinhosParser.parseSeriesTableWithPagination(html))
        } catch (e: Exception) {
            Log.e(TAG, "searchSeries error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchSeriesNextPage(
        query: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String
    ): Result<SeriesSearchResult> {
        return try {
            val url = "$BASE_URL/titulos/${query.trim().replace(" ", "%20")}"
            applyRateLimit()
            Log.d(TAG, "POST próxima página → $url (target=$eventTarget)")
            val html = service.postPageNavigation(
                url             = url,
                viewState       = viewState,
                eventValidation = eventValidation,
                eventTarget     = eventTarget
            )
            Result.success(GuiaQuadrinhosParser.parseSeriesTableWithPagination(html))
        } catch (e: Exception) {
            Log.e(TAG, "searchSeriesNextPage error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── Capas da Série ────────────────────────────────────────────────────────

    override suspend fun getSeriesCovers(
        seriesUrl: String,
        seriesTitle: String
    ): Result<CoversSearchResult> {
        return try {
            val url = if (seriesUrl.startsWith("http")) seriesUrl else "$BASE_URL/$seriesUrl"
            applyRateLimit()
            Log.d(TAG, "GET capas → $url")
            val html = service.getPageHtml(url)
            Result.success(GuiaQuadrinhosParser.parseCoverListWithPagination(html, seriesTitle))
        } catch (e: Exception) {
            Log.e(TAG, "getSeriesCovers error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getSeriesCoversNextPage(
        seriesUrl: String,
        seriesTitle: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String
    ): Result<CoversSearchResult> {
        return try {
            val url = if (seriesUrl.startsWith("http")) seriesUrl else "$BASE_URL/$seriesUrl"
            applyRateLimit()
            Log.d(TAG, "POST capas próxima página → $url")
            val html = service.postPageNavigation(
                url             = url,
                viewState       = viewState,
                eventValidation = eventValidation,
                eventTarget     = eventTarget
            )
            Result.success(GuiaQuadrinhosParser.parseCoverListWithPagination(html, seriesTitle))
        } catch (e: Exception) {
            Log.e(TAG, "getSeriesCoversNextPage error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── Detalhes ──────────────────────────────────────────────────────────────

    override suspend fun getComicDetails(url: String): Result<ComicDetails> {
        return try {
            val fullUrl = if (url.startsWith("http")) url else "$BASE_URL/$url"
            applyRateLimit()
            Log.d(TAG, "GET detalhes → $fullUrl")
            val html = service.getPageHtml(fullUrl)
            val details = GuiaQuadrinhosParser.parseDetails(html, fullUrl)
                ?: return Result.failure(IllegalStateException("Não foi possível extrair detalhes de $fullUrl"))
            Result.success(details)
        } catch (e: Exception) {
            Log.e(TAG, "getComicDetails error: ${e.message}", e)
            Result.failure(e)
        }
    }
}

