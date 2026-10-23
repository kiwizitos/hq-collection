package com.kiwizitos.collection.presentation.viewmodel

import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.data.repository.ComicDataSource
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextPageParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

internal class FakeComicDataSource : ComicDataSource {
    val searchCalls = mutableListOf<String>()
    val nextSearchCalls = mutableListOf<LoadNextPageParams>()
    val coversCalls = mutableListOf<Pair<String, String>>()
    val nextCoversCalls = mutableListOf<LoadNextCoversPageParams>()
    val detailsCalls = mutableListOf<String>()

    var search: suspend (String) -> Result<SeriesSearchResult> = {
        error("Unexpected search request")
    }
    var nextSearch: suspend (LoadNextPageParams) -> Result<SeriesSearchResult> = {
        error("Unexpected search pagination")
    }
    var covers: suspend (String, String) -> Result<CoversSearchResult> = { _, _ ->
        error("Unexpected covers request")
    }
    var nextCovers: suspend (LoadNextCoversPageParams) -> Result<CoversSearchResult> = {
        error("Unexpected covers pagination")
    }
    var details: suspend (String) -> Result<ComicDetails> = {
        error("Unexpected details request")
    }

    override suspend fun searchSeries(query: String): Result<SeriesSearchResult> {
        searchCalls += query
        return search(query)
    }

    override suspend fun searchSeriesNextPage(
        query: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String,
        viewStateGenerator: String
    ): Result<SeriesSearchResult> {
        val params = LoadNextPageParams(query, viewState, eventValidation, eventTarget, viewStateGenerator)
        nextSearchCalls += params
        return nextSearch(params)
    }

    override suspend fun getSeriesCovers(
        seriesUrl: String,
        seriesTitle: String
    ): Result<CoversSearchResult> {
        coversCalls += seriesUrl to seriesTitle
        return covers(seriesUrl, seriesTitle)
    }

    override suspend fun getSeriesCoversNextPage(
        seriesUrl: String,
        seriesTitle: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String,
        viewStateGenerator: String
    ): Result<CoversSearchResult> {
        val params = LoadNextCoversPageParams(
            seriesUrl, seriesTitle, viewState, eventValidation, eventTarget, viewStateGenerator
        )
        nextCoversCalls += params
        return nextCovers(params)
    }

    override suspend fun getComicDetails(url: String): Result<ComicDetails> {
        detailsCalls += url
        return details(url)
    }
}
