package com.kiwizitos.collection.data.remote

import com.kiwizitos.collection.data.repository.GuiaQuadrinhosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GuiaQuadrinhosRepositoryTest {
    private class FakeService : GuiaQuadrinhosService {
        val requestTimes = mutableListOf<Long>()
        var postedGenerator: String? = null
        var postedTarget: String? = null
        var requestedUrl: String? = null
        var cancel = false

        override suspend fun getPageHtml(url: String): String {
            if (cancel) throw CancellationException("Cancelled request")
            requestedUrl = url
            requestTimes += System.nanoTime()
            return """<table id="tlbProfile"></table>"""
        }

        override suspend fun postPageNavigation(
            url: String,
            viewState: String,
            viewStateEncrypted: String,
            eventValidation: String,
            eventTarget: String,
            eventArgument: String,
            viewStateGenerator: String
        ): String {
            requestedUrl = url
            postedGenerator = viewStateGenerator
            postedTarget = eventTarget
            return """<div class="Lista_album_imagem_colecao"></div>"""
        }
    }

    @Test fun forwardsPageSpecificGeneratorAndNormalizesLegacyUrl() = runBlocking {
        val service = FakeService()
        val repository = GuiaQuadrinhosRepository(service)
        repository.getSeriesCoversNextPage(
            "http://guiadosquadrinhos.com/capas/titulo/ab01", "Título",
            "state", "validation", "next", "BDEF46B4"
        ).getOrThrow()
        assertEquals("BDEF46B4", service.postedGenerator)
        assertEquals("next", service.postedTarget)
        assertEquals("${GuiaUrls.BASE_URL}capas/titulo/ab01", service.requestedUrl)
    }

    @Test fun spacesConcurrentRequestsByAtLeastTwoSeconds() = runBlocking {
        val service = FakeService()
        val repository = GuiaQuadrinhosRepository(service)
        val first = async { repository.searchSeries("x-men").getOrThrow() }
        val second = async { repository.searchSeries("batman").getOrThrow() }
        first.await()
        second.await()
        assertEquals(2, service.requestTimes.size)
        assertTrue(service.requestTimes[1] - service.requestTimes[0] >= 2_000_000_000L)
    }

    @Test fun propagatesCancellationInsteadOfReturningFailure() = runBlocking {
        val service = FakeService().apply { cancel = true }
        try {
            GuiaQuadrinhosRepository(service).searchSeries("x-men")
            fail("Expected coroutine cancellation")
        } catch (expected: CancellationException) {
            assertEquals("Cancelled request", expected.message)
        }
    }
}
