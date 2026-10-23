package com.kiwizitos.collection.presentation.viewmodel

import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.remote.GuiaFailureMessages
import com.kiwizitos.collection.domain.usecase.GetComicDetailsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
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
class EditionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val source = FakeComicDataSource()

    @Test
    fun newEditionCancelsPreviousRequestWithoutPublishingObsoleteError() =
        runTest(mainDispatcherRule.dispatcher) {
            var cancelled = false
            source.details = { url ->
                if (url == "old") {
                    try {
                        awaitCancellation()
                    } catch (error: CancellationException) {
                        cancelled = true
                        Result.failure(IOException("obsolete", error))
                    }
                } else {
                    Result.success(ComicDetails(title = "New edition"))
                }
            }
            val vm = EditionViewModel(GetComicDetailsUseCase(source))
            val states = mutableListOf<UiState<ComicDetails>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.state.collect { states += it }
            }

            vm.load("old")
            runCurrent()
            assertEquals(UiState.Loading, vm.state.value)
            vm.load("new")
            advanceUntilIdle()

            assertTrue(cancelled)
            assertEquals(listOf("old", "new"), source.detailsCalls)
            assertEquals(ComicDetails("New edition"), (vm.state.value as UiState.Success).data)
            assertTrue(states.none { it is UiState.Error })
        }

    @Test
    fun loadingSameEditionDoesNotDuplicateRequest() =
        runTest(mainDispatcherRule.dispatcher) {
            val response = CompletableDeferred<Result<ComicDetails>>()
            source.details = { response.await() }
            val vm = EditionViewModel(GetComicDetailsUseCase(source))

            vm.load("edition")
            runCurrent()
            vm.load("edition")
            runCurrent()
            assertEquals(listOf("edition"), source.detailsCalls)

            response.complete(Result.success(ComicDetails("Edition")))
            advanceUntilIdle()
            assertEquals("Edition", (vm.state.value as UiState.Success).data.title)
        }

    @Test
    fun failedEditionUsesSharedMessageAndRetriesOnlyWhenRequested() =
        runTest(mainDispatcherRule.dispatcher) {
            val failure = IOException("offline")
            source.details = { Result.failure(failure) }
            val vm = EditionViewModel(GetComicDetailsUseCase(source))

            vm.load("edition")
            advanceUntilIdle()
            val state = vm.state.value as UiState.Error
            assertEquals(GuiaFailureMessages.message(failure), state.message)
            assertSame(failure, state.throwable)
            advanceUntilIdle()
            assertEquals(1, source.detailsCalls.size)

            source.details = { Result.success(ComicDetails("Edition")) }
            vm.load("edition")
            advanceUntilIdle()
            assertEquals(2, source.detailsCalls.size)
            assertEquals("Edition", (vm.state.value as UiState.Success).data.title)
        }
}
