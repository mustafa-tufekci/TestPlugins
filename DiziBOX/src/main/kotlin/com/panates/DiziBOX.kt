package com.panates

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class DiziBox : MainAPI() {
    override var mainUrl = "https://www.dizibox.live"
    override var name = "DiziBox"
    override val supportedTypes = setOf(TvType.TvSeries)
    override var lang = "tr"
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val allPages = mutableListOf<HomePageList>()

        val doc = try {
            app.get(mainUrl).document
        } catch (_: Exception) {
            return newHomePageResponse(emptyList())
        }

        // Beklenen / Eklenen Diziler
        try {
            val items = doc.select("#recommended_series li").mapNotNull { li ->
                val link = li.selectFirst("a") ?: return@mapNotNull null
                val href = link.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                val title = link.selectFirst("span.baslik")?.text()?.trim()
                    ?: link.attr("title").substringBefore(" ").trim()
                if (title.isBlank()) return@mapNotNull null
                val season = link.selectFirst("small")?.text()?.trim() ?: ""
                val posterUrl = link.selectFirst("img")?.attr("data-src")?.let { src ->
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Beklenen / Eklenen Diziler", items))
        } catch (_: Exception) {}

        // Dikkat Çeken Yeni Diziler
        try {
            val items = doc.select("#new-series article.article-series-poster").mapNotNull { article ->
                val titleLink = article.selectFirst("a.poster-title") ?: return@mapNotNull null
                val href = titleLink.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                val title = titleLink.text().trim()
                if (title.isBlank()) return@mapNotNull null
                val imdb = article.selectFirst("div.imdb")?.text()?.replace(Regex("[^0-9.]"), "")?.trim()
                val year = article.selectFirst("div.release")?.text()?.replace(Regex("[^0-9]"), "")?.trim()?.toIntOrNull()
                val posterUrl = article.selectFirst("img.afis")?.attr("data-src")?.let { src ->
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                    this.year = year
                    if (imdb != null) this.score = Score.from10(imdb)
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Dikkat Çeken Yeni Diziler", items))
        } catch (_: Exception) {}

        // Popüler Dizilerden Son Bölümler
        try {
            val firstSection = doc.select("section.m-b-1").firstOrNull()
            if (firstSection != null) {
                val items = firstSection.select("article.article-episode-card").mapNotNull { article ->
                    val titleLink = article.selectFirst("a.episode-card-title") ?: return@mapNotNull null
                    val href = titleLink.attr("href")
                    if (href.isBlank()) return@mapNotNull null
                    val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                    val seriesName = article.selectFirst("b.series-name")?.text()?.trim() ?: return@mapNotNull null
                    val seasonText = article.selectFirst("span.season")?.text()?.trim() ?: ""
                    val episodeText = article.selectFirst("b.episode")?.text()?.trim() ?: ""
                    val title = "$seriesName $seasonText $episodeText".trim()
                    val posterUrl = article.selectFirst("img.afis")?.attr("data-src")?.let { src ->
                        if (src.startsWith("/")) "$mainUrl$src" else src
                    }
                    newTvSeriesSearchResponse(seriesName, fullUrl, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                }.distinctBy { it.url }
                if (items.isNotEmpty()) allPages.add(HomePageList("Popüler Dizilerden Son Bölümler", items))
            }
        } catch (_: Exception) {}

        // Yeni Eklenen Bölümler
        try {
            val secondSection = doc.select("section.m-b-1").getOrNull(1)
            if (secondSection != null) {
                val items = secondSection.select("article.article-episode-card").mapNotNull { article ->
                    val titleLink = article.selectFirst("a.episode-card-title") ?: return@mapNotNull null
                    val href = titleLink.attr("href")
                    if (href.isBlank()) return@mapNotNull null
                    val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                    val seriesName = article.selectFirst("b.series-name")?.text()?.trim() ?: return@mapNotNull null
                    val posterUrl = article.selectFirst("img.afis")?.attr("data-src")?.let { src ->
                        if (src.startsWith("/")) "$mainUrl$src" else src
                    }
                    newTvSeriesSearchResponse(seriesName, fullUrl, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                }.distinctBy { it.url }
                if (items.isNotEmpty()) allPages.add(HomePageList("Yeni Eklenen Bölümler", items))
            }
        } catch (_: Exception) {}

        // Efsane Diziler
        try {
            val items = doc.select("#best-series article.article-series-small-grid").mapNotNull { article ->
                val detailsLink = article.selectFirst("a.series-details") ?: return@mapNotNull null
                val href = detailsLink.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                val title = article.selectFirst("div.tv-title")?.text()?.trim()?.removeSuffix(" izle") ?: return@mapNotNull null
                if (title.isBlank()) return@mapNotNull null
                val imdb = article.selectFirst("span.imdb")?.text()?.trim()
                val genres = article.selectFirst("div.turler")?.text()?.trim()
                val posterUrl = article.selectFirst("img.afis")?.attr("data-src")?.let { src ->
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                    if (imdb != null) this.score = Score.from10(imdb)
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Efsane Diziler", items))
        } catch (_: Exception) {}

        // Çeviri Durumu
        try {
            val items = doc.select("#translation-status article.article-episode-small-grid").mapNotNull { article ->
                val titleLink = article.selectFirst("a.tv-title") ?: article.selectFirst("a.square-thumbnail") ?: return@mapNotNull null
                val href = titleLink.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                val seriesName = article.selectFirst("strong.archive")?.text()?.trim() ?: return@mapNotNull null
                val seasonText = article.selectFirst("span.season")?.text()?.trim() ?: ""
                val episodeText = article.selectFirst("span.episode")?.text()?.trim() ?: ""
                val posterUrl = article.selectFirst("img")?.attr("data-src")?.let { src ->
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(seriesName, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Çeviri Durumu", items))
        } catch (_: Exception) {}

        // Önerilen Diziler
        try {
            val items = doc.select("#recommended-series article.article-series-small-grid").mapNotNull { article ->
                val detailsLink = article.selectFirst("a.series-details") ?: return@mapNotNull null
                val href = detailsLink.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
                val title = article.selectFirst("div.tv-title")?.text()?.trim()?.removeSuffix(" izle") ?: return@mapNotNull null
                if (title.isBlank()) return@mapNotNull null
                val imdb = article.selectFirst("span.imdb")?.text()?.trim()
                val genres = article.selectFirst("div.turler")?.text()?.trim()
                val posterUrl = article.selectFirst("img.afis")?.attr("data-src")?.let { src ->
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                    if (imdb != null) this.score = Score.from10(imdb)
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Önerilen Diziler", items))
        } catch (_: Exception) {}

        return newHomePageResponse(allPages)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // Try AJAX search first (Dave's WordPress Live Search)
        try {
            val ajaxResults = try {
                app.get(
                    "$mainUrl/wp-admin/admin-ajax.php",
                    params = mapOf("action" to "dwls_search", "s" to query)
                ).parsedSafe<AjaxSearchResponse>()
            } catch (_: Exception) { null }

            if (ajaxResults != null && ajaxResults.results.isNotEmpty()) {
                return ajaxResults.results.mapNotNull { result ->
                    val href = result.permalink ?: return@mapNotNull null
                    val title = result.postTitle ?: return@mapNotNull null
                    if (href.isBlank() || title.isBlank()) return@mapNotNull null
                    val posterUrl = result.attachmentThumbnail
                    newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback: standard ?s= search
        try {
            val doc = app.get("$mainUrl/?s=$query").document
            return doc.select("article.detailed-article").mapNotNull { article ->
                val titleEl = article.selectFirst("h3 a") ?: return@mapNotNull null
                val href = titleEl.attr("href")
                val title = titleEl.text().trim()
                if (href.isBlank() || title.isBlank()) return@mapNotNull null
                val posterUrl = article.selectFirst("figure img")?.attr("src")?.let { src ->
                    if (src.startsWith("/")) "$mainUrl$src" else src
                }
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }
        } catch (_: Exception) {}

        return emptyList()
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst(".tv-overview h1, h1.cat-title")?.text()?.trim()
            ?: doc.title().substringBefore(" - DiziBOX").trim().ifEmpty { "Unknown" }

        val posterUrl = doc.selectFirst(".tv-overview img, .category-cover img")?.attr("src")?.let { src ->
            if (src.startsWith("/")) "$mainUrl$src" else src
        }

        val plot = doc.selectFirst(".tv-overview .text-muted-darker, .tv-overview p")?.text()?.trim()

        val genres = doc.select(".tv-overview .custom-field a, .custom-fields-container .custom-field a")
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }

        val imdbText = doc.selectFirst("span.label-imdb b, .label-imdb")?.text()
        val imdbScore = imdbText?.toDoubleOrNull()

        val yearText = doc.selectFirst(".custom-field:has(i.icon-globe)")?.text()
        val year = yearText?.replace(Regex("[^0-9]"), "")?.take(4)?.toIntOrNull()

        val episodes = mutableListOf<Episode>()

        val seasonLinks = doc.select("#seasons-list a.btn").map { link ->
            val href = link.attr("href")
            if (href.startsWith("/")) "$mainUrl$href" else href
        }

        val firstSeasonEpisodes = parseEpisodesFromGrid(doc)
        episodes.addAll(firstSeasonEpisodes)

        for (seasonUrl in seasonLinks.drop(1)) {
            try {
                val seasonDoc = app.get(seasonUrl).document
                val seasonEpisodes = parseEpisodesFromGrid(seasonDoc)
                episodes.addAll(seasonEpisodes)
            } catch (_: Exception) {
            }
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = this@DiziBox.mainUrl.let { _ -> episodes.firstOrNull()?.posterUrl ?: posterUrl }
            this.plot = plot
            this.year = year
            this.tags = genres
            this.score = imdbScore?.let { Score.from10(it) }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document

        val sourceOptions = doc.select("select.woca-linkpages-dd option")
        if (sourceOptions.isEmpty()) return false

        val sourceUrls = mutableListOf<Pair<String, String>>()

        for (option in sourceOptions) {
            val sourceName = option.text().trim()
            val value = option.attr("value").trim()
            val href = option.attr("href").trim()

            if (sourceName.isEmpty()) continue

            val pageUrl = if (value.isNotEmpty()) {
                value
            } else if (href.isNotEmpty()) {
                href
            } else {
                data
            }

            val fullUrl = if (pageUrl.startsWith("/")) "$mainUrl$pageUrl" else pageUrl
            sourceUrls.add(sourceName to fullUrl)
        }

        var found = false

        for ((sourceName, pageUrl) in sourceUrls) {
            try {
                val sourceDoc = app.get(pageUrl).document

                val commentName = sourceDoc.html().let { html ->
                    Regex("<!--baslik:(.*?)-->").find(html)?.groupValues?.get(1)?.trim()
                } ?: sourceName

                val iframeSrc = sourceDoc.selectFirst("#video-area iframe")?.attr("src")
                if (iframeSrc.isNullOrBlank()) continue

                val resolvedUrl = resolvePlayerUrl(iframeSrc)

                if (loadExtractor(resolvedUrl, mainUrl, subtitleCallback) { link ->
                        runCatching {
                            callback(
                                ExtractorLink(
                                    link.source ?: "",
                                    "$commentName - ${link.name}",
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
                    }) {
                    found = true
                }
            } catch (_: Exception) {
            }
        }

        return found
    }

    private fun parseEpisodesFromGrid(doc: org.jsoup.nodes.Document): List<Episode> {
        return doc.select("#category-posts article.grid-box.grid-four").mapNotNull { article ->
            val link = article.selectFirst("a.season-episode") ?: return@mapNotNull null
            val href = link.attr("href")
            if (href.isBlank()) return@mapNotNull null

            val fullUrl = if (href.startsWith("/")) "$mainUrl$href" else href
            val linkText = link.text().trim()

            val match = Regex("(\\d+)\\.(?:Sezon|sezon)\\s+(\\d+)\\.(?:Bolum|bolum|Bölüm|bölüm)").find(linkText)
            val season = match?.groupValues?.get(1)?.toIntOrNull()
            val episode = match?.groupValues?.get(2)?.toIntOrNull()

            val title = article.selectFirst(".post-title a:not(.season-episode)")?.text()?.trim()
                ?: linkText

            newEpisode(fullUrl) {
                this.name = title
                this.season = season
                this.episode = episode
            }
        }
    }

    private fun resolvePlayerUrl(iframeSrc: String): String {
        if (iframeSrc.contains("haydi.php")) {
            val base64Param = Regex("v=([A-Za-z0-9+/=]+)").find(iframeSrc)?.groupValues?.get(1)
            if (base64Param != null) {
                try {
                    val decoded = String(Base64.decode(base64Param, Base64.DEFAULT))
                    if (decoded.startsWith("http")) return decoded
                } catch (_: Exception) {
                }
            }
        }
        return iframeSrc
    }

    data class AjaxSearchResult(
        @JsonProperty("permalink") val permalink: String?,
        @JsonProperty("post_title") val postTitle: String?,
        @JsonProperty("attachment_thumbnail") val attachmentThumbnail: String?,
        @JsonProperty("post_excerpt") val postExcerpt: String?
    )

    data class AjaxSearchResponse(
        @JsonProperty("searchTerms") val searchTerms: String?,
        @JsonProperty("results") val results: List<AjaxSearchResult>
    )
}
