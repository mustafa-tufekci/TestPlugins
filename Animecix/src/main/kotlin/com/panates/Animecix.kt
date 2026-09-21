package com.panates

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class Animecix : MainAPI() {
    override var mainUrl = "https://animecix.tv"
    override var name = "Animecix"
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Anime)
    override var lang = "tr"
    override val hasMainPage = true

    companion object {
        private const val TAU_VIDEO_URL = "https://tau-video.xyz"

        private val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Accept" to "application/json, text/plain, */*",
            "Accept-Language" to "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
            "Sec-Ch-Ua" to "\"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            "Sec-Ch-Ua-Mobile" to "?0",
            "Sec-Ch-Ua-Platform" to "\"Windows\"",
            "Sec-Fetch-Dest" to "empty",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Site" to "same-origin"
        )

        private val tauHeaders = mapOf(
            "User-Agent" to defaultHeaders["User-Agent"]!!,
            "Referer" to "$TAU_VIDEO_URL/",
            "Origin" to TAU_VIDEO_URL
        )
    }

    // ── Search ──────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        val slug = query.trim().replace(Regex("\\s+"), "-")
            .replace(Regex("[^\\w-]"), "")

        val url = "$mainUrl/secure/search/${java.net.URLEncoder.encode(slug, "UTF-8")}"

        return try {
            val response = app.get(
                url,
                params = mapOf("type" to "", "limit" to "20"),
                headers = defaultHeaders
            ).parsedSafe<SearchApiResponse>()

            response?.results?.mapNotNull { result ->
                val id = result.id ?: return@mapNotNull null
                val title = result.name ?: return@mapNotNull null
                val posterUrl = result.poster?.let { fixUrl(it) }
                val type = result.titleType ?: ""

                if (type.contains("movie", ignoreCase = true)) {
                    newMovieSearchResponse(title, "$mainUrl/anime/$id", TvType.Movie) {
                        this.posterUrl = posterUrl
                        this.year = result.year
                    }
                } else {
                    newTvSeriesSearchResponse(title, "$mainUrl/anime/$id", TvType.TvSeries) {
                        this.posterUrl = posterUrl
                        this.year = result.year
                    }
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Main Page ───────────────────────────────────────────────────────

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val allPages = mutableListOf<HomePageList>()

        // Try the browse page which has sections
        try {
            val doc = app.get("$mainUrl/browse", headers = defaultHeaders).document

            // Popüler Animeler
            val popularSection = doc.selectFirst(".section-popular, .popular-section, section:has(h2)")
            if (popularSection != null) {
                val items = popularSection.select("a[href*='/anime/'], a[href*='/title/']").mapNotNull { link ->
                    val href = link.attr("href")
                    if (href.isBlank()) return@mapNotNull null
                    val fullUrl = if (href.startsWith("http")) href else "$mainUrl$href"
                    val title = link.selectFirst(".card-title, .title, h3, h4")?.text()?.trim()
                        ?: link.attr("title").trim().ifEmpty { return@mapNotNull null }
                    val posterUrl = link.selectFirst("img")?.let { img ->
                        (img.attr("data-src").ifEmpty { null } ?: img.attr("src")).let { src ->
                            if (src.startsWith("http")) src else "$mainUrl$src"
                        }
                    }
                    newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                }.distinctBy { it.url }
                if (items.isNotEmpty()) allPages.add(HomePageList("Popüler Animeler", items))
            }

            // Recent releases from general card grid
            val cards = doc.select(".card, .anime-card, .item, a.column[title]")
            if (cards.isNotEmpty() && allPages.isEmpty()) {
                val items = cards.mapNotNull { card ->
                    val link = if (card.tagName() == "a") card else card.selectFirst("a[href]")
                    val href = link?.attr("href") ?: return@mapNotNull null
                    if (href.isBlank()) return@mapNotNull null
                    val fullUrl = if (href.startsWith("http")) href else "$mainUrl$href"
                    val title = card.selectFirst(".card-title, .title, .description, h3")?.text()?.trim()
                        ?: link.attr("title").trim().ifEmpty { return@mapNotNull null }
                    val posterUrl = card.selectFirst("img")?.let { img ->
                        (img.attr("data-src").ifEmpty { null } ?: img.attr("src")).let { src ->
                            if (src.startsWith("http")) src else "$mainUrl$src"
                        }
                    }
                    newTvSeriesSearchResponse(title, fullUrl, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                }.distinctBy { it.url }.take(30)
                if (items.isNotEmpty()) allPages.add(HomePageList("Son Eklenenler", items))
            }
        } catch (_: Exception) {}

        // If main page didn't yield much, try fetching popular via search API with empty-ish query
        if (allPages.isEmpty()) {
            try {
                val response = app.get(
                    "$mainUrl/secure/search/a",
                    params = mapOf("type" to "", "limit" to "30"),
                    headers = defaultHeaders
                ).parsedSafe<SearchApiResponse>()

                val items = response?.results?.mapNotNull { result ->
                    val id = result.id ?: return@mapNotNull null
                    val title = result.name ?: return@mapNotNull null
                    val posterUrl = result.poster?.let { fixUrl(it) }
                    newTvSeriesSearchResponse(title, "$mainUrl/anime/$id", TvType.TvSeries) {
                        this.posterUrl = posterUrl
                        this.year = result.year
                    }
                }?.distinctBy { it.url } ?: emptyList()

                if (items.isNotEmpty()) allPages.add(HomePageList("Animeler", items))
            } catch (_: Exception) {}
        }

        return newHomePageResponse(allPages)
    }

    // ── Load Details ────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        // Extract title ID from URL
        val titleId = extractTitleId(url)
            ?: throw ErrorLoadingException("Unable to extract title ID from '$url'")

        val data = app.get(
            "$mainUrl/secure/titles/$titleId",
            params = mapOf("titleId" to titleId),
            headers = defaultHeaders
        ).parsedSafe<TitleApiResponse>()

        val title = data?.title ?: throw ErrorLoadingException("No title data found")

        val name = title.name ?: "Unknown"
        val posterUrl = title.poster?.let { fixUrl(it) }
        val plot = title.description
        val year = title.year
        val genres = title.genres?.mapNotNull { it.name } ?: emptyList()

        // Determine content type
        val titleType = (title.type ?: title.titleType ?: "").lowercase()
        val isMovie = titleType.contains("movie") || titleType.contains("film")

        val episodes = mutableListOf<Episode>()

        if (isMovie) {
            // Movie: single episode
            episodes.add(newEpisode("$mainUrl/movie/$titleId") {
                this.name = name
                this.season = 1
                this.episode = 1
            })
        } else {
            // Series: fetch episodes from seasons
            val seasons = title.seasons ?: emptyList()

            if (seasons.isNotEmpty()) {
                for (season in seasons) {
                    val seasonNumber = season.number ?: continue
                    val episodeList = season.episodePagination?.data ?: emptyList()

                    for (ep in episodeList) {
                        val epNumber = ep.episodeNumber ?: continue
                        val epName = ep.name ?: "Bölüm $epNumber"
                        val epId = ep.id ?: continue

                        // Store composite data for loadLinks
                        val epData = mapOf(
                            "id" to titleId,
                            "season" to seasonNumber.toString(),
                            "episode" to epNumber.toString()
                        )
                        val dataStr = org.json.JSONObject(epData).toString()

                        episodes.add(newEpisode(dataStr) {
                            this.name = epName
                            this.season = seasonNumber
                            this.episode = epNumber
                            this.posterUrl = posterUrl
                        })
                    }
                }
            }
        }

        return if (isMovie) {
            newMovieLoadResponse(name, url, TvType.Movie, episodes) {
                this.posterUrl = posterUrl
                this.plot = plot
                this.year = year
                this.tags = genres
            }
        } else {
            newTvSeriesLoadResponse(name, url, TvType.TvSeries, episodes) {
                this.posterUrl = posterUrl
                this.plot = plot
                this.year = year
                this.tags = genres
            }
        }
    }

    // ── Load Links ──────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // Parse episode data
        val episodeData = try {
            org.json.JSONObject(data)
        } catch (_: Exception) {
            return false
        }

        val titleId = episodeData.optString("id", null) ?: return false
        val season = episodeData.optInt("season", 1)
        val episode = episodeData.optInt("episode", 1)

        // Fetch video sources for this episode
        val videoResponse = try {
            app.get(
                "$mainUrl/secure/episode-videos",
                params = mapOf(
                    "titleId" to titleId,
                    "episode" to episode.toString(),
                    "season" to season.toString()
                ),
                headers = defaultHeaders
            ).parsedSafe<EpisodeVideosResponse>()
        } catch (_: Exception) {
            null
        }

        val videos = videoResponse?.videos
        if (videos.isNullOrEmpty()) return false

        var found = false

        for (video in videos) {
            val videoUrl = video.url ?: continue
            val extra = video.extra // subtitle label or null

            try {
                val resolvedUrls = resolveVideoUrl(videoUrl)

                for (resolvedUrl in resolvedUrls) {
                    if (loadExtractor(resolvedUrl, mainUrl, subtitleCallback) { link ->
                        runCatching {
                            val linkName = if (!extra.isNullOrBlank()) {
                                "$extra - ${link.name}"
                            } else {
                                link.name
                            }
                            callback(
                                ExtractorLink(
                                    link.source ?: "",
                                    linkName,
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

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun extractTitleId(url: String): String? {
        // Try /anime/{id} or /title/{id} patterns
        val match = Regex("""/(?:anime|title)/(\d+)""").find(url)
        if (match != null) return match.groupValues[1]

        // Try plain numeric ID at end
        val numMatch = Regex("""/(\d+)(?:\?|$)""").find(url)
        return numMatch?.groupValues?.get(1)
    }

    private suspend fun resolveVideoUrl(embedUrl: String): List<String> {
        // If it's a tau-video embed, resolve via API
        if (embedUrl.contains("tau-video.xyz")) {
            val embedId = extractTauVideoId(embedUrl)
            if (embedId != null) {
                return resolveTauVideo(embedId)
            }
        }

        // If it's a direct mp4/m3u8 URL, return as-is
        if (embedUrl.contains(".mp4") || embedUrl.contains(".m3u8")) {
            return listOf(embedUrl)
        }

        // Try fetching the embed page to find iframe or video source
        try {
            val doc = app.get(embedUrl, headers = defaultHeaders).document

            // Check for nested iframe
            val iframeSrc = doc.selectFirst("iframe[src]")?.attr("src")
            if (!iframeSrc.isNullOrBlank()) {
                val fullSrc = if (iframeSrc.startsWith("//")) "https:$iframeSrc" else iframeSrc
                return resolveVideoUrl(fullSrc)
            }

            // Check for video source
            val videoSrc = doc.selectFirst("video source[src]")?.attr("src")
            if (!videoSrc.isNullOrBlank()) {
                val fullSrc = if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc
                return listOf(fullSrc)
            }

            // Check for direct video tag
            val videoUrl = doc.selectFirst("video[src]")?.attr("src")
            if (!videoUrl.isNullOrBlank()) {
                val fullUrl = if (videoUrl.startsWith("//")) "https:$videoUrl" else videoUrl
                return listOf(fullUrl)
            }
        } catch (_: Exception) {}

        return listOf(embedUrl)
    }

    private fun extractTauVideoId(url: String): String? {
        // Match tau-video.xyz/embed/{id} or similar patterns
        val match = Regex("""tau-video\.xyz/(?:embed/)?([A-Za-z0-9]+)""").find(url)
        return match?.groupValues?.get(1)
    }

    private suspend fun resolveTauVideo(embedId: String): List<String> {
        return try {
            val response = app.get(
                "$TAU_VIDEO_URL/api/video/$embedId",
                headers = tauHeaders
            ).parsedSafe<TauVideoResponse>()

            response?.urls?.mapNotNull { it.url } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Data Classes ────────────────────────────────────────────────────

    data class SearchResult(
        @JsonProperty("id") val id: Int?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("name_english") val nameEnglish: String?,
        @JsonProperty("name_romanji") val nameRomanji: String?,
        @JsonProperty("poster") val poster: String?,
        @JsonProperty("year") val year: Int?,
        @JsonProperty("title_type") val titleType: String?,
        @JsonProperty("tmdb_id") val tmdbId: Any?
    )

    data class SearchApiResponse(
        @JsonProperty("results") val results: List<SearchResult>?
    )

    // Title details
    data class TitleDetails(
        @JsonProperty("id") val id: Int?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("name_english") val nameEnglish: String?,
        @JsonProperty("description") val description: String?,
        @JsonProperty("poster") val poster: String?,
        @JsonProperty("year") val year: Int?,
        @JsonProperty("type") val type: String?,
        @JsonProperty("title_type") val titleType: String?,
        @JsonProperty("genres") val genres: List<Genre>?,
        @JsonProperty("seasons") val seasons: List<Season>?,
        @JsonProperty("season_count") val seasonCount: Int?,
        @JsonProperty("videos") val videos: List<MovieVideo>?
    )

    data class Genre(
        @JsonProperty("id") val id: Int?,
        @JsonProperty("name") val name: String?
    )

    data class Season(
        @JsonProperty("number") val number: Int?,
        @JsonProperty("episodePagination") val episodePagination: EpisodePagination?
    )

    data class EpisodePagination(
        @JsonProperty("data") val data: List<EpisodeItem>?
    )

    data class EpisodeItem(
        @JsonProperty("id") val id: Int?,
        @JsonProperty("episode_number") val episodeNumber: Int?,
        @JsonProperty("name") val name: String?
    )

    data class MovieVideo(
        @JsonProperty("url") val url: String?
    )

    data class TitleApiResponse(
        @JsonProperty("title") val title: TitleDetails?
    )

    // Episode videos
    data class VideoSource(
        @JsonProperty("id") val id: Any?,
        @JsonProperty("url") val url: String?,
        @JsonProperty("extra") val extra: String?
    )

    data class EpisodeVideosResponse(
        @JsonProperty("videos") val videos: List<VideoSource>?
    )

    // Tau video
    data class TauVideoUrl(
        @JsonProperty("url") val url: String?,
        @JsonProperty("label") val label: String?,
        @JsonProperty("size") val size: Long?
    )

    data class TauVideoResponse(
        @JsonProperty("urls") val urls: List<TauVideoUrl>?
    )
}
