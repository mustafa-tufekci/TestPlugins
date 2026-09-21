package com.panates

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup

class SezonlukDizi : MainAPI() {
    override var mainUrl = "https://sezonlukdizi.cc"
    override var name = "SezonlukDizi"
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Anime)
    override var lang = "tr"
    override val hasMainPage = true

    private val ajaxHeaders = mapOf(
        "X-Requested-With" to "XMLHttpRequest",
        "Content-Type" to "application/x-www-form-urlencoded"
    )

    // mainPageOf enables per-category lazy loading; each category is fetched
    // only when the user scrolls to it — no more 7 simultaneous requests on startup.
    override val mainPage = mainPageOf(
        "$mainUrl/diziler.asp?kat=1" to "Yabancı Diziler",
        "$mainUrl/diziler.asp?kat=2" to "Yerli Diziler",
        "$mainUrl/diziler.asp?kat=3" to "Asya Dizileri",
        "$mainUrl/diziler.asp?kat=4" to "Animasyonlar",
        "$mainUrl/diziler.asp?kat=5" to "Animeler",
        "$mainUrl/diziler.asp?kat=6" to "Belgeseller"
    )

    // ── Main Page ────────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        // On first load (before any category is selected) show Popular Diziler from homepage
        val url = request.data
        val doc = try {
            app.get(url).document
        } catch (_: Exception) {
            return newHomePageResponse(request.name, emptyList())
        }

        val shows = parseShowCards(doc)
        return newHomePageResponse(request.name, shows)
    }

    // ── Card Parser ──────────────────────────────────────────────────────

    /**
     * Parse series cards from listing pages.
     * Confirmed selector from live HTML: href attribute comes before class in the tag,
     * so we match both orderings.
     * Card structure: <a href="..." class="column" title="..."><div class="ui card">...
     */
    private fun parseShowCards(doc: org.jsoup.nodes.Document): List<SearchResponse> {
        return doc.select("a.column[title]").mapNotNull { link ->
            val card = link.selectFirst("div.ui.card") ?: return@mapNotNull null
            val href = link.attr("href")
            if (href.isBlank()) return@mapNotNull null
            val fullUrl = fixUrl(href)

            val title = card.selectFirst(".content .description")?.text()
                ?: link.attr("title").removeSuffix(" izle").trim()

            val img = card.selectFirst("img[data-src], img[src]")
            val posterUrl = img?.let {
                val src = it.attr("data-src").ifEmpty { it.attr("src") }
                when {
                    src.startsWith("data:") -> null
                    src.startsWith("/") -> "$mainUrl$src"
                    src.isBlank() -> null
                    else -> src
                }
            }

            newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
    }

    // ── Search ───────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            val response = app.post(
                "$mainUrl/ajax/arama.asp",
                data = mapOf("q" to query),
                headers = ajaxHeaders
            ).parsedSafe<SearchApiResponse>()

            response?.results?.diziler?.results?.mapNotNull { item ->
                val href = item.url ?: return@mapNotNull null
                val title = item.title ?: return@mapNotNull null
                val posterUrl = item.image?.let { img ->
                    if (img.startsWith("/")) "$mainUrl$img" else img
                }
                newTvSeriesSearchResponse(title, fixUrl(href), TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        // Title: confirmed selector ".content .header" from live test
        val title = doc.selectFirst(".content .header")?.text()?.trim()
            ?: doc.selectFirst("h1")?.text()?.trim()
            ?: "Unknown"

        val img = doc.selectFirst("img[data-src]")
        val posterUrl = img?.let {
            val src = it.attr("data-src")
            if (src.startsWith("/")) "$mainUrl$src" else src
        }

        val plot = doc.selectFirst("#tartismayorum-konu blockquote")?.text()

        // Year: confirmed "1999" appears in ".extra.content .right.floated" text
        val yearText = doc.selectFirst(".extra.content .right.floated")?.text()
        val year = yearText?.split("-")?.firstOrNull()?.trim()?.toIntOrNull()

        val genres = doc.select("a.ui.blue.label.golge").map { it.text() }

        // IMDB: "9,0" — replace comma with dot before parsing
        val imdbText = doc.selectFirst(".ui.label.imdb .detail")?.text()
            ?.replace(",", ".")?.replace(Regex("[^0-9.]"), "")

        val dataDizi = doc.selectFirst("#dizidetay")?.attr("data-dizi")
            ?: doc.selectFirst("[data-dizi]")?.attr("data-dizi")

        val episodes = mutableListOf<Episode>()

        if (dataDizi != null) {
            val episodesUrl = "$mainUrl/bolumler/$dataDizi.html"
            try {
                val episodesDoc = app.get(episodesUrl).document

                episodesDoc.select("table[sid]").forEach { table ->
                    table.select("tr").forEach { row ->
                        val link = row.selectFirst("a[href*='-sezon-']") ?: return@forEach
                        val href = link.attr("href")
                        val fullUrl = fixUrl(href)

                        val match = Regex("(\\d+)-sezon-(\\d+)-bolum").find(href)
                            ?: return@forEach
                        val s = match.groupValues[1].toIntOrNull() ?: return@forEach
                        val e = match.groupValues[2].toIntOrNull() ?: return@forEach

                        val tds = row.select("td")
                        val epTitle = tds.getOrNull(3)?.text()?.trim()
                            ?: "$s. Sezon $e. Bölüm"

                        episodes.add(newEpisode(fullUrl) {
                            name = epTitle
                            season = s
                            episode = e
                            this.posterUrl = posterUrl
                        })
                    }
                }
            } catch (_: Exception) {}
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = posterUrl
            this.plot = plot
            this.year = year
            this.tags = genres
            if (!imdbText.isNullOrBlank()) {
                this.score = try {
                    Score.from10(imdbText)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    // ── Load Links ───────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document

        val episodeId = doc.selectFirst("#dilsec")?.attr("data-id") ?: return false

        val languages = listOf(
            "1" to "Altyazılı",
            "0" to "Dublajlı"
        )

        // Hosts/names to skip (confirmed broken or non-video sources)
        val skipHosts = listOf("reCAPTCHADATA", "dzen.ru")
        val skipNames = listOf("pixel", "dzen", "netu", "streamruby", "abstream")

        var found = false
        for ((dilCode, langName) in languages) {
            try {
                val alternatives = getAlternatives(episodeId, dilCode)
                for (alt in alternatives) {
                    val altName = alt.name.lowercase()
                    if (altName in skipNames) continue

                    val embedHtml = getEmbedHtml(alt.id) ?: continue
                    if (skipHosts.any { embedHtml.contains(it, ignoreCase = true) }) continue

                    val embedDoc = Jsoup.parse(embedHtml)
                    val iframe = embedDoc.selectFirst("iframe") ?: continue
                    var src = iframe.attr("src")
                    if (src.isBlank()) continue
                    if (src.startsWith("//")) src = "https:$src"

                    // vidmoly.net → vidmoly.biz (confirmed: site returns .net, .biz needed for playback)
                    if (src.contains("vidmoly.net")) {
                        src = src.replace("vidmoly.net", "vidmoly.biz")
                    }

                    val langCallback: (ExtractorLink) -> Unit = { link ->
                        runCatching {
                            callback(
                                ExtractorLink(
                                    link.source ?: "",
                                    "$langName - ${link.name}",
                                    link.url ?: "",
                                    link.referer ?: mainUrl,
                                    link.quality,
                                    link.headers ?: emptyMap(),
                                    link.extractorData,
                                    link.type,
                                    link.audioTracks ?: emptyList()
                                )
                            )
                        }
                    }

                    if (loadExtractor(src, mainUrl, subtitleCallback, langCallback)) {
                        found = true
                    }
                }
            } catch (_: Exception) {}
        }

        return found
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    private suspend fun getAlternatives(episodeId: String, dil: String): List<Alternative> {
        val response = app.post(
            "$mainUrl/ajax/dataAlternatif22.asp",
            data = mapOf("bid" to episodeId, "dil" to dil),
            headers = ajaxHeaders
        ).parsedSafe<AlternativesResponse>()

        return if (response?.status == "success") response.data else emptyList()
    }

    private suspend fun getEmbedHtml(id: Int): String? {
        return app.post(
            "$mainUrl/ajax/dataEmbed22.asp",
            data = mapOf("id" to id.toString()),
            headers = ajaxHeaders
        ).text
    }

    // ── Data Classes ─────────────────────────────────────────────────────

    data class Alternative(
        @JsonProperty("id") val id: Int,
        @JsonProperty("baslik") val name: String
    )

    data class AlternativesResponse(
        @JsonProperty("status") val status: String,
        @JsonProperty("data") val data: List<Alternative>
    )

    data class SearchApiResponse(
        @JsonProperty("status") val status: String,
        @JsonProperty("results") val results: SearchApiResults?
    )

    data class SearchApiResults(
        @JsonProperty("diziler") val diziler: SearchApiCategory?
    )

    data class SearchApiCategory(
        @JsonProperty("results") val results: List<SearchApiItem>?
    )

    data class SearchApiItem(
        @JsonProperty("did") val did: Int?,
        @JsonProperty("title") val title: String?,
        @JsonProperty("url") val url: String?,
        @JsonProperty("image") val image: String?,
        @JsonProperty("imdb") val imdb: Any?
    )
}
