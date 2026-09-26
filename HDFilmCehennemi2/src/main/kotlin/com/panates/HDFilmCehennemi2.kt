package com.panates

import com.fasterxml.jackson.databind.ObjectMapper
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
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URLEncoder

class HDFilmCehennemi2 : MainAPI() {
    override var mainUrl = "https://www.hdfilmcehennemi2.biz"
    override var name = "HDFilmCehennemi2"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport = true
    override var supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/" to "Yeni Eklenenler",
        "${mainUrl}/filmler/" to "Filmler",
        "${mainUrl}/dizi-izle/" to "Diziler",
        "${mainUrl}/tur/aile-filmleri/" to "Aile",
        "${mainUrl}/tur/aksiyon-filmleri-izle/" to "Aksiyon",
        "${mainUrl}/tur/animasyon-film-izle/" to "Animasyon",
        "${mainUrl}/tur/belgesel-filmleri/" to "Belgesel",
        "${mainUrl}/tur/bilim-kurgu-filmleri-izle/" to "Bilim Kurgu",
        "${mainUrl}/tur/biyografi-filmleri/" to "Biyografi",
        "${mainUrl}/tur/dram-filmleri/" to "Dram",
        "${mainUrl}/tur/fantastik-filmleri/" to "Fantastik",
        "${mainUrl}/tur/gerilim-filmleri/" to "Gerilim",
        "${mainUrl}/tur/gizem-filmleri/" to "Gizem",
        "${mainUrl}/tur/komedi-filmleri/" to "Komedi",
        "${mainUrl}/tur/korku-filmleri/" to "Korku",
        "${mainUrl}/tur/macera-filmleri/" to "Macera",
        "${mainUrl}/tur/romantik-filmler/" to "Romantik",
        "${mainUrl}/tur/savas-filmleri/" to "Savaş",
        "${mainUrl}/tur/suc-filmleri/" to "Suç",
        "${mainUrl}/tur/tarih-filmleri/" to "Tarih",
        "${mainUrl}/yil/2026/" to "2026"
    )

    private val cardRegex = Regex("""loadMoreGrid\(\{(.*?)\}\)""", RegexOption.DOT_MATCHES_ALL)

    private fun parseCards(document: Document): List<SearchResponse> {
        return document.select("a[class*=group/poster]").mapNotNull { element ->
            val href = fixUrlNull(element.attr("href")) ?: return@mapNotNull null
            val img = element.selectFirst("img") ?: return@mapNotNull null
            val title = img.attr("alt").trim().substringBefore(" izle").ifBlank {
                return@mapNotNull null
            }
            val poster = fixUrlNull(img.attr("src"))
            val score = element.selectFirst("span.bg-primary")?.text()?.trim()
            val year = element.select("span").map { it.text().trim() }
                .firstOrNull { it.matches(Regex("""\d{4}""")) }

            if (href.contains("/dizi/")) {
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = poster
                    this.score = Score.from10(score)
                    this.year = year?.toIntOrNull()
                }
            } else {
                newMovieSearchResponse(title, href, TvType.Movie) {
                    this.posterUrl = poster
                    this.score = Score.from10(score)
                    this.year = year?.toIntOrNull()
                }
            }
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get(request.data)
        val document = response.document
        val items = parseCards(document)
        if (page <= 1) {
            return newHomePageResponse(request.name, items, items.size >= 24)
        }

        val block = cardRegex.find(document.html())?.groupValues?.get(1)
        val loadMoreUrl = block?.let { Regex("""url:\s*'([^']+)'""").find(it)?.groupValues?.get(1) }
        val gridId = block?.let { Regex("""gridId:\s*'([^']+)'""").find(it)?.groupValues?.get(1) }
        val sort = block?.let { Regex("""sort:\s*'([^']+)'""").find(it)?.groupValues?.get(1) }
        val offsetStep = block?.let { Regex("""offset:\s*(\d+)""").find(it)?.groupValues?.get(1)?.toIntOrNull() }
        if (loadMoreUrl == null || gridId == null || sort == null || offsetStep == null) {
            return newHomePageResponse(request.name, emptyList(), false)
        }

        val rawToken = response.cookies["XSRF-TOKEN"]
            ?: return newHomePageResponse(request.name, emptyList(), false)
        val token = if (rawToken.contains('%')) {
            runCatching { java.net.URLDecoder.decode(rawToken, "UTF-8") }.getOrNull() ?: rawToken
        } else rawToken

        val offset = offsetStep * (page - 1)
        val html = runCatching {
            app.post(
                loadMoreUrl,
                data = mapOf(
                    "offset" to offset.toString(),
                    "sort" to sort,
                    "gridId" to gridId
                ),
                headers = mapOf(
                    "X-XSRF-TOKEN" to token,
                    "X-Requested-With" to "XMLHttpRequest",
                    "Accept" to "*/*"
                ),
                referer = request.data
            ).text
        }.getOrNull() ?: return newHomePageResponse(request.name, emptyList(), false)

        val fragment = runCatching {
            ObjectMapper().readValue(html, Hdc2LoadMoreResponse::class.java).html
        }.getOrNull()
        if (fragment.isNullOrBlank()) {
            return newHomePageResponse(request.name, emptyList(), false)
        }

        val pageItems = parseCards(org.jsoup.Jsoup.parse(fragment, mainUrl))
        return newHomePageResponse(request.name, pageItems, pageItems.size >= offsetStep)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val text = app.get(
            "$mainUrl/api/search?q=${URLEncoder.encode(query, "UTF-8")}"
        ).text
        val data = runCatching {
            ObjectMapper().readTree(text).path("data")
        }.getOrNull() ?: return emptyList()

        return data.mapNotNull { item ->
            val slug = item.path("slug").asText(null) ?: return@mapNotNull null
            val title = item.path("title").asText(null)?.trim()?.ifBlank { null }
                ?: return@mapNotNull null
            val href = fixUrlNull("$mainUrl/$slug") ?: return@mapNotNull null
            val poster = fixUrlNull(item.path("posterUrl").asText(null))
            val year = item.path("releaseYear").asText(null)?.toIntOrNull()
            val score = item.path("imdbRating").asText(null)
            val isSeries = item.path("contentableType").asText("").contains("TVSeries")

            if (isSeries) {
                newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                    this.posterUrl = poster
                    this.year = year
                    this.score = Score.from10(score)
                }
            } else {
                newMovieSearchResponse(title, href, TvType.Movie) {
                    this.posterUrl = poster
                    this.year = year
                    this.score = Score.from10(score)
                }
            }
        }
    }

    private fun parseJsonLd(document: Document): com.fasterxml.jackson.databind.JsonNode? {
        document.select("script[type=application/ld+json]").forEach { script ->
            val node = runCatching {
                ObjectMapper().readTree(script.data())
            }.getOrNull() ?: return@forEach
            val type = node.path("@type").asText("")
            if (type == "Movie" || type == "TVSeries") return node
        }
        return null
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val ld = parseJsonLd(document)
        val rawTitle = document.selectFirst("h1")?.text()?.trim()
            ?: ld?.path("name")?.asText(null)?.substringBefore(" izle")?.trim()
            ?: return null
        val title = rawTitle.substringBefore(" izle").trim().ifBlank { rawTitle }

        val plot = ld?.path("description")?.asText(null)
        val poster = fixUrlNull(ld?.path("image")?.asText(null))
        val year = ld?.path("dateCreated")?.asText(null)?.take(4)?.toIntOrNull()
        val score = ld?.path("aggregateRating")?.path("ratingValue")?.asText(null)
        val tags = ld?.path("genre")?.mapNotNull { it.asText(null)?.trim()?.ifBlank { null } }
        val duration = ld?.path("duration")?.asText(null)?.let { parseDuration(it) }

        val actors = ld?.path("actor")?.mapNotNull { person ->
            val name = person.path("name").asText(null)?.trim()?.ifBlank { null } ?: return@mapNotNull null
            Actor(name = name, image = fixUrlNull(person.path("image").asText(null)))
        } ?: emptyList()

        if (url.contains("/dizi/")) {
            val episodes = LinkedHashMap<String, Episode>()
            document.select("a[href]").forEach { anchor ->
                val href = fixUrlNull(anchor.attr("href")) ?: return@forEach
                if (!href.contains(Regex("""/sezon-\d+/bolum-\d+"""))) return@forEach
                if (episodes.containsKey(href)) return@forEach
                val season = Regex("""/sezon-(\d+)""").find(href)?.groupValues?.get(1)?.toIntOrNull()
                val episode = Regex("""/bolum-(\d+)""").find(href)?.groupValues?.get(1)?.toIntOrNull()
                val epName = anchor.text().trim().ifBlank { null }
                episodes[href] = newEpisode(href) {
                    this.season = season
                    this.episode = episode
                    this.name = epName
                }
            }
            if (episodes.isEmpty()) return null

            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes.values.toList()) {
                this.posterUrl = poster
                this.plot = plot
                this.year = year
                this.tags = tags
                this.score = Score.from10(score)
                this.duration = duration
                addActors(actors)
            }
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = plot
            this.year = year
            this.tags = tags
            this.score = Score.from10(score)
            this.duration = duration
            addActors(actors)
        }
    }

    private fun parseDuration(value: String): Int? {
        val hours = Regex("""PT(\d+)H""").find(value)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = Regex("""PT(?:\d+H)?(\d+)M""").find(value)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val total = hours * 60 + minutes
        return total.takeIf { it > 0 }
    }

    private fun jsUnescape(value: String): String {
        val sb = StringBuilder(value.length)
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c != '\\' || i + 1 >= value.length) {
                sb.append(c)
                i++
                continue
            }
            when (val next = value[i + 1]) {
                'u' -> {
                    if (i + 5 < value.length) {
                        val code = value.substring(i + 2, i + 6).toIntOrNull(16)
                        if (code != null) {
                            sb.append(code.toChar())
                            i += 6
                            continue
                        }
                    }
                    sb.append(next)
                    i += 2
                }
                'n' -> { sb.append('\n'); i += 2 }
                'r' -> { sb.append('\r'); i += 2 }
                't' -> { sb.append('\t'); i += 2 }
                else -> { sb.append(next); i += 2 }
            }
        }
        return sb.toString()
    }

    private fun languageLabel(lang: String): String = when (lang.lowercase()) {
        "dual" -> "Türkçe Dublaj & Altyazı"
        "dub", "dublaj" -> "Türkçe Dublaj"
        "alt", "altyazi" -> "Türkçe Altyazılı"
        "orj", "orijinal" -> "Orijinal"
        else -> lang.replaceFirstChar { it.uppercase() }
    }

    private fun subtitleLabel(url: String): String {
        val file = url.substringAfterLast("/")
        val tag = Regex("""_(tur_forced|tur|eng|forced)\.vtt""").find(file)?.groupValues?.get(1)
            ?: return file
        return when (tag) {
            "tur" -> "Türkçe"
            "eng" -> "İngilizce"
            "tur_forced" -> "Türkçe (Forced)"
            else -> "Forced"
        }
    }

    private suspend fun resolveVidload(
        iframeUrl: String,
        label: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val page = runCatching {
            app.get(iframeUrl, referer = mainUrl).text
        }.getOrNull() ?: return

        val stream = Regex("""file:\s*"(https?://[^"]+\.m3u8[^"]*)"""").find(page)?.groupValues?.get(1)
            ?: return

        callback.invoke(
            newExtractorLink(
                source = "Vidload",
                name = label,
                url = stream,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = "https://${URI(stream).host}/"
                this.quality = Qualities.Unknown.value
            }
        )

        Regex(""","file":"(https?://[^"]+\.vtt[^"]*)"""").findAll(page).forEach { match ->
            val url = match.groupValues[1]
            subtitleCallback.invoke(SubtitleFile(subtitleLabel(url), url))
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val html = app.get(data).text
        val match = Regex(
            """videoPlayerData\(JSON\.parse\('(.*?)'\),\s*'([^']*)'""",
            RegexOption.DOT_MATCHES_ALL
        ).find(html) ?: return false

        val payload = runCatching {
            ObjectMapper().readTree(jsUnescape(match.groupValues[1]))
        }.getOrNull() ?: return false

        var found = false
        payload.fieldNames().forEach { langKey ->
            val langLabel = languageLabel(langKey)
            payload.path(langKey).forEach { item ->
                val link = item.path("link").asText(null) ?: return@forEach
                val service = item.path("service_name").asText(null)
                val quality = item.path("quality").asText(null)
                val template = runCatching {
                    base64Decode(item.path("template").asText(""))
                }.getOrNull() ?: return@forEach
                val rawSrc = Regex("""data-src="([^"]+)"""").find(template)?.groupValues?.get(1)
                    ?: return@forEach
                val src = rawSrc.replace("{url}", link).let {
                    if (it.startsWith("//")) "https:$it" else it
                }
                val label = listOfNotNull(service, langLabel, quality)
                    .filter { it.isNotBlank() }
                    .joinToString(" - ")

                found = true
                if (src.contains("vidload")) {
                    resolveVidload(src, label, subtitleCallback, callback)
                } else {
                    loadExtractor(src, mainUrl, subtitleCallback, callback)
                }
            }
        }
        return found
    }
}
