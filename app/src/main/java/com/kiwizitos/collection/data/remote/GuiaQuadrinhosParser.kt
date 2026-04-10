package com.kiwizitos.collection.data.remote

import android.util.Log
import com.kiwizitos.collection.data.model.CoverItem
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.model.CoversSearchResult
import com.kiwizitos.collection.data.model.PaginationInfo
import com.kiwizitos.collection.data.model.SerieResult
import com.kiwizitos.collection.data.model.SeriesSearchResult
import org.jsoup.Jsoup

private const val TAG = "GuiaParser"
private const val BASE_URL = "http://www.guiadosquadrinhos.com"

/**
 * Parser de HTML para o Guia dos Quadrinhos usando Jsoup.
 *
 * Baseado na estrutura real do site (HTML analisado em abril/2026):
 * - Resultados em `table#tlbProfile tr`
 * - Paginação em `span#dataPagerNumeric` / `span#dataPagerNumeric2`
 * - Contador em `span#dataPageDisplayNumberOfPages`
 */
object GuiaQuadrinhosParser {

    // ── Séries ────────────────────────────────────────────────────────────────

    /**
     * Parseia a página de resultados de séries e retorna séries + paginação.
     */
    fun parseSeriesTableWithPagination(html: String): SeriesSearchResult {
        val series = parseSeriesTable(html)
        val pagination = extractPaginationInfo(html)
        Log.d(TAG, "parseSeriesTable: ${series.size} séries, pg ${pagination.currentPage}, total ${pagination.totalResults}, hasNext=${pagination.hasNextPage}")
        return SeriesSearchResult(series = series, paginationInfo = pagination)
    }

    private fun parseSeriesTable(html: String): List<SerieResult> {
        val results = mutableListOf<SerieResult>()
        return try {
            val doc = Jsoup.parse(html)

            // Seletor exato baseado no HTML real: table#tlbProfile tr
            val rows = doc.select("table#tlbProfile tr")

            for (row in rows) {
                try {
                    val cells = row.select("td")
                    if (cells.size < 5) continue

                    // O link está sempre no primeiro td, tag <a>
                    val linkEl = cells[0].selectFirst("a") ?: continue
                    val title = linkEl.text().trim()
                    if (title.isBlank()) continue

                    // href vem como '../capas/titulo/codigo' — remove o '../'
                    val relativeLink = linkEl.attr("href")
                        .removePrefix("../")
                        .trim()

                    results.add(
                        SerieResult(
                            title             = title,
                            relativeLink      = relativeLink,
                            publisher         = cells[1].text().trim(),
                            originalPublisher = cells[2].text().trim(),
                            year              = cells[3].text().trim(),
                            issueCount        = cells[4].text().trim()
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao parsear linha: ${e.message}")
                }
            }

            Log.d(TAG, "parseSeriesTable: ${results.size} séries encontradas")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parsear tabela de séries", e)
            results
        }
    }

    // ── Capas ─────────────────────────────────────────────────────────────────

    /**
     * Parseia a lista de capas de uma série + paginação.
     */
    fun parseCoverListWithPagination(html: String, seriesTitle: String): CoversSearchResult {
        val covers = parseCoverList(html)
        val pagination = extractPaginationInfo(html)
        Log.d(TAG, "parseCoverList: ${covers.size} capas, pg ${pagination.currentPage}, hasNext=${pagination.hasNextPage}")
        return CoversSearchResult(
            seriesTitle    = seriesTitle,
            covers         = covers,
            paginationInfo = pagination
        )
    }

    private fun parseCoverList(html: String): List<CoverItem> {
        val results = mutableListOf<CoverItem>()
        return try {
            val doc = Jsoup.parse(html)

            // Seletor exato do HTML real:
            // <div class="Lista_album_imagem_colecao"><li>...<a href="../../edicao/..."><img src="http://..."/></a>
            val items = doc.select("div.Lista_album_imagem_colecao li")

            for (item in items) {
                try {
                    // Primeiro link tem a imagem; segundo link (após <br/>) tem o número
                    val links = item.select("a")
                    if (links.isEmpty()) continue

                    val imgLink  = links[0]  // link com a imagem e href para a edição
                    val imgEl    = imgLink.selectFirst("img") ?: continue

                    // href: "../../edicao/titulo/codigo/id" → remove "../../"
                    val relativeLink = imgLink.attr("href")
                        .removePrefix("../../")
                        .trim()

                    // Título vem do atributo alt da imagem: "Fabulosos X-Men, Os n° 1 - Abril"
                    val titleText = imgEl.attr("alt").trim()
                        .ifBlank { imgLink.attr("title").trim() }
                        .ifBlank { "Edição" }

                    // URL da capa já é absoluta: "http://www.guiadosquadrinhos.com//capasthumbs/..."
                    val coverUrl = imgEl.attr("src").trim()

                    val year = Regex("(\\d{4})").find(
                        // Tenta pegar o ano do texto do segundo link (ex: "janeiro de 1996")
                        links.getOrNull(1)?.text() ?: ""
                    )?.value

                    results.add(
                        CoverItem(
                            title        = titleText,
                            relativeLink = relativeLink,
                            coverUrl     = coverUrl,
                            year         = year
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao parsear capa: ${e.message}")
                }
            }

            Log.d(TAG, "parseCoverList: ${results.size} capas encontradas")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parsear capas", e)
            results
        }
    }

    // ── Detalhes ──────────────────────────────────────────────────────────────

    fun parseDetails(html: String, url: String): ComicDetails? {
        return try {
            val doc = Jsoup.parse(html)

            val title = doc.selectFirst("span#nome_titulo_lb")
                ?.text()?.trim() ?: return null

            val coverUrl    = doc.selectFirst("div#cover img")?.attr("src")?.trim()?.ifBlank { null }
            val publishedIn = doc.selectFirst("span#data_publi")?.text()?.trim()?.ifBlank { null }
            val publisher   = doc.selectFirst("a#editora_link")?.text()?.trim()?.ifBlank { null }
            val licensor    = doc.selectFirst("span#licenciador")?.text()?.trim()?.ifBlank { null }
            val category    = doc.selectFirst("span#categoria")?.text()?.trim()?.ifBlank { null }
            val genre       = doc.selectFirst("span#genero")?.text()?.trim()?.ifBlank { null }
            val status      = doc.selectFirst("span#status")?.text()?.trim()?.ifBlank { null }
            val pages       = doc.selectFirst("span#paginas")?.text()?.trim()?.ifBlank { null }
            val format      = doc.selectFirst("span#formato")?.text()?.trim()?.ifBlank { null }
            val coverPrice  = doc.selectFirst("span#preco")?.text()?.trim()?.ifBlank { null }
            val coverArtist = doc.selectFirst("div#texto_pag_detalhe")
                ?.let { extractCoverArtist(it.html()) }

            ComicDetails(
                title       = title,
                publishedIn = publishedIn,
                publisher   = publisher,
                licensor    = licensor,
                category    = category,
                genre       = genre,
                status      = status,
                pages       = pages,
                format      = format,
                coverPrice  = coverPrice,
                coverUrl    = coverUrl,
                coverArtist = coverArtist
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parsear detalhes: $url", e)
            null
        }
    }

    /** Extrai apenas o crédito de Arte da capa, antes das histórias. */
    private fun extractCoverArtist(html: String): String? {
        return try {
            val doc = Jsoup.parseBodyFragment(html)
            val strong = doc.select("strong").firstOrNull {
                it.text().trim().equals("Arte:", ignoreCase = true)
            } ?: return null
            val result = StringBuilder()
            var node = strong.nextSibling()
            while (node != null) {
                val s = node.toString()
                if (s.contains("<br") || s.contains("<strong") || s.contains("<div")) break
                if (node is org.jsoup.nodes.Element) result.append(node.text())
                else result.append(s.trim())
                node = node.nextSibling()
            }
            result.toString().trim().replace(Regex("\\s+"), " ").ifBlank { null }
        } catch (e: Exception) { null }
    }

    // ── Paginação ─────────────────────────────────────────────────────────────

    /**
     * Extrai informações de paginação ASP.NET do HTML real do site.
     *
     * Estrutura real (confirmada no HTML):
     * - ViewState: `input#__VIEWSTATE`
     * - EventValidation: `input#__EVENTVALIDATION`
     * - Próxima página: `a.next_last` com `>` OU `a.next_button` com `...`
     *   — contêm `javascript:__doPostBack('ctl00$...$ctl02$ctl00','')` no href
     * - Página atual: `span.current_page`
     * - Contador: `span#dataPageDisplayNumberOfPages` → "Itens: 1 - 30 de 208"
     */
    fun extractPaginationInfo(html: String): PaginationInfo {
        return try {
            val doc = Jsoup.parse(html)

            val viewState       = doc.selectFirst("input#__VIEWSTATE")?.attr("value")
            val eventValidation = doc.selectFirst("input#__EVENTVALIDATION")?.attr("value")

            // Suporta tanto a página de títulos quanto a de capas.
            // Títulos:  span#dataPagerNumeric / span#dataPagerNumeric2
            // Capas:    span#MainContent_lstProfileView_dataPagerNumeric2
            val pagerSpan = doc.selectFirst(
                "span#dataPagerNumeric, " +
                "span#dataPagerNumeric2, " +
                "span#MainContent_lstProfileView_dataPagerNumeric, " +
                "span#MainContent_lstProfileView_dataPagerNumeric2"
            )

            // Botão ">" — a.next_last cujo texto é exatamente ">"
            val nextLink = pagerSpan?.select("a.next_last")
                ?.firstOrNull { it.text().trim() == ">" }

            val hasNextPage = nextLink != null

            val eventTarget = nextLink?.attr("href")?.let { href ->
                Regex("""__doPostBack\('([^']+)'""").find(href)?.groupValues?.get(1)
            }

            val currentPage = pagerSpan?.selectFirst("span.current_page")
                ?.text()?.trim()?.toIntOrNull() ?: 1

            // Suporta ambos os IDs do contador de resultados
            val pageInfoText = doc.selectFirst(
                "span#dataPageDisplayNumberOfPages, " +
                "span#dataPageDisplayNumberOfPages2, " +
                "span#MainContent_lstProfileView_dataPageDisplayNumberOfPages, " +
                "span#MainContent_lstProfileView_dataPageDisplayNumberOfPages2"
            )?.text() ?: ""

            val totalResults = Regex("""de\s+(\d+)""")
                .find(pageInfoText)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            Log.d(TAG, "Paginação: pg=$currentPage, total=$totalResults, hasNext=$hasNextPage, target=$eventTarget")

            PaginationInfo(
                currentPage     = currentPage,
                totalResults    = totalResults,
                hasNextPage     = hasNextPage,
                viewState       = viewState,
                eventValidation = eventValidation,
                eventTarget     = eventTarget
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao extrair paginação", e)
            PaginationInfo()
        }
    }
}
