package com.panates

import android.util.Base64
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.*
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val SLC_AES_KEY = "9bYMCNQiWsXIYFWYAu7EkdsSbmGBTyUI"
private val slcJacksonMapper = ObjectMapper()

class SelcukFlix : MainAPI() {
    override var mainUrl              = "https://selcukflix.app"
    override var name                 = "SelcukFlix"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    override var sequentialMainPage            = true
    override var sequentialMainPageDelay       = 50L
    override var sequentialMainPageScrollDelay = 50L

    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor      by lazy { SlcCloudflareInterceptor(cloudflareKiller) }

    class SlcCloudflareInterceptor(private val cloudflareKiller: CloudflareKiller) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request  = chain.request()
            val response = chain.proceed(request)
            val doc      = Jsoup.parse(response.peekBody(10 * 1024).string())
            if (response.code == 503
                || doc.html().contains("Just a moment")
                || doc.html().contains("verifying")
                || doc.selectFirst("meta[name='cloudflare']") != null
            ) {
                return cloudflareKiller.intercept(chain)
            }
            return response
        }
    }

    // data formatı: "orderType|categoryIdsComma" (categoryIds boş = tüm içerikler).
    // Site ana sayfasındaki 4 bloğu yansıtır; tür satırları kaldırıldı (29 → 5 istek).
    override val mainPage = mainPageOf(
        "date_desc|"    to "Yeni Eklenen Filmler",
        "date_desc|"    to "Yeni Eklenen Diziler",
        "imdb_desc|"   to "IMDb Top Filmler",
        "imdb_desc|"   to "IMDb Top Diziler",
        "comment_desc|" to "Popüler Diziler",
    )

    private fun decryptAES(encryptedData: String): String? {
        if (encryptedData.isBlank()) return null
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val bytes  = SLC_AES_KEY.toByteArray(Charsets.UTF_8)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(bytes, "AES"), IvParameterSpec(ByteArray(16)))
            String(cipher.doFinal(Base64.decode(encryptedData, 0)), Charsets.UTF_8)
        } catch (_: Exception) { null }
    }

    private fun decodeSecureData(secureData: String): String? {
        return if (secureData.startsWith("eyJ")) {
            try { String(Base64.decode(secureData, 0), Charsets.UTF_8) } catch (_: Exception) { null }
        } else {
            decryptAES(secureData)
        }
    }

    private fun extractSecureData(html: String): String? {
        return try {
            val doc    = Jsoup.parse(html)
            val script = doc.selectFirst("script#__NEXT_DATA__")?.data() ?: return null
            val root   = slcJacksonMapper.readTree(script)
            root?.get("props")?.get("pageProps")?.get("secureData")?.asText()
        } catch (_: Exception) { null }
    }

    private fun fixPosterUrl(raw: String?): String? {
        if (raw.isNullOrBlank() || raw == "null") return null
        var url = raw
            .replace("images-macellan-online.cdn.ampproject.org/i/s/", "")
        url = Regex("file\\.[\\w.]+/").replace(url, "file.macellan.online/")
        url = Regex("images\\.[\\w.]+/").replace(url, "images.macellan.online/")
        url = url.replace("/f/f/", "/630/910/")
        return fixUrlNull(url)
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val endpoint = if (request.name.contains("Dizi")) "findSeries" else "findMovies"
        val (orderType, categoryIds) = request.data.split("|", limit = 2).let {
            it[0] to it.getOrElse(1) { "" }
        }
        val url = "$mainUrl/api/bg/$endpoint" +
            "?releaseYearStart=1900&releaseYearEnd=$year&imdbPointMin=1&imdbPointMax=10" +
            "&categoryIdsComma=$categoryIds&countryIdsComma=&orderType=$orderType&languageId=-1" +
            "&currentPage=$page&currentPageCount=12&queryStr=&categorySlugsComma=&countryCodesComma="

        val response = app.post(
            url = url,
            headers = mapOf(
                "User-Agent"       to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0",
                "Accept"           to "application/json, text/plain, */*",
                "Accept-Language"  to "en-US,en;q=0.5",
                "X-Requested-With" to "XMLHttpRequest",
                "Sec-Fetch-Site"   to "same-origin",
                "Sec-Fetch-Mode"   to "cors",
                "Sec-Fetch-Dest"   to "empty",
                "Referer"          to "$mainUrl/"
            ),
            referer     = "$mainUrl/",
            interceptor = interceptor
        ).text

        val encrypted = runCatching { slcJacksonMapper.readTree(response)?.get("response")?.asText() }.getOrNull()
        val decoded = encrypted?.takeIf { it.isNotBlank() }?.let { decryptAES(it) } ?: return newHomePageResponse(request.name, emptyList())
        val root = runCatching { slcJacksonMapper.readTree(decoded) }.getOrNull() ?: return newHomePageResponse(request.name, emptyList())

        val items = mutableListOf<SearchResponse>()
        root.get("result")?.forEach { item: JsonNode ->
            val title = item.get("original_title")?.asText()?.takeIf { it.isNotBlank() && it != "null" } ?: return@forEach
            val slug = item.get("used_slug")?.asText()?.takeIf { it.isNotBlank() } ?: return@forEach
            val href = fixUrlNull(slug) ?: return@forEach
            val poster = fixPosterUrl(item.get("poster_url")?.asText())
            val rating = item.get("imdb_point")?.takeIf { !it.isNull }?.asDouble()?.toString()

            if (href.contains("/dizi/")) {
                items.add(newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    posterUrl = poster
                    score = rating?.let { runCatching { Score.from10(it) }.getOrNull() }
                })
            } else {
                items.add(newMovieSearchResponse(title, href, TvType.Movie) {
                    posterUrl = poster
                    score = rating?.let { runCatching { Score.from10(it) }.getOrNull() }
                })
            }
        }

        val hasNext = root.get("pagination")?.get("hasMore")?.asBoolean() ?: items.isNotEmpty()
        return newHomePageResponse(request.name, items.distinctBy { it.url }, hasNext = hasNext)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val results = mutableListOf<SearchResponse>()

        try {
            val searchUrl = "$mainUrl/api/bg/searchcontent?searchterm=$query"
            val response  = app.post(
                url         = searchUrl,
                headers     = mapOf(
                    "User-Agent"       to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0",
                    "Accept"           to "application/json, text/plain, */*",
                    "Accept-Language"  to "en-US,en;q=0.5",
                    "X-Requested-With" to "XMLHttpRequest",
                    "Sec-Fetch-Site"   to "same-origin",
                    "Sec-Fetch-Mode"   to "cors",
                    "Sec-Fetch-Dest"   to "empty",
                    "Referer"          to "$mainUrl/"
                ),
                referer     = "$mainUrl/",
                interceptor = interceptor
            ).text

            val encryptedData = slcJacksonMapper.readTree(response)?.get("response")?.asText()
            if (!encryptedData.isNullOrBlank()) {
                val decoded = decryptAES(encryptedData)
                if (decoded != null) {
                    val json: JsonNode = slcJacksonMapper.readTree(decoded)
                    json.get("result")?.forEach { item: JsonNode ->
                        val title  = item.get("object_name")?.asText() ?: return@forEach
                        val slug   = item.get("used_slug")?.asText() ?: return@forEach
                        val poster = fixPosterUrl(item.get("object_poster_url")?.asText())
                        val type   = item.get("type")?.asText() ?: ""
                        val href   = fixUrl(slug)
                        if (!href.contains("/seri-filmler/")) {
                            if (type == "Movies") {
                                results.add(newMovieSearchResponse(title, href, TvType.Movie) { posterUrl = poster })
                            } else {
                                results.add(newTvSeriesSearchResponse(title, href, TvType.TvSeries) { posterUrl = poster })
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (results.isEmpty()) {
            try {
                val doc = app.get("$mainUrl/arama?q=$query", interceptor = interceptor).document
                doc.select("a[href^=/film/], a[href^=/dizi/]").forEach { el ->
                    val href  = fixUrlNull(el.attr("href")) ?: return@forEach
                    if (href == "$mainUrl/film-izle" || href == "$mainUrl/dizi-izle") return@forEach
                    val img   = el.selectFirst("img")
                    val title = el.selectFirst("h2,h3")?.text()
                        ?: img?.attr("alt")?.replace(" izle", "")?.trim()
                        ?: return@forEach
                    val poster = fixPosterUrl(img?.attr("data-src")?.takeIf { it.isNotBlank() } ?: img?.attr("src"))
                    if (href.contains("/dizi/")) {
                        results.add(newTvSeriesSearchResponse(title, href.substringBefore("/sezon"), TvType.TvSeries) { posterUrl = poster })
                    } else {
                        results.add(newMovieSearchResponse(title, href, TvType.Movie) { posterUrl = poster })
                    }
                }
            } catch (_: Exception) {}
        }

        return results.distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse {
        val html     = app.get(url, interceptor = interceptor).text
        val isSeries = url.contains("/dizi/")

        var title       = Jsoup.parse(html).selectFirst("h1")?.text() ?: ""
        var poster      : String? = null
        var bgPoster    : String? = null
        var description : String? = null
        var year        : Int?    = null
        var duration    : Int?    = null
        var rating      : String? = null
        var trailer     : String? = null
        var tags        : List<String>? = null
        var actors      : List<Actor>? = null
        val episodes    = mutableListOf<Episode>()

        val secureDataRaw = extractSecureData(html)
        if (secureDataRaw != null) {
            val jsonText = decodeSecureData(secureDataRaw)
            if (jsonText != null) {
                try {
                    val json: JsonNode = slcJacksonMapper.readTree(jsonText)

                    val item: JsonNode? = json.get("contentItem")
                    if (item != null) {
                        val origTitle = item.get("original_title")?.asText()
                        val cultTitle = item.get("culture_title")?.asText()
                            ?.takeIf { it.isNotBlank() && it != "null" }
                        if (!origTitle.isNullOrBlank() && origTitle != "null") {
                            title = if (cultTitle != null && cultTitle != origTitle && title.isBlank()) {
                                "$origTitle - $cultTitle"
                            } else if (title.isBlank()) {
                                origTitle
                            } else {
                                title
                            }
                        }

                        val pUrl = item.get("poster_url")?.asText()
                        if (!pUrl.isNullOrBlank() && pUrl != "null") poster = pUrl

                        val bUrl = item.get("back_url")?.asText()
                        if (!bUrl.isNullOrBlank() && bUrl != "null") bgPoster = bUrl

                        val desc = item.get("description")?.asText()
                        if (!desc.isNullOrBlank() && desc != "null") {
                            description = desc.replace("\\n", "\n").replace("\\r", "").replace("\\", "")
                        }

                        val yearNode = item.get("release_year")
                        if (yearNode != null && !yearNode.isNull) year = yearNode.asInt().takeIf { it > 0 }

                        val durNode = item.get("total_minutes")
                        if (durNode != null && !durNode.isNull) duration = durNode.asInt().takeIf { it > 0 }

                        val ratingNode = item.get("imdb_point")
                        if (ratingNode != null && !ratingNode.isNull) {
                            rating = runCatching { ratingNode.asDouble().toString() }.getOrNull()
                        }

                        val cats = item.get("categories")?.asText()
                        if (!cats.isNullOrBlank() && cats != "null") {
                            tags = cats.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        }
                    }

                    val relatedNode: JsonNode? = json.get("RelatedResults")
                    if (relatedNode != null) {
                        actors = relatedNode.get("getMovieCastsById")
                            ?.get("result")
                            ?.mapNotNull { cast ->
                                val name = cast.get("name")?.asText()
                                    ?: cast.get("actor_name")?.asText()
                                    ?: return@mapNotNull null
                                if (name.isBlank() || name == "null") return@mapNotNull null
                                val image = cast.get("cast_image")?.asText()
                                    ?.takeIf { it.isNotBlank() && it != "null" }
                                    ?.let { fixPosterUrl(it) }
                                Actor(name, image)
                            }?.takeIf { it.isNotEmpty() }

                        trailer = relatedNode.get("getContentTrailers")
                            ?.get("result")
                            ?.get(0)
                            ?.get("raw_url")
                            ?.asText()
                            ?.takeIf { it.isNotBlank() && it != "null" }
                    }

                    if (isSeries) {
                        val seasons: JsonNode? = json.get("RelatedResults")
                            ?.get("getSerieSeasonAndEpisodes")
                            ?.get("result")
                        seasons?.forEach { season: JsonNode ->
                            val sNum = season.get("season_no")?.asInt() ?: return@forEach
                            season.get("episodes")?.forEach { ep: JsonNode ->
                                val eNum   = ep.get("episode_no")?.asInt() ?: return@forEach
                                val epText = ep.get("episode_text")?.asText()?.takeIf { it.isNotBlank() } ?: "Bölüm $eNum"
                                val epSlug = ep.get("used_slug")?.asText() ?: return@forEach
                                val epUrl  = fixUrl(epSlug)
                                episodes.add(newEpisode(epUrl) {
                                    this.name    = epText
                                    this.season  = sNum
                                    this.episode = eNum
                                })
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        poster   = fixPosterUrl(poster)
        bgPoster = fixPosterUrl(bgPoster)

        if (isSeries && episodes.isEmpty()) {
            Jsoup.parse(html).select("a[href*=/sezon]").forEach { link ->
                val epUrl  = fixUrlNull(link.attr("href")) ?: return@forEach
                val epTxt  = link.text().takeIf { it.isNotEmpty() }
                    ?: link.selectFirst("h2,h3,span")?.text() ?: "Bölüm"
                val sMatch = Regex("/sezon-([0-9]+)").find(epUrl)
                val eMatch = Regex("/bolum-([0-9]+)").find(epUrl)
                val sNum   = sMatch?.groupValues?.get(1)?.toIntOrNull() ?: return@forEach
                val eNum   = eMatch?.groupValues?.get(1)?.toIntOrNull() ?: return@forEach
                episodes.add(newEpisode(epUrl) {
                    this.name    = epTxt
                    this.season  = sNum
                    this.episode = eNum
                })
            }
        }

        return if (isSeries) {
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes.distinctBy { it.data }) {
                posterUrl           = poster
                backgroundPosterUrl = bgPoster ?: poster
                plot                = description
                this.year           = year
                this.tags           = tags
                this.duration       = duration
                this.score          = rating?.let { runCatching { Score.from10(it) }.getOrNull() }
                addActors(actors)
                if (!trailer.isNullOrBlank()) addTrailer(trailer)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                posterUrl           = poster
                backgroundPosterUrl = bgPoster ?: poster
                plot                = description
                this.year           = year
                this.tags           = tags
                this.duration       = duration
                this.score          = rating?.let { runCatching { Score.from10(it) }.getOrNull() }
                addActors(actors)
                if (!trailer.isNullOrBlank()) addTrailer(trailer)
            }
        }
    }

    override suspend fun loadLinks(
        data             : String,
        isCasting        : Boolean,
        subtitleCallback : (SubtitleFile) -> Unit,
        callback         : (ExtractorLink) -> Unit
    ): Boolean {
        val html = app.get(data, interceptor = interceptor).text

        val secureDataRaw = extractSecureData(html) ?: return false
        val jsonText      = decodeSecureData(secureDataRaw) ?: return false
        val json: JsonNode = try { slcJacksonMapper.readTree(jsonText) } catch (_: Exception) { return false }
        val related: JsonNode = json.get("RelatedResults") ?: return false

        // One page can carry several alternative players (verified 4 on a series
        // episode). Try them all instead of just result[0] so the user gets a
        // working fallback when the first source is down or unplayable.
        val sourceContents = mutableListOf<String>()
        if (data.contains("/dizi/") || data.contains("/bolum-")) {
            related.get("getEpisodeSources")
                ?.get("result")
                ?.forEach { src ->
                    src.get("source_content")?.asText()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { sourceContents.add(it) }
                }
        } else {
            related.fieldNames().forEachRemaining { key ->
                if (key.startsWith("getMoviePartSourcesBy")) {
                    related.get(key)
                        ?.get("result")
                        ?.forEach { src ->
                            src.get("source_content")?.asText()
                                ?.takeIf { it.isNotBlank() }
                                ?.let { sourceContents.add(it) }
                        }
                }
            }
        }

        if (sourceContents.isEmpty()) return false

        var found = false
        sourceContents.distinct().forEach { sourceContent ->
            runCatching {
                val iframeEl  = Jsoup.parse(sourceContent).selectFirst("iframe")
                val iframeUrl = iframeEl?.attr("src") ?: return@forEach
                var finalUrl  = fixUrlNull(iframeUrl) ?: return@forEach

                finalUrl = finalUrl
                    .replace("sn.dplayer74.site", "sn.hotlinger.com")
                    .replace("sn.dplayer82.site", "sn.hotlinger.com")
                    .replace("sn.dplayer.site",   "sn.hotlinger.com")

                if (loadExtractor(finalUrl, data, subtitleCallback, callback)) found = true
            }
        }
        return found
    }
}
