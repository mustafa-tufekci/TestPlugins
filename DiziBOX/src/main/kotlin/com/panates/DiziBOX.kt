package com.panates

import android.util.Base64
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

        // Önerilen Diziler - Latest shows from archive
        val latestDoc = try {
            app.get("$mainUrl/dizi-arsivi/page/$page/").document
        } catch (_: Exception) {
            null
        }
        if (latestDoc != null) {
            val latestShows = parseDetailedArticles(latestDoc)
            if (latestShows.isNotEmpty()) {
                allPages.add(HomePageList("Önerilen Diziler", latestShows))
            }
        }

        // Efsane Diziler - IMDb 7+ shows
        val imdbDoc = try {
            app.get("$mainUrl/arsiv/?&imdb=7").document
        } catch (_: Exception) {
            null
        }
        if (imdbDoc != null) {
            val imdbShows = parseDetailedArticles(imdbDoc)
            if (imdbShows.isNotEmpty()) {
                allPages.add(HomePageList("Efsane Diziler", imdbShows))
            }
        }

        // Takvim - Shows airing this week from calendar
        val calendarDoc = try {
            app.get("$mainUrl/dizi-takvimi/").document
        } catch (_: Exception) {
            null
        }
        if (calendarDoc != null) {
            val calendarShows = calendarDoc.select("table.tv-calendar a.button-link").mapNotNull { link ->
                val href = link.attr("href")
                val title = link.text()
                if (href.isBlank() || title.isBlank()) return@mapNotNull null
                val posterUrl = extractPosterUrl(href)
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = posterUrl
                }
            }.distinctBy { it.url }
            if (calendarShows.isNotEmpty()) {
                allPages.add(HomePageList("Bu Hafta", calendarShows))
            }
        }

        return newHomePageResponse(allPages)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            val doc = app.get("$mainUrl/?s=$query").document
            parseDetailedArticles(doc)
        } catch (_: Exception) {
            emptyList()
        }
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

        // Collect season links
        val seasonLinks = doc.select("#seasons-list a.btn").map { link ->
            val href = link.attr("href")
            if (href.startsWith("/")) "$mainUrl$href" else href
        }

        // Parse episodes from first season page (show page already has one season's episodes)
        val firstSeasonEpisodes = parseEpisodesFromGrid(doc)
        episodes.addAll(firstSeasonEpisodes)

        // Fetch remaining seasons
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

        // Build list of source page URLs
        val sourceUrls = mutableListOf<Pair<String, String>>() // (sourceName, pageUrl)

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
                data // Current page (no suffix)
            }

            val fullUrl = if (pageUrl.startsWith("/")) "$mainUrl$pageUrl" else pageUrl
            sourceUrls.add(sourceName to fullUrl)
        }

        var found = false

        for ((sourceName, pageUrl) in sourceUrls) {
            try {
                val sourceDoc = app.get(pageUrl).document

                // Extract source name from HTML comment <!--baslik:...-->
                val commentName = sourceDoc.html().let { html ->
                    Regex("<!--baslik:(.*?)-->").find(html)?.groupValues?.get(1)?.trim()
                } ?: sourceName

                // Extract iframe src
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

    private fun parseDetailedArticles(doc: org.jsoup.nodes.Document): List<SearchResponse> {
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
        // Handle Odnok proxy: haydi.php?v={base64} decodes to ok.ru URL
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
        // For king.php and moly.php, pass through to loadExtractor as-is
        return iframeSrc
    }

    private fun extractPosterUrl(showPageUrl: String): String? {
        val slug = Regex("/diziler/([^/]+)/").find(showPageUrl)?.groupValues?.get(1)
            ?: return null
        return "$mainUrl/wp-content/uploads/afisler/${slug}-200x290.jpg"
    }
}
