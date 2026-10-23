package com.kiwizitos.collection.data.remote

import org.junit.Assert.*
import org.junit.Test

class GuiaQuadrinhosParserTest {
    private fun coverPage(total: Int = 1, next: Boolean = false) = """
        <input id="__VIEWSTATE" value="state">
        <input id="__EVENTVALIDATION" value="validation">
        <input id="__VIEWSTATEGENERATOR" value="BDEF46B4">
        <span id="MainContent_lstProfileView_dataPageDisplayNumberOfPages2">Itens: 1 - 1 de $total</span>
        <span id="MainContent_lstProfileView_dataPagerNumeric2"><span class="current_page">1</span>
          ${if (next) """<a class="next_last" href="javascript:__doPostBack('next','')">&gt;</a>""" else ""}
        </span>
        <div class="Lista_album_imagem_colecao"><ul><li>
          <a href="../../edicao/titulo/ab01/123"><img alt="Título 1" src="http://www.guiadosquadrinhos.com//capasthumbs/capa.jpg"></a>
          <a>janeiro de 1996</a>
        </li></ul></div>
    """.trimIndent()

    @Test fun extractsCoversAndPageSpecificGenerator() {
        val result = GuiaQuadrinhosParser.parseCoverListWithPagination(coverPage(55, true), "Título")
        assertEquals("BDEF46B4", result.paginationInfo.viewStateGenerator)
        assertEquals("state", result.paginationInfo.viewState)
        assertEquals("validation", result.paginationInfo.eventValidation)
        assertEquals("next", result.paginationInfo.eventTarget)
        assertTrue(result.paginationInfo.hasNextPage)
        assertEquals(55, result.paginationInfo.totalResults)
        assertFalse(result.isStandalone)
        assertEquals("edicao/titulo/ab01/123", result.covers.single().relativeLink)
        assertEquals("https://www.guiadosquadrinhos.com//capasthumbs/capa.jpg", result.covers.single().coverUrl)
        assertEquals("1996", result.covers.single().year)
    }

    @Test fun detectsSingleEditionWithoutMisclassifyingLastPage() {
        assertTrue(GuiaQuadrinhosParser.parseCoverListWithPagination(coverPage(), "Título").isStandalone)
        assertFalse(GuiaQuadrinhosParser.parseCoverListWithPagination(coverPage(31), "Título").isStandalone)
    }

    @Test fun ignoresDisabledNextButton() {
        val result = GuiaQuadrinhosParser.extractPaginationInfo(
            """<span id="dataPagerNumeric"><a class="aspNetDisabled next_last">&gt;</a></span>"""
        )
        assertFalse(result.hasNextPage)
    }

    @Test fun parsesSeriesAndLegitimateEmptyTable() {
        val result = GuiaQuadrinhosParser.parseSeriesTableWithPagination("""
            <input id="__VIEWSTATEGENERATOR" value="EEB05935">
            <table id="tlbProfile"><tr><th>Título</th></tr><tr>
            <td><a href="../capas/titulo/ab01">Título</a></td><td>Abril</td>
            <td>Marvel</td><td>1996</td><td>55</td></tr></table>
        """)
        assertEquals("capas/titulo/ab01", result.series.single().relativeLink)
        assertEquals("EEB05935", result.paginationInfo.viewStateGenerator)
        assertTrue(GuiaQuadrinhosParser.parseSeriesTableWithPagination(
            """<table id="tlbProfile"></table>"""
        ).series.isEmpty())
    }

    @Test fun resolvesDetailsImageRelativeToEditionPage() {
        val details = GuiaQuadrinhosParser.parseDetails("""
            <span id="nome_titulo_lb">Título <nobr>n° 1</nobr></span>
            <div id="cover"><img src="ShowImage.aspx?id=123&amp;path=a.jpg"></div>
            <div class="boxnumeros"><a href="../../../capas/titulo/ab01">Galeria de capas</a></div>
        """, "${GuiaUrls.BASE_URL}edicao/titulo/ab01/123")
        assertEquals("capas/titulo/ab01", details.seriesUrl)
        assertEquals("${GuiaUrls.BASE_URL}edicao/titulo/ab01/ShowImage.aspx?id=123&path=a.jpg", details.coverUrl)
    }

    @Test fun acceptsActualEmptySearchMarkupWithoutTableOrCounters() {
        val result = GuiaQuadrinhosParser.parseSeriesTableWithPagination("""
            <div id="UpdatePanel1">
              Sua pesquisa pelo título <strong>inexistente</strong> não encontrou nenhuma referência.
            </div>
        """)
        assertTrue(result.series.isEmpty())
        assertFalse(result.paginationInfo.hasNextPage)
    }

    @Test(expected = GuiaAccessChallengeException::class)
    fun rejectsChallengeEvenWithSuccessfulHttpStatus() {
        GuiaQuadrinhosParser.parseSeriesTableWithPagination(
            """<html><script src="/cdn-cgi/challenge-platform/test"></script></html>"""
        )
    }

    @Test(expected = GuiaUnexpectedPageException::class)
    fun rejectsUnexpectedPageInsteadOfReportingEmptyResults() {
        GuiaQuadrinhosParser.parseSeriesTableWithPagination("<html><h1>Manutenção</h1></html>")
    }

    @Test(expected = GuiaUnexpectedPageException::class)
    fun rejectsUnexpectedDetails() {
        GuiaQuadrinhosParser.parseDetails("<html></html>", GuiaUrls.BASE_URL)
    }
}
