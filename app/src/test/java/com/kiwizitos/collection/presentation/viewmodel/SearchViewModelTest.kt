package com.kiwizitos.collection.presentation.viewmodel

import com.kiwizitos.collection.data.model.CoverItem
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.PaginationInfo
import com.kiwizitos.collection.data.model.SerieResult
import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.domain.usecase.GetSeriesCoversUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextPageUseCase
import com.kiwizitos.collection.domain.usecase.SearchComicsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val source = FakeComicDataSource()

    private fun viewModel() = SearchViewModel(
        SearchComicsUseCase(source),
        LoadNextPageUseCase(source),
        GetSeriesCoversUseCase(source),
        LoadNextCoversPageUseCase(source)
    )

    @Test
    fun searchPaginationUsesLatestGeneratorAndAccumulatesDistinctResults() =
        runTest(mainDispatcherRule.dispatcher) {
            source.search = { Result.success(searchPage("first", pagination(1))) }
            source.nextSearch = {
                val page = source.nextSearchCalls.size + 1
                Result.success(SeriesSearchResult(
                    listOf(series("first"), series("page-$page")),
                    pagination(page)
                ))
            }
            val vm = viewModel()
            vm.searchSeries("batman")
            advanceUntilIdle()

            repeat(2) {
                vm.loadNextSearchPage()
                advanceUntilIdle()
            }

            assertEquals(
                listOf(searchParams(1), searchParams(2)),
                source.nextSearchCalls
            )
            val data = (vm.searchState.value as UiState.Success).data
            assertEquals(listOf("first", "page-2", "page-3"), data.series.map { it.title })
            assertEquals(pagination(3), data.paginationInfo)
            assertNull(data.paginationError)
        }

    @Test
    fun coversPaginationUsesLatestGeneratorAndPreservesMetadata() =
        runTest(mainDispatcherRule.dispatcher) {
            source.covers = { _, _ -> Result.success(coversPage("first", pagination(1))) }
            source.nextCovers = {
                val page = source.nextCoversCalls.size + 1
                Result.success(CoversSearchResult(
                    "Other title",
                    listOf(cover("first"), cover("page-$page")),
                    pagination(page)
                ))
            }
            val vm = viewModel()
            vm.getSeriesCovers("capas/batman/1", "Batman")
            advanceUntilIdle()

            repeat(2) {
                vm.loadNextCoversPage()
                advanceUntilIdle()
            }

            assertEquals(listOf(coversParams(1), coversParams(2)), source.nextCoversCalls)
            val data = (vm.coversState.value as UiState.Success).data
            assertEquals(listOf("first", "page-2", "page-3"), data.covers.map { it.title })
            assertEquals("Batman", data.seriesTitle)
            assertEquals(pagination(3), data.paginationInfo)
            assertNull(data.paginationError)
        }

    @Test
    fun failedSearchPaginationRetainsDataAndOnlyRetriesOnExplicitCalls() =
        runTest(mainDispatcherRule.dispatcher) {
            source.search = { Result.success(searchPage("first", pagination(1))) }
            source.nextSearch = { Result.failure(IOException("offline")) }
            val vm = viewModel()
            vm.searchSeries("batman")
            advanceUntilIdle()
            val original = (vm.searchState.value as UiState.Success).data

            repeat(3) { attempt ->
                vm.loadNextSearchPage()
                val loading = vm.searchState.value as UiState.LoadingMore
                assertEquals(original.series, loading.currentData.series)
                assertNull(loading.currentData.paginationError)
                advanceUntilIdle()
                val data = (vm.searchState.value as UiState.Success).data
                assertEquals(original.series, data.series)
                assertEquals(original.paginationInfo, data.paginationInfo)
                assertFalse(data.paginationError.isNullOrBlank())
                assertEquals(attempt + 1, source.nextSearchCalls.size)
                advanceUntilIdle()
                assertEquals(attempt + 1, source.nextSearchCalls.size)
            }
        }

    @Test
    fun failedCoversPaginationRetainsDataAndOnlyRetriesOnExplicitCalls() =
        runTest(mainDispatcherRule.dispatcher) {
            source.covers = { _, _ -> Result.success(coversPage("first", pagination(1))) }
            source.nextCovers = { Result.failure(IOException("offline")) }
            val vm = viewModel()
            vm.getSeriesCovers("capas/batman/1", "Batman")
            advanceUntilIdle()
            val original = (vm.coversState.value as UiState.Success).data

            repeat(3) { attempt ->
                vm.loadNextCoversPage()
                val loading = vm.coversState.value as UiState.LoadingMore
                assertEquals(original.covers, loading.currentData.covers)
                assertNull(loading.currentData.paginationError)
                advanceUntilIdle()
                val data = (vm.coversState.value as UiState.Success).data
                assertEquals(original.covers, data.covers)
                assertEquals(original.paginationInfo, data.paginationInfo)
                assertFalse(data.paginationError.isNullOrBlank())
                assertEquals(attempt + 1, source.nextCoversCalls.size)
                advanceUntilIdle()
                assertEquals(attempt + 1, source.nextCoversCalls.size)
            }
        }

    @Test
    fun missingGeneratorReportsPaginationErrorWithoutDiscardingDataOrPosting() =
        runTest(mainDispatcherRule.dispatcher) {
            val missingGenerator = pagination(1).copy(viewStateGenerator = null)
            source.search = { Result.success(searchPage("first", missingGenerator)) }
            source.covers = { _, _ -> Result.success(coversPage("first", missingGenerator)) }
            val vm = viewModel()
            vm.searchSeries("batman")
            vm.getSeriesCovers("capas/batman/1", "Batman")
            advanceUntilIdle()

            vm.loadNextSearchPage()
            vm.loadNextCoversPage()
            advanceUntilIdle()

            val search = (vm.searchState.value as UiState.Success).data
            val covers = (vm.coversState.value as UiState.Success).data
            assertEquals(listOf(series("first")), search.series)
            assertEquals(listOf(cover("first")), covers.covers)
            assertTrue(search.paginationError.orEmpty().contains("Tokens"))
            assertTrue(covers.paginationError.orEmpty().contains("Tokens"))
            assertTrue(source.nextSearchCalls.isEmpty())
            assertTrue(source.nextCoversCalls.isEmpty())
        }

    @Test
    fun newSearchCancelsPreviousRequestWithoutPublishingObsoleteError() =
        runTest(mainDispatcherRule.dispatcher) {
            var cancelled = false
            source.search = { query ->
                if (query == "old") {
                    try {
                        awaitCancellation()
                    } catch (error: CancellationException) {
                        cancelled = true
                        Result.failure(IOException("obsolete", error))
                    }
                } else {
                    Result.success(searchPage("new"))
                }
            }
            val vm = viewModel()
            val states = mutableListOf<UiState<PaginatedSearchResult>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.searchState.collect { states += it }
            }
            vm.searchSeries("old")
            runCurrent()
            vm.searchSeries("new")
            advanceUntilIdle()

            assertTrue(cancelled)
            assertEquals(listOf("old", "new"), source.searchCalls)
            assertEquals("new", (vm.searchState.value as UiState.Success).data.series.single().title)
            assertTrue(states.none { it is UiState.Error })
        }

    @Test
    fun newSearchCancelsPendingPaginationWithoutRestoringOldData() =
        runTest(mainDispatcherRule.dispatcher) {
            var cancelled = false
            source.search = { Result.success(searchPage(it, pagination(1))) }
            source.nextSearch = {
                try {
                    awaitCancellation()
                } catch (error: CancellationException) {
                    cancelled = true
                    Result.failure(error)
                }
            }
            val vm = viewModel()
            vm.searchSeries("old")
            advanceUntilIdle()
            vm.loadNextSearchPage()
            runCurrent()
            vm.searchSeries("new")
            advanceUntilIdle()

            assertTrue(cancelled)
            val data = (vm.searchState.value as UiState.Success).data
            assertEquals("new", data.series.single().title)
            assertNull(data.paginationError)
            assertEquals(1, source.nextSearchCalls.size)
        }

    @Test
    fun newSeriesCancelsPreviousCoversRequestWithoutPublishingObsoleteError() =
        runTest(mainDispatcherRule.dispatcher) {
            var cancelled = false
            source.covers = { url, _ ->
                if (url == "old") {
                    try {
                        awaitCancellation()
                    } catch (error: CancellationException) {
                        cancelled = true
                        Result.failure(IOException("obsolete", error))
                    }
                } else {
                    Result.success(coversPage("new"))
                }
            }
            val vm = viewModel()
            val states = mutableListOf<UiState<PaginatedCoversResult>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.coversState.collect { states += it }
            }
            vm.getSeriesCovers("old", "Old")
            runCurrent()
            vm.getSeriesCovers("new", "New")
            advanceUntilIdle()

            assertTrue(cancelled)
            assertEquals("new", vm.currentSeriesUrl)
            assertEquals("new", (vm.coversState.value as UiState.Success).data.covers.single().title)
            assertTrue(states.none { it is UiState.Error })
        }

    @Test
    fun resetCoversCancelsPendingPaginationAndKeepsIdleState() =
        runTest(mainDispatcherRule.dispatcher) {
            var cancelled = false
            source.covers = { _, _ -> Result.success(coversPage("first", pagination(1))) }
            source.nextCovers = {
                try {
                    awaitCancellation()
                } catch (error: CancellationException) {
                    cancelled = true
                    Result.failure(error)
                }
            }
            val vm = viewModel()
            vm.getSeriesCovers("capas/batman/1", "Batman")
            advanceUntilIdle()
            vm.loadNextCoversPage()
            runCurrent()
            vm.resetCoversState()
            advanceUntilIdle()

            assertTrue(cancelled)
            assertEquals(UiState.Idle, vm.coversState.value)
            assertEquals("", vm.currentSeriesUrl)
        }

    private fun pagination(page: Int) = PaginationInfo(
        currentPage = page,
        totalResults = 10,
        hasNextPage = true,
        viewState = "state-$page",
        eventValidation = "validation-$page",
        eventTarget = "next-$page",
        viewStateGenerator = "generator-$page"
    )

    private fun searchParams(page: Int) = LoadNextPageParams(
        "batman", "state-$page", "validation-$page", "next-$page", "generator-$page"
    )

    private fun coversParams(page: Int) = LoadNextCoversPageParams(
        "capas/batman/1", "Batman", "state-$page", "validation-$page", "next-$page", "generator-$page"
    )

    private fun series(title: String) = SerieResult(title, "serie/$title", "", "", "", "")

    private fun cover(title: String) = CoverItem(title, "edicao/$title", "", null)

    private fun searchPage(title: String, pagination: PaginationInfo = PaginationInfo()) =
        SeriesSearchResult(listOf(series(title)), pagination)

    private fun coversPage(title: String, pagination: PaginationInfo = PaginationInfo()) =
        CoversSearchResult("Batman", listOf(cover(title)), pagination)
}
