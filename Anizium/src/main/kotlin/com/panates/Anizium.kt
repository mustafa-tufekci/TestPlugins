package com.panates

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities

/**
 * Anizium — anime catalog API client.
 *
 * Endpoints (all JSON, no login required):
 * - GET api.anizium.co/page/search?value=..&page=..   (search, paged via not_displayed)
 * - GET api.anizium.co/anime/get?id=..                (details + seasons/episodes)
 * - GET api.anizium.co/anime/source?id=..&plan=standart&season=..&episode=..&server=1|2
 * - GET api.anizium.co/page/home                      (homepage rows in special_list)
 *
 * Auth is a static header (Cf-Control); if it rotates, every call fails and
 * the module needs a new token.
 */
class Anizium : MainAPI() {
    override var mainUrl              = "https://anizium.co"
    override var name                 = "Anizium"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Anime, TvType.Movie)

    private val apiUrl = "https://api.anizium.co"

    companion object {
        private const val CF_CONTROL =
            "134e1e595b580d51550809065948050306434c065c54530f"
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"

        private val apiHeaders = mapOf(
            "User-Agent"  to USER_AGENT,
            "Accept"      to "application/json, text/plain, */*",
            "Cf-Control"  to CF_CONTROL,
            "Referer"     to "https://anizium.co/",
            "Origin"      to "https://anizium.co"
        )
    }

    private val mapper = ObjectMapper()

    // ── Main page ────────────────────────────────────────────────────────

    override val mainPage = mainPageOf(
        "Popüler"                  to "Popüler",
        "Öne Çıkanlar"             to "Öne Çıkanlar",
        "Anizium'da Yeni İçerikler" to "Anizium'da Yeni İçerikler",
        "Türkçe Dublaj"            to "Türkçe Dublaj"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        if (page > 1) return newHomePageResponse(request.name, emptyList())
        return try {
            val root = app.get("$apiUrl/page/home", headers = apiHeaders).text
                .let { mapper.readTree(it) }
            val items = root.get("special_list")
                ?.firstOrNull { it.get("name")?.asText() == request.data }
                ?.get("data")
                ?.mapNotNull { it.toCard() }
                ?.distinctBy { it.url }
                .orEmpty()
            newHomePageResponse(request.name, items)
        } catch (_: Exception) {
            newHomePageResponse(request.name, emptyList())
        }
    }

    private fun JsonNode.toCard(): SearchResponse? {
        val id = get("ID")?.asText()?.takeIf { it.isNotBlank() } ?: return null
        val title = get("name")?.asText()?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val href = "$mainUrl/anime/$id"
        val poster = get("poster")?.asText()?.takeIf { it.isNotBlank() && it != "null" }
        val score = get("imdb_point")?.takeIf { it.isNumber }?.asDouble()
            ?.let { runCatching { Score.from10(it) }.getOrNull() }
        val year = get("release_year")?.asText()?.take(4)?.toIntOrNull()
            ?: get("created")?.asText()?.take(4)?.toIntOrNull()
        return if (get("type")?.asText().equals("movie", ignoreCase = true)) {
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
                this.year = year
                this.score = score
            }
        } else {
            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = poster
                this.year = year
                this.score = score
            }
        }
    }

    // ── Search ───────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        val out = mutableListOf<SearchResponse>()
        var page = 1
        // not_displayed > 0 means further pages exist (Migurdex logic); cap at 5 pages
        while (page <= 5) {
            val root = try {
                mapper.readTree(
                    app.get(
                        "$apiUrl/page/search",
                        params = mapOf("value" to query, "page" to "$page"),
                        headers = apiHeaders
                    ).text
                )
            } catch (_: Exception) {
                break
            }
            val pageObj = root.get("page") ?: break
            pageObj.get("data")?.forEach { item ->
                item.toCard()?.let { out.add(it) }
            }
            val left = pageObj.get("not_displayed")?.asInt(0) ?: 0
            if (left <= 0) break
            page++
        }
        return out.distinctBy { it.url }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse {
        val id = Regex("""/anime/(\d+)""").find(url)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("Anizium: id yok ($url)")
        val data = try {
            mapper.readTree(
                app.get(
                    "$apiUrl/anime/get",
                    params = mapOf("id" to id),
                    headers = apiHeaders
                ).text
            ).get("data") ?: throw ErrorLoadingException("Anizium: boş yanıt")
        } catch (e: Exception) {
            throw ErrorLoadingException(e.message)
        }

        val title = data.get("name")?.asText()?.trim().orEmpty()
        if (title.isBlank()) throw ErrorLoadingException("Anizium: başlık yok")
        val poster = data.get("poster")?.asText()?.takeIf { it.isNotBlank() && it != "null" }
        val plot = data.get("overview")?.asText()?.takeIf { it.isNotBlank() }
        val year = data.get("release_year")?.asText()?.take(4)?.toIntOrNull()
        val tags = data.get("genre")?.mapNotNull { it.asText()?.takeIf { g -> g.isNotBlank() } }
        val score = data.get("imdb_point")?.takeIf { it.isNumber }?.asDouble()
            ?.let { runCatching { Score.from10(it) }.getOrNull() }
        val isMovie = data.get("type")?.asText().equals("movie", ignoreCase = true)

        val episodes = mutableListOf<Episode>()
        if (isMovie) {
            episodes.add(newEpisode(buildEpData(id, 1, 1)) {
                this.name = "Film"
                this.season = 1
                this.episode = 1
            })
        } else {
            data.get("seasons")?.forEach { season ->
                val sNum = season.get("number")?.asInt(1) ?: 1
                season.get("episodes")?.forEach { ep ->
                    val eNum = ep.get("number")?.asInt() ?: return@forEach
                    val eName = ep.get("name")?.asText()?.trim()?.takeIf { it.isNotBlank() }
                        ?: "$eNum. Bölüm"
                    episodes.add(newEpisode(buildEpData(id, sNum, eNum)) {
                        this.name = eName
                        this.season = sNum
                        this.episode = eNum
                    })
                }
            }
        }
        if (episodes.isEmpty()) throw ErrorLoadingException("Anizium: bölüm yok")

        return if (isMovie) {
            newMovieLoadResponse(title, url, TvType.Movie, episodes) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                this.tags = tags
                this.score = score
            }
        } else {
            newTvSeriesLoadResponse(title, url, TvType.Anime, episodes) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                this.tags = tags
                this.score = score
            }
        }
    }

    private fun buildEpData(id: String, season: Int, episode: Int): String {
        return org.json.JSONObject(
            mapOf("id" to id, "season" to season.toString(), "episode" to episode.toString())
        ).toString()
    }

    // ── Load links ───────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ep = try {
            org.json.JSONObject(data)
        } catch (_: Exception) {
            return false
        }
        val id = ep.optString("id", null) ?: return false
        val season = ep.optString("season", "1")
        val episode = ep.optString("episode", "1")

        var found = false
        for (server in listOf(1, 2)) {
            val root = try {
                mapper.readTree(
                    app.get(
                        "$apiUrl/anime/source",
                        params = mapOf(
                            "id" to id,
                            "plan" to "standart",
                            "season" to season,
                            "episode" to episode,
                            "server" to "$server"
                        ),
                        headers = apiHeaders
                    ).text
                )
            } catch (_: Exception) {
                continue
            }

            root.get("subtitles")?.forEach { sub ->
                val subUrl = sub.get("link")?.asText()?.takeIf { it.isNotBlank() } ?: return@forEach
                val lang = sub.get("group")?.asText()?.takeIf { it.isNotBlank() } ?: "tr"
                subtitleCallback(SubtitleFile(lang, subUrl))
            }

            root.get("groups")?.forEach { group ->
                val groupName = group.get("name")?.asText()?.takeIf { it.isNotBlank() } ?: "Anizium"
                val isMp4 = group.get("type")?.asText().equals("mp4", ignoreCase = true)
                group.get("items")?.forEach { item ->
                    val streamUrl = item.get("link")?.asText()?.takeIf { it.isNotBlank() }
                        ?: return@forEach
                    val qNode = item.get("quality")
                    val qInt = qNode?.let {
                        if (it.isNumber) it.asInt()
                        else it.asText().filter { c -> c.isDigit() }.toIntOrNull()
                    }
                    val q = if (qInt != null) "${qInt}p" else "Auto"
                    val quality = when (qInt) {
                        2160 -> Qualities.P2160
                        1440 -> Qualities.P1440
                        1080 -> Qualities.P1080
                        720 -> Qualities.P720
                        480 -> Qualities.P480
                        360 -> Qualities.P360
                        else -> Qualities.Unknown
                    }.value
                    callback(
                        ExtractorLink(
                            source = "Anizium",
                            name = "Anizium $groupName $q (S$server)",
                            url = streamUrl,
                            referer = "$mainUrl/",
                            quality = quality,
                            headers = mapOf(
                                "User-Agent" to USER_AGENT,
                                "Referer" to "$mainUrl/",
                                "Origin" to mainUrl
                            ),
                            type = if (isMp4) ExtractorLinkType.VIDEO else ExtractorLinkType.M3U8
                        )
                    )
                    found = true
                }
            }
        }
        return found
    }
}
