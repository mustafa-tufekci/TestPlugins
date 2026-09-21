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

    // ── Main Page ────────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val allPages = mutableListOf<HomePageList>()

        val doc = try {
            app.get(mainUrl).document
        } catch (_: Exception) {
            return newHomePageResponse(emptyList())
        }

        // Beklenen / Eklenen Diziler — #recommended_series
        try {
            val items = doc.select("#recommended_series li").mapNotNull { li ->
                val link = li.selectFirst("a") ?: return@mapNotNull null
                val href = link.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = fixUrl(href)
                val title = link.selectFirst("span.baslik")?.text()?.trim()
                    ?: link.attr("title").substringBefore(" ").trim()
                if (title.isBlank()) return@mapNotNull null
                val posterUrl = link.selectFirst("img")?.attr("data-src")?.let { fixUrl(it) }
                newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Beklenen / Eklenen Diziler", items))
        } catch (_: Exception) {}

        // Efsane Diziler — dedicated page
        try {
            val efsaneDoc = app.get("$mainUrl/efsane-diziler/").document
            val items = efsaneDoc.select("article.article-series-poster").mapNotNull { article ->
                val titleLink = article.selectFirst("a.poster-title") ?: return@mapNotNull null
                val href = titleLink.attr("href")
                if (href.isBlank()) return@mapNotNull null
                val fullUrl = fixUrl(href)
                val title = titleLink.text().trim()
                if (title.isBlank()) return@mapNotNull null
                val imdbRaw = article.selectFirst("div.imdb")?.text()?.replace(Regex("[^0-9.]"), "")?.trim()
                val year = article.selectFirst("div.release")?.text()?.replace(Regex("[^0-9]"), "")?.trim()?.toIntOrNull()
                val posterUrl = article.selectFirst("img.afis")?.attr("data-src")?.let { fixUrl(it) }
                newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                    this.year = year
                    if (!imdbRaw.isNullOrBlank()) this.score = Score.from10(imdbRaw)
                }
            }.distinctBy { it.url }
            if (items.isNotEmpty()) allPages.add(HomePageList("Efsane Diziler", items))
        } catch (_: Exception) {}

        return newHomePageResponse(allPages)
    }

    // ── Search ───────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        // Try AJAX search first (Dave's WordPress Live Search)
        try {
            val ajaxResults = app.get(
                "$mainUrl/wp-admin/admin-ajax.php",
                params = mapOf("action" to "dwls_search", "s" to query)
            ).parsedSafe<AjaxSearchResponse>()

            if (ajaxResults != null && ajaxResults.results.isNotEmpty()) {
                return ajaxResults.results.mapNotNull { result ->
                    val href = result.permalink ?: return@mapNotNull null
                    val title = result.postTitle ?: return@mapNotNull null
                    if (href.isBlank() || title.isBlank()) return@mapNotNull null
                    newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                        this.posterUrl = result.attachmentThumbnail
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback: standard ?s= search
        return try {
            val doc = app.get("$mainUrl/?s=$query").document
            doc.select("article.detailed-article").mapNotNull { article ->
                val titleEl = article.selectFirst("h3 a") ?: return@mapNotNull null
                val href = titleEl.attr("href")
                val title = titleEl.text().trim()
                if (href.isBlank() || title.isBlank()) return@mapNotNull null
                val posterUrl = article.selectFirst("figure img")?.attr("src")?.let { fixUrl(it) }
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst(".tv-overview h1, h1.cat-title")?.text()?.trim()
            ?: doc.title().substringBefore(" - DiziBOX").trim().ifEmpty { "Unknown" }

        val posterUrl = doc.selectFirst(".tv-overview img, .category-cover img")?.attr("src")?.let { fixUrl(it) }

        val plot = doc.selectFirst(".tv-overview .text-muted-darker, .tv-overview p")?.text()?.trim()

        val genres = doc.select(".tv-overview .custom-field a, .custom-fields-container .custom-field a")
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }

        // IMDB: "imdb: <b>9.1</b>" — extract only the numeric value from <b>
        val imdbScore = doc.selectFirst("span.label-imdb b, .label-imdb b")?.text()
            ?.replace(Regex("[^0-9.]"), "")
            ?.toDoubleOrNull()
            ?: doc.selectFirst("span.label-imdb, .label-imdb")?.text()
                ?.replace(Regex("[^0-9.]"), "")
                ?.toDoubleOrNull()

        val yearText = doc.selectFirst(".custom-field:has(i.icon-globe)")?.text()
        val year = yearText?.replace(Regex("[^0-9]"), "")?.take(4)?.toIntOrNull()

        val episodes = mutableListOf<Episode>()

        // Season tabs — first season is on the current page, rest need separate fetches
        val seasonLinks = doc.select("#seasons-list a.btn").map { link ->
            fixUrl(link.attr("href"))
        }

        val firstSeasonEpisodes = parseEpisodesFromGrid(doc)
        episodes.addAll(firstSeasonEpisodes)

        for (seasonUrl in seasonLinks.drop(1)) {
            try {
                val seasonDoc = app.get(seasonUrl).document
                val seasonEpisodes = parseEpisodesFromGrid(seasonDoc)
                episodes.addAll(seasonEpisodes)
            } catch (_: Exception) {}
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = posterUrl
            this.plot = plot
            this.year = year
            this.tags = genres
            this.score = imdbScore?.let { Score.from10(it) }
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

        val sourceOptions = doc.select("select.woca-linkpages-dd option")

        // Fallback: if no dropdown, try a direct #video-area iframe on this page
        if (sourceOptions.isEmpty()) {
            val iframeSrc = doc.selectFirst("#video-area iframe")?.attr("src")
            if (!iframeSrc.isNullOrBlank()) {
                var found = false
                for (resolved in resolvePlayerUrl(iframeSrc)) {
                    if (loadExtractor(resolved, mainUrl, subtitleCallback, callback)) found = true
                }
                return found
            }
            return false
        }

        val sourceUrls = mutableListOf<Pair<String, String>>()

        for (option in sourceOptions) {
            val sourceName = option.text().trim()
            val value = option.attr("value").trim()
            val href = option.attr("href").trim()

            if (sourceName.isEmpty()) continue

            val pageUrl = when {
                value.isNotEmpty() -> value
                href.isNotEmpty() -> href
                else -> data
            }

            sourceUrls.add(sourceName to fixUrl(pageUrl))
        }

        var found = false

        for ((sourceName, pageUrl) in sourceUrls) {
            try {
                val sourceDoc = app.get(pageUrl).document

                // Source display name from HTML comment <!-- baslik:... -->
                val commentName = Regex("<!--baslik:(.*?)-->")
                    .find(sourceDoc.html())?.groupValues?.get(1)?.trim()
                    ?: sourceName

                val iframeSrc = sourceDoc.selectFirst("#video-area iframe")?.attr("src")
                if (iframeSrc.isNullOrBlank()) continue

                for (resolvedUrl in resolvePlayerUrl(iframeSrc)) {
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
                }
            } catch (_: Exception) {}
        }

        return found
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Parse episodes from a season/series page grid.
     * Episode link text format confirmed: "1.Sezon 1.Bölüm" (mixed case, Turkish chars)
     */
    private fun parseEpisodesFromGrid(doc: org.jsoup.nodes.Document): List<Episode> {
        return doc.select("#category-posts article.grid-box.grid-four").mapNotNull { article ->
            val link = article.selectFirst("a.season-episode") ?: return@mapNotNull null
            val href = link.attr("href")
            if (href.isBlank()) return@mapNotNull null

            val fullUrl = fixUrl(href)
            val linkText = link.text().trim()

            // Regex covers: "1.Sezon 1.Bölüm", "1.sezon 1.bolum", "1.Sezon 1.Bolüm" etc.
            val match = Regex(
                "(\\d+)\\s*\\.\\s*[Ss]ezon\\s+(\\d+)\\s*\\.\\s*[Bb][oö]l[uü]m",
                RegexOption.IGNORE_CASE
            ).find(linkText)
            val season = match?.groupValues?.get(1)?.toIntOrNull()
            val episode = match?.groupValues?.get(2)?.toIntOrNull()

            val epTitle = article.selectFirst(".post-title a:not(.season-episode)")?.text()?.trim()
                ?: linkText

            newEpisode(fullUrl) {
                this.name = epTitle
                this.season = season
                this.episode = episode
            }
        }
    }

    /**
     * Resolve player iframe src to an actual video URL.
     */
    private suspend fun resolvePlayerUrl(iframeSrc: String): List<String> {
        // Odnok: base64 decode v= param
        if (iframeSrc.contains("haydi.php")) {
            val base64Param = Regex("v=([A-Za-z0-9+/=]+)").find(iframeSrc)?.groupValues?.get(1)
            if (base64Param != null) {
                try {
                    val decoded = String(Base64.decode(base64Param, Base64.DEFAULT))
                    if (decoded.startsWith("http")) return listOf(decoded)
                } catch (_: Exception) {}
            }
        }

        // DBX Pro (king.php) or Moly+ (moly.php): fetch player with cookies
        if (iframeSrc.contains("king.php") || iframeSrc.contains("moly.php")) {
            try {
                val playerDoc = app.get(
                    iframeSrc,
                    headers = mapOf("Cookie" to "isTrustedUser=true; LockUser=true")
                ).document

                val realIframes = playerDoc.select("iframe[src]")
                    .map { it.attr("src") }
                    .filter { it.isNotBlank() }
                    .map { if (it.startsWith("//")) "https:$it" else it }
                if (realIframes.isNotEmpty()) return realIframes

                val videoSrc = playerDoc.selectFirst("video source[src]")?.attr("src")
                if (!videoSrc.isNullOrBlank()) {
                    return listOf(if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc)
                }

                val videoUrl = playerDoc.selectFirst("video[src]")?.attr("src")
                if (!videoUrl.isNullOrBlank()) {
                    return listOf(if (videoUrl.startsWith("//")) "https:$videoUrl" else videoUrl)
                }
            } catch (_: Exception) {}
        }

        return listOf(iframeSrc)
    }

    // ── Data Classes ─────────────────────────────────────────────────────

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
