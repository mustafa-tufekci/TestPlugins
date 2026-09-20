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

        // Efsane Diziler (from dedicated page)
        try {
            val efsaneDoc = app.get("$mainUrl/efsane-diziler/").document
            val items = efsaneDoc.select("article.article-series-poster").mapNotNull { article ->
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
            if (items.isNotEmpty()) allPages.add(HomePageList("Efsane Diziler", items))
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

                val resolvedUrls = resolvePlayerUrl(iframeSrc)

                for (resolvedUrl in resolvedUrls) {
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

    private suspend fun resolvePlayerUrl(iframeSrc: String): List<String> {
        // Odnok: base64 decode
        if (iframeSrc.contains("haydi.php")) {
            val base64Param = Regex("v=([A-Za-z0-9+/=]+)").find(iframeSrc)?.groupValues?.get(1)
            if (base64Param != null) {
                try {
                    val decoded = String(Base64.decode(base64Param, Base64.DEFAULT))
                    if (decoded.startsWith("http")) return listOf(decoded)
                } catch (_: Exception) {
                }
            }
        }

        // DBX Pro (king.php) or Moly+ (moly.php): fetch the player page to get the real video iframe
        if (iframeSrc.contains("king.php") || iframeSrc.contains("moly.php")) {
            try {
                val playerDoc = app.get(iframeSrc).document
                val realIframes = playerDoc.select("iframe[src]").map { it.attr("src") }
                    .filter { it.isNotBlank() }
                    .map { src -> if (src.startsWith("//")) "https:$src" else src }
                if (realIframes.isNotEmpty()) return realIframes

                // Try embedded video sources
                val videoSrc = playerDoc.selectFirst("video source[src]")?.attr("src")
                if (!videoSrc.isNullOrBlank()) {
                    return listOf(if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc)
                }

                // Try direct video tag
                val videoUrl = playerDoc.selectFirst("video[src]")?.attr("src")
                if (!videoUrl.isNullOrBlank()) {
                    return listOf(if (videoUrl.startsWith("//")) "https:$videoUrl" else videoUrl)
                }
            } catch (_: Exception) {
            }
        }

        return listOf(iframeSrc)
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
