@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

// Ported from baristomruk-max/BTVault (KekikAkademi style) into the house
// com.panates package. Server list on an episode page is base64-embedded:
// var servers = JSON.parse(atob("...")) -> resolve -> quality urls / sibnet

package com.panates

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import com.lagradost.cloudstream3.network.CloudflareKiller
import java.net.URLEncoder

data class CmxServer(
    val type: String? = null,
    val streamUrl: String? = null,
    /** iframe servers carry their route here (/oynat/?t=...), not streamUrl */
    val src: String? = null,
    /** dzen servers carry an opaque resolve route here (/api/np-resolve/?t=...) -> {"id": "<tau embed id>"} */
    val resolveUrl: String? = null,
    val label: String? = null,
    val embedId: Int? = null,
    val videoId: String? = null,
)

data class CmxStream(
    val id: Int? = null,
    val url: String? = null,
    val urls: List<CmxQuality>? = null,
    val captions: List<CmxCaption>? = null,
)

data class CmxQuality(
    val label: String? = null,
    val url: String? = null,
)

data class CmxCaption(
    val lang: String? = null,
    val name: String? = null,
    val url: String? = null,
)

class CizgiMax : MainAPI() {
    private companion object {
        const val TAU_API = "https://tau-video.xyz"
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
    }

    // Only engages on actual Cloudflare challenges — reading the challenge-free
    // response body inside a custom interceptor corrupts it (body already consumed).
    private val cloudflareKiller by lazy { CloudflareKiller() }

    override var mainUrl        = "https://cizgimax.online"
    override var name           = "CizgiMax"
    override val hasMainPage    = true
    override var lang           = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Cartoon)

    override val mainPage = mainPageOf(
        "/"               to "Son Eklenenler",
        "/diziler/anime/" to "Animeler",
        "/diziler/cizgi-film/" to "Çizgi Film",
        "/diziler/dizi/"  to "Çizgi Diziler",
        "/tur/aile/"      to "Aile",
        "/tur/aksiyon/"   to "Aksiyon",
        "/tur/komedi/"    to "Komedi",
        "/tur/bilim-kurgu/" to "Bilim Kurgu",
        "/tur/cocuk/"     to "Çocuk"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val sep  = if (request.data.contains("?")) "&" else "?"
        val home = app.get("$mainUrl${request.data}$sep page=$page".replace(" ", ""), interceptor = cloudflareKiller)
            .document
            .select("div.film-item")
            .mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val nameEl = this.selectFirst("a.film-name")
        val title  = nameEl?.text()?.trim().orEmpty()
            .ifBlank { this.attr("data-anime-name").trim() }
        val href   = nameEl?.attr("href")?.trim().orEmpty()
            .ifBlank { this.selectFirst("a.poster")?.attr("href")?.trim().orEmpty() }

        if (title.isBlank() || href.isNullOrBlank()) return null

        val img    = this.selectFirst("a.poster img")
        val poster = img?.let { it.attr("src").ifBlank { it.attr("data-src") } }

        return newTvSeriesSearchResponse(title, fixUrl(href), TvType.Cartoon) {
            this.posterUrl = fixUrlNull(poster)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return app.get("$mainUrl/ara/?q=$encoded", interceptor = cloudflareKiller)
            .document
            .select("div.film-list div.film-item")
            .mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url, interceptor = cloudflareKiller).document

        val title       = document.selectFirst("div.anime-head-titles h1")?.text()?.trim()?.ifBlank { null }
            ?: document.selectFirst("h1")?.text()?.trim()?.ifBlank { null }
            ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.anime-poster img")?.attr("src"))
        val description = document.selectFirst("p.anime-desc")?.text()?.trim()
        val tags        = document.select("li.meta-genres strong a")
            .mapNotNull { it.text().trim().ifBlank { null } }
        val rating      = document.selectFirst("span.ep-score-value")?.text()?.trim()
            ?.replace(",", ".")?.toRatingInt()

        val episodes = document.select("div.ep-grid-numbers").flatMap { pane ->
            val season = pane.attr("data-season-pane").toIntOrNull()

            pane.select("a.ep-num-btn").mapNotNull { ep ->
                val href = fixUrlNull(ep.attr("href")) ?: return@mapNotNull null
                val num  = ep.selectFirst("span.ep-num-label")?.text()?.trim()?.toIntOrNull()
                val alt  = ep.attr("title").trim().ifBlank { ep.text().trim() }

                val epName = when {
                    num == null && alt.isBlank() -> return@mapNotNull null
                    num == null                  -> alt
                    alt.isBlank() || alt == "$num. Bölüm" -> "$num. Bölüm"
                    else                         -> "$num. Bölüm - $alt"
                }

                newEpisode(href) {
                    this.name    = epName
                    this.season  = season
                    this.episode = num
                }
            }
        }

        return newTvSeriesLoadResponse(title, url, TvType.Cartoon, episodes) {
            this.posterUrl = poster
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
        }
    }

    /**
     * dzen servers (new player, 2026-05): two-step resolve.
     *   1) /api/np-resolve/?t=<token>  -> {"id": "<tau embed id>"}
     *   2) https://tau-video.xyz/api/video/<id> -> { urls: [{label, url}], title_id, season_number, episode_number }
     *   3) soft subs (optional) via animecix.tv/secure/episode-videos?titleId=&season=&episode=
     * The mp4 urls are CDN links: sending a Referer makes the CDN refuse, so none is attached.
     */
    private suspend fun emitTauServer(
        server: CmxServer,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val label = server.label?.takeIf { it.isNotBlank() } ?: "Dzen"
        val resolveUrl = server.resolveUrl ?: return false

        val embedId = runCatching {
            JSONObject(app.get(fixUrl(resolveUrl), referer = referer, interceptor = cloudflareKiller).text)
                .optString("id")
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: return false

        val body = runCatching {
            app.get(
                "$TAU_API/api/video/$embedId",
                headers = mapOf(
                    "User-Agent" to USER_AGENT,
                    "Referer" to "$TAU_API/",
                    "Origin" to TAU_API
                )
            ).text
        }.getOrNull() ?: return false

        val root = runCatching { JSONObject(body) }.getOrNull() ?: return false
        val urls = root.optJSONArray("urls") ?: return false

        var emitted = false
        for (i in 0 until urls.length()) {
            val entry = urls.optJSONObject(i) ?: continue
            val link = entry.optString("url")
            if (link.isBlank()) continue
            val qualityLabel = entry.optString("label")
            callback.invoke(
                newExtractorLink(
                    source = label,
                    name   = "$label $qualityLabel".trim(),
                    url    = link,
                    type   = ExtractorLinkType.VIDEO
                ) {
                    this.referer = ""
                    this.quality = cmxQuality(qualityLabel)
                }
            )
            emitted = true
        }

        // Soft subtitles live on Animecix's secure endpoint, keyed by title/season/episode.
        val titleId = root.optInt("title_id", -1)
        val season  = root.optInt("season_number", -1)
        val episode = root.optInt("episode_number", -1)
        if (titleId >= 0 && season >= 0 && episode >= 0) {
            runCatching {
                val arr = JSONArray(
                    app.get(
                        "https://animecix.tv/secure/episode-videos?titleId=$titleId&season=$season&episode=$episode",
                        headers = mapOf("User-Agent" to USER_AGENT, "Accept" to "application/json")
                    ).text
                )
                for (i in 0 until arr.length()) {
                    val entry = arr.optJSONObject(i) ?: continue
                    if (!entry.optString("url").contains("/embed/$embedId")) continue
                    val caps = entry.optJSONArray("captions") ?: break
                    for (j in 0 until caps.length()) {
                        val cap = caps.optJSONObject(j) ?: continue
                        val capUrl = cap.optString("url")
                        if (capUrl.isBlank()) continue
                        subtitleCallback.invoke(
                            newSubtitleFile(
                                cap.optString("name").ifBlank { cap.optString("language").ifBlank { "Türkçe" } },
                                fixUrl(capUrl)
                            )
                        )
                    }
                    break
                }
            }
        }

        return emitted
    }

    private fun cmxQuality(label: String?): Int {
        val value = label?.lowercase().orEmpty()
        val digits = Regex("""(\d{3,4})""").find(value)?.groupValues?.get(1)?.toIntOrNull()
        return when {
            value.contains("4k") || (digits ?: 0) >= 2160 -> Qualities.P2160.value
            (digits ?: 0) >= 1080 || value.contains("full") -> Qualities.P1080.value
            (digits ?: 0) >= 720 -> Qualities.P720.value
            (digits ?: 0) >= 480 -> Qualities.P480.value
            (digits ?: 0) >= 360 -> Qualities.P360.value
            else -> Qualities.Unknown.value
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val html = app.get(data, interceptor = cloudflareKiller).text

        // Server list base64-embedded: var servers = JSON.parse(atob("..."))
        val servers = Regex("""var\s+servers\s*=\s*JSON\.parse\(atob\("([^"]+)"\)\)""")
            .find(html)
            ?.groupValues?.get(1)
            ?.replace("\\/", "/")
            ?.let { json -> runCatching { ObjectMapper().readValue(json, object : TypeReference<List<CmxServer>>() {}) }.getOrNull() }
            ?: emptyList()

        for (server in servers) {
            val route = server.streamUrl ?: server.src ?: server.resolveUrl ?: continue

            // dzen servers: /api/np-resolve/?t=<token> -> {"id": "<embed id>"} on the
            // tau-video API (same upstream Animecix serves), which returns mp4 urls[].
            if (server.resolveUrl != null && (server.type.isNullOrBlank() || server.type.equals("dzen", true))) {
                emitTauServer(server, data, subtitleCallback, callback)
                continue
            }

            // byse servers: /api/stream sequence -> json {url, urls[], captions}
            if (server.type.equals("byse", true)) {
                val resolve = runCatching {
                    val resp = app.get(fixUrl(route), referer = data, interceptor = cloudflareKiller).text
                    ObjectMapper().readValue(resp, CmxStream::class.java)
                }.getOrNull() ?: continue

                val qualities = resolve?.urls.orEmpty().filter { !it.url.isNullOrBlank() }
                for (quality in qualities) {
                    val link = quality.url ?: continue
                    callback.invoke(
                        newExtractorLink(
                            source = server.label ?: name,
                            name   = "${server.label ?: name} ${quality.label ?: ""}".trim(),
                            url    = fixUrl(link),
                            type   = ExtractorLinkType.M3U8
                        ) {
                            this.headers = mapOf("Referer" to "$mainUrl/")
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
                if (qualities.isNotEmpty()) continue

                val direct = resolve?.url
                if (!direct.isNullOrBlank()) {
                    callback.invoke(
                        newExtractorLink(
                            source = server.label ?: name,
                            name   = server.label ?: name,
                            url    = fixUrl(direct),
                            type   = if (direct.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                        ) {
                            this.headers = mapOf("Referer" to "$mainUrl/")
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
                continue
            }

            // iframe servers: /oynat/?t=<jwt> 302-redirects to the embed host
            // (vidmoly, sibnet, ...). Resolve the redirect and hand the embed
            // URL to the extractor registry.
            try {
                val resp = app.get(fixUrl(route), referer = data, interceptor = cloudflareKiller)
                val embedUrl = resp.url
                if (fixUrl(route) == embedUrl || embedUrl == null) continue
                loadExtractor(embedUrl, "$mainUrl/", subtitleCallback, callback)
            } catch (_: Exception) {
                // embed unreachable — try next server
            }
        }

        // Classic extractor chain for direct iframes on the page
        for (frame in Jsoup.parse(html).select("iframe[src]")) {
            val src = frame.attr("src").trim()
            if (src.isBlank() || src.contains("/oynat/") || !src.startsWith("http")) continue
            loadExtractor(fixUrl(src), "$mainUrl/", subtitleCallback, callback)
        }

        return true
    }
}
