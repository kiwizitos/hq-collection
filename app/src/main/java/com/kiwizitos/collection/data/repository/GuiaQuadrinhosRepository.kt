package com.kiwizitos.collection.data.repository

import android.util.Log
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.data.remote.GuiaQuadrinhosParser
import com.kiwizitos.collection.data.remote.GuiaQuadrinhosService
import com.kiwizitos.collection.data.remote.GuiaUrls
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val TAG = "GuiaRepository"
private const val DELAY_MS = 2000L

class GuiaQuadrinhosRepository @Inject constructor(
    private val service: GuiaQuadrinhosService
) : ComicDataSource {
    private val requestMutex = Mutex()
    private var lastRequestNanos: Long? = null

    private suspend fun <T> request(operation: String, block: suspend () -> T): Result<T> {
        return try {
            val result = requestMutex.withLock {
                lastRequestNanos?.let { previous ->
                    val elapsedMs = (System.nanoTime() - previous) / 1_000_000
                    if (elapsedMs < DELAY_MS) delay(DELAY_MS - elapsedMs)
                }
                lastRequestNanos = System.nanoTime()
                block()
            }
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "$operation: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchSeries(query: String): Result<SeriesSearchResult> =
        request("searchSeries") {
            GuiaQuadrinhosParser.parseSeriesTableWithPagination(
                service.getPageHtml(GuiaUrls.searchUrl(query))
            )
        }

    override suspend fun searchSeriesNextPage(
        query: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String,
        viewStateGenerator: String
    ): Result<SeriesSearchResult> = request("searchSeriesNextPage") {
        GuiaQuadrinhosParser.parseSeriesTableWithPagination(
            service.postPageNavigation(
                url = GuiaUrls.searchUrl(query),
                viewState = viewState,
                eventValidation = eventValidation,
                eventTarget = eventTarget,
                viewStateGenerator = viewStateGenerator
            )
        )
    }

    override suspend fun getSeriesCovers(
        seriesUrl: String,
        seriesTitle: String
    ): Result<CoversSearchResult> = request("getSeriesCovers") {
        GuiaQuadrinhosParser.parseCoverListWithPagination(
            service.getPageHtml(GuiaUrls.pageUrl(seriesUrl)), seriesTitle
        )
    }

    override suspend fun getSeriesCoversNextPage(
        seriesUrl: String,
        seriesTitle: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String,
        viewStateGenerator: String
    ): Result<CoversSearchResult> = request("getSeriesCoversNextPage") {
        GuiaQuadrinhosParser.parseCoverListWithPagination(
            service.postPageNavigation(
                url = GuiaUrls.pageUrl(seriesUrl),
                viewState = viewState,
                eventValidation = eventValidation,
                eventTarget = eventTarget,
                viewStateGenerator = viewStateGenerator
            ),
            seriesTitle
        )
    }

    override suspend fun getComicDetails(url: String): Result<ComicDetails> =
        request("getComicDetails") {
            val fullUrl = GuiaUrls.pageUrl(url)
            GuiaQuadrinhosParser.parseDetails(service.getPageHtml(fullUrl), fullUrl)
        }
}
