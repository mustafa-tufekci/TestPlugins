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
        val slug = query.trim()
            .replace(Regex("\\s+"), "-")
            .lowercase()

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
                val type = result.titleType ?: result.type ?: ""

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

        val searchQueries = listOf(
            "naruto" to "Naruto",
            "one-piece" to "One Piece",
            "attack-on-titan" to "Attack on Titan",
            "demon-slayer" to "Demon Slayer",
            "jujutsu-kaisen" to "Jujutsu Kaisen"
        )

        for ((slug, label) in searchQueries) {
            try {
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

        // Fallback: broad search
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
        val titleId = extractTitleId(url)
            ?: throw ErrorLoadingException("Unable to extract title ID from '$url'")

        // IMPORTANT: /secure/titles/{id} ignores the path ID entirely.
        // The API only reads the `titleId` query parameter.
        val data = app.get(
            "$mainUrl/secure/titles",
            params = mapOf("titleId" to titleId),
            headers = defaultHeaders
        ).parsedSafe<TitleApiResponse>()

        val title = data?.title ?: throw ErrorLoadingException("No title data found for titleId=$titleId")

        val name = title.name ?: "Unknown"
        val posterUrl = title.poster?.let { fixUrl(it) }
        val plot = title.description
        val year = title.year
        val genres = title.genres?.mapNotNull { it.displayName ?: it.name } ?: emptyList()

        val titleType = (title.type ?: title.titleType ?: "").lowercase()
        val isMovie = titleType.contains("movie") || titleType.contains("film")

        val seasonCount = title.seasonCount ?: title.seasons?.size ?: 1

        val episodes = mutableListOf<Episode>()

        if (isMovie) {
            val epData = buildEpData(titleId, 1, 1)
            episodes.add(newEpisode(epData) {
                this.name = name
                this.season = 1
                this.episode = 1
                this.posterUrl = posterUrl
            })
        } else {
            // Step 1: collect episode metadata (name, poster, description) via episodeList
            // from the first episode of each season
            val episodeMeta = mutableMapOf<Pair<Int, Int>, EpisodeItem>()
            for (s in 1..seasonCount) {
                try {
                    val epResponse = app.get(
                        "$mainUrl/secure/episode-videos-points",
                        params = mapOf(
                            "titleId" to titleId,
                            "season" to s.toString(),
                            "episode" to "1"
                        ),
                        headers = defaultHeaders
                    ).parsedSafe<EpisodeVideosResponse>()

                    epResponse?.episodeList?.forEach { item ->
                        val sNum = item.seasonNumber ?: s
                        val eNum = item.episodeNumber ?: return@forEach
                        episodeMeta[Pair(sNum, eNum)] = item
                    }
                } catch (_: Exception) {}
            }

            // Step 2: use mangacix related-videos to get all episode entries with ordering
            for (s in 1..seasonCount) {
                try {
                    val relResponse = app.get(
                        "https://mangacix.net/secure/related-videos",
                        params = mapOf(
                            "episode" to "1",
                            "season" to s.toString(),
                            "titleId" to titleId,
                            "videoId" to "637113"
                        ),
                        headers = defaultHeaders
                    ).parsedSafe<RelatedVideosResponse>()

                    val videos = relResponse?.videos ?: continue

                    for (video in videos) {
                        val epNum = video.episodeNum ?: continue
                        val sNum = video.seasonNum ?: s
                        val epData = buildEpData(titleId, sNum, epNum)
                        val meta = episodeMeta[Pair(sNum, epNum)]

                        episodes.add(newEpisode(epData) {
                            this.name = meta?.name ?: video.name ?: "$sNum. Sezon $epNum. Bölüm"
                            this.season = sNum
                            this.episode = epNum
                            this.posterUrl = meta?.poster?.let { fixUrl(it) }
                            this.description = meta?.description
                        })
                    }
                } catch (_: Exception) {}
            }

            // Fallback: if related-videos returned nothing, use episodeMeta directly
            if (episodes.isEmpty() && episodeMeta.isNotEmpty()) {
                episodeMeta.entries
                    .sortedWith(compareBy({ it.key.first }, { it.key.second }))
                    .forEach { (key, item) ->
                        val (sNum, eNum) = key
                        val epData = buildEpData(titleId, sNum, eNum)
                        episodes.add(newEpisode(epData) {
                            this.name = item.name ?: "$sNum. Sezon $eNum. Bölüm"
                            this.season = sNum
                            this.episode = eNum
                            this.posterUrl = item.poster?.let { fixUrl(it) }
                            this.description = item.description
                        })
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
        val episodeData = try {
            org.json.JSONObject(data)
        } catch (_: Exception) {
            return false
        }

        val titleId = episodeData.optString("id", null) ?: return false
        val season = episodeData.optInt("season", 1)
        val episode = episodeData.optInt("episode", 1)

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

    private fun buildEpData(titleId: String, season: Int, episode: Int): String {
        return org.json.JSONObject(
            mapOf(
                "id" to titleId,
                "season" to season.toString(),
                "episode" to episode.toString()
            )
        ).toString()
    }

    private fun extractTitleId(url: String): String? {
        val match = Regex("""/(?:anime|title)/(\d+)""").find(url)
        if (match != null) return match.groupValues[1]
        val numMatch = Regex(""""/(\d+)(?:\?|$)""").find(url)
        return numMatch?.groupValues?.get(1)
    }

    private suspend fun resolveVideoUrl(embedUrl: String): List<String> {
        if (embedUrl.contains("tau-video.xyz")) {
            val embedId = extractTauVideoId(embedUrl)
            if (embedId != null) {
                return resolveTauVideo(embedId)
            }
        }

        if (embedUrl.contains(".mp4") || embedUrl.contains(".m3u8")) {
            return listOf(embedUrl)
        }

        try {
            val doc = app.get(embedUrl, headers = defaultHeaders).document

            val iframeSrc = doc.selectFirst("iframe[src]")?.attr("src")
            if (!iframeSrc.isNullOrBlank()) {
                val fullSrc = if (iframeSrc.startsWith("//")) "https:$iframeSrc" else iframeSrc
                return resolveVideoUrl(fullSrc)
            }

            val videoSrc = doc.selectFirst("video source[src]")?.attr("src")
            if (!videoSrc.isNullOrBlank()) {
                val fullSrc = if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc
                return listOf(fullSrc)
            }

            val videoUrl = doc.selectFirst("video[src]")?.attr("src")
            if (!videoUrl.isNullOrBlank()) {
                val fullUrl = if (videoUrl.startsWith("//")) "https:$videoUrl" else videoUrl
                return listOf(fullUrl)
            }
        } catch (_: Exception) {}

        return listOf(embedUrl)
    }

    private fun extractTauVideoId(url: String): String? {
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
        @JsonProperty("type") val type: String?,
        @JsonProperty("title_type") val titleType: String?,
        @JsonProperty("tmdb_id") val tmdbId: Any?
    )

    data class SearchApiResponse(
        @JsonProperty("results") val results: List<SearchResult>?
    )

    data class Genre(
        @JsonProperty("id") val id: Int?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("display_name") val displayName: String?
    )

    data class Season(
        @JsonProperty("number") val number: Int?
    )

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
        @JsonProperty("season_count") val seasonCount: Int?
    )

    data class TitleApiResponse(
        @JsonProperty("title") val title: TitleDetails?
    )

    data class EpisodeItem(
        @JsonProperty("id") val id: Int?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("description") val description: String?,
        @JsonProperty("poster") val poster: String?,
        @JsonProperty("season_number") val seasonNumber: Int?,
        @JsonProperty("episode_number") val episodeNumber: Int?
    )

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

    data class VideoSource(
        @JsonProperty("id") val id: Any?,
        @JsonProperty("url") val url: String?,
        @JsonProperty("extra") val extra: String?
    )

    data class EpisodeVideosResponse(
        @JsonProperty("videos") val videos: List<VideoSource>?,
        @JsonProperty("episodeList") val episodeList: List<EpisodeItem>?
    )

    data class TauVideoUrl(
        @JsonProperty("url") val url: String?,
        @JsonProperty("label") val label: String?,
        @JsonProperty("size") val size: Long?
    )

    data class TauVideoResponse(
        @JsonProperty("urls") val urls: List<TauVideoUrl>?
    )
}
