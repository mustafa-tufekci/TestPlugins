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

    private val categories = mapOf(
        "Yabancı Diziler" to "/diziler.asp?kat=1",
        "Yerli Diziler" to "/diziler.asp?kat=2",
        "Asya Dizileri" to "/diziler.asp?kat=3",
        "Animasyonlar" to "/diziler.asp?kat=4",
        "Animeler" to "/diziler.asp?kat=5",
        "Belgeseller" to "/diziler.asp?kat=6"
    )

    private fun parseShowCards(doc: org.jsoup.nodes.Document): List<SearchResponse> {
        return doc.select("a.column[title]").mapNotNull { link ->
            val card = link.selectFirst("div.ui.card") ?: return@mapNotNull null
            val href: String = link.attr("href")
            if (href.isBlank()) return@mapNotNull null

            val title: String = card.selectFirst(".content .description")?.text()
                ?: link.attr("title").removeSuffix(" izle")

            val img = card.selectFirst("img[data-src], img[src]")
            val posterUrl: String? = img?.let {
                val src = it.attr("data-src").ifEmpty { it.attr("src") }
                if (src.startsWith("/") || src.startsWith("data:")) {
                    if (src.startsWith("/")) "$mainUrl$src" else null
                } else src
            }

            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val mainDoc = try {
            app.get(mainUrl).document
        } catch (_: Exception) {
            return newHomePageResponse(emptyList())
        }

        val allPages = mutableListOf<HomePageList>()

        // Popüler Diziler from homepage #enler section
        val enlerSection = mainDoc.selectFirst("#enler")
        if (enlerSection != null) {
            val popularShows = enlerSection.select("a.column[title]").mapNotNull { link ->
                val card = link.selectFirst("div.ui.card") ?: return@mapNotNull null
                val href: String = link.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val title: String = card.selectFirst(".content .description")?.text()
                    ?: link.attr("title")
                val img = card.selectFirst("img[src]")
                val posterUrl: String? = img?.let {
                    val src = it.attr("src")
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }
            if (popularShows.isNotEmpty()) {
                allPages.add(HomePageList("Popüler Diziler", popularShows))
            }
        }

        // Category pages
        for ((catName, path) in categories) {
            val doc = try {
                app.get("$mainUrl$path").document
            } catch (_: Exception) {
                continue
            }
            val shows = parseShowCards(doc)
            if (shows.isNotEmpty()) {
                allPages.add(HomePageList(catName, shows))
            }
        }

        return newHomePageResponse(allPages)
    }

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
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc: org.jsoup.nodes.Document = app.get(url).document

        val title: String = doc.selectFirst(".content .header")?.text()
            ?: doc.selectFirst("h1")?.text()
            ?: "Unknown"

        val img = doc.selectFirst("img[data-src]")
        val posterUrl: String? = img?.let {
            val src = it.attr("data-src")
            if (src.startsWith("/")) "$mainUrl$src" else src
        }

        val plot: String? = doc.selectFirst("#tartismayorum-konu blockquote")?.text()

        val yearText: String? = doc.selectFirst(".extra.content .right.floated")?.text()
        val year: Int? = yearText?.split("-")?.firstOrNull()?.trim()?.toIntOrNull()

        val genres: List<String> = doc.select("a.ui.blue.label.golge").map { it.text() }

        val imdbText: String? = doc.selectFirst(".ui.label.imdb .detail")?.text()

        val dataDizi: String? = doc.selectFirst("#dizidetay")?.attr("data-dizi")
            ?: doc.selectFirst("[data-dizi]")?.attr("data-dizi")

        val episodes: MutableList<Episode> = mutableListOf()

        if (dataDizi != null) {
            val episodesUrl = "$mainUrl/bolumler/$dataDizi.html"
            val episodesDoc: org.jsoup.nodes.Document = app.get(episodesUrl).document

            episodesDoc.select("table[sid]").forEach { table ->
                table.select("tr").forEach { row ->
                    val link = row.selectFirst("a[href*='-sezon-']") ?: return@forEach
                    val href: String = link.attr("href")
                    val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href

                    val match = Regex("(\\d+)-sezon-(\\d+)-bolum").find(href) ?: return@forEach
                    val s: Int = match.groupValues[1].toIntOrNull() ?: return@forEach
                    val e: Int = match.groupValues[2].toIntOrNull() ?: return@forEach

                    val tds = row.select("td")
                    val epTitle: String = tds.getOrNull(3)?.text()
                        ?: "$s. Sezon $e. Bölüm"

                    episodes.add(newEpisode(fullUrl) {
                        name = epTitle
                        season = s
                        episode = e
                        this.posterUrl = posterUrl
                    })
                }
            }
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = posterUrl
            this.plot = plot
            this.year = year
            this.tags = genres
            if (imdbText != null) {
                this.score = try {
                    Score.from10(imdbText)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc: org.jsoup.nodes.Document = app.get(data).document

        val episodeId: String = doc.selectFirst("#dilsec")?.attr("data-id") ?: return false

        val languages = listOf(
            Pair("1", "Altyazılı"),
            Pair("0", "Dublajlı")
        )

        val skipHosts = listOf("reCAPTCHADATA", "dzen.ru")
        val skipNames = listOf("pixel", "dzen")

        var found = false
        for ((dilCode, langName) in languages) {
            try {
                val alternatives: List<Alternative> = getAlternatives(episodeId, dilCode)
                for (alt in alternatives) {
                    val altName = alt.name.lowercase()

                    if (altName in skipNames) continue

                    val embedHtml: String = getEmbedHtml(alt.id) ?: continue

                    if (skipHosts.any { embedHtml.contains(it, ignoreCase = true) }) continue

                    val embedDoc: org.jsoup.nodes.Document = Jsoup.parse(embedHtml)
                    val iframe: org.jsoup.nodes.Element = embedDoc.selectFirst("iframe") ?: continue
                    var src: String = iframe.attr("src")
                    if (src.isBlank()) continue
                    if (src.startsWith("//")) src = "https:$src"

                    if (src.contains("bysejikuar.com")) {
                        src = src.replace("bysejikuar.com", "filemoon.to")
                    }

                    val langCallback: (ExtractorLink) -> Unit = { link ->
                        callback(
                            ExtractorLink(
                                link.source,
                                "$langName - ${link.name}",
                                link.url,
                                link.referer,
                                link.quality,
                                link.headers ?: emptyMap(),
                                link.extractorData,
                                link.type,
                                link.audioTracks ?: emptyList()
                            )
                        )
                    }

                    if (loadExtractor(src, mainUrl, subtitleCallback, langCallback)) {
                        found = true
                    }
                }
            } catch (_: Exception) {
            }
        }

        return found
    }

    private suspend fun getAlternatives(episodeId: String, dil: String): List<Alternative> {
        val response = app.post(
            "$mainUrl/ajax/dataAlternatif22.asp",
            data = mapOf("bid" to episodeId, "dil" to dil),
            headers = ajaxHeaders
        ).parsedSafe<AlternativesResponse>()

        return if (response?.status == "success") response.data else emptyList()
    }

    private suspend fun getEmbedHtml(id: String): String? {
        return app.post(
            "$mainUrl/ajax/dataEmbed22.asp",
            data = mapOf("id" to id),
            headers = ajaxHeaders
        ).text
    }

    data class Alternative(
        @JsonProperty("id") val id: String,
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
