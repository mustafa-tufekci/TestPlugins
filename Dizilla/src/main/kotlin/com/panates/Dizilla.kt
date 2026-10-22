package com.panates

import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.syncproviders.SyncIdName
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder

class Dizilla : MainAPI() {
    override var mainUrl = "https://dizilla.now"
    override var name = "Dizilla"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.TvSeries)

    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 150L
    override var sequentialMainPageScrollDelay = 150L

    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

    class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())

            if (doc.html().contains("verifying") || response.code in 400..599) {
                return cloudflareKiller.intercept(chain)
            }

            return response
        }
    }

    override val supportedSyncNames = setOf(
        SyncIdName.Simkl
    )

    override val mainPage = mainPageOf(
        "${mainUrl}/" to "Anasayfa",
        "${mainUrl}/yabanci-dizi-izle" to "Yabancı Diziler",
        "${mainUrl}/turkce-dublaj-dizi-izle" to "Türkçe Dublaj Diziler",
        "${mainUrl}/yerli-dizi-izle" to "Yerli Diziler",
        "${mainUrl}/anime-izle" to "Animeler",
        "${mainUrl}/kdrama-izle" to "K-Drama",
        "${mainUrl}/imdb-top-100" to "IMDb Top 100",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data, interceptor = interceptor).document
        val home = document.select("""a[href^="/dizi/"]""")
            .mapNotNull { it.kart() }
            .distinctBy { it.url }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.kart(): SearchResponse? {
        val href = fixUrlNull(this.attr("href")) ?: return null
        if (!href.contains("/dizi/") || href.endsWith("/dizi/")) return null

        val aria = this.attr("aria-label").trim()
        val img = this.selectFirst("img")
        val imgAlt = img?.attr("alt")?.trim().orEmpty()

        val rawTitle = if (aria.isNotBlank()) {
            aria.split(",").firstOrNull()?.trim() ?: aria
        } else if (imgAlt.isNotBlank()) {
            imgAlt
        } else {
            this.text().trim().takeIf { it.isNotBlank() } ?: return null
        }

        // Clean up common suffixes like "1. sıra: ", " izle", etc.
        val title = rawTitle.replace(Regex("""^\d+\.\s*sıra:\s*"""), "")
            .removeSuffix(" izle")
            .trim()
        if (title.isBlank()) return null

        val posterUrl = fixUrlNull(img?.attr("src"))
        val score = Regex("""(?:IMDb\s*|★\s*)(\d{1,2}(?:\.\d)?)""").find(aria.ifBlank { this.text() })
            ?.groupValues?.get(1)

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
            this.score = Score.from10(score)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "${mainUrl}/ara?q=${URLEncoder.encode(query, "UTF-8")}"
        val document = app.get(
            url,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Referer" to "${mainUrl}/"
            ),
            interceptor = interceptor
        ).document

        return document.select("""a[href^="/dizi/"]""")
            .mapNotNull { it.kart() }
            .distinctBy { it.url }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url, interceptor = interceptor).document

        // Try Schema.org TVSeries JSON-LD
        var seriesNode: JsonNode? = null
        for (script in document.select("script[type=application/ld+json]")) {
            val raw = script.data().ifBlank { script.html() }.trim()
            if (raw.isBlank()) continue
            try {
                val node = objectMapper.readTree(raw)
                if (node.path("@type").asText() == "TVSeries") {
                    seriesNode = node
                    break
                }
            } catch (_: Exception) {}
        }

        val ogTitle = document.selectFirst("meta[property=og:title]")?.attr("content").orEmpty()
        val ogDesc = document.selectFirst("meta[property=og:description]")?.attr("content").orEmpty()
        val ogImage = document.selectFirst("meta[property=og:image]")?.attr("content")

        val title = seriesNode?.path("name")?.asText(null)?.takeIf { it.isNotBlank() }
            ?: ogTitle.split("|").firstOrNull()?.removeSuffix("izle")?.trim()
            ?: document.title().split("|").firstOrNull()?.removeSuffix("izle")?.trim()
            ?: return null

        val description = seriesNode?.path("description")?.asText(null)
            ?.takeIf { it.isNotBlank() }
            ?: ogDesc.takeIf { it.isNotBlank() }

        val posterUrl = fixUrlNull(seriesNode?.path("image")?.asText(null))
            ?: fixUrlNull(ogImage)
            ?: fixUrlNull(document.selectFirst("img[src*='poster']")?.attr("src"))

        val rating = seriesNode?.path("aggregateRating")?.path("ratingValue")
            ?.takeIf { !it.isMissingNode && !it.isNull }?.asText()

        val yearMatch = Regex("""\b(19\d\d|20\d\d)\b""").find(ogDesc)
        val year = yearMatch?.groupValues?.get(1)?.toIntOrNull()

        val tags = Regex("""\b(?:19\d\d|20\d\d)\s+([^yapım\n]+?)\s+yapımını""").find(ogDesc)
            ?.groupValues?.get(1)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?: emptyList()

        val actors = mutableListOf<Actor>()
        seriesNode?.path("actor")?.forEach { actorNode ->
            val actorName = actorNode.path("name").asText(null)
            if (!actorName.isNullOrBlank()) {
                val actorImage = actorNode.path("image").asText(null)
                actors.add(Actor(actorName, actorImage))
            }
        }

        val episodes = mutableListOf<Episode>()
        seriesNode?.path("containsSeason")?.forEach { seasonNode ->
            val seasonNo = seasonNode.path("seasonNumber").asInt(1)
            seasonNode.path("episode").forEach { epNode ->
                val epUrl = fixUrlNull(epNode.path("url").asText(null)) ?: return@forEach
                val epNo = epNode.path("episodeNumber").takeIf { !it.isMissingNode && !it.isNull }?.asInt()
                val epName = epNode.path("name").asText(null)
                val epDesc = epNode.path("description").asText(null)

                episodes.add(newEpisode(epUrl) {
                    this.name = epName
                    this.season = seasonNo
                    this.episode = epNo
                    this.description = epDesc
                })
            }
        }

        // Fallback: If no episodes found in JSON-LD, parse episode links from page HTML
        if (episodes.isEmpty()) {
            document.select("""a[href*="-sezon-"][href*="-bolum"]""").forEach { a ->
                val epHref = fixUrlNull(a.attr("href")) ?: return@forEach
                val epText = a.attr("aria-label").ifBlank { a.text() }.trim()
                val sMatch = Regex("""(\d+)\.\s*Sezon""").find(epText)
                val eMatch = Regex("""(\d+)\.\s*Bölüm""").find(epText)
                val sNo = sMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val eNo = eMatch?.groupValues?.get(1)?.toIntOrNull()

                episodes.add(newEpisode(epHref) {
                    this.name = epText.split(",").firstOrNull()?.trim() ?: epText
                    this.season = sNo
                    this.episode = eNo
                })
            }
        }

        if (episodes.isEmpty()) return null

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes.distinctBy { it.data }) {
            this.posterUrl = posterUrl
            this.backgroundPosterUrl = posterUrl
            this.year = year
            this.plot = description
            this.tags = tags
            this.score = Score.from10(rating)
            if (actors.isNotEmpty()) addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val response = app.get(
            data,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Referer" to "${mainUrl}/"
            ),
            interceptor = interceptor
        )
        val html = response.text
        var found = false

        // 1) Find Next.js embedded sources JSON
        val match = Regex("""(?:\\"sources\\"|"sources")\s*:\s*(\[.*?\])\s*,\s*(?:\\"hosts\\"|"hosts")""").find(html)
        if (match != null) {
            val raw = match.groupValues[1]
                .replace("""\"""", """"""")
                .replace("""\\""", """\""")
            try {
                val sourcesNode = objectMapper.readTree(raw)
                sourcesNode.forEach { source ->
                    if (source.path("trailer").asBoolean(false)) return@forEach
                    val src = source.path("src").asText(null) ?: return@forEach
                    if (src.isNotBlank()) {
                        val fixedSrc = if (src.startsWith("//")) "https:$src" else fixUrl(src)
                        loadExtractor(fixedSrc, data, subtitleCallback, callback)
                        found = true
                    }
                }
            } catch (e: Exception) {
                Log.e("Dizilla", "Failed parsing sources JSON: ${e.message}")
            }
        }

        // 2) Fallback: Regex search for direct iframe player URLs
        if (!found) {
            Regex("""https?://[^"'\s<>]*(?:pichive|filese|hotlinger|playru|okvip)[^"'\s<>]*iframe\.php\?v=[^"'\s<>&]+""").findAll(html).forEach {
                val iframeUrl = it.value
                loadExtractor(iframeUrl, data, subtitleCallback, callback)
                found = true
            }
        }

        return found
    }

    private val objectMapper by lazy {
        ObjectMapper().registerModule(KotlinModule.Builder().build()).apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
}
