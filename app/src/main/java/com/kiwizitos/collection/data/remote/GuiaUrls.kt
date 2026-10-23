package com.kiwizitos.collection.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object GuiaUrls {
    const val BASE_URL = "https://www.guiadosquadrinhos.com/"

    private fun isGuiaHost(host: String) =
        host == "www.guiadosquadrinhos.com" || host == "guiadosquadrinhos.com"

    fun pageUrl(value: String): String {
        require(value.isNotBlank()) { "URL do Guia vazia" }
        val url = BASE_URL.toHttpUrl().resolve(value.trim())
        require(url != null && isGuiaHost(url.host) && url.username.isEmpty() &&
            url.password.isEmpty() && url.port in listOf(80, 443)) {
            "URL fora do Guia dos Quadrinhos"
        }
        return url.newBuilder().scheme("https").host("www.guiadosquadrinhos.com")
            .port(443).fragment(null).build().toString()
    }

    fun relativePath(value: String): String {
        val url = pageUrl(value).toHttpUrl()
        return url.encodedPath.trimStart('/') +
            (url.encodedQuery?.let { "?$it" } ?: "")
    }

    fun searchUrl(query: String): String {
        require(query.isNotBlank()) { "O termo de busca não pode estar vazio" }
        return BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment("titulos").addPathSegment(query.trim()).build().toString()
    }

    fun imageUrl(value: String, pageUrl: String = BASE_URL): String? {
        if (value.isBlank()) return null
        val base = pageUrl.toHttpUrlOrNull() ?: return null
        val url = base.resolve(value.trim()) ?: return null
        if (!isGuiaHost(url.host) || url.username.isNotEmpty() || url.password.isNotEmpty() ||
            url.port !in listOf(80, 443)) return null
        return url.newBuilder().scheme("https").host("www.guiadosquadrinhos.com")
            .port(443).fragment(null).build().toString()
    }
}
