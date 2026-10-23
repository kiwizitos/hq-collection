package com.kiwizitos.collection.data.repository

import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.Ownership
import com.kiwizitos.collection.data.model.ReadStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseGalleryRepositoryTest {

    private val oldCover = "https://www.guiadosquadrinhos.com/old.jpg"
    private val newCover = "https://www.guiadosquadrinhos.com/edicao/ShowImage.aspx?id=1&w=400"
    private val editionUrl = "edicao/exemplo/ab01/1"
    private val seriesUrl = "capas/exemplo/ab01"

    private data class CapturedPatch(
        val path: String,
        val query: Map<String, List<String>>,
        val body: JsonObject,
        val authorization: String?
    )

    private class Server(
        var editionsJson: String = "[]",
        var seriesJson: String = "[]",
        var patchStatus: HttpStatusCode = HttpStatusCode.OK,
        var patchJson: String = "[]"
    ) {
        val requests = mutableListOf<HttpRequestData>()
        val patches = mutableListOf<CapturedPatch>()
    }

    @Test
    fun editionRefreshPatchesOnlyCoverWithIdentityFiltersAndPreservesCache() = runBlocking {
        val server = Server(
            editionsJson = "[${editionJson(oldCover)}]",
            patchJson = "[${editionJson(newCover)}]"
        )
        val repo = repository(server).first
        repo.loadGallery("user-1").getOrThrow()
        val before = repo.editionsFull.value.getValue(editionUrl)
        val statusBefore = repo.editionsCache.value

        val result = repo.refreshItemCover(editionUrl, newCover)

        assertTrue(result.isSuccess)
        val patch = server.patches.single()
        assertTrue(patch.path.endsWith("/rest/v1/user_editions"))
        assertEquals(setOf("cover_url"), patch.body.keys)
        assertEquals(JsonPrimitive(newCover), patch.body["cover_url"])
        assertEquals(listOf("eq.user-1"), patch.query["user_id"])
        assertEquals(listOf("eq.$editionUrl"), patch.query["guia_url"])
        assertEquals(listOf("eq.edition-1"), patch.query["id"])
        assertEquals(listOf("eq.$oldCover"), patch.query["cover_url"])
        assertEquals("Bearer fake-access-token", patch.authorization)
        assertEquals(before.copy(coverUrl = newCover), repo.editionsFull.value[editionUrl])
        assertEquals(statusBefore, repo.editionsCache.value)
        assertEquals(ItemStatus(Ownership.TENHO, ReadStatus.LIDO), repo.editionsCache.value[editionUrl])
        assertTrue(server.requests.none { it.method == HttpMethod.Post })
    }

    @Test
    fun seriesRefreshPatchesOnlyCoverAndPreservesMetadata() = runBlocking {
        val server = Server(
            seriesJson = "[${seriesJson(null)}]",
            patchJson = "[${seriesJson(newCover)}]"
        )
        val repo = repository(server).first
        repo.loadGallery("user-1").getOrThrow()
        val before = repo.seriesCache.value.getValue(seriesUrl)

        assertTrue(repo.refreshSeriesCover(seriesUrl, newCover).isSuccess)

        val patch = server.patches.single()
        assertTrue(patch.path.endsWith("/rest/v1/user_series"))
        assertEquals(setOf("cover_url"), patch.body.keys)
        assertEquals(JsonPrimitive(newCover), patch.body["cover_url"])
        assertEquals(listOf("eq.user-1"), patch.query["user_id"])
        assertEquals(listOf("eq.$seriesUrl"), patch.query["series_url"])
        assertEquals(listOf("eq.series-1"), patch.query["id"])
        assertEquals(listOf("is.null"), patch.query["cover_url"])
        assertEquals(before.copy(coverUrl = newCover), repo.seriesCache.value[seriesUrl])
        assertTrue(server.requests.none { it.method == HttpMethod.Post })
    }

    @Test
    fun skippedRefreshesDoNotSendRequestsOrCreateRows() = runBlocking {
        val server = Server(
            editionsJson = "[${editionJson(oldCover)}]",
            seriesJson = "[${seriesJson(oldCover)}]"
        )
        val repo = repository(server).first
        repo.loadGallery("user-1").getOrThrow()
        val editionsBefore = repo.editionsFull.value
        val statusBefore = repo.editionsCache.value
        val seriesBefore = repo.seriesCache.value
        server.requests.clear()

        listOf(
            repo.refreshItemCover("edicao/nao/salva/9", newCover),
            repo.refreshItemCover(editionUrl, null),
            repo.refreshItemCover(editionUrl, ""),
            repo.refreshItemCover(editionUrl, "https://example.com/capa.jpg"),
            repo.refreshItemCover(editionUrl, oldCover),
            repo.refreshSeriesCover("capas/nao/salva", newCover),
            repo.refreshSeriesCover(seriesUrl, " ")
        ).forEach { assertTrue(it.isSuccess) }

        assertTrue(server.requests.isEmpty())
        assertEquals(editionsBefore, repo.editionsFull.value)
        assertEquals(statusBefore, repo.editionsCache.value)
        assertEquals(seriesBefore, repo.seriesCache.value)
    }

    @Test
    fun remoteFailureAndNoUpdatedRowPreserveCache() = runBlocking {
        val server = Server(
            editionsJson = "[${editionJson(oldCover)}]",
            patchStatus = HttpStatusCode.InternalServerError,
            patchJson = """{"message":"falha simulada"}"""
        )
        val repo = repository(server).first
        repo.loadGallery("user-1").getOrThrow()
        val before = repo.editionsFull.value
        val statusBefore = repo.editionsCache.value

        assertTrue(repo.refreshItemCover(editionUrl, newCover).isFailure)
        assertEquals(before, repo.editionsFull.value)

        server.patchStatus = HttpStatusCode.OK
        server.patchJson = "[]"
        assertTrue(repo.refreshItemCover(editionUrl, newCover).isFailure)
        assertEquals(before, repo.editionsFull.value)
        assertEquals(statusBefore, repo.editionsCache.value)
        assertEquals(2, server.patches.size)
        assertTrue(server.requests.none { it.method == HttpMethod.Post })
    }

    @Test
    fun cacheOwnedByAnotherUserFailsWithoutRequest() = runBlocking {
        val server = Server(
            editionsJson = "[${editionJson(oldCover)}]",
            seriesJson = "[${seriesJson(oldCover)}]"
        )
        val (repo, client) = repository(server)
        repo.loadGallery("user-1").getOrThrow()
        importUser(client, "user-2")
        server.requests.clear()

        assertTrue(repo.refreshItemCover(editionUrl, newCover).isFailure)
        assertTrue(repo.refreshSeriesCover(seriesUrl, newCover).isFailure)
        assertTrue(server.requests.isEmpty())
        assertEquals(oldCover, repo.editionsFull.value.getValue(editionUrl).coverUrl)
        assertEquals(oldCover, repo.seriesCache.value.getValue(seriesUrl).coverUrl)
    }

    private suspend fun repository(server: Server): Pair<SupabaseGalleryRepository, SupabaseClient> {
        val client = createSupabaseClient(
            supabaseUrl = "https://unit-test.supabase.co",
            supabaseKey = "unit-test-anon-key"
        ) {
            httpEngine = MockEngine { request ->
                server.requests += request
                val json = headersOf(HttpHeaders.ContentType, "application/json")
                when (request.method) {
                    HttpMethod.Get -> respond(
                        if (request.url.encodedPath.endsWith("/user_series")) server.seriesJson
                        else server.editionsJson,
                        HttpStatusCode.OK,
                        json
                    )
                    HttpMethod.Patch -> {
                        server.patches += CapturedPatch(
                            path = request.url.encodedPath,
                            query = request.url.parameters.names()
                                .associateWith { request.url.parameters.getAll(it).orEmpty() },
                            body = Json.parseToJsonElement(
                                request.body.toByteArray().decodeToString()
                            ).jsonObject,
                            authorization = request.headers[HttpHeaders.Authorization]
                        )
                        respond(server.patchJson, server.patchStatus, json)
                    }
                    else -> respond("[]", HttpStatusCode.MethodNotAllowed, json)
                }
            }
            install(Auth) { minimalSettings() }
            install(Postgrest)
        }
        importUser(client, "user-1")
        return SupabaseGalleryRepository(client) to client
    }

    private suspend fun importUser(client: SupabaseClient, userId: String) {
        client.auth.importSession(
            UserSession(
                accessToken = "fake-access-token",
                refreshToken = "fake-refresh-token",
                expiresIn = 3600,
                tokenType = "bearer",
                user = UserInfo(aud = "authenticated", id = userId)
            ),
            autoRefresh = false
        )
    }

    private fun editionJson(cover: String?) = """
        {"id":"edition-1","user_id":"user-1","guia_url":"$editionUrl","title":"Edição salva",
         "cover_url":${jsonString(cover)},"series_url":"$seriesUrl",
         "series_title":"Série salva","ownership":"TENHO","read_status":"LIDO","is_standalone":false}
    """.trimIndent()

    private fun seriesJson(cover: String?) = """
        {"id":"series-1","user_id":"user-1","series_url":"$seriesUrl","series_title":"Série salva",
         "cover_url":${jsonString(cover)},"publisher":"Editora","issue_count":"12"}
    """.trimIndent()

    private fun jsonString(value: String?) = value?.let { JsonPrimitive(it).toString() } ?: "null"
}