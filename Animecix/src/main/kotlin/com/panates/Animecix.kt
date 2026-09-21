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
        private const val API_URL = "https://mangacix.net"
        private const val TAU_VIDEO_URL = "https://tau-video.xyz"

        // Minimal headers matching reference implementations
        private val defaultHeaders = mapOf(
            "Accept" to "application/json",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
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

        // Use search API to populate homepage categories
        val categories = listOf(
            "naruto" to "Anime",
            "one piece" to "Anime",
            "attack on titan" to "Anime",
            "demon slayer" to "Anime",
            "jujutsu kaisen" to "Anime"
        )

        for ((query, label) in categories) {
            try {
                val slug = query.replace(Regex("\\s+"), "-")
                val url = "$mainUrl/secure/search/${java.net.URLEncoder.encode(slug, "UTF-8")}"
                val response = app.get(
                    url,
                    params = mapOf("type" to "", "limit" to "10"),
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

                if (items.isNotEmpty()) allPages.add(HomePageList(label, items))
            } catch (_: Exception) {}
        }

        // Fallback: single broad search if categories failed
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
        // Extract title ID from URL (format: /anime/{id})
        val titleId = extractTitleId(url)
            ?: throw ErrorLoadingException("Unable to extract title ID from '$url'")

        // Fetch title metadata
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
            val epData = org.json.JSONObject(mapOf(
                "id" to titleId,
                "season" to "1",
                "episode" to "1"
            )).toString()

            episodes.add(newEpisode(epData) {
                this.name = name
                this.season = 1
                this.episode = 1
            })
        } else {
            // Series: fetch episodes from mangacix.net related-videos API
            // First get season count
            val seasonCount = title.seasonCount
                ?: title.seasons?.size
                ?: 1

            for (s in 1..seasonCount) {
                try {
                    val seasonEpisodes = fetchSeasonEpisodes(titleId, s)
                    episodes.addAll(seasonEpisodes)
                } catch (_: Exception) {}
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

    /**
     * Fetch episodes for a given season using the mangacix.net related-videos API.
     * Reference: https://github.com/fmustafayaman/turkish-nuvio/blob/main/src/animecix/episodes.js
     */
    private suspend fun fetchSeasonEpisodes(titleId: String, seasonNum: Int): List<Episode> {
        val response = app.get(
            "$API_URL/secure/related-videos",
            params = mapOf(
                "episode" to "1",
                "season" to seasonNum.toString(),
                "titleId" to titleId,
                "videoId" to "637113"
            ),
            headers = defaultHeaders
        ).parsedSafe<RelatedVideosResponse>()

        val videos = response?.videos ?: return emptyList()

        return videos.mapNotNull { video ->
            val videoUrl = video.url ?: return@mapNotNull null
            val videoName = video.name ?: return@mapNotNull null
            val epNum = video.episodeNum ?: return@mapNotNull null
            val sNum = video.seasonNum ?: seasonNum

            // Store titleId + season + episode for loadLinks
            val epData = org.json.JSONObject(mapOf(
                "id" to titleId,
                "season" to sNum.toString(),
                "episode" to epNum.toString()
            )).toString()

            newEpisode(epData) {
                this.name = videoName
                this.season = sNum
                this.episode = epNum
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

        // Fetch video sources using episode-videos-points endpoint
        val videoResponse = try {
            app.get(
                "$mainUrl/secure/episode-videos-points",
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
            val extra = video.extra

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
        @JsonProperty("url") val url: String?,
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

    // Related videos (mangacix.net)
    data class RelatedVideo(
        @JsonProperty("id") val id: Any?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("url") val url: String?,
        @JsonProperty("episode_num") val episodeNum: Int?,
        @JsonProperty("season_num") val seasonNum: Int?,
        @JsonProperty("extra") val extra: String?
    )

    data class RelatedVideosResponse(
        @JsonProperty("videos") val videos: List<RelatedVideo>?
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
