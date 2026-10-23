package com.kiwizitos.collection.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class GuiaAccessInterceptorTest {
    private fun withServer(
        status: Int,
        challenge: Boolean,
        test: (String, MockWebServer) -> Unit
    ) {
        val server = MockWebServer()
        val response = MockResponse().setResponseCode(status).setBody("<html>test</html>")
        if (challenge) response.addHeader("Cf-Mitigated", "challenge")
        server.enqueue(response)
        server.start()
        try {
            test(server.url("/").toString(), server)
        } finally {
            server.shutdown()
        }
    }

    @Test fun classifiesChallengeAndDoesNotRetry() {
        withServer(403, true) { url, server ->
            val client = OkHttpClient.Builder().addInterceptor(GuiaAccessInterceptor()).build()
            try {
                client.newCall(Request.Builder().url(url).build()).execute().use {
                    fail("Expected challenge exception")
                }
            } catch (expected: GuiaAccessChallengeException) {
                assertTrue(expected.message.orEmpty().contains("verificação"))
            }
            assertEquals(1, server.requestCount)
        }
    }

    @Test fun preservesOrdinaryForbiddenResponse() {
        withServer(403, false) { url, _ ->
            val client = OkHttpClient.Builder().addInterceptor(GuiaAccessInterceptor()).build()
            client.newCall(Request.Builder().url(url).build()).execute().use {
                assertEquals(403, it.code)
                assertEquals("<html>test</html>", it.body?.string())
            }
        }
    }

    @Test fun preservesSuccessfulResponse() {
        withServer(200, false) { url, _ ->
            val client = OkHttpClient.Builder().addInterceptor(GuiaAccessInterceptor()).build()
            client.newCall(Request.Builder().url(url).build()).execute().use {
                assertEquals(200, it.code)
                assertEquals("<html>test</html>", it.body?.string())
            }
        }
    }
}
