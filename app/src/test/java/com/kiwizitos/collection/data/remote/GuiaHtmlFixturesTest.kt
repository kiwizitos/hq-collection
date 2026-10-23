package com.kiwizitos.collection.data.remote

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class GuiaHtmlFixturesTest {
    private fun fixture(name: String) = File(
        checkNotNull(System.getProperty("collection.fixtureDir")), name
    ).readText()

    @Test fun parsesExistingSearchSnapshot() {
        val result = GuiaQuadrinhosParser.parseSeriesTableWithPagination(fixture("guia.txt"))
        assertEquals(30, result.series.size)
        assertTrue(result.series.all { it.relativeLink.startsWith("capas/") })
        assertEquals("EEB05935", result.paginationInfo.viewStateGenerator)
        assertTrue(result.paginationInfo.hasNextPage)
    }

    @Test fun parsesExistingCoverSnapshot() {
        val result = GuiaQuadrinhosParser.parseCoverListWithPagination(fixture("capas.txt"), "X-Men")
        assertEquals(30, result.covers.size)
        assertEquals("edicao/fabulosos-x-men-os-n-1/fxm0301/5968", result.covers.first().relativeLink)
        assertTrue(result.covers.all { it.coverUrl.startsWith(GuiaUrls.BASE_URL) })
        assertEquals("BDEF46B4", result.paginationInfo.viewStateGenerator)
        assertTrue(result.paginationInfo.hasNextPage)
        assertFalse(result.isStandalone)
    }

    @Test fun parsesExistingDetailsSnapshot() {
        val result = GuiaQuadrinhosParser.parseDetails(
            fixture("edicao.txt"), "${GuiaUrls.BASE_URL}edicao/fabulosos-x-men-os-n-1/fxm0301/5968"
        )
        assertTrue(result.title.contains("Fabulosos X-Men"))
        assertEquals("capas/fabulosos-x-men-os/fxm0301", result.seriesUrl)
        assertEquals("https://www.guiadosquadrinhos.com/edicao/ShowImage.aspx" +
            "?id=5968&path=abril/f/fxm030101.jpg&w=400&h=622", result.coverUrl)
    }
}
