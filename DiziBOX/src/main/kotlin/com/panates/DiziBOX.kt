package com.panates

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
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

        // If no dropdown, extract directly from current page
        val sourceUrls = mutableListOf<Pair<String, String>>()
        if (sourceOptions.isEmpty()) {
            sourceUrls.add("Varsayılan" to data)
        } else {
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
        }

        var found = false

        for ((sourceName, pageUrl) in sourceUrls) {
            try {
                val sourceDoc = if (pageUrl == data) doc else app.get(pageUrl).document

                // Source display name from HTML comment <!-- baslik:... -->
                val commentName = Regex("<!--baslik:(.*?)-->")
                    .find(sourceDoc.html())?.groupValues?.get(1)?.trim()
                    ?: sourceName

                val iframeSrc = sourceDoc.selectFirst("#video-area iframe")?.attr("src")
                    ?: sourceDoc.select("iframe[src]").firstOrNull {
                        val s = it.attr("src")
                        s.contains("player") || s.contains("king") || s.contains("moly") || s.contains("haydi") || s.contains("mecnun")
                    }?.attr("src")

                if (iframeSrc.isNullOrBlank()) continue

                val resolvedUrls = resolvePlayerUrl(iframeSrc, pageUrl)

                for (resolvedUrl in resolvedUrls) {
                    // 1. MOLYSTREAM HLS (DBX Pro / King backend)
                    if (resolvedUrl.contains("molystream.org")) {
                        val streamId = Regex("embed/(?:sheila/)?([A-Za-z0-9-]+)")
                            .find(resolvedUrl)?.groupValues?.get(1)
                        if (!streamId.isNullOrBlank()) {
                            val m3u8Url = "https://dbx.molystream.org/embed/sheila/$streamId"
                            callback(
                                ExtractorLink(
                                    source = "DBX Pro",
                                    name = "$commentName - DBX Pro 1080p",
                                    url = m3u8Url,
                                    referer = "https://dbx.molystream.org/",
                                    quality = Qualities.P1080.value,
                                    headers = mapOf(
                                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                                        "Referer" to "https://dbx.molystream.org/"
                                    ),
                                    type = ExtractorLinkType.M3U8
                                )
                            )
                            found = true
                            continue
                        }
                    }

                    // 2. VIDMOLY (Moly+ player)
                    if (resolvedUrl.contains("vidmoly")) {
                        var vmUrl = resolvedUrl
                        if (vmUrl.contains("vidmoly.net")) {
                            vmUrl = vmUrl.replace("vidmoly.net", "vidmoly.biz")
                        }

                        // Try built-in extractor first
                        val extracted = loadExtractor(vmUrl, "https://vidmoly.biz/", subtitleCallback) { link ->
                            callback(
                                ExtractorLink(
                                    link.source ?: "",
                                    "$commentName - ${link.name}",
                                    link.url ?: "",
                                    link.referer ?: "https://vidmoly.biz/",
                                    link.quality,
                                    link.headers ?: emptyMap(),
                                    link.extractorData,
                                    link.type,
                                    link.audioTracks ?: emptyList()
                                )
                            )
                        }

                        if (extracted) {
                            found = true
                            continue
                        }

                        // Fallback: scrape Vidmoly master.m3u8 directly from embed HTML
                        try {
                            val vmHtml = app.get(
                                vmUrl,
                                headers = mapOf("Referer" to "https://www.dizibox.live/")
                            ).text
                            val m3u8Direct = Regex("""['"](https?://[^'"]+\.m3u8[^'"]*)['"]""")
                                .find(vmHtml)?.groupValues?.get(1)
                            if (!m3u8Direct.isNullOrBlank()) {
                                callback(
                                    ExtractorLink(
                                        source = "Vidmoly",
                                        name = "$commentName - Vidmoly HLS",
                                        url = m3u8Direct,
                                        referer = "https://vidmoly.biz/",
                                        quality = Qualities.P1080.value,
                                        headers = mapOf("Referer" to "https://vidmoly.biz/"),
                                        type = ExtractorLinkType.M3U8
                                    )
                                )
                                found = true
                                continue
                            }
                        } catch (_: Exception) {}
                    }

                    // 3. Direct video file (.m3u8 or .mp4)
                    if (resolvedUrl.contains(".m3u8") || resolvedUrl.contains(".mp4")) {
                        val isM3u8 = resolvedUrl.contains(".m3u8")
                        callback(
                            ExtractorLink(
                                source = commentName,
                                name = "$commentName - Direct",
                                url = resolvedUrl,
                                referer = pageUrl,
                                quality = Qualities.P1080.value,
                                type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                            )
                        )
                        found = true
                        continue
                    }

                    // 4. General extractor fallback (OK.ru, etc.)
                    val refererUrl = when {
                        resolvedUrl.contains("ok.ru") -> "https://ok.ru/"
                        else -> pageUrl
                    }

                    if (loadExtractor(resolvedUrl, refererUrl, subtitleCallback) { link ->
                        runCatching {
                            callback(
                                ExtractorLink(
                                    link.source ?: "",
                                    "$commentName - ${link.name}",
                                    link.url ?: "",
                                    link.referer ?: refererUrl,
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
     * Resolve player iframe src to actual video/embed URLs.
     */
    private suspend fun resolvePlayerUrl(iframeSrc: String, refererPage: String): List<String> {
        // Odnok: base64 decode v= param -> ensure https
        if (iframeSrc.contains("haydi.php")) {
            val base64Param = Regex("v=([A-Za-z0-9+/=]+)").find(iframeSrc)?.groupValues?.get(1)
            if (base64Param != null) {
                try {
                    val decoded = String(Base64.decode(base64Param, Base64.DEFAULT))
                    if (decoded.startsWith("http")) {
                        val secureUrl = decoded.replace("http://", "https://")
                        return listOf(secureUrl)
                    }
                } catch (_: Exception) {}
            }
        }

        // DBX Pro (king.php), Moly+ (moly.php), or King (mecnun.php): fetch player page with auth cookies
        if (iframeSrc.contains("king.php") || iframeSrc.contains("moly.php") || iframeSrc.contains("mecnun.php")) {
            try {
                val playerDoc = app.get(
                    iframeSrc,
                    headers = mapOf(
                        "Cookie" to "isTrustedUser=true; LockUser=true",
                        "Referer" to refererPage
                    )
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

                // Check for inline JWPlayer/HLS file in scripts
                val scripts = playerDoc.select("script").map { it.html() }
                for (script in scripts) {
                    val fileMatch = Regex("""['"]file['"]\s*:\s*['"](https?://[^'"]+)['"]""").find(script)
                        ?: Regex("""file\s*:\s*['"](https?://[^'"]+)['"]""").find(script)
                    val foundUrl = fileMatch?.groupValues?.get(1)
                    if (!foundUrl.isNullOrBlank()) {
                        return listOf(foundUrl)
                    }
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
