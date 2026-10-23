package com.kiwizitos.collection.data.remote

import org.junit.Assert.*
import org.junit.Test

class GuiaUrlsTest {
    @Test fun normalizesLegacyAndRelativeUrls() {
        val expected = "${GuiaUrls.BASE_URL}edicao/exemplo/ab01/123"
        listOf(
            "../../../edicao/exemplo/ab01/123",
            "/edicao/exemplo/ab01/123",
            "http://guiadosquadrinhos.com/edicao/exemplo/ab01/123",
            "//www.guiadosquadrinhos.com/edicao/exemplo/ab01/123",
            "HTTP://WWW.GUIADOSQUADRINHOS.COM/edicao/exemplo/ab01/123"
        ).forEach { assertEquals(expected, GuiaUrls.pageUrl(it)) }
        assertEquals("edicao/exemplo/ab01/123", GuiaUrls.relativePath(expected))
    }

    @Test fun preservesImagePathAndParameters() {
        assertEquals(
            "${GuiaUrls.BASE_URL}edicao/ShowImage.aspx?id=123&path=abril/f/capa.jpg&w=400&h=622",
            GuiaUrls.imageUrl("http://www.guiadosquadrinhos.com/edicao/ShowImage.aspx" +
                "?id=123&path=abril/f/capa.jpg&w=400&h=622")
        )
        assertEquals(
            "https://www.guiadosquadrinhos.com//capasthumbs/capa.jpg",
            GuiaUrls.imageUrl("http://www.guiadosquadrinhos.com//capasthumbs/capa.jpg")
        )
        assertEquals(
            "${GuiaUrls.BASE_URL}edicao/ShowImage.aspx?id=1",
            GuiaUrls.imageUrl("ShowImage.aspx?id=1", "${GuiaUrls.BASE_URL}edicao/titulo")
        )
    }

    @Test fun rejectsInvalidImageUrls() {
        listOf("", "javascript:alert(1)", "https://guiadosquadrinhos.com.evil.test/a.jpg",
            "https://example.com/a.jpg", "https://user@www.guiadosquadrinhos.com/a.jpg",
            "https://www.guiadosquadrinhos.com:8080/a.jpg"
        ).forEach { assertNull(GuiaUrls.imageUrl(it)) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsForeignPages() {
        GuiaUrls.pageUrl("https://guiadosquadrinhos.com.evil.test/edicao/a")
    }

    @Test fun encodesSearchAsOnePathSegment() {
        assertEquals(
            "${GuiaUrls.BASE_URL}titulos/Homem%20Aranha%2FMarvel%3F%23%25",
            GuiaUrls.searchUrl(" Homem Aranha/Marvel?#% ")
        )
        assertEquals("${GuiaUrls.BASE_URL}titulos/a%C3%A7%C3%A3o", GuiaUrls.searchUrl("ação"))
    }
}
