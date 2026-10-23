package com.kiwizitos.collection.data.remote

import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.model.CoverItem
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.PaginationInfo
import com.kiwizitos.collection.data.model.SerieResult
import com.kiwizitos.collection.data.model.SeriesSearchResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object GuiaQuadrinhosParser {
    fun parseSeriesTableWithPagination(html: String): SeriesSearchResult {
        val doc = GuiaHtml.document(html)
        val explicitlyEmpty = doc.selectFirst("div#UpdatePanel1")?.text()
            ?.contains("não encontrou nenhuma referência.", ignoreCase = true) == true
        if (doc.selectFirst("table#tlbProfile, span#dataPageDisplayNumberOfPages, " +
                "span#dataPageDisplayNumberOfPages2") == null && !explicitlyEmpty) {
            throw GuiaUnexpectedPageException()
        }
        val series = doc.select("table#tlbProfile tr").mapNotNull { row ->
            val cells = row.select("td")
            if (cells.size < 5) return@mapNotNull null
            val link = cells[0].selectFirst("a") ?: throw GuiaUnexpectedPageException()
            if (link.text().isBlank() || link.attr("href").isBlank()) {
                throw GuiaUnexpectedPageException()
            }
            SerieResult(
                title = link.text().trim(),
                relativeLink = GuiaUrls.relativePath(link.attr("href")),
                publisher = cells[1].text().trim(),
                originalPublisher = cells[2].text().trim(),
                year = cells[3].text().trim(),
                issueCount = cells[4].text().trim()
            )
        }
        val pagination = pagination(doc)
        if (series.isEmpty() && pagination.totalResults > 0) throw GuiaUnexpectedPageException()
        return SeriesSearchResult(series, pagination)
    }

    fun parseCoverListWithPagination(html: String, seriesTitle: String): CoversSearchResult {
        val doc = GuiaHtml.document(html)
        if (doc.selectFirst("div.Lista_album_imagem_colecao, " +
                "span#MainContent_lstProfileView_dataPageDisplayNumberOfPages, " +
                "span#MainContent_lstProfileView_dataPageDisplayNumberOfPages2") == null) {
            throw GuiaUnexpectedPageException()
        }
        val covers = doc.select("div.Lista_album_imagem_colecao li").map { item ->
            val links = item.select("a")
            val link = links.firstOrNull() ?: throw GuiaUnexpectedPageException()
            val image = link.selectFirst("img") ?: throw GuiaUnexpectedPageException()
            if (link.attr("href").isBlank()) throw GuiaUnexpectedPageException()
            CoverItem(
                title = image.attr("alt").trim()
                    .ifBlank { link.attr("title").trim() }.ifBlank { "Edição" },
                relativeLink = GuiaUrls.relativePath(link.attr("href")),
                coverUrl = GuiaUrls.imageUrl(image.attr("src")).orEmpty(),
                year = Regex("(\\d{4})").find(links.getOrNull(1)?.text().orEmpty())?.value
            )
        }
        val pagination = pagination(doc)
        if (covers.isEmpty() && pagination.totalResults > 0) throw GuiaUnexpectedPageException()
        val hasStandaloneText = doc.body().text().contains("edição única", ignoreCase = true)
        val editionLinks = covers.map { it.relativeLink }.distinct()
        val isStandalone = !pagination.hasNextPage && pagination.totalResults <= 1 &&
            (hasStandaloneText || editionLinks.size == 1)
        return CoversSearchResult(
            seriesTitle = seriesTitle,
            covers = covers,
            paginationInfo = pagination,
            isStandalone = isStandalone,
            singleEditionUrl = if (isStandalone) editionLinks.firstOrNull() else null
        )
    }

    fun parseDetails(html: String, url: String): ComicDetails {
        val doc = GuiaHtml.document(html)
        val title = doc.selectFirst("span#nome_titulo_lb")?.text()?.trim()
            ?.takeIf { it.isNotBlank() } ?: throw GuiaUnexpectedPageException()
        fun text(selector: String) = doc.selectFirst(selector)?.text()?.trim()?.ifBlank { null }
        val galleryLink = doc.selectFirst("div.boxnumeros a[href*='capas/']")
            ?.attr("href")?.takeIf { it.isNotBlank() }
        return ComicDetails(
            title = title,
            publishedIn = text("span#data_publi"),
            publisher = text("a#editora_link"),
            licensor = text("span#licenciador"),
            category = text("span#categoria"),
            genre = text("span#genero"),
            status = text("span#status"),
            pages = text("span#paginas"),
            format = text("span#formato"),
            coverPrice = text("span#preco"),
            coverUrl = doc.selectFirst("div#cover img")?.attr("src")
                ?.let { GuiaUrls.imageUrl(it, GuiaUrls.pageUrl(url)) },
            coverArtist = doc.selectFirst("div#texto_pag_detalhe")?.let { extractCoverArtist(it.html()) },
            seriesUrl = galleryLink?.let(GuiaUrls::relativePath),
            seriesTitle = doc.selectFirst("span#nome_titulo_lb")?.textNodes()
                ?.firstOrNull()?.text()?.trim()?.ifBlank { null }
        )
    }

    private fun extractCoverArtist(html: String): String? {
        val doc = Jsoup.parseBodyFragment(html)
        val strong = doc.select("strong").firstOrNull {
            it.text().trim().equals("Arte:", ignoreCase = true)
        } ?: return null
        val result = StringBuilder()
        var node = strong.nextSibling()
        while (node != null) {
            val value = node.toString()
            if (value.contains("<br") || value.contains("<strong") || value.contains("<div")) break
            if (node is Element) result.append(node.text()) else result.append(value.trim())
            node = node.nextSibling()
        }
        return result.toString().trim().replace(Regex("\\s+"), " ").ifBlank { null }
    }

    fun extractPaginationInfo(html: String): PaginationInfo = pagination(GuiaHtml.document(html))

    private fun pagination(doc: Document): PaginationInfo {
        val pager = doc.selectFirst(
            "span#dataPagerNumeric, span#dataPagerNumeric2, " +
                "span#MainContent_lstProfileView_dataPagerNumeric, " +
                "span#MainContent_lstProfileView_dataPagerNumeric2"
        )
        val next = pager?.select("a.next_last")?.firstOrNull {
            it.text().trim() == ">" && it.hasAttr("href")
        }
        val eventTarget = next?.attr("href")?.let {
            Regex("""__doPostBack\('([^']+)'""").find(it)?.groupValues?.get(1)
        }
        val countText = doc.selectFirst(
            "span#dataPageDisplayNumberOfPages, span#dataPageDisplayNumberOfPages2, " +
                "span#MainContent_lstProfileView_dataPageDisplayNumberOfPages, " +
                "span#MainContent_lstProfileView_dataPageDisplayNumberOfPages2"
        )?.text().orEmpty()
        return PaginationInfo(
            currentPage = pager?.selectFirst("span.current_page")?.text()?.trim()?.toIntOrNull() ?: 1,
            totalResults = Regex("""de\s+([\d.]+)""").find(countText)
                ?.groupValues?.get(1)?.replace(".", "")?.toIntOrNull() ?: 0,
            hasNextPage = next != null,
            viewState = doc.selectFirst("input#__VIEWSTATE")?.attr("value"),
            eventValidation = doc.selectFirst("input#__EVENTVALIDATION")?.attr("value"),
            eventTarget = eventTarget,
            viewStateGenerator = doc.selectFirst("input#__VIEWSTATEGENERATOR")?.attr("value")
        )
    }
}
