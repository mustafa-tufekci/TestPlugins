@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

// Ported from baristomruk-max/BTVault (KekikAkademi style) into the house
// com.panates package. Server list on an episode page is base64-embedded:
// var servers = JSON.parse(atob("...")) -> resolve -> quality urls / sibnet

package com.panates

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
            val route = server.streamUrl ?: server.src ?: continue

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
